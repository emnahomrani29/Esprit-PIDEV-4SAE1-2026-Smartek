# 🎯 Guide Pas-à-Pas : De Zéro à la Présentation

## 📋 Vue d'Ensemble

Ce guide vous emmène de l'installation à la présentation devant votre prof.

**Temps total : 15 minutes**

---

## ✅ ÉTAPE 1 : Vérifications Préalables (2 min)

### 1.1 Vérifier Docker

```powershell
# Ouvrir PowerShell
docker --version
docker ps
```

**Résultat attendu :**
```
Docker version 20.x.x
CONTAINER ID   IMAGE   ...
```

**Si erreur :** Démarrer Docker Desktop

---

### 1.2 Vérifier Git

```powershell
git --version
git status
```

**Résultat attendu :**
```
git version 2.x.x
On branch sponsor
```

**Si pas sur sponsor :**
```powershell
git checkout sponsor
```

---

### 1.3 Vérifier Jenkins

```powershell
docker ps | findstr jenkins
```

**Résultat attendu :**
```
xxx   jenkins/jenkins   ...   0.0.0.0:9091->8080/tcp
```

**Si absent :**
```powershell
docker start jenkins
```

---

## ✅ ÉTAPE 2 : Pousser le Code sur Git (3 min)

### 2.1 Aller dans le Projet

```powershell
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor"
```

### 2.2 Vérifier la Branche

```powershell
git branch
```

**Vous devez voir :**
```
* sponsor
```

### 2.3 Copier le Jenkinsfile

```powershell
cd Backend/smartek_sponsor
copy Jenkinsfile.git Jenkinsfile
```

### 2.4 Commit et Push

```powershell
git add Jenkinsfile
git commit -m "Add Jenkins pipeline with Git integration"
git push origin sponsor
```

**Résultat attendu :**
```
To https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git
   xxxxxxx..yyyyyyy  sponsor -> sponsor
```

✅ **Code sur GitHub !**

---

## ✅ ÉTAPE 3 : Configurer Jenkins (5 min)

### 3.1 Ouvrir Jenkins

**Navigateur :**
```
http://localhost:9091
```

**Login :**
- Username : `admin`
- Password : `admin123`

---

### 3.2 Créer le Pipeline

**Cliquer :** `New Item` (en haut à gauche)

**Remplir :**
```
Item name: smartek-sponsor-git-pipeline
Type: Pipeline
```

**Cliquer :** `OK`

---

### 3.3 Configurer Git

**Section "General" :**
- ☑️ Cocher `GitHub project`
- Project url : `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/`

**Section "Pipeline" :**

| Champ | Valeur |
|-------|--------|
| Definition | `Pipeline script from SCM` |
| SCM | `Git` |
| Repository URL | `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git` |
| Credentials | `- none -` (si public) |
| Branch Specifier | `*/sponsor` |
| Script Path | `Backend/smartek_sponsor/Jenkinsfile.git` |

**Cliquer :** `Save`

✅ **Pipeline créé !**

---

## ✅ ÉTAPE 4 : Tester le Build (3 min)

### 4.1 Lancer le Build

