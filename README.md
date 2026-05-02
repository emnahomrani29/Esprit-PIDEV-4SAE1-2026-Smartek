# Sponsor & Contract Management Module

## Overview

This module manages sponsor contracts and sponsorships within the Smartek training platform. It provides budget management, approval workflows, and automated lifecycle management.

## Architecture

### Backend (Spring Boot)
- **Port:** 8082
- **Base URL:** http://localhost:8082/api/v1
- **Database:** MySQL (training_platform)

### Frontend (Angular)
- **Port:** 4200
- **Base URL:** http://localhost:4200

## Key Features

### Budget Management
- Real-time budget tracking
- Budget states: Available, Reserved, Spent
- Warning levels at 80% and 90% usage
- Automatic budget updates on approval/rejection

### Approval Workflow
- Sponsorship states: PENDING → APPROVED/REJECTED → COMPLETED
- Admin approval required
- Email notifications (console logs)
- Rejection with reason

### Tier-Based Pricing
- LOGO: 500€ minimum
- FEATURED: 1,500€ minimum
- TITLE: 3,000€ minimum

### Automated Lifecycle
- Daily contract status updates (2 AM)
- Automatic sponsorship completion (3 AM)
- Budget threshold monitoring (4 AM)

## API Endpoints

### Budget
```
GET  /api/v1/budget/available/{contractId}
GET  /api/v1/budget/summary/contract/{contractId}
GET  /api/v1/budget/summary/sponsor/{sponsorId}
```

### Workflow
```
POST /api/v1/workflow/sponsorships/{id}/approve
POST /api/v1/workflow/sponsorships/{id}/reject
POST /api/v1/workflow/sponsorships/{id}/cancel
GET  /api/v1/workflow/sponsorships/pending
GET  /api/v1/workflow/sponsorships/pending/count
GET  /api/v1/workflow/sponsorships/status-summary/{sponsorId}
```

### Sponsorships
```
POST   /api/v1/sponsorships
GET    /api/v1/sponsorships
GET    /api/v1/sponsorships/{id}
PUT    /api/v1/sponsorships/{id}
DELETE /api/v1/sponsorships/{id}
```

## Running the Application

### Backend
```bash
cd Backend/smartek_sponsor
mvn clean install
mvn spring-boot:run
```

### Frontend
```bash
cd Frontend/angular-app
npm install
ng serve
```

## Technical Stack

### Backend
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- Bean Validation
- Scheduled Tasks

### Frontend
- Angular 17+
- RxJS
- Tailwind CSS
- HttpClient

## Data Flow

1. User interacts with Angular component
2. Component calls Angular service
3. Service makes HTTP request to Spring Boot API
4. Controller validates request (Bean Validation)
5. Service layer applies business logic
6. Repository persists to database
7. Response mapped to DTO
8. JSON response sent to frontend
9. Component updates UI

## Validation Rules

- Contract must be ACTIVE to create sponsorships
- Amount must not exceed available budget
- Amount must meet minimum tier requirements
- Dates must be within contract period
- No overlapping sponsorships for same target

## Error Handling

Global exception handler provides consistent error responses:
- 400: Validation errors, business rule violations
- 404: Resource not found
- 409: Conflict (e.g., overlap)
- 500: Server errors

## Security

- JWT authentication
- Role-based access control
- Admin-only approval endpoints
- Token validation on all requests

