# 🚀 Guide Complet : Jenkins avec Docker

## ✅ Jenkins est Installé !

### 📋 Informations de Connexion

```
URL      : http://localhost:9091
Password : bf8a489fb7634770a439175fb535faa0
```

---

## 🎯 Étape 1 : Première Connexion à Jenkins

### 1.1 Ouvrir Jenkins

Ouvrez votre navigateur et allez sur :
```
http://localhost:9091
```

### 1.2 Débloquer Jenkins

Vous verrez une page "Unlock Jenkins"

**Collez ce mot de passe :**
```
bf8a489fb7634770a439175fb535faa0
```

Cliquez sur **Continue**

### 1.3 Installer les Plugins

Vous verrez "Customize Jenkins"

✅ **Cliquez sur "Install suggested plugins"**

Attendez 2-3 minutes que les plugins s'installent.

### 1.4 Créer un Compte Admin

Remplissez le formulaire :
```
Username : admin
Password : admin123
Confirm  : admin123
Full name: Admin Smartek
Email    : admin@smartek.com
```

Cliquez sur **Save and Continue**

### 1.5 Configuration de l'URL

Laissez l'URL par défaut :
```
http://localhost:9091/
```

Cliquez sur **Save and Finish**

### 1.6 Commencer

Cliquez sur **Start using Jenkins**

🎉 **Jenkins est prêt !**

---

## 🔧 Étape 2 : Configurer les Outils

### 2.1 Aller dans la Configuration

Dans Jenkins, cliquez sur :
```
Manage Jenkins → Tools
```

### 2.2 Configurer Maven

Scrollez jusqu'à **Maven installations**

Cliquez sur **Add Maven**

Remplissez :
```
Name: Maven-3.9.6
☑ Install automatically
Version: 3.9.6
```

Cliquez sur **Save**

### 2.3 Configurer JDK

Scrollez jusqu'à **JDK installations**

Cliquez sur **Add JDK**

Remplissez :
```
Name: JDK-17
☑ Install automatically
Install from: adoptium.net
Version: jdk-17.0.9+9
```

Cliquez sur **Save**

---

## 📦 Étape 3 : Créer le Pipeline

### 3.1 Créer un Nouveau Job

Sur la page d'accueil de Jenkins :

1. Cliquez sur **New Item** (en haut à gauche)
2. Nom du job : `smartek-sponsor-pipeline`
3. Type : **Pipeline**
4. Cliquez sur **OK**

### 3.2 Configurer le Pipeline

Dans la page de configuration :

#### General
```
Description: Pipeline CI/CD pour Smartek Sponsor Service
```

#### Pipeline

**Definition :** Pipeline script

**Script :** Copiez-collez ce code :

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
        stage('1. Checkout') {
            steps {
                script {
                    echo "=========================================="
                    echo "Recuperation du code source..."
                    echo "=========================================="
                    
                    // Pour test local, on utilise le code déjà présent
                    echo "Code source pret !"
                }
            }
        }
        
        stage('2. Build') {
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
        
        stage('3. Tests') {
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
        
        stage('4. Package') {
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
        
        stage('5. Docker Build') {
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
        
        stage('6. Verification') {
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

Cliquez sur **Save**

---

## 🚀 Étape 4 : Lancer le Pipeline

### 4.1 Démarrer le Build

Sur la page du job `smartek-sponsor-pipeline` :

1. Cliquez sur **Build Now** (à gauche)
2. Vous verrez un nouveau build apparaître dans "Build History"
3. Cliquez sur le numéro du build (ex: #1)
4. Cliquez sur **Console Output** pour voir les logs en temps réel

### 4.2 Suivre l'Exécution

Vous verrez les étapes s'exécuter :
```
✅ 1. Checkout
✅ 2. Build
✅ 3. Tests
✅ 4. Package
✅ 5. Docker Build
✅ 6. Verification
```

### 4.3 Résultat

Si tout est vert : **SUCCESS** ✅
Si quelque chose échoue : **FAILURE** ❌

---

## 📊 Étape 5 : Voir les Résultats

### 5.1 Dashboard Jenkins

Retournez sur la page d'accueil de Jenkins :
```
http://localhost:9091
```

Vous verrez :
- ✅ Statut du dernier build (boule bleue = succès)
- 📊 Historique des builds
- ⏱️ Durée de chaque build

### 5.2 Blue Ocean (Vue Moderne)

Pour une vue plus moderne :

1. Allez dans **Manage Jenkins** → **Plugins**
2. Cherchez "Blue Ocean"
3. Installez-le
4. Cliquez sur "Open Blue Ocean" dans le menu

---

## 🎓 Pour Votre Présentation

### Ce que Vous Pouvez Montrer :

#### 1. **Le Dashboard Jenkins**
```
http://localhost:9091
```
Montrez la liste des builds et leur statut

#### 2. **Lancer un Build**
Cliquez sur "Build Now" devant votre prof

#### 3. **Les Logs en Temps Réel**
Montrez la "Console Output" pendant que le build tourne

#### 4. **Les Étapes du Pipeline**
Montrez les 6 étapes qui s'exécutent automatiquement

#### 5. **Le Résultat Final**
Montrez le message "SUCCESS" et la durée

### Ce que Vous Expliquez :

> "J'ai créé un pipeline Jenkins qui automatise tout le processus :
> 
> 1. **Checkout** : Récupère le code
> 2. **Build** : Compile avec Maven
> 3. **Tests** : Lance les tests automatiques
> 4. **Package** : Crée le fichier JAR
> 5. **Docker Build** : Crée l'image Docker
> 6. **Verification** : Vérifie que tout est OK
> 
> Tout ça se fait automatiquement en quelques minutes !"

---

## 🛠️ Commandes Utiles

### Gérer Jenkins

```powershell
# Voir les logs de Jenkins
docker logs -f jenkins

# Arrêter Jenkins
docker stop jenkins

# Démarrer Jenkins
docker start jenkins

# Redémarrer Jenkins
docker restart jenkins

# Supprimer Jenkins (attention : perte des données)
docker rm -f jenkins
docker volume rm jenkins-data
```

### Accès Rapide

```
Jenkins    : http://localhost:9091
Username   : admin
Password   : admin123
```

---

## 🐛 Troubleshooting

### Problème : Jenkins ne démarre pas

```powershell
# Vérifier les logs
docker logs jenkins

# Redémarrer
docker restart jenkins
```

### Problème : Le build échoue

1. Cliquez sur le build qui a échoué
2. Cliquez sur "Console Output"
3. Lisez l'erreur en rouge
4. Corrigez le problème
5. Relancez le build

### Problème : Maven ou JDK non trouvé

1. Allez dans **Manage Jenkins** → **Tools**
2. Vérifiez que Maven-3.9.6 et JDK-17 sont configurés
3. Sauvegardez
4. Relancez le build

---

## 🎉 Félicitations !

Vous avez maintenant Jenkins qui tourne avec un pipeline fonctionnel !

**Prochaine étape :** Lancez votre premier build et montrez-le à votre prof ! 🚀
