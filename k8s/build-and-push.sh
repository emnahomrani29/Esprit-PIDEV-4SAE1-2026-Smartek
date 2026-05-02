#!/bin/bash
# SMARTEK Docker Image Build and Push Script

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configuration
REGISTRY="${1:-your-docker-registry}"
TAG="${2:-latest}"
SKIP_LOGIN="${SKIP_LOGIN:-false}"

if [ "$REGISTRY" = "help" ] || [ "$REGISTRY" = "-h" ] || [ "$REGISTRY" = "--help" ]; then
    echo "Usage: $0 <registry> [tag] [--skip-login]"
    echo ""
    echo "Examples:"
    echo "  $0 myusername latest              # Build and push to Docker Hub"
    echo "  $0 registry.example.com:5000 v1.0 # Build and push to private registry"
    echo "  SKIP_LOGIN=true $0 myuser latest  # Skip docker login"
    exit 0
fi

echo -e "${YELLOW}================================================${NC}"
echo -e "${YELLOW}SMARTEK Docker Image Build & Push Script${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""
echo "Registry: ${REGISTRY}"
echo "Tag: ${TAG}"
echo ""

# Login to registry if not skipped
if [ "$SKIP_LOGIN" != "true" ]; then
    echo -e "${YELLOW}Logging in to Docker registry...${NC}"
    if [[ "$REGISTRY" == *"docker.io"* ]] || [[ "$REGISTRY" != *"."* ]]; then
        docker login docker.io
    else
        docker login "${REGISTRY}"
    fi
    echo ""
fi

# Check if we're in the right directory
if [ ! -d "Backend" ] || [ ! -d "Frontend" ]; then
    echo -e "${RED}Error: Must run from project root directory${NC}"
    exit 1
fi

# Array of services to build
declare -a SERVICES=(
    "Backend/eureka-server:eureka-server"
    "Backend/auth-service:auth-service"
    "Backend/event-service:event-service"
    "Backend/planning-service:planning-service"
    "Backend/training-service:training-service"
    "Backend/offers-service:offers-service"
    "Backend/exam-service:exam-service"
    "Backend/course-service:course-service"
    "Backend/learning:learning-service"
    "Backend/skiil-evidence-service:skill-evidence-service"
    "Backend/config-server:config-server"
    "Backend/api-gateway:api-gateway"
    "Frontend/angular-app:frontend"
)

# Counter
CURRENT=1
TOTAL=${#SERVICES[@]}

# Build and push each service
for SERVICE in "${SERVICES[@]}"; do
    IFS=':' read -r CONTEXT IMAGE_NAME <<< "$SERVICE"
    
    FULL_IMAGE="${REGISTRY}/smartek/${IMAGE_NAME}:${TAG}"
    
    echo -e "${GREEN}[$CURRENT/$TOTAL] Building and pushing ${IMAGE_NAME}...${NC}"
    echo "  Path: ${CONTEXT}"
    echo "  Image: ${FULL_IMAGE}"
    
    # Build image
    if docker build -t "${FULL_IMAGE}" "${CONTEXT}"; then
        echo -e "${GREEN}  ✓ Build successful${NC}"
    else
        echo -e "${RED}  ✗ Build failed${NC}"
        exit 1
    fi
    
    # Push image
    if docker push "${FULL_IMAGE}"; then
        echo -e "${GREEN}  ✓ Push successful${NC}"
    else
        echo -e "${RED}  ✗ Push failed${NC}"
        exit 1
    fi
    
    echo ""
    ((CURRENT++))
done

echo -e "${YELLOW}================================================${NC}"
echo -e "${GREEN}✓ All images built and pushed successfully!${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""
echo "Next steps:"
echo "1. Update Kubernetes YAML files with your registry:"
echo "   sed -i 's|your-docker-registry|${REGISTRY}|g' k8s/**/*.yaml"
echo ""
echo "2. Deploy to Kubernetes:"
echo "   cd k8s && ./deploy.sh"
echo ""
