# Certification and Badge Management Service

## Overview

The Certification and Badge Management Service is a Spring Boot microservice that enables the SMARTEK learning platform to issue, manage, and display digital certifications and badges.

## Technical Stack

- **Spring Boot**: 3.2.0
- **Java**: 17
- **Database**: MySQL (smartek_db)
- **Service Discovery**: Eureka Client
- **Testing**: JUnit 5, jqwik (Property-Based Testing), H2 (in-memory for tests)

## Configuration

- **Port**: 8082
- **Eureka Server**: http://localhost:8761/eureka/
- **Database**: jdbc:mysql://localhost:3306/smartek_db

## Running the Service

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL server running on localhost:3306
- Eureka server running on localhost:8761

### Build and Run

```bash
# Build the project
mvn clean install

# Run the service
mvn spring-boot:run
```

## API Endpoints

All endpoints are prefixed with `/api/certifications-badges` and accessed through the API Gateway (port 8080).

### Badge Template Management
- `POST /badge-templates` - Create badge template
- `PUT /badge-templates/{id}` - Update badge template
- `GET /badge-templates` - Get all badge templates
- `GET /badge-templates/{id}` - Get badge template by ID
- `DELETE /badge-templates/{id}` - Delete badge template

### Certification Template Management
- `POST /certification-templates` - Create certification template
- `PUT /certification-templates/{id}` - Update certification template
- `GET /certification-templates` - Get all certification templates
- `GET /certification-templates/{id}` - Get certification template by ID
- `DELETE /certification-templates/{id}` - Delete certification template

### Award Operations
- `POST /earned-badges` - Award badge to learner
- `POST /earned-badges/bulk` - Award badge to multiple learners
- `POST /earned-certifications` - Award certification to learner
- `POST /earned-certifications/bulk` - Award certification to multiple learners

### Query Operations
- `GET /earned-badges/learner/{learnerId}` - Get earned badges for learner
- `GET /earned-certifications/learner/{learnerId}` - Get earned certifications for learner

### Statistics
- `GET /statistics/badges` - Get badge statistics
- `GET /statistics/certifications` - Get certification statistics
- `GET /statistics/learners/{learnerId}` - Get learner statistics

## Testing

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only property-based tests
mvn test -Dtest=*PropertyTest
```

## Architecture

The service follows a layered architecture:
- **Controllers**: REST API endpoints
- **Services**: Business logic
- **Repositories**: Data access layer
- **Security**: JWT authentication and role-based authorization
