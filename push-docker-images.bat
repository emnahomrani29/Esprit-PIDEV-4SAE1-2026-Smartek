@echo off
REM ============================================================================
REM Script Windows pour pousser les images Docker vers Docker Hub
REM Usage: push-docker-images.bat [REGISTRY] [TAG]
REM ============================================================================

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║  Push des images Docker - Projet Smartek                  ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Vérifier si Git Bash est installé
where bash >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Git Bash n'est pas installé ou pas dans le PATH
    echo.
    echo Veuillez installer Git Bash depuis: https://git-scm.com/downloads
    echo.
    pause
    exit /b 1
)

REM Vérifier si Docker est en cours d'exécution
docker info >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Docker n'est pas en cours d'exécution
    echo.
    echo Veuillez démarrer Docker Desktop
    echo.
    pause
    exit /b 1
)

echo [OK] Docker est en cours d'exécution
echo.

REM Paramètres
set REGISTRY=%1
set TAG=%2

REM Valeurs par défaut
if "%REGISTRY%"=="" set REGISTRY=smartek
if "%TAG%"=="" set TAG=latest

echo Configuration:
echo   Registry: %REGISTRY%
echo   Tag:      %TAG%
echo.

REM Demander confirmation
set /p CONFIRM="Voulez-vous continuer? (O/n): "
if /i "%CONFIRM%"=="n" (
    echo Operation annulee
    exit /b 0
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo   ETAPE 1/3 : Construction des images
echo ═══════════════════════════════════════════════════════════════
echo.

REM Construire les images
bash scripts/build-all-images.sh --registry %REGISTRY% --tag %TAG%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] Echec de la construction des images
    pause
    exit /b 1
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo   ETAPE 2/3 : Connexion a Docker Hub
echo ═══════════════════════════════════════════════════════════════
echo.

REM Vérifier si déjà connecté
docker info 2>nul | findstr /C:"Username" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Deja connecte a Docker Hub
) else (
    echo Veuillez vous connecter a Docker Hub:
    docker login
    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo [ERREUR] Echec de la connexion
        pause
        exit /b 1
    )
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo   ETAPE 3/3 : Push des images vers Docker Hub
echo ═══════════════════════════════════════════════════════════════
echo.

REM Pousser les images
bash scripts/push-all-images.sh --registry %REGISTRY% --tag %TAG%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] Echec du push des images
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║  ✓ TOUTES LES IMAGES ONT ETE POUSSEES AVEC SUCCES !       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo Images disponibles sur: https://hub.docker.com/r/%REGISTRY%
echo.

pause
exit /b 0
