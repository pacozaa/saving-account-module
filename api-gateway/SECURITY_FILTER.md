# API Gateway Security Filter

## Overview

The API Gateway implements JWT-based authentication through a `JwtAuthenticationFilter` that validates tokens and enriches requests with user context before routing to downstream microservices.

## Architecture

```
Client Request
     ↓
[Authorization: Bearer <token>]
     ↓
API Gateway (Port 8080)
     ↓
JwtAuthenticationFilter (Order: -100)
     ↓
┌─────────────────────────────┐
│ Is Public Route?            │
│ - /api/auth/login           │
│ - /api/register             │
│ - /actuator/**              │
│ - /swagger-ui/**            │
└─────────────────────────────┘
     ↓
   YES → Skip Authentication → Route to Service
     ↓
   NO → Validate JWT Token
     ↓
┌─────────────────────────────┐
│ Token Valid?                │
│ - Check signature           │
│ - Check expiration          │
│ - Extract claims            │
└─────────────────────────────┘
     ↓
   NO → Return 401 Unauthorized
     ↓
   YES → Add Headers:
         - X-User-Id
         - X-Username
         - X-User-Role
     ↓
Route to Downstream Service
```

## Components

### 1. JwtAuthenticationFilter

**Location:** `com.banking.gateway.filter.JwtAuthenticationFilter`

**Type:** `GlobalFilter` with `Ordered` interface

**Execution Order:** `-100` (executes before routing)

**Responsibilities:**
- Intercept all incoming requests
- Check if route is public (bypass authentication)
- Extract and validate JWT token from Authorization header
- Extract user claims from token
- Add user context headers for downstream services
- Return 401 for authentication failures

### 2. JwtUtil

**Location:** `com.banking.gateway.util.JwtUtil`

**Responsibilities:**
- Validate JWT token signature and expiration
- Extract claims from token:
  - `userId` (Long)
  - `username` (String - from subject)
  - `role` (String - PERSON, CUSTOMER, TELLER)

**Configuration:**
- Uses same JWT secret as Auth Service
- Secret: `banking-system-secret-key-for-jwt-token-generation-and-validation`

## Public Routes (No Authentication Required)

The following routes bypass JWT authentication:

| Route Pattern | Purpose |
|--------------|---------|
| `/api/auth/login` | User login endpoint |
| `/api/auth/validate` | Token validation endpoint |
| `/api/register` | User registration |
| `/actuator` | Health checks and monitoring |
| `/swagger-ui` | API documentation UI |
| `/v3/api-docs` | OpenAPI specification |
| `/webjars` | Swagger UI static resources |

## Protected Routes (Authentication Required)

All other routes require a valid JWT token:

| Service | Route Pattern | Requires Token |
|---------|--------------|----------------|
| Deposit Service | `/api/deposit/**` | ✅ Yes |
| Transfer Service | `/api/transfer/**` | ✅ Yes |
| Account Service | `/api/accounts/**` | ✅ Yes |
| Transaction Service | `/api/transactions/**` | ✅ Yes |

## Authentication Flow

### Successful Authentication

```http
GET /api/accounts/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJDVVNUT01FUiIsInN1YiI6ImpvaG5kb2UifQ...

↓ Gateway processes request ↓

GET /accounts/123 HTTP/1.1
Host: account-service:8083
X-User-Id: 1
X-Username: johndoe
X-User-Role: CUSTOMER
```

### Failed Authentication (Missing Token)

```http
GET /api/accounts/123 HTTP/1.1
Host: localhost:8080

↓ Response ↓

HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

### Failed Authentication (Invalid Token)

```http
GET /api/accounts/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer invalid.token.here

↓ Response ↓

HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Invalid or expired token",
  "status": 401
}
```

## User Context Headers

The filter adds the following headers to authenticated requests for downstream services:

| Header Name | Type | Description | Example |
|------------|------|-------------|---------|
| `X-User-Id` | Long | Unique user identifier | `1` |
| `X-Username` | String | Username from token | `johndoe` |
| `X-User-Role` | String | User role | `CUSTOMER` |

**Downstream services can use these headers to:**
- Identify the authenticated user
- Implement role-based access control
- Audit and log user actions
- Validate ownership of resources

## Configuration

### application.yml

```yaml
jwt:
  secret: banking-system-secret-key-for-jwt-token-generation-and-validation
```

**Important:** The JWT secret MUST match the secret configured in the Auth Service.

## Security Considerations

### ✅ What's Implemented

1. **Token Validation** - Verifies signature and expiration
2. **Public Routes** - Allows unauthenticated access where needed
3. **Header Injection** - Provides user context to services
4. **Error Handling** - Returns proper 401 responses

### ⚠️ Limitations (short period constraint tradeoffs)

1. **No Refresh Tokens** - Tokens expire after 24 hours, requiring re-login
2. **No Token Revocation** - Once issued, tokens remain valid until expiration
3. **No Rate Limiting** - No protection against brute force attacks
4. **No HTTPS Enforcement** - Tokens transmitted over HTTP in development
5. **Shared Secret** - All services share the same JWT secret

### 🔒 Production Recommendations

1. **Use HTTPS** - Encrypt all traffic
2. **Implement Refresh Tokens** - Allow token renewal without re-login
3. **Add Token Blacklist** - Enable token revocation
4. **Rate Limiting** - Protect against brute force
5. **Rotate Secrets** - Use key rotation strategy
6. **Separate Secrets** - Use different signing keys per environment
7. **Add CORS** - Configure proper CORS policies
8. **Logging & Monitoring** - Track authentication failures

## Testing

### Test Public Route (No Token Required)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"password123"}'
```

### Test Protected Route (Token Required)

```bash
# Get token from login
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Access protected route
curl -X GET http://localhost:8080/api/accounts/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Test Invalid Token

```bash
curl -X GET http://localhost:8080/api/accounts/1 \
  -H "Authorization: Bearer invalid-token"

# Expected: 401 Unauthorized
```

### Test Missing Token

```bash
curl -X GET http://localhost:8080/api/accounts/1

# Expected: 401 Unauthorized
```

## Troubleshooting

### Issue: Getting 401 on valid token

**Possible Causes:**
1. JWT secret mismatch between Gateway and Auth Service
2. Token expired (check expiration in token)
3. Token malformed (check Bearer prefix)

**Solution:**
- Verify `jwt.secret` matches in both services
- Login again to get fresh token
- Ensure header format: `Authorization: Bearer <token>`

### Issue: Public routes returning 401

**Possible Causes:**
1. Route path not in PUBLIC_ROUTES list
2. Path matching issue (case sensitivity)

**Solution:**
- Check JwtAuthenticationFilter PUBLIC_ROUTES array
- Verify exact path in request

### Issue: Downstream services not receiving headers

**Possible Causes:**
1. Filter order incorrect
2. Request not being mutated properly

**Solution:**
- Verify filter order is -100 (before routing)
- Check ServerHttpRequest mutation in filter

## Code References

- **Filter Implementation:** `api-gateway/src/main/java/com/banking/gateway/filter/JwtAuthenticationFilter.java`
- **JWT Utility:** `api-gateway/src/main/java/com/banking/gateway/util/JwtUtil.java`
- **Configuration:** `api-gateway/src/main/resources/application.yml`
- **Dependencies:** `api-gateway/pom.xml` (JJWT libraries)

## Related Documentation

- [Auth Service Documentation](../auth-service/README.md)
- [JWT Token Structure](../docs/JWT_TOKENS.md)
- [API Gateway Routes](../docs/GATEWAY_ROUTES.md)
