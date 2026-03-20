# MaintainX — Network Maintenance Window Scheduling System

A Spring Boot REST API for planning and managing network maintenance windows across telecom/IT infrastructure. Engineers
submit maintenance requests, approvers review and approve or reject them, and admins manage network elements and audit
logs — all secured with JWT-based authentication.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Default Users](#default-users)
- [Running the Application](#running-the-application)
- [Accessing the Application](#accessing-the-application)
- [API Overview](#api-overview)
- [Authentication](#authentication)
- [Using MySQL](#using-mysql)
- [Known Seed Data](#known-seed-data)

---

## Tech Stack

| Layer           | Technology                         |
|-----------------|------------------------------------|
| Language        | Java 17                            |
| Framework       | Spring Boot 4.x                    |
| Security        | Spring Security + JWT (jjwt 0.13)  |
| Persistence     | Spring Data JPA + Hibernate        |
| Database (dev)  | H2 (embedded, file-based)          |
| Database (prod) | MySQL                              |
| Validation      | Jakarta Bean Validation            |
| API Docs        | SpringDoc OpenAPI (Swagger UI)     |
| Build           | Maven                              |
| Testing         | JUnit 5, MockMvc, Karate, Selenium |
| Code Coverage   | JaCoCo (80% line minimum)          |
| CI              | Jenkins                            |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/tus/maintainx/
│   │   ├── MaintainXApplication.java       # Entry point
│   │   ├── config/                         # JWT filter, JWT utils, Security config
│   │   ├── controller/                     # REST controllers
│   │   ├── service/                        # Business logic
│   │   ├── repository/                     # Spring Data JPA interfaces
│   │   ├── entity/                         # JPA entities
│   │   ├── dto/                            # Request/response DTOs
│   │   ├── enums/                          # AuditAction, AuditEntityType, ExecutionStatus
│   │   ├── exception/                      # Custom exceptions
│   │   ├── validation/                     # MaintenanceWindowValidator
│   │   └── apihandler/                     # GlobalExceptionHandler
│   └── resources/
│       ├── application.properties          # App configuration
│       ├── data.sql                        # Seed data (users, elements, windows)
│       └── static/                         # Frontend (index.html, script.js, styles.css)
└── test/
    ├── java/com/tus/maintainx/
    │   ├── controller/                     # MockMvc unit tests
    │   ├── service/                        # Service unit tests
    │   ├── integration/                    # Spring integration tests + Karate API tests
    │   └── E2E/                            # Selenium end-to-end tests
    └── resources/
        ├── application-test.properties     # In-memory H2 for tests
        └── karate/                         # Karate .feature files
```

---

## Prerequisites

Make sure the following are installed before running the project:

- Java 17 or higher
- Maven
- Git
- MySQL

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/MadhuriRudrakshawar/MaintainX.git
cd MaintainX
```

### 2. Build the project

```bash
mvn clean package -DskipTests
```

This compiles the code and packages it into a JAR file at `target/maintainx-0.0.1-SNAPSHOT.jar`.

### 3. Run the application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/maintainx-0.0.1-SNAPSHOT.jar
```

The server starts on **port 8081** by default.

---

## Default Users

The database is seeded automatically on first startup via `data.sql`. Three users are created:

| Email            | Password   | Role     |
|------------------|------------|----------|
| `admin@mail.com` | `admin123` | ADMIN    |
| `appr@mail.com`  | `apr123`   | APPROVER |
| `eng@mail.com`   | `eng123`   | ENGINEER |

> Passwords are stored as BCrypt hashes. The plain-text passwords above match the hashes in `data.sql`.

---

## Running the Application

### Option A — Maven (recommended for development)

```bash
mvn spring-boot:run
```

### Option B — JAR file

```bash
mvn clean package -DskipTests
java -jar target/maintainx-0.0.1-SNAPSHOT.jar
```

### Option C — Your IDE

Open the project in IntelliJ IDEA or Eclipse, locate `MaintainXApplication.java`, and click **Run**.

---

## Accessing the Application

Once running, the following URLs are available:

| URL                                           | Description                       |
|-----------------------------------------------|-----------------------------------|
| `http://localhost:8081`                       | Web frontend (login page)         |
| `http://localhost:8081/swagger-ui/index.html` | Swagger UI — interactive API docs |
| `http://localhost:8081/v3/api-docs`           | Raw OpenAPI JSON spec             |

---

## API Overview

All API endpoints are prefixed with `/api/v1/`. Most endpoints require a valid JWT token in the `Authorization` header (
see [Authentication](#authentication) below).

### Authentication

| Method | Endpoint              | Access | Description                        |
|--------|-----------------------|--------|------------------------------------|
| POST   | `/api/v1/auth/login`  | Public | Login and receive a JWT token      |
| POST   | `/api/v1/auth/logout` | Public | Logout (client-side token discard) |

### Maintenance Windows

| Method | Endpoint                                   | Description                         |
|--------|--------------------------------------------|-------------------------------------|
| GET    | `/api/v1/maintenance-windows`              | Get all maintenance windows         |
| GET    | `/api/v1/maintenance-windows/{id}`         | Get one maintenance window by ID    |
| POST   | `/api/v1/maintenance-windows`              | Create a new maintenance window     |
| PUT    | `/api/v1/maintenance-windows/{id}`         | Update an existing window           |
| DELETE | `/api/v1/maintenance-windows/{id}`         | Delete a window                     |
| PATCH  | `/api/v1/maintenance-windows/{id}/approve` | Approve a pending window            |
| PATCH  | `/api/v1/maintenance-windows/{id}/reject`  | Reject a pending window with reason |

### Network Elements

| Method | Endpoint                                   | Description                    |
|--------|--------------------------------------------|--------------------------------|
| GET    | `/api/v1/network-elements`                 | Get all network elements       |
| GET    | `/api/v1/network-elements/{id}`            | Get one element by ID          |
| POST   | `/api/v1/network-elements`                 | Create a network element       |
| PUT    | `/api/v1/network-elements/{id}`            | Update a network element       |
| DELETE | `/api/v1/network-elements/{id}`            | Delete a network element       |
| PATCH  | `/api/v1/network-elements/{id}/activate`   | Set element status to ACTIVE   |
| PATCH  | `/api/v1/network-elements/{id}/deactivate` | Set element status to DEACTIVE |

### Audit Logs

| Method | Endpoint                                          | Access          | Description                                       |
|--------|---------------------------------------------------|-----------------|---------------------------------------------------|
| GET    | `/api/v1/audit-logs`                              | ADMIN, APPROVER | All audit log records                             |
| GET    | `/api/v1/audit-logs/type/{entityType}`            | ADMIN, APPROVER | Filter by entity type (e.g. `MAINTENANCE_WINDOW`) |
| GET    | `/api/v1/audit-logs/type/{entityType}/{entityId}` | ADMIN, APPROVER | Logs for a specific entity                        |

### Analytics

| Method | Endpoint                      | Description                                 |
|--------|-------------------------------|---------------------------------------------|
| GET    | `/api/v1/analytics/dashboard` | Dashboard summary (status counts, timeline) |

---

## Using MySQL

### 1. Create the database

```sql
CREATE DATABASE IF NOT EXISTS maintainx_db;
USE maintainx_db;
```

### 2. Set environment variables before starting the app

**Windows (Command Prompt):**

```cmd
set DB_URL=jdbc:mysql://localhost:3306/maintainx_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USERNAME=your_mysql_user
set DB_PASSWORD=your_mysql_password
set DB_DRIVER=com.mysql.cj.jdbc.Driver
set DB_DIALECT=org.hibernate.dialect.MySQLDialect
set JWT_SECRET=8uV3pQ9mW2xK7nZ5cR1tY6aS0dF4gJ8pL3nV7qW

mvn spring-boot:run
```

Hibernate will create the schema automatically on first start (`ddl-auto=update`). Seed data in `data.sql` is applied
once using `INSERT ... WHERE NOT EXISTS` guards, so it is safe to restart.

---

## Known Seed Data

On first startup, `data.sql` inserts the following seed data if it does not already exist:

- **3 users** — one per role (Admin, Approver, Engineer)
- **25 network elements** — across Dublin, Cork, Galway, Donegal, and Nationwide regions with types including
  CORE_ROUTER, EDGE_SWITCH, ACCESS_SWITCH, AGGREGATION_SWITCH, and BACKBONE_FIREWALL
- **25 maintenance windows** — a mix of PENDING, APPROVED, and REJECTED statuses with various network elements assigned

This gives you a fully populated dataset to explore immediately after startup.
