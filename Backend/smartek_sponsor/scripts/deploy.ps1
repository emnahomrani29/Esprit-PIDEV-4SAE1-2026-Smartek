# Deploy script for Windows PowerShell
# Smartek Sponsor Service Deployment

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Smartek Sponsor Service Deployment" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

$NAMESPACE = "smartek-production"
$APP_NAME = "smartek-sponsor"
$K8S_DIR = ".\k8s"

# Check if kubectl is installed
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: kubectl is not installed" -ForegroundColor Red
    exit 1
}

# Check if connected to cluster
try {
    kubectl cluster-info | Out-Null
} catch {
    Write-Host "Error: Not connected to Kubernetes cluster" -ForegroundColor Red
    exit 1
}

Write-Host "`nStep 1: Creating namespace..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\namespace.yaml"

Write-Host "`nStep 2: Creating ConfigMap..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\configmap.yaml"

Write-Host "`nStep 3: Creating Secret..." -ForegroundColor Yellow
Write-Host "WARNING: Update secrets before production deployment!" -ForegroundColor Red
kubectl apply -f "$K8S_DIR\secret.yaml"

Write-Host "`nStep 4: Creating Service..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\service.yaml"

Write-Host "`nStep 5: Creating Deployment..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\deployment.yaml"

Write-Host "`nStep 6: Creating HPA..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\hpa.yaml"

Write-Host "`nStep 7: Creating Ingress..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\ingress.yaml"

Write-Host "`nStep 8: Creating ServiceMonitor for Prometheus..." -ForegroundColor Yellow
kubectl apply -f "$K8S_DIR\servicemonitor.yaml"

Write-Host "`nStep 9: Waiting for deployment to be ready..." -ForegroundColor Yellow
kubectl rollout status deployment/$APP_NAME-deployment -n $NAMESPACE --timeout=5m

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Deployment completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Display deployment info
Write-Host "`nDeployment Information:" -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE -l app=$APP_NAME
kubectl get svc -n $NAMESPACE -l app=$APP_NAME
kubectl get ingress -n $NAMESPACE

Write-Host "`nTo view logs:" -ForegroundColor Yellow
Write-Host "kubectl logs -f -n $NAMESPACE -l app=$APP_NAME"

Write-Host "`nTo check health:" -ForegroundColor Yellow
Write-Host "kubectl exec -n $NAMESPACE -it `$(kubectl get pod -n $NAMESPACE -l app=$APP_NAME -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:8080/actuator/health"
