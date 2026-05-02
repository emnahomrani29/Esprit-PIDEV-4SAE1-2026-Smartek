#!/bin/bash
# SMARTEK Kubernetes Deployment Script
# This script deploys all components to Kubernetes in the correct order

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="smartek"
CONTEXT=$(kubectl config current-context)
REGISTRY="${DOCKER_REGISTRY:-your-docker-registry}"

echo -e "${YELLOW}================================================${NC}"
echo -e "${YELLOW}SMARTEK Kubernetes Deployment Script${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""
echo "Kubernetes Context: ${CONTEXT}"
echo "Namespace: ${NAMESPACE}"
echo "Docker Registry: ${REGISTRY}"
echo ""

# Function to check if resource exists
resource_exists() {
    kubectl get "$1" "$2" -n "$NAMESPACE" &> /dev/null
}

# Function to wait for deployment
wait_for_deployment() {
    local deployment=$1
    echo -e "${YELLOW}Waiting for deployment ${deployment} to be ready...${NC}"
    kubectl wait --for=condition=available --timeout=300s deployment "${deployment}" -n "${NAMESPACE}" || true
    kubectl rollout status deployment/"${deployment}" -n "${NAMESPACE}" --timeout=300s
}

# Function to wait for statefulset
wait_for_statefulset() {
    local statefulset=$1
    echo -e "${YELLOW}Waiting for statefulset ${statefulset} to be ready...${NC}"
    kubectl wait --for=condition=ready pod -l app="${statefulset}" -n "${NAMESPACE}" --timeout=300s || true
}

# Step 1: Create Namespace
echo -e "${GREEN}[1/6] Creating namespace...${NC}"
if ! resource_exists namespace "${NAMESPACE}"; then
    kubectl apply -f 01-namespace/namespace.yaml
    echo -e "${GREEN}✓ Namespace created${NC}"
else
    echo -e "${GREEN}✓ Namespace already exists${NC}"
fi
echo ""

# Step 2: Apply Secrets and ConfigMaps
echo -e "${GREEN}[2/6] Creating secrets and configmaps...${NC}"
kubectl apply -f 02-secrets-configmaps/configmap.yaml
kubectl apply -f 02-secrets-configmaps/secrets.yaml
echo -e "${GREEN}✓ Secrets and ConfigMaps created${NC}"
echo ""

# Step 3: Deploy Database
echo -e "${GREEN}[3/6] Deploying MySQL database...${NC}"
kubectl apply -f 03-database/mysql-statefulset.yaml
wait_for_statefulset mysql
echo -e "${GREEN}✓ MySQL deployed and ready${NC}"
echo ""

# Step 4: Deploy Microservices
echo -e "${GREEN}[4/6] Deploying microservices...${NC}"

# Deploy Eureka first (service discovery)
echo "  - Deploying Eureka Server..."
kubectl apply -f 04-microservices/eureka-server.yaml
wait_for_deployment eureka-server

# Deploy other services
echo "  - Deploying Auth Service..."
kubectl apply -f 04-microservices/auth-service.yaml

echo "  - Deploying Event, Planning, Training, Offers services..."
kubectl apply -f 04-microservices/services-part1.yaml

echo "  - Deploying Exam, Course, Learning, Skill Evidence, Config services..."
kubectl apply -f 04-microservices/services-part2.yaml

echo "  - Deploying API Gateway and Frontend..."
kubectl apply -f 04-microservices/gateway-frontend.yaml

# Wait for all deployments
echo "  - Waiting for all deployments to be ready..."
kubectl wait --for=condition=available --timeout=600s deployment --all -n "${NAMESPACE}" || true

echo -e "${GREEN}✓ All microservices deployed${NC}"
echo ""

# Step 5: Deploy Ingress
echo -e "${GREEN}[5/6] Deploying Ingress...${NC}"
kubectl apply -f 05-ingress/ingress.yaml
echo -e "${GREEN}✓ Ingress deployed${NC}"
echo ""

# Step 6: Deploy Monitoring (Optional)
echo -e "${GREEN}[6/6] Deploying monitoring stack...${NC}"
kubectl apply -f 06-monitoring/prometheus-grafana.yaml
wait_for_deployment prometheus
wait_for_deployment grafana
echo -e "${GREEN}✓ Monitoring stack deployed${NC}"
echo ""

# Display summary
echo -e "${YELLOW}================================================${NC}"
echo -e "${YELLOW}Deployment Summary${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""
echo -e "${GREEN}Deployments:${NC}"
kubectl get deployments -n "${NAMESPACE}"
echo ""
echo -e "${GREEN}Services:${NC}"
kubectl get services -n "${NAMESPACE}"
echo ""
echo -e "${GREEN}Pods:${NC}"
kubectl get pods -n "${NAMESPACE}"
echo ""

# Display access information
echo -e "${YELLOW}================================================${NC}"
echo -e "${YELLOW}Access Information${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""
echo "To access services, use port forwarding:"
echo ""
echo "API Gateway:"
echo "  kubectl port-forward svc/api-gateway -n ${NAMESPACE} 8090:8090"
echo "  Access: http://localhost:8090"
echo ""
echo "Eureka Server:"
echo "  kubectl port-forward svc/eureka-server -n ${NAMESPACE} 8761:8761"
echo "  Access: http://localhost:8761"
echo ""
echo "Frontend:"
echo "  kubectl port-forward svc/frontend-service -n ${NAMESPACE} 4200:80"
echo "  Access: http://localhost:4200"
echo ""
echo "Prometheus:"
echo "  kubectl port-forward svc/prometheus -n ${NAMESPACE} 9090:9090"
echo "  Access: http://localhost:9090"
echo ""
echo "Grafana:"
echo "  kubectl port-forward svc/grafana -n ${NAMESPACE} 3000:3000"
echo "  Access: http://localhost:3000 (admin/admin123)"
echo ""

echo -e "${GREEN}✓ Deployment completed successfully!${NC}"
