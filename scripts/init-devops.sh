#!/bin/bash
# scripts/init-devops.sh
# Initialisation complète de l'infrastructure DevOps

set -euo pipefail

# ─────────────────────────────────────────
# COULEURS POUR LES LOGS
# ─────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $*"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $*"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_step() { echo -e "${CYAN}[STEP]${NC} $*"; }

# ─────────────────────────────────────────
# VÉRIFICATION DES PRÉREQUIS
# ─────────────────────────────────────────
check_prerequisites() {
  log_step "Vérification des prérequis..."
  
  local missing=0
  
  if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé"
    ((missing++))
  else
    log_success "Docker: $(docker --version)"
  fi
  
  if ! command -v docker compose &> /dev/null; then
    log_error "Docker Compose n'est pas installé"
    ((missing++))
  else
    log_success "Docker Compose: $(docker compose version)"
  fi
  
  if ! command -v git &> /dev/null; then
    log_error "Git n'est pas installé"
    ((missing++))
  else
    log_success "Git: $(git --version)"
  fi
  
  if [ $missing -gt 0 ]; then
    log_error "$missing prérequis manquants"
    exit 1
  fi
  
  log_success "Tous les prérequis sont installés"
}

# ─────────────────────────────────────────
# CRÉATION DES RÉPERTOIRES
# ─────────────────────────────────────────
create_directories() {
  log_step "Création des répertoires nécessaires..."
  
  local dirs=(
    "backups"
    "logs"
    "devops/database/migrations"
    "devops/database/seeds"
    "devops/database/exports"
    "devops/nginx/ssl"
    "devops/nginx/conf.d"
  )
  
  for dir in "${dirs[@]}"; do
    if [ ! -d "$dir" ]; then
      mkdir -p "$dir"
      log_info "Créé: $dir"
    fi
  done
  
  log_success "Répertoires créés"
}

# ─────────────────────────────────────────
# VÉRIFICATION DES FICHIERS .env
# ─────────────────────────────────────────
check_env_files() {
  log_step "Vérification des fichiers d'environnement..."
  
  if [ ! -f "Backend/.env" ]; then
    if [ -f "Backend/.env.example" ]; then
      log_warn "Fichier Backend/.env manquant"
      read -p "Voulez-vous copier .env.example vers .env? (y/N) " -n 1 -r
      echo
      if [[ $REPLY =~ ^[Yy]$ ]]; then
        cp Backend/.env.example Backend/.env
        log_success "Fichier .env créé depuis .env.example"
        log_warn "N'oubliez pas de configurer les variables dans Backend/.env"
      fi
    else
      log_error "Aucun fichier .env ou .env.example trouvé"
    fi
  else
    log_success "Fichier Backend/.env existe"
  fi
}

# ─────────────────────────────────────────
# NETTOYAGE DES CONTENEURS EXISTANTS
# ─────────────────────────────────────────
cleanup_containers() {
  log_step "Nettoyage des conteneurs existants..."
  
  if docker ps -a | grep -q "smartek-"; then
    log_warn "Des conteneurs Smartek existent déjà"
    read -p "Voulez-vous les supprimer? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      docker compose down -v
      log_success "Conteneurs supprimés"
    fi
  else
    log_info "Aucun conteneur à nettoyer"
  fi
}

# ─────────────────────────────────────────
# BUILD DES IMAGES DOCKER
# ─────────────────────────────────────────
build_images() {
  log_step "Build des images Docker..."
  
  read -p "Voulez-vous builder toutes les images? (y/N) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    log_info "Build en cours (cela peut prendre plusieurs minutes)..."
    docker compose build --parallel
    log_success "Images buildées avec succès"
  else
    log_info "Build des images ignoré"
  fi
}

# ─────────────────────────────────────────
# DÉMARRAGE DES SERVICES
# ─────────────────────────────────────────
start_services() {
  log_step "Démarrage des services..."
  
  read -p "Voulez-vous démarrer tous les services? (y/N) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    log_info "Démarrage en cours..."
    ./scripts/deploy.sh
    log_success "Services démarrés"
  else
    log_info "Démarrage des services ignoré"
  fi
}

# ─────────────────────────────────────────
# AFFICHAGE DES INFORMATIONS
# ─────────────────────────────────────────
display_info() {
  log_step "Informations d'accès:"
  echo ""
  echo "════════════════════════════════════════"
  echo "  SERVICES DISPONIBLES"
  echo "════════════════════════════════════════"
  echo ""
  echo "Backend:"
  echo "  • API Gateway:    http://localhost:8080"
  echo "  • Eureka Server:  http://localhost:8761"
  echo "  • Auth Service:   http://localhost:8081"
  echo ""
  echo "Frontend:"
  echo "  • Angular App:    http://localhost:4200"
  echo ""
  echo "Monitoring:"
  echo "  • Prometheus:     http://localhost:9090"
  echo "  • Grafana:        http://localhost:3000 (admin/admin)"
  echo "  • Jaeger:         http://localhost:16686"
  echo "  • Alertmanager:   http://localhost:9093"
  echo "  • Uptime Kuma:    http://localhost:3001"
  echo ""
  echo "Base de données:"
  echo "  • MySQL:          localhost:3306 (root/root)"
  echo ""
  echo "════════════════════════════════════════"
  echo ""
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  SMARTEK - INITIALISATION DEVOPS"
  echo "════════════════════════════════════════"
  echo ""
  
  check_prerequisites
  create_directories
  check_env_files
  cleanup_containers
  build_images
  start_services
  display_info
  
  log_success "Initialisation DevOps terminée!"
  echo ""
  log_info "Commandes utiles:"
  echo "  • Voir les logs:        docker compose logs -f"
  echo "  • Arrêter les services: docker compose down"
  echo "  • Redémarrer:           docker compose restart"
  echo "  • Status:               ./scripts/status-devops.sh"
  echo ""
}

main "$@"
