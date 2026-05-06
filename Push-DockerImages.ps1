# ============================================================================
# Script PowerShell pour pousser les images Docker vers Docker Hub
# Usage: .\Push-DockerImages.ps1 -Registry "VOTRE_USERNAME" -Tag "v1.0.0"
# ============================================================================

param(
    [string]$Registry = "smartek",
    [string]$Tag = "latest",
    [switch]$SkipBuild = $false,
    [switch]$PullExternal = $false,
    [switch]$Help = $false
)

# Afficher l'aide
if ($Help) {
    Write-Host @"

Usage: .\Push-DockerImages.ps1 [OPTIONS]

Options:
  -Registry STRING      Registry Docker (défaut: smartek)
  -Tag STRING          Tag de version (défaut: latest)
  -SkipBuild           Sauter la construction (push uniquement)
  -PullExternal        Télécharger les images externes
  -Help                Afficher cette aide

Exemples:
  .\Push-DockerImages.ps1 -Registry "johndoe" -Tag "v1.0.0"
  .\Push-DockerImages.ps1 -Registry "mycompany" -Tag "latest" -PullExternal
  .\Push-DockerImages.ps1 -Registry "johndoe" -SkipBuild

"@
    exit 0
}

# Couleurs
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Header {
    param([string]$Title)
    Write-Host ""
    Write-ColorOutput "╔════════════════════════════════════════════════════════════╗" "Blue"
    Write-ColorOutput "║  $Title" "Blue"
    Write-ColorOutput "╚════════════════════════════════════════════════════════════╝" "Blue"
    Write-Host ""
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-ColorOutput "═══════════════════════════════════════════════════════════════" "Cyan"
    Write-ColorOutput "  $Title" "Cyan"
    Write-ColorOutput "═══════════════════════════════════════════════════════════════" "Cyan"
    Write-Host ""
}

# En-tête
Write-Header "Push des images Docker - Projet Smartek"

# ═══════════════════════════════════════════════════════════════════════════
# Vérifications
# ═══════════════════════════════════════════════════════════════════════════

Write-Section "Vérifications préalables"

# Vérifier Docker
try {
    $dockerVersion = docker --version
    Write-ColorOutput "✓ Docker installé: $dockerVersion" "Green"
} catch {
    Write-ColorOutput "✗ Docker n'est pas installé" "Red"
    Write-ColorOutput "Veuillez installer Docker Desktop: https://www.docker.com/products/docker-desktop" "Yellow"
    exit 1
}

# Vérifier que Docker est en cours d'exécution
try {
    docker info | Out-Null
    Write-ColorOutput "✓ Docker est en cours d'exécution" "Green"
} catch {
    Write-ColorOutput "✗ Docker n'est pas en cours d'exécution" "Red"
    Write-ColorOutput "Veuillez démarrer Docker Desktop" "Yellow"
    exit 1
}

# Afficher la configuration
Write-Host ""
Write-ColorOutput "Configuration:" "Yellow"
Write-Host "  Registry:     $Registry"
Write-Host "  Tag:          $Tag"
Write-Host "  Build:        $(if ($SkipBuild) { 'Non' } else { 'Oui' })"
Write-Host "  External:     $(if ($PullExternal) { 'Oui' } else { 'Non' })"
Write-Host ""

$confirmation = Read-Host "Continuer avec cette configuration? (O/n)"
if ($confirmation -eq 'n' -or $confirmation -eq 'N') {
    Write-ColorOutput "Opération annulée" "Yellow"
    exit 0
}

# ═══════════════════════════════════════════════════════════════════════════
# Construction des images
# ═══════════════════════════════════════════════════════════════════════════

if (-not $SkipBuild) {
    Write-Section "ÉTAPE 1/3 : Construction des images"
    
    $buildArgs = "--registry $Registry --tag $Tag"
    if ($PullExternal) {
        $buildArgs += " --pull-external"
    }
    
    Write-ColorOutput "Commande: bash scripts/build-all-images.sh $buildArgs" "Cyan"
    Write-Host ""
    
    bash scripts/build-all-images.sh $buildArgs.Split()
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "✗ Échec de la construction" "Red"
        exit 1
    }
    
    Write-ColorOutput "✓ Construction réussie" "Green"
} else {
    Write-ColorOutput "⚠ Construction ignorée (--SkipBuild)" "Yellow"
}

# ═══════════════════════════════════════════════════════════════════════════
# Connexion à Docker Hub
# ═══════════════════════════════════════════════════════════════════════════

