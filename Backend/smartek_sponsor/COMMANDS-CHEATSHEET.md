# ⚡ Aide-Mémoire : Commandes Essentielles

## 🎯 Commandes Git

### Vérifier l'État
```powershell
# Voir la branche actuelle
git branch

# Voir les modifications
git status

# Voir l'historique
git log --oneline -5

# Voir les remotes
git remote -v
```

### Changer de Branche
```powershell
# Basculer sur sponsor
git checkout sponsor

# Créer et basculer sur une nouvelle branche
git checkout -b nouvelle-branche
```

### Commit et Push
```powershell
# Ajouter tous les fichiers modifiés
git add .

# Ajouter un fichier spécifique
git add Jenkinsfile

# Commit
git commit -m "Votre message"

# Push vers GitHub
git push origin sponsor

# Push force (ATTENTION : dangereux)
git push -f origin sponsor
```

### Récupérer les Changements
```powershell
# Récupérer les derniers changements
git pull origin sponsor

# Voir les différences
git diff
```

---

## 🐳 Commandes Docker

### Vérifier les Conteneurs
```powershell
# Voir tous les conteneurs actifs
docker ps

# Voir tous les conteneurs (actifs et arrêtés)
docker ps -a

# Voir Jenkins spécifiquement
docker ps | findstr jenkins
```

### Gérer Jenkins
```powershell
# Démarrer Jenkins
docker start jenkins

# Arrêter Jenkins
docker stop jenkins

# Redémarrer Jenkins
docker restart jenkins

# Voir les logs Jenkins
docker logs jenkins

# Voir les logs en temps réel
docker logs -f jenkins

# Supprimer le conteneur Jenkins (ATTENTION)
docker rm -f jenkins
```

### Gérer Docker Compose
```powershell
# Démarrer tous les services
docker-compose up -d

# Arrêter tous les services
docker-compose down

# Redémarrer tous les services
docker-compose restart

# Voir les logs
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f smartek-sponsor

# Voir l'état des services
docker-compose ps

# Reconstruire les images
docker-compose build

# Reconstruire et redémarrer
docker-compose up -d --build
```

### Nettoyage Docker
```powershell
# Supprimer les conteneurs arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Tout nettoyer (ATTENTION)
docker system prune -a
```

---

## 🔧 Commandes Jenkins

### Accès Jenkins
```powershell
# Ouvrir Jenkins dans le navigateur
start http://localhost:9091

# Voir le mot de passe initial
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Informations Jenkins
```powershell
# Entrer dans le conteneur Jenkins
docker exec -it jenkins bash

# Voir les workspaces
docker exec jenkins ls -la /var/jenkins_home/workspace

# Voir les jobs
docker exec jenkins ls -la /var/jenkins_home/jobs
```

---

## 📦 Commandes Maven

### Build et Tests
```powershell
# Compiler
mvn clean compile

# Lancer les tests
mvn test

# Créer le package
mvn package

# Créer le package sans tests
mvn package -DskipTests

# Nettoyer et tout refaire
mvn clean install

# Voir la version de Maven
mvn -version
```

---

## 🌐 Commandes Réseau

### Tester les Services
```powershell
# Tester l'application
curl http://localhost:8080/actuator/health

# Tester Jenkins
curl http://localhost:9091

# Tester Grafana
curl http://localhost:3000

# Tester Prometheus
curl http://localhost:9090
```

### Ouvrir dans le Navigateur
```powershell
# Application
start http://localhost:8080/actuator/health

# Jenkins
start http://localhost:9091

# Grafana
start http://localhost:3000

# Prometheus
start http://localhost:9090
```

---

## 📂 Commandes Fichiers

### Navigation
```powershell
# Aller dans le projet
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor"

# Aller dans smartek_sponsor
cd Backend/smartek_sponsor

# Revenir au dossier parent
cd ..

# Voir le chemin actuel
pwd

# Lister les fichiers
ls

# Lister avec détails
ls -la
```

### Copier et Déplacer
```powershell
# Copier un fichier
copy source.txt destination.txt

# Copier le Jenkinsfile Git
copy Jenkinsfile.git Jenkinsfile

# Déplacer un fichier
move source.txt destination.txt

# Supprimer un fichier
rm fichier.txt
```

### Voir le Contenu
```powershell
# Voir tout le fichier
cat Jenkinsfile

