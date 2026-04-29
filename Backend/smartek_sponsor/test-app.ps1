# Script de test pour l'application Smartek Sponsor

Write-Host "========================================" -ForegroundColor Green
Write-Host "Test de l'Application Smartek Sponsor" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`n1. Vérification des conteneurs..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`n2. Test du endpoint Health..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    Write-Host "✅ Application Status: $($response.status)" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}

Write-Host "`n3. Test du endpoint Info..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/info" -Method Get
    Write-Host "✅ Info récupérée" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "⚠️  Endpoint info non disponible (normal)" -ForegroundColor Yellow
}

Write-Host "`n4. Test du endpoint Prometheus..." -ForegroundColor Yellow
try {
    $metrics = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -UseBasicParsing
    Write-Host "✅ Métriques Prometheus disponibles ($($metrics.Content.Length) bytes)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Services disponibles:" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ Application:  http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "✅ Grafana:      http://localhost:3000 (admin/admin)" -ForegroundColor Cyan
Write-Host "✅ Prometheus:   http://localhost:9090" -ForegroundColor Cyan
Write-Host "✅ MySQL:        localhost:3306" -ForegroundColor Cyan

Write-Host "`nPour voir les logs:" -ForegroundColor Yellow
Write-Host "docker-compose logs -f smartek-sponsor" -ForegroundColor White
