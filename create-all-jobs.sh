#!/bin/bash

JENKINS_URL="http://localhost:8080"
USER="wafa"
PASSWORD="WAFAch12#"
REPO_URL="https://github.com/emnahomrani29/Esprit-PIDEV-4SAE1-2026-Smartek"
BRANCH="*/skill-evidence-learning-service"

# Get crumb
CRUMB=$(curl -s -u "$USER:$PASSWORD" "$JENKINS_URL/crumbIssuer/api/json" | python3 -c "import sys,json; print(json.load(sys.stdin)['crumb'])")
echo "Crumb: $CRUMB"

create_job() {
  local JOB_NAME=$1
  local SCRIPT_PATH=$2
  local DESCRIPTION=$3

  XML="<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin=\"workflow-job\">
  <description>${DESCRIPTION}</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition\" plugin=\"workflow-cps\">
    <scm class=\"hudson.plugins.git.GitSCM\" plugin=\"git\">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${REPO_URL}</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>${BRANCH}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class=\"empty-list\"/>
      <extensions/>
    </scm>
    <scriptPath>${SCRIPT_PATH}</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>"

  echo "Creating job: $JOB_NAME ..."
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$USER:$PASSWORD" \
    -H "Jenkins-Crumb: $CRUMB" \
    -H "Content-Type: application/xml" \
    -X POST "$JENKINS_URL/createItem?name=$JOB_NAME" \
    --data-binary "$XML")

  if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ $JOB_NAME créé"
  elif [ "$HTTP_CODE" = "400" ]; then
    echo "⚠️  $JOB_NAME existe déjà"
  else
    echo "❌ $JOB_NAME échoué (HTTP $HTTP_CODE)"
  fi
}

# Backend services
create_job "auth-service"               "Backend/auth-service/Jenkinsfile"               "Pipeline for Auth Service"
create_job "api-gateway"                "Backend/api-gateway/Jenkinsfile"                "Pipeline for API Gateway"
create_job "config-server"              "Backend/config-server/Jenkinsfile"              "Pipeline for Config Server"
create_job "eureka-server"              "Backend/eureka-server/Jenkinsfile"              "Pipeline for Eureka Server"
create_job "certification-badge-service" "Backend/certification-badge-service/Jenkinsfile" "Pipeline for Certification Badge Service"
create_job "course-service"             "Backend/course-service/Jenkinsfile"             "Pipeline for Course Service"
create_job "event-service"              "Backend/event-service/Jenkinsfile"              "Pipeline for Event Service"
create_job "exam-service"               "Backend/exam-service/Jenkinsfile"               "Pipeline for Exam Service"
create_job "offers-service"             "Backend/offers-service/Jenkinsfile"             "Pipeline for Offers Service"
create_job "planning-service"           "Backend/planning-service/Jenkinsfile"           "Pipeline for Planning Service"
create_job "training-service"           "Backend/training-service/Jenkinsfile"           "Pipeline for Training Service"

# Frontend
create_job "frontend"                   "Frontend/angular-app/Jenkinsfile"               "Pipeline for Frontend Angular App"

echo ""
echo "Done! Vérifie sur $JENKINS_URL"
