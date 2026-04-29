# Backend Services - Smartek Platform

## 📋 Vue d'ensemble

Ce dossier contient tous les microservices backend de la plateforme Smartek.

## 🏗️ Architecture

```
Backend/
├── api-gateway/          # API Gateway (Spring Cloud Gateway)
├── auth-service/         # Service d'authentification
├── config-server/        # Configuration centralisée
├── eureka-server/        # Service Discovery
└── smartek_sponsor/      # Service de gestion des sponsors ⭐
```

## 🌟 Service Smartek Sponsor

Le service **smartek_sponsor** dispose d'un **pipeline CI/CD complet** avec :

- ✅ Jenkins Pipeline
- ✅ Docker & Docker Compose
- ✅ Kubernetes (kubeadm)
- ✅ Nexus Repository Manager
- ✅ SonarQube
- ✅ Prometheus & Grafana
- ✅ Auto-scaling (HPA)
- ✅ Documentation complète

### 🚀 Démarrage rapide

```bash
cd Backend/smartek_sponsor

# Voir la documentation complète
cat INDEX.md

# Démarrage rapide (5 minutes)
cat QUICK-START.md

# Build local
docker-compose up -d
```

### 📚 Documentation

Le service smartek_sponsor contient une documentation complète :

| Document | Description |
|----------|-------------|
| [INDEX.md](smartek_sponsor/INDEX.md) | Index de navigation |
| [QUICK-START.md](smartek_sponsor/QUICK-START.md) | Démarrage rapide (5 min) |
| [README.md](smartek_sponsor/README.md) | Documentation complète |
| [DEPLOYMENT.md](smartek_sponsor/DEPLOYMENT.md) | Guide de déploiement |
| [PIPELINE-OVERVIEW.md](smartek_sponsor/PIPELINE-OVERVIEW.md) | Architecture du pipeline |
| [WINDOWS-GUIDE.md](smartek_sponsor/WINDOWS-GUIDE.md) | Guide Windows |
| [SETUP-COMPLETE.md](smartek_sponsor/SETUP-COMPLETE.md) | Résumé de la configuration |

## 🔧 Services

### API Gateway
- **Port** : 8080
- **Description** : Point d'entrée unique pour tous les services
- **Tech** : Spring Cloud Gateway

### Auth Service
- **Port** : 8081
- **Description** : Authentification et autorisation
- **Tech** : Spring Security, JWT

### Config Server
- **Port** : 8888
- **Description** : Configuration centralisée
- **Tech** : Spring Cloud Config

### Eureka Server
- **Port** : 8761
- **Description** : Service Discovery
- **Tech** : Spring Cloud Netflix Eureka

### Smartek Sponsor ⭐
- **Port** : 8082
- **Description** : Gestion des sponsors et sponsorships
- **Tech** : Spring Boot, MySQL, Docker, Kubernetes
- **Pipeline** : Jenkins, Nexus, SonarQube, Prometheus, Grafana

## 🚀 Démarrage

### Prérequis

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- MySQL 8.0

### Démarrage local

```bash
# Démarrer tous les services avec Docker Compose
docker-compose up -d

# Ou démarrer individuellement
cd auth-service
mvn spring-boot:run

cd ../api-gateway
mvn spring-boot:run

cd ../smartek_sponsor
mvn spring-boot:run
```

## 📊 Monitoring

Le service smartek_sponsor inclut un monitoring complet :

- **Prometheus** : Collecte des métriques
- **Grafana** : Visualisation
- **Alertes** : 8 règles d'alertes configurées

```bash
# Accéder au monitoring
cd smartek_sponsor
docker-compose up -d

# Grafana : http://localhost:3000 (admin/admin)
# Prometheus : http://localhost:9090
```

## 🔄 CI/CD

Le service smartek_sponsor dispose d'un pipeline complet :

```
GitHub → Jenkins → Build → Tests → SonarQube → Docker → Nexus → Kubernetes
```

Voir [smartek_sponsor/PIPELINE-OVERVIEW.md](smartek_sponsor/PIPELINE-OVERVIEW.md) pour plus de détails.

## 📚 Documentation complète

Pour une documentation détaillée du service smartek_sponsor avec pipeline CI/CD :

```bash
cd Backend/smartek_sponsor
cat INDEX.md  # Commencer ici
```

## 🤝 Contribution

Pour contribuer au projet :

1. Fork le repository
2. Créer une branche : `git checkout -b feature/ma-feature`
3. Commit : `git commit -m "feat: ajout de ma feature"`
4. Push : `git push origin feature/ma-feature`
5. Créer une Pull Request

## 📞 Support

- **Email** : team@smartek.com
- **Slack** : #smartek-backend
- **Documentation** : Voir les README de chaque service

## 📄 License

Copyright © 2024 Smartek. All rights reserved.
