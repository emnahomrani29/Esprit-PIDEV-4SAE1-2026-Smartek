# 🧪 Guide de Test Complet

## ✅ Tous les Services Fonctionnent !

Suivez ce guide pour tester chaque composant.

---

## 🌐 PARTIE 1 : Tests dans le Navigateur

### Test 1 : Application Spring Boot ✅

**URL :** http://localhost:8080/actuator/health

**Résultat attendu :**
```json
{
  "status": "UP"
}
```

**✅ Si vous voyez ça, l'application fonctionne !**

---

### Test 2 : Jenkins ✅

**URL :** http://localhost:9091

**Première fois :**
1. Vous verrez "Unlock Jenkins"
2. Mot de passe : `bf8a489fb7634770a439175fb535faa0`
3. Cliquez "Continue"
4. Cliquez "Install suggested plugins"
5. Attendez 2-3 minutes

**Créer un compte :**
```
Username : admin
Password : admin123
Email    : admin@smartek.com
```

**✅ Si vous voyez le dashboard Jenkins, c'est bon !**

---

### Test 3 : Grafana ✅

**URL :** http://localhost:3000

**Login :**
```
Username : admin
Password : admin
```

**Première fois :**
- Il vous demandera de changer le mot de passe
- Vous pouvez cliquer "Skip" ou mettre un nouveau mot de passe

**Explorer :**
1. Cliquez sur le menu (☰) en haut à gauche
2. Allez dans "Dashboards"
3. Vous verrez les dashboards disponibles

**✅ Si vous voyez l'interface Grafana, c'est bon !**

---

### Test 4 : Prometheus ✅

**URL :** http://localhost:9090

**Tester une requête :**
1. Dans la barre de recherche, tapez : `up`
2. Cliquez "Execute"
3. Vous verrez les services monitorés

**✅ Si vous voyez des métriques, c'est bon !**

---

## 🔧 PARTIE 2 : Configurer Jenkins (10 minutes)

### Étape 1 : Configurer les Outils

1. **Aller dans la configuration**
   - Cliquez sur "Manage Jenkins" (à gauche)
   - Cliquez sur "Tools"

2. **Configurer Maven**
   - Scrollez jusqu'à "Maven installations"
   - Cliquez "Add Maven"
   - Name : `Maven-3.9.6`
   - ☑ Cochez "Install automatically"
   - Version : `3.9.6`

3. **Configurer JDK**
   - Scrollez jusqu'à "JDK installations"
   - Cliquez "Add JDK"
   - Name : `JDK-17`
   - ☑ Cochez "Install automatically"
   - Install from : `adoptium.net`
   - Version : `jdk-17.0.9+9`

4. **Sauvegarder**
   - Cliquez "Save" en bas de la page

**✅ Outils configurés !**

---

### Étape 2 : Créer le Pipeline

1. **Retour à l'accueil**
   - Cliquez sur le logo Jenkins en haut à gauche

2. **Nouveau Job**
   - Cliquez "New Item" (à gauche)
   - Nom : `smartek-sponsor-pipeline`
   - Type : Sélectionnez "Pipeline"
   - Cliquez "OK"

3. **Configuration du Pipeline**
   
   **Dans "General" :**
   - Description : `Pipeline CI/CD pour Smartek Sponsor`

   **Dans "Pipeline" :**
   - Definition : Sélectionnez "Pipeline script"
   - Script : Copiez-collez ce code :

```groovy
pipeline {
    agent any
    
    environment {
        APP_NAME = 'smartek-sponsor'
        APP_VERSION = '0.0.1-SNAPSHOT'
    }
    
    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }
    
    stages {
        stage('📥 1. Checkout') {
            steps {
                script {
                    echo "=========================================="
                    echo "Recuperation du code source..."
                    echo "=========================================="
                    echo "Code source pret !"
                }
            }
        }
        
        stage('🔨 2. Build') {
            steps {
                script {
                    echo "=========================================="
                    echo "Compilation du code Java..."
                    echo "=========================================="
                    
                    dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                        bat 'mvn clean compile'
                    }
                    
                    echo "Compilation reussie !"
                }
            }
        }
        
        stage('🧪 3. Tests') {
            steps {
                script {
                    echo "=========================================="
                    echo "Execution des tests..."
                    echo "=========================================="
                    
                    dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                        bat 'mvn test'
                    }
                    
                    echo "Tests reussis !"
                }
            }
        }
        
        stage('📦 4. Package') {
            steps {
                script {
                    echo "=========================================="
                    echo "Creation du fichier JAR..."
                    echo "=========================================="
                    
                    dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                        bat 'mvn package -DskipTests'
                    }
                    
                    echo "JAR cree avec succes !"
                }
            }
        }
        
        stage('🐳 5. Docker Build') {
            steps {
                script {
                    echo "=========================================="
                    echo "Construction de l image Docker..."
                    echo "=========================================="
                    
                    dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                        bat """
                            docker build -t ${APP_NAME}:${BUILD_NUMBER} .
                            docker tag ${APP_NAME}:${BUILD_NUMBER} ${APP_NAME}:latest
                        """
                    }
                    
                    echo "Image Docker creee !"
                }
            }
        }
        
        stage('✅ 6. Verification') {
            steps {
                script {
                    echo "=========================================="
                    echo "Verification finale..."
                    echo "=========================================="
                    
                    dir('C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor') {
                        bat 'dir target\\*.jar'
                        bat "docker images | findstr ${APP_NAME}"
                    }
                    
                    echo "Toutes les verifications sont OK !"
                }
            }
        }
    }
    
    post {
        success {
            script {
                echo "=========================================="
                echo "PIPELINE REUSSI !"
                echo "=========================================="
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Duree: ${currentBuild.durationString}"
            }
        }
        
        failure {
            script {
                echo "=========================================="
                echo "PIPELINE ECHOUE !"
                echo "=========================================="
            }
        }
    }
}
```

