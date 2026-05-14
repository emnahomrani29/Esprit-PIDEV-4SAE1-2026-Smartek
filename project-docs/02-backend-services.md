# Backend Services Catalog

All backend services are **Spring Boot 3.2.0** applications using **Java 17**, built with **Maven**, and containerized with **Docker**.

---

## 1. Infrastructure Services

### Eureka Server (`eureka-server`)
| Property | Value |
|----------|-------|
| **Port** | 8761 |
| **Role** | Service Discovery |
| **Dependencies** | `spring-cloud-starter-netflix-eureka-server`, `spring-boot-starter-actuator` |
| **Dockerfile** | Multi-stage Maven build |

**Key notes:**
- All business services register themselves here.
- Health-check endpoint used by Docker Compose: `/actuator/health`

### Config Server (`config-server`)
| Property | Value |
|----------|-------|
| **Port** | 8888 |
| **Role** | Centralized external configuration |
| **Dockerfile** | Multi-stage Maven build |

**Key notes:**
- Depends on Eureka being healthy first.
- Not all services appear to actively use Config Client (check individual `bootstrap.yml`).

### API Gateway (`api-gateway`)
| Property | Value |
|----------|-------|
| **Port** | 8090 |
| **Role** | Edge gateway, routing, CORS, JWT validation |
| **Dependencies** | `spring-cloud-starter-gateway`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-actuator`, `jjwt-*` |
| **Dockerfile** | Multi-stage Maven build |

**Key notes:**
- Routes traffic to downstream services via `lb://<service-name>`.
- CORS configured for origins: `http://localhost:4200`, `http://frontend:80`.
- JWT filter validates tokens on secured paths.

---

## 2. Business Services

### Auth Service (`auth-service`)
| Property | Value |
|----------|-------|
| **Port** | 8081 |
| **Database** | `smartek_db` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Security, JWT, Eureka Client, Lombok |

**Features:**
- User registration (`POST /api/auth/register`)
- Login (`POST /api/auth/login`) → returns JWT
- Roles: `LEARNER`, `ADMIN`, `TRAINER`, `RH_COMPANY`, `RH_SMARTEK`, `PARTNER`
- Password hashing with BCrypt
- Token expiry: 24h

### Course Service (`course-service`)
| Property | Value |
|----------|-------|
| **Port** | 8086 |
| **Database** | `course_db` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

### Training Service (`training-service`)
| Property | Value |
|----------|-------|
| **Port** | 8084 |
| **Database** | `training_db` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

### Exam Service (`exam-service`)
| Property | Value |
|----------|-------|
| **Port** | 8087 |
| **Database** | `exam_db` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

### Event Service (`event-service`)
| Property | Value |
|----------|-------|
| **Port** | 8082 |
| **Database** | `smartek_events` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

### Planning Service (`planning-service`)
| Property | Value |
|----------|-------|
| **Port** | 8083 |
| **Database** | `smartek_planning` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

### Offers Service (`offers-service`)
| Property | Value |
|----------|-------|
| **Port** | 8085 |
| **Database** | `offers_db` (MySQL) |
| **Dependencies** | Web, Data JPA, Validation, Eureka Client |

**Features:**
- CRUD on job offers
- Filter by `companyId` and `status` (`ACTIVE`, `CLOSED`, `DRAFT`)
- Health check: `GET /api/offers/health`
- **Well-documented** (see `Backend/offers-service/README.md`)
- Has **TROUBLESHOOTING.md** for common issues
- **CI/CD pipeline exists** (`.github/workflows/offers-service-tests.yml`)

### Certification & Badge Service (`certification-badge-service`)
| Property | Value |
|----------|-------|
| **Port** | *unknown* |
| **Database** | *unknown* |

*(No Dockerfile found at time of writing; module exists in source tree.)*

### Learning (`learning`)
| Property | Value |
|----------|-------|
| **Port** | 8085 *(collides with offers-service!)* |
| **Dockerfile** | Simple JAR copy (not multi-stage) |
| **Note** | Port overlap with `offers-service` should be resolved before running both locally.

### Skill Evidence Service (`skiil-evidence-service`)
| Property | Value |
|----------|-------|
| **Port** | 8089 |
| **Dockerfile** | Simple JAR copy (not multi-stage) |

---

## 3. Common Patterns

### Parent POM (`Backend/pom.xml`)
- `groupId`: `com.smartek`
- `artifactId`: `smartek-parent`
- `version`: `1.0.0`
- Manages Spring Boot `3.2.0` and Spring Cloud `2023.0.0` BOMs.
- Modules listed: `eureka-server`, `config-server`, `api-gateway`, `auth-service`, `course-service`, `training-service`, `exam-service`, `offers-service`.
  - *Missing from parent:* `event-service`, `planning-service`, `learning`, `skiil-evidence-service`, `certification-badge-service`.

### Dockerfile Pattern (Standard Services)
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Application Properties Pattern
Each service typically configures:
- `server.port`
- `spring.datasource.url` (pointing to local MySQL for dev, or `mysql` host in Docker)
- `spring.datasource.username/password` (root/root)
- `eureka.client.service-url.defaultZone=http://localhost:8761/eureka`
- `spring.application.name`

---

## 4. Known Issues / Tech Debt

1. **Port collision:** `learning` and `offers-service` both claim port `8085`.
2. **Incomplete parent POM:** Not all backend modules are declared in `Backend/pom.xml`.
3. **Inconsistent Dockerfiles:** `learning` and `skiil-evidence-service` use a simpler, non-build Dockerfile.
4. **Missing `init-db.sql`:** Databases rely on `createDatabaseIfNotExist=true`; no explicit schema initialization script exists at the project root.
5. **Config Server adoption:** Verify whether all services actually load config from the Config Server or rely solely on local `application.yml`.
