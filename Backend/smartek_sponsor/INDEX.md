# 📚 Index de la Documentation - Smartek Sponsor Pipeline

## 🎯 Bienvenue !

Bienvenue dans la documentation complète du pipeline CI/CD du service Smartek Sponsor. Cette page vous aide à naviguer rapidement vers la documentation dont vous avez besoin.

## 🚀 Par où commencer ?

### Nouveau sur le projet ?
1. 📖 Commencez par [QUICK-START.md](QUICK-START.md) - Guide de démarrage en 5 minutes
2. 📘 Lisez [README.md](README.md) - Documentation complète du projet
3. 🔄 Consultez [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md) - Comprendre le pipeline

### Prêt à déployer ?
1. 📋 Suivez [DEPLOYMENT.md](DEPLOYMENT.md) - Guide de déploiement détaillé
2. ✅ Vérifiez [FILES-SUMMARY.md](FILES-SUMMARY.md) - Liste de tous les fichiers

### Utilisateur Windows ?
1. 🪟 Consultez [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md) - Guide spécifique Windows

## 📑 Documentation par catégorie

### 🎓 Guides de démarrage

| Document | Description | Niveau | Temps |
|----------|-------------|--------|-------|
| [QUICK-START.md](QUICK-START.md) | Démarrage rapide | Débutant | 5 min |
| [README.md](README.md) | Documentation complète | Tous | 30 min |
| [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md) | Guide Windows | Débutant | 15 min |

### 🚀 Déploiement

| Document | Description | Niveau | Temps |
|----------|-------------|--------|-------|
| [DEPLOYMENT.md](DEPLOYMENT.md) | Guide de déploiement complet | Intermédiaire | 45 min |
| [nexus-setup.md](nexus-setup.md) | Configuration Nexus | Intermédiaire | 30 min |

### 🔄 Pipeline CI/CD

| Document | Description | Niveau | Temps |
|----------|-------------|--------|-------|
| [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md) | Architecture du pipeline | Intermédiaire | 20 min |
| [Jenkinsfile](Jenkinsfile) | Pipeline Jenkins | Avancé | - |
| [.gitlab-ci.yml](.gitlab-ci.yml) | Pipeline GitLab | Avancé | - |

### 📊 Référence

| Document | Description | Niveau | Temps |
|----------|-------------|--------|-------|
| [FILES-SUMMARY.md](FILES-SUMMARY.md) | Liste de tous les fichiers | Tous | 10 min |
| [INDEX.md](INDEX.md) | Ce fichier | Tous | 5 min |

## 🗂️ Documentation par rôle

### 👨‍💻 Développeur

**Vous voulez :**
- Développer localement → [QUICK-START.md](QUICK-START.md) + [docker-compose.yml](docker-compose.yml)
- Comprendre le build → [README.md](README.md) + [pom.xml](pom.xml)
- Exécuter les tests → [scripts/local-build.sh](scripts/local-build.sh)
- Voir les métriques → [monitoring/grafana-dashboard.json](monitoring/grafana-dashboard.json)

**Commandes rapides :**
```bash
# Build local
./scripts/local-build.sh

# Environnement local
docker-compose up -d

# Tests
mvn test
```

### 🔧 DevOps / SRE

**Vous voulez :**
- Déployer sur K8s → [DEPLOYMENT.md](DEPLOYMENT.md)
- Configurer Jenkins → [scripts/setup-jenkins.sh](scripts/setup-jenkins.sh)
- Configurer Nexus → [nexus-setup.md](nexus-setup.md)
- Setup monitoring → [scripts/setup-monitoring.sh](scripts/setup-monitoring.sh)
- Comprendre le pipeline → [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md)

**Commandes rapides :**
```bash
# Déploiement
./scripts/deploy.sh

# Rollback
./scripts/rollback.sh

# Monitoring
./scripts/setup-monitoring.sh
```

### 👔 Manager / Lead

