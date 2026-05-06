#!/usr/bin/env pwsh
# ============================================================================
# Script: Build-And-Push-All.ps1
# Description: Build et push de toutes les images Docker du projet Smartek
# ============================================================================

param(
    [string]$Registry = "localhost",
    [switch]$SkipBuild,
    [switch]$SkipPush
)

$ErrorActionPreference = "Stop"

# Couleurs pour les messages
function Write-Success { Write-Host "✓ $args" -ForegroundColor Green }
function Write-Info { Write-Host "ℹ $args" -ForegroundColor Cyan }
function Write-Warning { Write-Host "⚠ $args" -ForegroundColor Yellow }
function Write-Error { Write-Host "✗ $args" -ForegroundColor Red }

# ============================================================================
# CONFIGURATION
# ============================================================================

$PROJECT_NAME = "smartek"
$VERSION = "latest"

# Services Backend avec Dockerfile
$BACKEND_SERVICES = @(
    @{Name="eureka-server"; Path="Backend/eureka-server"},
    @{Name="config-server"; Path="Backend/config-server"},
    @{Name="api-gateway"; Path="Backend/api-gateway"},
    @{Name="auth-service"; Path="Backend/auth-service"},
    @{Name="event-service"; Path="Backend/event-service"},
    @{Name="certification-badge-service"; Path="Backend/certification-badge-service"},
    @{Name="planning-service"; Path="Backend/planning-service"},
    @{Name="training-service"; Path="Backend/training-service"},
    @{Name="offers-service"; Path="Backend/offers-service"},
    @{Name="course-service"; Path="Backend/course-service"},
    @{Name="exam-service"; Path="Backend/exam-service"},
    @{Name="skill-evidence-service"; Path="Backend/skiil-evidence-service"},
    @{Name="learning"; Path="Backend/learning"},
    @{Name="sponsor-service"; Path="Backend/smartek_sponsor"}
)

# Frontend
$FRONTEND_SERVICE = @{Name="frontend"; Path="Frontend/angular-app"}

# Images officielles (pull uniquement, pas de build)
$OFFICIAL_IMAGES = @(
    @{Name="mysql"; Image="mysql:8.0"},
    @{Name="prometheus"; Image="prom/prometheus:latest"},
    @{Name="grafana"; Image="grafana/grafana:10.4.0"},
    @{Name="loki"; Image="grafana/loki:latest"},
    @{Name="promtail"; Image="grafana/promtail:latest"},
    @{Name="jaeger"; Image="jaegertracing/all-in-one:latest"},
    @{Name="sonarqube"; Image="sonarqube:community"},
    @{Name="nexus"; Image="sonatype/nexus3:latest"},
    @{Name="jenkins"; Image="jenkins/jenkins:lts"}
)

# ============================================================================
# STATISTIQUES
# ============================================================================

$stats = @{
    Total = 0
    Success = 0
    Failed = 0
    Skipped = 0
    StartTime = Get-Date
}

# ============================================================================
# FONCTIONS
# ============================================================================

function Build-DockerImage {
    param(
        [string]$Name,
        [string]$Path,
        [string]$Tag
    )
    
    Write-Info "Building $Name..."
    
    if (-not (Test-Path $Path)) {
        Write-Error "Path not found: $Path"
        return $false
    }
    
    try {
        $buildArgs = @(
            "build",
            "-t", $Tag,
            "-f", "$Path/Dockerfile",
            $Path
        )
        
        $process = Start-Process -FilePath "docker" -ArgumentList $buildArgs -NoNewWindow -Wait -PassThru
        
        if ($process.ExitCode -eq 0) {
            Write-Success "Built $Name successfully"
            return $true
        } else {
            Write-Error "Failed to build $Name (Exit code: $($process.ExitCode))"
            return $false
        }
    }
    catch {
        Write-Error "Error building $Name : $_"
        return $false
    }
}

function Push-DockerImage {
    param([string]$Tag)
    
    Write-Info "Pushing $Tag..."
    
    try {
        $pushArgs = @("push", $Tag)
        $process = Start-Process -FilePath "docker" -ArgumentList $pushArgs -NoNewWindow -Wait -PassThru
        
        if ($process.ExitCode -eq 0) {
            Write-Success "Pushed $Tag successfully"
            return $true
        } else {
            Write-Error "Failed to push $Tag (Exit code: $($process.ExitCode))"
            return $false
        }
    }
    catch {
        Write-Error "Error pushing $Tag : $_"
        return $false
    }
}

function Pull-And-Tag-Image {
    param(
        [string]$SourceImage,
        [string]$TargetTag
    )
    
    Write-Info "Pulling $SourceImage..."
    
    try {
        # Pull l'image officielle
        $pullArgs = @("pull", $SourceImage)
        $process = Start-Process -FilePath "docker" -ArgumentList $pullArgs -NoNewWindow -Wait -PassThru
        
        if ($process.ExitCode -ne 0) {
            Write-Error "Failed to pull $SourceImage"
            return $false
        }
        
        # Tag l'image
        Write-Info "Tagging $SourceImage as $TargetTag..."
        $tagArgs = @("tag", $SourceImage, $TargetTag)
        $process = Start-Process -FilePath "docker" -ArgumentList $tagArgs -NoNewWindow -Wait -PassThru
        
        if ($process.ExitCode -eq 0) {
            Write-Success "Tagged $SourceImage as $TargetTag"
            return $true
        } else {
            Write-Error "Failed to tag $SourceImage"
            return $false
        }
    }
    catch {
        Write-Error "Error processing $SourceImage : $_"
        return $false
    }
}

