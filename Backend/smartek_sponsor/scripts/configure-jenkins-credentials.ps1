# Configure Jenkins Credentials
Write-Host "🔐 Configuring Jenkins Credentials..." -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 You need to configure these credentials in Jenkins:" -ForegroundColor Yellow
Write-Host ""

Write-Host "1️⃣  SonarQube Token:" -ForegroundColor Green
Write-Host "   - Go to SonarQube: http://localhost:9000" -ForegroundColor White
Write-Host "   - Login: admin / admin123" -ForegroundColor White
Write-Host "   - Go to: My Account > Security > Generate Token" -ForegroundColor White
Write-Host "   - Token Name: jenkins" -ForegroundColor White
Write-Host "   - Copy the generated token" -ForegroundColor White
Write-Host ""
Write-Host "   Then in Jenkins:" -ForegroundColor White
Write-Host "   - Go to: Manage Jenkins > Credentials > System > Global credentials" -ForegroundColor White
Write-Host "   - Click: Add Credentials" -ForegroundColor White
Write-Host "   - Kind: Secret text" -ForegroundColor White
Write-Host "   - Secret: [paste SonarQube token]" -ForegroundColor White
Write-Host "   - ID: sonarqube-token" -ForegroundColor White
Write-Host "   - Description: SonarQube Authentication Token" -ForegroundColor White
Write-Host ""

Write-Host "2️⃣  Nexus Credentials:" -ForegroundColor Green
Write-Host "   - In Jenkins: Manage Jenkins > Credentials > System > Global credentials" -ForegroundColor White
Write-Host "   - Click: Add Credentials" -ForegroundColor White
Write-Host "   - Kind: Username with password" -ForegroundColor White
Write-Host "   - Username: admin" -ForegroundColor White
Write-Host "   - Password: admin123" -ForegroundColor White
Write-Host "   - ID: nexus-credentials" -ForegroundColor White
Write-Host "   - Description: Nexus Repository Credentials" -ForegroundColor White
Write-Host ""

Write-Host "3️⃣  Docker Registry (Optional):" -ForegroundColor Green
Write-Host "   - For localhost:5000, no credentials needed" -ForegroundColor White
Write-Host "   - Registry is running without authentication" -ForegroundColor White
Write-Host ""

Write-Host "✅ After configuring credentials, run your pipeline again!" -ForegroundColor Green
Write-Host ""
