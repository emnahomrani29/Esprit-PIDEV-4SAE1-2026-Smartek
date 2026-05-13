#!/bin/bash
CRUMB=$(curl -s -u wafa:WAFAch12# http://localhost:8080/crumbIssuer/api/json | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['crumbRequestField']+': '+d['crumb'])")
echo "Using: $CRUMB"
curl -s -w "HTTP:%{http_code}" -u wafa:WAFAch12# -H "$CRUMB" -X POST "http://localhost:8080/reload"
