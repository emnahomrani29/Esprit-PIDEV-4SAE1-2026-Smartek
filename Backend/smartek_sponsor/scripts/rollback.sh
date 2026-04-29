#!/bin/bash

###############################################################################
# Smartek Sponsor Service - Rollback Script
# This script rolls back the deployment to the previous version
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="smartek-production"
DEPLOYMENT="smartek-sponsor-deployment"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Rolling back ${DEPLOYMENT}...${NC}"
echo -e "${YELLOW}========================================${NC}"

# Show rollout history
echo -e "\n${YELLOW}Rollout History:${NC}"
kubectl rollout history deployment/${DEPLOYMENT} -n ${NAMESPACE}

# Perform rollback
echo -e "\n${YELLOW}Performing rollback...${NC}"
kubectl rollout undo deployment/${DEPLOYMENT} -n ${NAMESPACE}

# Wait for rollback to complete
echo -e "\n${YELLOW}Waiting for rollback to complete...${NC}"
kubectl rollout status deployment/${DEPLOYMENT} -n ${NAMESPACE} --timeout=5m

echo -e "\n${GREEN}Rollback completed successfully!${NC}"

# Display current status
echo -e "\n${YELLOW}Current Status:${NC}"
kubectl get pods -n ${NAMESPACE} -l app=smartek-sponsor
