# Postman Testing Guide - Auto-Award System

## Complete Testing Workflow

This guide will walk you through testing the entire auto-award system using Postman.

---

## Prerequisites

- Postman installed
- Backend running on `http://localhost:8083`
- MySQL database running with `smartek_db`

---

## Step 1: Setup - Create Test Data

### 1.1 Create Certification Template

**Request:**
```
POST http://localhost:8083/api/certifications-badges/certification-templates
Content-Type: application/json
```

**Body:**
```json
{
  "title": "Spring Boot Fundamentals Certification",
  "description": "Awarded for passing the Spring Boot Fundamentals exam with a score of 60% or higher",
  "examId": 102
}
```

**Expected Response:** `201 Created`
```json
{
  "id": 12,
  "title": "Spring Boot Fundamentals Certification",
  "description": "Awarded for passing the Spring Boot Fundamentals exam with a score of 60% or higher",
  "examId": 102
}
```

**Save the `id` for later reference.**

---

### 1.2 Create Bronze Badge Template (60%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Spring Boot Bronze Badge",
  "description": "Awarded for scoring 60% or higher on the Spring Boot exam",
  "examId": 102,
  "minimumScore": 60.0
}
```

**Expected Response:** `201 Created`

---

### 1.3 Create Silver Badge Template (75%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Spring Boot Silver Badge",
  "description": "Awarded for scoring 75% or higher on the Spring Boot exam",
  "examId": 102,
  "minimumScore": 75.0
}
```

**Expected Response:** `201 Created`

---

### 1.4 Create Gold Badge Template (90%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Spring Boot Gold Badge",
  "description": "Awarded for scoring 90% or higher on the Spring Boot exam",
  "examId": 102,
  "minimumScore": 90.0
}
```

**Expected Response:** `201 Created`

---

## Step 2: Test Auto-Award System

### 2.1 Test 1 - Failing Score (45%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 100,
  "examId": 102,
  "score": 45.0,
  "maxScore": 100.0
}
```

**Expected Response:** `200 OK`
```json
{
  "learnerId": 100,
  "examId": 102,
  "percentage": 45.0,
  "passed": false,
  "certificationAwarded": false,
  "certificationId": null,
  "badgeAwarded": false,
  "badgeId": null,
  "message": "Exam score: 45.00%. Did not pass (minimum 60% required)."
}
```

**✅ Verification:**
- `passed` = false
- `certificationAwarded` = false
- `badgeAwarded` = false

---

### 2.2 Test 2 - Bronze Badge (60%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 101,
  "examId": 102,
  "score": 60.0,
  "maxScore": 100.0
}
```

**Expected Response:** `200 OK`
```json
{
  "learnerId": 101,
  "examId": 102,
  "percentage": 60.0,
  "passed": true,
  "certificationAwarded": true,
  "certificationId": 15,
  "badgeAwarded": true,
  "badgeId": 8,
  "message": "Exam score: 60.00%. Passed! Certification awarded. Badge awarded for high achievement!"
}
```

**✅ Verification:**
- `passed` = true
- `certificationAwarded` = true
- `badgeAwarded` = true
- Badge should be Bronze (60% threshold)

---

### 2.3 Test 3 - Silver Badge (75%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 102,
  "examId": 102,
  "score": 75.0,
  "maxScore": 100.0
}
```

**Expected Response:** `200 OK`
```json
{
  "learnerId": 102,
  "examId": 102,
  "percentage": 75.0,
  "passed": true,
  "certificationAwarded": true,
  "certificationId": 16,
  "badgeAwarded": true,
  "badgeId": 9,
  "message": "Exam score: 75.00%. Passed! Certification awarded. Badge awarded for high achievement!"
}
```

**✅ Verification:**
- Badge should be Silver (75% threshold)
- Bronze badge should NOT be awarded (only highest)

---

### 2.4 Test 4 - Gold Badge (92%)

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 103,
  "examId": 102,
  "score": 92.0,
  "maxScore": 100.0
}
```

**Expected Response:** `200 OK`
```json
{
  "learnerId": 103,
  "examId": 102,
  "percentage": 92.0,
  "passed": true,
  "certificationAwarded": true,
  "certificationId": 17,
  "badgeAwarded": true,
  "badgeId": 10,
  "message": "Exam score: 92.00%. Passed! Certification awarded. Badge awarded for high achievement!"
}
```

**✅ Verification:**
- Badge should be Gold (90% threshold)
- Silver and Bronze badges should NOT be awarded

---

### 2.5 Test 5 - Duplicate Prevention

**Request:** (Same as Test 3, same learnerId)
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 102,
  "examId": 102,
  "score": 85.0,
  "maxScore": 100.0
}
```

**Expected Response:** `200 OK`
```json
{
  "learnerId": 102,
  "examId": 102,
  "percentage": 85.0,
  "passed": true,
  "certificationAwarded": false,
  "certificationId": null,
  "badgeAwarded": false,
  "badgeId": null,
  "message": "Exam score: 85.00%. Passed!"
}
```

**✅ Verification:**
- `certificationAwarded` = false (already has it)
- `badgeAwarded` = false (already has it)
- Duplicate prevention working

---

## Step 3: Verify Data in Database

### 3.1 Query Earned Certifications (Requires Authentication)

**⚠️ Note:** This endpoint requires JWT authentication. You need to login first.

#### 3.1.1 Login to Get JWT Token

**Request:**
```
POST http://localhost:8083/api/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "username": "learner1@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 101,
  "username": "learner1@example.com",
  "role": "LEARNER"
}
```

**📋 Copy the `token` value**

---

#### 3.1.2 Query Earned Certifications

