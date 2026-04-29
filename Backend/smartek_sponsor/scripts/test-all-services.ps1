# Test All Services
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TESTING ALL SERVICES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allGood = $true

# Test 1: Application
Write-Host "1. Testing Application (http://localhost:8080)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Application is running" -ForegroundColor Green
        Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [FAIL] Application not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 2: Jenkins
Write-Host "2. Testing Jenkins (http://localhost:9091)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9091" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Jenkins is running" -ForegroundColor Green
    }
} catch {
    Write-Host "   [FAIL] Jenkins not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 3: SonarQube
Write-Host "3. Testing SonarQube (http://localhost:9000)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] SonarQube is running" -ForegroundColor Green
    }
} catch {
    Write-Host "   [FAIL] SonarQube not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 4: Nexus
Write-Host "4. Testing Nexus (http://localhost:8081)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Nexus is running" -ForegroundColor Green
    }
} catch {
    Write-Host "   [FAIL] Nexus not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 5: Docker Registry
Write-Host "5. Testing Docker Registry (http://localhost:5000)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5000/v2/_catalog" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Docker Registry is running" -ForegroundColor Green
        Write-Host "   Images: $($response.Content)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [FAIL] Docker Registry not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 6: Prometheus
Write-Host "6. Testing Prometheus (http://localhost:9090)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Prometheus is running" -ForegroundColor Green
    }
} catch {
    Write-Host "   [FAIL] Prometheus not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 7: Grafana
Write-Host "7. Testing Grafana (http://localhost:3000)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "   [OK] Grafana is running" -ForegroundColor Green
    }
} catch {
    Write-Host "   [FAIL] Grafana not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 8: MySQL
Write-Host "8. Testing MySQL (localhost:3306)..." -ForegroundColor Yellow
$mysqlTest = docker exec smartek-mysql mysqladmin ping -h localhost -u root -prootpassword 2>$null
if ($mysqlTest -like "*mysqld is alive*") {
    Write-Host "   [OK] MySQL is running" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] MySQL not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 9: Kubernetes
Write-Host "9. Testing Kubernetes..." -ForegroundColor Yellow
try {
    $nodes = kubectl get nodes --no-headers 2>$null
    if ($nodes) {
        Write-Host "   [OK] Kubernetes is running" -ForegroundColor Green
        Write-Host "   Nodes: $nodes" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [FAIL] Kubernetes not responding" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 10: Kubernetes Deployment
Write-Host "10. Testing Kubernetes Deployment..." -ForegroundColor Yellow
try {
    $pods = kubectl get pods -n smartek-production --no-headers 2>$null
    if ($pods) {
        $podCount = ($pods | Measure-Object).Count
        Write-Host "   [OK] Kubernetes deployment exists" -ForegroundColor Green
        Write-Host "   Pods running: $podCount" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [FAIL] Kubernetes deployment not found" -ForegroundColor Red
    $allGood = $false
}
Write-Host ""

# Test 11: Docker Containers
Write-Host "11. Testing Docker Containers..." -ForegroundColor Yellow
$containers = docker ps --format "{{.Names}}" | Select-String "smartek"
$containerCount = ($containers | Measure-Object).Count
Write-Host "   [OK] Docker containers running: $containerCount" -ForegroundColor Green
docker ps --format "table {{.Names}}\t{{.Status}}" | Select-String "smartek"
Write-Host ""

# Final Summary
Write-Host "========================================" -ForegroundColor Cyan
if ($allGood) {
    Write-Host "ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "READY TO DEMONSTRATE:" -ForegroundColor Yellow
    Write-Host "1. Open Jenkins: http://localhost:9091" -ForegroundColor White
    Write-Host "2. Click: smartek-sponsor-git-pipeline" -ForegroundColor White
    Write-Host "3. Click: Build Now" -ForegroundColor White
    Write-Host "4. Watch all 12 stages execute!" -ForegroundColor White
} else {
    Write-Host "SOME TESTS FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Check the failed services above" -ForegroundColor Yellow
}
Write-Host ""
