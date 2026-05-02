# SMARTEK Kubernetes Deployment Script for Windows
# This script deploys all components to Kubernetes in the correct order

param(
    [string]$Namespace = "smartek",
    [string]$Registry = $env:DOCKER_REGISTRY -or "your-docker-registry"
)

$ErrorActionPreference = "Stop"

# Get current context
$context = kubectl config current-context

Write-Host "================================================" -ForegroundColor Yellow
Write-Host "SMARTEK Kubernetes Deployment Script" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Kubernetes Context: $context"
Write-Host "Namespace: $Namespace"
Write-Host "Docker Registry: $Registry"
Write-Host ""

# Function to check if resource exists
function Test-ResourceExists {
    param(
        [string]$Kind,
        [string]$Name
    )
    
    $result = kubectl get $Kind $Name -n $Namespace 2>&1
    return $LASTEXITCODE -eq 0
}

# Function to wait for deployment
function Wait-ForDeployment {
    param(
        [string]$DeploymentName
    )
    
    Write-Host "Waiting for deployment $DeploymentName to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=available --timeout=300s deployment $DeploymentName -n $Namespace
    kubectl rollout status deployment/$DeploymentName -n $Namespace --timeout=300s
}

# Function to wait for StatefulSet
function Wait-ForStatefulSet {
    param(
        [string]$StatefulSetName
    )
    
    Write-Host "Waiting for statefulset $StatefulSetName to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l app=$StatefulSetName -n $Namespace --timeout=300s
}

# Step 1: Create Namespace
Write-Host "[1/6] Creating namespace..." -ForegroundColor Green
if (-not (Test-ResourceExists "namespace" $Namespace)) {
    kubectl apply -f 01-namespace/namespace.yaml
    Write-Host "✓ Namespace created" -ForegroundColor Green
} else {
    Write-Host "✓ Namespace already exists" -ForegroundColor Green
}
Write-Host ""

# Step 2: Apply Secrets and ConfigMaps
Write-Host "[2/6] Creating secrets and configmaps..." -ForegroundColor Green
kubectl apply -f 02-secrets-configmaps/configmap.yaml
kubectl apply -f 02-secrets-configmaps/secrets.yaml
Write-Host "✓ Secrets and ConfigMaps created" -ForegroundColor Green
Write-Host ""

# Step 3: Deploy Database
Write-Host "[3/6] Deploying MySQL database..." -ForegroundColor Green
kubectl apply -f 03-database/mysql-statefulset.yaml
Wait-ForStatefulSet "mysql"
Write-Host "✓ MySQL deployed and ready" -ForegroundColor Green
Write-Host ""

# Step 4: Deploy Microservices
Write-Host "[4/6] Deploying microservices..." -ForegroundColor Green

Write-Host "  - Deploying Eureka Server..."
kubectl apply -f 04-microservices/eureka-server.yaml
Wait-ForDeployment "eureka-server"

Write-Host "  - Deploying Auth Service..."
kubectl apply -f 04-microservices/auth-service.yaml

Write-Host "  - Deploying Event, Planning, Training, Offers services..."
kubectl apply -f 04-microservices/services-part1.yaml

Write-Host "  - Deploying Exam, Course, Learning, Skill Evidence, Config services..."
kubectl apply -f 04-microservices/services-part2.yaml

Write-Host "  - Deploying API Gateway and Frontend..."
kubectl apply -f 04-microservices/gateway-frontend.yaml

Write-Host "  - Waiting for all deployments to be ready..."
kubectl wait --for=condition=available --timeout=600s deployment --all -n $Namespace

Write-Host "✓ All microservices deployed" -ForegroundColor Green
Write-Host ""

# Step 5: Deploy Ingress
Write-Host "[5/6] Deploying Ingress..." -ForegroundColor Green
kubectl apply -f 05-ingress/ingress.yaml
Write-Host "✓ Ingress deployed" -ForegroundColor Green
Write-Host ""

# Step 6: Deploy Monitoring
Write-Host "[6/6] Deploying monitoring stack..." -ForegroundColor Green
kubectl apply -f 06-monitoring/prometheus-grafana.yaml
Wait-ForDeployment "prometheus"
Wait-ForDeployment "grafana"
Write-Host "✓ Monitoring stack deployed" -ForegroundColor Green
Write-Host ""

# Display summary
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "Deployment Summary" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""

Write-Host "Deployments:" -ForegroundColor Green
kubectl get deployments -n $Namespace

Write-Host ""
Write-Host "Services:" -ForegroundColor Green
kubectl get services -n $Namespace

Write-Host ""
Write-Host "Pods:" -ForegroundColor Green
kubectl get pods -n $Namespace

Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "Access Information" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "To access services, use port forwarding:"
Write-Host ""
Write-Host "API Gateway:"
Write-Host "  kubectl port-forward svc/api-gateway -n $Namespace 8090:8090"
Write-Host "  Access: http://localhost:8090"
Write-Host ""
Write-Host "Eureka Server:"
Write-Host "  kubectl port-forward svc/eureka-server -n $Namespace 8761:8761"
Write-Host "  Access: http://localhost:8761"
Write-Host ""
Write-Host "Frontend:"
Write-Host "  kubectl port-forward svc/frontend-service -n $Namespace 4200:80"
Write-Host "  Access: http://localhost:4200"
Write-Host ""
Write-Host "Prometheus:"
Write-Host "  kubectl port-forward svc/prometheus -n $Namespace 9090:9090"
Write-Host "  Access: http://localhost:9090"
Write-Host ""
Write-Host "Grafana:"
Write-Host "  kubectl port-forward svc/grafana -n $Namespace 3000:3000"
Write-Host "  Access: http://localhost:3000 (admin/admin123)"
Write-Host ""

Write-Host "✓ Deployment completed successfully!" -ForegroundColor Green
