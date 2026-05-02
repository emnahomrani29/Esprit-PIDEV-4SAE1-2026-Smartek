# Exam Integration API Documentation

## Overview
This API allows the exam service to automatically award certifications and badges to learners based on their exam performance.

## Authentication
All endpoints require an internal API key for security:
```
X-Internal-Api-Key: exam-service-dev-key
```

## Endpoint

### Process Exam Result
Automatically awards certifications and badges based on exam performance.

**URL:** `POST /api/exam-integration/process-exam-result`

**Headers:**
```
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key
```

**Request Body:**
```json
{
  "learnerId": 2,
  "examId": 102,
  "score": 75.0,
  "maxScore": 100.0
}
```

**Field Descriptions:**
- `learnerId` (Long, required): ID of the learner who took the exam
- `examId` (Long, required): ID of the exam that was completed
- `score` (Double, required): Score achieved by the learner (must be >= 0)
- `maxScore` (Double, required): Maximum possible score for the exam (must be > 0)

**Response (200 OK):**
```json
{
  "learnerId": 2,
  "examId": 102,
  "percentage": 75.0,
  "passed": true,
  "certificationAwarded": true,
  "certificationId": 5,
  "badgeAwarded": true,
  "badgeId": 3,
  "message": "Exam score: 75.00%. Passed! Certification awarded. Badge awarded for high achievement!"
}
```

**Response Field Descriptions:**
- `learnerId`: ID of the learner
- `examId`: ID of the exam
- `percentage`: Calculated percentage score (0-100)
- `passed`: Whether the learner passed (>= 60%)
- `certificationAwarded`: Whether a certification was awarded
- `certificationId`: ID of the awarded certification (null if not awarded)
- `badgeAwarded`: Whether a badge was awarded
- `badgeId`: ID of the awarded badge (null if not awarded)
- `message`: Human-readable result message

## Business Logic

### Certification Awarding
- **Trigger:** Learner achieves >= 60% on an exam
- **Process:**
  1. Find certification template linked to the exam ID
  2. Check if learner already has this certification (prevent duplicates)
  3. Award certification with 2-year validity
- **Result:** Certification appears in learner's "My Certifications" dashboard

### Badge Awarding
- **Trigger:** Learner achieves >= minimum score threshold for badge
- **Process:**
  1. Find all badge templates linked to the exam ID
  2. Filter badges where `minimumScore <= achieved percentage`
  3. Award the highest-level badge (highest minimum score)
  4. Check for duplicates (prevent awarding same badge twice)
- **Result:** Badge appears in learner's "My Badges" dashboard

### Scoring Thresholds
- **Passing Score:** 60% (for certification)
- **Badge Score:** Configurable per badge template (default: 60%)

## Setup Requirements

### 1. Create Certification Template
Before exams can award certifications, create a template linked to the exam:

```bash
POST http://localhost:8083/api/certifications-badges/certification-templates
Content-Type: application/json

{
  "title": "Spring Boot Fundamentals Certification",
  "description": "Awarded for passing the Spring Boot exam",
  "examId": 102
}
```

### 2. Create Badge Templates
Create one or more badge templates for different achievement levels:

```bash
# Bronze Badge (60-79%)
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json

{
  "name": "Spring Boot Bronze Badge",
  "description": "Awarded for scoring 60% or higher",
  "examId": 102,
  "minimumScore": 60.0
}

# Silver Badge (80-89%)
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json

{
  "name": "Spring Boot Silver Badge",
  "description": "Awarded for scoring 80% or higher",
  "examId": 102,
  "minimumScore": 80.0
}

# Gold Badge (90-100%)
POST http://localhost:8083/api/certifications-badges/badge-templates
Content-Type: application/json

{
  "name": "Spring Boot Gold Badge",
  "description": "Awarded for scoring 90% or higher",
  "examId": 102,
  "minimumScore": 90.0
}
```

## Testing Scenarios

