#!/bin/bash
# scripts/deploy.sh
# Demarrage ordonne des services avec verification de sante

set -euo pipefail

COMPOSE_CMD="docker compose"
TIMEOUT=120

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

wait_healthy() {
  local service=$1
  local url=$2
  local elapsed=0
  log "Waiting for $service to be healthy..."
  until curl -sf "$url" > /dev/null 2>&1; do
    sleep 5
    elapsed=$((elapsed + 5))
    if [ $elapsed -ge $TIMEOUT ]; then
      log "ERROR: $service did not become healthy within ${TIMEOUT}s"
      exit 1
    fi
  done
  log "$service is healthy"
}

# 1. Base de donnees
log "Starting MySQL..."
$COMPOSE_CMD up -d mysql
sleep 15

# 2. Infrastructure
log "Starting Eureka Server..."
$COMPOSE_CMD up -d eureka-server
wait_healthy "eureka-server" "http://localhost:8761/actuator/health"

log "Starting Config Server..."
$COMPOSE_CMD up -d config-server
wait_healthy "config-server" "http://localhost:8888/actuator/health"

log "Starting API Gateway..."
$COMPOSE_CMD up -d api-gateway
wait_healthy "api-gateway" "http://localhost:8080/actuator/health"

# 3. Auth Service
log "Starting Auth Service..."
$COMPOSE_CMD up -d auth-service
sleep 15

# 4. Microservices
log "Starting microservices..."
$COMPOSE_CMD up -d event-service planning-service training-service offers-service course-service exam-service skill-evidence-service learning sponsor-service certification-badge-service
sleep 30

# 5. Frontend
log "Starting frontend..."
$COMPOSE_CMD up -d frontend

# 6. Monitoring
log "Starting monitoring stack..."
$COMPOSE_CMD up -d prometheus grafana

log "All services started successfully"
$COMPOSE_CMD ps
