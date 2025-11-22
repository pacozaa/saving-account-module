# Auth Service - CURL Commands

Base URL: `http://localhost:8080/api/auth`

## 1. User Login

Authenticates user credentials and returns a JWT token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_user",
    "password": "alicePass123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "alice_user",
  "role": "CUSTOMER"
}
```

## 2. Validate Token

Validates a JWT token and returns token information.

```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "valid": true,
  "userId": 1,
  "username": "alice_user",
  "role": "CUSTOMER"
}
```

## 3. Health Check

Check if the auth service is running.

```bash
curl -X GET http://localhost:8080/api/auth/health
```

**Response:**
```
Auth Service is running
```

---

## Example E2E Flow

### Step 1: Register a User (via Register Service)
```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_user",
    "password": "alicePass123",
    "email": "alice@example.com",
    "citizenId": "3101234567890",
    "thaiName": "อลิซ คัสโตเมอร์",
    "englishName": "Alice Customer",
    "pin": "111111",
    "role": "CUSTOMER"
  }'
```

### Step 2: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_user",
    "password": "alicePass123"
  }'
```

Save the token from the response for subsequent authenticated requests.
