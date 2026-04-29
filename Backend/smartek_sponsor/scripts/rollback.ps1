# Rollback script for Windows PowerShell
# Smartek Sponsor Service Rollback

$ErrorActionPreference = "Stop"

$NAMESPACE = "smartek-production"
$DEPLOYMENT = "smartek-sponsor-deployment"

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Rolling back $DEPLOYMENT..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# Show rollout history
Write-Host "`nRollout History:" -ForegroundColor Yellow
kubectl rollout history deployment/$DEPLOYMENT -n $NAMESPACE

# Perform rollback
Write-Host "`nPerforming rollback..." -ForegroundColor Yellow
kubectl rollout undo deployment/$DEPLOYMENT -n $NAMESPACE

# Wait for rollback to complete
Write-Host "`nWaiting for rollback to complete..." -ForegroundColor Yellow
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE --timeout=5m

Write-Host "`nRollback completed successfully!" -ForegroundColor Green

# Display current status
Write-Host "`nCurrent Status:" -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE -l app=smartek-sponsor
