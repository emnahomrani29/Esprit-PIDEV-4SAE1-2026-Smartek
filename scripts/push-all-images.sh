#!/bin/bash

###############################################################################
# Script pour pousser toutes les images Docker vers un registry
# Usage: ./scripts/push-all-images.sh [--registry REGISTRY] [--tag VERSION]
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
DOCKER_USERNAME=""
DOCKER_PASSWORD=""

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
        --username)
            DOCKER_USERNAME="$2"
            shift 2
            ;;
        --password)
            DOCKER_PASSWORD="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            echo "Usage: $0 [--registry REGISTRY] [--tag VERSION] [--username USER] [--password PASS]"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Push des images Docker vers le registry                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Vérifications
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[1/4] Vérifications préalables...${NC}"
echo ""

# Vérifier Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker n'est pas installé${NC}"
    exit 1
fi

# Vérifier que Docker est en cours d'exécution
if ! docker info &> /dev/null; then
    echo -e "${RED}✗ Docker n'est pas en cours d'exécution${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker est prêt${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Configuration du registry
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[2/4] Configuration du registry...${NC}"
echo ""

echo -e "${CYAN}Registry cible:${NC} ${DOCKER_REGISTRY}"
echo -e "${CYAN}Tag de version:${NC} ${VERSION}"
echo ""

# Déterminer le type de registry
if [[ "$DOCKER_REGISTRY" == *"docker.io"* ]] || [[ "$DOCKER_REGISTRY" != *"."* ]]; then
    REGISTRY_TYPE="Docker Hub"
    REGISTRY_URL="docker.io"
elif [[ "$DOCKER_REGISTRY" == *"gcr.io"* ]]; then
    REGISTRY_TYPE="Google Container Registry"
    REGISTRY_URL="gcr.io"
elif [[ "$DOCKER_REGISTRY" == *"azurecr.io"* ]]; then
    REGISTRY_TYPE="Azure Container Registry"
    REGISTRY_URL="${DOCKER_REGISTRY}"
elif [[ "$DOCKER_REGISTRY" == *"amazonaws.com"* ]]; then
    REGISTRY_TYPE="Amazon ECR"
    REGISTRY_URL="${DOCKER_REGISTRY}"
else
    REGISTRY_TYPE="Registry privé"
    REGISTRY_URL="${DOCKER_REGISTRY}"
fi

echo -e "${CYAN}Type de registry:${NC} ${REGISTRY_TYPE}"
echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Authentification
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[3/4] Authentification au registry...${NC}"
echo ""

# Vérifier si déjà connecté
if docker info 2>/dev/null | grep -q "Username"; then
    CURRENT_USER=$(docker info 2>/dev/null | grep "Username" | awk '{print $2}')
    echo -e "${GREEN}✓ Déjà connecté en tant que: ${CURRENT_USER}${NC}"
    echo ""
    read -p "Voulez-vous vous reconnecter? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Utilisation de la session existante${NC}"
    else
        DOCKER_USERNAME=""
    fi
fi

# Demander les credentials si nécessaire
if [ -z "$DOCKER_USERNAME" ]; then
    echo -e "${CYAN}Authentification requise pour ${REGISTRY_TYPE}${NC}"
    echo ""
    
    if [ "$REGISTRY_TYPE" == "Docker Hub" ]; then
        echo -e "${YELLOW}Entrez vos identifiants Docker Hub:${NC}"
        read -p "Username: " DOCKER_USERNAME
        read -sp "Password (ou Token): " DOCKER_PASSWORD
        echo ""
        
        if [ -n "$DOCKER_USERNAME" ] && [ -n "$DOCKER_PASSWORD" ]; then
            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ Authentification réussie${NC}"
            else
                echo -e "${RED}✗ Échec de l'authentification${NC}"
                exit 1
            fi
        else
            echo -e "${YELLOW}⚠ Tentative sans authentification...${NC}"
        fi
    else
        echo -e "${YELLOW}Pour les registries privés, assurez-vous d'être déjà authentifié${NC}"
        echo -e "${CYAN}Commandes d'authentification:${NC}"
        echo -e "  Docker Hub:  ${CYAN}docker login${NC}"
        echo -e "  GCR:         ${CYAN}gcloud auth configure-docker${NC}"
        echo -e "  ECR:         ${CYAN}aws ecr get-login-password | docker login --username AWS --password-stdin <registry>${NC}"
        echo -e "  Azure:       ${CYAN}az acr login --name <registry>${NC}"
        echo ""
        read -p "Êtes-vous déjà authentifié? (Y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Nn]$ ]]; then
            echo -e "${RED}Veuillez vous authentifier d'abord${NC}"
            exit 1
        fi
    fi
