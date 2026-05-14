#!/bin/bash
CRUMB=$(curl -s -c /tmp/c.txt 'http://localhost:8080/crumbIssuer/api/json' | grep -o '"crumb":"[^"]*"' | cut -d'"' -f4)
echo "Crumb: $CRUMB"
curl -s -b /tmp/c.txt -X POST "http://localhost:8080/safeRestart" -H "Jenkins-Crumb: $CRUMB"
echo "Jenkins restart requested - attendre 60s"
