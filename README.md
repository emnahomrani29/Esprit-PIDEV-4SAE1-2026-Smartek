# SMARTEK — Learning & Certification Platform

SMARTEK is a training management platform built with a Spring Boot microservices backend and an Angular 18 frontend. It handles user authentication, badge management, certification issuance, and exam-driven auto-awards.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Microservices — What Each One Actually Does](#4-microservices--what-each-one-actually-does)
5. [JWT Authentication Flow](#5-jwt-authentication-flow)
6. [Database Schema](#6-database-schema)
7. [Angular Frontend](#7-angular-frontend)
8. [Security Configuration](#8-security-configuration)
9. [Setup & Installation](#9-setup--installation)
10. [Environment Variables & Hardcoded Secrets](#10-environment-variables--hardcoded-secrets)
11. [What Works vs What Is Incomplete](#11-what-works-vs-what-is-incomplete)
12. [Honest Production Assessment](#12-honest-production-assessment)
13. [Advanced Features](#13-advanced-features)
8. [Security Configuration](#8-security-configuration)
9. [Setup & Installation](#9-setup--installation)
10. [Environment Variables & Hardcoded Secrets](#10-environment-variables--hardcoded-secrets)
11. [What Works vs What Is Incomplete](#11-what-works-vs-what-is-incomplete)
12. [Honest Production Assessment](#12-honest-production-assessment)

---

## 1. Project Overview

SMARTEK is a learning management system (LMS) for training organizations. It supports six user roles (LEARNER, TRAINER, ADMIN, RH_COMPANY, RH_SMARTEK, PARTNER) and provides:

- User registration and JWT-based login
- Badge template management and manual/bulk badge awarding
- Certification template management and manual/bulk certification awarding
- Automatic certification and badge awarding triggered by exam results
- Learner-facing pages to view earned badges and certifications
- PDF certificate generation in the browser

The backend is five independent Spring Boot services communicating through an API Gateway, with Eureka for service discovery. The frontend is a standalone Angular 18 SPA.

---

## 2. Tech Stack

**Backend** (all services use Spring Boot 3.2.0, Java 17, Maven 3.9+)

| Component | Technology | Version |
|---|---|---|
| Service framework | Spring Boot | 3.2.0 |
| Service discovery | Spring Cloud Netflix Eureka | 2023.0.0 |
| API routing | Spring Cloud Gateway | 2023.0.0 |
| Centralized config | Spring Cloud Config Server | 2023.0.0 |
| Authentication | Spring Security + JWT (jjwt) | jjwt 0.11.5 (auth-service), 0.12.3 (cert-service) |
| ORM | Spring Data JPA / Hibernate | Boot-managed |
| Database | MySQL 8.0+ | — |
| DB migrations | Flyway | Boot-managed (cert-service only) |
| Boilerplate | Lombok | 1.18.42 |
| Testing | JUnit 5, jqwik 1.8.2, H2 | — |

**Frontend**

| Component | Technology | Version |
|---|---|---|
| Framework | Angular | 18.2.0 |
| Language | TypeScript | 5.5.2 |
| Styling | Tailwind CSS + SCSS | 3.4.19 |
| HTTP | Angular HttpClient + interceptor | — |
| Routing | Angular Router with functional guards | — |
| PDF generation | jsPDF + html2canvas | 2.5.2 / 1.4.1 |
| Build | Angular CLI | 18.2.21 |

---

## 3. Project Structure

```
Smartek/
├── Backend/
│   ├── eureka-server/          # Service registry (port 8761)
│   ├── config-server/          # Centralized config (port 8888)
│   ├── api-gateway/            # Request routing + CORS (port 8084)
│   ├── auth-service/           # Users + JWT issuance (port 8081)
│   └── certification-badge-service/  # Badges + certifications (port 8083)
│
├── Frontend/
│   └── angular-app/
│       └── src/app/
│           ├── core/           # Services, guards, interceptors, models, enums, config
│           ├── features/       # auth, home, certifications-badges, documentation
│           └── shared/         # header, footer, pagination component
│
└── seed-users.sql              # Two test users (Formateur@smartek.com, Learner@smartek.com)
```

---

## 4. Microservices — What Each One Actually Does

### eureka-server (port 8761)

`EurekaServerApplication` annotated with `@EnableEurekaServer`. Runs a Netflix Eureka registry. Self-preservation is disabled (`enable-self-preservation: false`). Eviction interval is 4 seconds. No business logic — pure infrastructure.

### config-server (port 8888)

`@EnableConfigServer` with `spring.profiles.active: native`. Reads config from `classpath:/config`. Registers itself with Eureka. **In practice, all other services have `spring.cloud.config.enabled: false` in their own `application.yml`, so they never actually pull config from this server.** It runs but is not used.

### api-gateway (port 8084)

`ApiGatewayApplication` with `@EnableDiscoveryClient`. Pure configuration — no custom Java filters. Routes defined in `application.yml`:

- `Path=/api/auth/**` → load-balanced to `auth-service` via Eureka (`lb://auth-service`)
- `Path=/api/certifications-badges/**` → load-balanced to `certification-badge-service` (`lb://certification-badge-service`)

CORS is configured at the gateway level to allow `http://localhost:4200` only. **Note: the Angular frontend's `AuthService` and `BadgeService`/`CertificationService` hardcode direct service URLs (`localhost:8081` and `localhost:8083`) — they bypass the gateway entirely.**

### auth-service (port 8081)

Single controller: `AuthController` at `/api/auth`.

**Endpoints:**
- `POST /api/auth/register` — validates `RegisterRequest` (firstName, email, password ≥8 chars, role), BCrypt-hashes the password, saves a `User` entity, returns a JWT + user info
- `POST /api/auth/login` — authenticates via Spring's `AuthenticationManager`, returns a JWT + user info
- `GET /api/auth/health` — returns `"Auth Service is running"`
- `GET /api/auth/validate/{userId}` — returns `true`/`false` whether the user ID exists
- `GET /api/auth/user/{userId}` — returns user data (no password) for a given ID
- `GET /api/auth/hash/{password}` — **development utility** that returns a BCrypt hash of any plain-text password passed in the URL. This endpoint is public and should not exist in production.

`JwtService` generates tokens using `Jwts.builder()` (jjwt 0.11.5 API). The token subject is the user's email. Custom claims added: `role` (string) and `userId` (Long). Expiry is 86400000 ms (24 hours). The signing key is derived from `jwt.secret` via `Keys.hmacShaKeyFor(secret.getBytes())`.

`SecurityConfig` in auth-service: all `/api/auth/**` requests are `permitAll()`. Everything else requires authentication. No JWT filter is installed in auth-service — it only issues tokens, it does not validate them on incoming requests.

### certification-badge-service (port 8083)

The most complete service. Seven controllers, six services, four entities, four repositories.

**Controllers and their endpoints:**

`BadgeTemplateController` — `/api/certifications-badges/badge-templates`
- `GET /` — list all badge templates (no auth enforced at runtime — see Security section)
- `GET /paginated` — paginated list (page, size, sortBy, sortDirection params)
- `GET /{id}` — get by ID
- `POST /` — create; `@PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")` — **this annotation is active**
- `PUT /{id}` — update; `@PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")` — **active**
- `DELETE /{id}` — delete; `@PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")` — **active**

`CertificationTemplateController` — `/api/certifications-badges/certification-templates`
- `GET /`, `GET /paginated`, `GET /{id}` — open
- `POST /` — create; `@PreAuthorize` is **commented out** (`// @PreAuthorize(...)`)
- `PUT /{id}` — update; `@PreAuthorize` is **commented out**
- `DELETE /{id}` — delete; `@PreAuthorize` is **commented out**

`EarnedBadgeController` — `/api/certifications-badges/earned-badges`
- `POST /` — award badge to one learner; `@PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")` — **active**; `awardedBy` is extracted from JWT via `AuthorizationService.getCurrentUserId()`
- `POST /bulk` — award badge to multiple learners; `@PreAuthorize` — **active**; uses `REQUIRES_NEW` transactions per learner
- `GET /learner/{learnerId}` — get learner's badges; checks `authorizationService.canAccessLearnerData(learnerId)` — learners can only see their own
- `GET /learner/{learnerId}/paginated` — paginated version of above

`EarnedCertificationController` — `/api/certifications-badges/earned-certifications`
- `POST /` — award certification; **no `@PreAuthorize`** (missing)
- `POST /bulk` — bulk award; **no `@PreAuthorize`** (missing)
- `GET /learner/{learnerId}` — checks `canAccessLearnerData`
- `GET /learner/{learnerId}/paginated` — paginated
- `GET /{id}/details` — get full certification details by ID

`AutoAwardController` — `/api/certifications`
- `POST /auto-award` — validates `X-Internal-Api-Key` header via `InternalApiAuthService`, then calls `EarnedCertificationService.autoAwardCertification()`. The `awardedBy` is set to `0L` (system). Prevents duplicate awards.

`ExamIntegrationController` — `/api/certifications-badges/exam-integration`
- `POST /process-exam-result` — receives `ExamResultDTO` (learnerId, examId, percentage), awards certification if score ≥ 60%, awards the highest-threshold badge if score ≥ 60%. Certifications auto-expire after 2 years. **This endpoint is `permitAll()` in SecurityConfig** — no auth required.
- `GET /health` — health check

`StatisticsController` — `/api/certifications-badges/statistics`
- `GET /badges` — `@PreAuthorize("hasAnyRole('ADMIN', 'RH_COMPANY', 'RH_SMARTEK')")` — **active**
- `GET /certifications` — same role restriction — **active**
- `GET /learners/{learnerId}` — same role restriction — **active**

---

## 5. JWT Authentication Flow

### Token Generation (auth-service)

1. Client sends `POST /api/auth/login` with `{ email, password }`.
2. `AuthService.login()` calls `authenticationManager.authenticate()` which triggers `DaoAuthenticationProvider` → `UserDetailsService` → loads user by email from MySQL → BCrypt password comparison.
3. On success, `JwtService.generateToken(email, claims)` builds a token:
   - Subject: user's email
   - Custom claims: `role` (e.g. `"TRAINER"`), `userId` (Long)
   - Signed with HS256 using `Keys.hmacShaKeyFor(secret.getBytes())`
   - Expiry: 24 hours
4. Response: `AuthResponse` containing `token`, `userId`, `email`, `firstName`, `role`, `imageBase64`, `experience`.

### Token Storage (Angular)

`AuthService` in Angular stores the token in `localStorage` under the key `token`. User info is stored under `userInfo` as a JSON string. The `authInterceptor` (functional interceptor registered in `app.config.ts`) clones every outgoing HTTP request and adds `Authorization: Bearer <token>`.

**Important:** The interceptor catches 401/403 errors but the auto-logout call is **commented out** (`// authService.logout()`). Expired or invalid tokens will log an error to the console but will not redirect the user.

### Token Validation (certification-badge-service)

`JwtAuthenticationFilter` extends `OncePerRequestFilter`:
1. Reads `Authorization` header; skips if absent or not `Bearer `.
2. Calls `JwtService.validateToken(jwt)` — uses jjwt 0.12.3 API (`Jwts.parser().verifyWith(...)`).
3. Extracts `username` (email), `userId`, and `role` from claims.
4. Creates a `UserDetailsImpl` and sets a `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` with authority `ROLE_<role>`.
5. If the token is invalid or expired, the filter logs a warning and **continues the filter chain** — it does not block the request. The request proceeds unauthenticated.

### JWT Secret Mismatch Risk

auth-service uses jjwt `0.11.5` (`Jwts.parserBuilder()`). certification-badge-service uses jjwt `0.12.3` (`Jwts.parser()`). Both use the same secret string and HS256 algorithm, so tokens are cross-compatible at runtime. However, the two different API versions are a maintenance risk.

### Gateway and JWT

The API Gateway (`api-gateway`) does **not** validate JWT tokens. It only routes requests. JWT validation happens inside each downstream service.

---

## 6. Database Schema

Both services share the single `smartek_db` MySQL database. The schema is created by two mechanisms:

- `users` table: created by Hibernate `ddl-auto: update` in auth-service on first startup.
- All other tables: created by Flyway migrations in certification-badge-service (7 migration files, V1–V7).

**Note:** There are two V6 migration files (`V6__Add_exam_id_to_earned_certification.sql` and `V6__add_exam_integration_fields.sql`). Flyway will fail with a checksum conflict if both exist and the database has already run one of them. This is a real bug.

### Table: `users` (auth-service, Hibernate-managed)

| Column | Type | Notes |
|---|---|---|
| user_id | BIGINT PK AUTO_INCREMENT | |
| image | BLOB | nullable, profile photo |
| first_name | VARCHAR(50) NOT NULL | |
| email | VARCHAR(100) NOT NULL UNIQUE | |
| password | VARCHAR NOT NULL | BCrypt hash |
| phone | VARCHAR(20) | nullable |
| experience | INT DEFAULT 0 | |
| role | VARCHAR(20) NOT NULL | LEARNER, TRAINER, ADMIN, RH_COMPANY, RH_SMARTEK, PARTNER |

### Table: `badge_template` (V1 + V6 + V7)

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| name | VARCHAR(100) NOT NULL | |
| description | VARCHAR(1000) | nullable |
| exam_id | BIGINT | nullable; added in V7 (renamed from course_id) |
| minimum_score | DOUBLE DEFAULT 80.0 | added in V6; entity default is 60.0 — mismatch |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP | |

### Table: `certification_template` (V2 + V6 + V7)

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| title | VARCHAR(200) NOT NULL | |
| description | VARCHAR(1000) | nullable |
| exam_id | BIGINT | nullable; renamed from course_id in V7 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP | |

### Table: `earned_badge` (V3 + V5)

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| badge_template_id | BIGINT NOT NULL FK → badge_template | |
| learner_id | BIGINT NOT NULL | references users.user_id by convention only — no FK |
| award_date | DATE NOT NULL | |
| awarded_by | BIGINT NOT NULL | user_id of awarder; 0 = system |
| created_at | TIMESTAMP NOT NULL | |
| UNIQUE | (badge_template_id, learner_id) | prevents duplicate awards |

### Table: `earned_certification` (V4 + V5 + V6)

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| certification_template_id | BIGINT NOT NULL FK → certification_template | |
| learner_id | BIGINT NOT NULL | no FK to users |
| issue_date | DATE NOT NULL | |
| expiry_date | DATE | nullable; auto-set to +2 years for exam-triggered awards |
| certificate_url | VARCHAR(500) | nullable; no file storage backend |
| awarded_by | BIGINT NOT NULL | 0 = system |
| exam_id | VARCHAR(100) | nullable; added in V6 |
| created_at | TIMESTAMP NOT NULL | |

---

## 7. Angular Frontend

### Routing (`app.routes.ts`)

The dashboard layout and all non-certification routes have been removed. The only active routes are:

| Path | Component | Guard | Roles |
|---|---|---|---|
| `/` | `HomePageComponent` | none | Public |
| `/auth/sign-in` | `SignInComponent` | none | Public |
| `/auth/sign-up` | `SignUpComponent` | none | Public |
| `/certifications` | lazy → `CertificationsModule` | `authGuard` + `permissionGuard` | TRAINER, RH_SMARTEK, ADMIN |
| `/certifications/new` | `CertificationTemplateFormComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/certifications/edit/:id` | `CertificationTemplateFormComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/certifications/award` | `AwardCertificationComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/badges` | lazy → `BadgesModule` | `authGuard` + `permissionGuard` | TRAINER, RH_SMARTEK, ADMIN |
| `/badges/new` | `BadgeTemplateFormComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/badges/edit/:id` | `BadgeTemplateFormComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/badges/award` | `AwardBadgeComponent` | — (inherited) | TRAINER, RH_SMARTEK, ADMIN |
| `/my-certifications` | `MyCertificationsComponent` | `authGuard` + `permissionGuard` | LEARNER only |
| `/my-badges` | `MyBadgesComponent` | `authGuard` + `permissionGuard` | LEARNER only |
| `/certificate-viewer/:id` | `CertificateViewerComponent` | `authGuard` | Any authenticated user |
| `**` | redirect to `/` | — | — |

### Guards

`authGuard` — checks `authService.isAuthenticated()` (i.e. `!!localStorage.getItem('token')`). Does not validate the token against the backend. A user with an expired token will still pass this guard. On failure, redirects to `/auth/sign-in`.

`permissionGuard` — reads `route.data.roles` and `route.data.permissions`, checks against the role stored in `localStorage` via `PermissionService`. All permission logic is client-side only — it is not enforced by the backend for most endpoints. On failure, redirects to `/`.

### API Base URLs in Angular Services

All Angular services call backend services **directly**, bypassing the API Gateway:

- `AuthService`: `http://localhost:8081/api/auth`
- `BadgeService`: `http://localhost:8083/api/certifications-badges/badge-templates` and `.../earned-badges`
- `CertificationService`: `http://localhost:8083/api/certifications-badges/certification-templates` and `.../earned-certifications`

These URLs are hardcoded strings in the service files. There is no `environment.ts` being used for these values.

### Home Page

`HomePageComponent` composes: `HeroComponent`, `CompaniesComponent`, `CoursesComponent`, `MentorsComponent`, `TestimonialsComponent`, `NewsletterComponent`, `ContactComponent`. All data is static/hardcoded — no backend calls.

### Sign-Up Flow

`SignUpComponent` is a 3-step form:
1. firstName + email
2. password + confirmPassword (requires uppercase, lowercase, digit; min 8 chars)
3. phone, profile image (base64), experience, role selection

On submit, calls `AuthService.register()` → `POST /api/auth/register` on port 8081. On success, redirects to `/` (home).

### Sign-In Flow

`SignInComponent` calls `AuthService.login()` → `POST /api/auth/login` on port 8081. On success, the redirect is role-based:

- `LEARNER` → `/my-certifications`
- All other roles (TRAINER, ADMIN, RH_SMARTEK, RH_COMPANY, PARTNER) → `/certifications`

### Post-Login Navigation (Header)

The user avatar dropdown in `HeaderComponent` has a "My Certifications" button that calls `goToCertifications()`, which applies the same role-based logic: LEARNER → `/my-certifications`, others → `/certifications`.

### Certificate PDF Generation

`CertificateViewerComponent` loads a certification by ID via `GET /api/certifications-badges/earned-certifications/{id}/details`. It renders a certificate HTML element with ID `certificate-content`, then uses `html2canvas` to capture it and `jsPDF` in landscape A4 format to produce a downloadable PDF. The learner name is read from `localStorage`, not from the certification record itself. The back button navigates to `/my-certifications`.

---

## 8. Security Configuration

### auth-service (`SecurityConfig.java`)

```
/api/auth/**  → permitAll()
everything else → authenticated()
```

No JWT filter. CORS allows all origins (`addAllowedOriginPattern("*")`) with credentials. Sessions are stateless.

### certification-badge-service (`SecurityConfig.java`)

```java
.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
.requestMatchers("/api/certifications-badges/exam-integration/**").permitAll()
.anyRequest().permitAll()   // ← EVERYTHING IS OPEN
```

The last line is `.anyRequest().permitAll()`. This means **all endpoints in this service are publicly accessible without any token**. The `JwtAuthenticationFilter` still runs and will populate the `SecurityContext` if a valid token is present, which allows `@PreAuthorize` annotations to work when a token is provided. But without a token, any request still succeeds.

**Consequence:** The `@PreAuthorize` annotations on `BadgeTemplateController` (POST/PUT/DELETE) and `StatisticsController` only enforce roles when a token is present. Without a token, those endpoints are open. The `EarnedCertificationController` POST endpoints have no `@PreAuthorize` at all, so they are open regardless.

### What is actually protected end-to-end

| Endpoint | Token required? | Role enforced? |
|---|---|---|
| `POST /api/auth/register` | No | No |
| `POST /api/auth/login` | No | No |
| `GET /api/auth/hash/{password}` | No | No — **dangerous** |
| `POST /badge-templates` | No (permitAll) | Only if token present |
| `PUT /badge-templates/{id}` | No (permitAll) | Only if token present |
| `DELETE /badge-templates/{id}` | No (permitAll) | Only if token present |
| `POST /certification-templates` | No | No (`@PreAuthorize` commented out) |
| `PUT /certification-templates/{id}` | No | No (commented out) |
| `DELETE /certification-templates/{id}` | No | No (commented out) |
| `POST /earned-badges` | No (permitAll) | Only if token present |
| `POST /earned-certifications` | No | No (no annotation) |
| `GET /earned-badges/learner/{id}` | No | `canAccessLearnerData` only runs if authenticated |
| `POST /exam-integration/process-exam-result` | No | No |
| `GET /statistics/badges` | No (permitAll) | Only if token present |

---

## 9. Setup & Installation

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+ and npm
- Angular CLI 18: `npm install -g @angular/cli@18`
- MySQL 8.0+ running on port 3306

### 1. Database Setup

```sql
CREATE DATABASE smartek_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are created automatically on first service startup. To seed test users:

```sql
SOURCE Backend/auth-service/seed-users.sql;
```

This creates two accounts:

| Email | Password | Role |
|---|---|---|
| Formateur@smartek.com | Formateur123 | TRAINER |
| Learner@smartek.com | Learner123 | LEARNER |

### 2. Configure Database Credentials

Edit both `application.yml` files — `Backend/auth-service/src/main/resources/application.yml` and `Backend/certification-badge-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root
    password: your_mysql_password
```

### 3. Start Backend Services (in order)

```bash
# 1. Eureka Server
cd Backend/eureka-server && mvn spring-boot:run

# 2. Config Server
cd Backend/config-server && mvn spring-boot:run

# 3. Auth Service
cd Backend/auth-service && mvn spring-boot:run

# 4. Certification-Badge Service
cd Backend/certification-badge-service && mvn spring-boot:run

# 5. API Gateway
cd Backend/api-gateway && mvn spring-boot:run
```

Wait ~30 seconds for all services to register with Eureka. Verify at: http://localhost:8761

### 4. Start the Frontend

```bash
cd Frontend/angular-app
npm install
ng serve
```

App available at http://localhost:4200.

### Service URLs

| Service | Port | URL |
|---|---|---|
| Angular Frontend | 4200 | http://localhost:4200 |
| API Gateway | 8084 | http://localhost:8084 |
| Auth Service | 8081 | http://localhost:8081 |
| Certification-Badge Service | 8083 | http://localhost:8083 |
| Config Server | 8888 | http://localhost:8888 |
| Eureka Dashboard | 8761 | http://localhost:8761 |

---

## 10. Environment Variables & Hardcoded Secrets

The following values are hardcoded in `application.yml` files and must be externalized before any production deployment.

### auth-service

| Key | Hardcoded value | Risk |
|---|---|---|
| `spring.datasource.username` | `root` | Exposed in source |
| `spring.datasource.password` | _(empty)_ | No password set |
| `jwt.secret` | `smartek-secret-key-for-jwt-token-generation-2024-very-secure` | In version control |
| `jwt.expiration` | `86400000` | 24h, no refresh mechanism |

### certification-badge-service

| Key | Hardcoded value | Risk |
|---|---|---|
| `spring.datasource.username` | `root` | Exposed in source |
| `spring.datasource.password` | _(empty)_ | No password set |
| `jwt.secret` | same string as above | Must match auth-service |
| `internal.api-key` | `exam-service-dev-key` | In version control |

To externalize, update the YAML to use Spring's `${ENV_VAR:default}` syntax:

```yaml
jwt:
  secret: ${JWT_SECRET:smartek-secret-key-for-jwt-token-generation-2024-very-secure}
spring:
  datasource:
    password: ${DB_PASSWORD:}
```

---

## 11. What Works vs What Is Incomplete

### Working

- User registration and login with BCrypt + JWT
- Badge template CRUD (create, list, update, delete)
- Certification template CRUD
- Manual badge awarding (single and bulk) with duplicate prevention
- Manual certification awarding (single and bulk)
- Automatic certification + badge awarding via `POST /exam-integration/process-exam-result`
- Auto-award via internal API key (`POST /api/certifications/auto-award`)
- Learner view: paginated list of earned certifications (`/my-certifications`)
- Learner view: list of earned badges (`/my-badges`)
- Certificate PDF download (client-side, jsPDF + html2canvas)
- Statistics endpoints (badge counts, certification counts, per-learner stats)
- Eureka service discovery and API Gateway routing
- Role-based frontend guards and permission checks
- Paginated endpoints on all list operations

### Incomplete or Broken

- **Duplicate V6 Flyway migration** — `V6__Add_exam_id_to_earned_certification.sql` and `V6__add_exam_integration_fields.sql` both have version `V6`. Flyway will throw a checksum error on a fresh database. One must be renamed or removed.
- **Config Server unused** — all services have `spring.cloud.config.enabled: false`; the Config Server runs but serves nothing.
- **`@PreAuthorize` commented out** on all three `CertificationTemplateController` write endpoints (POST, PUT, DELETE) — anyone can create/modify/delete certification templates without a token.
- **`EarnedCertificationController` POST endpoints** have no `@PreAuthorize` — anyone can award certifications without authentication.
- **JWT filter does not block on invalid token** — `JwtAuthenticationFilter` catches exceptions and calls `filterChain.doFilter()` anyway, so a malformed token is silently ignored.
- **Auth interceptor auto-logout is commented out** — expired tokens on the frontend do not trigger a redirect to sign-in.
- **`GET /api/auth/hash/{password}`** is a public endpoint that BCrypt-hashes any password passed in the URL. Must be removed before production.
- **No token refresh** — tokens expire after 24 hours with no refresh endpoint; users must log in again.
- **Learner name in PDF** is read from `localStorage`, not from the certification record — if the user clears storage, the name will be blank.
- **`certificate_url` field** in `earned_certification` is stored as a string but there is no file storage backend wired up server-side.
- **`BadgeService.getActiveTemplates()`** calls `GET /badge-templates/active` — this endpoint does not exist in `BadgeTemplateController`. It will return 404.
- **`BadgeService.revokeBadge()`** and **`CertificationService.revokeCertification()`** call DELETE on earned badge/certification endpoints that do not exist in the controllers. They will return 404.
- **`BadgeService.bulkAwardBadge()`** posts to `/earned-badges/award/bulk` but the actual endpoint is `/earned-badges/bulk`. URL mismatch — will return 404.
- **`CertificationService.bulkAwardCertification()`** posts to `/earned-certifications/award/bulk` but the actual endpoint is `/earned-certifications/bulk`. Same mismatch.

---

## 12. Honest Production Assessment

**Do not deploy this as-is.** The following must be addressed first:

- Change `.anyRequest().permitAll()` to `.anyRequest().authenticated()` in `certification-badge-service/SecurityConfig.java` and uncomment all `@PreAuthorize` annotations
- Add `@PreAuthorize` to `EarnedCertificationController` POST endpoints
- Remove `GET /api/auth/hash/{password}` from `AuthController`
- Externalize all secrets via environment variables (JWT secret, DB password, internal API key)
- Fix or remove the duplicate V6 Flyway migration
- Fix the four URL mismatches in `BadgeService` and `CertificationService`
- Enable the auto-logout in `auth.interceptor.ts` when a 401/403 is received
- Replace hardcoded `localhost` URLs in Angular services with `environment.ts` configuration

---

## 13. Advanced Features

### Feature 1 — QR Code Verification System

Every `EarnedCertification` and `EarnedBadge` record is assigned a UUID `verificationCode` automatically on creation (`@PrePersist`). This code is used to generate a QR code on the frontend and to power a public verification endpoint.

**New Flyway migration:** `V8__add_verification_code.sql`
- Adds `verification_code VARCHAR(36)` with a unique index to both `earned_certification` and `earned_badge`.

**New backend endpoint (no JWT required):**

```
GET /api/certifications-badges/verify/{verificationCode}
```

Returns a `VerificationResponseDTO`:

| Field | Description |
|---|---|
| `type` | `"CERTIFICATION"` or `"BADGE"` |
| `title` | Certification title or badge name |
| `description` | Template description |
| `learnerId` | Learner's ID |
| `issueDate` | Date issued |
| `expiryDate` | Expiry date (null for badges) |
| `expired` | Boolean |
| `verificationCode` | The UUID |
| `issuer` | Always `"SMARTEK"` |

Returns `404` if the code is not found.

**Frontend:**
- `certificate-viewer` generates a QR code (using `qrcode` npm library) encoding `http://localhost:4200/verify/{verificationCode}`. The QR is rendered inside the certificate card and included in the PDF download.
- `my-badges` generates a QR code per badge card.
- New public route `/verify/:verificationCode` → `VerifyCertificateComponent` — no `authGuard`, accessible by anyone who scans the QR.

---

### Feature 2 — Digital Signature on Certificate PDF

**New Flyway migration:** `V9__add_signed_pdf_path.sql`
- Adds `signed_pdf_path VARCHAR(500)` to `earned_certification`.

**New dependencies in `certification-badge-service/pom.xml`:**
- `com.itextpdf:itext7-core:7.2.5` — PDF generation
- `com.itextpdf:sign:7.2.5` — PDF digital signing
- `org.bouncycastle:bcprov-jdk18on:1.77` — cryptographic provider
- `org.bouncycastle:bcpkix-jdk18on:1.77` — PKCS12 keystore support

**New services:**
- `PdfSigningService` — loads a PKCS12 keystore at startup, signs PDF bytes using SHA256withRSA. If the keystore is unavailable, signing is skipped gracefully and the unsigned PDF is returned.
- `CertificatePdfService` — builds a certificate PDF using iText 7 (A4 landscape), then passes it to `PdfSigningService`.

**Dev keystore:** `src/main/resources/smartek-keystore.p12` — self-signed, alias `smartek`, password `smartek-dev-2024`. Generated with `keytool`.

**New endpoint:**

```
GET /api/certifications-badges/earned-certifications/{id}/download?learnerName=John
```

Returns the signed PDF as `application/pdf` with `Content-Disposition: attachment`.

**PDF signing config (application.yml):**

```yaml
pdf:
  signing:
    keystore-path: ${PDF_KEYSTORE_PATH:classpath:smartek-keystore.p12}
    keystore-password: ${PDF_KEYSTORE_PASSWORD:smartek-dev-2024}
    key-alias: ${PDF_KEY_ALIAS:smartek}
```

---

### Feature 3 — Automatic Email Delivery

**New dependency:** `spring-boot-starter-mail`

**New service:** `EmailService` — all methods are `@Async` (enabled via `@EnableAsync` on the application class) so email sending never blocks the award transaction.

- `sendCertificationAwardEmail(cert, learnerName, toEmail)` — sends HTML email with the signed PDF attached.
- `sendBadgeAwardEmail(badge, learnerName, toEmail)` — sends HTML email with the verification link.

**Trigger:** Email is sent automatically inside `EarnedCertificationService.awardCertification()` and `EarnedBadgeService.awardBadge()` if the caller includes `learnerEmail` and `learnerName` in the request body. These fields are optional — if absent, no email is sent and the award still succeeds.

**Request body additions** (`AwardCertificationRequestDTO` / `AwardBadgeRequestDTO`):

```json
{
  "certificationTemplateId": 1,
  "learnerId": 42,
  "issueDate": "2026-04-10",
  "learnerEmail": "learner@example.com",
  "learnerName": "Jane Doe"
}
```

**Mail configuration (application.yml):**

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

If `MAIL_USERNAME` is empty, the service logs a warning and skips sending — the application starts and awards work normally without mail configured.

**Environment variables for production:**

| Variable | Description |
|---|---|
| `MAIL_HOST` | SMTP server (default: `smtp.gmail.com`) |
| `MAIL_PORT` | SMTP port (default: `587`) |
| `MAIL_USERNAME` | Sender email address |
| `MAIL_PASSWORD` | SMTP password or app password |
| `APP_BASE_URL` | Frontend base URL for verification links (default: `http://localhost:4200`) |

---

### Feature 4 — LinkedIn Sharing Integration

**Certificate Viewer (`/certificate-viewer/:id`):**

Two new buttons appear in the action bar:

- **Share on LinkedIn** — opens `https://www.linkedin.com/sharing/share-offsite/?url=<verification-url>` in a new tab. Pre-fills a LinkedIn post with the public verification URL.

- **Add to LinkedIn Profile** — opens the LinkedIn deep link:
  ```
  https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME
    &name=<cert-title>
    &organizationName=SMARTEK
    &issueYear=<year>
    &issueMonth=<month>
    &certUrl=<verification-url>
    &certId=<verificationCode>
  ```
  This pre-fills the "Add Certification" form on the learner's LinkedIn profile.

**My Badges (`/my-badges`):**

Each badge card has a **Share on LinkedIn** button using the same `share-offsite` URL with the badge's verification URL.

Both features require the `verificationCode` field to be present on the record. For existing records created before V8 migration, the code will be `null` and the buttons will not render (`*ngIf="badge.verificationCode"`).
