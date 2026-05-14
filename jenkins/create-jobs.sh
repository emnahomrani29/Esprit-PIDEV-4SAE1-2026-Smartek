#!/bin/bash
# ============================================================
# Crée tous les jobs Jenkins Smartek via l'API interne
# Exécuté DANS le conteneur Jenkins
# ============================================================

JENKINS="http://localhost:8080"
REPO="https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git"
CRED="github-token"
BRANCH="*/main"
COOKIE_JAR="/tmp/jenkins-cookies.txt"

# Récupérer le crumb + cookie de session en une seule requête
CRUMB_RESPONSE=$(curl -s -c "$COOKIE_JAR" "$JENKINS/crumbIssuer/api/json")
CRUMB=$(echo "$CRUMB_RESPONSE" | grep -o '"crumb":"[^"]*"' | cut -d'"' -f4)
echo "Crumb: $CRUMB"

# ── Fonction création job ──────────────────────────────────
create_job() {
  local NAME=$1
  local SCRIPT_PATH=$2

  cat > /tmp/job.xml << XMLEOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <description>Smartek - ${NAME}</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <com.cloudbees.jenkins.GitHubPushTrigger plugin="github">
          <spec></spec>
        </com.cloudbees.jenkins.GitHubPushTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${REPO}</url>
          <credentialsId>${CRED}</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>${BRANCH}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>${SCRIPT_PATH}</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
XMLEOF

  CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -b "$COOKIE_JAR" \
    -X POST "${JENKINS}/createItem?name=${NAME}" \
    -H "Content-Type: application/xml" \
    -H "Jenkins-Crumb: ${CRUMB}" \
    --data-binary "@/tmp/job.xml")

  if [ "$CODE" = "200" ]; then
    echo "  ✅ ${NAME}"
  else
    echo "  ❌ ${NAME} → HTTP ${CODE}"
  fi
}

# ── Créer tous les jobs ────────────────────────────────────
echo ""
echo "=== Création des jobs Jenkins Smartek ==="
echo ""

create_job "smartek-all-services"           "jenkins/Jenkinsfile.all-services"
create_job "smartek-config-server"          "jenkins/Jenkinsfile.config-server"
create_job "smartek-eureka-server"          "jenkins/Jenkinsfile.eureka-server"
create_job "smartek-api-gateway"            "jenkins/Jenkinsfile.api-gateway"
create_job "smartek-auth-service"           "jenkins/Jenkinsfile.auth-service"
create_job "smartek-event-service"          "jenkins/Jenkinsfile.event-service"
create_job "smartek-planning-service"       "jenkins/Jenkinsfile.planning-service"
create_job "smartek-training-service"       "jenkins/Jenkinsfile.training-service"
create_job "smartek-offers-service"         "jenkins/Jenkinsfile.offers-service"
create_job "smartek-course-service"         "jenkins/Jenkinsfile.course-service"
create_job "smartek-exam-service"           "jenkins/Jenkinsfile.exam-service"
create_job "smartek-skill-evidence-service" "jenkins/Jenkinsfile.skill-evidence-service"
create_job "smartek-learning-service"       "jenkins/Jenkinsfile.learning-service"
create_job "smartek-sponsor-service"        "jenkins/Jenkinsfile.sponsor-service"
create_job "smartek-certification-badge"    "jenkins/Jenkinsfile.certification-badge-service"
create_job "smartek-frontend"               "jenkins/Jenkinsfile.frontend"

echo ""
echo "=== Terminé ! Ouvre http://localhost:8090 ==="
