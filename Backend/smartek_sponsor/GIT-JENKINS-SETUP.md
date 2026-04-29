# 🚀 Guide Complet : Jenkins avec Git Integration

## 📋 Vue d'Ensemble

Vous allez configurer Jenkins pour qu'il récupère automatiquement le code depuis GitHub et exécute le pipeline.

**Votre Repository Git :**
- URL : `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git`
- Branche : `sponsor`
- Dossier : `Backend/smartek_sponsor`

---

## ✅ ÉTAPE 1 : Vérifier que Jenkins Tourne

```powershell
# Vérifier que Jenkins est actif
docker ps | findstr jenkins

# Si Jenkins n'est pas actif, le démarrer
docker start jenkins

# Attendre 1 minute que Jenkins démarre complètement
```

**Ouvrir Jenkins :**
```
http://localhost:9091
Login: admin / admin123
```

---

## ✅ ÉTAPE 2 : Installer le Plugin Git (Si Nécessaire)

### 2.1 Vérifier si Git est installé

1. Dans Jenkins, cliquer sur **"Manage Jenkins"** (à gauche)
2. Cliquer sur **"Plugins"**
3. Cliquer sur **"Installed plugins"**
4. Chercher **"Git"** dans la liste

**Si vous voyez "Git plugin" ✅ → Passez à l'ÉTAPE 3**

### 2.2 Installer Git Plugin (si absent)

1. Cliquer sur **"Available plugins"**
2. Chercher **"Git"**
3. Cocher **"Git plugin"**
4. Cliquer **"Install"**
5. Attendre la fin de l'installation
6. Redémarrer Jenkins si demandé

---

## ✅ ÉTAPE 3 : Créer le Pipeline avec Git

### 3.1 Créer un Nouveau Pipeline

1. Sur la page d'accueil Jenkins, cliquer **"New Item"** (en haut à gauche)
2. Entrer le nom : `smartek-sponsor-git-pipeline`
3. Sélectionner **"Pipeline"**
4. Cliquer **"OK"**

### 3.2 Configurer le Pipeline

#### Section "General"
- ✅ Cocher **"GitHub project"**
- Project url : `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/`

#### Section "Build Triggers" (Optionnel)
- ✅ Cocher **"Poll SCM"** si vous voulez que Jenkins vérifie automatiquement
- Schedule : `H/5 * * * *` (vérifie toutes les 5 minutes)

#### Section "Pipeline"

**Sélectionner :** `Pipeline script from SCM`

**SCM :** `Git`

**Repository URL :** 
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
```

**Credentials :** 
- Cliquer **"Add"** → **"Jenkins"**
- Kind : `Username with password`
- Username : `votre_username_github`
- Password : `votre_token_github` (voir section suivante si besoin)
- ID : `github-credentials`
- Description : `GitHub Credentials`
- Cliquer **"Add"**
- Sélectionner les credentials que vous venez de créer

**Branches to build :**
- Branch Specifier : `*/sponsor`

**Script Path :**
```
Backend/smartek_sponsor/Jenkinsfile.git
```

**Lightweight checkout :** ✅ Cocher

### 3.3 Sauvegarder

Cliquer **"Save"** en bas de la page

---

## 🔑 ÉTAPE 4 : Créer un Token GitHub (Si Nécessaire)

Si votre repository est privé, vous avez besoin d'un token :

### 4.1 Créer le Token sur GitHub

1. Aller sur GitHub : https://github.com
2. Cliquer sur votre **photo de profil** (en haut à droite)
3. Cliquer **"Settings"**
4. Dans le menu de gauche, tout en bas : **"Developer settings"**
5. Cliquer **"Personal access tokens"** → **"Tokens (classic)"**
6. Cliquer **"Generate new token"** → **"Generate new token (classic)"**
7. Note : `Jenkins Access`
8. Expiration : `90 days` (ou plus)
9. Cocher les permissions :
   - ✅ `repo` (tous les sous-items)
   - ✅ `admin:repo_hook` (pour les webhooks)
10. Cliquer **"Generate token"**
11. **COPIER LE TOKEN** (vous ne pourrez plus le voir après !)

### 4.2 Utiliser le Token dans Jenkins

- Username : `votre_username_github`
- Password : `le_token_que_vous_venez_de_copier`

---

## ✅ ÉTAPE 5 : Pousser le Jenkinsfile sur Git

### 5.1 Vérifier que vous êtes sur la bonne branche

```powershell
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor"

# Vérifier la branche actuelle
git branch

# Si vous n'êtes pas sur 'sponsor', basculer dessus
git checkout sponsor
```

### 5.2 Copier le bon Jenkinsfile

```powershell
cd Backend/smartek_sponsor

# Copier le Jenkinsfile Git comme Jenkinsfile principal
copy Jenkinsfile.git Jenkinsfile
```

### 5.3 Commit et Push

```powershell
# Ajouter le fichier
git add Jenkinsfile

# Commit
git commit -m "Add Jenkins pipeline with Git integration"

