#!/bin/bash
# Installe les plugins manquants dans Jenkins

JENKINS="http://localhost:8080"
COOKIE_JAR="/tmp/jenkins-cookies.txt"

# Crumb + cookie
CRUMB=$(curl -s -c "$COOKIE_JAR" "$JENKINS/crumbIssuer/api/json" | grep -o '"crumb":"[^"]*"' | cut -d'"' -f4)
echo "Crumb: $CRUMB"

# Installer ws-cleanup
curl -s -b "$COOKIE_JAR" \
  -X POST "$JENKINS/pluginManager/installNecessaryPlugins" \
  -H "Jenkins-Crumb: $CRUMB" \
  -H "Content-Type: application/xml" \
  -d '<jenkins><install plugin="ws-cleanup@latest"/></jenkins>'

echo ""
echo "Plugin ws-cleanup installation requested"
echo "Attente 30s..."
sleep 30

# Vérifier
ls /var/jenkins_home/plugins/ | grep ws-cleanup && echo "✅ ws-cleanup installé" || echo "⏳ Pas encore visible"
