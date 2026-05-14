#!/bin/bash

###############################################################################
# Script de vérification de toutes les images Docker nécessaires
# Usage: ./scripts/verify-images.sh
###############################################################################

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Vérification des images Docker - Projet Smartek          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

MISSING_COUNT=0
PRESENT_COUNT=0
declare -a MISSING_IMAGES

# Fonction pour vérifier une image
check_image() {
    local image_name=$1
    local image_tag=${2:-latest}
    local full_image="${image_name}:${image_tag}"
    
    if docker image inspect "${full_image}" &> /dev/null; then
        echo -e "${GREEN}✓${NC} ${full_image}"
        PRESENT_COUNT=$((PRESENT_COUNT + 1))
        return 0
    else
        echo -e "${RED}✗${NC} ${full_image} ${YELLOW}(manquante)${NC}"
        MISSING_IMAGES+=("${full_image}")
        MISSING_COUNT=$((MISSING_COUNT + 1))
        return 1
    fi
}

# ═══════════════════════════════════════════════════════════════════════════
# IMAGES EXTERNES
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${CYAN}━━━ Images Externes (Infrastructure) ━━━${NC}"
echo ""

check_image "mysql" "8.0"
check_image "prom/prometheus" "latest"
check_image "grafana/grafana" "10.4.0"
check_image "grafana/loki" "latest"
check_image "grafana/promtail" "latest"
check_image "jaegertracing/all-in-one" "latest"
check_image "sonarqube" "community"
check_image "sonatype/nexus3" "latest"
check_image "jenkins/jenkins" "lts"

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# IMAGES CUSTOM - Backend
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${CYAN}━━━ Images Custom (Backend Services) ━━━${NC}"
echo ""

DOCKER_REGISTRY="${DOCKER_REGISTRY:-smartek}"

check_image "${DOCKER_REGISTRY}/api-gateway" "latest"
check_image "${DOCKER_REGISTRY}/auth-service" "latest"
check_image "${DOCKER_REGISTRY}/certification-badge-service" "latest"
check_image "${DOCKER_REGISTRY}/config-server" "latest"
check_image "${DOCKER_REGISTRY}/course-service" "latest"
check_image "${DOCKER_REGISTRY}/eureka-server" "latest"
check_image "${DOCKER_REGISTRY}/event-service" "latest"
check_image "${DOCKER_REGISTRY}/exam-service" "latest"
check_image "${DOCKER_REGISTRY}/learning-service" "latest"
check_image "${DOCKER_REGISTRY}/offers-service" "latest"
check_image "${DOCKER_REGISTRY}/planning-service" "latest"
check_image "${DOCKER_REGISTRY}/skill-evidence-service" "latest"
check_image "${DOCKER_REGISTRY}/sponsor-service" "latest"
check_image "${DOCKER_REGISTRY}/training-service" "latest"

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# IMAGES CUSTOM - Frontend
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${CYAN}━━━ Images Custom (Frontend) ━━━${NC}"
echo ""

check_image "${DOCKER_REGISTRY}/angular-app" "latest"

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# RÉSUMÉ
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  RÉSUMÉ DE LA VÉRIFICATION                                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ Images présentes:${NC} $PRESENT_COUNT"
echo -e "${RED}✗ Images manquantes:${NC} $MISSING_COUNT"
echo -e "${YELLOW}Total:${NC} $((PRESENT_COUNT + MISSING_COUNT))"
echo ""

if [ $MISSING_COUNT -gt 0 ]; then
    echo -e "${RED}Images manquantes:${NC}"
    for image in "${MISSING_IMAGES[@]}"; do
        echo -e "  ${RED}✗${NC} $image"
    done
    echo ""
    echo -e "${YELLOW}Pour télécharger/construire les images manquantes:${NC}"
    echo -e "  ${CYAN}./scripts/build-all-images.sh --pull-external${NC}"
    echo ""
    exit 1
else
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ TOUTES LES IMAGES SONT PRÉSENTES !                     ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Statistiques
    echo -e "${YELLOW}Espace disque utilisé:${NC}"
    docker system df
    echo ""
fi

exit 0
