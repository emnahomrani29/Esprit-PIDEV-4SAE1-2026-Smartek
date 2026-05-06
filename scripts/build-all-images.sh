#!/bin/bash

###############################################################################
# Script de construction et préparation de toutes les images Docker du projet
# Usage: ./scripts/build-all-images.sh [--push] [--tag VERSION] [--pull-external]
###############################################################################

set -e  # Arrêter en cas d'erreur

# Couleurs pour les logs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Variables
DOCKER_REGISTRY="${DOCKER_REGISTRY:-smartek}"
VERSION="${VERSION:-latest}"
PUSH_IMAGES=false
PULL_EXTERNAL=false
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --push)
            PUSH_IMAGES=true
            shift
            ;;
        --tag)
            VERSION="$2"
            shift 2
            ;;
        --registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --pull-external)
            PULL_EXTERNAL=true
            shift
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Préparation des images Docker - Projet Smartek           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Registry:${NC} $DOCKER_REGISTRY"
echo -e "${YELLOW}Version:${NC} $VERSION"
echo -e "${YELLOW}Push:${NC} $PUSH_IMAGES"
echo -e "${YELLOW}Pull External:${NC} $PULL_EXTERNAL"
echo -e "${YELLOW}Build Date:${NC} $BUILD_DATE"
echo ""

# Compteurs
TOTAL_SERVICES=0
TOTAL_EXTERNAL=0
SUCCESS_COUNT=0
FAILED_COUNT=0
declare -a FAILED_SERVICES

# Fonction pour pull une image externe
pull_external_image() {
    local image_name=$1
    local image_tag=${2:-latest}
    local full_image="${image_name}:${image_tag}"
    
    TOTAL_EXTERNAL=$((TOTAL_EXTERNAL + 1))
    
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}[EXTERNAL] Pull: ${full_image}${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    if docker pull "${full_image}"; then
        echo -e "${GREEN}✓ Pull réussi: ${full_image}${NC}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo -e "${RED}✗ Échec du pull: ${full_image}${NC}"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_SERVICES+=("${full_image} (pull failed)")
        return 1
    fi
    
    echo ""
}

# Fonction pour construire une image
build_image() {
    local service_name=$1
    local service_path=$2
    local image_name="${DOCKER_REGISTRY}/${service_name}:${VERSION}"
    
    TOTAL_SERVICES=$((TOTAL_SERVICES + 1))
    
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}[$TOTAL_SERVICES] Construction: ${service_name}${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    if [ ! -f "${service_path}/Dockerfile" ]; then
        echo -e "${RED}✗ Dockerfile non trouvé: ${service_path}/Dockerfile${NC}"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_SERVICES+=("$service_name (Dockerfile manquant)")
        return 1
    fi
    
    echo -e "Image: ${GREEN}${image_name}${NC}"
    echo -e "Path: ${service_path}"
    echo ""
    
    # Construction de l'image
    if docker build \
        --build-arg BUILD_DATE="${BUILD_DATE}" \
        --build-arg VERSION="${VERSION}" \
        -t "${image_name}" \
        -t "${DOCKER_REGISTRY}/${service_name}:latest" \
        "${service_path}"; then
        
        echo -e "${GREEN}✓ Build réussi: ${service_name}${NC}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        
        # Push si demandé
        if [ "$PUSH_IMAGES" = true ]; then
            echo -e "${YELLOW}Pushing ${image_name}...${NC}"
            if docker push "${image_name}" && docker push "${DOCKER_REGISTRY}/${service_name}:latest"; then
                echo -e "${GREEN}✓ Push réussi: ${service_name}${NC}"
            else
                echo -e "${RED}✗ Échec du push: ${service_name}${NC}"
                FAILED_COUNT=$((FAILED_COUNT + 1))
                FAILED_SERVICES+=("$service_name (push failed)")
                return 1
            fi
        fi
    else
        echo -e "${RED}✗ Échec du build: ${service_name}${NC}"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_SERVICES+=("$service_name (build failed)")
        return 1
    fi
    
    echo ""
}

