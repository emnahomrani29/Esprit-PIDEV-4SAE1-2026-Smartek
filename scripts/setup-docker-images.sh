#!/bin/bash

###############################################################################
# Script de configuration initiale pour la préparation des images Docker
# Ce script configure l'environnement et lance la préparation complète
# Usage: ./scripts/setup-docker-images.sh
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
echo -e "${BLUE}║  Configuration initiale - Images Docker Smartek           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Vérifications préalables
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[1/5] Vérification des prérequis...${NC}"
echo ""

# Vérifier Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker n'est pas installé${NC}"
    echo -e "${YELLOW}Veuillez installer Docker: https://docs.docker.com/get-docker/${NC}"
    exit 1
else
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✓ Docker installé: ${DOCKER_VERSION}${NC}"
fi

# Vérifier Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}⚠ Docker Compose n'est pas installé (optionnel)${NC}"
else
    COMPOSE_VERSION=$(docker-compose --version)
    echo -e "${GREEN}✓ Docker Compose installé: ${COMPOSE_VERSION}${NC}"
fi

# Vérifier que Docker est en cours d'exécution
if ! docker info &> /dev/null; then
    echo -e "${RED}✗ Docker n'est pas en cours d'exécution${NC}"
    echo -e "${YELLOW}Veuillez démarrer Docker${NC}"
    exit 1
else
    echo -e "${GREEN}✓ Docker est en cours d'exécution${NC}"
fi

# Vérifier l'espace disque
AVAILABLE_SPACE=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
if [ "$AVAILABLE_SPACE" -lt 10 ]; then
    echo -e "${RED}✗ Espace disque insuffisant: ${AVAILABLE_SPACE}GB disponible${NC}"
    echo -e "${YELLOW}Au moins 10GB d'espace libre sont recommandés${NC}"
    read -p "Voulez-vous continuer quand même? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}✓ Espace disque suffisant: ${AVAILABLE_SPACE}GB disponible${NC}"
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Rendre les scripts exécutables
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[2/5] Configuration des permissions des scripts...${NC}"
echo ""

chmod +x scripts/build-all-images.sh
chmod +x scripts/verify-images.sh
chmod +x scripts/check-services.sh
chmod +x scripts/backup.sh
chmod +x scripts/start-devops.sh
chmod +x scripts/stop-devops.sh

echo -e "${GREEN}✓ Permissions configurées${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Configuration
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[3/5] Configuration...${NC}"
echo ""

# Demander si l'utilisateur veut télécharger les images externes
echo -e "${CYAN}Voulez-vous télécharger les images externes (MySQL, Prometheus, etc.)?${NC}"
echo -e "${YELLOW}Cela prendra environ 10-15 minutes et utilisera ~5GB d'espace disque${NC}"
read -p "Télécharger les images externes? (Y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Nn]$ ]]; then
    PULL_EXTERNAL=false
    echo -e "${YELLOW}⚠ Les images externes ne seront pas téléchargées${NC}"
else
    PULL_EXTERNAL=true
    echo -e "${GREEN}✓ Les images externes seront téléchargées${NC}"
fi

echo ""

# Demander le registry
echo -e "${CYAN}Registry Docker (défaut: smartek):${NC}"
read -p "Registry: " REGISTRY
REGISTRY=${REGISTRY:-smartek}
echo -e "${GREEN}✓ Registry: ${REGISTRY}${NC}"

echo ""

# Demander le tag
echo -e "${CYAN}Tag de version (défaut: latest):${NC}"
read -p "Tag: " TAG
TAG=${TAG:-latest}
echo -e "${GREEN}✓ Tag: ${TAG}${NC}"

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Résumé de la configuration
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  RÉSUMÉ DE LA CONFIGURATION                                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Registry:${NC} ${REGISTRY}"
echo -e "${YELLOW}Tag:${NC} ${TAG}"
echo -e "${YELLOW}Images externes:${NC} $([ "$PULL_EXTERNAL" = true ] && echo "Oui" || echo "Non")"
echo -e "${YELLOW}Services à construire:${NC} 15 (14 backend + 1 frontend)"
if [ "$PULL_EXTERNAL" = true ]; then
    echo -e "${YELLOW}Images externes:${NC} 9 (MySQL, Prometheus, Grafana, etc.)"
fi
echo ""

read -p "Continuer avec cette configuration? (Y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo -e "${YELLOW}Configuration annulée${NC}"
    exit 0
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Construction des images
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[4/5] Construction des images Docker...${NC}"
echo ""

BUILD_CMD="./scripts/build-all-images.sh --registry ${REGISTRY} --tag ${TAG}"
if [ "$PULL_EXTERNAL" = true ]; then
    BUILD_CMD="${BUILD_CMD} --pull-external"
fi

echo -e "${CYAN}Commande: ${BUILD_CMD}${NC}"
echo ""

# Exécuter la construction
if eval "$BUILD_CMD"; then
    echo -e "${GREEN}✓ Construction réussie${NC}"
else
    echo -e "${RED}✗ Échec de la construction${NC}"
    exit 1
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Vérification
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[5/5] Vérification des images...${NC}"
echo ""

if ./scripts/verify-images.sh; then
    echo -e "${GREEN}✓ Vérification réussie${NC}"
else
    echo -e "${RED}✗ Certaines images sont manquantes${NC}"
    exit 1
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Finalisation
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ CONFIGURATION TERMINÉE AVEC SUCCÈS !                    ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}Prochaines étapes:${NC}"
echo ""
echo -e "  ${YELLOW}1.${NC} Démarrer tous les services:"
echo -e "     ${CYAN}docker-compose up -d${NC}"
echo ""
echo -e "  ${YELLOW}2.${NC} Vérifier l'état des services:"
echo -e "     ${CYAN}./scripts/check-services.sh${NC}"
echo ""
echo -e "  ${YELLOW}3.${NC} Accéder aux interfaces:"
echo -e "     ${CYAN}Frontend:${NC}     http://localhost:4200"
echo -e "     ${CYAN}API Gateway:${NC}  http://localhost:8080"
echo -e "     ${CYAN}Eureka:${NC}       http://localhost:8761"
echo -e "     ${CYAN}Grafana:${NC}      http://localhost:3000"
echo -e "     ${CYAN}Prometheus:${NC}   http://localhost:9090"
echo ""
echo -e "  ${YELLOW}4.${NC} Consulter la documentation:"
echo -e "     ${CYAN}cat scripts/DOCKER_IMAGES_README.md${NC}"
echo ""

exit 0
