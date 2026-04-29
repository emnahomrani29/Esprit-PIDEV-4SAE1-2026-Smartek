#!/bin/bash

###############################################################################
# Monitoring Setup Script - Prometheus & Grafana
# This script sets up monitoring for smartek-sponsor service
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

NAMESPACE="smartek-production"
MONITORING_NAMESPACE="monitoring"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Setting up Monitoring${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Prometheus Operator is installed
if ! kubectl get crd prometheuses.monitoring.coreos.com &> /dev/null; then
    echo -e "${YELLOW}Prometheus Operator not found. Installing...${NC}"
    
    # Add Prometheus Operator Helm repo
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo update
    
    # Install Prometheus Operator
    helm install prometheus prometheus-community/kube-prometheus-stack \
        --namespace ${MONITORING_NAMESPACE} \
        --create-namespace \
        --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
    
    echo -e "${GREEN}Prometheus Operator installed successfully!${NC}"
else
    echo -e "${GREEN}Prometheus Operator already installed${NC}"
fi

# Apply Prometheus Rules
echo -e "\n${YELLOW}Applying Prometheus Rules...${NC}"
kubectl apply -f ../monitoring/prometheus-rules.yaml

# Create Grafana Dashboard ConfigMap
echo -e "\n${YELLOW}Creating Grafana Dashboard...${NC}"
kubectl create configmap smartek-sponsor-dashboard \
    --from-file=../monitoring/grafana-dashboard.json \
    -n ${MONITORING_NAMESPACE} \
    --dry-run=client -o yaml | kubectl apply -f -

kubectl label configmap smartek-sponsor-dashboard \
    grafana_dashboard=1 \
    -n ${MONITORING_NAMESPACE} \
    --overwrite

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Monitoring setup completed!${NC}"
echo -e "${GREEN}========================================${NC}"

# Get Grafana credentials
echo -e "\n${YELLOW}Grafana Access Information:${NC}"
echo "Username: admin"
echo -n "Password: "
kubectl get secret -n ${MONITORING_NAMESPACE} prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 --decode
echo ""

# Port forward instructions
echo -e "\n${YELLOW}To access Grafana:${NC}"
echo "kubectl port-forward -n ${MONITORING_NAMESPACE} svc/prometheus-grafana 3000:80"
echo "Then open: http://localhost:3000"

echo -e "\n${YELLOW}To access Prometheus:${NC}"
echo "kubectl port-forward -n ${MONITORING_NAMESPACE} svc/prometheus-kube-prometheus-prometheus 9090:9090"
echo "Then open: http://localhost:9090"
