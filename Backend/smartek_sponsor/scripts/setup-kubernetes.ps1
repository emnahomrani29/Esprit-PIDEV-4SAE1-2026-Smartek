# Setup Kubernetes on Docker Desktop for Windows
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "KUBERNETES SETUP FOR DOCKER DESKTOP" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker Desktop is running
$dockerRunning = docker info 2>$null
if (-not $dockerRunning) {
    Write-Host "[ERROR] Docker Desktop is not running" -ForegroundColor Red
    Write-Host "Please start Docker Desktop first" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Docker Desktop is running" -ForegroundColor Green
Write-Host ""

# Check if kubectl is available
$kubectlInstalled = Get-Command kubectl -ErrorAction SilentlyContinue

if (-not $kubectlInstalled) {
    Write-Host "Installing kubectl..." -ForegroundColor Yellow
    
    # Download kubectl
    $kubectlUrl = "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
    $kubectlPath = "$env:USERPROFILE\kubectl.exe"
    
    Invoke-WebRequest -Uri $kubectlUrl -OutFile $kubectlPath
    
    # Add to PATH for current session
    $env:Path += ";$env:USERPROFILE"
    
    Write-Host "[OK] kubectl installed to $kubectlPath" -ForegroundColor Green
    Write-Host "Add $env:USERPROFILE to your PATH permanently" -ForegroundColor Yellow
} else {
    Write-Host "[OK] kubectl is already installed" -ForegroundColor Green
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "ENABLE KUBERNETES IN DOCKER DESKTOP" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "MANUAL STEPS:" -ForegroundColor Yellow
Write-Host "1. Open Docker Desktop" -ForegroundColor White
Write-Host "2. Click Settings (gear icon)" -ForegroundColor White
Write-Host "3. Go to: Kubernetes" -ForegroundColor White
Write-Host "4. Check: Enable Kubernetes" -ForegroundColor White
Write-Host "5. Click: Apply & Restart" -ForegroundColor White
Write-Host "6. Wait 2-3 minutes for Kubernetes to start" -ForegroundColor White
Write-Host ""

# Check if Kubernetes is enabled
Write-Host "Checking Kubernetes status..." -ForegroundColor Yellow
$k8sRunning = kubectl cluster-info 2>$null

if ($k8sRunning) {
    Write-Host "[OK] Kubernetes is running!" -ForegroundColor Green
    Write-Host ""
    kubectl cluster-info
    Write-Host ""
    kubectl get nodes
    Write-Host ""
    
    # Create namespace
    Write-Host "Creating smartek-production namespace..." -ForegroundColor Yellow
    kubectl create namespace smartek-production --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "[OK] Namespace created" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "KUBERNETES IS READY!" -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "You can now deploy with:" -ForegroundColor White
    Write-Host "  kubectl apply -f k8s/" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or run Jenkins pipeline - Stage 11 will work!" -ForegroundColor Green
    
} else {
    Write-Host "[WARNING] Kubernetes is not running yet" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "After enabling Kubernetes in Docker Desktop:" -ForegroundColor White
    Write-Host "  Run this script again to verify" -ForegroundColor Cyan
    Write-Host "  Or run: kubectl cluster-info" -ForegroundColor Cyan
}

Write-Host ""
