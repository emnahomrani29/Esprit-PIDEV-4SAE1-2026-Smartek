#!/bin/bash
CRUMB=$(curl -s -u wafa:WAFAch12# http://localhost:8080/crumbIssuer/api/json | python3 -c "import sys,json; print(json.load(sys.stdin)['crumb'])")
echo "Crumb: $CRUMB"

echo "Creating learning-service..."
curl -s -w "\nHTTP:%{http_code}\n" -u wafa:WAFAch12# \
  -H "Jenkins-Crumb: $CRUMB" \
  -H "Content-Type: application/xml" \
  -X POST "http://localhost:8080/createItem?name=learning-service" \
  --data-binary @/tmp/learning-job.xml

echo "Creating skill-evidence-service..."
curl -s -w "\nHTTP:%{http_code}\n" -u wafa:WAFAch12# \
  -H "Jenkins-Crumb: $CRUMB" \
  -H "Content-Type: application/xml" \
  -X POST "http://localhost:8080/createItem?name=skill-evidence-service" \
  --data-binary @/tmp/skill-evidence-job.xml
