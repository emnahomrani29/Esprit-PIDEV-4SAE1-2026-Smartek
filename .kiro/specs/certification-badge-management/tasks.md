# Implementation Plan: Certification and Badge Management System

## Overview

This plan implements a Spring Boot microservice for managing digital certifications and badges in the SMARTEK learning platform. The service runs on port 8082, integrates with Eureka service discovery and API Gateway, validates JWT tokens through the Auth Service, and persists data to MySQL. The implementation follows a layered architecture with REST controllers, service layer, repository layer, and security configuration.

## Tasks

- [x] 1. Set up Spring Boot project structure and dependencies
  - Create Maven/Gradle project with Spring Boot 2.7+ or 3.x
  - Add dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-cloud-starter-netflix-eureka-client, mysql-connector-java, lombok, validation-api
  - Add test dependencies: spring-boot-starter-test, jqwik (net.jqwik:jqwik), h2 database for testing
  - Configure application.yml with server port 8082, database connection, Eureka client settings
  - _Requirements: 8.3, 9.1-9.4_

- [x] 2. Create database entities and repositories
  - [x] 2.1 Create BadgeTemplate entity
    - Define entity with id, name, description, createdAt, updatedAt fields
    - Add validation annotations (@NotBlank, @Size)
    - Add @OneToMany relationship to EarnedBadge
    - _Requirements: 1.1, 9.1, 13.1, 13.3, 13.5_
  
  - [x] 2.2 Create CertificationTemplate entity
    - Define entity with id, title, description, createdAt, updatedAt fields
    - Add validation annotations (@NotBlank, @Size)
    - Add @OneToMany relationship to EarnedCertification
    - _Requirements: 2.1, 9.2, 13.2, 13.4, 13.5_
  
  - [x] 2.3 Create EarnedBadge entity
    - Define entity with id, badgeTemplate, learnerId, awardDate, awardedBy, createdAt fields
    - Add @ManyToOne relationship to BadgeTemplate
    - Add unique constraint on (badge_template_id, learner_id)
    - _Requirements: 3.1, 3.5, 9.3_
  
  - [x] 2.4 Create EarnedCertification entity
    - Define entity with id, certificationTemplate, learnerId, issueDate, expiryDate, certificateUrl, awardedBy, createdAt fields
    - Add @ManyToOne relationship to CertificationTemplate
    - Implement isExpired() transient method
    - Add certificate URL validation
    - _Requirements: 4.1, 4.5, 6.6, 9.4, 10.1, 10.4, 13.6_
  
  - [x] 2.5 Create Spring Data JPA repositories
    - Create BadgeTemplateRepository extending JpaRepository
    - Create CertificationTemplateRepository extending JpaRepository
    - Create EarnedBadgeRepository with custom queries: existsByBadgeTemplateIdAndLearnerId, findByLearnerId, countByBadgeTemplateId
    - Create EarnedCertificationRepository with custom queries: findByLearnerId, countByCertificationTemplateId, countByLearnerIdAndExpiryDateAfter
    - _Requirements: 3.2, 5.1, 6.1, 12.1, 12.2, 12.3_

- [ ]* 2.6 Write property test for template creation round-trip
  - **Property 1: Template Creation Round-Trip**
  - **Validates: Requirements 1.1, 2.1**

- [x] 3. Create DTOs and mappers
  - [x] 3.1 Create request/response DTOs
    - Create BadgeTemplateDTO with validation annotations
    - Create CertificationTemplateDTO with validation annotations
    - Create AwardBadgeRequestDTO and BulkAwardBadgeRequestDTO
    - Create AwardCertificationRequestDTO and BulkAwardCertificationRequestDTO
    - Create EarnedBadgeDTO and EarnedCertificationDTO
    - Create BulkAwardResponseDTO, AwardResultDTO
    - Create statistics DTOs: BadgeStatisticsDTO, CertificationStatisticsDTO, LearnerStatisticsDTO
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 11.1, 11.2, 12.1, 12.2, 12.3, 13.1-13.7_
  
  - [x] 3.2 Create entity-DTO mappers
    - Create BadgeTemplateMapper with toDTO and toEntity methods
    - Create CertificationTemplateMapper with toDTO and toEntity methods
    - Create EarnedBadgeMapper with toDTO method
    - Create EarnedCertificationMapper with toDTO method (include isExpired computation)
    - _Requirements: 5.1, 6.1, 6.6_

