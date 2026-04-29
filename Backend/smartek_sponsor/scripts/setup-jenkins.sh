#!/bin/bash

###############################################################################
# Jenkins Setup Script for Smartek Sponsor Service
# This script helps configure Jenkins for the CI/CD pipeline
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Jenkins Setup for Smartek Sponsor${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${BLUE}Required Jenkins Plugins:${NC}"
echo "1. Pipeline"
echo "2. Git"
echo "3. Docker Pipeline"
echo "4. Kubernetes CLI"
echo "5. SonarQube Scanner"
echo "6. JaCoCo"
echo "7. Email Extension"
echo "8. Credentials Binding"
echo "9. Nexus Artifact Uploader"

echo -e "\n${BLUE}Required Credentials in Jenkins:${NC}"
echo "1. nexus-credentials (Username/Password)"
echo "2. nexus-docker-credentials (Username/Password)"
echo "3. sonarqube-token (Secret Text)"
echo "4. kubeconfig-credentials (Secret File)"

echo -e "\n${BLUE}Required Tools Configuration:${NC}"
echo "1. Maven-3.9.6"
echo "2. JDK-17"

echo -e "\n${YELLOW}To create Jenkins Pipeline:${NC}"
echo "1. Go to Jenkins Dashboard"
echo "2. Click 'New Item'"
echo "3. Enter name: 'smartek-sponsor-pipeline'"
echo "4. Select 'Pipeline'"
echo "5. In Pipeline section:"
echo "   - Definition: Pipeline script from SCM"
echo "   - SCM: Git"
echo "   - Repository URL: <your-git-repo>"
echo "   - Script Path: Backend/smartek_sponsor/Jenkinsfile"

echo -e "\n${YELLOW}Environment Variables to Configure:${NC}"
echo "Update the following in Jenkinsfile:"
echo "- DOCKER_REGISTRY"
echo "- NEXUS_URL"
echo "- SONAR_HOST_URL"
echo "- K8S_NAMESPACE"

echo -e "\n${GREEN}Setup instructions completed!${NC}"
