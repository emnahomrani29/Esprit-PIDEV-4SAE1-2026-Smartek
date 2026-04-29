# Setup Minikube for Kubernetes
Write-Host "🚀 Setting up Minikube for Kubernetes..." -ForegroundColor Green

# Check if Minikube is installed
$minikubeInstalled = Get-Command minikube -ErrorAction SilentlyContinue

if (-not $minikubeInstalled) {
    Write-Host "📦 Installing Minikube..." -ForegroundColor Yellow
    
    # Download Minikube
    $minikubeUrl = "https://github.com/kubernetes/minikube/releases/latest/download/minikube-windows-amd64.exe"
    $minikubePath = "$env:USERPROFILE\minikube.exe"
    
    Invoke-WebRequest -Uri $minikubeUrl -OutFile $minikubePath
    
    # Add to PATH
    $env:Path += ";$env:USERPROFILE"
    
    Write-Host "✅ Minikube installed" -ForegroundColor Green
} else {
    Write-Host "✅ Minikube already installed" -ForegroundColor Green
}

# Check if kubectl is installed
$kubectlInstalled = Get-Command kubectl -ErrorAction SilentlyContinue

if (-not $kubectlInstalled) {
    Write-Host "📦 Installing kubectl..." -ForegroundColor Yellow
    
    # Download kubectl
    $kubectlUrl = "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
    $kubectlPath = "$env:USERPROFILE\kubectl.exe"
    
    Invoke-WebRequest -Uri $kubectlUrl -OutFile $kubectlPath
    
    Write-Host "✅ kubectl installed" -ForegroundColor Green
} else {
    Write-Host "✅ kubectl already installed" -ForegroundColor Green
}

# Start Minikube
Write-Host "🚀 Starting Minikube cluster..." -ForegroundColor Yellow
minikube start --driver=docker --memory=4096 --cpus=2

# Enable addons
Write-Host "📦 Enabling Minikube addons..." -ForegroundColor Yellow
minikube addons enable ingress
minikube addons enable metrics-server

# Verify
Write-Host "✅ Verifying Minikube status..." -ForegroundColor Green
minikube status
kubectl cluster-info

Write-Host ""
Write-Host "✅ Minikube setup complete!" -ForegroundColor Green
Write-Host "   Dashboard: minikube dashboard" -ForegroundColor Cyan
Write-Host "   Status: minikube status" -ForegroundColor Cyan
Write-Host "   Stop: minikube stop" -ForegroundColor Cyan