- [x] 4. Implement service layer
  - [x] 4.1 Create BadgeTemplateService
    - Implement create, update, findAll, findById, delete methods
    - Add validation for empty names and length constraints
    - Ensure delete preserves earned badges
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 13.1, 13.3, 13.5_
  
  - [x] 4.2 Create CertificationTemplateService
    - Implement create, update, findAll, findById, delete methods
    - Add validation for empty titles and length constraints
    - Ensure delete preserves earned certifications
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 13.2, 13.4, 13.5_
  
  - [x] 4.3 Create EarnedBadgeService
    - Implement awardBadge method with duplicate check
    - Implement bulkAwardBadges with individual processing and partial failure handling
    - Implement findByLearnerId method
    - Add validation for non-existent learner and template
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 11.1, 11.2, 11.3_
  
  - [x] 4.4 Create EarnedCertificationService
    - Implement awardCertification method with date and URL validation
    - Implement bulkAwardCertifications with individual processing and partial failure handling
    - Implement findByLearnerId method with expiration status
    - Add validation for non-existent learner and template
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 6.1, 6.6, 10.1, 10.3, 10.4, 10.5, 11.1, 11.2, 11.3, 13.6_
  
  - [x] 4.5 Create StatisticsService
    - Implement getBadgeStatistics method
    - Implement getCertificationStatistics method
    - Implement getLearnerStatistics method with active certification count
    - _Requirements: 12.1, 12.2, 12.3, 12.5_

- [ ]* 4.6 Write property tests for service layer
  - **Property 2: Template Update Preserves Earned Achievements**
  - **Validates: Requirements 1.2, 2.2**
  - **Property 6: Duplicate Badge Award Rejected**
  - **Validates: Requirements 3.2**
  - **Property 7: Invalid Learner Rejected**
  - **Validates: Requirements 3.3, 4.3**
  - **Property 8: Invalid Template Rejected**
  - **Validates: Requirements 3.4, 4.4**

- [ ]* 4.7 Write unit tests for service layer
  - Test badge template CRUD operations with specific examples
  - Test certification template CRUD operations with specific examples
  - Test badge award with duplicate detection
  - Test certification award with date validation
  - Test bulk operations with mixed success/failure scenarios
  - Test statistics calculations
  - _Requirements: 1.1-1.4, 2.1-2.4, 3.1-3.5, 4.1-4.5, 11.1-11.3, 12.1-12.3_

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement security configuration
  - [x] 6.1 Create JWT authentication filter
    - Implement filter to extract JWT from Authorization header
    - Call Auth Service to validate token and extract user details
    - Set SecurityContext with authenticated user
    - Handle invalid/expired tokens with 401 response
    - _Requirements: 7.1, 7.2_
  
  - [x] 6.2 Create custom UserDetails implementation
    - Create class to hold user ID and roles from JWT
    - Map roles from Auth Service to Spring Security authorities
    - _Requirements: 7.1_
  
  - [x] 6.3 Configure Spring Security
    - Create SecurityConfig class with SecurityFilterChain bean
    - Add JWT filter before UsernamePasswordAuthenticationFilter
    - Configure authorization rules for endpoints
    - Disable CSRF for stateless API
    - Configure exception handling for 401/403 responses
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [x] 6.4 Create authorization service
    - Implement method to check if user can access learner data (learner can only access own data)
    - Implement role-based authorization checks
    - _Requirements: 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

- [ ]* 6.5 Write unit tests for security components
  - Test JWT filter with valid and invalid tokens
  - Test authorization rules for different roles
  - Test learner self-access restrictions
  - _Requirements: 7.1-7.8_

