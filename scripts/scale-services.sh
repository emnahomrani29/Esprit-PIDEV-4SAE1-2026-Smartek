#!/bin/bash
# scripts/scale-services.sh
# Scaling automatique des services

set -euo pipefail

# ─────────────────────────────────────────
# COULEURS POUR LES LOGS
# ─────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $*"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $*"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ─────────────────────────────────────────
# CONFIGURATION
# ─────────────────────────────────────────
COMPOSE_CMD="docker compose"

# ─────────────────────────────────────────
# SCALE UN SERVICE
# ─────────────────────────────────────────
scale_service() {
  local service=$1
  local replicas=$2
  
  log_info "Scaling $service à $replicas réplicas..."
  
  if $COMPOSE_CMD up -d --scale "$service=$replicas" "$service"; then
    log_success "$service scalé à $replicas réplicas"
  else
    log_error "Échec du scaling de $service"
    return 1
  fi
}

# ─────────────────────────────────────────
# AFFICHER L'ÉTAT ACTUEL
# ─────────────────────────────────────────
show_status() {
  log_info "État actuel des services:"
  echo ""
  $COMPOSE_CMD ps
  echo ""
}

# ─────────────────────────────────────────
# AFFICHER L'AIDE
# ─────────────────────────────────────────
show_help() {
  echo "Usage: $0 <service> <replicas>"
  echo ""
  echo "Exemples:"
  echo "  $0 auth-service 3"
  echo "  $0 course-service 2"
  echo ""
  echo "Services disponibles:"
  echo "  • auth-service"
  echo "  • event-service"
  echo "  • planning-service"
  echo "  • training-service"
  echo "  • offers-service"
  echo "  • course-service"
  echo "  • exam-service"
  echo "  • skill-evidence-service"
  echo "  • learning"
  echo "  • sponsor-service"
  echo "  • certification-badge-service"
  echo ""
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo "════════════════════════════════════════"
  echo "  SMARTEK - SCALING DES SERVICES"
  echo "════════════════════════════════════════"
  echo ""
  
  if [ $# -lt 2 ]; then
    show_help
    exit 1
  fi
  
  local service=$1
  local replicas=$2
  
  if ! [[ "$replicas" =~ ^[0-9]+$ ]]; then
    log_error "Le nombre de réplicas doit être un nombre entier"
    exit 1
  fi
  
  show_status
  scale_service "$service" "$replicas"
  sleep 5
  show_status
  
  log_success "Scaling terminé"
}

main "$@"