**Dans Jenkins :**
1. Cliquer sur `smartek-sponsor-git-pipeline`
2. Cliquer `Build Now`
3. Cliquer sur le numéro du build (#1)
4. Cliquer `Console Output`

### 4.2 Observer

**Vous devriez voir :**
```
Started by user admin
Cloning repository https://github.com/...
[Pipeline] stage (1. Checkout)
📥 Récupération du code depuis Git...
[Pipeline] stage (2. Build)
🔨 Compilation du projet...
...
✅ PIPELINE COMPLETED SUCCESSFULLY!
Finished: SUCCESS
```

✅ **Build réussi !**

---

## ✅ ÉTAPE 5 : Préparer la Présentation (2 min)

### 5.1 Imprimer les Notes

**Ouvrir et imprimer :**
```
Backend/smartek_sponsor/PRESENTATION-NOTES.md
```

### 5.2 Préparer les Onglets

**Ouvrir dans le navigateur :**
1. `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/tree/sponsor`
2. `http://localhost:9091`
3. `http://localhost:8080/actuator/health`
4. `http://localhost:3000`

### 5.3 Ouvrir VS Code

```powershell
code .
```

**Ouvrir ces fichiers :**
- `Jenkinsfile.git`
- `Dockerfile`
- `k8s/deployment.yaml`

✅ **Prêt pour la démo !**

---

## 🎬 ÉTAPE 6 : La Présentation (8 min)

### Minute 0-1 : Introduction

**Dire :**
> "Bonjour Professeur. J'ai créé un pipeline CI/CD complet pour notre application. Le code est sur GitHub, et Jenkins automatise tout."

---

### Minute 1-2 : Montrer GitHub

**Onglet 1 : GitHub**
```
https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek/tree/sponsor
```

**Naviguer vers :**
```
Backend/smartek_sponsor/Jenkinsfile.git
```

**Dire :**
> "Voici notre code source sur GitHub. Le Jenkinsfile définit toutes les étapes du pipeline."

---

### Minute 2-7 : Lancer le Build

**Onglet 2 : Jenkins**
```
http://localhost:9091
```

**Actions :**
1. Cliquer `smartek-sponsor-git-pipeline`
2. Cliquer `Build Now`
3. Cliquer sur le numéro du build
4. Cliquer `Console Output`

**Dire pendant que ça tourne :**
> "Jenkins exécute 12 étapes automatiquement :
> Checkout, Build, Tests, SonarQube, Quality Gate, Package,
> Nexus Maven, Docker Build, Security Scan, Nexus Docker,
> Kubernetes Deploy, Health Check.
> 
> Tout ça sans intervention humaine."

---

### Minute 7-8 : Conclusion

**Quand vous voyez SUCCESS :**

**Dire :**
> "Voilà ! Le pipeline est terminé avec succès. C'est production-ready et suit les meilleures pratiques DevOps."

**Puis :**
> "Avez-vous des questions ?"

---

## 💡 Réponses Rapides aux Questions

### "Pourquoi Docker ?"
> "Garantit que ça marche partout : dev, test, production"

### "Pourquoi Jenkins ?"
> "Automatise tout : compile, teste, déploie. Évite les erreurs humaines"

### "Comment tu testes ?"
> "3 niveaux : tests unitaires JUnit, qualité SonarQube, sécurité Trivy"

### "Et si ça casse ?"
> "Kubernetes redémarre automatiquement. Rollback possible. Monitoring avec alertes"

### "C'est utilisé en entreprise ?"
> "Oui ! Netflix, Amazon, Google utilisent exactement ce type de pipeline"

---

## ✅ Checklist Finale

**Avant la présentation :**

- [ ] Docker Desktop tourne
- [ ] Jenkins accessible
- [ ] Code sur GitHub
- [ ] Pipeline créé
- [ ] Build a réussi au moins une fois
- [ ] Onglets du navigateur prêts
- [ ] VS Code ouvert
- [ ] Notes imprimées
- [ ] Confiant !

---

## 🆘 Plan B

### Si Jenkins ne répond pas
```powershell
docker restart jenkins
# Attendre 1 minute
```

### Si le build échoue
**Montrer l'historique des builds précédents**

**Dire :**
> "Voici un build qui a réussi précédemment. En production, on aurait des alertes automatiques."

### Si Internet ne marche pas
**Montrer le code du Jenkinsfile**

**Dire :**
> "Le pipeline peut aussi fonctionner en mode offline avec un repository Git local."

---

## 🎉 Vous Êtes Prêt !

**Vous avez :**

✅ Code sur GitHub
✅ Pipeline Jenkins configuré
✅ Build qui fonctionne
✅ Documentation complète
✅ Notes de présentation
✅ Réponses aux questions

**C'est du travail professionnel ! 🚀**

---

## 📞 Aide d'Urgence

### Commandes Rapides

```powershell
# Tout vérifier
docker ps
git status
curl http://localhost:9091

# Tout redémarrer
docker restart jenkins
docker-compose restart

# Voir les logs
docker logs jenkins
```

### Documentation

- **PRESENTATION-NOTES.md** - À imprimer
- **COMMANDS-CHEATSHEET.md** - Commandes
- **DEMO-SCRIPT.md** - Script détaillé

---

## 🎯 Récapitulatif

```
1. Vérifications (2 min)     ✅
2. Push sur Git (3 min)      ✅
3. Config Jenkins (5 min)    ✅
4. Test Build (3 min)        ✅
5. Préparation (2 min)       ✅
6. Présentation (8 min)      ✅

Total : 23 minutes
```

---

**Bonne chance ! 💪**

**Vous allez impressionner votre prof ! 🌟**

---

*Guide créé avec ❤️ pour votre succès*
*Version : 1.0.0*
*Date : 2024*
