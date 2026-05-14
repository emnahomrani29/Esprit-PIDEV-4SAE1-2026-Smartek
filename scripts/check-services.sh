#!/bin/bash
# scripts/check-services.sh

SERVICES=(
  "Eureka|http://localhost:8761/actuator/health"
  "Config Server|http://localhost:8888/actuator/health"
  "API Gateway|http://localhost:8080/actuator/health"
  "Auth Service|http://localhost:8081/actuator/health"
  "Event Service|http://localhost:8082/actuator/health"
  "Planning Service|http://localhost:8083/actuator/health"
  "Training Service|http://localhost:8084/actuator/health"
  "Offers Service|http://localhost:8085/actuator/health"
  "Course Service|http://localhost:8086/actuator/health"
  "Exam Service|http://localhost:8087/actuator/health"
  "Skill Evidence|http://localhost:8091/actuator/health"
  "Learning|http://localhost:8092/actuator/health"
  "Sponsor Service|http://localhost:8093/actuator/health"
  "Certif/Badge|http://localhost:8094/actuator/health"
  "Frontend|http://localhost:4200"
  "Prometheus|http://localhost:9090/-/ready"
  "Grafana|http://localhost:3000/api/health"
)

ALL_OK=true

echo "=== Smartek Service Health Check ==="
for entry in "${SERVICES[@]}"; do
  name="${entry%%|*}"
  url="${entry##*|}"
  if curl -sf "$url" > /dev/null 2>&1; then
    echo "  [OK]   $name"
  else
    echo "  [DOWN] $name ($url)"
    ALL_OK=false
  fi
done

echo ""
if $ALL_OK; then
  echo "All services are healthy"
else
  echo "Some services are down"
  exit 1
fi
