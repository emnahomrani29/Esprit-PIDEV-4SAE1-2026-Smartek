#!/bin/bash
# scripts/docker-build-all.sh

set -euo pipefail

TAG=${1:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}

SERVICES=(
  "Backend/eureka-server:smartek/eureka-server"
  "Backend/config-server:smartek/config-server"
  "Backend/api-gateway:smartek/api-gateway"
  "Backend/auth-service:smartek/auth-service"
  "Backend/event-service:smartek/event-service"
  "Backend/planning-service:smartek/planning-service"
  "Backend/training-service:smartek/training-service"
  "Backend/offers-service:smartek/offers-service"
  "Backend/course-service:smartek/course-service"
  "Backend/exam-service:smartek/exam-service"
  "Backend/skiil-evidence-service:smartek/skill-evidence-service"
  "Backend/learning:smartek/learning-service"
  "Backend/smartek_sponsor:smartek/sponsor-service"
  "Backend/certification-badge-service:smartek/certification-badge-service"
  "Frontend/angular-app:smartek/frontend"
)

echo "Building all images with tag: $TAG"

for entry in "${SERVICES[@]}"; do
  context="${entry%%:*}"
  name="${entry##*:}"
  echo "Building $name..."
  docker build -t "$name:$TAG" -t "$name:latest" "$context"
  echo "  $name built"
done

echo ""
echo "All images built with tag: $TAG"
docker images | grep smartek
