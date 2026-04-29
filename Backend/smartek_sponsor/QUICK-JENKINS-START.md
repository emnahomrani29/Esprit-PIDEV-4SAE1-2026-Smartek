# ⚡ Démarrage Rapide Jenkins (2 Minutes)

## ✅ Jenkins est Installé et Tourne !

```
🌐 URL      : http://localhost:9091
🔑 Password : bf8a489fb7634770a439175fb535faa0
👤 Username : admin (après configuration)
🔐 Password : admin123 (après configuration)
```

---

## 🚀 3 Étapes pour Commencer

### 1️⃣ Ouvrir Jenkins (30 secondes)

```
1. Ouvrez votre navigateur
2. Allez sur : http://localhost:9091
3. Collez le mot de passe : bf8a489fb7634770a439175fb535faa0
4. Cliquez "Continue"
5. Cliquez "Install suggested plugins"
6. Attendez 2 minutes
```

### 2️⃣ Créer un Compte (30 secondes)

```
Username : admin
Password : admin123
Email    : admin@smartek.com

Cliquez "Save and Continue" → "Save and Finish" → "Start using Jenkins"
```

### 3️⃣ Configurer les Outils (1 minute)

```
1. Manage Jenkins → Tools
2. Maven installations → Add Maven
   - Name: Maven-3.9.6
   - ☑ Install automatically
   - Version: 3.9.6
3. JDK installations → Add JDK
   - Name: JDK-17
   - ☑ Install automatically
   - Version: jdk-17.0.9+9
4. Save
```

---

## 🎯 Créer Votre Premier Pipeline

### Étape 1 : Nouveau Job

```
1. New Item
2. Nom: smartek-sponsor-pipeline
3. Type: Pipeline
4. OK
```

### Étape 2 : Configuration

Dans **Pipeline** → **Script**, collez ce code :

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Build') {
            steps {
                echo "Building..."
                dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                    bat 'mvn clean compile'
                }
            }
        }
        
        stage('Test') {
            steps {
                echo "Testing..."
                dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                    bat 'mvn test'
                }
            }
        }
        
        stage('Package') {
            steps {
                echo "Packaging..."
                dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                    bat 'mvn package -DskipTests'
                }
            }
        }
    }
    
    post {
        success {
            echo "SUCCESS!"
        }
    }
}
```

Cliquez **Save**

### Étape 3 : Lancer

```
1. Cliquez "Build Now"
2. Cliquez sur le numéro du build (#1)
3. Cliquez "Console Output"
4. Regardez le pipeline s'exécuter !
```

---

## 📊 Ce que Vous Verrez

```
Started by user admin
Running in Durability level: MAX_SURVIVABILITY
[Pipeline] Start of Pipeline
[Pipeline] node
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Build)
[Pipeline] echo
Building...
[Pipeline] bat
[mvn clean compile]
BUILD SUCCESS
[Pipeline] }
[Pipeline] stage
[Pipeline] { (Test)
[Pipeline] echo
Testing...
[Pipeline] bat
[mvn test]
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
[Pipeline] }
[Pipeline] stage
[Pipeline] { (Package)
[Pipeline] echo
Packaging...
[Pipeline] bat
[mvn package -DskipTests]
BUILD SUCCESS
[Pipeline] }
[Pipeline] echo
SUCCESS!
[Pipeline] End of Pipeline
Finished: SUCCESS
```

---

## 🎓 Pour Votre Prof

### Montrez :

1. **Le Dashboard** : http://localhost:9091
2. **Cliquez "Build Now"** devant lui
3. **Montrez les logs** en temps réel
4. **Montrez le résultat** : SUCCESS ✅

### Expliquez :

> "Quand je clique sur 'Build Now', Jenkins :
> 1. Compile mon code Java avec Maven
> 2. Lance tous les tests automatiques
> 3. Crée le fichier JAR de l'application
> 
> Tout ça automatiquement en 2-3 minutes !"

---

## 🛠️ Commandes Utiles

```powershell
# Voir si Jenkins tourne
docker ps | findstr jenkins

# Voir les logs
docker logs -f jenkins

# Redémarrer Jenkins
docker restart jenkins

# Arrêter Jenkins
docker stop jenkins

# Démarrer Jenkins
docker start jenkins
```

---

## 📚 Documentation Complète

Pour plus de détails, voir :
- [JENKINS-SETUP-GUIDE.md](JENKINS-SETUP-GUIDE.md) - Guide complet
- [PIPELINE-OVERVIEW.md](PIPELINE-OVERVIEW.md) - Architecture du pipeline

---

## ✅ Checklist

- [ ] Jenkins accessible sur http://localhost:9091
- [ ] Compte admin créé (admin/admin123)
- [ ] Maven-3.9.6 configuré
- [ ] JDK-17 configuré
- [ ] Pipeline créé
- [ ] Premier build lancé
- [ ] Build réussi (SUCCESS)

---

## 🎉 Vous êtes Prêt !

Jenkins est configuré et votre pipeline fonctionne !

**Lancez un build et impressionnez votre prof ! 🚀**