fi

echo ""

# ═══════════════════════════════════════════════════════════════════════════
# Push des images
# ═══════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}[4/4] Push des images vers le registry...${NC}"
echo ""

TOTAL_IMAGES=0
SUCCESS_COUNT=0
FAILED_COUNT=0
declare -a FAILED_IMAGES

# Fonction pour pousser une image
push_image() {
    local service_name=$1
    local image_name="${DOCKER_REGISTRY}/${service_name}:${VERSION}"
    local image_latest="${DOCKER_REGISTRY}/${service_name}:latest"
    
    TOTAL_IMAGES=$((TOTAL_IMAGES + 1))
    
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}[$TOTAL_IMAGES] Push: ${service_name}${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    # Vérifier que l'image existe localement
    if ! docker image inspect "${image_name}" &> /dev/null; then
        echo -e "${RED}✗ Image non trouvée localement: ${image_name}${NC}"
        echo -e "${YELLOW}  Construisez d'abord l'image avec: ./scripts/build-all-images.sh${NC}"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_IMAGES+=("$service_name (image non trouvée)")
        return 1
    fi
    
    echo -e "Image: ${GREEN}${image_name}${NC}"
    
    # Push de l'image avec le tag de version
    if docker push "${image_name}"; then
        echo -e "${GREEN}✓ Push réussi: ${image_name}${NC}"
        
        # Push de l'image avec le tag latest
        if docker image inspect "${image_latest}" &> /dev/null; then
            if docker push "${image_latest}"; then
                echo -e "${GREEN}✓ Push réussi: ${image_latest}${NC}"
            else
                echo -e "${YELLOW}⚠ Échec du push du tag latest${NC}"
            fi
        fi
        
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo -e "${RED}✗ Échec du push: ${service_name}${NC}"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_IMAGES+=("$service_name (push failed)")
        return 1
    fi
    
    echo ""
}

START_TIME=$(date +%s)

# Liste des services à pousser
echo -e "${BLUE}═══ Backend Services ═══${NC}"
echo ""

push_image "api-gateway"
push_image "auth-service"
push_image "certification-badge-service"
push_image "config-server"
push_image "course-service"
push_image "eureka-server"
push_image "event-service"
push_image "exam-service"
push_image "learning-service"
push_image "offers-service"
push_image "planning-service"
push_image "skill-evidence-service"
push_image "sponsor-service"
push_image "training-service"

echo -e "${BLUE}═══ Frontend Service ═══${NC}"
echo ""

push_image "angular-app"

# ═══════════════════════════════════════════════════════════════════════════
# Résumé
# ═══════════════════════════════════════════════════════════════════════════

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  RÉSUMÉ DU PUSH                                            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Registry:${NC} ${DOCKER_REGISTRY}"
echo -e "${CYAN}Tag:${NC} ${VERSION}"
echo -e "${YELLOW}Total d'images:${NC} $TOTAL_IMAGES"
echo -e "${GREEN}✓ Réussies:${NC} $SUCCESS_COUNT"
echo -e "${RED}✗ Échouées:${NC} $FAILED_COUNT"
echo -e "${YELLOW}Durée totale:${NC} ${MINUTES}m ${SECONDS}s"
echo ""

if [ $FAILED_COUNT -gt 0 ]; then
    echo -e "${RED}Images échouées:${NC}"
    for image in "${FAILED_IMAGES[@]}"; do
        echo -e "  ${RED}✗${NC} $image"
    done
    echo ""
    exit 1
else
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ TOUTES LES IMAGES ONT ÉTÉ POUSSÉES AVEC SUCCÈS !       ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Afficher les URLs des images
    echo -e "${YELLOW}Images disponibles sur le registry:${NC}"
    echo ""
    if [ "$REGISTRY_TYPE" == "Docker Hub" ]; then
        echo -e "${CYAN}https://hub.docker.com/r/${DOCKER_REGISTRY}${NC}"
    fi
    echo ""
    
    # Liste des images
    for service in "api-gateway" "auth-service" "certification-badge-service" "config-server" \
                   "course-service" "eureka-server" "event-service" "exam-service" \
                   "learning-service" "offers-service" "planning-service" \
                   "skill-evidence-service" "sponsor-service" "training-service" "angular-app"; do
        echo -e "  ${GREEN}✓${NC} ${DOCKER_REGISTRY}/${service}:${VERSION}"
    done
    echo ""
fi

exit 0
