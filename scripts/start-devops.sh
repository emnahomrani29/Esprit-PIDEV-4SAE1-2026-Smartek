#!/bin/bash

# Script de démarrage de l'environnement DevOps Smartek
# Auteur: DevOps Team
# Date: 2026-05-03

echo "🚀 Démarrage de l'environnement DevOps Smartek..."
echo "=================================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Vérifier Docker
echo -e "${BLUE}📦 Vérification de Docker...${NC}"
if ! systemctl is-active --quiet docker; then
    echo -e "${YELLOW}⚠️  Docker n'est pas démarré. Démarrage...${NC}"
    sudo systemctl start docker
    sleep 3
fi
echo -e "${GREEN}✅ Docker est actif${NC}"
echo ""

# 2. Démarrer les conteneurs Docker
echo -e "${BLUE}🐳 Démarrage des conteneurs Docker...${NC}"

# Vérifier si les conteneurs existent et les démarrer
if docker ps -a | grep -q jenkins; then
    if ! docker ps | grep -q jenkins; then
        echo "   Démarrage de Jenkins..."
        docker start jenkins
    else
        echo "   Jenkins est déjà en cours d'exécution"
    fi
else
    echo -e "${YELLOW}   ⚠️  Jenkins n'existe pas. Création...${NC}"
    docker volume create jenkins-data
    docker run -d \
      --name jenkins \
      --restart=always \
      -p 8080:8080 \
      -p 50000:50000 \
      -v jenkins-data:/var/jenkins_home \
      -v /var/run/docker.sock:/var/run/docker.sock \
      jenkins/jenkins:lts
fi

if docker ps -a | grep -q sonarqube-db; then
    if ! docker ps | grep -q sonarqube-db; then
        echo "   Démarrage de PostgreSQL (SonarQube DB)..."
        docker start sonarqube-db
    else
        echo "   PostgreSQL est déjà en cours d'exécution"
    fi
else
    echo -e "${YELLOW}   ⚠️  PostgreSQL n'existe pas. Création...${NC}"
    docker network create sonarnet 2>/dev/null || true
    docker run -d \
      --name sonarqube-db \
      --network sonarnet \
      --restart=always \
      -e POSTGRES_USER=sonar \
      -e POSTGRES_PASSWORD=sonar \
      -e POSTGRES_DB=sonarqube \
      -v sonarqube-db:/var/lib/postgresql/data \
      postgres:15-alpine
fi

sleep 5

if docker ps -a | grep -q sonarqube; then
    if ! docker ps | grep -q sonarqube; then
        echo "   Démarrage de SonarQube..."
        docker start sonarqube
    else
        echo "   SonarQube est déjà en cours d'exécution"
    fi
else
    echo -e "${YELLOW}   ⚠️  SonarQube n'existe pas. Création...${NC}"
    sudo sysctl -w vm.max_map_count=524288 2>/dev/null
    sudo sysctl -w fs.file-max=131072 2>/dev/null
    docker run -d \
      --name sonarqube \
      --network sonarnet \
      --restart=always \
      -p 9000:9000 \
      -e SONAR_JDBC_URL=jdbc:postgresql://sonarqube-db:5432/sonarqube \
      -e SONAR_JDBC_USERNAME=sonar \
      -e SONAR_JDBC_PASSWORD=sonar \
      -v sonarqube-data:/opt/sonarqube/data \
      -v sonarqube-logs:/opt/sonarqube/logs \
      -v sonarqube-extensions:/opt/sonarqube/extensions \
      sonarqube:lts-community
fi

echo -e "${GREEN}✅ Conteneurs Docker démarrés${NC}"
echo ""

# 3. Vérifier Kubernetes
echo -e "${BLUE}☸️  Vérification de Kubernetes...${NC}"
if ! systemctl is-active --quiet kubelet; then
    echo -e "${YELLOW}⚠️  Kubelet n'est pas démarré. Démarrage...${NC}"
    sudo systemctl start kubelet
    sleep 5
fi

# Attendre que le node soit Ready
echo "   Attente que le node Kubernetes soit Ready..."
timeout=60
elapsed=0
while [ $elapsed -lt $timeout ]; do
    if kubectl get nodes 2>/dev/null | grep -q "Ready"; then
        echo -e "${GREEN}✅ Kubernetes est actif${NC}"
        break
    fi
    sleep 2
    elapsed=$((elapsed + 2))
done

if [ $elapsed -ge $timeout ]; then
    echo -e "${YELLOW}⚠️  Kubernetes prend plus de temps que prévu...${NC}"
fi
echo ""

# 4. Vérifier les pods Kubernetes
echo -e "${BLUE}📊 Vérification des pods Kubernetes...${NC}"
kubectl get pods -n monitoring 2>/dev/null || echo "   Namespace monitoring n'existe pas encore"
echo ""

# 5. Afficher le statut
echo "=================================================="
echo -e "${GREEN}✅ ENVIRONNEMENT DEVOPS DÉMARRÉ !${NC}"
echo "=================================================="
echo ""
echo "📊 Statut des Services:"
echo ""
echo "🐳 Docker Services:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "jenkins|sonarqube"
echo ""
echo "☸️  Kubernetes:"
kubectl get nodes 2>/dev/null
echo ""
echo "📦 Pods Monitoring:"
kubectl get pods -n monitoring 2>/dev/null
echo ""
echo "=================================================="
echo "🌐 URLs d'Accès:"
echo "=================================================="
echo "   Jenkins:     http://localhost:8080"
echo "   SonarQube:   http://localhost:9000"
echo "   Prometheus:  http://localhost:30090"
echo "   Grafana:     http://localhost:30300"
echo "=================================================="
echo ""
echo "💡 Astuce: Attendez 1-2 minutes que tous les services soient complètement démarrés"
echo ""
