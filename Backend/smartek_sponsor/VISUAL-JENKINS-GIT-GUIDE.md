# 📸 Guide Visuel : Configuration Jenkins + Git

## 🎯 Ce Guide en Images

Ce guide vous montre **exactement où cliquer** dans Jenkins pour configurer Git.

---

## 🚀 PARTIE 1 : Pousser le Code sur Git

### Étape 1.1 : Ouvrir PowerShell

```powershell
# Aller dans votre projet
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor"
```

### Étape 1.2 : Vérifier la Branche

```powershell
# Voir la branche actuelle
git branch

# Vous devriez voir :
# * sponsor  ← L'étoile indique la branche active
```

**Si vous n'êtes pas sur 'sponsor' :**
```powershell
git checkout sponsor
```

### Étape 1.3 : Copier le Jenkinsfile

```powershell
cd Backend/smartek_sponsor

# Copier le Jenkinsfile Git
copy Jenkinsfile.git Jenkinsfile

# Vérifier que ça a marché
ls Jenkinsfile
```

### Étape 1.4 : Commit et Push

```powershell
# Ajouter le fichier
git add Jenkinsfile

# Commit
git commit -m "Add Jenkins pipeline with Git integration"

# Push vers GitHub
git push origin sponsor
```

**Résultat attendu :**
```
Enumerating objects: X, done.
Counting objects: 100% (X/X), done.
Writing objects: 100% (X/X), XXX bytes | XXX KiB/s, done.
Total X (delta X), reused X (delta X)
To https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
   xxxxxxx..yyyyyyy  sponsor -> sponsor
```

✅ **Votre code est maintenant sur GitHub !**

---

## 🔧 PARTIE 2 : Configurer Jenkins

### Étape 2.1 : Ouvrir Jenkins

**Navigateur :**
```
http://localhost:9091
```

**Login :**
- Username : `admin`
- Password : `admin123`

---

### Étape 2.2 : Créer un Nouveau Pipeline

**📍 Où cliquer :**

1. **En haut à gauche**, cliquer sur **"New Item"**

2. **Remplir le formulaire :**
   ```
   Enter an item name: smartek-sponsor-git-pipeline
   ```

