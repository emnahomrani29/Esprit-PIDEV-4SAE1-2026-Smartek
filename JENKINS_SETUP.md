# 🚀 Guide Rapide - Configuration Jenkins pour l'Évaluation

## ⚡ Installation Express (5 minutes)

### 1️⃣ Démarrer Jenkins

```bash
# Méthode automatique (recommandée)
./jenkins/scripts/setup-jenkins.sh

# OU méthode manuelle
cd jenkins
docker-compose up -d
```

### 2️⃣ Accéder à Jenkins

- **URL**: http://localhost:8090
- **Username**: `admin`
- **Password**: `admin123`

### 3️⃣ Configurer les Credentials

#### A. SonarCloud Token

1. Aller sur [SonarCloud](https://sonarcloud.io)
2. **My Account** → **Security** → **Generate Token**
3. Dans Jenkins :
   - **Manage Jenkins** → **Manage Credentials** → **Add Credentials**
   - **Kind**: Secret text
   - **Secret**: Votre token SonarCloud
   - **ID**: `sonar-token`

#### B. GitHub Token

1. GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Generate new token**
2. Permissions : `repo`, `admin:repo_hook`, `write:packages`
3. Dans Jenkins :
   - **Manage Jenkins** → **Manage Credentials** → **Add Credentials**
   - **Kind**: Username with password
   - **Username**: Votre username GitHub
   - **Password**: Le token généré
   - **ID**: `github-token`

### 4️⃣ Configurer les Webhooks GitHub

#### Pour le Backend

1. Aller sur votre repo GitHub
2. **Settings** → **Webhooks** → **Add webhook**
3. Configurer :
   ```
   Payload URL: http://VOTRE_IP:8090/github-webhook/
   Content type: application/json
   Events: Just the push event
   Active: ✓
   ```

#### Pour le Frontend

Même configuration que le Backend (le même webhook déclenche les deux pipelines).

### 5️⃣ Créer les Jobs Jenkins

#### Méthode 1 : Via l'interface (Recommandé pour la démo)

**Job Backend:**
1. **New Item** → Nom: `smartek-backend` → **Pipeline** → OK
2. Dans **Pipeline** :
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: URL de votre repo
   - **Credentials**: Sélectionner `github-token`
   - **Script Path**: `Jenkinsfile.backend`
3. Dans **Build Triggers** :
   - ✓ **GitHub hook trigger for GITScm polling**
4. **Save**

**Job Frontend:**
1. **New Item** → Nom: `smartek-frontend` → **Pipeline** → OK
2. Dans **Pipeline** :
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: URL de votre repo
   - **Credentials**: Sélectionner `github-token`
   - **Script Path**: `Jenkinsfile.frontend`
3. Dans **Build Triggers** :
   - ✓ **GitHub hook trigger for GITScm polling**
4. **Save**

---

## 🎯 Test de l'Automatisation (Pour la Démo)

### Test Backend

```bash
# 1. Modifier un fichier backend
cd Backend/auth-service
echo "// Test webhook" >> src/main/java/com/smartek/authservice/AuthServiceApplication.java

# 2. Commit et push
git add .
git commit -m "Test Jenkins webhook - Backend"
git push origin main

# 3. Vérifier dans Jenkins
# Le job smartek-backend devrait se déclencher automatiquement ! 🎉
```

### Test Frontend

```bash
# 1. Modifier un fichier frontend
cd Frontend/angular-app
echo "// Test webhook" >> src/main.ts

# 2. Commit et push
git add .
git commit -m "Test Jenkins webhook - Frontend"
git push origin main

# 3. Vérifier dans Jenkins
# Le job smartek-frontend devrait se déclencher automatiquement ! 🎉
```

---

## 📊 Ce que fait chaque Pipeline

### Pipeline Backend (`Jenkinsfile.backend`)

1. ✅ **Checkout** : Clone le code
2. ✅ **Setup MySQL** : Démarre une base de test
3. ✅ **Build** : Compile avec Maven
4. ✅ **Tests** : Exécute les tests unitaires + JaCoCo coverage
5. ✅ **SonarQube** : Analyse la qualité du code
6. ✅ **Docker Build** : Crée l'image Docker
7. ✅ **Docker Push** : Push vers GHCR
8. ✅ **Archive** : Sauvegarde les artifacts

### Pipeline Frontend (`Jenkinsfile.frontend`)

1. ✅ **Checkout** : Clone le code
2. ✅ **Install** : npm ci
3. ✅ **Lint** : ESLint
4. ✅ **Tests** : Karma + Jasmine + Coverage
5. ✅ **SonarQube** : Analyse la qualité du code
6. ✅ **Build** : ng build --prod
7. ✅ **Docker Build** : Crée l'image Docker
8. ✅ **Docker Push** : Push vers GHCR
9. ✅ **Archive** : Sauvegarde les artifacts

---

## 🎓 Pour l'Évaluation - Points Clés

### ✅ Automatisation Totale (3 points)

**Ce que vous devez montrer :**

1. **Webhook configuré** :
   - Montrer dans GitHub : Settings → Webhooks
   - Montrer l'historique des deliveries

2. **Déclenchement automatique** :
   - Faire un push en direct
   - Montrer que le build démarre automatiquement dans Jenkins
   - Montrer les logs en temps réel

3. **Pipeline complet** :
   - Montrer les étapes du pipeline
   - Montrer les tests qui passent
   - Montrer l'analyse SonarQube
   - Montrer l'image Docker créée

### 📝 Script de Démonstration

```bash
# 1. Montrer Jenkins UI
firefox http://localhost:8090

# 2. Montrer les webhooks GitHub
firefox https://github.com/VOTRE_ORG/VOTRE_REPO/settings/hooks

# 3. Faire une modification
cd Backend/auth-service
echo "// Demo evaluation" >> README.md
git add .
git commit -m "Demo: Test automatisation Jenkins"
git push origin main

# 4. Retourner sur Jenkins
# → Le build démarre automatiquement
# → Montrer les logs
# → Montrer les résultats des tests
# → Montrer le rapport SonarQube
# → Montrer l'image Docker pushée
```

---

## 🔧 Troubleshooting Rapide

### Problème : Webhook ne fonctionne pas

**Solution 1** : Utiliser ngrok
```bash
ngrok http 8090
# Utiliser l'URL ngrok dans le webhook GitHub
```

**Solution 2** : Polling SCM (fallback)
```groovy
// Dans Jenkinsfile, remplacer:
triggers {
    githubPush()
}
// Par:
triggers {
    pollSCM('H/5 * * * *')  // Check toutes les 5 min
}
```

### Problème : Tests échouent

```bash
# Vérifier les logs
docker logs smartek-jenkins

# Vérifier MySQL
docker ps | grep mysql
```

### Problème : Docker build échoue

```bash
# Donner les permissions Docker
docker exec -it smartek-jenkins bash
usermod -aG docker jenkins
exit
docker-compose restart jenkins
```

---

## 📞 Commandes Utiles

```bash
# Voir les logs Jenkins
docker logs -f smartek-jenkins

# Redémarrer Jenkins
docker-compose restart jenkins

# Arrêter Jenkins
docker-compose down

# Supprimer tout et recommencer
docker-compose down -v
docker-compose up -d
```

---

## ✅ Checklist Finale

Avant l'évaluation, vérifier :

- [ ] Jenkins accessible sur http://localhost:8090
- [ ] Credentials configurés (SonarCloud + GitHub)
- [ ] Webhooks GitHub configurés
- [ ] Job `smartek-backend` créé
- [ ] Job `smartek-frontend` créé
- [ ] Test de push → build automatique fonctionne
- [ ] Tests unitaires passent
- [ ] SonarQube analysis fonctionne
- [ ] Images Docker sont pushées

---

## 🎯 Résumé pour le Validateur

**Vous avez maintenant :**

1. ✅ **Jenkins** installé et configuré
2. ✅ **Webhooks** Git → Jenkins (automatisation totale)
3. ✅ **Pipelines** Backend + Frontend
4. ✅ **Tests** automatiques avec coverage
5. ✅ **SonarQube** intégré
6. ✅ **Docker** build & push automatique
7. ✅ **GitHub Actions** (en parallèle, non supprimé)

**= 3/3 points pour Jenkins & Webhooks ! 🎉**

---

**Bonne chance pour votre évaluation ! 🚀**
