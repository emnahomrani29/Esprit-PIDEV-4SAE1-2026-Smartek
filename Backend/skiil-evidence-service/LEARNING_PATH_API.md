# Learning Path API Documentation

## Entité LearningPath

### Champs
- `pathId` (Long) - ID auto-généré
- `title` (String) - Titre du parcours (obligatoire)
- `description` (String) - Description du parcours
- `learnerId` (Long) - ID de l'apprenant (obligatoire)
- `learnerName` (String) - Nom de l'apprenant (obligatoire)
- `status` (LearningPathStatus) - Statut du parcours (obligatoire)
- `startDate` (LocalDate) - Date de début (obligatoire)
- `endDate` (LocalDate) - Date de fin (optionnelle)
- `progress` (Integer) - Progrès en pourcentage 0-100 (obligatoire)

### Statuts disponibles
- `PLANIFIE` - Parcours planifié
- `EN_COURS` - Parcours en cours
- `TERMINE` - Parcours terminé
- `ABANDONNE` - Parcours abandonné

## Endpoints API

### 1. Créer un parcours
**POST** `/api/learning-paths`

**Body:**
```json
{
  "title": "Parcours Java Spring Boot",
  "description": "Apprendre Java et Spring Boot",
  "learnerId": 1,
  "learnerName": "John Doe",
  "status": "EN_COURS",
  "startDate": "2024-01-15",
  "endDate": "2024-06-15",
  "progress": 25
}
```

**Response:** 201 Created
```json
{
  "pathId": 1,
  "title": "Parcours Java Spring Boot",
  "description": "Apprendre Java et Spring Boot",
  "learnerId": 1,
  "learnerName": "John Doe",
  "status": "EN_COURS",
  "startDate": "2024-01-15",
  "endDate": "2024-06-15",
  "progress": 25
}
```

### 2. Récupérer tous les parcours d'un apprenant
**GET** `/api/learning-paths/learner/{learnerId}`

**Response:** 200 OK
```json
[
  {
    "pathId": 1,
    "title": "Parcours Java Spring Boot",
    "description": "Apprendre Java et Spring Boot",
    "learnerId": 1,
    "learnerName": "John Doe",
    "status": "EN_COURS",
    "startDate": "2024-01-15",
    "endDate": "2024-06-15",
    "progress": 25
  }
]
```

### 3. Récupérer tous les parcours (Admin)
**GET** `/api/learning-paths`

**Response:** 200 OK - Liste de tous les parcours

### 4. Récupérer un parcours par ID
**GET** `/api/learning-paths/{pathId}`

**Response:** 200 OK

### 5. Récupérer les parcours par statut
**GET** `/api/learning-paths/status/{status}`

**Exemples:**
- `/api/learning-paths/status/EN_COURS`
- `/api/learning-paths/status/TERMINE`

**Response:** 200 OK - Liste des parcours avec ce statut

### 6. Récupérer les parcours d'un apprenant par statut
**GET** `/api/learning-paths/learner/{learnerId}/status/{status}`

**Exemple:** `/api/learning-paths/learner/1/status/EN_COURS`

**Response:** 200 OK

### 7. Mettre à jour un parcours
**PUT** `/api/learning-paths/{pathId}`

**Body:**
```json
{
  "title": "Parcours Java Spring Boot - Avancé",
  "description": "Apprendre Java et Spring Boot niveau avancé",
  "learnerId": 1,
  "learnerName": "John Doe",
  "status": "EN_COURS",
  "startDate": "2024-01-15",
  "endDate": "2024-06-15",
  "progress": 50
}
```

**Response:** 200 OK

### 8. Supprimer un parcours
**DELETE** `/api/learning-paths/{pathId}`

**Response:** 204 No Content

## Validations

- Le titre est obligatoire
- L'ID de l'apprenant est obligatoire
- Le nom de l'apprenant est obligatoire
- Le statut est obligatoire
- La date de début est obligatoire
- Le progrès doit être entre 0 et 100
- Pas de doublons: un apprenant ne peut pas avoir deux parcours avec le même titre

## Erreurs courantes

### 400 Bad Request
- Champs obligatoires manquants
- Progrès hors de la plage 0-100
- Format de date invalide

### 404 Not Found
- Parcours non trouvé

### 500 Internal Server Error
- "Un parcours avec ce titre existe déjà pour cet apprenant"

## Test avec curl

### Créer un parcours
```bash
curl -X POST http://localhost:8089/api/learning-paths \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Parcours Java",
    "description": "Apprendre Java",
    "learnerId": 1,
    "learnerName": "Test User",
    "status": "EN_COURS",
    "startDate": "2024-01-15",
    "endDate": "2024-06-15",
    "progress": 0
  }'
```

### Récupérer les parcours d'un apprenant
```bash
curl http://localhost:8089/api/learning-paths/learner/1
```

### Mettre à jour le progrès
```bash
curl -X PUT http://localhost:8089/api/learning-paths/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Parcours Java",
    "description": "Apprendre Java",
    "learnerId": 1,
    "learnerName": "Test User",
    "status": "EN_COURS",
    "startDate": "2024-01-15",
    "endDate": "2024-06-15",
    "progress": 75
  }'
```

## Fichiers créés

### Backend
- ✅ `entity/LearningPath.java` - Entité JPA
- ✅ `entity/LearningPathStatus.java` - Enum des statuts
- ✅ `dto/LearningPathRequest.java` - DTO pour les requêtes
- ✅ `dto/LearningPathResponse.java` - DTO pour les réponses
- ✅ `repository/LearningPathRepository.java` - Repository JPA
- ✅ `service/LearningPathService.java` - Logique métier
- ✅ `controller/LearningPathController.java` - API REST

## Prochaines étapes

1. **Recompiler le backend:**
   ```bash
   cd Backend/skiil-evidence-service
   mvn clean install
   ```

2. **Redémarrer le service:**
   ```bash
   mvn spring-boot:run
   ```

3. **Vérifier la table dans MySQL:**
   ```sql
   USE skill_db;
   SHOW TABLES;
   DESCRIBE learning_paths;
   ```

4. **Tester l'API** avec Postman ou curl

La table `learning_paths` sera créée automatiquement au démarrage du service grâce à `ddl-auto: update`.
