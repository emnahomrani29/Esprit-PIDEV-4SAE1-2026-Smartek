# ⚡ Setup Rapide : Jenkins + Git (5 Minutes)

## 🎯 Objectif
Connecter Jenkins à votre GitHub pour automatiser le pipeline.

---

## 📝 Informations Importantes

**Repository Git :**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
```

**Branche :**
```
sponsor
```

**Jenkinsfile Path :**
```
Backend/smartek_sponsor/Jenkinsfile.git
```

---

## ⚡ 5 Étapes Rapides

### 1️⃣ Pousser le Jenkinsfile sur Git (2 min)

```powershell
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor"

# Vérifier la branche
git branch

# Basculer sur sponsor si nécessaire
git checkout sponsor

# Aller dans le dossier
cd Backend/smartek_sponsor

# Copier le bon Jenkinsfile
copy Jenkinsfile.git Jenkinsfile

# Commit et push
git add Jenkinsfile
git commit -m "Add Jenkins pipeline with Git integration"
git push origin sponsor
```

---

### 2️⃣ Créer le Pipeline dans Jenkins (2 min)

1. Ouvrir http://localhost:9091
2. Login : `admin` / `admin123`
3. Cliquer **"New Item"**
4. Nom : `smartek-sponsor-git-pipeline`
5. Type : **"Pipeline"**
6. Cliquer **"OK"**

---

### 3️⃣ Configurer Git dans le Pipeline (1 min)

**Section "Pipeline" :**

| Champ | Valeur |
|-------|--------|
| Definition | `Pipeline script from SCM` |
| SCM | `Git` |
| Repository URL | `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git` |
| Credentials | Cliquer "Add" → Username + Token GitHub |
| Branch Specifier | `*/sponsor` |
| Script Path | `Backend/smartek_sponsor/Jenkinsfile.git` |

**Cliquer "Save"**

---

### 4️⃣ Créer Token GitHub (Si Repository Privé)

1. GitHub → Settings → Developer settings
2. Personal access tokens → Tokens (classic)
3. Generate new token (classic)
4. Cocher : `repo` + `admin:repo_hook`
5. Generate token
6. **COPIER LE TOKEN**

**Dans Jenkins Credentials :**
- Username : `votre_username_github`
- Password : `le_token_copié`

---

### 5️⃣ Lancer le Build (30 sec)

1. Cliquer sur votre pipeline
2. Cliquer **"Build Now"**
3. Cliquer sur le numéro du build
4. Cliquer **"Console Output"**
5. Attendre le **"SUCCESS"** ✅

---

## 🎬 Pour la Démo au Prof

### Montrer en 3 Étapes

**1. GitHub (30 sec)**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek
```
> "Voici notre code source sur GitHub"

**2. Jenkins (30 sec)**
```
http://localhost:9091
```
> "Jenkins est connecté à GitHub"

**3. Build (5 min)**
- Cliquer "Build Now"
- Montrer les logs
- Expliquer les 12 étapes

> "Jenkins récupère automatiquement le code et exécute tout le pipeline"

---

## ✅ Vérification Rapide

```powershell
# Jenkins tourne ?
docker ps | findstr jenkins

# Git configuré ?
git remote -v

# Sur la bonne branche ?
git branch

# Jenkinsfile existe ?
ls Backend/smartek_sponsor/Jenkinsfile.git
```

---

## 🆘 Problèmes Courants

### "Failed to connect to repository"
→ Vérifier URL et credentials

### "Couldn't find any revision to build"
→ Vérifier Branch Specifier : `*/sponsor`

### "Script not found"
→ Vérifier Script Path : `Backend/smartek_sponsor/Jenkinsfile.git`

### Maven/JDK non trouvé
→ Manage Jenkins → Tools → Vérifier Maven-3.9.6 et JDK-17

---

## 📚 Documentation Complète

Pour plus de détails : **[GIT-JENKINS-SETUP.md](GIT-JENKINS-SETUP.md)**

---

## 🎉 C'est Tout !

**Vous avez maintenant un pipeline CI/CD complet connecté à Git ! 🚀**

**Temps total : 5 minutes**
**Résultat : Pipeline automatisé professionnel**

---

**Bonne chance ! 💪**