**Request:**
```
GET http://localhost:8083/api/certifications-badges/earned-certifications/learner/101
Authorization: Bearer <paste-your-token-here>
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 15,
    "certificationTemplate": {
      "id": 12,
      "title": "Spring Boot Fundamentals Certification",
      "description": "Awarded for passing the Spring Boot Fundamentals exam with a score of 60% or higher",
      "examId": 102
    },
    "learnerId": 101,
    "issueDate": "2026-02-27",
    "expiryDate": "2028-02-27",
    "certificateUrl": null,
    "awardedBy": 0,
    "expired": false
  }
]
```

---

#### 3.1.3 Query Earned Badges

**Request:**
```
GET http://localhost:8083/api/certifications-badges/earned-badges/learner/101
Authorization: Bearer <paste-your-token-here>
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 8,
    "badgeTemplate": {
      "id": 5,
      "name": "Spring Boot Bronze Badge",
      "description": "Awarded for scoring 60% or higher on the Spring Boot exam",
      "examId": 102,
      "minimumScore": 60.0
    },
    "learnerId": 101,
    "awardDate": "2026-02-27",
    "awardedBy": 0
  }
]
```

---

## Step 4: Test Edge Cases

### 4.1 Test with Different Max Score

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 104,
  "examId": 102,
  "score": 45.0,
  "maxScore": 50.0
}
```

**Expected Response:**
```json
{
  "percentage": 90.0,
  "passed": true,
  "certificationAwarded": true,
  "badgeAwarded": true
}
```

**✅ Verification:** 45/50 = 90% → Should award Gold badge

---

### 4.2 Test with Non-Existent Exam

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 105,
  "examId": 999,
  "score": 85.0,
  "maxScore": 100.0
}
```

**Expected Response:**
```json
{
  "percentage": 85.0,
  "passed": true,
  "certificationAwarded": false,
  "badgeAwarded": false,
  "message": "Exam score: 85.00%. Passed!"
}
```

**✅ Verification:** No templates exist for exam 999, so nothing awarded

---

### 4.3 Test Validation - Missing Fields

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 106,
  "examId": 102
}
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-27T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "Score is required",
    "Max score is required"
  ]
}
```

---

### 4.4 Test Validation - Negative Score

**Request:**
```
POST http://localhost:8083/api/certifications-badges/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Body:**
```json
{
  "learnerId": 107,
  "examId": 102,
  "score": -10.0,
  "maxScore": 100.0
}
```

**Expected Response:** `400 Bad Request`

---

## Step 5: Query All Templates

### 5.1 Get All Certification Templates

**Request:**
```
GET http://localhost:8083/api/certifications-badges/certification-templates
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 12,
    "title": "Spring Boot Fundamentals Certification",
    "description": "Awarded for passing the Spring Boot Fundamentals exam with a score of 60% or higher",
    "examId": 102
  }
]
```

---

### 5.2 Get All Badge Templates

**Request:**
```
GET http://localhost:8083/api/certifications-badges/badge-templates
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 5,
    "name": "Spring Boot Bronze Badge",
    "description": "Awarded for scoring 60% or higher on the Spring Boot exam",
    "examId": 102,
    "minimumScore": 60.0
  },
  {
    "id": 6,
    "name": "Spring Boot Silver Badge",
    "description": "Awarded for scoring 75% or higher on the Spring Boot exam",
    "examId": 102,
    "minimumScore": 75.0
  },
  {
    "id": 7,
    "name": "Spring Boot Gold Badge",
    "description": "Awarded for scoring 90% or higher on the Spring Boot exam",
    "examId": 102,
    "minimumScore": 90.0
  }
]
```

---

## Summary Checklist

### Setup Phase
- [ ] Created certification template for exam 102
- [ ] Created Bronze badge template (60%)
- [ ] Created Silver badge template (75%)
- [ ] Created Gold badge template (90%)

### Auto-Award Tests
- [ ] Test 1: 45% score → Nothing awarded ✅
- [ ] Test 2: 60% score → Certification + Bronze badge ✅
- [ ] Test 3: 75% score → Certification + Silver badge ✅
- [ ] Test 4: 92% score → Certification + Gold badge ✅
- [ ] Test 5: Duplicate prevention working ✅

### Verification Tests
- [ ] Login successful and JWT token obtained
- [ ] Query earned certifications working
- [ ] Query earned badges working
- [ ] Data matches what was awarded

### Edge Cases
- [ ] Different max score calculation correct
- [ ] Non-existent exam handled gracefully
- [ ] Validation errors returned correctly

---

## Expected Results Summary

| Test | Score | Certification | Badge | Level |
|------|-------|---------------|-------|-------|
| 1 | 45% | ❌ | ❌ | - |
| 2 | 60% | ✅ | ✅ | Bronze |
| 3 | 75% | ✅ | ✅ | Silver |
| 4 | 92% | ✅ | ✅ | Gold |
| 5 | 85% (duplicate) | ❌ | ❌ | - |

---

## Troubleshooting

### Issue: 401 Unauthorized on Query Endpoints
**Solution:** Make sure you're including the JWT token in the Authorization header

### Issue: 500 Internal Server Error
**Solution:** Check backend logs at `Backend/certification-badge-service/logs/certification-badge-service-error.log`

### Issue: No templates found
**Solution:** Run Step 1 to create the test data first

### Issue: certificationAwarded = false even though passed
**Solution:** Make sure certification template exists with the correct examId

---

## Quick Test Script

Copy this into Postman as a Collection and run all tests in sequence:

1. Create Templates (4 requests)
2. Test Auto-Award (5 requests)
3. Login (1 request)
4. Query Data (2 requests)
5. Edge Cases (4 requests)

**Total: 16 requests**

All tests should pass! ✅
