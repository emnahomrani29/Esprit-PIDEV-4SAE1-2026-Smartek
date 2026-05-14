# SMARTEK – Project Overview

## Context
SMARTEK is a **PI (Projet Intégré)** developed by Esprit students (4SAE1 – 2026).  
It is a **microservices-based e-learning & job-offer platform** that combines:
- Online course management
- Training & exam management
- Event & planning management
- Job-offer publishing (offers-service)
- User authentication & authorization
- Skill-evidence & certification tracking

## Repository
- **URL:** `https://github.com/emnahomrani29/Esprit-PIDEV-4SAE1-2026-Smartek`
- **Active branch for offers-service:** `offers-service`
- **Other branches:** `main`, `develop`, `skill-evidence-learning-service`, `event/planning-service`

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Backend** | Spring Boot | 3.2.0 |
| **Backend** | Spring Cloud | 2023.0.0 |
| **Backend** | Java | 17 (Temurin) |
| **Backend** | Maven | 3.9.6 |
| **Frontend** | Angular | 18.2.x |
| **Frontend** | TypeScript | ~5.5.2 |
| **Frontend** | Tailwind CSS | 3.4.19 |
| **Database** | MySQL | 8.0 |
| **Service Discovery** | Netflix Eureka | 2023.0.0 |
| **Gateway** | Spring Cloud Gateway | 2023.0.0 |
| **Config Server** | Spring Cloud Config | 2023.0.0 |
| **Containerization** | Docker + Docker Compose | 3.8 |
| **CI/CD** | GitHub Actions | – |
| **Artifact Repo** | Nexus (planned/used) | – |
| **Monitoring** | Prometheus + Grafana (partial) | – |
| **Code Quality** | SonarQube (partial) | – |

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Angular Frontend                       │
│                   (localhost:4200 / nginx:80)                 │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                    API Gateway (8090)                         │
│              (Spring Cloud Gateway + JWT filter)              │
└────────────────────────────┬────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼──────┐  ┌──────────▼──────────┐  ┌─────▼──────┐
│ Eureka Server│  │   Config Server     │  │   MySQL    │
│   (8761)     │  │      (8888)         │  │  (3306)    │
└──────────────┘  └─────────────────────┘  └────────────┘
        │
        ├──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
        │          │          │          │          │          │          │          │
   ┌────▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐ ┌────▼───┐
   │ Auth   │ │ Course │ │Training│ │ Offers │ │  Exam  │ │ Event  │ │Planning│ │ ...    │
   │ 8081   │ │ 8086   │ │ 8084   │ │ 8085   │ │ 8087   │ │ 8082   │ │ 8083   │ │        │
   └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘
```

## Repository Structure

```
Esprit-PIDEV-4SAE1-2026-Smartek/
├── .github/workflows/           # GitHub Actions CI/CD
├── Backend/
│   ├── pom.xml                  # Maven parent POM
│   ├── api-gateway/
│   ├── auth-service/
│   ├── certification-badge-service/
│   ├── config-server/
│   ├── course-service/
│   ├── eureka-server/
│   ├── event-service/
│   ├── exam-service/
│   ├── learning/
│   ├── offers-service/
│   ├── planning-service/
│   ├── skiil-evidence-service/
│   └── training-service/
├── Frontend/
│   └── angular-app/
├── docker-compose.yml           # Full stack orchestration
├── package.json                 # Root npm scripts (convenience)
├── generate-report.js           # Technical report generator
├── git-bash-commands.sh         # Helper git script
└── SMARTEK-Rapport-Technique.pdf
```

## Quick Start (Local)

### Prerequisites
- JDK 17
- Maven 3.9+
- Node.js 18+
- Angular CLI 18+
- Docker & Docker Compose (optional)
- MySQL 8.0

### 1. Start Infrastructure
```bash
docker-compose up -d mysql eureka-server config-server
```

### 2. Start Backend Services
```bash
cd Backend
mvn clean install -DskipTests
# Then start each service individually (or use provided scripts)
```

### 3. Start Frontend
```bash
cd Frontend/angular-app
npm install
ng serve
```

### 4. Access Application
- Frontend: `http://localhost:4200`
- API Gateway: `http://localhost:8090`
- Eureka Dashboard: `http://localhost:8761`

## Notes
- The `learning` and `skiil-evidence-service` modules have **simpler Dockerfiles** (pre-built JAR copy) compared to the multi-stage Maven builds used by other services.
- No `init-db.sql` is currently present at the root; databases are created automatically via `createDatabaseIfNotExist=true` in JDBC URLs.
