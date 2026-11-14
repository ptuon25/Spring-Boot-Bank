# Tuon Bank

A digital bank backend built with **Java 25** and **Spring Boot**, focused on a consistent domain model, secure authentication/authorization, and clean layered architecture.

> **Status:** under active development. See the [roadmap](#roadmap) for what is done, in progress and planned.

## Highlights

- **Stateless JWT authentication** (HS256) for clients and employees, with role-based and ownership-based authorization.
- **Method-level security** (`@PreAuthorize`) so rules live next to the business logic, not only in HTTP filters.
- **Logical deletion** to preserve financial and audit history.
- **Consistent error contract** (`StandardError`) mapped from business, validation and database exceptions.
- **Layered tests**: repository, service, controller and security integration tests.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, OAuth2 Resource Server, Validation) |
| Persistence | PostgreSQL (main), H2 in-memory (test profile) |
| Build | Maven (wrapper included) |
| Tests | JUnit 5, MockMvc, Spring Security Test |

## Architecture

```
controller  →  service  →  repository  →  database
    │             │
    DTOs     business rules + @PreAuthorize
```

- **Controllers** expose DTOs only; entities are never returned or accepted directly.
- **Services** hold validation, normalization (e-mail, document) and business rules.
- **Authorization components** (`clientAuthorization`, `employeeAuthorization`, `companyAuthorization`) answer "may this principal do this?" and are reused by `@PreAuthorize`.
- **Inheritance**: `Person` → `Client` / `Employee`, and `Account` → checking, savings, joint, business (JPA `JOINED`).

### Roles

| Role | Scope |
|---|---|
| `CLIENT` | Own data and own operations only |
| `ASSOCIATE` | Employee with search access |
| `MANAGER` | Manages clients, companies and members |
| `ADMIN` | Full administrative access, including employees and audits |

More details in [SECURITY.md](SECURITY.md).

## Getting started

**Prerequisites:** JDK 25.

**Run with the test profile** (in-memory H2, sample data, H2 console enabled):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

**Run with PostgreSQL:** copy `.env.example` to `.env`, fill in the values (generate a unique `JWT_SECRET`), then:

```bash
./mvnw spring-boot:run
```

**Run the tests:**

```bash
./mvnw test
```

### Try it

```bash
# 1. Log in (test profile seeds sample users, see TestConfig)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice_johnson@tbank.com","password":"manager-test-password"}'

# 2. Use the returned token
curl http://localhost:8080/clients -H "Authorization: Bearer <accessToken>"
```

## API overview

| Resource | Examples |
|---|---|
| Auth | `POST /auth/login` |
| Clients | `POST /clients`, `GET /clients/{id}`, `PATCH /clients/{id}/block`, `PATCH /clients/{id}/password` |
| Employees | `POST /employees`, `PATCH /employees/{id}/role`, `PATCH /employees/{id}/terminate` |
| Companies | `POST /companies`, `GET /companies/search/status` |
| Company members | `POST /companies/{id}/members`, `PATCH .../revoke` |

The full access matrix is in [SECURITY.md](SECURITY.md).

## Roadmap

**Done**
- [x] Clients, employees and companies (CRUD, status lifecycle, logical deletion)
- [x] Company memberships with roles (`ADMIN`, `OPERATOR`, `VIEWER`)
- [x] JWT authentication and role/ownership authorization
- [x] Password change and reset flows
- [x] Repository, service, controller and security tests

**In progress**
- [ ] Bank accounts: checking, savings, joint and business (DTOs, repositories, authorization, services)

**Planned**
- [ ] Deposits, withdrawals, transfers, Pix and scheduled transactions
- [ ] Transaction reversal with manager/admin approval
- [ ] Investments (CDB, Treasury, debentures, LCI/LCA; CDI/IPCA rates)
- [ ] Audit trail and CSV reports
- [ ] OpenAPI/Swagger documentation
- [ ] Docker setup (PostgreSQL)
- [ ] Event-driven messaging (Kafka) and a Python risk-scoring service for suspicious transfers
- [ ] Consultative AI assistant (Spring AI + Ollama) that respects the caller's permissions
- [ ] Refresh tokens and token revocation

## Known limitations

- Access tokens live 15 minutes and are not revoked when a user is blocked or changes password.
- CORS is not configured and CSRF is disabled (stateless API).

## License

To be defined.