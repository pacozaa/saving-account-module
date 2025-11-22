# API Gateway Path Resolution Guide

This document explains how the API Gateway resolves incoming requests and applies authentication filtering.

## Overview

The API Gateway acts as the single entry point for all client requests. It performs two main functions:
1. **Path Routing** - Routes requests to appropriate microservices based on URL paths
2. **Authentication** - Validates JWT tokens and adds user context headers

---

## 1. Path Routing (`application.yml`)

### Configuration Location
`api-gateway/src/main/resources/application.yml`

### How It Works

The gateway uses Spring Cloud Gateway's routing mechanism to match incoming requests and forward them to the appropriate microservice.

#### Route Configuration Structure

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: <unique-route-id>
          uri: lb://<service-name>
          predicates:
            - Path=<pattern>
          filters:
            - StripPrefix=<number>
```

### Current Route Mappings

| Client Request Path | Predicate Match | Service URI | StripPrefix | Actual Service Path |
|-------------------|----------------|-------------|-------------|-------------------|
| `/api/auth/login` | `/api/auth/**` | `lb://auth-service` | 1 | `/auth/login` |
| `/api/auth/validate` | `/api/auth/**` | `lb://auth-service` | 1 | `/auth/validate` |
| `/api/register/customer` | `/api/register/**` | `lb://register-service` | 1 | `/register/customer` |
| `/api/deposit/account/123` | `/api/deposit/**` | `lb://deposit-service` | 1 | `/deposit/account/123` |
| `/api/transfer/execute` | `/api/transfer/**` | `lb://transfer-service` | 1 | `/transfer/execute` |
| `/api/accounts/123` | `/api/accounts/**` | `lb://account-service` | 1 | `/accounts/123` |
| `/api/transactions/123` | `/api/transactions/**` | `lb://transaction-service` | 1 | `/transactions/123` |

### Key Concepts

#### 1. **Path Predicate**
- Matches incoming request paths using pattern matching
- `**` is a wildcard that matches any number of path segments
- Example: `/api/auth/**` matches `/api/auth/login`, `/api/auth/validate`, etc.

#### 2. **Load Balancer URI (`lb://`)**
- `lb://` prefix tells Spring Cloud Gateway to use the load balancer
- Service name (e.g., `auth-service`) is resolved via Eureka service discovery
- The gateway looks up the actual instance IP/port from Eureka Server

#### 3. **StripPrefix Filter**
- Removes the specified number of path segments before forwarding
- `StripPrefix=1` removes the first path segment (e.g., `/api`)
- This allows microservices to define routes without the `/api` prefix

#### Example Flow

```
Client Request: GET http://localhost:8080/api/auth/login

1. Gateway receives: /api/auth/login
2. Matches route: id=auth-service, Path=/api/auth/**
3. Applies StripPrefix=1: /api/auth/login → /auth/login
4. Resolves lb://auth-service via Eureka
5. Forwards to: http://<auth-service-host>:<port>/auth/login
```

---

## 2. JWT Authentication Filter (`JwtAuthenticationFilter.java`)

### Configuration Location
`api-gateway/src/main/java/com/banking/gateway/filter/JwtAuthenticationFilter.java`

### How It Works

The `JwtAuthenticationFilter` is a **GlobalFilter** that intercepts **ALL** requests before they are routed to microservices.

#### Filter Execution Order
- Order: `-100` (executes early in the filter chain, before routing)
- Applied globally to every request passing through the gateway

### Authentication Flow

```mermaid
flowchart TD
    A[Client Request] --> B{Is Public Path?}
    B -->|Yes| C[Skip Authentication]
    C --> D[Forward to Service]
    B -->|No| E{Has Bearer Token?}
    E -->|No| F[Return 401 Unauthorized]
    E -->|Yes| G{Token Valid?}
    G -->|No| H[Return 401 Unauthorized]
    G -->|Yes| I[Extract User Info]
    I --> J[Add Headers: X-User-Id, X-Username, X-User-Role]
    J --> D[Forward to Service]
```

### Public Routes (No Authentication Required)

The following paths are **exempt from authentication**:

```java
private static final List<String> PUBLIC_ROUTES = Arrays.asList(
    "/api/auth/login",
    "/api/auth/validate",
    "/api/register",
    "/actuator",
    "/swagger-ui",
    "/v3/api-docs",
    "/webjars"
);
```

| Path Pattern | Purpose | Example |
|-------------|---------|---------|
| `/api/auth/login` | User login endpoint | `POST /api/auth/login` |
| `/api/auth/validate` | Token validation | `POST /api/auth/validate` |
| `/api/register` | User registration | `POST /api/register/customer` |
| `/actuator` | Health/monitoring endpoints | `GET /actuator/health` |
| `/swagger-ui` | Swagger UI access | `GET /swagger-ui.html` |
| `/v3/api-docs` | OpenAPI documentation | `GET /v3/api-docs` |
| `/webjars` | Static resources | `GET /webjars/...` |

**Matching Logic**: Uses `path.startsWith(publicRoute)` - any path starting with these patterns is public.

### Protected Routes (Authentication Required)

All other routes require a valid JWT token in the `Authorization` header.

#### Required Header Format
```
Authorization: Bearer <jwt-token>
```

#### Authentication Steps

1. **Extract Token**
   - Looks for `Authorization` header
   - Validates format: must start with "Bearer "
   - Extracts token by removing "Bearer " prefix

2. **Validate Token** (via `JwtUtil`)
   - Verifies token signature using secret key
   - Checks token expiration
   - Returns 401 if invalid or expired

3. **Extract Claims** (via `JwtUtil`)
   - `userId` - User's database ID
   - `username` - User's username (subject)
   - `role` - User's role (CUSTOMER, TELLER, ADMIN)

4. **Add Headers to Request**
   ```
   X-User-Id: <userId>
   X-Username: <username>
   X-User-Role: <role>
   ```

5. **Forward Modified Request**
   - Original request + user context headers
   - Downstream services can access user info without re-validating token

### Error Responses

#### Missing or Invalid Authorization Header
```json
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

#### Invalid or Expired Token
```json
{
  "error": "Invalid or expired token",
  "status": 401
}
```

#### Token Validation Failed
```json
{
  "error": "Token validation failed: <reason>",
  "status": 401
}
```

---

## 3. Complete Request Flow Example

### Example 1: Public Route (Login)

```
Client → Gateway → Service

1. Client Request:
   POST http://localhost:8080/api/auth/login
   Body: { "username": "john", "password": "pass123" }

2. JwtAuthenticationFilter:
   - Path: /api/auth/login
   - Matches public route: /api/auth/login ✓
   - Skip authentication
   - Continue to routing

3. Gateway Routing:
   - Matches route: id=auth-service
   - StripPrefix=1: /api/auth/login → /auth/login
   - Resolve lb://auth-service via Eureka
   - Forward to: http://auth-service:8081/auth/login

4. Auth Service Response:
   - Returns JWT token
   - Gateway forwards response to client
```

### Example 2: Protected Route (Get Account)

```
Client → Gateway → Service

1. Client Request:
   GET http://localhost:8080/api/accounts/123
   Headers:
     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter:
   - Path: /api/accounts/123
   - NOT a public route → require authentication
   - Extract token from Authorization header
   - Validate token with JwtUtil
   - Extract claims:
     * userId: 42
     * username: john_doe
     * role: CUSTOMER
   - Add headers:
     * X-User-Id: 42
     * X-Username: john_doe
     * X-User-Role: CUSTOMER

3. Gateway Routing:
   - Matches route: id=account-service
   - StripPrefix=1: /api/accounts/123 → /accounts/123
   - Resolve lb://account-service via Eureka
   - Forward to: http://account-service:8083/accounts/123
   - Include modified headers

4. Account Service:
   - Receives request with user context headers
   - No need to validate JWT again
   - Can use X-User-Id for authorization logic
   - Returns account data

5. Gateway:
   - Forwards response to client
```

### Example 3: Failed Authentication

```
Client → Gateway → Client (401)

1. Client Request:
   GET http://localhost:8080/api/accounts/123
   Headers:
     Authorization: Bearer invalid-or-expired-token

2. JwtAuthenticationFilter:
   - Path: /api/accounts/123
   - NOT a public route → require authentication
   - Extract token: invalid-or-expired-token
   - Validate token with JwtUtil → FAILED
   - Return 401 response immediately

3. Response to Client:
   HTTP 401 Unauthorized
   {
     "error": "Invalid or expired token",
     "status": 401
   }

4. Request never reaches account-service
```

---

## 4. Integration with JWT Utility

### JWT Token Structure

The gateway expects JWT tokens with the following claims:

```json
{
  "sub": "john_doe",           // Username (standard JWT claim)
  "userId": 42,                // Custom claim: user ID
  "role": "CUSTOMER",          // Custom claim: user role
  "iat": 1700000000,           // Issued at timestamp
  "exp": 1700003600            // Expiration timestamp (1 hour)
}
```

### JwtUtil Methods Used by Filter

| Method | Purpose | Return Type |
|--------|---------|-------------|
| `validateToken(token)` | Validates signature and expiration | `boolean` |
| `extractUserId(token)` | Extracts userId claim | `Long` |
| `extractUsername(token)` | Extracts sub (username) claim | `String` |
| `extractRole(token)` | Extracts role claim | `String` |

### Token Validation Logic

```java
// 1. Parse and verify signature
Jwts.parser()
    .verifyWith(getSigningKey())
    .build()
    .parseSignedClaims(token);

// 2. Check expiration
return !isTokenExpired(token);

// 3. Signing key derived from secret
SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
```

**Secret Key**: Configured in `application.yml` under `jwt.secret`

---

## 5. Important Notes

### Service Design Pattern
- **Microservices define routes WITHOUT `/api` prefix**
- Example: Auth service has `@RequestMapping("/auth")` not `@RequestMapping("/api/auth")`
- Gateway adds `/api` prefix for external clients
- `StripPrefix=1` removes `/api` before forwarding

### Security Considerations
1. **JWT validation happens once** - at the gateway level
2. **Downstream services trust** the `X-User-*` headers
3. **Services should not be exposed** directly to the internet
4. **Only the gateway (port 8080)** should be publicly accessible

### Troubleshooting

#### Issue: 401 on public routes
- **Check**: Does path exactly match PUBLIC_ROUTES?
- **Note**: Match uses `startsWith()` logic

#### Issue: 401 with valid token
- **Check**: Token format is `Bearer <token>` (note the space)
- **Check**: Token not expired
- **Check**: `jwt.secret` matches between auth-service and api-gateway

#### Issue: Route not found (404)
- **Check**: Path predicate in `application.yml`
- **Check**: Service registered in Eureka
- **Check**: Service name matches `lb://` URI

#### Issue: Wrong service receives request
- **Check**: Route order (more specific routes should come first)
- **Check**: Path predicates don't overlap unexpectedly

---

## 6. Summary

### Path Resolution Process
```
1. Client Request → API Gateway (port 8080)
2. JwtAuthenticationFilter checks authentication
3. Gateway routes by path predicate
4. StripPrefix removes /api segment
5. Service discovery resolves service instance
6. Request forwarded with user context headers
7. Service processes request
8. Response flows back through gateway to client
```

### Key Components
- **application.yml**: Defines routing rules and path mappings
- **JwtAuthenticationFilter**: Validates JWT and adds user context
- **JwtUtil**: Handles token parsing and validation
- **Eureka**: Provides service discovery for load balancing

### Benefits of This Architecture
- ✅ Single entry point for all client requests
- ✅ Centralized authentication and authorization
- ✅ Services don't need to implement JWT validation
- ✅ Easy to add/modify routes without service changes
- ✅ Load balancing across service instances
- ✅ Clear separation of concerns
