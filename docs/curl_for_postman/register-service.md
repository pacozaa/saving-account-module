# Register Service - CURL Commands

Base URL: `http://localhost:8080/api/register`

## 1. Register a New User

Creates a new user with the specified role (CUSTOMER or TELLER).

### Register a Customer
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

### Register a Teller
```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_user",
    "password": "tellerPass123",
    "email": "teller@bank.com",
    "citizenId": "3129876543210",
    "thaiName": "เทลเลอร์ ธนาคาร",
    "englishName": "Bank Teller",
    "pin": "999999",
    "role": "TELLER"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "alice_user",
    "email": "alice@example.com",
    "citizenId": "3101234567890",
    "thaiName": "อลิซ คัสโตเมอร์",
    "englishName": "Alice Customer",
    "role": "CUSTOMER"
  }
}
```

## 2. Get User by Username

Retrieves user information by username.

```bash
curl -X GET http://localhost:8080/api/register/user/alice_user
```

**Response:**
```json
{
  "id": 1,
  "username": "alice_user",
  "email": "alice@example.com",
  "citizenId": "3101234567890",
  "thaiName": "อลิซ คัสโตเมอร์",
  "englishName": "Alice Customer",
  "role": "CUSTOMER"
}
```

## 3. Get User by ID

Retrieves user information by user ID.

```bash
curl -X GET http://localhost:8080/api/register/user/id/1
```

**Response:**
```json
{
  "id": 1,
  "username": "alice_user",
  "email": "alice@example.com",
  "citizenId": "3101234567890",
  "thaiName": "อลิซ คัสโตเมอร์",
  "englishName": "Alice Customer",
  "role": "CUSTOMER"
}
```

## 4. Validate PIN

Validates if the provided PIN matches the user's stored PIN.

```bash
curl -X POST "http://localhost:8080/api/register/validate-pin?userId=1&pin=111111"
```

**Response:**
```json
true
```

## 5. Health Check

Check if the register service is running.

```bash
curl -X GET http://localhost:8080/api/register/health
```

**Response:**
```
Register Service is running
```

---

## Complete User Registration Flow

### 1. Register Alice (Customer)
```bash
ALICE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/register \
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
  }')

# Extract user ID
ALICE_USER_ID=$(echo "$ALICE_RESPONSE" | jq -r '.user.id')
echo "Alice User ID: $ALICE_USER_ID"
```

### 2. Register Bob (Customer)
```bash
BOB_RESPONSE=$(curl -s -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_user",
    "password": "bobPass123",
    "email": "bob@example.com",
    "citizenId": "3119876543210",
    "thaiName": "บ็อบ คัสโตเมอร์",
    "englishName": "Bob Customer",
    "pin": "222222",
    "role": "CUSTOMER"
  }')

BOB_USER_ID=$(echo "$BOB_RESPONSE" | jq -r '.user.id')
echo "Bob User ID: $BOB_USER_ID"
```

### 3. Register Teller
```bash
TELLER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_user",
    "password": "tellerPass123",
    "email": "teller@bank.com",
    "citizenId": "3129876543210",
    "thaiName": "เทลเลอร์ ธนาคาร",
    "englishName": "Bank Teller",
    "pin": "999999",
    "role": "TELLER"
  }')

TELLER_USER_ID=$(echo "$TELLER_RESPONSE" | jq -r '.user.id')
echo "Teller User ID: $TELLER_USER_ID"
```

### 4. Verify User Registration
```bash
# Get Alice by username
curl -X GET http://localhost:8080/api/register/user/alice_user | jq '.'

# Get Bob by ID
curl -X GET http://localhost:8080/api/register/user/id/$BOB_USER_ID | jq '.'

# Validate Alice's PIN
curl -X POST "http://localhost:8080/api/register/validate-pin?userId=$ALICE_USER_ID&pin=111111"
```
