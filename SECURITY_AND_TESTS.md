# Sécurité et Tests — Projet SMARTEK (Sprint 2)

## Table des matières

1. [Architecture de sécurité JWT](#1-architecture-de-sécurité-jwt)
2. [Sécurité Backend — Spring Security](#2-sécurité-backend--spring-security)
3. [Sécurité Frontend — Intercepteur Angular](#3-sécurité-frontend--intercepteur-angular)
4. [Tests Backend — JUnit / Mockito](#4-tests-backend--junit--mockito)
5. [Tests Frontend — Karma / Jasmine](#5-tests-frontend--karma--jasmine)
6. [Lancer les tests](#6-lancer-les-tests)
7. [Résultats](#7-résultats)

---

## 1. Architecture de sécurité JWT

### Qu'est-ce que JWT ?

JWT (JSON Web Token) est un standard ouvert (RFC 7519) qui permet de transmettre des informations de manière sécurisée entre deux parties sous forme de token signé.

Un token JWT est composé de trois parties séparées par des points :

```
eyJhbGciOiJIUzI1NiJ9   ←  Header (algorithme)
.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlRSQUlORVIifQ  ←  Payload (données)
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ←  Signature
```

Le **payload** contient les informations de l'utilisateur :
- `sub` : email de l'utilisateur
- `userId` : identifiant unique
- `role` : rôle (TRAINER, LEARNER, ADMIN)
- `exp` : date d'expiration (24h)

### Flux d'authentification dans SMARTEK

```
┌─────────────┐     1. POST /api/auth/login      ┌──────────────┐
│   Frontend  │ ──────────────────────────────►  │ auth-service │
│  (Angular)  │ ◄──────────────────────────────  │  (port 8081) │
└─────────────┘     2. Retourne JWT token         └──────────────┘
       │
       │  3. Stocke le token dans localStorage
       │
       │  4. Chaque requête → Authorization: Bearer <token>
       ▼
┌─────────────┐     5. Valide le token            ┌──────────────────┐
│ API Gateway │ ──────────────────────────────►  │  Microservices   │
│ (port 8090) │                                   │ course / exam /  │
└─────────────┘                                   │    planning      │
                                                   └──────────────────┘
```

---

## 2. Sécurité Backend — Spring Security

### Structure du package security

Chaque microservice (`course-service`, `exam-service`, `planning-service`) possède un package `security/` identique :

```
src/main/java/com/smartek/<service>/security/
├── JwtService.java                 ← Valide et décode le token
├── JwtAuthenticationFilter.java    ← Filtre HTTP (intercepte chaque requête)
├── JwtAuthenticationEntryPoint.java ← Retourne 401 en JSON
├── UserDetailsImpl.java            ← Représente l'utilisateur authentifié
└── SecurityConfig.java             ← Règles d'accès par rôle
```

### JwtService — Validation du token

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret; // Même secret que l'auth-service

    // Valide que le token n'est pas expiré
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Extrait le rôle depuis le payload du token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
```

### JwtAuthenticationFilter — Filtre de sécurité

Ce filtre s'exécute **avant chaque requête HTTP** :

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        // 1. Lire le header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Vérifier le format "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire et valider le token
        String jwt = authHeader.substring(7);
        if (jwtService.validateToken(jwt)) {
            // 4. Placer l'utilisateur dans le SecurityContext
            String username = jwtService.extractUsername(jwt);
            String role = jwtService.extractRole(jwt);
            // → Spring Security connaît maintenant l'utilisateur et son rôle
        }

        filterChain.doFilter(request, response);
    }
}
```

### SecurityConfig — Règles de rôles

#### course-service

| Méthode | Endpoint | Rôle requis |
|---------|----------|-------------|
| `POST` | `/api/courses` | `TRAINER` |
| `PUT` | `/api/courses/**` | `TRAINER` |
| `DELETE` | `/api/courses/**` | `TRAINER` |
| `GET` | `/api/courses/**` | Authentifié |
| `POST` | `/api/courses/*/complete` | `LEARNER` |
| `GET` | `/api/courses/health` | Public |

#### exam-service

| Méthode | Endpoint | Rôle requis |
|---------|----------|-------------|
| `POST` | `/api/exams` | `TRAINER` |
| `PUT` | `/api/exams/**` | `TRAINER` |
| `DELETE` | `/api/exams/**` | `TRAINER` |
| `GET` | `/api/exams/**` | Authentifié |
| `POST` | `/api/exam-results/submit` | `LEARNER` |
| `GET` | `/api/analytics/**` | `TRAINER` |

#### planning-service

| Méthode | Endpoint | Rôle requis |
|---------|----------|-------------|
| `POST` | `/api/plannings` | `TRAINER` |
| `PUT` | `/api/plannings/**` | `TRAINER` |
| `DELETE` | `/api/plannings/**` | `TRAINER` |
| `POST` | `/api/plannings/*/publish` | `TRAINER` |
| `GET` | `/api/plannings/**` | `TRAINER` |
| `GET` | `/api/plannings/published` | Authentifié |

### Configuration application.yml

Chaque service partage le même secret JWT que l'auth-service :

```yaml
# application.yml de chaque microservice
jwt:
  secret: smartek-secret-key-for-jwt-token-generation-2024-very-secure
```

---

## 3. Sécurité Frontend — Intercepteur Angular

### AuthInterceptor

L'intercepteur Angular ajoute automatiquement le token JWT à **chaque requête HTTP** :

```typescript
// src/app/core/interceptors/auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = authService.getToken();

  // Ne pas ajouter le token sur login/register
  const isPublic = req.url.includes('/api/auth/login') ||
                   req.url.includes('/api/auth/register');

  if (token && !isPublic) {
    // Cloner la requête avec le header Authorization
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expiré → déconnexion automatique
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

### Stockage du token

```typescript
// Après login réussi
localStorage.setItem('token', response.token);
localStorage.setItem('userInfo', JSON.stringify(response));

// Récupération
getToken(): string | null {
  return localStorage.getItem('token');
}

// Vérification d'authentification
isAuthenticated(): boolean {
  return !!this.getToken();
}
```

### Guards de route

Les routes protégées utilisent des guards Angular :

```typescript
// app.routes.ts
{
  path: 'trainer/courses',
  loadComponent: () => import('./features/trainer/courses/...'),
  canActivate: [permissionGuard],
  data: { roles: [Role.TRAINER] }  // Seul le TRAINER peut accéder
}
```

---

## 4. Tests Backend — JUnit / Mockito

### Concepts utilisés

**JUnit 5** : framework de tests unitaires Java
**Mockito** : framework de mocking pour simuler les dépendances
**@WebMvcTest** : test d'intégration des contrôleurs Spring MVC
**MockMvc** : simule les requêtes HTTP sans démarrer le serveur

### Structure des tests

```
src/test/java/com/smartek/<service>/
├── service/
│   ├── CourseServiceTest.java        ← Tests unitaires du service
│   └── ExamResultServiceTest.java    ← Tests de la logique de correction
└── controller/
    └── CourseControllerTest.java     ← Tests d'intégration HTTP
```

### Tests unitaires de service

Les tests unitaires vérifient la **logique métier** en isolation totale :

```java
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;  // Simulé, pas de vraie BDD

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;  // Classe testée

    @Test
    @DisplayName("Doit lever DuplicateResourceException si le titre existe déjà")
    void shouldThrowExceptionWhenTitleAlreadyExists() {
        // GIVEN : un cours avec ce titre existe déjà
        when(courseRepository.findByTitle("Spring Boot"))
            .thenReturn(Optional.of(existingCourse));

        // WHEN + THEN : la création doit échouer
        assertThatThrownBy(() -> courseService.createCourse(request))
            .isInstanceOf(DuplicateResourceException.class);

        // Vérifier que save() n'a jamais été appelé
        verify(courseRepository, never()).save(any());
    }
}
```

### Tests d'intégration de contrôleur

Les tests de contrôleur vérifient les **règles de sécurité** et les **codes HTTP** :

```java
@WebMvcTest(
    value = CourseController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class  // Exclu pour les tests
    )
)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Test
    @DisplayName("Doit refuser la création avec le rôle LEARNER → 403")
    @WithMockUser(roles = "LEARNER")  // Simule un utilisateur LEARNER
    void shouldForbidCourseCreationAsLearner() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isForbidden());  // 403 attendu
    }

    @Test
    @DisplayName("Doit créer un cours avec le rôle TRAINER → 201")
    @WithMockUser(roles = "TRAINER")  // Simule un utilisateur TRAINER
    void shouldCreateCourseAsTrainer() throws Exception {
        when(courseService.createCourse(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())  // 201 attendu
            .andExpect(jsonPath("$.courseId").value(1L));
    }
}
```

### Tests de logique métier complexe — ExamResultService

```java
@Test
@DisplayName("Doit calculer le score correct quand toutes les réponses sont bonnes")
void shouldCalculatePerfectScoreWhenAllAnswersCorrect() {
    // Réponse correcte : option index 1
    ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
    answer.setQuestionId(1L);
    answer.setSelectedOptions(List.of(1));  // Index de la bonne réponse

    // ...setup...

    ExamResultResponse result = examResultService.submitExam(submission);

    assertThat(result.getObtainedMarks()).isEqualTo(10);
    assertThat(result.getPercentage()).isEqualTo(100.0);
    assertThat(result.getPassed()).isTrue();
}
```

### Tests de logique métier — PlanningBusinessService

```java
@Test
@DisplayName("Doit détecter un conflit de formateur sur le même créneau")
void shouldDetectTrainerConflict() {
    // Un planning existe déjà de 9h à 11h pour le trainer 10
    when(planningRepository.findPlanningsByDate(any()))
        .thenReturn(List.of(existingPlanning));

    ConflictCheckRequest request = new ConflictCheckRequest();
    request.setStartTime(LocalTime.of(9, 30));  // Chevauchement !
    request.setEndTime(LocalTime.of(10, 30));
    request.setTrainerId(10L);  // Même trainer

    ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

    assertThat(result.isHasConflict()).isTrue();
    assertThat(result.getConflicts().get(0).getType()).isEqualTo("TRAINER");
}
```

---

## 5. Tests Frontend — Karma / Jasmine

### Concepts utilisés

**Karma** : lanceur de tests Angular (exécute les tests dans Chrome)
**Jasmine** : framework de tests BDD (Behavior Driven Development)
**HttpClientTestingModule** : simule les appels HTTP
**HttpTestingController** : vérifie les requêtes HTTP attendues
**SpyObj** : mock d'un service Angular

### Tests de service

Les tests de service vérifient que les **bons appels HTTP** sont effectués :

```typescript
describe('PlanningService', () => {
  let service: PlanningService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PlanningService]
    });
    service = TestBed.inject(PlanningService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Vérifie qu'aucune requête non attendue n'est en attente
  });

  it('should publish a planning via POST and return PUBLISHED status', () => {
    const publishedPlanning = { ...mockPlanning, status: 'PUBLISHED' };

    service.publish(1).subscribe(planning => {
      expect(planning.status).toBe('PUBLISHED');  // Vérifier la réponse
    });

    // Vérifier que la bonne requête HTTP a été envoyée
    const req = httpMock.expectOne(`${apiUrl}/1/publish`);
    expect(req.request.method).toBe('POST');
    req.flush(publishedPlanning);  // Simuler la réponse du serveur
  });
});
```

### Tests de composant

Les tests de composant vérifient la **logique du composant** avec des services mockés :

```typescript
describe('LearnerExamsComponent', () => {
  let component: LearnerExamsComponent;
  let examServiceSpy: jasmine.SpyObj<ExamService>;

  beforeEach(async () => {
    // Créer un mock du service
    examServiceSpy = jasmine.createSpyObj('ExamService',
      ['getMyExams', 'startExam', 'retakeExam']
    );
    examServiceSpy.getMyExams.and.returnValue(of(mockExams));

    await TestBed.configureTestingModule({
      imports: [LearnerExamsComponent],
      providers: [
        { provide: ExamService, useValue: examServiceSpy }
      ]
    }).compileComponents();
  });

  it('should separate quizzes and exams by type', () => {
    expect(component.quizzes.length).toBe(1);  // 1 QUIZ
    expect(component.exams.length).toBe(1);    // 1 EXAM
  });

  it('should show alert when exam is locked', () => {
    spyOn(window, 'alert');
    component.startExam(lockedExam);
    expect(window.alert).toHaveBeenCalled();
    expect(examServiceSpy.startExam).not.toHaveBeenCalled();
  });

  it('should sort quizzes by name ascending', () => {
    component.sortBy = 'name';
    component.sortOrder = 'asc';
    component.applyFiltersAndSort();
    expect(component.filteredQuizzes[0].title).toBe('A Quiz');
  });
});
```

---

## 6. Lancer les tests

### Backend (Maven)

```bash
# Lancer tous les tests d'un service
cd Backend/course-service
mvn test

