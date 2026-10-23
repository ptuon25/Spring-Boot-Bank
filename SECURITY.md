# Authentication and authorization

## Components

- `config.SecurityConfig`: configures BCrypt, the `AuthenticationManager`, HS256 JWT encoding and validation, stateless API access rules, and the optional test-only H2 Console access.
- `security.auth.BankUserDetailsService`: loads clients and employees by e-mail, requires a nonblank password hash, and returns their identity, role, and active status.
- `security.auth.JwtService`: issues signed tokens containing the user ID, identity type, authorities, issuer, and issue/expiration timestamps.
- `security.auth.ClientAuthorization.isOwner`: verifies that the caller has `CLIENT` identity, `ROLE_CLIENT` authority, and a token `user_id` matching the route ID. `canReadClient` reuses this check for client-owned profile reads and permits employees with the `MANAGER` or `ADMIN` role.
- `security.auth.EmployeeAuthorization`: verifies employee identity and role for client searches and client-administration checks.
- `controller.person.ClientController` and `service.person.ClientService`: expose client routes and apply method-level authorization to protected operations. Client registration hashes the supplied password with BCrypt.
- `dto.person.ClientPasswordChangeRequest` and `ClientPasswordResetRequest`: validate the request bodies for a client's own password change and an employee's client-password reset.
- `Person.changePasswordHash`: the only method outside the `Person` hierarchy for changing the password hash; the `passwordHash` setter is `protected`.

## Configuration

Set these environment variables when starting outside the test profile:

| Variable | Purpose |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://localhost:5432/tbank` |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Base64-encoded HS256 key that decodes to at least 32 bytes |
| `JWT_EXPIRATION` | Optional ISO-8601 token lifetime; defaults to `PT15M` |
| `JWT_ISSUER` | Optional JWT issuer; defaults to `tbank` |

Generate a unique key for each environment; do not reuse the fixed test-profile key. For example, in PowerShell:

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DB_URL = "jdbc:postgresql://localhost:5432/tbank"
$env:DB_USERNAME = "tbank"
$env:DB_PASSWORD = "<database-password>"
.\mvnw.cmd spring-boot:run
```

The test profile uses an in-memory H2 database and enables the H2 Console. To run with that profile:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

The test-profile JWT key is for testing only and must not be reused in another environment.

## Login and Postman

Send `POST /auth/login` with `Content-Type: application/json`:

```json
{
  "email": "alice_johnson@tbank.com",
  "password": "account-password"
}
```

The response contains `accessToken`, `tokenType` (`Bearer`), and `expiresIn` in seconds. In Postman, set the request authentication type to JWT bearer and paste `accessToken` into its token value field.

Login accepts clients and employees. `BankUserDetailsService` looks up clients using `findByEmailAndDeletedFalse`, so logically deleted clients are excluded from login. Employee lookup uses `findByEmail` and does not filter a deleted field. Both identity types must have an `ACTIVE` status and a nonblank password hash; an employee must also have a role. Tokens use HS256 and include the configured issuer; the decoder validates the signature and issuer.

## Routes and access matrix

| Route | Access configured in `SecurityConfig` and method authorization |
|---|---|
| `POST /auth/login` | Public |
| `POST /clients` | Public client registration |
| `GET /clients` | `ASSOCIATE`, `MANAGER`, or `ADMIN` |
| `GET /clients/search/name`, `/clients/search/email`, `/clients/search/status` | `ASSOCIATE`, `MANAGER`, or `ADMIN` |
| `GET /clients/search/document` | See [Known inconsistencies](#known-inconsistencies) |
| `GET /clients/{id}` | The client identified by `{id}`, or an employee with `MANAGER` or `ADMIN`; see [Known inconsistencies](#known-inconsistencies) |
| `GET /clients/deleted` | `MANAGER` or `ADMIN` |
| `PUT /clients/{id}` and `DELETE /clients/{id}` | `MANAGER` or `ADMIN` |
| `PATCH /clients/{id}/block`, `/deactivate`, `/reactivate` | `MANAGER` or `ADMIN` |
| `PATCH /clients/{id}/password` | The client whose `user_id` matches `{id}`; requires `CLIENT` identity and `ROLE_CLIENT`. Body: `{"currentPassword":"...","newPassword":"..."}`. Responds `204 No Content`. |
| `PATCH /clients/{id}/password/reset` | `MANAGER` or `ADMIN`. Body: `{"newPassword":"..."}`; the current password is not required. Responds `204 No Content`. |
| `POST /business-accounts` and `POST /business-accounts/**` | `MANAGER` or `ADMIN` according to the HTTP security rules |
| `/employees/**` and `/audits/**` | `ADMIN` according to the HTTP security rules |
| Other requests | `ASSOCIATE`, `MANAGER`, or `ADMIN` according to the catch-all HTTP security rule; method-level authorization may impose additional restrictions |

The client-administration routes (`PUT`, `DELETE`, `block`, `deactivate`, `reactivate`, `/clients/deleted`, and password reset) use `EmployeeAuthorization.canManageClients`, which requires employee identity and `MANAGER` or `ADMIN` authority. Client search methods use employee identity checks as well.

### Password rules

Both passwords request DTOs require a nonblank new password of 12 to 72 characters. `ClientService` also rejects new passwords longer than 72 bytes in UTF-8, matching BCrypt's input limit. For an own-password change, the current password must match the stored hash and the new password must differ from the current password. An employee reset requires no current password.

### Known inconsistencies

- `GET /clients/search/document`: `SecurityConfig` allows `ASSOCIATE`, `MANAGER`, or `ADMIN` at the HTTP-filter layer, while `ClientController` requires `canManageClients`, which permits only employees with `MANAGER` or `ADMIN`. The effective request must pass both checks.
- `GET /clients/{id}`: `SecurityConfig` allows the `ROLE_CLIENT` authority without checking ownership, while `ClientController` delegates to `ClientAuthorization.canReadClient`, which requires that a client token's user ID match `{id}`. The method authorization also permits employees only when they have `MANAGER` or `ADMIN`. The effective request must pass both layers.

### Planned role model (not implemented yet)

- **CLIENT:** can access only their own data and operations; cannot search for other clients or employees.
- **ASSOCIATE:** teller role; can make withdrawals, deposits, and payments. Can create accounts, subject to approval by a `MANAGER`.
- **MANAGER:** administers clients and employees (hire, dismiss, deactivate, and block) and approves account creation.
- **ADMIN:** has general technical access but cannot perform operations on behalf of a client (for example, deposits). Only `ADMIN` can simulate an incoming international transfer (external bank to client). An outgoing international transfer (client to external bank) can also be performed by `ASSOCIATE` and `MANAGER`. Resetting a client's password is an implemented exception to this planned restriction.

## Limits

The token lifetime defaults to 15 minutes. The login response and the token service provide an access token; they do not implement refresh tokens or immediate token revocation. Changing a user's status or role, or changing/resetting a client's password, does not alter a token that has already been issued; it remains valid until expiration, subject to the configured signature and issuer validation. Keep the signing key private and rotate it to invalidate tokens signed with the previous key.

Sessions are stateless and CSRF protection is disabled. `SecurityConfig` does not configure CORS. The H2 Console is disabled by default and enabled by the test profile.

Password recovery through a "forgot password" e-mail link, temporary passwords requiring a change at first login, and employee password changes are not implemented.
