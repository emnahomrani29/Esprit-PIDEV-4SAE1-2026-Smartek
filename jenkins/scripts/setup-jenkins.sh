#!/bin/bash

# 🚀 Script d'installation et configuration Jenkins pour Smartek ESPRIT
# Ce script automatise l'installation complète de Jenkins avec tous les plugins nécessaires

set -e

echo "🚀 Installation Jenkins - Smartek ESPRIT"
echo "========================================"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérifier les prérequis
log_info "Vérification des prérequis..."

if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

log_info "✅ Docker et Docker Compose sont installés"

# Vérifier si les ports sont disponibles
log_info "Vérification des ports..."

if lsof -Pi :8090 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_error "Le port 8090 est déjà utilisé. Veuillez libérer ce port."
    exit 1
fi

if lsof -Pi :9000 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warn "Le port 9000 est déjà utilisé. SonarQube ne sera pas démarré."
    SKIP_SONAR=true
fi

log_info "✅ Ports disponibles"

# Créer le fichier .env s'il n'existe pas
if [ ! -f jenkins/.env ]; then
    log_info "Création du fichier .env..."
    cp jenkins/.env.example jenkins/.env
    log_warn "⚠️  Veuillez éditer jenkins/.env avec vos credentials"
    log_warn "   Appuyez sur Entrée pour continuer après avoir édité le fichier..."
    read
fi

# Démarrer Jenkins
log_info "Démarrage de Jenkins..."
cd jenkins

if [ "$SKIP_SONAR" = true ]; then
    docker-compose up -d jenkins
else
    docker-compose up -d
fi

log_info "⏳ Attente du démarrage de Jenkins (cela peut prendre 1-2 minutes)..."

# Attendre que Jenkins soit prêt
JENKINS_URL="http://localhost:8090"
MAX_ATTEMPTS=60
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s -o /dev/null -w "%{http_code}" "$JENKINS_URL/login" | grep -q "200"; then
        log_info "✅ Jenkins est prêt!"
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    sleep 2
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    log_error "Jenkins n'a pas démarré dans le temps imparti"
    exit 1
fi

echo ""
log_info "========================================"
log_info "✅ Installation terminée avec succès!"
log_info "========================================"
echo ""
log_info "📋 Informations de connexion:"
log_info "   URL: $JENKINS_URL"
log_info "   Username: admin"
log_info "   Password: admin123"
echo ""
log_warn "⚠️  IMPORTANT: Changez le mot de passe après la première connexion!"
echo ""
log_info "📝 Prochaines étapes:"
log_info "   1. Ouvrir $JENKINS_URL dans votre navigateur"
log_info "   2. Se connecter avec les credentials ci-dessus"
log_info "   3. Configurer les credentials (GitHub, SonarCloud)"
log_info "   4. Configurer les webhooks GitHub"
log_info "   5. Lancer votre premier build!"
echo ""
log_info "📖 Documentation complète: jenkins/README.md"
echo ""

# Afficher les logs
log_info "📊 Logs Jenkins (Ctrl+C pour quitter):"
docker logs -f smartek-jenkins