- [x] 7. Create REST controllers
  - [x] 7.1 Create BadgeTemplateController
    - Implement POST /api/certifications-badges/badge-templates (TRAINER, ADMIN)
    - Implement PUT /api/certifications-badges/badge-templates/{id} (TRAINER, ADMIN)
    - Implement GET /api/certifications-badges/badge-templates (ALL)
    - Implement GET /api/certifications-badges/badge-templates/{id} (ALL)
    - Implement DELETE /api/certifications-badges/badge-templates/{id} (TRAINER, ADMIN)
    - Add @PreAuthorize annotations
    - Add validation error handling
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 7.4, 7.6, 7.7, 13.7, 14.2_
  
  - [x] 7.2 Create CertificationTemplateController
    - Implement POST /api/certifications-badges/certification-templates (TRAINER, ADMIN)
    - Implement PUT /api/certifications-badges/certification-templates/{id} (TRAINER, ADMIN)
    - Implement GET /api/certifications-badges/certification-templates (ALL)
    - Implement GET /api/certifications-badges/certification-templates/{id} (ALL)
    - Implement DELETE /api/certifications-badges/certification-templates/{id} (TRAINER, ADMIN)
    - Add @PreAuthorize annotations
    - Add validation error handling
    - _Requirements: 2.1, 2.2, 2.3, 2.5, 7.4, 7.6, 7.7, 13.7, 14.2_
  
  - [x] 7.3 Create EarnedBadgeController
    - Implement POST /api/certifications-badges/earned-badges (TRAINER, ADMIN)
    - Implement POST /api/certifications-badges/earned-badges/bulk (TRAINER, ADMIN)
    - Implement GET /api/certifications-badges/earned-badges/learner/{learnerId} with authorization check
    - Add @PreAuthorize annotations
    - Extract awardedBy from JWT SecurityContext
    - _Requirements: 3.1, 5.1, 5.2, 5.3, 5.4, 7.5, 7.6, 11.1, 11.2_
  
  - [x] 7.4 Create EarnedCertificationController
    - Implement POST /api/certifications-badges/earned-certifications (TRAINER, ADMIN)
    - Implement POST /api/certifications-badges/earned-certifications/bulk (TRAINER, ADMIN)
    - Implement GET /api/certifications-badges/earned-certifications/learner/{learnerId} with authorization check
    - Add @PreAuthorize annotations
    - Extract awardedBy from JWT SecurityContext
    - _Requirements: 4.1, 6.1, 6.2, 6.3, 6.4, 7.5, 7.6, 11.1, 11.2_
  
  - [x] 7.5 Create StatisticsController
    - Implement GET /api/certifications-badges/statistics/badges (ADMIN, RH)
    - Implement GET /api/certifications-badges/statistics/certifications (ADMIN, RH)
    - Implement GET /api/certifications-badges/statistics/learners/{learnerId} (ADMIN, RH)
    - Add @PreAuthorize annotations
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 7.8_

- [ ]* 7.6 Write property tests for controllers
  - **Property 3: Query All Templates Returns Complete Set**
  - **Validates: Requirements 1.3, 2.3**
  - **Property 5: Award Badge Records Complete Metadata**
  - **Validates: Requirements 3.1, 3.5**
  - **Property 9: Award Certification Records Complete Metadata**
  - **Validates: Requirements 4.1, 4.5**
  - **Property 10: Query Earned Badges Returns Complete Set**
  - **Validates: Requirements 5.1, 5.2**
  - **Property 11: Query Earned Certifications Returns Complete Set**
  - **Validates: Requirements 6.1, 6.2**

- [ ]* 7.7 Write unit tests for controllers
  - Test all endpoints with valid requests
  - Test validation error responses (400)
  - Test authorization errors (403)
  - Test resource not found errors (404)
  - Test conflict errors for duplicate awards (409)
  - _Requirements: 1.1-1.5, 2.1-2.5, 3.1-3.5, 4.1-4.5, 5.1-5.4, 6.1-6.6, 12.1-12.4, 14.2-14.5_

- [x] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implement global exception handling
  - [x] 9.1 Create custom exception classes
    - Create DuplicateAwardException for duplicate badge awards
    - Create ResourceNotFoundException for missing templates/learners
    - Create ValidationException for business validation errors
    - Create ServiceException for system errors
    - _Requirements: 3.2, 3.3, 3.4, 9.5, 14.2_
  
  - [x] 9.2 Create global exception handler
    - Create @ControllerAdvice class
    - Handle ValidationException with 400 response
    - Handle DuplicateAwardException with 409 response
    - Handle ResourceNotFoundException with 404 response
    - Handle ServiceException with 500 response
    - Handle MethodArgumentNotValidException for validation errors
    - Include timestamp, status, error, message, details, path in error response
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6_

- [ ]* 9.3 Write unit tests for exception handling
  - Test each exception type returns correct HTTP status
  - Test error response format
  - Test validation error details
  - _Requirements: 14.2-14.6_

