# 🔄 Pipeline CI/CD - Vue d'ensemble

## 📊 Architecture du Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PIPELINE CI/CD                               │
└─────────────────────────────────────────────────────────────────────┘

┌──────────┐
│  GitHub  │  Déclencheur : Push sur main, Pull Request
└────┬─────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           JENKINS                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Stage 1: CHECKOUT                                                   │
│  ├─ Clone du repository                                             │
│  └─ Récupération du commit SHA                                      │
│                                                                       │
│  Stage 2: BUILD                                                      │
│  ├─ Maven compile                                                    │
│  └─ Vérification de la compilation                                  │
│                                                                       │
│  Stage 3: UNIT TESTS                                                │
│  ├─ Exécution des tests unitaires                                   │
│  ├─ Génération du rapport JaCoCo                                    │
│  └─ Publication des résultats                                       │
│                                                                       │
│  Stage 4: SONARQUBE ANALYSIS                    ┌──────────────┐   │
│  ├─ Analyse de qualité du code ────────────────▶│  SonarQube   │   │
│  ├─ Calcul de la couverture de code            └──────────────┘   │
│  └─ Détection des bugs et vulnérabilités                           │
│                                                                       │
│  Stage 5: QUALITY GATE                                              │
│  ├─ Vérification des seuils de qualité                             │
│  └─ Échec si Quality Gate non passé                                │
│                                                                       │
│  Stage 6: PACKAGE                                                    │
│  ├─- Maven package                                                   │
│  └─ Création du JAR                                                 │
│                                                                       │
│  Stage 7: PUBLISH TO NEXUS                      ┌──────────────┐   │
│  ├─ Upload de l'artefact Maven ────────────────▶│    Nexus     │   │
│  └─ Versioning automatique                      │   (Maven)    │   │
│                                                  └──────────────┘   │
│                                                                       │
│  Stage 8: BUILD DOCKER IMAGE                                        │
│  ├─ Construction de l'image Docker                                  │
│  ├─ Tag avec version + commit SHA                                   │
│  └─ Tag latest                                                       │
│                                                                       │
│  Stage 9: SECURITY SCAN                         ┌──────────────┐   │
│  ├─ Scan Trivy des vulnérabilités ─────────────▶│    Trivy     │   │
│  └─ Génération du rapport de sécurité          └──────────────┘   │
│                                                                       │
│  Stage 10: PUSH TO REGISTRY                     ┌──────────────┐   │
│  ├─ Push vers Nexus Docker Registry ───────────▶│    Nexus     │   │
│  └─ Images versionnées disponibles             │   (Docker)   │   │
│                                                  └──────────────┘   │
│                                                                       │
│  Stage 11: DEPLOY TO KUBERNETES                 ┌──────────────┐   │
│  ├─ Mise à jour du deployment ──────────────────▶│ Kubernetes   │   │
│  ├─ Rolling update                              │   Cluster    │   │
│  └─ Attente du rollout                          └──────────────┘   │
│                                                                       │
│  Stage 12: HEALTH CHECK                                             │
│  ├─ Vérification des pods                                           │
│  ├─ Test des endpoints health                                       │
│  └─ Validation du déploiement                                       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │   NOTIFICATION   │
                    │  (Email/Slack)   │
                    └──────────────────┘
