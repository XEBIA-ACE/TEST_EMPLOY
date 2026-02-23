# Employee Management Service

A production-ready REST API for managing employee records, built with **Java 21** and **Spring Boot 3**.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start (Docker)](#quick-start-docker)
- [Local Development (without Docker)](#local-development-without-docker)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Features

- Full CRUD for employee records
- Pagination, sorting, department/status filtering, and keyword search
- Input validation with detailed field-level error responses
- Flyway database migrations (PostgreSQL)
- OpenAPI 3 / Swagger UI documentation
- Spring Actuator health and metrics endpoints
- Structured JSON logging
- Multi-stage Docker build with non-root runtime user
- Multi-environment profiles (dev / prod)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     HTTP Clients                        │
└───────────────────────────┬─────────────────────────────┘
                            │
               ┌────────────▼────────────┐
               │   EmployeeController    │  ← REST / JSON (Spring MVC)
               └────────────┬────────────┘
                            │
               ┌────────────▼────────────┐
               │    EmployeeService      │  ← Business logic
               └────────────┬────────────┘
                            │
               ┌────────────▼────────────┐
               │  EmployeeRepository     │  ← Spring Data JPA
               └────────────┬────────────┘
                            │
               ┌────────────▼────────────┐
               │      PostgreSQL         │
               └─────────────────────────┘
```

**Key design decisions:**

| Concern | Approach |
|---|---|
| Persistence | Spring Data JPA + Hibernate |
| Schema migrations | Flyway |
| DTO mapping | MapStruct (compile-time) |
| Validation | Jakarta Bean Validation |
| Error handling | `@RestControllerAdvice` global handler |
| Documentation | SpringDoc OpenAPI 3 |
| Security | Spring Security (stateless; JWT placeholder) |

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9 (or use the bundled `mvnw`) |
| Docker & Docker Compose | 24+ |
| PostgreSQL | 15+ (not required when using Docker) |

---

## Quick Start (Docker)

```bash
# 1. Clone and enter the project
git clone <repo-url>
cd employee-management-service

# 2. Build and start both PostgreSQL and the application
docker compose up --build -d

# 3. Verify services are healthy
docker compose ps

# 4. Open the Swagger UI
open http://localhost:8080/swagger-ui/index.html

# 5. Check the health endpoint
curl http://localhost:8080/actuator/health
```

To stop:

```bash
docker compose down          # stop containers (keep volumes)
docker compose down -v       # stop containers AND remove volumes
```

---

## Local Development (without Docker)

### 1. Start PostgreSQL

You can use Docker just for the database:

```bash
docker run -d \
  --name employee-postgres \
  -e POSTGRES_DB=employee_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env with your database credentials
```

### 3. Run the application

```bash
# Using Maven wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build the JAR and run it directly
./mvnw package -DskipTests
java -jar target/employee-management-service-*.jar --spring.profiles.active=dev
```

---

## Configuration

All sensitive values are read from environment variables. See `.env.example` for the full list.

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP server port |
| `SPRING_PROFILES_ACTIVE` | — | Active profile (`dev` / `prod`) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/employee_db` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `DB_POOL_SIZE` | `10` | HikariCP maximum pool size |
| `APP_SERVER_URL` | `http://localhost:8080` | Base URL shown in OpenAPI spec |
| `SWAGGER_ENABLED` | `true` | Set to `false` in production |

---

## API Reference

Interactive documentation: **`/swagger-ui/index.html`**
OpenAPI spec: **`/v3/api-docs`**

### Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/employees` | List all employees (paginated) |
| `GET` | `/api/v1/employees/{id}` | Get employee by ID |
| `POST` | `/api/v1/employees` | Create a new employee |
| `PUT` | `/api/v1/employees/{id}` | Update an employee (partial) |
| `DELETE` | `/api/v1/employees/{id}` | Delete an employee |
| `GET` | `/api/v1/employees/department/{dept}` | List by department |
| `GET` | `/api/v1/employees/status/{status}` | List by status |
| `GET` | `/api/v1/employees/search?query=…` | Keyword search |

### Pagination Query Parameters

| Parameter | Default | Description |
|---|---|---|
| `page` | `0` | Page number (0-based) |
| `size` | `20` | Items per page |
| `sortBy` | `lastName` | Field to sort by |
| `sortDir` | `asc` | Sort direction (`asc` / `desc`) |

### Employee Status Values

`ACTIVE` · `INACTIVE` · `ON_LEAVE` · `TERMINATED`

### Example: Create an employee

```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@company.com",
    "phone": "+1-555-123-4567",
    "department": "Engineering",
    "jobTitle": "Senior Software Engineer",
    "salary": 105000.00,
    "hireDate": "2022-03-15",
    "status": "ACTIVE"
  }'
```

### Example: Search employees

```bash
curl "http://localhost:8080/api/v1/employees/search?query=engineering&page=0&size=10"
```

### Error response format

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/employees",
  "timestamp": "2024-01-15T10:30:00Z",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "not-an-email",
      "message": "Email must be a valid address"
    }
  ]
}
```

---

## Testing

```bash
# Run all tests
./mvnw test

# Run only unit tests
./mvnw test -pl . -Dtest="*ServiceTest,*ControllerTest"

# Run with coverage report (target/site/jacoco/index.html)
./mvnw verify
```

Tests use **H2 in-memory database** (PostgreSQL-compatible mode) so no external service is required.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/company/employee/
│   │   ├── EmployeeManagementApplication.java   # Entry point
│   │   ├── config/
│   │   │   ├── AuditConfig.java                 # JPA audit timestamps
│   │   │   ├── OpenApiConfig.java               # Swagger metadata
│   │   │   └── SecurityConfig.java              # Spring Security rules
│   │   ├── controller/
│   │   │   └── EmployeeController.java          # REST endpoints
│   │   ├── service/
│   │   │   ├── EmployeeService.java             # Interface
│   │   │   └── impl/EmployeeServiceImpl.java    # Implementation
│   │   ├── repository/
│   │   │   └── EmployeeRepository.java          # Spring Data JPA
│   │   ├── model/entity/
│   │   │   └── Employee.java                    # JPA entity
│   │   ├── dto/
│   │   │   ├── request/                         # CreateEmployeeRequest, UpdateEmployeeRequest
│   │   │   └── response/                        # EmployeeResponse, PagedResponse
│   │   ├── mapper/
│   │   │   └── EmployeeMapper.java              # MapStruct mapper
│   │   └── exception/
│   │       ├── EmployeeNotFoundException.java
│   │       ├── DuplicateEmailException.java
│   │       ├── ErrorResponse.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.yml                      # Base config
│       ├── application-dev.yml                  # Dev overrides
│       ├── application-prod.yml                 # Prod overrides
│       └── db/migration/
│           ├── V1__create_employees_table.sql
│           └── V2__seed_sample_employees.sql
└── test/
    ├── java/com/company/employee/
    │   ├── controller/EmployeeControllerTest.java
    │   └── service/EmployeeServiceTest.java
    └── resources/
        └── application-test.yml
```