function Show-Summary {
    $duration = (Get-Date) - $stats.StartTime
    
    Write-Host "`n" + ("=" * 70) -ForegroundColor Cyan
    Write-Host "SUMMARY" -ForegroundColor Cyan
    Write-Host ("=" * 70) -ForegroundColor Cyan
    
    Write-Host "Total images:    " -NoNewline
    Write-Host $stats.Total -ForegroundColor White
    
    Write-Host "Successful:      " -NoNewline
    Write-Host $stats.Success -ForegroundColor Green
    
    Write-Host "Failed:          " -NoNewline
    Write-Host $stats.Failed -ForegroundColor Red
    
    Write-Host "Skipped:         " -NoNewline
    Write-Host $stats.Skipped -ForegroundColor Yellow
    
    Write-Host "Duration:        " -NoNewline
    Write-Host ("{0:mm}m {0:ss}s" -f $duration) -ForegroundColor White
    
    Write-Host ("=" * 70) -ForegroundColor Cyan
}

# ============================================================================
# MAIN SCRIPT
# ============================================================================

Write-Host "`n" + ("=" * 70) -ForegroundColor Cyan
Write-Host "SMARTEK DOCKER BUILD & PUSH" -ForegroundColor Cyan
Write-Host ("=" * 70) -ForegroundColor Cyan
Write-Host "Registry: $Registry" -ForegroundColor White
Write-Host "Version:  $VERSION" -ForegroundColor White
Write-Host ("=" * 70) -ForegroundColor Cyan

# Vérifier que Docker est disponible
try {
    $null = docker --version
    Write-Success "Docker is available"
}
catch {
    Write-Error "Docker is not available. Please install Docker Desktop."
    exit 1
}

# ============================================================================
# TRAITEMENT DES SERVICES BACKEND
# ============================================================================

Write-Host "`n[1/3] BACKEND SERVICES ($($BACKEND_SERVICES.Count) services)" -ForegroundColor Yellow

foreach ($service in $BACKEND_SERVICES) {
    $stats.Total++
    $tag = "$Registry/${PROJECT_NAME}-$($service.Name):$VERSION"
    
    Write-Host "`n--- $($service.Name) ---" -ForegroundColor Magenta
    
    $success = $true
    
    if (-not $SkipBuild) {
        $success = Build-DockerImage -Name $service.Name -Path $service.Path -Tag $tag
    } else {
        Write-Warning "Build skipped"
        $stats.Skipped++
    }
    
    if ($success -and -not $SkipPush) {
        $success = Push-DockerImage -Tag $tag
    } elseif ($SkipPush) {
        Write-Warning "Push skipped"
    }
    
    if ($success) {
        $stats.Success++
    } else {
        $stats.Failed++
    }
}

# ============================================================================
# TRAITEMENT DU FRONTEND
# ============================================================================

Write-Host "`n[2/3] FRONTEND SERVICE" -ForegroundColor Yellow

$stats.Total++
$tag = "$Registry/${PROJECT_NAME}-$($FRONTEND_SERVICE.Name):$VERSION"

Write-Host "`n--- $($FRONTEND_SERVICE.Name) ---" -ForegroundColor Magenta

$success = $true

if (-not $SkipBuild) {
    $success = Build-DockerImage -Name $FRONTEND_SERVICE.Name -Path $FRONTEND_SERVICE.Path -Tag $tag
} else {
    Write-Warning "Build skipped"
    $stats.Skipped++
}

if ($success -and -not $SkipPush) {
    $success = Push-DockerImage -Tag $tag
} elseif ($SkipPush) {
    Write-Warning "Push skipped"
}

if ($success) {
    $stats.Success++
} else {
    $stats.Failed++
}

# ============================================================================
# TRAITEMENT DES IMAGES OFFICIELLES
# ============================================================================

Write-Host "`n[3/3] INFRASTRUCTURE & MONITORING ($($OFFICIAL_IMAGES.Count) images)" -ForegroundColor Yellow

foreach ($image in $OFFICIAL_IMAGES) {
    $stats.Total++
    $tag = "$Registry/${PROJECT_NAME}-$($image.Name):$VERSION"
    
    Write-Host "`n--- $($image.Name) ---" -ForegroundColor Magenta
    
    $success = $true
    
    if (-not $SkipBuild) {
        $success = Pull-And-Tag-Image -SourceImage $image.Image -TargetTag $tag
    } else {
        Write-Warning "Pull skipped"
        $stats.Skipped++
    }
    
    if ($success -and -not $SkipPush) {
        $success = Push-DockerImage -Tag $tag
    } elseif ($SkipPush) {
        Write-Warning "Push skipped"
    }
    
    if ($success) {
        $stats.Success++
    } else {
        $stats.Failed++
    }
}

# ============================================================================
# AFFICHAGE DU RÉSUMÉ
# ============================================================================

Show-Summary

if ($stats.Failed -gt 0) {
    Write-Host "`n⚠ Some images failed to process. Check the logs above." -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "`n✓ All images processed successfully!" -ForegroundColor Green
    exit 0
}