- [x] 10. Implement logging and monitoring
  - [x] 10.1 Configure logging
    - Configure Logback with appropriate log levels
    - Add log format with timestamp, level, user ID, operation, details
    - Configure file appenders for production
    - _Requirements: 14.1, 14.7_
  
  - [x] 10.2 Add logging to service layer
    - Log all successful operations at INFO level
    - Log validation errors at WARN level
    - Log system errors at ERROR level with stack traces
    - Include user ID and operation type in all logs
    - _Requirements: 14.1, 14.7_
  
  - [x] 10.3 Add transaction management
    - Add @Transactional annotations to service methods
    - Configure transaction manager
    - Implement savepoint-based partial rollback for bulk operations
    - _Requirements: 11.3_

- [ ]* 10.4 Write property tests for validation
  - **Property 19: Empty Template Name Rejected**
  - **Validates: Requirements 13.1, 13.2**
  - **Property 20: Template Name Length Validation**
  - **Validates: Requirements 13.3, 13.4**
  - **Property 21: Description Length Validation**
  - **Validates: Requirements 13.5**
  - **Property 22: Date Validation**
  - **Validates: Requirements 13.6**
  - **Property 23: Required Fields Validation**
  - **Validates: Requirements 13.7**

- [x] 11. Configure Eureka integration
  - [x] 11.1 Add Eureka client configuration
    - Add @EnableEurekaClient annotation to main application class
    - Configure application name as "certification-badge-service" in application.yml
    - Configure Eureka server URL
    - Configure heartbeat interval (30 seconds)
    - Configure retry settings for registration failures
    - _Requirements: 8.1, 8.2, 8.5_
  
  - [x] 11.2 Add health check endpoint
    - Enable Spring Boot Actuator
    - Configure /actuator/health endpoint
    - Add custom health indicators for database connectivity
    - _Requirements: 8.2_

- [ ]* 11.3 Write integration tests for Eureka
  - Test service registration on startup
  - Test heartbeat mechanism
  - Test retry on registration failure
  - _Requirements: 8.1, 8.2, 8.5_

- [x] 12. Create database migration scripts
  - [x] 12.1 Create Flyway/Liquibase migration scripts
    - Create migration for badge_template table
    - Create migration for certification_template table
    - Create migration for earned_badge table with unique constraint
    - Create migration for earned_certification table
    - Create indexes for performance optimization
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_

- [ ] 13. Implement remaining property-based tests
  - [ ]* 13.1 Write property test for template deletion
    - **Property 4: Template Deletion Preserves Earned Achievements**
    - **Validates: Requirements 1.4, 2.4**
  
  - [ ]* 13.2 Write property test for expiration status
    - **Property 12: Expiration Status Computed Correctly**
    - **Validates: Requirements 6.6**
  
  - [ ]* 13.3 Write property test for certificate URL validation
    - **Property 13: Certificate URL Validation**
    - **Validates: Requirements 10.4, 10.5**
  
  - [ ]* 13.4 Write property test for bulk award processing
    - **Property 14: Bulk Award Processes All Learners**
    - **Validates: Requirements 11.1, 11.2**
  
  - [ ]* 13.5 Write property test for bulk award partial failure
    - **Property 15: Bulk Award Partial Failure Handling**
    - **Validates: Requirements 11.3**
  
  - [ ]* 13.6 Write property tests for statistics accuracy
    - **Property 16: Badge Statistics Accuracy**
    - **Validates: Requirements 12.1**
    - **Property 17: Certification Statistics Accuracy**
    - **Validates: Requirements 12.2**
    - **Property 18: Learner Statistics Accuracy**
    - **Validates: Requirements 12.3**

- [ ] 14. Create integration tests
  - [ ]* 14.1 Write end-to-end API tests
    - Test complete flow: create template → award → query
    - Test JWT authentication with Auth Service
    - Test authorization for different roles
    - Use Testcontainers for MySQL
    - _Requirements: 7.1-7.8_
  
  - [ ]* 14.2 Write performance tests
    - Test bulk award operations complete within 5 seconds for 100 learners
    - Test concurrent request handling
    - Test database query performance for statistics
    - _Requirements: 11.5_

- [x] 15. Final checkpoint - Ensure all tests pass and service is ready
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik
- Unit tests validate specific examples and edge cases
- The service integrates with existing SMARTEK microservices architecture
- All endpoints are secured with JWT authentication and role-based authorization