# Lancer tous les tests d'un service
cd Backend/exam-service
mvn test

# Lancer tous les tests d'un service
cd Backend/planning-service
mvn test
```

### Frontend (Angular / Karma)

```bash
cd Frontend/angular-app

# Lancer une seule fois (CI/CD)
npm test -- --watch=false --browsers=ChromeHeadless

# Lancer en mode watch (développement)
npm test
```

---

## 7. Résultats

### Backend — 101 tests ✅

| Service | Classe de test | Tests | Couverture |
|---------|---------------|-------|------------|
| course-service | `CourseServiceTest` | 12 | CRUD, doublons, stats |
| course-service | `CourseControllerTest` | 15 | Sécurité par rôle, HTTP codes |
| exam-service | `ExamServiceTest` | 13 | CRUD, calcul marks, cascade |
| exam-service | `ExamResultServiceTest` | 10 | Correction QCM, Vrai/Faux, score |
| exam-service | `ExamControllerTest` | 12 | Sécurité TRAINER/LEARNER |
| planning-service | `PlanningServiceTest` | 14 | CRUD, conflits, publication |
| planning-service | `PlanningBusinessServiceTest` | 11 | Conflits, charge, suggestions |
| planning-service | `PlanningControllerTest` | 14 | Sécurité, publication |
| **TOTAL** | | **101** | |

### Frontend — 253 tests ✅

| Fichier | Tests | Couverture |
|---------|-------|------------|
| `auth.service.spec.ts` | 10 | login, register, logout, token, localStorage |
| `course.service.spec.ts` | 9 | GET/POST/PUT/DELETE, completeCourse |
| `exam.service.spec.ts` | 14 | CRUD, submit, draft, start/pause/resume |
| `planning.service.spec.ts` | 11 | CRUD, publish/unpublish, publishWeek |
| `trainer-planning.component.spec.ts` | 20 | Calendrier, publication, modal, helpers |
| `learner-exams.component.spec.ts` | 18 | Chargement, filtrage, tri, startExam |
| `trainer-courses.component.spec.ts` | 20 | Chargement, filtrage, tri, chapitres |
| Autres (préexistants) | 151 | Autres modules du projet |
| **TOTAL** | **253** | |

---

## Glossaire

| Terme | Définition |
|-------|-----------|
| **JWT** | JSON Web Token — token signé contenant les informations de l'utilisateur |
| **Bearer Token** | Format d'envoi du JWT dans le header HTTP : `Authorization: Bearer <token>` |
| **Spring Security** | Framework Java pour sécuriser les applications Spring Boot |
| **SecurityFilterChain** | Chaîne de filtres qui s'exécutent sur chaque requête HTTP |
| **@WithMockUser** | Annotation de test qui simule un utilisateur authentifié avec un rôle |
| **Mockito** | Framework Java pour créer des objets simulés (mocks) dans les tests |
| **Jasmine** | Framework de tests BDD pour JavaScript/TypeScript |
| **Karma** | Lanceur de tests Angular qui exécute les specs dans un navigateur |
| **HttpTestingController** | Outil Angular pour intercepter et vérifier les requêtes HTTP dans les tests |
| **SpyObj** | Mock Jasmine qui enregistre les appels de méthodes pour les vérifier |
| **NO_ERRORS_SCHEMA** | Schéma Angular qui ignore les composants inconnus dans les tests |
| **TRAINER** | Rôle formateur — peut créer/modifier/supprimer les ressources |
| **LEARNER** | Rôle apprenant — peut lire et soumettre des réponses |
