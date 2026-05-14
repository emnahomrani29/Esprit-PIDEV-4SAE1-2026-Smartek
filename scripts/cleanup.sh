#!/bin/bash
# scripts/cleanup.sh
# Nettoyage des ressources Docker

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
# NETTOYAGE DES CONTENEURS ARRÊTÉS
# ─────────────────────────────────────────
cleanup_containers() {
  log_info "Nettoyage des conteneurs arrêtés..."
  
  local stopped=$(docker ps -a -q -f status=exited | wc -l)
  
  if [ "$stopped" -gt 0 ]; then
    docker container prune -f
    log_success "$stopped conteneurs arrêtés supprimés"
  else
    log_info "Aucun conteneur arrêté à nettoyer"
  fi
}

# ─────────────────────────────────────────
# NETTOYAGE DES IMAGES NON UTILISÉES
# ─────────────────────────────────────────
cleanup_images() {
  log_info "Nettoyage des images non utilisées..."
  
  local dangling=$(docker images -f "dangling=true" -q | wc -l)
  
  if [ "$dangling" -gt 0 ]; then
    docker image prune -f
    log_success "$dangling images non utilisées supprimées"
  else
    log_info "Aucune image non utilisée à nettoyer"
  fi
}

# ─────────────────────────────────────────
# NETTOYAGE DES VOLUMES NON UTILISÉS
# ─────────────────────────────────────────
cleanup_volumes() {
  log_warn "Nettoyage des volumes non utilisés..."
  log_warn "ATTENTION: Cela supprimera les données non attachées!"
  
  read -p "Voulez-vous continuer? (y/N) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker volume prune -f
    log_success "Volumes non utilisés supprimés"
  else
    log_info "Nettoyage des volumes annulé"
  fi
}

# ─────────────────────────────────────────
# NETTOYAGE DES RÉSEAUX NON UTILISÉS
# ─────────────────────────────────────────
cleanup_networks() {
  log_info "Nettoyage des réseaux non utilisés..."
  
  docker network prune -f
  log_success "Réseaux non utilisés supprimés"
}

# ─────────────────────────────────────────
# NETTOYAGE DES ANCIENNES IMAGES SMARTEK
# ─────────────────────────────────────────
cleanup_old_smartek_images() {
  log_info "Nettoyage des anciennes images Smartek..."
  
  # Garder seulement les 3 dernières versions de chaque image
  for service in auth-service event-service planning-service training-service offers-service course-service exam-service; do
    local images=$(docker images "smartek-$service" --format "{{.ID}}" | tail -n +4)
    if [ -n "$images" ]; then
      echo "$images" | xargs -r docker rmi -f 2>/dev/null || true
      log_info "Anciennes images de $service supprimées"
    fi
  done
  
  log_success "Anciennes images Smartek nettoyées"
}

# ─────────────────────────────────────────
# NETTOYAGE DES LOGS
# ─────────────────────────────────────────
cleanup_logs() {
  log_info "Nettoyage des logs Docker..."
  
  # Tronquer les logs des conteneurs
  for container in $(docker ps -q); do
    local log_file=$(docker inspect --format='{{.LogPath}}' "$container" 2>/dev/null || echo "")
    if [ -n "$log_file" ] && [ -f "$log_file" ]; then
      truncate -s 0 "$log_file" 2>/dev/null || true
    fi
  done
  
  log_success "Logs Docker nettoyés"
}

# ─────────────────────────────────────────
# AFFICHAGE DE L'ESPACE LIBÉRÉ
# ─────────────────────────────────────────
show_disk_usage() {
  log_info "Utilisation du disque Docker:"
  echo ""
  docker system df
  echo ""
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo "════════════════════════════════════════"
  echo "  SMARTEK - NETTOYAGE DES RESSOURCES"
  echo "════════════════════════════════════════"
  echo ""
  
  log_info "Espace disque avant nettoyage:"
  show_disk_usage
  
  cleanup_containers
  cleanup_images
  cleanup_networks
  cleanup_old_smartek_images
  cleanup_logs
  
  if [ "${1:-}" == "--all" ]; then
    cleanup_volumes
  fi
  
  echo ""
  log_info "Espace disque après nettoyage:"
  show_disk_usage
  
  log_success "Nettoyage terminé!"
  echo ""
  log_info "Pour un nettoyage complet incluant les volumes:"
  echo "  ./scripts/cleanup.sh --all"
  echo ""
}

main "$@"
