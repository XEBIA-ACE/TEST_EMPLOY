# Employee Management Service

A production-ready REST API for managing employees and organizational departments, built with **Java 17** and **Spring Boot 3.2**.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Security](#security)
- [Observability](#observability)
- [Testing](#testing)
- [Docker](#docker)

---

## Architecture Overview

```
┌───────────────────────────────────────────────────┐
│                   HTTP Clients                    │
└──────────────────────┬────────────────────────────┘
                       │
          ┌────────────▼────────────┐
          │     REST Controllers    │  ← Input validation, HTTP mapping
          └────────────┬────────────┘
                       │
          ┌────────────▼────────────┐
          │     Service Layer       │  ← Business logic, transactions
          └────────────┬────────────┘
                       │
          ┌────────────▼────────────┐
          │   Repository Layer      │  ← JPA / Spring Data queries
          └────────────┬────────────┘
                       │
          ┌────────────▼────────────┐
          │     PostgreSQL 15       │  ← Managed by Flyway migrations
          └─────────────────────────┘
```

**Key design decisions:**
- **Clean Architecture** — strict layer separation; controllers never touch repositories directly.
- **Soft Delete** — `DELETE /employees/{id}` marks employees as `TERMINATED`; data is never physically removed.
- **MapStruct** — compile-time DTO ↔ entity mapping with zero reflection overhead.
- **JPA Specifications** — dynamic filtering without unsafe string concatenation.
- **Flyway** — versioned, repeatable schema migrations.

---

## Technology Stack

| Component          | Choice                         |
|--------------------|--------------------------------|
| Language           | Java 17                        |
| Framework          | Spring Boot 3.2                |
| Build              | Maven 3.9                      |
| Database           | PostgreSQL 15                  |
| ORM                | Spring Data JPA / Hibernate 6  |
| Migrations         | Flyway                         |
| DTO Mapping        | MapStruct 1.5                  |
| Validation         | Jakarta Bean Validation        |
| API Docs           | springdoc-openapi (Swagger UI) |
| Metrics            | Micrometer + Prometheus        |
| Security           | Spring Security (JWT-ready)    |
| Testing            | JUnit 5, Mockito, MockMvc      |
| Containerisation   | Docker (multi-stage build)     |

---

## Project Structure

```
employee-management-service/
├── src/
│   ├── main/java/com/example/employee/
│   │   ├── EmployeeManagementApplication.java
│   │   ├── config/
│   │   │   ├── OpenApiConfig.java          # Swagger / OpenAPI setup
│   │   │   ├── RequestLoggingConfig.java   # Per-request logging filter
│   │   │   └── SecurityConfig.java         # Spring Security rules
│   │   ├── controller/
│   │   │   ├── EmployeeController.java     # /api/v1/employees
│   │   │   └── DepartmentController.java   # /api/v1/departments
│   │   ├── dto/
│   │   │   ├── ApiResponse.java            # Uniform response envelope
│   │   │   ├── PagedResponse.java          # Pagination wrapper
│   │   │   ├── EmployeeRequest/Response.java
│   │   │   └── DepartmentRequest/Response.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java # Centralised error handling
│   │   │   ├── ErrorResponse.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── DuplicateResourceException.java
│   │   ├── mapper/
│   │   │   ├── EmployeeMapper.java         # MapStruct interface
│   │   │   └── DepartmentMapper.java
│   │   ├── model/
│   │   │   ├── Employee.java
│   │   │   ├── Department.java
│   │   │   ├── EmployeeStatus.java
│   │   │   └── EmploymentType.java
│   │   ├── repository/
│   │   │   ├── EmployeeRepository.java
│   │   │   ├── DepartmentRepository.java
│   │   │   └── EmployeeSpecification.java  # Dynamic filter builder
│   │   └── service/
│   │       ├── EmployeeService.java        # Interface
│   │       ├── DepartmentService.java
│   │       └── impl/
│   │           ├── EmployeeServiceImpl.java
│   │           └── DepartmentServiceImpl.java
│   └── main/resources/
│       ├── application.yml                 # Base configuration
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_departments_table.sql
│           ├── V2__create_employees_table.sql
│           └── V3__seed_data.sql
├── src/test/
│   ├── java/com/example/employee/
│   │   ├── controller/EmployeeControllerTest.java
│   │   ├── repository/EmployeeRepositoryTest.java
│   │   └── service/EmployeeServiceTest.java
│   └── resources/application-test.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
└── .gitignore
```

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Option A — Docker Compose (recommended)

```bash
# 1. Clone the repository
git clone <repo-url>
cd employee-management-service

# 2. Copy and review environment variables
cp .env.example .env

# 3. Start the full stack (PostgreSQL + application)
docker compose up --build

# The API is now available at http://localhost:8080
# Swagger UI:            http://localhost:8080/swagger-ui.html
# Health check:          http://localhost:8080/actuator/health
```

### Option B — Local Maven (requires PostgreSQL)

```bash
# 1. Start PostgreSQL (or use an existing instance)
docker compose up db -d

# 2. Copy environment variables
cp .env.example .env
# Edit .env with your database credentials

# 3. Build and run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Configuration

All sensitive values are loaded from environment variables. Copy `.env.example` to `.env` and adjust:

| Variable               | Description                        | Default           |
|------------------------|------------------------------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile             | `dev`             |
| `SERVER_PORT`          | HTTP port                          | `8080`            |
| `DB_HOST`              | PostgreSQL hostname                | `localhost`       |
| `DB_PORT`              | PostgreSQL port                    | `5432`            |
| `DB_NAME`              | Database name                      | `employee_db`     |
| `DB_USER`              | Database username                  | `postgres`        |
| `DB_PASSWORD`          | Database password                  | —                 |
| `ADMIN_USER`           | Basic auth username (dev only)     | `admin`           |
| `ADMIN_PASSWORD`       | Basic auth password (dev only)     | —                 |

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

> For the full interactive documentation visit **`/swagger-ui.html`**.

### Employees

| Method | Path                           | Description                          |
|--------|--------------------------------|--------------------------------------|
| GET    | `/employees`                   | List employees (paginated + filters) |
| GET    | `/employees/{id}`              | Get employee by ID                   |
| POST   | `/employees`                   | Create employee                      |
| PUT    | `/employees/{id}`              | Update employee (full replace)       |
| DELETE | `/employees/{id}`              | Soft-delete (sets status=TERMINATED) |
| GET    | `/employees/{id}/direct-reports` | List direct reports                |

#### Query Parameters for `GET /employees`

| Parameter        | Type           | Description                        |
|------------------|----------------|------------------------------------|
| `departmentId`   | Long           | Filter by department               |
| `status`         | EmployeeStatus | ACTIVE / INACTIVE / ON_LEAVE / TERMINATED |
| `employmentType` | EmploymentType | FULL_TIME / PART_TIME / CONTRACT / INTERN |
| `minSalary`      | Decimal        | Minimum salary                     |
| `maxSalary`      | Decimal        | Maximum salary                     |
| `search`         | String         | Free-text search (name, email, title) |
| `page`           | int            | Page number (default: 0)           |
| `size`           | int            | Page size (default: 20)            |
| `sortBy`         | String         | Sort field (default: `lastName`)   |
| `direction`      | String         | `asc` or `desc` (default: `asc`)  |

#### Example Requests

```bash
# List all active full-time engineers, sorted by salary descending
curl -u admin:secret \
  "http://localhost:8080/api/v1/employees?status=ACTIVE&employmentType=FULL_TIME&sortBy=salary&direction=desc"

# Search by name
curl -u admin:secret \
  "http://localhost:8080/api/v1/employees?search=alice"

# Create an employee
curl -u admin:secret -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "jobTitle": "Software Engineer",
    "salary": 90000,
    "hireDate": "2024-03-01",
    "employmentType": "FULL_TIME",
    "departmentId": 1
  }' \
  http://localhost:8080/api/v1/employees
```

### Departments

| Method | Path                  | Description               |
|--------|-----------------------|---------------------------|
| GET    | `/departments`        | List departments          |
| GET    | `/departments/{id}`   | Get department by ID      |
| POST   | `/departments`        | Create department         |
| PUT    | `/departments/{id}`   | Update department         |
| DELETE | `/departments/{id}`   | Delete (if no employees)  |

### Response Envelope

All responses are wrapped in a standard envelope:

```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": { ... },
  "timestamp": "2024-03-01T10:00:00"
}
```

Error responses follow the same structure with additional detail:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/employees",
  "timestamp": "2024-03-01T10:00:00",
  "fieldErrors": [
    { "field": "email", "rejectedValue": "bad", "message": "Email must be a valid address" }
  ]
}
```

---

## Database Schema

```
departments
├── id              BIGSERIAL PK
├── name            VARCHAR(100) UNIQUE NOT NULL
├── description     VARCHAR(500)
├── budget          DOUBLE PRECISION NOT NULL
├── location        VARCHAR(200)
├── created_at      TIMESTAMP
└── updated_at      TIMESTAMP

employees
├── id              BIGSERIAL PK
├── first_name      VARCHAR(100) NOT NULL
├── last_name       VARCHAR(100) NOT NULL
├── email           VARCHAR(255) UNIQUE NOT NULL
├── phone           VARCHAR(20)
├── job_title       VARCHAR(150) NOT NULL
├── salary          NUMERIC(12,2) NOT NULL
├── hire_date       DATE NOT NULL
├── date_of_birth   DATE
├── status          VARCHAR(20) CHECK (ACTIVE|INACTIVE|ON_LEAVE|TERMINATED)
├── employment_type VARCHAR(20) CHECK (FULL_TIME|PART_TIME|CONTRACT|INTERN)
├── department_id   FK → departments.id
├── manager_id      FK → employees.id (self-referential)
├── address         VARCHAR(500)
├── city            VARCHAR(100)
├── country         VARCHAR(100)
├── created_at      TIMESTAMP
└── updated_at      TIMESTAMP
```

Migrations are managed by **Flyway** and applied automatically on startup.

---

## Security

- **Spring Security** is configured for stateless (JWT-compatible) operation.
- In `dev` profile, HTTP Basic Auth is enabled for quick testing.
- In `prod` profile, replace the Basic auth block in `SecurityConfig` with a JWT filter.
- All endpoints under `/api/v1/**` require authentication.
- Public paths: `/actuator/health`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**`.

---

## Observability

| Endpoint                    | Description                          |
|-----------------------------|--------------------------------------|
| `GET /actuator/health`      | Liveness / readiness (Kubernetes-ready) |
| `GET /actuator/info`        | Application metadata                 |
| `GET /actuator/metrics`     | JVM and application metrics          |
| `GET /actuator/prometheus`  | Prometheus-format metrics scrape     |

Structured logging is configured per environment:
- **Dev** — human-readable console output with DEBUG level.
- **Prod** — JSON-structured output suitable for log aggregation (ELK/Splunk).

---

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw verify

# Run a specific test class
./mvnw test -Dtest=EmployeeServiceTest
```

Test suite coverage:

| Layer       | Test type             | File                        |
|-------------|-----------------------|-----------------------------|
| Service     | Unit (Mockito)        | `EmployeeServiceTest`       |
| Controller  | Web MVC Slice (MockMvc)| `EmployeeControllerTest`   |
| Repository  | JPA Slice (H2)        | `EmployeeRepositoryTest`    |

---

## Docker

### Build the image

```bash
docker build -t employee-management-service:latest .
```

### Run with Docker Compose

```bash
# All services
docker compose up --build

# Database only (run the app locally with Maven)
docker compose up db

# Include optional pgAdmin GUI
docker compose --profile tools up
```

### Environment overrides

```bash
docker compose run -e DB_PASSWORD=mypassword app
```
