# ============================================================
# Script PowerShell - Création des jobs Jenkins Smartek
# Usage: .\jenkins\create-jenkins-jobs.ps1
# ============================================================

$JENKINS_URL = "http://localhost:8090"
$CONTAINER   = "smartek-jenkins"
$REPO_URL    = "https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git"
$CREDENTIALS = "github-token"
$BRANCH      = "*/main"

# Récupérer le crumb CSRF depuis le conteneur
$crumbJson = docker exec $CONTAINER curl -s "http://localhost:8080/crumbIssuer/api/json" | ConvertFrom-Json
$crumb = $crumbJson.crumb
Write-Host "✅ Crumb: $crumb" -ForegroundColor Green

# ── Fonction pour créer un job ─────────────────────────────
function Create-Job {
    param(
        [string]$JobName,
        [string]$ScriptPath,
        [string]$Description
    )

    $xml = @"
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <description>$Description</description>
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
          <url>$REPO_URL</url>
          <credentialsId>$CREDENTIALS</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>$BRANCH</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>$ScriptPath</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
"@

    # Écrire le XML dans le conteneur
    $xmlEscaped = $xml -replace '"', '\"'
    docker exec $CONTAINER bash -c "cat > /tmp/job-config.xml << 'XMLEOF'
$xml
XMLEOF"

    # Créer le job via l'API Jenkins interne
    $result = docker exec $CONTAINER curl -s -o /dev/null -w "%{http_code}" `
        -X POST "http://localhost:8080/createItem?name=$JobName" `
        -H "Content-Type: application/xml" `
        -H "Jenkins-Crumb: $crumb" `
        --data-binary "@/tmp/job-config.xml"

    if ($result -eq "200") {
        Write-Host "  ✅ $JobName créé" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  $JobName - HTTP $result" -ForegroundColor Yellow
    }
}

# ── Liste des jobs à créer ─────────────────────────────────
Write-Host ""
Write-Host "🚀 Création des jobs Jenkins Smartek..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$jobs = @(
    @{ Name="smartek-all-services";              Path="jenkins/Jenkinsfile.all-services";              Desc="Pipeline global - tous les services" },
    @{ Name="smartek-api-gateway";               Path="jenkins/Jenkinsfile.api-gateway";               Desc="API Gateway" },
    @{ Name="smartek-auth-service";              Path="jenkins/Jenkinsfile.auth-service";              Desc="Auth Service" },
    @{ Name="smartek-event-service";             Path="jenkins/Jenkinsfile.event-service";             Desc="Event Service" },
    @{ Name="smartek-planning-service";          Path="jenkins/Jenkinsfile.planning-service";          Desc="Planning Service" },
    @{ Name="smartek-training-service";          Path="jenkins/Jenkinsfile.training-service";          Desc="Training Service" },
    @{ Name="smartek-offers-service";            Path="jenkins/Jenkinsfile.offers-service";            Desc="Offers Service" },
    @{ Name="smartek-course-service";            Path="jenkins/Jenkinsfile.course-service";            Desc="Course Service" },
    @{ Name="smartek-exam-service";              Path="jenkins/Jenkinsfile.exam-service";              Desc="Exam Service" },
    @{ Name="smartek-skill-evidence-service";    Path="jenkins/Jenkinsfile.skill-evidence-service";    Desc="Skill Evidence Service" },
    @{ Name="smartek-learning-service";          Path="jenkins/Jenkinsfile.learning-service";          Desc="Learning Service" },
    @{ Name="smartek-sponsor-service";           Path="jenkins/Jenkinsfile.sponsor-service";           Desc="Sponsor Service" },
    @{ Name="smartek-certification-badge";       Path="jenkins/Jenkinsfile.certification-badge-service"; Desc="Certification Badge Service" },
    @{ Name="smartek-eureka-server";             Path="jenkins/Jenkinsfile.eureka-server";             Desc="Eureka Server" },
    @{ Name="smartek-config-server";             Path="jenkins/Jenkinsfile.config-server";             Desc="Config Server" },
    @{ Name="smartek-frontend";                  Path="jenkins/Jenkinsfile.frontend";                  Desc="Frontend Angular" }
)

foreach ($job in $jobs) {
    Create-Job -JobName $job.Name -ScriptPath $job.Path -Description $job.Desc
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Tous les jobs ont été créés !" -ForegroundColor Green
Write-Host "🌐 Ouvre Jenkins : http://localhost:8090" -ForegroundColor Yellow
