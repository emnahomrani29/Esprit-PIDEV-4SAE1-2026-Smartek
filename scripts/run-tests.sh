#!/bin/bash
# scripts/run-tests.sh

set -euo pipefail

BACKEND_SERVICES=(
  auth-service
  event-service
  planning-service
  training-service
  offers-service
  course-service
  exam-service
  certification-badge-service
)
FAILED=0

echo "=== Running Backend Tests ==="
for service in "${BACKEND_SERVICES[@]}"; do
  echo "Testing $service..."
  if mvn clean test -f "Backend/$service/pom.xml" -q 2>/dev/null; then
    echo "  [OK] $service"
  else
    echo "  [FAIL] $service"
    FAILED=$((FAILED + 1))
  fi
done

echo ""
echo "=== Running Frontend Tests ==="
if (cd Frontend/angular-app && npm ci --silent && npm run test -- --watch=false --browsers=ChromeHeadless 2>/dev/null); then
  echo "  [OK] Frontend"
else
  echo "  [FAIL] Frontend"
  FAILED=$((FAILED + 1))
fi

echo ""
echo "=== Test Summary ==="
if [ $FAILED -eq 0 ]; then
  echo "All tests passed"
else
  echo "$FAILED test suite(s) FAILED"
  exit 1
fi
