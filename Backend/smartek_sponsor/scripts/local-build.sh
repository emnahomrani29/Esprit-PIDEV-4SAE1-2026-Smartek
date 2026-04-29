#!/bin/bash

###############################################################################
# Local Build Script for Smartek Sponsor Service
# This script builds and tests the application locally
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Smartek Sponsor - Local Build${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

# Navigate to project directory
cd "$(dirname "$0")/.."

echo -e "\n${YELLOW}Step 1: Cleaning previous builds...${NC}"
mvn clean

echo -e "\n${YELLOW}Step 2: Compiling source code...${NC}"
mvn compile

echo -e "\n${YELLOW}Step 3: Running unit tests...${NC}"
mvn test

echo -e "\n${YELLOW}Step 4: Generating code coverage report...${NC}"
mvn jacoco:report

echo -e "\n${YELLOW}Step 5: Packaging application...${NC}"
mvn package -DskipTests

echo -e "\n${YELLOW}Step 6: Building Docker image...${NC}"
docker build -t smartek-sponsor:local .

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Build completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${BLUE}Artifacts:${NC}"
echo "JAR: target/smartek-sponsor-0.0.1-SNAPSHOT.jar"
echo "Docker Image: smartek-sponsor:local"

echo -e "\n${BLUE}Coverage Report:${NC}"
echo "target/site/jacoco/index.html"

echo -e "\n${YELLOW}To run the application locally:${NC}"
echo "docker run -p 8080:8080 smartek-sponsor:local"

echo -e "\n${YELLOW}To run with docker-compose:${NC}"
echo "docker-compose up -d"
