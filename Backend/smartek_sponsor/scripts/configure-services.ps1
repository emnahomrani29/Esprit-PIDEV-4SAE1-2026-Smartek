# Configure Nexus and SonarQube for Jenkins Pipeline
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "SERVICE CONFIGURATION SCRIPT" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Check if services are running
Write-Host "1. Checking Services Status..." -ForegroundColor Yellow
$nexusRunning = docker ps --filter "name=smartek-nexus" --format "{{.Names}}"
$sonarRunning = docker ps --filter "name=smartek-sonarqube" --format "{{.Names}}"

if ($nexusRunning) {
    Write-Host "   [OK] Nexus is running" -ForegroundColor Green
} else {
    Write-Host "   [ERROR] Nexus is not running" -ForegroundColor Red
    Write-Host "   Run: docker-compose up -d nexus" -ForegroundColor Yellow
}

if ($sonarRunning) {
    Write-Host "   [OK] SonarQube is running" -ForegroundColor Green
} else {
    Write-Host "   [ERROR] SonarQube is not running" -ForegroundColor Red
    Write-Host "   Run: docker-compose up -d sonarqube" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "NEXUS CONFIGURATION" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Nexus URL: http://localhost:8081" -ForegroundColor White
Write-Host ""

# Get Nexus admin password
$nexusPassword = docker exec smartek-nexus cat /nexus-data/admin.password 2>$null
if ($nexusPassword) {
    Write-Host "Initial Admin Password: $nexusPassword" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "MANUAL STEPS REQUIRED:" -ForegroundColor Red
    Write-Host "1. Open http://localhost:8081 in browser" -ForegroundColor White
    Write-Host "2. Click 'Sign In' (top right)" -ForegroundColor White
    Write-Host "3. Username: admin" -ForegroundColor White
    Write-Host "4. Password: $nexusPassword" -ForegroundColor Yellow
    Write-Host "5. Click 'Next' and set new password to: admin123" -ForegroundColor White
    Write-Host "6. Enable anonymous access: YES" -ForegroundColor White
    Write-Host "7. Click 'Finish'" -ForegroundColor White
    Write-Host ""
    Write-Host "REPOSITORIES TO CREATE:" -ForegroundColor Yellow
    Write-Host "- maven-releases (hosted, release)" -ForegroundColor White
    Write-Host "- maven-snapshots (hosted, snapshot)" -ForegroundColor White
    Write-Host "- maven-public (group, includes central + releases + snapshots)" -ForegroundColor White
} else {
    Write-Host "Nexus is still initializing or already configured" -ForegroundColor Yellow
    Write-Host "If already configured, use: admin/admin123" -ForegroundColor White
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "SONARQUBE CONFIGURATION" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "SonarQube URL: http://localhost:9000" -ForegroundColor White
Write-Host ""
Write-Host "MANUAL STEPS REQUIRED:" -ForegroundColor Red
Write-Host "1. Open http://localhost:9000 in browser" -ForegroundColor White
Write-Host "2. Login with default credentials:" -ForegroundColor White
Write-Host "   Username: admin" -ForegroundColor White
Write-Host "   Password: admin" -ForegroundColor White
Write-Host "3. Change password to: admin123" -ForegroundColor Yellow
Write-Host "4. Skip tutorial" -ForegroundColor White
Write-Host ""

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "JENKINS CONFIGURATION" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Jenkins URL: http://localhost:9091" -ForegroundColor White
Write-Host ""
Write-Host "CREDENTIALS TO ADD IN JENKINS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Nexus Credentials:" -ForegroundColor White
Write-Host "   - Go to: Manage Jenkins > Credentials" -ForegroundColor Gray
Write-Host "   - Add: Username with password" -ForegroundColor Gray
Write-Host "   - ID: nexus-credentials" -ForegroundColor Gray
Write-Host "   - Username: admin" -ForegroundColor Gray
Write-Host "   - Password: admin123" -ForegroundColor Gray
Write-Host ""
Write-Host "2. SonarQube Token:" -ForegroundColor White
Write-Host "   - In SonarQube: My Account > Security > Generate Token" -ForegroundColor Gray
Write-Host "   - Name: jenkins" -ForegroundColor Gray
Write-Host "   - Copy the token" -ForegroundColor Gray
Write-Host "   - In Jenkins: Manage Jenkins > Configure System" -ForegroundColor Gray
Write-Host "   - Add SonarQube server:" -ForegroundColor Gray
Write-Host "     Name: SonarQube" -ForegroundColor Gray
Write-Host "     URL: http://host.docker.internal:9000" -ForegroundColor Gray
Write-Host "     Token: [paste token]" -ForegroundColor Gray
Write-Host ""

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "ALL SERVICES STATUS" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String "smartek"
Write-Host ""
Write-Host "Configuration script completed!" -ForegroundColor Green
Write-Host "Follow the manual steps above to complete setup" -ForegroundColor Yellow
