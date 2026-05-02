# SMARTEK Docker Image Build and Push Script for Windows

param(
    [string]$Registry = "your-docker-registry",
    [string]$Tag = "latest",
    [switch]$SkipLogin = $false,
    [switch]$Help = $false
)

if ($Help) {
    Write-Host "Usage: .\build-and-push.ps1 -Registry <registry> -Tag <tag> [-SkipLogin]"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\build-and-push.ps1 -Registry myusername -Tag latest"
    Write-Host "  .\build-and-push.ps1 -Registry registry.example.com:5000 -Tag v1.0"
    Write-Host "  .\build-and-push.ps1 -Registry myuser -Tag latest -SkipLogin"
    exit 0
}

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Yellow
Write-Host "SMARTEK Docker Image Build & Push Script" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Registry: $Registry"
Write-Host "Tag: $Tag"
Write-Host ""

# Login to registry if not skipped
if (-not $SkipLogin) {
    Write-Host "Logging in to Docker registry..." -ForegroundColor Yellow
    if ($Registry -like "*docker.io*" -or $Registry -notlike "*.*") {
        docker login docker.io
    } else {
        docker login $Registry
    }
    Write-Host ""
}

# Check if we're in the right directory
if (-not (Test-Path "Backend") -or -not (Test-Path "Frontend")) {
    Write-Host "Error: Must run from project root directory" -ForegroundColor Red
    exit 1
}

# Array of services to build
$Services = @(
    @("Backend/eureka-server", "eureka-server"),
    @("Backend/auth-service", "auth-service"),
    @("Backend/event-service", "event-service"),
    @("Backend/planning-service", "planning-service"),
    @("Backend/training-service", "training-service"),
    @("Backend/offers-service", "offers-service"),
    @("Backend/exam-service", "exam-service"),
    @("Backend/course-service", "course-service"),
    @("Backend/learning", "learning-service"),
    @("Backend/skiil-evidence-service", "skill-evidence-service"),
    @("Backend/config-server", "config-server"),
    @("Backend/api-gateway", "api-gateway"),
    @("Frontend/angular-app", "frontend")
)

# Counter
$Current = 1
$Total = $Services.Count

# Build and push each service
foreach ($ServicePair in $Services) {
    $Context = $ServicePair[0]
    $ImageName = $ServicePair[1]
    
    $FullImage = "${Registry}/smartek/${ImageName}:${Tag}"
    
    Write-Host "[$Current/$Total] Building and pushing ${ImageName}..." -ForegroundColor Green
    Write-Host "  Path: $Context"
    Write-Host "  Image: $FullImage"
    
    # Build image
    try {
        docker build -t $FullImage $Context
        Write-Host "  ✓ Build successful" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Build failed" -ForegroundColor Red
        throw $_
    }
    
    # Push image
    try {
        docker push $FullImage
        Write-Host "  ✓ Push successful" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Push failed" -ForegroundColor Red
        throw $_
    }
    
    Write-Host ""
    $Current++
}

Write-Host "================================================" -ForegroundColor Yellow
Write-Host "✓ All images built and pushed successfully!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Update Kubernetes YAML files with your registry:"
Write-Host "   Get-ChildItem k8s -Recurse -Filter '*.yaml' | ForEach-Object { (Get-Content `$_.FullName) -replace 'your-docker-registry', '$Registry' | Set-Content `$_.FullName }"
Write-Host ""
Write-Host "2. Deploy to Kubernetes:"
Write-Host "   cd k8s"
Write-Host "   .\deploy.ps1 -Registry $Registry"
Write-Host ""
