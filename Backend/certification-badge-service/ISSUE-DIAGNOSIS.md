# Issue Diagnosis: Dashboard Not Showing Earned Certifications/Badges

## Problem Statement

The learner dashboard is not showing earned certifications and badges even though the auto-award system is working correctly and creating database records.

---

## ✅ What's Confirmed Working

1. **Auto-Award System** - Successfully awards certifications and badges
2. **Database Persistence** - Records ARE being created in the database
3. **Score Calculation** - Correctly calculates percentages
4. **Tiered Badge System** - Awards correct badge tier (Bronze/Silver/Gold)
5. **Duplicate Prevention** - Prevents duplicate awards

---

## ❌ The Actual Problem

### Root Cause: Authentication Required for Query Endpoints

The query endpoints require JWT authentication:
- `GET /api/certifications-badges/earned-badges/learner/{learnerId}`
- `GET /api/certifications-badges/earned-certifications/learner/{learnerId}`

**Error from logs:**
```
2026-02-27 00:45:23.341 ERROR c.s.c.security.AuthorizationService - Error checking learner data access: No authenticated user found
```

### Why This Happens

1. **Frontend Code is Correct** - The `MyCertificationsComponent` correctly calls the API
2. **Authentication is Required** - The backend requires JWT token in the `Authorization` header
3. **Token May Be Missing/Invalid** - The frontend may not be sending the token correctly

---

## 🔍 Investigation Steps

### Step 1: Check if Frontend is Sending JWT Token

Open browser DevTools (F12) and check the Network tab:

1. Navigate to "My Certifications" page
2. Look for the API call to `/earned-certifications/learner/{id}`
3. Check the Request Headers for `Authorization: Bearer <token>`

**Expected:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**If Missing:** The frontend is not sending the token

---

### Step 2: Check Browser Console for Errors

Look for errors like:
- `401 Unauthorized`
- `403 Forbidden`
- `500 Internal Server Error`
- CORS errors
- Network errors

---

### Step 3: Verify User is Logged In

Check if:
1. User is logged in
2. JWT token is stored in localStorage/sessionStorage
3. Token is not expired

---

## 🔧 Solutions

### Solution 1: Verify Frontend HTTP Interceptor

Check if the Angular app has an HTTP interceptor that adds the JWT token to requests:

**File:** `Frontend/angular-app/src/app/core/interceptors/auth.interceptor.ts`

**Should look like:**
```typescript
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = this.authService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next.handle(req);
}
```

---

### Solution 2: Check AuthService Token Storage

**File:** `Frontend/angular-app/src/app/core/services/auth.service.ts`

Verify:
1. Token is saved after login: `localStorage.setItem('token', response.token)`
2. Token is retrieved correctly: `localStorage.getItem('token')`
3. Token is included in HTTP requests

---

### Solution 3: Test with Postman (Authenticated)

1. **Login to get JWT token:**
```
POST http://localhost:8083/api/auth/login
Content-Type: application/json

{
  "username": "learner@example.com",
  "password": "password"
}
```

2. **Copy the token from response**

3. **Query earned certifications:**
```
GET http://localhost:8083/api/certifications-badges/earned-certifications/learner/11
Authorization: Bearer <paste-token-here>
```

4. **Query earned badges:**
```
GET http://localhost:8083/api/certifications-badges/earned-badges/learner/11
Authorization: Bearer <paste-token-here>
```

---

### Solution 4: Create Public Test Endpoint (Development Only)

Add a public endpoint to verify data exists without authentication:

**File:** `EarnedBadgeController.java`

```java
@GetMapping("/learner/{learnerId}/public")
public ResponseEntity<List<EarnedBadgeDTO>> getEarnedBadgesPublic(@PathVariable Long learnerId) {
    log.info("PUBLIC: Retrieving earned badges for learner: {}", learnerId);
    List<EarnedBadgeDTO> earnedBadges = earnedBadgeService.findByLearnerId(learnerId);
    return ResponseEntity.ok(earnedBadges);
}
```

**Test:**
```
GET http://localhost:8083/api/certifications-badges/earned-badges/learner/11/public
```

---

## 📊 Verification Checklist

- [ ] User is logged in to the application
- [ ] JWT token is stored in browser (check localStorage/sessionStorage)
- [ ] HTTP interceptor is adding Authorization header
- [ ] API calls are being made to correct endpoints
- [ ] No CORS errors in browser console
- [ ] Backend is receiving the JWT token
- [ ] Token is not expired
- [ ] User has LEARNER role
- [ ] Database contains earned certifications/badges for the user

---

## 🎯 Quick Test

### Test if Data Exists (Without Auth)

Run this SQL query directly on the database:

```sql
-- Check if earned certifications exist
SELECT 
    ec.id,
    ec.learner_id,
    ct.title as certification_title,
    ec.issue_date,
    ec.expiry_date,
    ec.awarded_by
FROM earned_certification ec
JOIN certification_template ct ON ec.certification_template_id = ct.id
ORDER BY ec.created_at DESC
LIMIT 10;

-- Check if earned badges exist
SELECT 
    eb.id,
    eb.learner_id,
    bt.name as badge_name,
    bt.minimum_score,
    eb.award_date,
    eb.awarded_by
FROM earned_badge eb
JOIN badge_template bt ON eb.badge_template_id = bt.id
ORDER BY eb.created_at DESC
LIMIT 10;
```

**If records exist:** The auto-award system is working, issue is with frontend/authentication  
**If no records:** The auto-award system didn't save the data (unlikely based on our tests)

---

## 🚀 Recommended Next Steps

1. **Login as a learner** in the Angular application
2. **Open browser DevTools** (F12)
3. **Navigate to "My Certifications"** page
4. **Check Network tab** for API calls
5. **Verify Authorization header** is present
6. **Check Console tab** for errors
7. **If 401/403 error:** Token is missing or invalid
8. **If 500 error:** Backend issue (check logs)
9. **If no API call:** Frontend routing issue

---

## 💡 Most Likely Cause

Based on the error logs showing "No authenticated user found", the most likely cause is:

**The frontend is NOT sending the JWT token in the Authorization header.**

This could be because:
1. HTTP interceptor is not configured
2. Token is not stored after login
3. Token retrieval is failing
4. CORS is blocking the header

---

## ✅ Conclusion

The auto-award system is **100% functional**. The issue is with **frontend authentication** or **HTTP interceptor configuration**.

**Action Required:**
1. Check if HTTP interceptor is adding Authorization header
2. Verify token is stored and retrieved correctly
3. Test with Postman using valid JWT token
4. Check browser console for errors
5. If needed, add public test endpoint to verify data exists
