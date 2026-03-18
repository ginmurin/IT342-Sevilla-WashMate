# IT342 Phase 1 – User Registration and Login
**WashMate – Laundry Management System**


---

## User Registration

### Registration Fields Used

The registration form collects the following fields:

| Field | Required | Notes |
|---|---|---|
| `firstName` | Yes | Must not be blank |
| `lastName` | Yes | Must not be blank |
| `email` | Yes | Must be a valid email format |
| `password` | Yes | Minimum 6 characters |
| `username` | No | Optional, must be unique if provided |
| `phoneNumber` | No | Optional contact number |
| `role` | No | Defaults to `CUSTOMER` if not provided; accepts `CUSTOMER` or `SHOP_OWNER` |

### Validation Process

Validation is enforced at two layers:

1. **Frontend (React + Zod):** The registration form uses Zod schema validation before the request is sent. Fields are validated for required values, minimum lengths, email format, password confirmation match, and terms acceptance.

2. **Backend (Spring Boot + Jakarta Validation):** The `RegisterRequest` DTO uses `@NotBlank`, `@Email`, and `@Size` annotations. Spring's `@Valid` annotation on the controller method triggers these constraints automatically and returns a `400 Bad Request` if any constraint is violated.

### How Duplicate Accounts Are Prevented

Before creating a new user, `AuthService.register()` calls:

```java
if (userRepository.existsByEmail(request.getEmail())) {
    throw new IllegalArgumentException("Email is already registered");
}
```

The `email` column in the `users` table also has a `UNIQUE` database constraint, and `username` has an additional unique constraint, providing a second layer of enforcement at the database level.

### How Passwords Are Stored Securely

Passwords are **never stored in plain text**. The `PasswordEncoder` bean (BCrypt via Spring Security) hashes the password before saving:

```java
.passwordHash(passwordEncoder.encode(request.getPassword()))
```

BCrypt automatically generates and embeds a per-user salt, meaning identical passwords produce different hashes. The raw password is discarded immediately after encoding and is never persisted.

---

## User Login

### Login Credentials Used

Users may log in using either:
- Their **email address** and password, or
- Their **username** and password

The single field `emailOrUsername` accepts both, making the login flexible.

### How the System Verifies Users

1. **Input detection:** `AuthService.login()` checks whether the input contains `@`. If it does, it is treated as an email; otherwise it queries the database by username to resolve the corresponding email.

2. **Authentication:** Spring Security's `AuthenticationManager` verifies the email/password pair against the stored BCrypt hash:
   ```java
   authenticationManager.authenticate(
       new UsernamePasswordAuthenticationToken(email, request.getPassword())
   );
   ```
   An `AuthenticationException` is thrown automatically if the credentials are wrong.

3. **Token generation:** On success, a signed **JWT (JSON Web Token)** is generated using JJWT 0.12.6. The token embeds the user's email (subject) and role as a claim, and is signed with a secret key (HMAC-SHA). The token's expiry is configured in `application.properties`.

4. **Frontend storage:** The frontend stores the JWT in `localStorage` and attaches it as a `Bearer` token to all subsequent API requests via an Axios interceptor.

### What Happens After Successful Login

1. The server returns a JSON response containing the JWT and the user's profile (`userId`, `firstName`, `lastName`, `email`, `role`).
2. The frontend stores the token and user object in `AuthContext` (React Context).
3. The user is redirected to their role-specific dashboard:
   - `CUSTOMER` → `/customer`
   - `SHOP_OWNER` → `/shop`
   - `ADMIN` → `/admin`
4. Protected routes use `ProtectedRoute` to guard pages, redirecting unauthenticated or unauthorised users back to `/login`.

---

## Database Table

**Table:** `users`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | BIGINT | PK, AUTO INCREMENT | Unique user identifier |
| `username` | VARCHAR | UNIQUE, NULLABLE | Optional display/login name |
| `first_name` | VARCHAR | NOT NULL | User's first name |
| `last_name` | VARCHAR | NOT NULL | User's last name |
| `email` | VARCHAR | NOT NULL, UNIQUE | Primary login identifier |
| `phone_number` | VARCHAR | NULLABLE | Contact number |
| `password_hash` | VARCHAR | NULLABLE | BCrypt-hashed password |
| `oauth_provider` | VARCHAR | NULLABLE | OAuth provider name (e.g., Google) |
| `oauth_id` | VARCHAR | UNIQUE, NULLABLE | OAuth subject ID |
| `role` | VARCHAR | NOT NULL | `CUSTOMER`, `SHOP_OWNER`, or `ADMIN` |
| `status` | VARCHAR | NOT NULL | `ACTIVE` or `INACTIVE` (default `ACTIVE`) |
| `email_verified` | BOOLEAN | NOT NULL | Whether email has been verified |
| `two_factor_enabled` | BOOLEAN | NOT NULL | Whether 2FA is enabled |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |
| `updated_at` | TIMESTAMP | NULLABLE | Last update timestamp |

The database is **PostgreSQL** hosted on **Supabase**. The schema is managed by Spring Boot JPA with `ddl-auto=update`, which automatically creates or updates the table structure to match the `User` entity on application startup.

---

## API Endpoints

| Method | Endpoint | Description | Request Body |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | `{ firstName, lastName, email, password, username?, phoneNumber?, role? }` |
| `POST` | `/api/auth/login` | Login and receive a JWT | `{ emailOrUsername, password }` |

### Sample Registration Request
```json
POST /api/auth/register
{
  "firstName": "Juan",
  "lastName": "Dela Cruz",
  "email": "juan@example.com",
  "password": "secret123",
  "username": "juandc",
  "role": "CUSTOMER"
}
```

### Sample Login Request
```json
POST /api/auth/login
{
  "emailOrUsername": "juan@example.com",
  "password": "secret123"
}
```

### Sample Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "juandc",
  "firstName": "Juan",
  "lastName": "Dela Cruz",
  "email": "juan@example.com",
  "role": "CUSTOMER"
}
```

---