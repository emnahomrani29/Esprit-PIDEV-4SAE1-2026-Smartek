# 🚀 Jenkins CI/CD Configuration - Smartek ESPRIT

## 📋 Table des matières
1. [Installation Jenkins](#installation-jenkins)
2. [Configuration des Webhooks](#configuration-des-webhooks)
3. [Configuration des Credentials](#configuration-des-credentials)
4. [Utilisation des Pipelines](#utilisation-des-pipelines)
5. [Troubleshooting](#troubleshooting)

---

## 🔧 Installation Jenkins

### Prérequis
- Docker et Docker Compose installés
- Port 8090 disponible (Jenkins UI)
- Port 9000 disponible (SonarQube - optionnel)

### Étape 1 : Démarrer Jenkins

```bash
cd jenkins
docker-compose up -d
```

### Étape 2 : Accéder à Jenkins

1. Ouvrir le navigateur : `http://localhost:8090`
2. Credentials par défaut :
   - **Username**: `admin`
   - **Password**: `admin123`

⚠️ **IMPORTANT** : Changez le mot de passe après la première connexion !

### Étape 3 : Installer les plugins requis

Jenkins est configuré avec **Configuration as Code (JCasC)**, mais vous devez installer ces plugins :

1. Aller dans **Manage Jenkins** → **Manage Plugins**
2. Installer les plugins suivants :
   - **Git Plugin**
   - **GitHub Plugin**
   - **Pipeline Plugin**
   - **Docker Pipeline Plugin**
   - **SonarQube Scanner Plugin**
   - **JaCoCo Plugin**
   - **NodeJS Plugin**
   - **Maven Integration Plugin**
   - **Credentials Binding Plugin**
   - **Configuration as Code Plugin**

3. Redémarrer Jenkins : `docker-compose restart jenkins`

---

## 🔗 Configuration des Webhooks

### Option 1 : GitHub Webhooks (Recommandé)

#### A. Générer un token GitHub

1. Aller sur GitHub → **Settings** → **Developer settings** → **Personal access tokens**
2. Créer un nouveau token avec les permissions :
   - `repo` (Full control)
   - `admin:repo_hook` (Write)
3. Copier le token généré

#### B. Configurer le Webhook dans GitHub

1. Aller dans votre repository GitHub
2. **Settings** → **Webhooks** → **Add webhook**
3. Configurer :
   ```
   Payload URL: http://VOTRE_IP_JENKINS:8090/github-webhook/
   Content type: application/json
   Secret: (laisser vide ou créer un secret)
   Events: Just the push event
   Active: ✓
   ```
4. Cliquer sur **Add webhook**

#### C. Tester le Webhook

```bash
# Faire un push sur votre repo
git add .
git commit -m "Test Jenkins webhook"
git push origin main
```

Le pipeline Jenkins devrait se déclencher automatiquement ! 🎉

### Option 2 : GitLab Webhooks

Si vous utilisez GitLab :

1. Aller dans **Settings** → **Webhooks**
2. URL : `http://VOTRE_IP_JENKINS:8090/project/smartek-backend`
3. Trigger : **Push events**
4. Cliquer sur **Add webhook**

### Option 3 : Polling SCM (Fallback)

Si les webhooks ne fonctionnent pas (firewall, réseau local) :

1. Éditer le `Jenkinsfile.backend` ou `Jenkinsfile.frontend`
2. Remplacer :
   ```groovy
   triggers {
       githubPush()
   }
   ```
   Par :
   ```groovy
   triggers {
       pollSCM('H/5 * * * *')  // Vérifie toutes les 5 minutes
   }
   ```

---

## 🔐 Configuration des Credentials

### 1. SonarCloud Token

```bash
# Dans Jenkins UI
Manage Jenkins → Manage Credentials → (global) → Add Credentials

Kind: Secret text
Scope: Global
Secret: VOTRE_SONAR_TOKEN
ID: sonar-token
Description: SonarCloud Token
```

### 2. GitHub Token

```bash
# Dans Jenkins UI
Manage Jenkins → Manage Credentials → (global) → Add Credentials

Kind: Username with password
Scope: Global
Username: votre-username-github
Password: VOTRE_GITHUB_TOKEN
ID: github-token
Description: GitHub Token for Docker Registry
```

### 3. Docker Registry (GHCR)

Les credentials GitHub servent aussi pour GHCR (GitHub Container Registry).

---

## 🚀 Utilisation des Pipelines

### Pipeline Backend

#### Déclencher manuellement

1. Aller dans **smartek-backend** job
2. Cliquer sur **Build with Parameters**
3. Sélectionner :
   - **SERVICE** : `auth-service` (ou autre)
   - **RUN_TESTS** : ✓
   - **RUN_SONAR** : ✓
   - **BUILD_DOCKER** : ✓
4. Cliquer sur **Build**

#### Déclencher automatiquement

```bash
# Faire un push sur le repo
cd Backend/auth-service
# Modifier un fichier
git add .
git commit -m "Update auth service"
git push origin main

# Le webhook déclenche automatiquement le pipeline ! 🎉
```

### Pipeline Frontend

#### Déclencher manuellement

1. Aller dans **smartek-frontend** job
2. Cliquer sur **Build with Parameters**
3. Sélectionner :
   - **BUILD_CONFIG** : `production`
   - **RUN_TESTS** : ✓
   - **RUN_SONAR** : ✓
   - **BUILD_DOCKER** : ✓
4. Cliquer sur **Build**

#### Déclencher automatiquement

```bash
# Faire un push sur le repo
cd Frontend/angular-app
# Modifier un fichier
git add .
git commit -m "Update frontend"
git push origin main

# Le webhook déclenche automatiquement le pipeline ! 🎉
```

---

## 📊 Visualiser les résultats

### Tests & Coverage

1. Aller dans le job → **Build #X**
2. Voir :
   - **Test Result** : Résultats des tests unitaires
   - **Coverage Report** : Rapport JaCoCo (Backend) ou Istanbul (Frontend)

### SonarQube

1. Aller sur [SonarCloud](https://sonarcloud.io)
2. Voir les analyses de qualité de code

### Docker Images

```bash
# Vérifier les images pushées
docker images | grep smartek

# Ou sur GitHub Container Registry
https://github.com/orgs/VOTRE_ORG/packages
```

---

## 🔍 Troubleshooting

### Problème : Webhook ne se déclenche pas

**Solution 1** : Vérifier les logs du webhook GitHub
```
GitHub → Settings → Webhooks → Recent Deliveries
```

**Solution 2** : Vérifier que Jenkins est accessible depuis Internet
```bash
curl http://VOTRE_IP_JENKINS:8090/github-webhook/
```

**Solution 3** : Utiliser ngrok pour exposer Jenkins
```bash
ngrok http 8090
# Utiliser l'URL ngrok dans le webhook GitHub
```

### Problème : Tests échouent (MySQL)

**Solution** : Vérifier que le port 3307 est disponible
```bash
# Changer le port dans Jenkinsfile.backend si nécessaire
-p 3308:3306  # Au lieu de 3307
```

### Problème : Docker build échoue

**Solution** : Vérifier les permissions Docker
```bash
# Donner les permissions au user Jenkins
docker exec -it smartek-jenkins bash
usermod -aG docker jenkins
```

### Problème : SonarQube analysis échoue

**Solution 1** : Vérifier le token SonarCloud
```bash
# Dans Jenkins → Manage Credentials
# Vérifier que le token est valide
```

**Solution 2** : Installer SonarQube Scanner
```bash
# Dans Jenkins → Manage Jenkins → Global Tool Configuration
# Ajouter SonarQube Scanner
```

---

## 📝 Structure des fichiers

```
jenkins/
├── docker-compose.yml          # Configuration Docker Jenkins + SonarQube
├── jenkins-casc.yaml           # Configuration as Code Jenkins
└── README.md                   # Ce fichier

Jenkinsfile.backend             # Pipeline Backend (tous les microservices)
Jenkinsfile.frontend            # Pipeline Frontend (Angular)
```

---

## 🎯 Checklist pour l'évaluation

### ✅ Jenkins & Webhooks (3 points)

- [ ] Jenkins installé et accessible
- [ ] Webhook GitHub configuré pour Backend
- [ ] Webhook GitHub configuré pour Frontend
- [ ] Pipeline Backend se déclenche automatiquement sur push
- [ ] Pipeline Frontend se déclenche automatiquement sur push
- [ ] Démonstration : Push → Webhook → Build automatique

### ✅ Tests & Qualité

- [ ] Tests unitaires exécutés dans le pipeline
- [ ] Rapport de coverage généré (JaCoCo/Istanbul)
- [ ] Analyse SonarQube intégrée
- [ ] Résultats visibles dans Jenkins UI

### ✅ Docker

- [ ] Images Docker buildées automatiquement
- [ ] Images pushées vers GHCR
- [ ] Tags avec commit SHA

---

## 📞 Support

Pour toute question :
- Consulter les logs Jenkins : `docker logs smartek-jenkins`
- Consulter les logs SonarQube : `docker logs smartek-sonarqube`
- Vérifier la configuration : `jenkins/jenkins-casc.yaml`

---

## 🎓 Ressources

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [GitHub Webhooks](https://docs.github.com/en/webhooks)
- [SonarQube Scanner](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/)
- [Docker Pipeline Plugin](https://plugins.jenkins.io/docker-workflow/)

---

**Bonne chance pour votre évaluation ! 🚀**
