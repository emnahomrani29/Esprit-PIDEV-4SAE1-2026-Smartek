#!/bin/bash

# Script de vérification du statut de l'environnement DevOps Smartek
# Auteur: DevOps Team
# Date: 2026-05-03

echo "📊 Statut de l'environnement DevOps Smartek"
echo "=============================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Docker
echo "🐳 Docker Services:"
echo "-------------------"
if systemctl is-active --quiet docker; then
    echo -e "${GREEN}✅ Docker daemon: Running${NC}"
    echo ""
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | head -1
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "jenkins|sonarqube" || echo -e "${RED}❌ Aucun conteneur en cours d'exécution${NC}"
else
    echo -e "${RED}❌ Docker daemon: Stopped${NC}"
fi
echo ""

# 2. Kubernetes
echo "☸️  Kubernetes:"
echo "---------------"
if systemctl is-active --quiet kubelet; then
    echo -e "${GREEN}✅ Kubelet: Running${NC}"
    echo ""
    kubectl get nodes 2>/dev/null || echo -e "${RED}❌ Impossible de se connecter au cluster${NC}"
else
    echo -e "${RED}❌ Kubelet: Stopped${NC}"
fi
echo ""

# 3. Pods Monitoring
echo "📦 Pods Monitoring (Kubernetes):"
echo "--------------------------------"
kubectl get pods -n monitoring 2>/dev/null || echo -e "${YELLOW}⚠️  Namespace monitoring n'existe pas${NC}"
echo ""

# 4. URLs
echo "🌐 URLs d'Accès:"
echo "----------------"
echo "   Jenkins:     http://localhost:8080"
echo "   SonarQube:   http://localhost:9000"
echo "   Prometheus:  http://localhost:30090"
echo "   Grafana:     http://localhost:30300"
echo ""

# 5. Résumé
echo "=============================================="
echo "📋 Résumé:"
echo "=============================================="

# Compter les services actifs
active_count=0
total_count=5

systemctl is-active --quiet docker && active_count=$((active_count + 1))
docker ps | grep -q jenkins && active_count=$((active_count + 1))
docker ps | grep -q sonarqube && active_count=$((active_count + 1))
systemctl is-active --quiet kubelet && active_count=$((active_count + 1))
kubectl get pods -n monitoring 2>/dev/null | grep -q "Running" && active_count=$((active_count + 1))

if [ $active_count -eq $total_count ]; then
    echo -e "${GREEN}✅ Tous les services sont actifs ($active_count/$total_count)${NC}"
elif [ $active_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Certains services sont actifs ($active_count/$total_count)${NC}"
else
    echo -e "${RED}❌ Aucun service n'est actif ($active_count/$total_count)${NC}"
fi
echo ""
