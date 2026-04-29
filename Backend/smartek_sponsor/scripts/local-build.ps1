# Local build script for Windows PowerShell
# Smartek Sponsor Service

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Smartek Sponsor - Local Build" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Check if Maven is installed
if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Maven is not installed" -ForegroundColor Red
    exit 1
}

# Check if Docker is installed
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Docker is not installed" -ForegroundColor Red
    exit 1
}

# Navigate to project directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $scriptPath "..")

Write-Host "`nStep 1: Cleaning previous builds..." -ForegroundColor Yellow
mvn clean

Write-Host "`nStep 2: Compiling source code..." -ForegroundColor Yellow
mvn compile

Write-Host "`nStep 3: Running unit tests..." -ForegroundColor Yellow
mvn test

Write-Host "`nStep 4: Generating code coverage report..." -ForegroundColor Yellow
mvn jacoco:report

Write-Host "`nStep 5: Packaging application..." -ForegroundColor Yellow
mvn package -DskipTests

Write-Host "`nStep 6: Building Docker image..." -ForegroundColor Yellow
docker build -t smartek-sponsor:local .

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nArtifacts:" -ForegroundColor Cyan
Write-Host "JAR: target\smartek-sponsor-0.0.1-SNAPSHOT.jar"
Write-Host "Docker Image: smartek-sponsor:local"

Write-Host "`nCoverage Report:" -ForegroundColor Cyan
Write-Host "target\site\jacoco\index.html"

Write-Host "`nTo run the application locally:" -ForegroundColor Yellow
Write-Host "docker run -p 8080:8080 smartek-sponsor:local"

Write-Host "`nTo run with docker-compose:" -ForegroundColor Yellow
Write-Host "docker-compose up -d"
