# Employee Management Service

A production-ready REST API for managing employee records, built with **Java 17** and **Spring Boot 3**.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Running Tests](#running-tests)
- [Docker](#docker)
- [Project Structure](#project-structure)

---

## Features

- Full CRUD for employee records
- Paginated listing, department/status filtering, and full-text search
- Auto-generated employee numbers (`EMP-000001`)
- JPA audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`)
- Input validation with descriptive error responses
- OpenAPI 3 / Swagger UI documentation
- Flyway database migrations
- HTTP Basic Auth with role-based access control (`ADMIN`, `HR`)
- Structured logging + Spring Actuator health/metrics endpoints
- Multi-stage Docker build with non-root user

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Containerization | Docker (multi-stage), docker-compose |

---

## Architecture

```
┌─────────────────────────────────────────────┐
│              HTTP Clients                   │
└────────────────────┬────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │  EmployeeController │  ← REST layer (validation, routing)
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │  EmployeeService    │  ← Business logic
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │ EmployeeRepository  │  ← Data access (Spring Data JPA)
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │     PostgreSQL      │
          └─────────────────────┘
```

**Package structure:**
- `controller` — REST endpoints, request/response mapping
- `service` — business rules, transaction boundaries
- `repository` — database queries (Spring Data JPA)
- `model` — JPA entities
- `dto` — request/response DTOs
- `mapper` — MapStruct entity ↔ DTO conversion
- `exception` — custom exceptions + global error handler
- `config` — security, OpenAPI, JPA auditing

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included `./mvnw`)
- Docker & docker-compose (for the full stack)

### Run with Docker Compose (recommended)

```bash
# 1. Copy environment template
cp .env.example .env
# Edit .env with your preferred passwords (or leave defaults for local dev)

# 2. Start PostgreSQL + application
docker compose up --build

# The API is now available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Run locally (without Docker)

```bash
# 1. Start PostgreSQL (Docker or local install)
docker run -d --name emp-postgres \
  -e POSTGRES_DB=employee_db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine

# 2. Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/employee_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# 3. Build and run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Configuration

All sensitive values are supplied via environment variables. Copy `.env.example` to `.env`:

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active profile (`dev`, `prod`) |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/employee_db` | JDBC URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `ADMIN_PASSWORD` | `changeme` | Password for `admin` user |
| `HR_PASSWORD` | `changeme` | Password for `hruser` user |

Profile-specific overrides live in `src/main/resources/application-{profile}.yml`.

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

Interactive docs: **`http://localhost:8080/swagger-ui.html`**

### Authentication

All endpoints (except `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`) require HTTP Basic Auth.

**Built-in users:**

| Username | Password (default) | Roles |
|---|---|---|
| `admin` | `changeme` | `ADMIN`, `HR` |
| `hruser` | `changeme` | `HR` |

### Endpoints

#### Create Employee
```
POST /api/v1/employees
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "phone": "+1-555-123-4567",
  "department": "Engineering",
  "jobTitle": "Senior Software Engineer",
  "employmentStatus": "ACTIVE",
  "salary": 95000.00,
  "hireDate": "2023-06-15",
  "dateOfBirth": "1990-04-22"
}

→ 201 Created   — returns the created employee
→ 400 Bad Request — validation errors
→ 409 Conflict  — email already exists
```

#### List Employees (paginated)
```
GET /api/v1/employees?page=0&size=20&sortBy=lastName&direction=asc

→ 200 OK  — paged list of employees
```

#### Get by ID
```
GET /api/v1/employees/{id}

→ 200 OK  — employee object
→ 404 Not Found
```

#### Get by Employee Number
```
GET /api/v1/employees/number/{employeeNumber}
```

#### Filter by Department
```
GET /api/v1/employees/department/{department}
```

#### Filter by Status
```
GET /api/v1/employees/status/{status}
# status ∈ {ACTIVE, INACTIVE, ON_LEAVE, TERMINATED}
```

#### Search
```
GET /api/v1/employees/search?query=engineering

→ 200 OK — employees matching query in name, email, department, or job title
```

#### Update Employee
```
PUT /api/v1/employees/{id}
Content-Type: application/json
# Same body as POST

→ 200 OK  — updated employee
→ 404 Not Found
→ 409 Conflict
```

#### Delete Employee
```
DELETE /api/v1/employees/{id}

→ 204 No Content
→ 404 Not Found
```

#### Health Check
```
GET /actuator/health   → 200 {"status":"UP"}
GET /actuator/info     → application metadata
GET /actuator/metrics  → Micrometer metrics
```

---

## Running Tests

```bash
# Unit + integration tests (H2 in-memory)
./mvnw test

# Skip tests during build
./mvnw package -DskipTests
```

Test categories:
- **`service/`** — unit tests for business logic (Mockito)
- **`controller/`** — MVC slice tests (`@WebMvcTest`)
- **`integration/`** — full Spring context against H2

---

## Docker

### Build image
```bash
docker build -t employee-management-service:latest .
```

### Run container only (external PostgreSQL required)
```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/employee_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  employee-management-service:latest
```

### With pgAdmin (dev tool)
```bash
docker compose --profile tools up
# pgAdmin: http://localhost:5050
```

---

## Project Structure

```
employee-management-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/employeemanagement/
│   │   │   ├── EmployeeManagementApplication.java
│   │   │   ├── config/           # Security, OpenAPI, Audit
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Request/Response DTOs
│   │   │   ├── exception/        # Custom exceptions + global handler
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   ├── model/            # JPA entities
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   └── service/          # Business logic (interface + impl)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/     # Flyway SQL migrations
│   └── test/
│       ├── java/...              # Unit + integration tests
│       └── resources/
│           └── application-test.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
├── .gitignore
└── README.md
```