```

## 🎯 Détails des Stages

### Stage 1: Checkout (30s)
- **Objectif** : Récupérer le code source
- **Actions** :
  - Clone du repository Git
  - Récupération du commit SHA court
  - Génération du build tag
- **Sortie** : Code source prêt pour le build

### Stage 2: Build (2-3 min)
- **Objectif** : Compiler le code source
- **Actions** :
  - `mvn clean compile`
  - Téléchargement des dépendances
  - Compilation Java
- **Sortie** : Classes compilées dans `target/classes`

### Stage 3: Unit Tests (1-2 min)
- **Objectif** : Exécuter les tests unitaires
- **Actions** :
  - `mvn test`
  - Exécution de tous les tests
  - Génération du rapport JaCoCo
- **Métriques** :
  - Nombre de tests exécutés
  - Taux de réussite
  - Couverture de code
- **Sortie** : Rapports JUnit et JaCoCo

### Stage 4: SonarQube Analysis (1-2 min)
- **Objectif** : Analyser la qualité du code
- **Actions** :
  - Analyse statique du code
  - Détection des bugs
  - Détection des vulnérabilités
  - Calcul de la dette technique
- **Métriques analysées** :
  - Bugs
  - Vulnérabilités
  - Code Smells
  - Couverture de code
  - Duplication de code
  - Complexité cyclomatique
- **Sortie** : Rapport SonarQube

### Stage 5: Quality Gate (30s)
- **Objectif** : Valider les seuils de qualité
- **Critères** :
  - Couverture de code > 80%
  - Bugs critiques = 0
  - Vulnérabilités = 0
  - Code Smells < 10
- **Action** : Échec du pipeline si non conforme

### Stage 6: Package (1-2 min)
- **Objectif** : Créer l'artefact déployable
- **Actions** :
  - `mvn package -DskipTests`
  - Création du JAR Spring Boot
- **Sortie** : `smartek-sponsor-0.0.1-SNAPSHOT.jar`

### Stage 7: Publish to Nexus (30s)
- **Objectif** : Publier l'artefact Maven
- **Actions** :
  - `mvn deploy`
  - Upload vers Nexus Maven Repository
- **Sortie** : Artefact disponible dans Nexus

### Stage 8: Build Docker Image (2-3 min)
- **Objectif** : Créer l'image Docker
- **Actions** :
  - Multi-stage build
  - Optimisation des layers
  - Tag avec version et latest
- **Tags créés** :
  - `smartek-sponsor:0.0.1-SNAPSHOT-abc123-42`
  - `smartek-sponsor:latest`
- **Sortie** : Image Docker prête

### Stage 9: Security Scan (1-2 min)
- **Objectif** : Scanner les vulnérabilités
- **Actions** :
  - Scan Trivy de l'image Docker
  - Détection des CVE
  - Analyse des dépendances
- **Niveaux** : HIGH, CRITICAL
- **Sortie** : Rapport JSON des vulnérabilités

### Stage 10: Push to Registry (1 min)
- **Objectif** : Publier l'image Docker
- **Actions** :
  - Push vers Nexus Docker Registry
  - Upload des layers
- **Sortie** : Image disponible dans le registry

### Stage 11: Deploy to Kubernetes (2-3 min)
- **Objectif** : Déployer sur K8s
- **Actions** :
  - `kubectl set image`
  - Rolling update du deployment
  - Attente du rollout complet
- **Stratégie** :
  - MaxSurge: 1
  - MaxUnavailable: 0
  - Zero-downtime deployment
- **Sortie** : Application déployée

### Stage 12: Health Check (30s)
- **Objectif** : Valider le déploiement
- **Actions** :
  - Vérification des pods Running
  - Test du endpoint `/actuator/health`
  - Validation des replicas
- **Sortie** : Déploiement validé

## ⏱️ Durée totale du pipeline

| Stage | Durée moyenne | Durée max |
|-------|---------------|-----------|
| Checkout | 30s | 1min |
| Build | 2min | 3min |
| Unit Tests | 1min | 2min |
| SonarQube | 1min | 2min |
| Quality Gate | 30s | 5min |
| Package | 1min | 2min |
| Publish Nexus | 30s | 1min |
| Build Docker | 2min | 3min |
| Security Scan | 1min | 2min |
| Push Registry | 1min | 2min |
| Deploy K8s | 2min | 3min |
| Health Check | 30s | 1min |
| **TOTAL** | **13min** | **27min** |

## 🔄 Déclencheurs du Pipeline

### Automatiques
1. **Push sur main** : Déploiement complet
2. **Pull Request** : Build + Tests + SonarQube (sans déploiement)
3. **Tag Git** : Release avec version spécifique

### Manuels
1. **Build Now** dans Jenkins
2. **Rebuild** d'un build précédent
3. **Parameterized Build** avec options

## 📊 Métriques et KPIs

### Métriques de qualité
- **Code Coverage** : > 80%
- **Bugs** : 0 critiques
- **Vulnerabilities** : 0
- **Code Smells** : < 10
- **Technical Debt** : < 1 jour

### Métriques de performance
- **Build Success Rate** : > 95%
- **Average Build Time** : < 15 min
- **Deployment Frequency** : Multiple par jour
- **Mean Time to Recovery** : < 10 min

### Métriques de sécurité
- **CVE Critical** : 0
- **CVE High** : < 5
- **Secrets Exposed** : 0
- **License Compliance** : 100%

## 🚨 Gestion des échecs

### Échec au Build
- **Cause** : Erreur de compilation
- **Action** : Notification immédiate
- **Rollback** : Non nécessaire

### Échec aux Tests
- **Cause** : Test unitaire échoué
- **Action** : Notification + logs détaillés
- **Rollback** : Non nécessaire

### Échec Quality Gate
- **Cause** : Seuils de qualité non atteints
- **Action** : Blocage du pipeline
- **Rollback** : Non nécessaire

### Échec au Déploiement
- **Cause** : Erreur K8s, image invalide
- **Action** : Rollback automatique
- **Rollback** : Vers version précédente

## 🔔 Notifications

### Email
- **Success** : Équipe de dev
- **Failure** : Équipe de dev + ops
- **Contenu** :
  - Status du build
  - Durée
  - Commit info
  - Lien vers les logs

### Slack (optionnel)
- **Channel** : #smartek-deployments
- **Format** : Message formaté avec couleurs
- **Mentions** : @channel pour les échecs

## 🔐 Sécurité du Pipeline

### Credentials
- Stockés dans Jenkins Credentials Store
- Chiffrés au repos
- Jamais exposés dans les logs

### Secrets K8s
- Stockés dans Kubernetes Secrets
- Base64 encodés
- Accès RBAC contrôlé

### Scan de sécurité
- Trivy pour les images Docker
- SonarQube pour le code
- Dependency-Check pour les dépendances

## 📈 Monitoring du Pipeline

### Jenkins
- **Dashboard** : Vue d'ensemble des builds
- **Blue Ocean** : Visualisation moderne
- **Metrics** : Durée, taux de succès

### Prometheus
- Métriques Jenkins exportées
- Alertes sur échecs répétés
- Dashboards Grafana

## 🔧 Optimisations

### Cache Maven
- `.m2/repository` en cache
- Réduction du temps de build de 50%

### Docker Layer Caching
- Réutilisation des layers
- Build incrémental

### Parallel Stages
- Tests et SonarQube en parallèle (optionnel)
- Réduction du temps total

## 📚 Ressources

### Documentation
- [Jenkinsfile](Jenkinsfile)
- [README.md](README.md)
- [DEPLOYMENT.md](DEPLOYMENT.md)

### Dashboards
- Jenkins: `http://jenkins:8080/job/smartek-sponsor-pipeline`
- SonarQube: `http://sonarqube:9000/dashboard?id=smartek-sponsor`
- Nexus: `http://nexus:8081`

## 🎓 Best Practices

1. **Commit fréquents** : Intégration continue
2. **Tests automatisés** : Couverture > 80%
3. **Code review** : Avant merge sur main
4. **Semantic versioning** : Versioning cohérent
5. **Documentation** : À jour avec le code
6. **Monitoring** : Surveillance continue
7. **Rollback plan** : Toujours prêt

## 📞 Support

Pour toute question sur le pipeline :
- Email: devops@smartek.com
- Slack: #smartek-cicd
- Documentation: [README.md](README.md)