**Vous voulez :**
- Vue d'ensemble → [README.md](README.md)
- Architecture pipeline → [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md)
- Métriques et KPIs → [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md#métriques-et-kpis)
- Temps de déploiement → [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md#durée-totale-du-pipeline)

### 🪟 Utilisateur Windows

**Vous voulez :**
- Guide complet Windows → [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md)
- Scripts PowerShell → [scripts/*.ps1](scripts/)
- Configuration WSL2 → [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md#1-installer-wsl2-recommandé)

## 📁 Structure des fichiers

### 📚 Documentation (ce que vous lisez)

```
├── INDEX.md                    ← Vous êtes ici
├── README.md                   ← Documentation principale
├── QUICK-START.md              ← Démarrage rapide
├── DEPLOYMENT.md               ← Guide de déploiement
├── PIPELINE-OVERVIEW.md        ← Architecture pipeline
├── WINDOWS-GUIDE.md            ← Guide Windows
├── FILES-SUMMARY.md            ← Liste des fichiers
└── nexus-setup.md              ← Configuration Nexus
```

### 🐳 Docker

```
├── Dockerfile                  ← Image Docker
├── .dockerignore              ← Exclusions
└── docker-compose.yml         ← Environnement local
```

### 🔄 CI/CD

```
├── Jenkinsfile                ← Pipeline Jenkins
└── .gitlab-ci.yml            ← Pipeline GitLab
```

### ☸️ Kubernetes

```
k8s/
├── namespace.yaml             ← Namespace
├── deployment.yaml            ← Deployment
├── service.yaml               ← Service
├── ingress.yaml               ← Ingress
├── configmap.yaml             ← ConfigMap
├── secret.yaml                ← Secret
├── hpa.yaml                   ← Auto-scaling
├── servicemonitor.yaml        ← Prometheus
└── kustomization.yaml         ← Kustomize
```

### 📊 Monitoring

```
monitoring/
├── prometheus.yml             ← Config Prometheus
├── prometheus-rules.yaml      ← Alertes
└── grafana-dashboard.json     ← Dashboard
```

### 🔧 Scripts

```
scripts/
├── deploy.sh                  ← Déploiement (Linux)
├── rollback.sh                ← Rollback (Linux)
├── local-build.sh             ← Build local (Linux)
├── setup-jenkins.sh           ← Setup Jenkins
├── setup-monitoring.sh        ← Setup monitoring
├── deploy.ps1                 ← Déploiement (Windows)
├── rollback.ps1               ← Rollback (Windows)
└── local-build.ps1            ← Build local (Windows)
```

## 🔍 Recherche rapide

### Je veux...

#### ...démarrer rapidement
→ [QUICK-START.md](QUICK-START.md)

#### ...déployer en production
→ [DEPLOYMENT.md](DEPLOYMENT.md)

#### ...comprendre le pipeline
→ [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md)

#### ...configurer Jenkins
→ [scripts/setup-jenkins.sh](scripts/setup-jenkins.sh) + [README.md](README.md#3-configuration-jenkins)

#### ...configurer Nexus
→ [nexus-setup.md](nexus-setup.md)

#### ...voir les métriques
→ [monitoring/grafana-dashboard.json](monitoring/grafana-dashboard.json)

#### ...faire un rollback
→ [scripts/rollback.sh](scripts/rollback.sh) ou [DEPLOYMENT.md](DEPLOYMENT.md#rollback)

#### ...développer sur Windows
→ [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md)

#### ...build localement
→ [scripts/local-build.sh](scripts/local-build.sh) ou [QUICK-START.md](QUICK-START.md#4-build-local)

#### ...voir tous les fichiers
→ [FILES-SUMMARY.md](FILES-SUMMARY.md)

## 🎯 Parcours recommandés

### Parcours 1 : Développeur débutant (1 heure)

1. ✅ [QUICK-START.md](QUICK-START.md) - 5 min
2. ✅ Build local avec `docker-compose up -d` - 10 min
3. ✅ [README.md](README.md) sections "Vue d'ensemble" et "Tests locaux" - 20 min
4. ✅ Modifier le code et rebuild - 15 min
5. ✅ Explorer Grafana local - 10 min

### Parcours 2 : DevOps débutant (2 heures)

1. ✅ [README.md](README.md) - 30 min
2. ✅ [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md) - 20 min
3. ✅ [nexus-setup.md](nexus-setup.md) - 30 min
4. ✅ [DEPLOYMENT.md](DEPLOYMENT.md) sections "Prérequis" et "Étape 1-3" - 30 min
5. ✅ Test de déploiement local - 10 min

### Parcours 3 : Déploiement production (4 heures)

1. ✅ [DEPLOYMENT.md](DEPLOYMENT.md) complet - 45 min
2. ✅ Configuration Nexus - 30 min
3. ✅ Configuration Jenkins - 45 min
4. ✅ Configuration SonarQube - 30 min
5. ✅ Setup Kubernetes - 45 min
6. ✅ Setup Monitoring - 30 min
7. ✅ Premier déploiement - 15 min

### Parcours 4 : Windows (30 minutes)

1. ✅ [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md) - 15 min
2. ✅ Installation des outils - 10 min
3. ✅ Premier build avec PowerShell - 5 min

## 📊 Statistiques de la documentation

- **Nombre de documents** : 8 fichiers markdown
- **Pages totales** : ~100 pages
- **Temps de lecture total** : ~3 heures
- **Nombre de commandes** : ~200 commandes
- **Langues** : Français

## 🔗 Liens externes utiles

### Technologies utilisées

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework Java
- [Docker](https://docs.docker.com/) - Conteneurisation
- [Kubernetes](https://kubernetes.io/docs/) - Orchestration
- [Jenkins](https://www.jenkins.io/doc/) - CI/CD
- [Nexus](https://help.sonatype.com/repomanager3) - Repository Manager
- [SonarQube](https://docs.sonarqube.org/) - Qualité du code
- [Prometheus](https://prometheus.io/docs/) - Monitoring
- [Grafana](https://grafana.com/docs/) - Visualisation

### Tutoriels

- [Kubernetes Basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Maven Guide](https://maven.apache.org/guides/)

## 🆘 Besoin d'aide ?

### Support technique

- **Email** : team@smartek.com
- **Slack** : #smartek-sponsor
- **Issues** : GitHub Issues

### FAQ

**Q: Par où commencer ?**
A: Commencez par [QUICK-START.md](QUICK-START.md)

**Q: Comment déployer en production ?**
A: Suivez [DEPLOYMENT.md](DEPLOYMENT.md)

**Q: Je suis sur Windows, que faire ?**
A: Consultez [WINDOWS-GUIDE.md](WINDOWS-GUIDE.md)

**Q: Le pipeline échoue, que faire ?**
A: Voir [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md#gestion-des-échecs)

**Q: Comment faire un rollback ?**
A: Exécutez `./scripts/rollback.sh` ou voir [DEPLOYMENT.md](DEPLOYMENT.md#rollback)

## 📝 Contribuer à la documentation

Pour améliorer cette documentation :

1. Créer une branche : `git checkout -b doc/improve-readme`
2. Modifier les fichiers markdown
3. Commit : `git commit -m "docs: improve documentation"`
4. Push et créer une Pull Request

## 🎉 Conclusion

Cette documentation couvre tous les aspects du pipeline CI/CD du service Smartek Sponsor, de la configuration initiale au déploiement en production.

**Bon déploiement ! 🚀**

---

*Dernière mise à jour : 2024*
*Version : 1.0.0*
