# Script pour démarrer Jenkins avec Docker
# Pour Windows PowerShell

Write-Host "========================================" -ForegroundColor Green
Write-Host "Installation de Jenkins avec Docker" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Vérifier si Docker est installé
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker n'est pas installé !" -ForegroundColor Red
    Write-Host "Installez Docker Desktop depuis : https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n✅ Docker est installé" -ForegroundColor Green

# Vérifier si Jenkins tourne déjà
$jenkinsRunning = docker ps -a --filter "name=jenkins" --format "{{.Names}}"

if ($jenkinsRunning -eq "jenkins") {
    Write-Host "`n⚠️  Jenkins est déjà installé" -ForegroundColor Yellow
    
    $status = docker ps --filter "name=jenkins" --format "{{.Status}}"
    
    if ($status) {
        Write-Host "✅ Jenkins est en cours d'exécution" -ForegroundColor Green
        Write-Host "`nAccédez à Jenkins : http://localhost:8080" -ForegroundColor Cyan
    } else {
        Write-Host "Jenkins est arrêté. Démarrage..." -ForegroundColor Yellow
        docker start jenkins
        Write-Host "✅ Jenkins démarré !" -ForegroundColor Green
        Write-Host "`nAccédez à Jenkins : http://localhost:8080" -ForegroundColor Cyan
    }
    
    exit 0
}

Write-Host "`n📦 Création du volume Jenkins..." -ForegroundColor Yellow
docker volume create jenkins-data

Write-Host "`n🚀 Démarrage de Jenkins..." -ForegroundColor Yellow
docker run -d `
  --name jenkins `
  -p 8080:8080 `
  -p 50000:50000 `
  -v jenkins-data:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  --restart unless-stopped `
  jenkins/jenkins:lts

Write-Host "`n⏳ Attente du démarrage de Jenkins (60 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✅ Jenkins est installé et démarré !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`n📋 Informations de connexion :" -ForegroundColor Cyan
Write-Host "URL : http://localhost:8080" -ForegroundColor White

Write-Host "`n🔑 Mot de passe initial :" -ForegroundColor Cyan
$password = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
Write-Host $password -ForegroundColor Yellow

Write-Host "`n📝 Prochaines étapes :" -ForegroundColor Cyan
Write-Host "1. Ouvrez http://localhost:8080 dans votre navigateur" -ForegroundColor White
Write-Host "2. Collez le mot de passe ci-dessus" -ForegroundColor White
Write-Host "3. Cliquez sur 'Install suggested plugins'" -ForegroundColor White
Write-Host "4. Créez un compte admin" -ForegroundColor White
Write-Host "5. Commencez à utiliser Jenkins !" -ForegroundColor White

Write-Host "`n🛠️  Commandes utiles :" -ForegroundColor Cyan
Write-Host "Arrêter Jenkins  : docker stop jenkins" -ForegroundColor White
Write-Host "Démarrer Jenkins : docker start jenkins" -ForegroundColor White
Write-Host "Voir les logs    : docker logs -f jenkins" -ForegroundColor White
Write-Host "Supprimer Jenkins: docker rm -f jenkins" -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Green
