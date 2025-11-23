# Lessons Learned

## Security Config Path Mismatch (Auth & Register Services)
**Problem:** POST requests to `/api/auth/login`, `/api/auth/validate`, and `/api/register/**` endpoints returned 403 (Forbidden) with empty response body.

**Root Cause:** `SecurityConfig` in both Auth and Register services was configured to allow `/api/auth/**` and `/api/register/**`, but the API Gateway strips the `/api/` prefix before routing to services. The actual endpoints received by services were `/auth/**` and `/register/**`, not `/api/auth/**` and `/api/register/**`.

**Architecture Context:**
- Client → `POST http://localhost:8080/api/auth/login`
- API Gateway (with `StripPrefix=2`) → removes `/api/auth` → routes to Auth Service
- Auth Service receives → `POST http://auth-service:8081/login`

**Solution:** Update `SecurityConfig.securityFilterChain()` in all services to match the actual endpoint path (without `/api/` prefix):
```java
// ✓ CORRECT - matches actual path the service receives
.requestMatchers("/auth/**").permitAll()
.requestMatchers("/register/**").permitAll()

// ✗ WRONG - the /api/ prefix is stripped before reaching the service
// .requestMatchers("/api/auth/**").permitAll()
// .requestMatchers("/api/register/**").permitAll()
```

**Key Lesson:** Since the API Gateway strips prefixes via `StripPrefix` filters before routing, microservices never receive the `/api/` prefix. Security rules must match the path exactly as the service receives it, NOT as the client sends it.

**Affected Files:** 
- `auth-service/src/main/java/com/banking/auth/config/SecurityConfig.java`
- `register-service/src/main/java/com/banking/register/config/SecurityConfig.java`

**Testing:**
```bash
# Before fix: Returns 403 Forbidden
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"securePass123"}'

# After fix: Returns 200 with token
```
