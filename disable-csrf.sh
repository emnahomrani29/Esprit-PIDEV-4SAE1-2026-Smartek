#!/bin/bash
# Disable CSRF via Groovy script
CRUMB=$(curl -s -u wafa:WAFAch12# http://localhost:8080/crumbIssuer/api/json | python3 -c "import sys,json; print(json.load(sys.stdin)['crumb'])")

curl -s -u wafa:WAFAch12# \
  -H "Jenkins-Crumb: $CRUMB" \
  -d 'script=import+jenkins.model.Jenkins%3BJenkins.instance.setCrumbIssuer(null)' \
  http://localhost:8080/scriptText

echo "CSRF disabled"
