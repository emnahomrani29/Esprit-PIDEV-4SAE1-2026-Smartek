# Complete CI/CD Pipeline Testing Script
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "COMPLETE CI/CD PIPELINE TEST" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"

# Function to check service health
function Test-ServiceHealth {
    param($Name, $Url)
    Write-Host "Checking $Name..." -ForegroundColor Yellow -NoNewline
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -UseBasicParsing
        Write-Host " [OK] RUNNING" -ForegroundColor Green
        return $true
    } catch {
        Write-Host " [FAIL] NOT AVAILABLE" -ForegroundColor Red
        return $false
    }
}

Write-Host "1. CHECKING DOCKER SERVICES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
$services = @(
    @{Name="Application"; Url="http://localhost:8080/actuator/health"},
    @{Name="MySQL"; Container="smartek-mysql"},
    @{Name="Jenkins"; Url="http://localhost:9091"},
    @{Name="SonarQube"; Url="http://localhost:9000"},
    @{Name="Nexus"; Url="http://localhost:8081"},
    @{Name="Docker Registry"; Url="http://localhost:5000/v2/"},
    @{Name="Prometheus"; Url="http://localhost:9090"},
    @{Name="Grafana"; Url="http://localhost:3000"}
)

$allServicesUp = $true
foreach ($service in $services) {
    if ($service.Url) {
        $result = Test-ServiceHealth -Name $service.Name -Url $service.Url
        if (-not $result) { $allServicesUp = $false }
    } elseif ($service.Container) {
        Write-Host "Checking $($service.Name)..." -ForegroundColor Yellow -NoNewline
        $container = docker ps --filter "name=$($service.Container)" --format "{{.Status}}"
        if ($container -match "Up") {
            Write-Host " [OK] RUNNING" -ForegroundColor Green
        } else {
            Write-Host " [FAIL] NOT RUNNING" -ForegroundColor Red
            $allServicesUp = $false
        }
    }
}
Write-Host ""

Write-Host "2. CHECKING KUBERNETES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
Write-Host "Checking Kubernetes cluster..." -ForegroundColor Yellow
$k8sNodes = kubectl get nodes 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Kubernetes cluster is running" -ForegroundColor Green
    Write-Host ""
    Write-Host "Pods in smartek-production namespace:" -ForegroundColor Cyan
    kubectl get pods -n smartek-production
    Write-Host ""
} else {
    Write-Host "[FAIL] Kubernetes cluster not available" -ForegroundColor Red
    Write-Host ""
}

Write-Host "3. TESTING APPLICATION ENDPOINTS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
$endpoints = @(
    @{Name="Health Check"; Url="http://localhost:8080/actuator/health"},
    @{Name="Actuator Info"; Url="http://localhost:8080/actuator/info"},
    @{Name="Prometheus Metrics"; Url="http://localhost:8080/actuator/prometheus"},
    @{Name="Swagger UI"; Url="http://localhost:8080/swagger-ui.html"}
)

foreach ($endpoint in $endpoints) {
    Write-Host "Testing $($endpoint.Name)..." -ForegroundColor Yellow -NoNewline
    try {
        $response = Invoke-WebRequest -Uri $endpoint.Url -TimeoutSec 5 -UseBasicParsing
        Write-Host " [OK] $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host " [FAIL] FAILED" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "4. CHECKING DOCKER IMAGES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
Write-Host "Local Docker images:" -ForegroundColor Cyan
docker images | Select-String "smartek-sponsor"
Write-Host ""
Write-Host "Registry images:" -ForegroundColor Cyan
try {
    $registryImages = Invoke-RestMethod -Uri "http://localhost:5000/v2/_catalog" -UseBasicParsing
    $registryImages.repositories | ForEach-Object {
        Write-Host "   - $_" -ForegroundColor White
    }
} catch {
    Write-Host "   [FAIL] Cannot access registry" -ForegroundColor Red
}
Write-Host ""

Write-Host "5. RUNNING UNIT TESTS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
Set-Location "Backend/smartek_sponsor"
Write-Host "Running Maven tests with H2 database..." -ForegroundColor Yellow
mvn test -Dspring.profiles.active=test
$testResult = $LASTEXITCODE
Set-Location "../.."
if ($testResult -eq 0) {
    Write-Host "[OK] All tests passed!" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Some tests failed" -ForegroundColor Red
}
Write-Host ""

Write-Host "6. JENKINS PIPELINE STATUS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
Write-Host "Jenkins Details:" -ForegroundColor Yellow
Write-Host "   URL: http://localhost:9091" -ForegroundColor White
Write-Host "   Username: admin" -ForegroundColor White
Write-Host "   Password: admin123" -ForegroundColor White
Write-Host "   Pipeline: smartek-sponsor-git-pipeline" -ForegroundColor White
Write-Host ""
Write-Host "To trigger pipeline:" -ForegroundColor Yellow
Write-Host "   1. Open http://localhost:9091" -ForegroundColor White
Write-Host "   2. Click on 'smartek-sponsor-git-pipeline'" -ForegroundColor White
Write-Host "   3. Click 'Build Now'" -ForegroundColor White
Write-Host ""

Write-Host "7. CREDENTIALS CONFIGURATION" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray
Write-Host "Make sure you configured Jenkins credentials:" -ForegroundColor Yellow
Write-Host "   - SonarQube Token (ID: sonarqube-token)" -ForegroundColor White
Write-Host "   - Nexus Credentials (ID: nexus-credentials)" -ForegroundColor White
Write-Host ""
Write-Host "   Run: .\scripts\configure-jenkins-credentials.ps1" -ForegroundColor Cyan
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
if ($allServicesUp) {
    Write-Host "[OK] All services are running" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Some services are not running" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Quick Links:" -ForegroundColor Cyan
Write-Host "   Application:    http://localhost:8080" -ForegroundColor White
Write-Host "   Jenkins:        http://localhost:9091" -ForegroundColor White
Write-Host "   SonarQube:      http://localhost:9000" -ForegroundColor White
Write-Host "   Nexus:          http://localhost:8081" -ForegroundColor White
Write-Host "   Prometheus:     http://localhost:9090" -ForegroundColor White
Write-Host "   Grafana:        http://localhost:3000" -ForegroundColor White
Write-Host "   Swagger:        http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Testing complete!" -ForegroundColor Green
