# Bank Card Management System (REST API)

A production-ready, highly secure backend system for managing bank cards, processing internal transfers, and handling user authentication. Built using Spring Boot, Spring Security with stateless JWT tokens, and PostgreSQL.

## 🛠️ Tech Stack & Architecture
* **Java Version:** 17
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security (Stateless JWT Authentication & RBAC)
* **API Approach:** API First / Contract First (Automated DTO and Controller generation via `openapi-generator-maven-plugin`)
* **Database & Migrations:** PostgreSQL, Liquibase
* **Testing:** JUnit 5, Mockito (Unit testing), MockMvc (Integration controller testing)
* **Containerization:** Docker Compose (Infrastructure provisioning)

---

## 🔒 Implemented Security Features (PCI DSS Compliance)
1. **Data Encryption at Rest:** Real card numbers are automatically encrypted before writing to the database using an `AttributeConverter` utilizing `AES/CBC/PKCS5Padding` with a unique cryptographically strong initialization vector (IV) per record.
2. **Data Masking:** Sensitive card numbers are automatically masked (`**** **** **** 1234`) upon JSON serialization using Jackson MixIn. This completely decouples serialization rules from automatically generated openapi source models.
3. **Role-Based Access Control (RBAC):** Layered security defense featuring synchronized URL filters in `SecurityConfig` and method-level `@PreAuthorize` annotations (`ROLE_ADMIN` and `ROLE_USER`). Blocked users are automatically rejected upon authentication.
4. **Performance Tuning:** High-performance JDBC batching for `INSERT` and `UPDATE` operations configured directly inside `application.yml` along with `@EntityGraph` fetch strategies to eliminate N+1 query bottlenecks.

---

## 🚀 Getting Started & Local Setup

### 1. Provision the Database (Docker)
Ensure Docker Desktop is up and running. Navigate to the project root and start the PostgreSQL container:
```bash
docker-compose up -d
```

### 2. Build and Run the Application Locally
Compile the OpenAPI specifications, apply database schemas through Liquibase, and run the Spring Boot server on port `8080`:
```bash
mvn clean spring-boot:run
```

### 3. Run Automated Tests
Execute the entire comprehensive test suite consisting of 10 unit and integration tests (Services + Controllers):
```bash
mvn clean test
```

---

## 📝 Seed Data & Test Accounts
Upon startup, the database is automatically seeded via Liquibase with pre-compiled BCrypt passwords:
1. **System Administrator:** Username: `admin` | Password: `admin` *(Role: `ROLE_ADMIN`)*
2. **Standard User:** Username: `user` | Password: `admin` *(Role: `ROLE_USER`)*

## 🔗 Interactive API Documentation
Once the server is initialized, complete documentation and interactive endpoints are available at:
* **Swagger UI (Interactive Playground):** http://localhost:8080/swagger-ui/index.html
* **OpenAPI Specification (JSON):** http://localhost:8080/v3/api-docs
