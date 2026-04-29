# Fix Kubernetes in Docker Desktop
Write-Host "==================================================" -ForegroundColor Red
Write-Host "KUBERNETES TROUBLESHOOTING" -ForegroundColor Red
Write-Host "==================================================" -ForegroundColor Red
Write-Host ""

Write-Host "Your Kubernetes cluster has errors." -ForegroundColor Yellow
Write-Host ""
Write-Host "TO FIX:" -ForegroundColor White
Write-Host ""
Write-Host "1. Open Docker Desktop" -ForegroundColor Cyan
Write-Host "2. Click Settings (gear icon)" -ForegroundColor Cyan
Write-Host "3. Go to: Kubernetes" -ForegroundColor Cyan
Write-Host "4. Click: Reset Kubernetes Cluster" -ForegroundColor Yellow
Write-Host "5. Confirm the reset" -ForegroundColor Yellow
Write-Host "6. Wait 2-3 minutes for Kubernetes to restart" -ForegroundColor Cyan
Write-Host ""
Write-Host "OR (Alternative):" -ForegroundColor White
Write-Host ""
Write-Host "1. Uncheck 'Enable Kubernetes'" -ForegroundColor Cyan
Write-Host "2. Click Apply & Restart" -ForegroundColor Cyan
Write-Host "3. Wait 30 seconds" -ForegroundColor Cyan
Write-Host "4. Check 'Enable Kubernetes' again" -ForegroundColor Cyan
Write-Host "5. Click Apply & Restart" -ForegroundColor Cyan
Write-Host "6. Wait 2-3 minutes" -ForegroundColor Cyan
Write-Host ""
Write-Host "After fixing, run:" -ForegroundColor Green
Write-Host "  kubectl cluster-info" -ForegroundColor White
Write-Host "  kubectl get nodes" -ForegroundColor White
Write-Host ""