# Voir les premières lignes
head -n 20 Jenkinsfile

# Voir les dernières lignes
tail -n 20 Jenkinsfile

# Chercher dans un fichier
grep "maven" Jenkinsfile
```

---

## 🔍 Commandes de Diagnostic

### Vérifier Tout
```powershell
# Vérifier Docker
docker --version
docker ps

# Vérifier Git
git --version
git status

# Vérifier Maven
mvn --version

# Vérifier Java
java -version

# Vérifier les ports utilisés
netstat -ano | findstr :8080
netstat -ano | findstr :9091
```

### Résoudre les Problèmes
```powershell
# Tuer un processus sur un port (remplacer PID)
taskkill /PID 1234 /F

# Redémarrer Docker Desktop
# (Faire clic droit sur l'icône Docker → Restart)

# Vider le cache Docker
docker system prune -a --volumes
```

---

## 🚀 Scripts Personnalisés

### Vérifier Tous les Services
```powershell
.\scripts\check-all.ps1
```

### Ouvrir Tous les Services
```powershell
.\scripts\open-all-services.ps1
```

### Déployer
```powershell
.\scripts\deploy.ps1
```

---

## 📋 Workflow Complet

### Démarrage du Projet
```powershell
# 1. Aller dans le projet
cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor"

# 2. Démarrer Docker Compose
docker-compose up -d

# 3. Démarrer Jenkins
docker start jenkins

# 4. Attendre 1 minute

# 5. Vérifier que tout tourne
docker ps
curl http://localhost:8080/actuator/health
```

### Workflow Git + Jenkins
```powershell
# 1. Modifier le code
code .

# 2. Vérifier les modifications
git status
git diff

# 3. Commit
git add .
git commit -m "Description des changements"

# 4. Push
git push origin sponsor

# 5. Ouvrir Jenkins
start http://localhost:9091

# 6. Lancer le build
# (Cliquer "Build Now" dans l'interface)
```

### Arrêt Propre
```powershell
# 1. Arrêter Docker Compose
docker-compose down

# 2. Arrêter Jenkins
docker stop jenkins

# 3. Vérifier
docker ps
```

---

## 🆘 Commandes d'Urgence

### Tout Redémarrer
```powershell
# Arrêter tout
docker-compose down
docker stop jenkins

# Attendre 10 secondes

# Redémarrer tout
docker-compose up -d
docker start jenkins

# Attendre 1 minute

# Vérifier
docker ps
```

### Réinitialiser Complètement
```powershell
# ATTENTION : Supprime tout !

# Arrêter tout
docker-compose down -v
docker stop jenkins
docker rm jenkins

# Supprimer les volumes
docker volume rm jenkins-data

# Redémarrer Docker Desktop

# Recréer tout
docker-compose up -d
# Puis recréer Jenkins (voir JENKINS-SETUP-GUIDE.md)
```

---

## 💡 Astuces

### Alias Utiles (Optionnel)
```powershell
# Ajouter dans votre profil PowerShell

# Aller dans le projet
function goto-project { cd "C:/Users/abdel/OneDrive/Desktop/smartek sponssor/Esprit-PI-4SAE1-2026-Smartek-sponsor/Backend/smartek_sponsor" }
Set-Alias gp goto-project

# Vérifier Docker
function check-docker { docker ps }
Set-Alias ck check-docker

# Ouvrir Jenkins
function open-jenkins { start http://localhost:9091 }
Set-Alias oj open-jenkins
```

### Raccourcis Clavier
```
Ctrl + C : Arrêter une commande en cours
Ctrl + L : Effacer l'écran
Tab : Auto-complétion
↑ ↓ : Naviguer dans l'historique des commandes
```

---

## 📞 Aide Rapide

### Problème : "Command not found"
→ Vérifier que l'outil est installé (docker --version, git --version, etc.)

### Problème : "Permission denied"
→ Lancer PowerShell en tant qu'administrateur

### Problème : "Port already in use"
→ Trouver et tuer le processus : netstat -ano | findstr :PORT

### Problème : "Cannot connect to Docker daemon"
→ Démarrer Docker Desktop

---

**Gardez ce fichier à portée de main ! 📌**

*Créé avec ❤️ pour votre projet Smartek Sponsor*
