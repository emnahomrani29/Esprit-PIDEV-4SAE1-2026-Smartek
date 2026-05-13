# SMARTEK - Plateforme de Gestion de Formation et Certification

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-18.2.0-red.svg)](https://angular.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

## 📋 Table des matières

- [À propos du projet](#-à-propos-du-projet)
- [Architecture](#-architecture)
- [Technologies utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation et démarrage](#-installation-et-démarrage)
- [Microservices](#-microservices)
- [API Gateway](#-api-gateway)
- [Monitoring et DevOps](#-monitoring-et-devops)
- [Tests](#-tests)
- [Contribution](#-contribution)
- [Équipe](#-équipe)

---

## 🎯 À propos du projet

**SMARTEK** est une plateforme complète de gestion de formation, certification et recrutement développée dans le cadre du projet intégré de développement (PIDEV) à l'ESPRIT.

### Fonctionnalités principales

- 🔐 **Authentification et gestion des utilisateurs** (JWT, multi-rôles)
- 📚 **Gestion des cours et formations**
- 📝 **Système d'examens et évaluations**
- 🏆 **Certifications et badges numériques**
- 💼 **Gestion des offres d'emploi**
- 📅 **Planification des sessions de formation**
- 🎓 **Suivi de l'apprentissage et progression**
- 🤝 **Gestion des sponsors et partenaires**
- 📊 **Preuves de compétences (Skill Evidence)**
- 🎪 **Gestion des événements**

---

## 🏗️ Architecture

Le projet suit une **architecture microservices** avec les composants suivants :

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Angular 18)                     │
│                     Port: 4200 / 80                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   API Gateway (Spring Cloud)                 │
│                        Port: 8090                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Eureka Server (Service Discovery)               │
│                        Port: 8761                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Auth Service │  │Course Service│  │ Exam Service │
│  Port: 8081  │  │  Port: 8086  │  │  Port: 8087  │
└──────────────┘  └──────────────┘  └──────────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
                  ┌─────────────────┐
                  │  MySQL Database │
                  │    Port: 3306   │
                  └─────────────────┘
```

### Microservices disponibles

| Service | Port | Description | Base de données |
|---------|------|-------------|-----------------|
| **Eureka Server** | 8761 | Service Discovery | - |
| **Config Server** | 8888 | Configuration centralisée | - |
| **API Gateway** | 8090 | Point d'entrée unique | - |
| **Auth Service** | 8081 | Authentification JWT | smartek_db |
| **Event Service** | 8082 | Gestion des événements | smartek_events |
| **Certification Service** | 8083 | Certifications et badges | smartek_db |
| **Training Service** | 8084 | Gestion des formations | training_db |
| **Offers Service** | 8085 | Offres d'emploi | offers_db |
| **Course Service** | 8086 | Gestion des cours | course_db |
| **Exam Service** | 8087 | Examens et évaluations | exam_db |
| **Skill Evidence Service** | 8091 | Preuves de compétences | skill_evidence_db |
| **Learning Service** | 8092 | Suivi d'apprentissage | learning_db |
| **Sponsor Service** | 8093 | Gestion des sponsors | sponsor_db |
| **Planning Service** | 8094 | Planification | smartek_planning |

---

## 🛠️ Technologies utilisées

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.2.0** - Framework principal
- **Spring Cloud 2023.0.0** - Microservices (Eureka, Gateway, Config)
- **Spring Security** - Sécurité et authentification
- **Spring Data JPA** - Accès aux données
- **MySQL 8.0** - Base de données relationnelle
- **JWT** - Authentification stateless
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

### Frontend
- **Angular 18.2.0** - Framework frontend
- **TypeScript 5.5.2** - Langage typé
- **RxJS 7.8.0** - Programmation réactive
- **Tailwind CSS 3.4.19** - Framework CSS
- **html2canvas & jsPDF** - Génération de PDF

### DevOps & Monitoring
- **Docker & Docker Compose** - Conteneurisation
- **Jenkins** - CI/CD (Port 9080)
- **SonarQube** - Qualité du code (Port 9000)
- **Nexus** - Gestionnaire d'artefacts (Port 8089)
- **Prometheus** - Collecte de métriques (Port 9090)
- **Grafana** - Visualisation (Port 3000)

---

## 📦 Prérequis

### Pour le développement local

- **Java JDK 17** ou supérieur
- **Node.js 18+** et **npm**
- **Maven 3.6+**
- **MySQL 8.0**
- **Git**

### Pour le déploiement Docker

- **Docker** 20.10+
- **Docker Compose** 2.0+

---

## 🚀 Installation et démarrage

### Option 1 : Démarrage avec Docker Compose (Recommandé)

Cette méthode démarre l'ensemble de la plateforme avec tous les services.

```bash
# Cloner le repository
git clone <repository-url>
cd smartek-platform

# Démarrer tous les services
docker-compose up -d

# Vérifier le statut des services
docker-compose ps

# Voir les logs
docker-compose logs -f [service-name]

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

**Temps de démarrage estimé** : 3-5 minutes

**URLs d'accès** :
- Frontend : http://localhost:4200
- API Gateway : http://localhost:8090
- Eureka Dashboard : http://localhost:8761
- Grafana : http://localhost:3000 (admin/admin)
- Prometheus : http://localhost:9090
- SonarQube : http://localhost:9000 (admin/admin)
- Jenkins : http://localhost:9080
- Nexus : http://localhost:8089

### Option 2 : Démarrage manuel (Développement)

#### 1. Démarrer MySQL

```bash
# Créer la base de données
mysql -u root -p
CREATE DATABASE smartek_db;
CREATE DATABASE training_db;
CREATE DATABASE offers_db;
CREATE DATABASE course_db;
CREATE DATABASE exam_db;
CREATE DATABASE smartek_events;
CREATE DATABASE smartek_planning;
CREATE DATABASE learning_db;
CREATE DATABASE skill_evidence_db;
CREATE DATABASE sponsor_db;
```

#### 2. Démarrer les services Backend (dans l'ordre)

```bash
# 1. Eureka Server
cd Backend/eureka-server
mvn spring-boot:run

# 2. Config Server (optionnel)
cd Backend/config-server
mvn spring-boot:run

# 3. Auth Service
cd Backend/auth-service
mvn spring-boot:run

# 4. Autres microservices (en parallèle)
cd Backend/course-service && mvn spring-boot:run
cd Backend/training-service && mvn spring-boot:run
cd Backend/exam-service && mvn spring-boot:run
cd Backend/offers-service && mvn spring-boot:run
# ... etc

# 5. API Gateway (en dernier)
cd Backend/api-gateway
mvn spring-boot:run
```

#### 3. Démarrer le Frontend

```bash
cd Frontend/angular-app

# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm start

# L'application sera accessible sur http://localhost:4200
```

### Option 3 : Build et déploiement en production

```bash
# Build tous les services Backend
cd Backend
mvn clean install

# Build le Frontend
cd Frontend/angular-app
npm install
npm run build

# Les artefacts seront dans :
# - Backend/*/target/*.jar
# - Frontend/angular-app/dist/
```

---

## 🔐 Microservices

### Auth Service (Port 8081)

Service d'authentification et gestion des utilisateurs.

**Rôles disponibles** :
- `LEARNER` - Apprenant
- `TRAINER` - Formateur
- `ADMIN` - Administrateur
- `RH_COMPANY` - RH Entreprise
- `RH_SMARTEK` - RH SMARTEK
- `PARTNER` - Partenaire

**Endpoints principaux** :
```http
POST /api/auth/register - Inscription
POST /api/auth/login - Connexion
GET /api/auth/health - Health check
```

**Exemple de requête** :
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@smartek.com",
    "password": "password123"
  }'
```

### Course Service (Port 8086)

Gestion des cours et contenus pédagogiques.

### Training Service (Port 8084)

Gestion des sessions de formation et inscriptions.

### Exam Service (Port 8087)

Création et gestion des examens et évaluations.

### Certification & Badge Service (Port 8083)

Émission et gestion des certifications et badges numériques.

**Fonctionnalités** :
- Création de templates de badges/certifications
- Attribution automatique ou manuelle
- Vérification de certificats
- Génération de PDF
- Statistiques

### Offers Service (Port 8085)

Gestion des offres d'emploi.

**Endpoints** :
```http
GET /api/offers - Liste des offres
POST /api/offers - Créer une offre
GET /api/offers/{id} - Détails d'une offre
PUT /api/offers/{id} - Modifier une offre
DELETE /api/offers/{id} - Supprimer une offre
GET /api/offers/company/{companyId} - Offres par entreprise
GET /api/offers/status/{status} - Offres par statut (ACTIVE, CLOSED, DRAFT)
```

### Event Service (Port 8082)

Gestion des événements (webinaires, conférences, ateliers).

### Planning Service (Port 8094)

Planification des sessions et gestion des calendriers.

### Learning Service (Port 8092)

Suivi de la progression et des parcours d'apprentissage.

### Skill Evidence Service (Port 8091)

Gestion des preuves de compétences et portfolios.

### Sponsor Service (Port 8093)

Gestion des sponsors et partenaires de la plateforme.

---

## 🌐 API Gateway

L'API Gateway (port 8090) est le point d'entrée unique pour tous les services.

### Configuration CORS

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedMethods: "*"
            allowedHeaders: "*"
```

### Routes configurées

Toutes les requêtes sont routées via le pattern :
```
http://localhost:8090/api/{service-name}/**
```

Exemples :
- `http://localhost:8090/api/auth/login`
- `http://localhost:8090/api/courses`
- `http://localhost:8090/api/offers`

---

## 📊 Monitoring et DevOps

### Prometheus (Port 9090)

Collecte automatique des métriques de tous les microservices.

**Accès** : http://localhost:9090

### Grafana (Port 3000)

Dashboards de visualisation des métriques.

**Accès** : http://localhost:3000
- Username : `admin`
- Password : `admin`

**Dashboards disponibles** :
- Vue d'ensemble de la plateforme SMARTEK
- Métriques par microservice
- Performance de la base de données
- Utilisation des ressources

### SonarQube (Port 9000)

Analyse de la qualité du code.

**Accès** : http://localhost:9000
- Username : `admin`
- Password : `admin`

**Analyse d'un projet** :
```bash
cd Backend/[service-name]
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

### Jenkins (Port 9080)

Pipeline CI/CD pour l'intégration et le déploiement continus.

**Accès** : http://localhost:9080

**Pipelines configurés** :
- Build et tests automatisés
- Analyse SonarQube
- Déploiement sur Nexus
- Déploiement Docker

### Nexus (Port 8089)

Gestionnaire d'artefacts Maven et Docker Registry.

**Accès** : http://localhost:8089

---

## 🧪 Tests

### Tests Backend

```bash
# Exécuter tous les tests d'un service
cd Backend/[service-name]
mvn test

# Tests avec couverture
mvn clean test jacoco:report

# Tests d'intégration
mvn verify
```

### Tests Frontend

```bash
cd Frontend/angular-app

# Tests unitaires
npm test

# Tests avec couverture
npm test -- --code-coverage

# Tests e2e
npm run e2e
```

### Tests avec Property-Based Testing (jqwik)

Le service de certification utilise des tests basés sur les propriétés :

```bash
cd Backend/certification-badge-service
mvn test -Dtest=*PropertyTest
```

---

## 📁 Structure du projet

```
smartek-platform/
├── Backend/
│   ├── api-gateway/           # API Gateway (8090)
│   ├── auth-service/          # Service d'authentification (8081)
│   ├── certification-badge-service/  # Certifications (8083)
│   ├── config-server/         # Configuration centralisée (8888)
│   ├── course-service/        # Gestion des cours (8086)
│   ├── eureka-server/         # Service Discovery (8761)
│   ├── event-service/         # Gestion des événements (8082)
│   ├── exam-service/          # Examens (8087)
│   ├── learning/              # Suivi d'apprentissage (8092)
│   ├── offers-service/        # Offres d'emploi (8085)
│   ├── planning-service/      # Planification (8094)
│   ├── skiil-evidence-service/  # Preuves de compétences (8091)
│   ├── smartek_sponsor/       # Sponsors (8093)
│   ├── training-service/      # Formations (8084)
│   └── pom.xml               # POM parent Maven
├── Frontend/
│   └── angular-app/          # Application Angular (4200)
├── monitoring/
│   ├── prometheus.yml        # Configuration Prometheus
│   └── grafana/              # Dashboards Grafana
├── .github/
│   └── workflows/            # GitHub Actions CI/CD
├── docker-compose.yml        # Orchestration Docker
├── init-db.sql              # Script d'initialisation DB
└── README.md                # Ce fichier
```

---

## 🔧 Configuration

### Variables d'environnement

Les services peuvent être configurés via des variables d'environnement :

```bash
# Base de données
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smartek_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Fichiers de configuration

Chaque service possède son fichier `application.yml` dans :
```
Backend/[service-name]/src/main/resources/application.yml
```

---

## 🐛 Dépannage

### Problème : Service ne démarre pas

```bash
# Vérifier les logs
docker-compose logs [service-name]

# Redémarrer un service spécifique
docker-compose restart [service-name]
```

### Problème : Base de données inaccessible

```bash
# Vérifier que MySQL est démarré
docker-compose ps mysql

# Recréer la base de données
docker-compose down -v
docker-compose up -d mysql
```

### Problème : Port déjà utilisé

```bash
# Trouver le processus utilisant le port
netstat -ano | findstr :8080

# Arrêter le processus ou changer le port dans docker-compose.yml
```

---

## 📚 Documentation supplémentaire

- [Guide d'authentification](Backend/auth-service/README.md)
- [API des offres d'emploi](Backend/offers-service/README.md)
- [Système de certification](Backend/certification-badge-service/README.md)
- [Tests et validation](Backend/certification-badge-service/TESTING-SUMMARY.md)

---

## 🤝 Contribution

### Workflow Git

```bash
# Créer une branche pour votre fonctionnalité
git checkout -b feature/ma-fonctionnalite

# Faire vos modifications et commits
git add .
git commit -m "feat: description de la fonctionnalité"

# Pousser vers le repository
git push origin feature/ma-fonctionnalite

# Créer une Pull Request sur GitHub/GitLab
```

### Conventions de commit

Nous suivons la convention [Conventional Commits](https://www.conventionalcommits.org/) :

- `feat:` - Nouvelle fonctionnalité
- `fix:` - Correction de bug
- `docs:` - Documentation
- `style:` - Formatage du code
- `refactor:` - Refactoring
- `test:` - Ajout de tests
- `chore:` - Tâches de maintenance

---

## 👥 Équipe

**Projet PIDEV - ESPRIT**
- **Classe** : 4SAE1
- **Année académique** : 2025-2026
- **Sujet** : 4

### Membres de l'équipe

- [Nom Prénom] - Développeur Backend
- [Nom Prénom] - Développeur Frontend
- [Nom Prénom] - DevOps
- [Nom Prénom] - Architecte

### Encadrement

- **Encadrant académique** : [Nom de l'enseignant]
- **Encadrant professionnel** : [Nom de l'encadrant]

---

## 📄 Licence

Ce projet est développé dans le cadre académique à l'ESPRIT.

---

## 📞 Contact

Pour toute question ou suggestion :

- **Email** : contact@smartek.tn
- **GitHub** : [Repository URL]
- **Documentation** : [Wiki URL]

---

## 🎓 Remerciements

Nous tenons à remercier :
- L'école ESPRIT pour l'encadrement et les ressources
- Nos encadrants pour leur accompagnement
- La communauté open source pour les outils utilisés

---

**Développé avec ❤️ par l'équipe SMARTEK - ESPRIT 2025-2026**