3. **Sélectionner :** `Pipeline` (l'icône avec des cercles connectés)

4. **Cliquer :** `OK` (en bas)

---

### Étape 2.3 : Configuration Générale

**📍 Section "General" :**

1. **Cocher :** ☑️ `GitHub project`

2. **Remplir :**
   ```
   Project url: https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/
   ```

---

### Étape 2.4 : Configuration Pipeline (LA PARTIE IMPORTANTE)

**📍 Descendre jusqu'à la section "Pipeline" :**

#### Champ 1 : Definition

**Cliquer sur le menu déroulant "Definition" :**
- ❌ Ne PAS choisir "Pipeline script"
- ✅ Choisir **"Pipeline script from SCM"**

#### Champ 2 : SCM

**Cliquer sur le menu déroulant "SCM" :**
- ✅ Choisir **"Git"**

#### Champ 3 : Repository URL

**Remplir exactement :**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
```

#### Champ 4 : Credentials

**Si le repository est PUBLIC :**
- Laisser `- none -`

**Si le repository est PRIVÉ :**

1. **Cliquer sur :** `Add` → `Jenkins`

2. **Remplir le formulaire :**
   - Kind : `Username with password`
   - Scope : `Global`
   - Username : `votre_username_github`
   - Password : `votre_token_github` (voir section Token ci-dessous)
   - ID : `github-credentials`
   - Description : `GitHub Credentials`

3. **Cliquer :** `Add`

4. **Sélectionner dans le menu déroulant :** `votre_username_github/****** (GitHub Credentials)`

#### Champ 5 : Branches to build

**Remplir :**
```
Branch Specifier: */sponsor
```

⚠️ **Important :** Ne pas oublier l'étoile et le slash : `*/sponsor`

#### Champ 6 : Script Path

**Remplir exactement :**
```
Backend/smartek_sponsor/Jenkinsfile.git
```

⚠️ **Important :** Pas de slash au début, pas de slash à la fin

#### Champ 7 : Lightweight checkout

**Cocher :** ☑️ `Lightweight checkout`

---

### Étape 2.5 : Sauvegarder

**📍 En bas de la page :**

**Cliquer :** `Save`

✅ **Votre pipeline est créé !**

---

## 🔑 PARTIE 3 : Créer un Token GitHub (Si Nécessaire)

### Quand avez-vous besoin d'un token ?

- ✅ Si votre repository est **privé**
- ❌ Si votre repository est **public** (pas besoin)

### Étape 3.1 : Aller sur GitHub

**Navigateur :**
```
https://github.com
```

### Étape 3.2 : Accéder aux Settings

**📍 Où cliquer :**

1. **En haut à droite**, cliquer sur votre **photo de profil**
2. Dans le menu, cliquer sur **"Settings"**

### Étape 3.3 : Developer Settings

**📍 Dans le menu de gauche :**

1. **Descendre tout en bas**
2. Cliquer sur **"Developer settings"**

### Étape 3.4 : Personal Access Tokens

**📍 Dans le menu de gauche :**

1. Cliquer sur **"Personal access tokens"**
2. Cliquer sur **"Tokens (classic)"**

### Étape 3.5 : Générer le Token

**📍 En haut à droite :**

1. Cliquer sur **"Generate new token"**
2. Cliquer sur **"Generate new token (classic)"**

### Étape 3.6 : Configurer le Token

**Remplir le formulaire :**

1. **Note :** `Jenkins Access`
2. **Expiration :** `90 days` (ou plus)
3. **Select scopes :**
   - ☑️ `repo` (cocher la case principale, tous les sous-items seront cochés)
   - ☑️ `admin:repo_hook` (pour les webhooks)

### Étape 3.7 : Générer et Copier

**📍 En bas de la page :**

1. **Cliquer :** `Generate token`

2. **COPIER LE TOKEN IMMÉDIATEMENT**
   ```
   ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

⚠️ **IMPORTANT :** Vous ne pourrez plus voir ce token après avoir quitté la page !

**Sauvegarder le token dans un fichier texte temporaire.**

---

## 🚀 PARTIE 4 : Lancer le Build

### Étape 4.1 : Retourner sur Jenkins

**Navigateur :**
```
http://localhost:9091
```

### Étape 4.2 : Ouvrir le Pipeline

**📍 Sur la page d'accueil :**

**Cliquer sur :** `smartek-sponsor-git-pipeline`

### Étape 4.3 : Lancer le Build

**📍 Dans le menu de gauche :**

**Cliquer sur :** `Build Now`

**Résultat :**
- Un nouveau build apparaît dans **"Build History"** (en bas à gauche)
- Vous voyez : `#1` avec une icône qui clignote (build en cours)

### Étape 4.4 : Voir les Logs

**📍 Dans "Build History" :**

1. **Cliquer sur le numéro du build :** `#1`
2. **Dans le menu de gauche, cliquer sur :** `Console Output`

### Étape 4.5 : Observer le Build

**Vous devriez voir :**

```
Started by user admin
Running in Durability level: MAX_SURVIVABILITY
[Pipeline] Start of Pipeline
[Pipeline] node
Running on Jenkins in /var/jenkins_home/workspace/smartek-sponsor-git-pipeline
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Declarative: Checkout SCM)
[Pipeline] checkout
Cloning the remote Git repository
Cloning repository https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
...
[Pipeline] stage (1. Checkout)
📥 Récupération du code depuis Git...
...
[Pipeline] stage (2. Build)
🔨 Compilation du projet...
...
✅ PIPELINE COMPLETED SUCCESSFULLY!
[Pipeline] End of Pipeline
Finished: SUCCESS
```

✅ **Si vous voyez "Finished: SUCCESS", tout fonctionne parfaitement !**

---

## 🎬 PARTIE 5 : Démonstration au Prof

### Préparation (Avant la Démo)

**Checklist :**
- [ ] Jenkins tourne
- [ ] Pipeline créé
- [ ] Au moins un build a réussi
- [ ] Vous savez où cliquer

### Scénario de Démonstration

**1. Montrer GitHub (30 secondes)**

**Ouvrir :**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/tree/sponsor
```

**Dire :**
> "Voici notre code source sur GitHub, branche sponsor. Vous pouvez voir le Jenkinsfile ici."

**Naviguer vers :**
```
Backend/smartek_sponsor/Jenkinsfile.git
```

---

**2. Montrer Jenkins (30 secondes)**

**Ouvrir :**
```
http://localhost:9091
```

**Dire :**
> "Voici Jenkins, notre outil d'intégration continue. Il est connecté à notre repository GitHub."

**Cliquer sur :** `smartek-sponsor-git-pipeline`

---

**3. Lancer le Build (5 minutes)**

**Dire :**
> "Je vais maintenant lancer un build devant vous. Jenkins va automatiquement récupérer le code depuis GitHub et exécuter toutes les étapes du pipeline."

**Cliquer sur :** `Build Now`

**Cliquer sur :** Le numéro du build qui apparaît

**Cliquer sur :** `Console Output`

**Expliquer pendant que ça tourne :**
> "Vous voyez, Jenkins :
> 1. Clone le repository depuis GitHub
> 2. Compile le code avec Maven
> 3. Lance les tests unitaires
> 4. Analyse la qualité du code
> 5. Crée le package JAR
> 6. Construit l'image Docker
> 7. Scanne les vulnérabilités
> 8. Déploie sur Kubernetes
> 9. Vérifie que tout fonctionne
> 
> Tout ça automatiquement, sans intervention humaine."

---

**4. Montrer le Succès (30 secondes)**

**Quand vous voyez :**
```
✅ PIPELINE COMPLETED SUCCESSFULLY!
Finished: SUCCESS
```

**Dire :**
> "Voilà ! Le pipeline est terminé avec succès. L'application est compilée, testée, packagée, et prête à être déployée en production. Tout ça en quelques minutes, de manière automatisée et reproductible."

---

**5. Montrer l'Historique (30 secondes)**

**Retourner sur la page du pipeline**

**Montrer :**
- La liste des builds
- Les graphiques de tendance
- Le temps d'exécution

**Dire :**
> "Jenkins garde un historique complet de tous les builds. On peut voir les tendances, identifier les problèmes, et revenir à n'importe quelle version."

---

## 💡 Réponses aux Questions du Prof

### Q: "Pourquoi utiliser Git avec Jenkins ?"

**R:** 
> "Git nous donne la traçabilité complète. Chaque build Jenkins est lié à un commit Git spécifique. On sait exactement quelle version du code a été déployée, qui l'a modifiée, et quand. C'est essentiel pour la collaboration en équipe et pour le rollback en cas de problème."

### Q: "Comment Jenkins sait quand lancer un build ?"

**R:**
> "On peut configurer Jenkins de plusieurs façons :
> 1. Manuellement avec 'Build Now' (ce que je viens de faire)
> 2. Automatiquement à chaque push sur GitHub (avec des webhooks)
> 3. Sur un planning (par exemple, tous les soirs à minuit)
> 4. Quand un autre build se termine
> 
> C'est très flexible."

### Q: "Et si le build échoue ?"

**R:**
> "Jenkins nous envoie une notification (email, Slack, etc.). On peut voir exactement quelle étape a échoué dans les logs. Le code défectueux n'est jamais déployé en production. C'est le principe du 'fail fast' : détecter les problèmes le plus tôt possible."

### Q: "C'est utilisé en entreprise ?"

**R:**
> "Absolument ! C'est l'industrie standard. Des entreprises comme Netflix, Amazon, Google utilisent exactement ce type de pipeline CI/CD. La seule différence, c'est l'échelle : ils ont des milliers de builds par jour, mais le principe est le même."

---

## ✅ Checklist Finale

**Avant la présentation :**

- [ ] Docker Desktop tourne
- [ ] Jenkins accessible sur http://localhost:9091
- [ ] Code poussé sur GitHub branche sponsor
- [ ] Pipeline créé dans Jenkins
- [ ] Au moins un build a réussi (pour montrer l'historique)
- [ ] Vous avez testé "Build Now" au moins une fois
- [ ] Vous savez expliquer chaque étape
- [ ] Vous avez lu les réponses aux questions

---

## 🎉 Vous êtes Prêt !

**Vous avez maintenant :**

✅ Un pipeline CI/CD complet
✅ Connecté à GitHub
✅ Avec 12 étapes automatisées
✅ Production-ready
✅ Conforme aux standards de l'industrie

**C'est du travail professionnel ! 🚀**

---

## 📞 Aide d'Urgence

### Jenkins ne répond pas
```powershell
docker restart jenkins
# Attendre 1 minute
```

### Build échoue
1. Console Output
2. Lire l'erreur
3. Vérifier Maven et JDK dans Tools

### Git connection failed
1. Vérifier l'URL du repository
2. Vérifier les credentials
3. Vérifier la branche : `*/sponsor`

---

**Bonne chance pour votre présentation ! 💪**

**Vous allez impressionner votre prof ! 🌟**
