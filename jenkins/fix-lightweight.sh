#!/bin/bash
# Désactive le lightweight checkout sur tous les jobs
# pour éviter l'erreur SCMFileSystem au démarrage

JENKINS="http://localhost:8080"
COOKIE_JAR="/tmp/jenkins-cookies.txt"

CRUMB=$(curl -s -c "$COOKIE_JAR" "$JENKINS/crumbIssuer/api/json" | grep -o '"crumb":"[^"]*"' | cut -d'"' -f4)
echo "Crumb: $CRUMB"

JOBS=$(ls /var/jenkins_home/jobs/)

for JOB in $JOBS; do
    CONFIG="/var/jenkins_home/jobs/$JOB/config.xml"
    if [ -f "$CONFIG" ]; then
        # Remplacer lightweight true par false
        sed -i 's|<lightweight>true</lightweight>|<lightweight>false</lightweight>|g' "$CONFIG"
        echo "  ✅ Fixed: $JOB"
    fi
done

# Recharger la config Jenkins
curl -s -b "$COOKIE_JAR" \
    -X POST "$JENKINS/reload" \
    -H "Jenkins-Crumb: $CRUMB"

echo ""
echo "Jenkins config reloaded"
