# Script pour ouvrir tous les services dans le navigateur

Write-Host "========================================" -ForegroundColor Green
Write-Host "Ouverture de tous les services" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nOuverture des services dans votre navigateur..." -ForegroundColor Yellow

# Application
Write-Host "`n1. Application Spring Boot..." -ForegroundColor Cyan
Start-Process "http://localhost:8080/actuator/health"
Start-Sleep -Seconds 2

# Jenkins
Write-Host "2. Jenkins..." -ForegroundColor Cyan
Start-Process "http://localhost:9091"
Start-Sleep -Seconds 2

# Grafana
Write-Host "3. Grafana..." -ForegroundColor Cyan
Start-Process "http://localhost:3000"
Start-Sleep -Seconds 2

# Prometheus
Write-Host "4. Prometheus..." -ForegroundColor Cyan
Start-Process "http://localhost:9090"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Tous les services sont ouverts !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nCredentials:" -ForegroundColor Yellow
Write-Host "  Jenkins  : admin / admin123" -ForegroundColor White
Write-Host "  Grafana  : admin / admin" -ForegroundColor White
Write-Host "  Jenkins Password (premiere fois) : bf8a489fb7634770a439175fb535faa0" -ForegroundColor White

Write-Host "`nPour configurer Jenkins, suivez TEST-GUIDE.md" -ForegroundColor Cyan
