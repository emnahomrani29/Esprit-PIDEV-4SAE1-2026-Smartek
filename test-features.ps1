# Test Advanced Features - SMARTEK Platform
# Run this script to test the new pagination and certificate features

Write-Host "🧪 Testing SMARTEK Advanced Features" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login
Write-Host "Step 1: Logging in as Learner..." -ForegroundColor Yellow
$loginBody = @{
    email = "Learner@smartek.com"
    password = "Learner123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    $userId = $loginResponse.userId
    Write-Host "✅ Login successful! User ID: $userId" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Login failed! Make sure auth-service is running on port 8081" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
}

# Step 2: Test Certification Templates Pagination
Write-Host "Step 2: Testing Certification Templates Pagination..." -ForegroundColor Yellow
try {
    $certTemplates = Invoke-RestMethod -Uri "http://localhost:8083/api/certifications-badges/certification-templates/paginated?page=0&size=5" -Headers $headers
    Write-Host "✅ Certification Templates Pagination works!" -ForegroundColor Green
    Write-Host "   Total Elements: $($certTemplates.totalElements)" -ForegroundColor Gray
    Write-Host "   Total Pages: $($certTemplates.totalPages)" -ForegroundColor Gray
    Write-Host "   Current Page: $($certTemplates.number)" -ForegroundColor Gray
    Write-Host "   Page Size: $($certTemplates.size)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Certification Templates Pagination failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 3: Test Badge Templates Pagination
Write-Host "Step 3: Testing Badge Templates Pagination..." -ForegroundColor Yellow
try {
    $badgeTemplates = Invoke-RestMethod -Uri "http://localhost:8083/api/certifications-badges/badge-templates/paginated?page=0&size=5" -Headers $headers
    Write-Host "✅ Badge Templates Pagination works!" -ForegroundColor Green
    Write-Host "   Total Elements: $($badgeTemplates.totalElements)" -ForegroundColor Gray
    Write-Host "   Total Pages: $($badgeTemplates.totalPages)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Badge Templates Pagination failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Test Earned Certifications Pagination
Write-Host "Step 4: Testing Earned Certifications Pagination..." -ForegroundColor Yellow
try {
    $earnedCerts = Invoke-RestMethod -Uri "http://localhost:8083/api/certifications-badges/earned-certifications/learner/$userId/paginated?page=0&size=10" -Headers $headers
    Write-Host "✅ Earned Certifications Pagination works!" -ForegroundColor Green
    Write-Host "   Total Elements: $($earnedCerts.totalElements)" -ForegroundColor Gray
    Write-Host "   Total Pages: $($earnedCerts.totalPages)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 5: Test Certificate Details (if certifications exist)
    if ($earnedCerts.content.Count -gt 0) {
        $certId = $earnedCerts.content[0].id
        Write-Host "Step 5: Testing Certificate Details Endpoint..." -ForegroundColor Yellow
        try {
            $certDetails = Invoke-RestMethod -Uri "http://localhost:8083/api/certifications-badges/earned-certifications/$certId/details" -Headers $headers
            Write-Host "✅ Certificate Details endpoint works!" -ForegroundColor Green
            Write-Host "   Certificate ID: $($certDetails.id)" -ForegroundColor Gray
            Write-Host "   Title: $($certDetails.certificationTemplate.title)" -ForegroundColor Gray
            Write-Host "   Issue Date: $($certDetails.issueDate)" -ForegroundColor Gray
            Write-Host ""
        } catch {
            Write-Host "❌ Certificate Details failed!" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️  No earned certifications found. Skipping certificate details test." -ForegroundColor Yellow
        Write-Host ""
    }
} catch {
    Write-Host "❌ Earned Certifications Pagination failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 6: Test Earned Badges Pagination
Write-Host "Step 6: Testing Earned Badges Pagination..." -ForegroundColor Yellow
try {
    $earnedBadges = Invoke-RestMethod -Uri "http://localhost:8083/api/certifications-badges/earned-badges/learner/$userId/paginated?page=0&size=10" -Headers $headers
    Write-Host "✅ Earned Badges Pagination works!" -ForegroundColor Green
    Write-Host "   Total Elements: $($earnedBadges.totalElements)" -ForegroundColor Gray
    Write-Host "   Total Pages: $($earnedBadges.totalPages)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Earned Badges Pagination failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Summary
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🎉 Testing Complete!" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Run 'npm install' in Frontend/angular-app" -ForegroundColor White
Write-Host "2. Start Angular dev server: 'npm start'" -ForegroundColor White
Write-Host "3. Navigate to http://localhost:4200" -ForegroundColor White
Write-Host "4. Test certificate viewer and PDF download" -ForegroundColor White
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Yellow
Write-Host "- ADVANCED-FEATURES-IMPLEMENTATION.md - Complete guide" -ForegroundColor White
Write-Host "- IMPLEMENTATION-SUMMARY.md - Quick summary" -ForegroundColor White
Write-Host "- test-advanced-features.http - API tests" -ForegroundColor White
Write-Host ""