Write-Section "ÉTAPE 2/3 : Connexion à Docker Hub"

# Vérifier si déjà connecté
$dockerInfo = docker info 2>&1 | Out-String
if ($dockerInfo -match "Username") {
    $username = ($dockerInfo -split "`n" | Where-Object { $_ -match "Username" }) -replace ".*Username:\s*", ""
    Write-ColorOutput "✓ Déjà connecté en tant que: $username" "Green"
    
    $reconnect = Read-Host "Voulez-vous vous reconnecter? (o/N)"
    if ($reconnect -eq 'o' -or $reconnect -eq 'O') {
        docker login
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "✗ Échec de la connexion" "Red"
            exit 1
        }
    }
} else {
    Write-ColorOutput "Connexion à Docker Hub requise" "Yellow"
    Write-Host ""
    docker login
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "✗ Échec de la connexion" "Red"
        exit 1
    }
    
    Write-ColorOutput "✓ Connexion réussie" "Green"
}

# ═══════════════════════════════════════════════════════════════════════════
# Push des images
# ═══════════════════════════════════════════════════════════════════════════

Write-Section "ÉTAPE 3/3 : Push des images vers Docker Hub"

$services = @(
    "api-gateway",
    "auth-service",
    "certification-badge-service",
    "config-server",
    "course-service",
    "eureka-server",
    "event-service",
    "exam-service",
    "learning-service",
    "offers-service",
    "planning-service",
    "skill-evidence-service",
    "sponsor-service",
    "training-service",
    "angular-app"
)

$totalImages = $services.Count
$successCount = 0
$failedCount = 0
$failedImages = @()

$counter = 0
foreach ($service in $services) {
    $counter++
    $imageName = "${Registry}/${service}:${Tag}"
    $imageLatest = "${Registry}/${service}:latest"
    
    Write-Host ""
    Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
    Write-ColorOutput "[$counter/$totalImages] Push: $service" "Yellow"
    Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
    
    # Vérifier que l'image existe localement
    $imageExists = docker image inspect $imageName 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "✗ Image non trouvée localement: $imageName" "Red"
        Write-ColorOutput "  Construisez d'abord l'image avec: .\Push-DockerImages.ps1 -Registry $Registry" "Yellow"
        $failedCount++
        $failedImages += "$service (image non trouvée)"
        continue
    }
    
    Write-Host "Image: $imageName"
    
    # Push de l'image avec le tag de version
    docker push $imageName
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✓ Push réussi: $imageName" "Green"
        
        # Push de l'image avec le tag latest
        $latestExists = docker image inspect $imageLatest 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            docker push $imageLatest | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "✓ Push réussi: $imageLatest" "Green"
            }
        }
        
        $successCount++
    } else {
        Write-ColorOutput "✗ Échec du push: $service" "Red"
        $failedCount++
        $failedImages += "$service (push failed)"
    }
}

# ═══════════════════════════════════════════════════════════════════════════
# Résumé
# ═══════════════════════════════════════════════════════════════════════════

Write-Host ""
Write-Header "RÉSUMÉ DU PUSH"

Write-ColorOutput "Registry:        $Registry" "Cyan"
Write-ColorOutput "Tag:             $Tag" "Cyan"
Write-ColorOutput "Total d'images:  $totalImages" "Yellow"
Write-ColorOutput "✓ Réussies:      $successCount" "Green"
Write-ColorOutput "✗ Échouées:      $failedCount" "Red"
Write-Host ""

if ($failedCount -gt 0) {
    Write-ColorOutput "Images échouées:" "Red"
    foreach ($image in $failedImages) {
        Write-ColorOutput "  ✗ $image" "Red"
    }
    Write-Host ""
    exit 1
} else {
    Write-ColorOutput "╔════════════════════════════════════════════════════════════╗" "Green"
    Write-ColorOutput "║  ✓ TOUTES LES IMAGES ONT ÉTÉ POUSSÉES AVEC SUCCÈS !       ║" "Green"
    Write-ColorOutput "╚════════════════════════════════════════════════════════════╝" "Green"
    Write-Host ""
    
    Write-ColorOutput "Images disponibles sur:" "Yellow"
    Write-ColorOutput "  https://hub.docker.com/r/$Registry" "Cyan"
    Write-Host ""
    
    Write-ColorOutput "Liste des images:" "Yellow"
    foreach ($service in $services) {
        Write-ColorOutput "  ✓ ${Registry}/${service}:${Tag}" "Green"
    }
    Write-Host ""
}

exit 0
