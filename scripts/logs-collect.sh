#!/bin/bash
# scripts/logs-collect.sh
# Collection centralisée des logs

set -euo pipefail

# ─────────────────────────────────────────
# CONFIGURATION
# ─────────────────────────────────────────
LOGS_DIR="./logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_DIR="$LOGS_DIR/collected_$TIMESTAMP"

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
# CRÉATION DU RÉPERTOIRE DE SORTIE
# ─────────────────────────────────────────
create_output_dir() {
  mkdir -p "$OUTPUT_DIR"
  log_info "Répertoire de sortie: $OUTPUT_DIR"
}

# ─────────────────────────────────────────
# COLLECTE DES LOGS D'UN SERVICE
# ─────────────────────────────────────────
collect_service_logs() {
  local service=$1
  local lines=${2:-1000}
  
  log_info "Collecte des logs de $service..."
  
  if docker ps | grep -q "$service"; then
    docker logs --tail "$lines" "$service" > "$OUTPUT_DIR/${service}.log" 2>&1
    log_success "Logs de $service collectés"
  else
    log_warn "Service $service non trouvé"
  fi
}

# ─────────────────────────────────────────
# COLLECTE DES LOGS DE TOUS LES SERVICES
# ─────────────────────────────────────────
collect_all_logs() {
  local lines=${1:-1000}
  
  log_info "Collecte des logs de tous les services Smartek..."
  
  for container in $(docker ps --filter "name=smartek-" --format "{{.Names}}"); do
    collect_service_logs "$container" "$lines"
  done
}

# ─────────────────────────────────────────
# COLLECTE DES INFORMATIONS SYSTÈME
# ─────────────────────────────────────────
collect_system_info() {
  log_info "Collecte des informations système..."
  
  {
    echo "════════════════════════════════════════"
    echo "  INFORMATIONS SYSTÈME"
    echo "════════════════════════════════════════"
    echo ""
    echo "Date: $(date)"
    echo ""
    echo "Docker Version:"
    docker --version
    echo ""
    echo "Docker Compose Version:"
    docker compose version
    echo ""
    echo "Conteneurs en cours d'exécution:"
    docker ps
    echo ""
    echo "Utilisation du disque Docker:"
    docker system df
    echo ""
    echo "Utilisation des ressources:"
    docker stats --no-stream
  } > "$OUTPUT_DIR/system_info.txt"
  
  log_success "Informations système collectées"
}

# ─────────────────────────────────────────
# COMPRESSION DES LOGS
# ─────────────────────────────────────────
compress_logs() {
  log_info "Compression des logs..."
  
  local archive="$LOGS_DIR/logs_$TIMESTAMP.tar.gz"
  
  tar -czf "$archive" -C "$LOGS_DIR" "collected_$TIMESTAMP"
  
  local size=$(du -h "$archive" | cut -f1)
  log_success "Archive créée: $archive ($size)"
  
  # Supprimer le répertoire temporaire
  rm -rf "$OUTPUT_DIR"
}

# ─────────────────────────────────────────
# AFFICHER L'AIDE
# ─────────────────────────────────────────
show_help() {
  echo "Usage: $0 [OPTIONS]"
  echo ""
  echo "Options:"
  echo "  --service <name>    Collecter les logs d'un service spécifique"
  echo "  --lines <number>    Nombre de lignes à collecter (défaut: 1000)"
  echo "  --all               Collecter tous les logs"
  echo "  --no-compress       Ne pas compresser les logs"
  echo "  --help              Afficher cette aide"
  echo ""
  echo "Exemples:"
  echo "  $0 --all"
  echo "  $0 --service smartek-auth --lines 5000"
  echo "  $0 --all --no-compress"
  echo ""
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo "════════════════════════════════════════"
  echo "  SMARTEK - COLLECTION DES LOGS"
  echo "════════════════════════════════════════"
  echo ""
  
  local service=""
  local lines=1000
  local compress=true
  local collect_all=false
  
  while [[ $# -gt 0 ]]; do
    case $1 in
      --service)
        service="$2"
        shift 2
        ;;
      --lines)
        lines="$2"
        shift 2
        ;;
      --all)
        collect_all=true
        shift
        ;;
      --no-compress)
        compress=false
        shift
        ;;
      --help)
        show_help
        exit 0
        ;;
      *)
        log_error "Option invalide: $1"
        show_help
        exit 1
        ;;
    esac
  done
  
  create_output_dir
  
  if [ "$collect_all" = true ]; then
    collect_all_logs "$lines"
    collect_system_info
  elif [ -n "$service" ]; then
    collect_service_logs "$service" "$lines"
  else
    show_help
    exit 1
  fi
  
  if [ "$compress" = true ]; then
    compress_logs
  else
    log_success "Logs collectés dans: $OUTPUT_DIR"
  fi
  
  log_success "Collection terminée"
}

main "$@"
