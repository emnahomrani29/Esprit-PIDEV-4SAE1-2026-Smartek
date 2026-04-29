# Fix Jenkins Pipeline Git Issue
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "JENKINS PIPELINE FIX" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "The pipeline failed because Jenkins couldn't checkout from Git." -ForegroundColor Yellow
Write-Host ""

Write-Host "SOLUTION: Reconfigure the pipeline in Jenkins" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Open Jenkins" -ForegroundColor Cyan
Write-Host "   URL: http://localhost:9091" -ForegroundColor White
Write-Host "   Login: admin / admin123" -ForegroundColor White
Write-Host ""

Write-Host "Step 2: Click on your pipeline" -ForegroundColor Cyan
Write-Host "   Pipeline name: smartek-sponsor-git-pipeline" -ForegroundColor White
Write-Host ""

Write-Host "Step 3: Click 'Configure' (left sidebar)" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 4: Scroll to 'Pipeline' section and verify:" -ForegroundColor Cyan
Write-Host "   Definition: Pipeline script from SCM" -ForegroundColor White
Write-Host "   SCM: Git" -ForegroundColor White
Write-Host "   Repository URL: https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git" -ForegroundColor White
Write-Host "   Credentials: - none -" -ForegroundColor White
Write-Host "   Branch Specifier: */sponsor" -ForegroundColor White
Write-Host "   Script Path: Backend/smartek_sponsor/Jenkinsfile" -ForegroundColor White
Write-Host ""

Write-Host "Step 5: Click 'Save'" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 6: Click 'Build Now'" -ForegroundColor Cyan
Write-Host ""

Write-Host "=========================================" -ForegroundColor Green
Write-Host "ALTERNATIVE: Use Direct Pipeline Script" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

Write-Host "If the above doesn't work:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. In Jenkins, click your pipeline > Configure" -ForegroundColor White
Write-Host "2. In Pipeline section:" -ForegroundColor White
Write-Host "   - Definition: Pipeline script (NOT from SCM)" -ForegroundColor White
Write-Host "3. Copy content from: Backend/smartek_sponsor/Jenkinsfile.direct" -ForegroundColor White
Write-Host "4. Paste it in the Script box" -ForegroundColor White
Write-Host "5. Click Save" -ForegroundColor White
Write-Host "6. Click Build Now" -ForegroundColor White
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "VERIFY GIT IN JENKINS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "To verify Git works in Jenkins:" -ForegroundColor Yellow
Write-Host "1. Jenkins > Manage Jenkins > Script Console" -ForegroundColor White
Write-Host "2. Run this script:" -ForegroundColor White
Write-Host '   def proc = "git --version".execute()' -ForegroundColor Gray
Write-Host '   proc.waitFor()' -ForegroundColor Gray
Write-Host '   println "Git version: ${proc.text}"' -ForegroundColor Gray
Write-Host ""

Write-Host "You should see Git version output" -ForegroundColor White
Write-Host ""

Write-Host "=========================================" -ForegroundColor Green
Write-Host "After fixing, your pipeline will work!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
