#!/bin/bash
# scripts/rollback.sh
# Rollback en cas de problème de déploiement

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
BACKUP_DIR="./backups"
COMPOSE_CMD="docker compose"

# ─────────────────────────────────────────
# ROLLBACK D'UN SERVICE
# ─────────────────────────────────────────
rollback_service() {
  local service=$1
  
  log_warn "Rollback du service: $service"
  
  # Arrêter le service
  log_info "Arrêt du service..."
  $COMPOSE_CMD stop "$service"
  
  # Supprimer le conteneur
  log_info "Suppression du conteneur..."
  $COMPOSE_CMD rm -f "$service"
  
  # Redémarrer avec l'ancienne image
  log_info "Redémarrage avec l'ancienne version..."
  $COMPOSE_CMD up -d "$service"
  
  # Vérifier la santé
  sleep 10
  if docker ps | grep -q "$service"; then
    log_success "Service $service rollback avec succès"
  else
    log_error "Échec du rollback du service $service"
    return 1
  fi
}

# ─────────────────────────────────────────
# ROLLBACK DE LA BASE DE DONNÉES
# ─────────────────────────────────────────
rollback_database() {
  log_warn "Rollback de la base de données"
  
  # Lister les backups disponibles
  log_info "Backups disponibles:"
  ls -lh "$BACKUP_DIR"/*.sql 2>/dev/null || {
    log_error "Aucun backup disponible"
    return 1
  }
  
  # Sélectionner le dernier backup
  local latest_backup=$(ls -t "$BACKUP_DIR"/*.sql | head -1)
  
  log_info "Utilisation du backup: $(basename "$latest_backup")"
  
  read -p "Confirmer le rollback de la base de données? (y/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warn "Rollback de la base de données annulé"
    return 0
  fi
  
  # Restaurer le backup
  ./scripts/restore.sh "$latest_backup"
}

# ─────────────────────────────────────────
# ROLLBACK COMPLET
# ─────────────────────────────────────────
rollback_all() {
  log_error "ROLLBACK COMPLET DE L'APPLICATION"
  
  read -p "Êtes-vous sûr de vouloir faire un rollback complet? (y/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warn "Rollback annulé"
    exit 0
  fi
  
  # Arrêter tous les services
  log_info "Arrêt de tous les services..."
  $COMPOSE_CMD down
  
  # Restaurer la base de données
  rollback_database
  
  # Redémarrer avec les anciennes images
  log_info "Redémarrage avec les anciennes versions..."
  ./scripts/deploy.sh
  
  log_success "Rollback complet terminé"
}

# ─────────────────────────────────────────
# AFFICHER L'AIDE
# ─────────────────────────────────────────
show_help() {
  echo "Usage: $0 [OPTIONS]"
  echo ""
  echo "Options:"
  echo "  --service <name>    Rollback d'un service spécifique"
  echo "  --database          Rollback de la base de données"
  echo "  --all               Rollback complet"
  echo "  --help              Afficher cette aide"
  echo ""
  echo "Exemples:"
  echo "  $0 --service auth-service"
  echo "  $0 --database"
  echo "  $0 --all"
  echo ""
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo "════════════════════════════════════════"
  echo "  SMARTEK - ROLLBACK"
  echo "════════════════════════════════════════"
  echo ""
  
  if [ $# -eq 0 ]; then
    show_help
    exit 0
  fi
  
  case "${1:-}" in
    --service)
      if [ -z "${2:-}" ]; then
        log_error "Nom du service manquant"
        exit 1
      fi
      rollback_service "$2"
      ;;
    --database)
      rollback_database
      ;;
    --all)
      rollback_all
      ;;
    --help)
      show_help
      ;;
    *)
      log_error "Option invalide: $1"
      show_help
      exit 1
      ;;
  esac
}

main "$@"
