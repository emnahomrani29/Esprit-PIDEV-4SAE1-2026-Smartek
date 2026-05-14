#!/bin/bash
# scripts/test-infrastructure.sh
# Tests automatisés de l'infrastructure

set -euo pipefail

# ─────────────────────────────────────────
# COULEURS POUR LES LOGS
# ─────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $*"; }
log_success() { echo -e "${GREEN}[✓]${NC} $*"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[✗]${NC} $*"; }

# ─────────────────────────────────────────
# COMPTEURS
# ─────────────────────────────────────────
TESTS_PASSED=0
TESTS_FAILED=0

# ─────────────────────────────────────────
# TEST D'UN ENDPOINT
# ─────────────────────────────────────────
test_endpoint() {
  local name=$1
  local url=$2
  local expected_code=${3:-200}
  
  log_info "Test: $name"
  
  local response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
  
  if [ "$response_code" == "$expected_code" ]; then
    log_success "$name - OK (HTTP $response_code)"
    ((TESTS_PASSED++))
    return 0
  else
    log_error "$name - FAILED (HTTP $response_code, attendu $expected_code)"
    ((TESTS_FAILED++))
    return 1
  fi
}

# ─────────────────────────────────────────
# TEST D'UN CONTENEUR
# ─────────────────────────────────────────
test_container() {
  local name=$1
  local container=$2
  
  log_info "Test: $name"
  
  if docker ps | grep -q "$container"; then
    log_success "$name - Container running"
    ((TESTS_PASSED++))
    return 0
  else
    log_error "$name - Container not running"
    ((TESTS_FAILED++))
    return 1
  fi
}

# ─────────────────────────────────────────
# TEST DE LA BASE DE DONNÉES
# ─────────────────────────────────────────
test_database() {
  log_info "Test: MySQL Database"
  
  if docker exec smartek-mysql mysqladmin ping -h localhost -u root -proot &>/dev/null; then
    log_success "MySQL Database - OK"
    ((TESTS_PASSED++))
  else
    log_error "MySQL Database - FAILED"
    ((TESTS_FAILED++))
  fi
}

# ─────────────────────────────────────────
# TESTS DES CONTENEURS
# ─────────────────────────────────────────
test_containers() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  TESTS DES CONTENEURS"
  echo "════════════════════════════════════════"
  echo ""
  
  test_container "MySQL" "smartek-mysql"
  test_container "Eureka Server" "smartek-eureka"
  test_container "API Gateway" "smartek-gateway"
  test_container "Auth Service" "smartek-auth"
  test_container "Frontend" "smartek-frontend"
  test_container "Prometheus" "smartek-prometheus"
  test_container "Grafana" "smartek-grafana"
}

# ─────────────────────────────────────────
# TESTS DES ENDPOINTS
# ─────────────────────────────────────────
test_endpoints() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  TESTS DES ENDPOINTS"
  echo "════════════════════════════════════════"
  echo ""
  
  test_endpoint "Eureka Server" "http://localhost:8761" 200
  test_endpoint "API Gateway Health" "http://localhost:8080/actuator/health" 200
  test_endpoint "Auth Service Health" "http://localhost:8081/actuator/health" 200
  test_endpoint "Frontend" "http://localhost:4200" 200
  test_endpoint "Prometheus" "http://localhost:9090/-/healthy" 200
  test_endpoint "Grafana" "http://localhost:3000/api/health" 200
  test_endpoint "Jaeger" "http://localhost:16686" 200
  test_endpoint "Alertmanager" "http://localhost:9093/-/healthy" 200
}

# ─────────────────────────────────────────
# TESTS DE LA BASE DE DONNÉES
# ─────────────────────────────────────────
test_databases() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  TESTS DES BASES DE DONNÉES"
  echo "════════════════════════════════════════"
  echo ""
  
  test_database
}

# ─────────────────────────────────────────
# TESTS DES RESSOURCES
# ─────────────────────────────────────────
test_resources() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  TESTS DES RESSOURCES"
  echo "════════════════════════════════════════"
  echo ""
  
  log_info "Utilisation des ressources Docker:"
  docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
}

# ─────────────────────────────────────────
# RÉSUMÉ DES TESTS
# ─────────────────────────────────────────
show_summary() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  RÉSUMÉ DES TESTS"
  echo "════════════════════════════════════════"
  echo ""
  
  local total=$((TESTS_PASSED + TESTS_FAILED))
  local success_rate=0
  
  if [ $total -gt 0 ]; then
    success_rate=$((TESTS_PASSED * 100 / total))
  fi
  
  echo "Total de tests: $total"
  echo -e "${GREEN}Tests réussis: $TESTS_PASSED${NC}"
  echo -e "${RED}Tests échoués: $TESTS_FAILED${NC}"
  echo "Taux de réussite: $success_rate%"
  echo ""
  
  if [ $TESTS_FAILED -eq 0 ]; then
    log_success "Tous les tests sont passés!"
    return 0
  else
    log_error "Certains tests ont échoué"
    return 1
  fi
}

# ─────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────
main() {
  echo ""
  echo "════════════════════════════════════════"
  echo "  SMARTEK - TESTS D'INFRASTRUCTURE"
  echo "════════════════════════════════════════"
  
  test_containers
  sleep 2
  test_endpoints
  test_databases
  test_resources
  show_summary
}

main "$@"
