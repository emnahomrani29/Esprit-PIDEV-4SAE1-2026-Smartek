# 🪟 Guide Windows - Smartek Sponsor Pipeline

## 📋 Vue d'ensemble

Ce guide est spécifiquement conçu pour les utilisateurs Windows qui souhaitent travailler avec le pipeline CI/CD du service Smartek Sponsor.

## 🔧 Prérequis Windows

### 1. Installer WSL2 (Recommandé)

WSL2 permet d'exécuter Linux sur Windows et facilite grandement le développement.

```powershell
# Ouvrir PowerShell en tant qu'administrateur
wsl --install

# Redémarrer l'ordinateur
# Après redémarrage, installer Ubuntu
wsl --install -d Ubuntu-22.04

# Vérifier l'installation
wsl --list --verbose
```

### 2. Installer Docker Desktop

```powershell
# Télécharger depuis https://www.docker.com/products/docker-desktop
# Installer et activer l'intégration WSL2
```

### 3. Installer les outils nécessaires

#### Via Chocolatey (Gestionnaire de paquets Windows)

```powershell
# Installer Chocolatey (PowerShell Admin)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Installer les outils
choco install git -y
choco install maven -y
choco install openjdk17 -y
choco install kubernetes-cli -y
choco install kubernetes-helm -y
```

#### Vérification

```powershell
git --version
mvn --version
java -version
kubectl version --client
helm version
```

## 🚀 Configuration de l'environnement

### 1. Cloner le repository

```powershell
# PowerShell
cd C:\Projects
git clone <your-repo-url>
cd smartek_sponsor\Backend\smartek_sponsor
```

### 2. Configuration Maven

Créer le fichier `C:\Users\<VotreNom>\.m2\settings.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>nexus</id>
      <username>admin</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
```

### 3. Configuration Docker

```powershell
# Vérifier Docker
docker --version
docker ps

# Login au registry Nexus
docker login your-nexus-registry:8083
```

### 4. Configuration kubectl

```powershell
# Créer le dossier .kube
mkdir $HOME\.kube

# Copier le fichier kubeconfig
# Depuis votre cluster K8s, copier le fichier config dans $HOME\.kube\config

# Vérifier la connexion
kubectl cluster-info
kubectl get nodes
```

## 🏗️ Build local sur Windows

### Option 1 : PowerShell natif

```powershell
# Naviguer vers le projet
cd Backend\smartek_sponsor

# Clean et compile
mvn clean compile

# Exécuter les tests
mvn test

# Package
mvn package

# Build Docker image
docker build -t smartek-sponsor:local .

# Run container
docker run -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=dev `
  smartek-sponsor:local
```

### Option 2 : WSL2 (Recommandé)

```bash
# Ouvrir WSL2
wsl

# Naviguer vers le projet (depuis WSL)
cd /mnt/c/Projects/smartek_sponsor/Backend/smartek_sponsor

