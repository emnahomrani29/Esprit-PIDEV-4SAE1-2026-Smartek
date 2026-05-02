#!/bin/bash
# scripts/logs.sh
# Usage: ./scripts/logs.sh [service] [lines]

SERVICE=${1:-""}
LINES=${2:-100}

if [ -z "$SERVICE" ]; then
  echo "Available services:"
  docker compose ps --services 2>/dev/null || echo "  (docker compose not running)"
  echo ""
  echo "Usage: $0 <service> [lines]"
  echo "       $0 auth-service 200"
  exit 0
fi

echo "=== Logs for $SERVICE (last $LINES lines) ==="
docker compose logs --tail="$LINES" -f "$SERVICE"
