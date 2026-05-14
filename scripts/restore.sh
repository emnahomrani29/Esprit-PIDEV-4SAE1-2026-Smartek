#!/bin/bash
# scripts/restore.sh
# Restauration des backups de base de données

set -euo pipefail

# ─────────────────────────────────────────
# CONFIGURATION
# ─────────────────────────────────────────
BACKUP_DIR="./backups"
MYSQL_CONTAINER="smartek-mysql"
MYSQL_USER="root"
MYSQL_PASSWORD="root"

# ─────────────────────────────────────────
# COULEURS POUR LES LOGS
# ─────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() { echo -e "${BLUE}[INFO]${NC} $*"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $*"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ─────────────────────────────────────────
# VÉRIFICATION DES PRÉREQUIS
# ─────────────────────────────────────────
check_prerequisites() {
  log_info "Vérification des prérequis..."
  
  if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé"
    exit 1
  fi
  
  if ! docker ps | grep -q "$MYSQL_CONTAINER"; then
    log_error "Le conteneur MySQL n'est pas en cours d'exécution"
    exit 1
  fi
  
  if [ ! -d "$BACKUP_DIR" ]; then
    log_error "Le répertoire de backup n'existe pas: $BACKUP_DIR"
    exit 1
  fi
  
  log_success "Prérequis vérifiés"
}

# ─────────────────────────────────────────
# LISTER LES BACKUPS DISPONIBLES
# ─────────────────────────────────────────
list_backups() {
  log_info "Backups disponibles:"
  echo ""
  
  local i=1
  for backup in "$BACKUP_DIR"/*.sql; do
    if [ -f "$backup" ]; then
      local size=$(du -h "$backup" | cut -f1)
      local date=$(stat -c %y "$backup" 2>/dev/null || stat -f "%Sm" "$backup")
      echo "  [$i] $(basename "$backup") - $size - $date"
      ((i++))
    fi
  done
  
  echo ""
}

# ─────────────────────────────────────────
# RESTAURER UN BACKUP
# ─────────────────────────────────────────
restore_backup() {
  local backup_file=$1
  
  if [ ! -f "$backup_file" ]; then
    log_error "Le fichier de backup n'existe pas: $backup_file"
    exit 1
  fi
  
  log_info "Restauration du backup: $(basename "$backup_file")"
  
  # Confirmation
  read -p "Êtes-vous sûr de vouloir restaurer ce backup? (y/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warn "Restauration annulée"
    exit 0
  fi
  
  # Restauration
  log_info "Restauration en cours..."
  
  if docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < "$backup_file"; then
    log_success "Backup restauré avec succès"
  else
    log_error "Échec de la restauration"
    exit 1
  fi
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo "════════════════════════════════════════"
  echo "  SMARTEK - RESTAURATION DE BACKUP"
  echo "════════════════════════════════════════"
  echo ""
  
  check_prerequisites
  
  if [ $# -eq 0 ]; then
    list_backups
    read -p "Entrez le numéro du backup à restaurer (ou le chemin complet): " choice
    
    if [[ "$choice" =~ ^[0-9]+$ ]]; then
      # Sélection par numéro
      backup_file=$(ls "$BACKUP_DIR"/*.sql | sed -n "${choice}p")
    else
      # Chemin complet
      backup_file="$choice"
    fi
  else
    backup_file="$1"
  fi
  
  restore_backup "$backup_file"
}

main "$@"
