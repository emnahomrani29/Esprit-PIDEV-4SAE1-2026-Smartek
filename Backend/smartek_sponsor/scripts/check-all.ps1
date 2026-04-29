# Script de vérification complète
# Vérifie que tous les services fonctionnent

Write-Host "========================================" -ForegroundColor Green
Write-Host "Verification Complete du Systeme" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

$allGood = $true

# 1. Vérifier Docker
Write-Host "`n[1/6] Verification Docker..." -ForegroundColor Yellow
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "  OK Docker est installe" -ForegroundColor Green
} else {
    Write-Host "  ERREUR Docker n'est pas installe" -ForegroundColor Red
    $allGood = $false
}

# 2. Vérifier Docker Compose
Write-Host "`n[2/6] Verification Docker Compose..." -ForegroundColor Yellow
$composeStatus = docker-compose ps 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  OK Docker Compose fonctionne" -ForegroundColor Green
    docker-compose ps
} else {
    Write-Host "  ERREUR Docker Compose ne fonctionne pas" -ForegroundColor Red
    $allGood = $false
}

# 3. Vérifier l'Application
Write-Host "`n[3/6] Verification Application..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5
    if ($response.status -eq "UP") {
        Write-Host "  OK Application est UP" -ForegroundColor Green
    } else {
        Write-Host "  AVERTISSEMENT Application status: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ERREUR Application ne repond pas" -ForegroundColor Red
    $allGood = $false
}

# 4. Vérifier Jenkins
Write-Host "`n[4/6] Verification Jenkins..." -ForegroundColor Yellow
$jenkinsRunning = docker ps --filter "name=jenkins" --format "{{.Status}}"
if ($jenkinsRunning) {
    Write-Host "  OK Jenkins est en cours d'execution" -ForegroundColor Green
    Write-Host "  URL: http://localhost:9091" -ForegroundColor Cyan
} else {
    Write-Host "  ERREUR Jenkins ne tourne pas" -ForegroundColor Red
    $allGood = $false
}

# 5. Vérifier Grafana
Write-Host "`n[5/6] Verification Grafana..." -ForegroundColor Yellow
try {
    $grafana = Invoke-WebRequest -Uri "http://localhost:3000" -Method Get -TimeoutSec 5 -UseBasicParsing
    if ($grafana.StatusCode -eq 200) {
        Write-Host "  OK Grafana est accessible" -ForegroundColor Green
        Write-Host "  URL: http://localhost:3000 (admin/admin)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "  ERREUR Grafana ne repond pas" -ForegroundColor Red
    $allGood = $false
}

# 6. Vérifier Prometheus
Write-Host "`n[6/6] Verification Prometheus..." -ForegroundColor Yellow
try {
    $prometheus = Invoke-WebRequest -Uri "http://localhost:9090" -Method Get -TimeoutSec 5 -UseBasicParsing
    if ($prometheus.StatusCode -eq 200) {
        Write-Host "  OK Prometheus est accessible" -ForegroundColor Green
        Write-Host "  URL: http://localhost:9090" -ForegroundColor Cyan
    }
} catch {
    Write-Host "  ERREUR Prometheus ne repond pas" -ForegroundColor Red
    $allGood = $false
}

# Résumé
Write-Host "`n========================================" -ForegroundColor Green
if ($allGood) {
    Write-Host "TOUT FONCTIONNE PARFAITEMENT !" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Write-Host "`nServices disponibles:" -ForegroundColor Cyan
    Write-Host "  Application : http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "  Jenkins     : http://localhost:9091 (admin/admin123)" -ForegroundColor White
    Write-Host "  Grafana     : http://localhost:3000 (admin/admin)" -ForegroundColor White
    Write-Host "  Prometheus  : http://localhost:9090" -ForegroundColor White
    
    Write-Host "`nVous etes pret pour la presentation !" -ForegroundColor Green
} else {
    Write-Host "CERTAINS SERVICES NE FONCTIONNENT PAS" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    
    Write-Host "`nPour redemarrer tout:" -ForegroundColor Yellow
    Write-Host "  docker-compose down" -ForegroundColor White
    Write-Host "  docker-compose up -d" -ForegroundColor White
    Write-Host "  docker start jenkins" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Green