# Push vers GitHub
git push origin sponsor
```

**Si vous avez une erreur de push, vérifiez vos credentials Git.**

---

## ✅ ÉTAPE 6 : Lancer le Build

### 6.1 Retourner sur Jenkins

```
http://localhost:9091
```

### 6.2 Ouvrir votre Pipeline

Cliquer sur **"smartek-sponsor-git-pipeline"**

### 6.3 Lancer le Build

1. Cliquer **"Build Now"** (à gauche)
2. Un nouveau build apparaît dans **"Build History"**
3. Cliquer sur le **numéro du build** (ex: #1)
4. Cliquer **"Console Output"**

### 6.4 Observer les Logs

Vous devriez voir :

```
Started by user admin
Checking out git https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git into /var/jenkins_home/workspace/smartek-sponsor-git-pipeline
Cloning repository https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
...
[Pipeline] stage (1. Checkout)
📥 Récupération du code depuis Git...
...
[Pipeline] stage (2. Build)
🔨 Compilation du projet...
...
✅ PIPELINE COMPLETED SUCCESSFULLY!
```

---

## 🎯 ÉTAPE 7 : Tester Devant le Prof

### Scénario de Démonstration

**1. Montrer le Repository Git (30 secondes)**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek
```
> "Voici notre code source sur GitHub, branche sponsor"

**2. Montrer Jenkins (30 secondes)**
```
http://localhost:9091
```
> "Jenkins est configuré pour surveiller ce repository"

**3. Lancer un Build (5 minutes)**
- Cliquer **"Build Now"**
- Cliquer sur le build
- Cliquer **"Console Output"**

> "Regardez, Jenkins récupère automatiquement le code depuis Git et exécute toutes les étapes du pipeline..."

**4. Expliquer pendant que ça tourne (3 minutes)**

> "Le pipeline fait 12 étapes :
> 1. Checkout - Récupère le code depuis GitHub
> 2. Build - Compile avec Maven
> 3. Tests - Lance les tests unitaires
> 4. SonarQube - Analyse la qualité du code
> 5. Quality Gate - Vérifie les seuils
> 6. Package - Crée le JAR
> 7. Nexus Maven - Sauvegarde l'artefact
> 8. Docker Build - Crée l'image Docker
> 9. Security Scan - Scanne les vulnérabilités
> 10. Nexus Docker - Sauvegarde l'image
> 11. Kubernetes Deploy - Déploie sur K8s
> 12. Health Check - Vérifie le déploiement"

**5. Montrer le Succès (30 secondes)**

Quand vous voyez :
```
✅ PIPELINE COMPLETED SUCCESSFULLY!
Finished: SUCCESS
```

> "Voilà ! Le pipeline est terminé avec succès. L'application est compilée, testée, et prête à être déployée."

---

## 📊 Ce que Vous Pouvez Dire au Prof

### Avantages de Git Integration

✅ **Automatisation Complète**
> "Dès que je push du code sur GitHub, Jenkins peut automatiquement lancer le pipeline"

✅ **Traçabilité**
> "Chaque build est lié à un commit Git spécifique, on sait exactement quelle version du code a été déployée"

✅ **Collaboration**
> "Toute l'équipe peut travailler sur le même code, Jenkins teste automatiquement chaque changement"

✅ **Rollback Facile**
> "Si un problème survient, on peut facilement revenir à une version précédente"

✅ **Best Practices**
> "C'est exactement comme ça que travaillent les grandes entreprises : Git + Jenkins + CI/CD"

---

## 🔧 Dépannage

### Problème : "Failed to connect to repository"

**Solution :**
1. Vérifier l'URL du repository
2. Vérifier les credentials (username + token)
3. Vérifier que la branche `sponsor` existe

### Problème : "Couldn't find any revision to build"

**Solution :**
1. Vérifier le Branch Specifier : `*/sponsor`
2. Vérifier que le Jenkinsfile existe dans `Backend/smartek_sponsor/Jenkinsfile.git`

### Problème : "Script not found"

**Solution :**
1. Vérifier le Script Path : `Backend/smartek_sponsor/Jenkinsfile.git`
2. Vérifier que le fichier existe dans le repository

### Problème : Maven ou JDK non trouvé

**Solution :**
1. Aller dans **"Manage Jenkins"** → **"Tools"**
2. Vérifier que **Maven-3.9.6** et **JDK-17** sont configurés
3. Les noms doivent correspondre exactement à ceux dans le Jenkinsfile

---

## ✅ Checklist Finale

Avant la présentation :

- [ ] Jenkins tourne sur http://localhost:9091
- [ ] Plugin Git installé
- [ ] Credentials GitHub configurés
- [ ] Pipeline créé avec Git SCM
- [ ] Jenkinsfile poussé sur GitHub branche sponsor
- [ ] Au moins un build a réussi
- [ ] Vous savez lancer un build
- [ ] Vous pouvez expliquer chaque étape

---

## 🎉 Résumé

Vous avez maintenant :

✅ **Jenkins connecté à GitHub**
✅ **Pipeline automatisé avec 12 étapes**
✅ **Récupération automatique du code**
✅ **Build, Test, Package, Deploy automatisés**
✅ **Production-ready CI/CD pipeline**

**C'est exactement ce qu'utilisent les professionnels ! 🚀**

---

## 📞 Aide Rapide

### Commandes Utiles

```powershell
# Vérifier Jenkins
docker ps | findstr jenkins

# Redémarrer Jenkins
docker restart jenkins

# Voir les logs Jenkins
docker logs -f jenkins

# Vérifier Git
git status
git branch
git log --oneline -5
```

### URLs Importantes

- Jenkins : http://localhost:9091
- GitHub : https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek
- Application : http://localhost:8080/actuator/health
- Grafana : http://localhost:3000

---

**Bonne chance pour votre présentation ! 💪**

*Si vous avez des questions, relisez ce guide étape par étape.*