# Utiliser les scripts bash
chmod +x scripts/*.sh
./scripts/local-build.sh
```

## 🐳 Docker Compose sur Windows

### Démarrer l'environnement

```powershell
# PowerShell
cd Backend\smartek_sponsor

# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f smartek-sponsor

# Arrêter les services
docker-compose down
```

### Accéder aux services

- Application : http://localhost:8080
- Prometheus : http://localhost:9090
- Grafana : http://localhost:3000 (admin/admin)
- MySQL : localhost:3306

## ☸️ Déploiement Kubernetes depuis Windows

### Méthode 1 : kubectl natif

```powershell
# Créer le namespace
kubectl apply -f k8s\namespace.yaml

# Appliquer tous les manifestes
kubectl apply -f k8s\configmap.yaml
kubectl apply -f k8s\secret.yaml
kubectl apply -f k8s\deployment.yaml
kubectl apply -f k8s\service.yaml
kubectl apply -f k8s\ingress.yaml
kubectl apply -f k8s\hpa.yaml
kubectl apply -f k8s\servicemonitor.yaml

# Vérifier le déploiement
kubectl get pods -n smartek-production -l app=smartek-sponsor

# Voir les logs
kubectl logs -f -n smartek-production -l app=smartek-sponsor
```

### Méthode 2 : Via WSL2

```bash
# Ouvrir WSL2
wsl

# Naviguer vers le projet
cd /mnt/c/Projects/smartek_sponsor/Backend/smartek_sponsor

# Exécuter le script de déploiement
./scripts/deploy.sh
```

## 🔄 Scripts PowerShell équivalents

### deploy.ps1

Créer `scripts\deploy.ps1` :

```powershell
# Deploy script for Windows
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Smartek Sponsor Service Deployment" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

$NAMESPACE = "smartek-production"
$APP_NAME = "smartek-sponsor"
$K8S_DIR = ".\k8s"

# Check kubectl
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: kubectl is not installed" -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Creating namespace..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\namespace.yaml"

Write-Host "Step 2: Creating ConfigMap..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\configmap.yaml"

Write-Host "Step 3: Creating Secret..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\secret.yaml"

Write-Host "Step 4: Creating Service..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\service.yaml"

Write-Host "Step 5: Creating Deployment..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\deployment.yaml"

Write-Host "Step 6: Creating HPA..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\hpa.yaml"

Write-Host "Step 7: Creating Ingress..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\ingress.yaml"

Write-Host "Step 8: Creating ServiceMonitor..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\servicemonitor.yaml"

Write-Host "Step 9: Waiting for deployment..." -ForegroundColor Yellow
kubectl rollout status deployment/$APP_NAME-deployment -n $NAMESPACE --timeout=5m

Write-Host "========================================" -ForegroundColor Green
Write-Host "Deployment completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Display info
kubectl get pods -n $NAMESPACE -l app=$APP_NAME
kubectl get svc -n $NAMESPACE -l app=$APP_NAME
```

### rollback.ps1

Créer `scripts\rollback.ps1` :

```powershell
# Rollback script for Windows
$ErrorActionPreference = "Stop"

$NAMESPACE = "smartek-production"
$DEPLOYMENT = "smartek-sponsor-deployment"

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Rolling back $DEPLOYMENT..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# Show history
Write-Host "`nRollout History:" -ForegroundColor Yellow
kubectl rollout history deployment/$DEPLOYMENT -n $NAMESPACE

# Perform rollback
Write-Host "`nPerforming rollback..." -ForegroundColor Yellow
kubectl rollout undo deployment/$DEPLOYMENT -n $NAMESPACE

# Wait for completion
Write-Host "`nWaiting for rollback to complete..." -ForegroundColor Yellow
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE --timeout=5m

Write-Host "`nRollback completed successfully!" -ForegroundColor Green

# Display status
kubectl get pods -n $NAMESPACE -l app=smartek-sponsor
```

### local-build.ps1

Créer `scripts\local-build.ps1` :

```powershell
# Local build script for Windows
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Smartek Sponsor - Local Build" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Check Maven
if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Maven is not installed" -ForegroundColor Red
    exit 1
}

# Check Docker
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Docker is not installed" -ForegroundColor Red
    exit 1
}

Write-Host "`nStep 1: Cleaning previous builds..." -ForegroundColor Yellow
mvn clean

Write-Host "`nStep 2: Compiling source code..." -ForegroundColor Yellow
mvn compile

Write-Host "`nStep 3: Running unit tests..." -ForegroundColor Yellow
mvn test

Write-Host "`nStep 4: Generating code coverage report..." -ForegroundColor Yellow
mvn jacoco:report

Write-Host "`nStep 5: Packaging application..." -ForegroundColor Yellow
mvn package -DskipTests

Write-Host "`nStep 6: Building Docker image..." -ForegroundColor Yellow
docker build -t smartek-sponsor:local .

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nArtifacts:" -ForegroundColor Cyan
Write-Host "JAR: target\smartek-sponsor-0.0.1-SNAPSHOT.jar"
Write-Host "Docker Image: smartek-sponsor:local"

Write-Host "`nCoverage Report:" -ForegroundColor Cyan
Write-Host "target\site\jacoco\index.html"

Write-Host "`nTo run the application locally:" -ForegroundColor Yellow
Write-Host "docker run -p 8080:8080 smartek-sponsor:local"
```

## 🔧 Exécution des scripts PowerShell

```powershell
# Autoriser l'exécution de scripts (une seule fois)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Exécuter les scripts
.\scripts\local-build.ps1
.\scripts\deploy.ps1
.\scripts\rollback.ps1
```

## 🐛 Troubleshooting Windows

### Problème : Scripts bash ne fonctionnent pas

**Solution** : Utiliser WSL2 ou les scripts PowerShell équivalents

```powershell
# Option 1 : WSL2
wsl
cd /mnt/c/Projects/...
./scripts/deploy.sh

# Option 2 : PowerShell
.\scripts\deploy.ps1
```

### Problème : Docker ne démarre pas

**Solution** :

```powershell
# Vérifier Docker Desktop
# Redémarrer Docker Desktop
# Vérifier WSL2 integration dans Docker Desktop settings
```

### Problème : kubectl ne trouve pas le cluster

**Solution** :

```powershell
# Vérifier le fichier config
cat $HOME\.kube\config

# Tester la connexion
kubectl cluster-info

# Si problème, copier à nouveau le kubeconfig depuis le cluster
```

### Problème : Maven ne trouve pas Java

**Solution** :

```powershell
# Vérifier JAVA_HOME
echo $env:JAVA_HOME

# Si vide, définir
$env:JAVA_HOME = "C:\Program Files\OpenJDK\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Vérifier
java -version
```

### Problème : Chemins de fichiers

**Solution** : Utiliser des backslashes `\` au lieu de `/` dans PowerShell

```powershell
# Correct pour PowerShell
kubectl apply -f k8s\deployment.yaml

# Correct pour WSL/Bash
kubectl apply -f k8s/deployment.yaml
```

## 📊 Monitoring depuis Windows

### Port-forward Grafana

```powershell
# PowerShell
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Ouvrir dans le navigateur
Start-Process "http://localhost:3000"
```

### Port-forward Prometheus

```powershell
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090

# Ouvrir dans le navigateur
Start-Process "http://localhost:9090"
```

### Port-forward Application

```powershell
kubectl port-forward -n smartek-production svc/smartek-sponsor-service 8080:8080

# Tester
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
```

## 🎯 Commandes utiles Windows

### PowerShell

```powershell
# Voir les pods
kubectl get pods -n smartek-production

# Logs en temps réel
kubectl logs -f -n smartek-production -l app=smartek-sponsor

# Décrire un pod
kubectl describe pod <pod-name> -n smartek-production

# Exécuter une commande dans un pod
kubectl exec -it <pod-name> -n smartek-production -- /bin/sh

# Copier un fichier depuis un pod
kubectl cp smartek-production/<pod-name>:/app/logs/app.log ./app.log

# Scaler le deployment
kubectl scale deployment smartek-sponsor-deployment --replicas=5 -n smartek-production
```

### Git Bash (Alternative)

Si vous préférez Git Bash sur Windows :

```bash
# Git Bash supporte les commandes Linux
cd /c/Projects/smartek_sponsor/Backend/smartek_sponsor
./scripts/deploy.sh
```

## 🔗 Liens utiles

### Téléchargements
- Docker Desktop : https://www.docker.com/products/docker-desktop
- Git for Windows : https://git-scm.com/download/win
- Maven : https://maven.apache.org/download.cgi
- OpenJDK : https://adoptium.net/
- kubectl : https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
- Helm : https://helm.sh/docs/intro/install/

### Documentation
- WSL2 : https://docs.microsoft.com/en-us/windows/wsl/
- Docker Desktop WSL2 : https://docs.docker.com/desktop/windows/wsl/
- Chocolatey : https://chocolatey.org/

## ✅ Checklist Windows

- [ ] WSL2 installé et configuré
- [ ] Docker Desktop installé avec intégration WSL2
- [ ] Git installé
- [ ] Maven installé
- [ ] Java 17 installé
- [ ] kubectl installé et configuré
- [ ] Helm installé
- [ ] Repository cloné
- [ ] Scripts PowerShell créés
- [ ] Build local réussi
- [ ] Docker Compose fonctionne
- [ ] Connexion au cluster K8s OK

## 📞 Support

Pour les problèmes spécifiques à Windows :
- Email : team@smartek.com
- Slack : #smartek-windows-support
- Documentation : README.md

## 🎓 Recommandations

1. **Utiliser WSL2** : Meilleure compatibilité avec les outils Linux
2. **Docker Desktop** : Intégration WSL2 activée
3. **PowerShell 7+** : Version moderne avec plus de fonctionnalités
4. **Windows Terminal** : Meilleure expérience de terminal
5. **VS Code** : Avec extensions WSL et Docker

---

**Bon développement sur Windows ! 🪟🚀**
