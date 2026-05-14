#!/bin/bash

# Script d'arrêt de l'environnement DevOps Smartek
# Auteur: DevOps Team
# Date: 2026-05-03

echo "🛑 Arrêt de l'environnement DevOps Smartek..."
echo "=============================================="
echo ""

# Couleurs
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Arrêter les conteneurs Docker
echo -e "${YELLOW}🐳 Arrêt des conteneurs Docker...${NC}"
docker stop jenkins sonarqube sonarqube-db 2>/dev/null
echo -e "${RED}✅ Conteneurs Docker arrêtés${NC}"
echo ""

# 2. Note sur Kubernetes
echo -e "${YELLOW}☸️  Note: Kubernetes continue de fonctionner${NC}"
echo "   Les pods Kubernetes restent actifs."
echo "   Pour les arrêter: kubectl delete namespace monitoring"
echo ""

echo "=============================================="
echo -e "${RED}✅ ENVIRONNEMENT DEVOPS ARRÊTÉ !${NC}"
echo "=============================================="
echo ""
echo "💡 Pour redémarrer: ./scripts/start-devops.sh"
echo ""