# Début du build
START_TIME=$(date +%s)

# ═══════════════════════════════════════════════════════════════════════════
# IMAGES EXTERNES (Infrastructure & Monitoring)
# ═══════════════════════════════════════════════════════════════════════════

if [ "$PULL_EXTERNAL" = true ]; then
    echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${MAGENTA}║  IMAGES EXTERNES - Infrastructure & Monitoring            ║${NC}"
    echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Base de données
    pull_external_image "mysql" "8.0"
    
    # Monitoring
    pull_external_image "prom/prometheus" "latest"
    pull_external_image "grafana/grafana" "10.4.0"
    pull_external_image "grafana/loki" "latest"
    pull_external_image "grafana/promtail" "latest"
    
    # Tracing
    pull_external_image "jaegertracing/all-in-one" "latest"
    
    # DevOps Tools
    pull_external_image "sonarqube" "community"
    pull_external_image "sonatype/nexus3" "latest"
    pull_external_image "jenkins/jenkins" "lts"
    
    echo -e "${GREEN}✓ Toutes les images externes ont été téléchargées${NC}"
    echo ""
fi

# ═══════════════════════════════════════════════════════════════════════════
# IMAGES CUSTOM - Backend Services
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  BACKEND SERVICES (Java/Spring Boot)                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Services Backend
build_image "api-gateway" "Backend/api-gateway"
build_image "auth-service" "Backend/auth-service"
build_image "certification-badge-service" "Backend/certification-badge-service"
build_image "config-server" "Backend/config-server"
build_image "course-service" "Backend/course-service"
build_image "eureka-server" "Backend/eureka-server"
build_image "event-service" "Backend/event-service"
build_image "exam-service" "Backend/exam-service"
build_image "learning-service" "Backend/learning"
build_image "offers-service" "Backend/offers-service"
build_image "planning-service" "Backend/planning-service"
build_image "skill-evidence-service" "Backend/skiil-evidence-service"
build_image "sponsor-service" "Backend/smartek_sponsor"
build_image "training-service" "Backend/training-service"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  FRONTEND SERVICE (Angular)                                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Service Frontend
build_image "angular-app" "Frontend/angular-app"

# Fin du build
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

# Résumé
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  RÉSUMÉ DE LA PRÉPARATION                                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Images externes:${NC} $TOTAL_EXTERNAL"
echo -e "${YELLOW}Services custom:${NC} $TOTAL_SERVICES"
echo -e "${YELLOW}Total:${NC} $((TOTAL_SERVICES + TOTAL_EXTERNAL))"
echo -e "${GREEN}✓ Réussis:${NC} $SUCCESS_COUNT"
echo -e "${RED}✗ Échoués:${NC} $FAILED_COUNT"
echo -e "${YELLOW}Durée totale:${NC} ${MINUTES}m ${SECONDS}s"
echo ""

# Liste des services échoués
if [ $FAILED_COUNT -gt 0 ]; then
    echo -e "${RED}Services échoués:${NC}"
    for service in "${FAILED_SERVICES[@]}"; do
        echo -e "  ${RED}✗${NC} $service"
    done
    echo ""
    exit 1
else
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ TOUTES LES IMAGES ONT ÉTÉ PRÉPARÉES AVEC SUCCÈS !      ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Afficher les images créées
    echo -e "${YELLOW}Images disponibles:${NC}"
    echo ""
    echo -e "${CYAN}=== Images Custom ===${NC}"
    docker images | grep "${DOCKER_REGISTRY}" | grep -E "(${VERSION}|latest)" | head -30
    echo ""
    
    if [ "$PULL_EXTERNAL" = true ]; then
        echo -e "${CYAN}=== Images Externes ===${NC}"
        docker images | grep -E "(mysql|prometheus|grafana|loki|promtail|jaeger|sonarqube|nexus|jenkins)" | head -20
        echo ""
    fi
    
    # Statistiques de taille
    echo -e "${YELLOW}Espace disque utilisé par les images:${NC}"
    docker system df
    echo ""
fi

exit 0
