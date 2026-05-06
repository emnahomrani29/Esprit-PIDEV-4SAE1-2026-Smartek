#!/bin/bash

###############################################################################
# Script pour construire ET pousser toutes les images Docker
# Usage: ./scripts/build-and-push.sh [OPTIONS]
###############################################################################

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Variables par défaut
DOCKER_REGISTRY="${DOCKER_REGISTRY:-smartek}"
VERSION="${VERSION:-latest}"
PULL_EXTERNAL=false
SKIP_BUILD=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --tag)
            VERSION="$2"
            shift 2
            ;;
        --pull-external)
            PULL_EXTERNAL=true
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --registry NAME      Registry Docker (défaut: smartek)"
            echo "  --tag VERSION        Tag de version (défaut: latest)"
            echo "  --pull-external      Télécharger les images externes"
            echo "  --skip-build         Sauter la construction (push uniquement)"
            echo "  --help               Afficher cette aide"
            echo ""
            echo "Exemples:"
            echo "  $0 --registry myuser --tag v1.0.0"
            echo "  $0 --registry mycompany/smartek --tag latest --pull-external"
            exit 0
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            echo "Utilisez --help pour voir les options disponibles"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Build & Push - Images Docker Smartek                     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Configuration
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}Configuration:${NC}"
echo -e "  Registry: ${CYAN}${DOCKER_REGISTRY}${NC}"
echo -e "  Tag:      ${CYAN}${VERSION}${NC}"
echo -e "  Build:    ${CYAN}$([ "$SKIP_BUILD" = true ] && echo "Non" || echo "Oui")${NC}"
echo -e "  External: ${CYAN}$([ "$PULL_EXTERNAL" = true ] && echo "Oui" || echo "Non")${NC}"
echo ""

read -p "Continuer avec cette configuration? (Y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo -e "${YELLOW}Opération annulée${NC}"
    exit 0
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Étape 1: Construction des images
# ═══════════════════════════════════════════════════════════════════════════

if [ "$SKIP_BUILD" = false ]; then
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  ÉTAPE 1/2 : Construction des images                      ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    BUILD_CMD="./scripts/build-all-images.sh --registry ${DOCKER_REGISTRY} --tag ${VERSION}"
    if [ "$PULL_EXTERNAL" = true ]; then
        BUILD_CMD="${BUILD_CMD} --pull-external"
    fi
    
    echo -e "${CYAN}Commande: ${BUILD_CMD}${NC}"
    echo ""
    
    if eval "$BUILD_CMD"; then
        echo -e "${GREEN}✓ Construction réussie${NC}"
    else
        echo -e "${RED}✗ Échec de la construction${NC}"
        exit 1
    fi
    
    echo ""
else
    echo -e "${YELLOW}⚠ Construction ignorée (--skip-build)${NC}"
    echo ""
fi

# ═══════════════════════════════════════════════════════════════════════════
# Étape 2: Push des images
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  ÉTAPE 2/2 : Push des images vers le registry             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

PUSH_CMD="./scripts/push-all-images.sh --registry ${DOCKER_REGISTRY} --tag ${VERSION}"

echo -e "${CYAN}Commande: ${PUSH_CMD}${NC}"
echo ""

if eval "$PUSH_CMD"; then
    echo -e "${GREEN}✓ Push réussi${NC}"
else
    echo -e "${RED}✗ Échec du push${NC}"
    exit 1
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Finalisation
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ BUILD & PUSH TERMINÉS AVEC SUCCÈS !                    ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}Images disponibles:${NC}"
echo ""
echo -e "  ${YELLOW}Registry:${NC} ${DOCKER_REGISTRY}"
echo -e "  ${YELLOW}Tag:${NC} ${VERSION}"
echo ""

# Afficher les commandes pour utiliser les images
echo -e "${CYAN}Pour utiliser ces images:${NC}"
echo ""
echo -e "  ${YELLOW}1.${NC} Pull une image:"
echo -e "     ${CYAN}docker pull ${DOCKER_REGISTRY}/auth-service:${VERSION}${NC}"
echo ""
echo -e "  ${YELLOW}2.${NC} Mettre à jour docker-compose.yml:"
echo -e "     ${CYAN}Remplacer 'build: ...' par 'image: ${DOCKER_REGISTRY}/service-name:${VERSION}'${NC}"
echo ""
echo -e "  ${YELLOW}3.${NC} Déployer avec les nouvelles images:"
echo -e "     ${CYAN}docker-compose pull${NC}"
echo -e "     ${CYAN}docker-compose up -d${NC}"
echo ""

exit 0
