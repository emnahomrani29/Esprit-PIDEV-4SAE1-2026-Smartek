# Restart Jenkins with Docker Socket Access
Write-Host "🔄 Restarting Jenkins with Docker socket access..." -ForegroundColor Cyan

# Stop and remove existing Jenkins container
Write-Host "Stopping existing Jenkins container..." -ForegroundColor Yellow
docker stop jenkins 2>$null
docker rm jenkins 2>$null

# Start Jenkins with Docker socket mounted
Write-Host "Starting Jenkins with Docker access..." -ForegroundColor Green
docker run -d `
  --name jenkins `
  -p 9091:8080 `
  -p 50000:50000 `
  -v jenkins-data:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -u root `
  jenkins/jenkins:lts

Write-Host ""
Write-Host "✅ Jenkins restarted successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Jenkins Details:" -ForegroundColor Cyan
Write-Host "   URL: http://localhost:9091" -ForegroundColor White
Write-Host "   Username: admin" -ForegroundColor White
Write-Host "   Password: admin123" -ForegroundColor White
Write-Host ""
Write-Host "⏳ Waiting for Jenkins to start (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Install Docker CLI inside Jenkins container
Write-Host "📦 Installing Docker CLI inside Jenkins..." -ForegroundColor Cyan
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y docker.io"

Write-Host ""
Write-Host "✅ Setup complete! Jenkins can now build Docker images." -ForegroundColor Green
Write-Host "🔗 Access Jenkins at: http://localhost:9091" -ForegroundColor Cyan