4. **Sauvegarder**
   - Cliquez "Save" en bas

**✅ Pipeline créé !**

---

## 🚀 PARTIE 3 : Lancer le Premier Build (5 minutes)

### Étape 1 : Démarrer le Build

1. Vous êtes sur la page du job `smartek-sponsor-pipeline`
2. Cliquez sur "Build Now" (à gauche)
3. Un nouveau build apparaît dans "Build History"

### Étape 2 : Suivre l'Exécution

1. Cliquez sur le numéro du build (ex: #1)
2. Cliquez sur "Console Output"
3. Vous verrez les logs en temps réel

### Étape 3 : Attendre le Résultat

Le build va :
1. ✅ Checkout (instantané)
2. ✅ Build (2-3 minutes)
3. ✅ Tests (1-2 minutes)
4. ✅ Package (1 minute)
5. ✅ Docker Build (2-3 minutes)
6. ✅ Verification (30 secondes)

**Durée totale : 7-10 minutes**

### Étape 4 : Vérifier le Résultat

**Si tout est vert :**
```
Finished: SUCCESS
```
✅ **Parfait ! Le pipeline fonctionne !**

**Si c'est rouge :**
```
Finished: FAILURE
```
❌ Lisez l'erreur dans les logs et corrigez

---

## 📊 PARTIE 4 : Vérifier les Résultats (2 minutes)

### Test 1 : Vérifier le JAR créé

```powershell
dir Backend\smartek_sponsor\target\*.jar
```

**Vous devriez voir :**
```
smartek-sponsor-0.0.1-SNAPSHOT.jar
```

### Test 2 : Vérifier l'Image Docker

```powershell
docker images | findstr smartek-sponsor
```

**Vous devriez voir :**
```
smartek-sponsor   1        ...   ...   ...
smartek-sponsor   latest   ...   ...   ...
```

### Test 3 : Voir l'Historique des Builds

1. Retournez sur http://localhost:9091
2. Cliquez sur `smartek-sponsor-pipeline`
3. Vous verrez l'historique des builds avec :
   - Numéro du build
   - Statut (boule bleue = succès)
   - Durée
   - Date

---

## 🎓 PARTIE 5 : Tests pour la Présentation

### Test Complet Devant le Prof

**Scénario :**

1. **Montrer l'application**
   ```
   http://localhost:8080/actuator/health
   ```
   > "Voici mon application qui tourne"

2. **Montrer Grafana**
   ```
   http://localhost:3000
   ```
   > "Voici le monitoring en temps réel"

3. **Montrer Jenkins**
   ```
   http://localhost:9091
   ```
   > "Voici mon pipeline CI/CD"

4. **Lancer un build**
   - Cliquez "Build Now"
   - Montrez les logs
   > "Regardez, tout s'exécute automatiquement"

5. **Montrer le résultat**
   - Attendez le SUCCESS
   > "Voilà ! L'application est compilée, testée, et l'image Docker est créée"

---

## ✅ Checklist Finale

Avant la présentation, vérifiez :

- [ ] Application répond sur http://localhost:8080/actuator/health
- [ ] Jenkins accessible sur http://localhost:9091
- [ ] Grafana accessible sur http://localhost:3000
- [ ] Prometheus accessible sur http://localhost:9090
- [ ] Pipeline Jenkins créé
- [ ] Au moins un build a réussi
- [ ] Vous savez lancer un build
- [ ] Vous savez voir les logs

---

## 🐛 Troubleshooting

### Problème : Jenkins ne répond pas

```powershell
docker restart jenkins
# Attendre 1 minute
# Réessayer http://localhost:9091
```

### Problème : Build échoue

1. Cliquer sur le build
2. Cliquer "Console Output"
3. Lire l'erreur en rouge
4. Corriger le problème
5. Relancer le build

### Problème : Maven ou JDK non trouvé

1. Manage Jenkins → Tools
2. Vérifier Maven-3.9.6 et JDK-17
3. Sauvegarder
4. Relancer le build

---

## 🎉 Vous êtes Prêt !

Si tous les tests passent, vous êtes prêt pour la présentation !

**Bonne chance ! 🚀**
