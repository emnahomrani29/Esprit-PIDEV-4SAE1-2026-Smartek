#!/bin/bash

###############################################################################
# Smartek Sponsor Service - Deployment Script
# This script deploys the smartek-sponsor service to Kubernetes
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="smartek-production"
APP_NAME="smartek-sponsor"
K8S_DIR="./k8s"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Smartek Sponsor Service Deployment${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}Error: kubectl is not installed${NC}"
    exit 1
fi

# Check if connected to cluster
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}Error: Not connected to Kubernetes cluster${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Creating namespace...${NC}"
kubectl apply -f ${K8S_DIR}/namespace.yaml

echo -e "${YELLOW}Step 2: Creating ConfigMap...${NC}"
kubectl apply -f ${K8S_DIR}/configmap.yaml

echo -e "${YELLOW}Step 3: Creating Secret...${NC}"
echo -e "${RED}WARNING: Update secrets before production deployment!${NC}"
kubectl apply -f ${K8S_DIR}/secret.yaml

echo -e "${YELLOW}Step 4: Creating Service...${NC}"
kubectl apply -f ${K8S_DIR}/service.yaml

echo -e "${YELLOW}Step 5: Creating Deployment...${NC}"
kubectl apply -f ${K8S_DIR}/deployment.yaml

echo -e "${YELLOW}Step 6: Creating HPA...${NC}"
kubectl apply -f ${K8S_DIR}/hpa.yaml

echo -e "${YELLOW}Step 7: Creating Ingress...${NC}"
kubectl apply -f ${K8S_DIR}/ingress.yaml

echo -e "${YELLOW}Step 8: Creating ServiceMonitor for Prometheus...${NC}"
kubectl apply -f ${K8S_DIR}/servicemonitor.yaml

echo -e "${YELLOW}Step 9: Waiting for deployment to be ready...${NC}"
kubectl rollout status deployment/${APP_NAME}-deployment -n ${NAMESPACE} --timeout=5m

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

# Display deployment info
echo -e "\n${YELLOW}Deployment Information:${NC}"
kubectl get pods -n ${NAMESPACE} -l app=${APP_NAME}
kubectl get svc -n ${NAMESPACE} -l app=${APP_NAME}
kubectl get ingress -n ${NAMESPACE}

echo -e "\n${YELLOW}To view logs:${NC}"
echo "kubectl logs -f -n ${NAMESPACE} -l app=${APP_NAME}"

echo -e "\n${YELLOW}To check health:${NC}"
echo "kubectl exec -n ${NAMESPACE} -it \$(kubectl get pod -n ${NAMESPACE} -l app=${APP_NAME} -o jsonpath='{.items[0].metadata.name}') -- wget -qO- http://localhost:8080/actuator/health"
