# ============================================================
#  SMARTEK - Script de demarrage local de tous les services
# ============================================================
# Prerequis : Java 17+, Maven, Node.js/npm, Angular CLI
# Usage     : .\start-all.ps1
# ============================================================

$ROOT = $PSScriptRoot

function Write-Info { param($msg) Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Write-Ok   { param($msg) Write-Host "[OK]    $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "[WARN]  $msg" -ForegroundColor Yellow }

function Start-SpringService {
    param(
        [string]$Name,
        [string]$Path,
        [string]$Color = "DarkCyan"
    )
    $fullPath = Join-Path $ROOT $Path
    if (-not (Test-Path $fullPath)) {
        Write-Warn "$Name : dossier introuvable ($fullPath) - ignore"
        return
    }
    Write-Info "Demarrage de $Name ..."
    $cmd = "Set-Location '$fullPath'; mvn spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd -WindowStyle Normal
    Start-Sleep -Seconds 2
}

function Start-Angular {
    param([string]$Path)
    $fullPath = Join-Path $ROOT $Path
    if (-not (Test-Path $fullPath)) {
        Write-Warn "Frontend : dossier introuvable ($fullPath) - ignore"
        return
    }
    Write-Info "Demarrage du Frontend Angular ..."
    $cmd = "Set-Location '$fullPath'; npx ng serve --open"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd -WindowStyle Normal
}

# ─── Demarrage ───────────────────────────────────────────────

Write-Host ""
Write-Host "====================================================" -ForegroundColor White
Write-Host "       SMARTEK - Demarrage local complet            " -ForegroundColor White
Write-Host "====================================================" -ForegroundColor White
Write-Host ""

# 0. Demarrer MySQL XAMPP si pas encore lance
Write-Host "-- Verification MySQL (XAMPP) --" -ForegroundColor DarkGray
$mysqlProc = Get-NetTCPConnection -LocalPort 3306 -ErrorAction SilentlyContinue
if ($mysqlProc) {
    Write-Ok "MySQL deja en cours d'execution sur le port 3306."
} else {
    Write-Info "MySQL non detecte - tentative de demarrage via XAMPP..."
    $xamppPaths = @(
        "C:\xampp\mysql\bin\mysqld.exe",
        "C:\xampp\mysql_start.bat",
        "C:\xampp\xampp-control.exe"
    )
    $started = $false
    foreach ($p in $xamppPaths) {
        if (Test-Path $p) {
            if ($p -like "*.bat") {
                Start-Process "cmd.exe" -ArgumentList "/c `"$p`"" -WindowStyle Minimized
            } else {
                Start-Process $p -WindowStyle Minimized
            }
            $started = $true
            Write-Ok "MySQL XAMPP demarre depuis : $p"
            break
        }
    }
    if (-not $started) {
        Write-Warn "MySQL introuvable. Demarre MySQL manuellement avant de continuer."
        Read-Host "Appuie sur Entree une fois MySQL demarre"
    }
    Write-Info "Attente 5s pour MySQL..."
    Start-Sleep -Seconds 5
}
Start-Sleep -Seconds 2

# 1. Liberer le port 8080 (Oracle TNS Listener)
Write-Host "-- Liberation du port 8080 --" -ForegroundColor DarkGray
$proc8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($proc8080) {
    foreach ($procId in $proc8080) {
        $procName = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
        Write-Warn "Port 8080 occupe par : $procName (PID $procId) - arret en cours..."
        taskkill /PID $procId /F 2>&1 | Out-Null
        Write-Ok "Processus $procName (PID $procId) arrete."
    }
} else {
    Write-Ok "Port 8080 libre."
}
Start-Sleep -Seconds 2

# 1. Eureka Server
Write-Host ""
Write-Host "-- Infrastructure --" -ForegroundColor DarkGray

Start-SpringService -Name "Eureka Server    (8761)" -Path "Backend/eureka-server" -Color "Yellow"
Write-Info "Attente 15s pour Eureka..."
Start-Sleep -Seconds 15

# 2. Config Server
Start-SpringService -Name "Config Server    (8888)" -Path "Backend/config-server" -Color "DarkYellow"
Write-Info "Attente 10s pour Config Server..."
Start-Sleep -Seconds 10

# 3. API Gateway (juste apres Config Server)
Start-SpringService -Name "API Gateway      (8080)" -Path "Backend/api-gateway"   -Color "White"
Write-Info "Attente 10s pour l'API Gateway..."
Start-Sleep -Seconds 10

# 4. Auth Service
Write-Host ""
Write-Host "-- Auth Service --" -ForegroundColor DarkGray

Start-SpringService -Name "Auth Service     (8081)" -Path "Backend/auth-service"  -Color "Green"
Write-Info "Attente 15s pour Auth Service..."
Start-Sleep -Seconds 15

# 5. Microservices metier
Write-Host ""
Write-Host "-- Microservices metier --" -ForegroundColor DarkGray

Start-SpringService -Name "Event Service    (8082)" -Path "Backend/event-service"               -Color "Cyan"
Start-SpringService -Name "Planning Service (8083)" -Path "Backend/planning-service"            -Color "Cyan"
Start-SpringService -Name "Training Service (8084)" -Path "Backend/training-service"            -Color "Cyan"
Start-SpringService -Name "Offers Service   (8085)" -Path "Backend/offers-service"              -Color "Cyan"
Start-SpringService -Name "Course Service   (8086)" -Path "Backend/course-service"              -Color "Cyan"
Start-SpringService -Name "Exam Service     (8087)" -Path "Backend/exam-service"                -Color "Cyan"
Start-SpringService -Name "Skill Evidence   (8091)" -Path "Backend/skiil-evidence-service"      -Color "Cyan"
Start-SpringService -Name "Learning Service (8092)" -Path "Backend/learning"                    -Color "Cyan"
Start-SpringService -Name "Sponsor Service  (8093)" -Path "Backend/smartek_sponsor"             -Color "Cyan"
Start-SpringService -Name "Certif/Badge     (8094)" -Path "Backend/certification-badge-service" -Color "Cyan"

Write-Info "Attente 20s pour les services metier..."
Start-Sleep -Seconds 20

# 6. Frontend Angular
Write-Host ""
Write-Host "-- Frontend Angular --" -ForegroundColor DarkGray

Start-Angular -Path "Frontend/angular-app"

# Resume
Write-Host ""
Write-Host "====================================================" -ForegroundColor Green
Write-Host "  Tous les services ont ete lances !" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green
Write-Host "  Eureka Dashboard  -> http://localhost:8761" -ForegroundColor Green
Write-Host "  API Gateway       -> http://localhost:8080" -ForegroundColor Green
Write-Host "  Frontend Angular  -> http://localhost:4200" -ForegroundColor Green
Write-Host "----------------------------------------------------" -ForegroundColor Green
Write-Host "  Auth       8081  |  Event    8082  |  Planning  8083" -ForegroundColor Green
Write-Host "  Training   8084  |  Offers   8085  |  Course    8086" -ForegroundColor Green
Write-Host "  Exam       8087  |  Skill    8091  |  Learning  8092" -ForegroundColor Green
Write-Host "  Sponsor    8093  |  Certif   8094  |  Gateway   8080" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green
Write-Host ""
Write-Ok "Pour arreter : fermez les fenetres PowerShell ouvertes."