### Scenario 1: Passing with Bronze Badge (65%)
```bash
POST http://localhost:8083/api/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key

{
  "learnerId": 2,
  "examId": 102,
  "score": 65.0,
  "maxScore": 100.0
}
```

**Expected Result:**
- Certification awarded ✅
- Bronze badge awarded ✅

### Scenario 2: Passing with Silver Badge (85%)
```bash
POST http://localhost:8083/api/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key

{
  "learnerId": 2,
  "examId": 102,
  "score": 85.0,
  "maxScore": 100.0
}
```

**Expected Result:**
- Certification awarded ✅
- Silver badge awarded ✅ (highest eligible badge)

### Scenario 3: Failing (45%)
```bash
POST http://localhost:8083/api/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key

{
  "learnerId": 2,
  "examId": 102,
  "score": 45.0,
  "maxScore": 100.0
}
```

**Expected Result:**
- No certification awarded ❌
- No badge awarded ❌

### Scenario 4: Perfect Score with Gold Badge (100%)
```bash
POST http://localhost:8083/api/exam-integration/process-exam-result
Content-Type: application/json
X-Internal-Api-Key: exam-service-dev-key

{
  "learnerId": 2,
  "examId": 102,
  "score": 100.0,
  "maxScore": 100.0
}
```

**Expected Result:**
- Certification awarded ✅
- Gold badge awarded ✅ (highest eligible badge)

## Error Responses

### 400 Bad Request
Invalid input data (missing required fields, negative scores, etc.)

```json
{
  "timestamp": "2026-02-25T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/exam-integration/process-exam-result"
}
```

### 401 Unauthorized
Missing or invalid API key

```json
{
  "timestamp": "2026-02-25T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid API key",
  "path": "/api/exam-integration/process-exam-result"
}
```

### 500 Internal Server Error
Server-side error during processing

```json
{
  "timestamp": "2026-02-25T14:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error processing exam result",
  "path": "/api/exam-integration/process-exam-result"
}
```

## Integration Flow

```
Exam Service                    Certification-Badge Service
     |                                      |
     |  1. Learner completes exam          |
     |------------------------------------->|
     |                                      |
     |  2. POST /process-exam-result       |
     |     (learnerId, examId, score)      |
     |------------------------------------->|
     |                                      |
     |                                      | 3. Calculate percentage
     |                                      | 4. Check if passed (>= 60%)
     |                                      | 5. Find certification template
     |                                      | 6. Award certification
     |                                      | 7. Find eligible badges
     |                                      | 8. Award highest badge
     |                                      |
     |  9. Return result                   |
     |<-------------------------------------|
     |                                      |
     | 10. Notify learner (optional)       |
     |                                      |
```

## Database Schema

### certification_template
- `id`: Primary key
- `title`: Certification title
- `description`: Description
- `exam_id`: Link to exam (nullable)
- `created_at`: Creation timestamp
- `updated_at`: Update timestamp

### badge_template
- `id`: Primary key
- `name`: Badge name
- `description`: Description
- `exam_id`: Link to exam (nullable)
- `minimum_score`: Minimum percentage required (default: 60.0)
- `created_at`: Creation timestamp
- `updated_at`: Update timestamp

### earned_certification
- `id`: Primary key
- `learner_id`: Learner who earned it
- `certification_template_id`: Template reference
- `issue_date`: Date awarded
- `expiry_date`: Expiration date (2 years from issue)

### earned_badge
- `id`: Primary key
- `learner_id`: Learner who earned it
- `badge_template_id`: Template reference
- `award_date`: Date awarded
- `awarded_by`: User ID (0 = system/automatic)

## Notes
- Duplicate prevention: Learners cannot earn the same certification or badge twice
- Badge selection: When multiple badges are eligible, the highest-level badge is awarded
- Certification validity: Certifications are valid for 2 years from issue date
- System awards: Automatically awarded certifications/badges have `awardedBy = 0`
