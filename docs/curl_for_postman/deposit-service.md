# Deposit Service - CURL Commands

Base URL: `http://localhost:8080/api/deposit`

## 1. Make a Deposit

Deposits money into a specified account (requires TELLER role).

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TELLER_JWT_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 5000.00,
    "tellerId": 3,
    "description": "Initial deposit"
  }'
```

**Request Body:**
- `accountId` (string, required): The 7-digit account number to deposit into
- `amount` (number, required): Amount to deposit (must be positive)
- `tellerId` (number, required): ID of the teller performing the deposit
- `description` (string, optional): Description of the deposit transaction

**Response:**
```json
{
  "success": true,
  "message": "Deposit successful",
  "transactionId": 1001,
  "accountId": "1234567",
  "amount": 5000.0,
  "newBalance": 15000.0,
  "timestamp": "2025-11-22T10:35:00"
}
```

## 2. Health Check

Check if the deposit service is running.

```bash
curl -X GET http://localhost:8080/api/deposit/health
```

**Response:**
```
Deposit Service is running
```

---

## Complete Deposit Flow

### Prerequisites
1. Have a registered Teller user
2. Have an existing account to deposit into

### Step 1: Login as Teller
```bash
TELLER_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_user",
    "password": "tellerPass123"
  }')

TELLER_TOKEN=$(echo "$TELLER_LOGIN" | jq -r '.token')
TELLER_USER_ID=$(echo "$TELLER_LOGIN" | jq -r '.userId')
echo "Teller Token: $TELLER_TOKEN"
echo "Teller User ID: $TELLER_USER_ID"
```

### Step 2: Make a Deposit to Bob's Account
```bash
DEPOSIT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "7654321",
    "amount": 5000.00,
    "tellerId": '$TELLER_USER_ID',
    "description": "Initial deposit for Bob"
  }')

echo "$DEPOSIT_RESPONSE" | jq '.'

# Extract transaction details
TRANSACTION_ID=$(echo "$DEPOSIT_RESPONSE" | jq -r '.transactionId')
NEW_BALANCE=$(echo "$DEPOSIT_RESPONSE" | jq -r '.newBalance')
echo "Transaction ID: $TRANSACTION_ID"
echo "New Balance: $NEW_BALANCE THB"
```

### Step 3: Verify the Deposit
```bash
# Login as Bob (account owner)
BOB_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_user",
    "password": "bobPass123"
  }')

BOB_TOKEN=$(echo "$BOB_LOGIN" | jq -r '.token')
BOB_USER_ID=$(echo "$BOB_LOGIN" | jq -r '.userId')

# Check Bob's account balance
curl -X GET http://localhost:8080/api/accounts/user/$BOB_USER_ID \
  -H "Authorization: Bearer $BOB_TOKEN" | jq '.'
```

### Step 4: View Transaction History
```bash
# Get Bob's transaction history (requires PIN)
curl -X GET "http://localhost:8080/api/transactions/account/7654321?pin=222222" \
  -H "Authorization: Bearer $BOB_TOKEN" | jq '.'
```

---

## Multiple Deposits Example

```bash
# Deposit #1: 5,000 THB
curl -s -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 5000.00,
    "tellerId": 3,
    "description": "Cash deposit"
  }' | jq '.'

# Deposit #2: 2,500 THB
curl -s -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 2500.00,
    "tellerId": 3,
    "description": "Check deposit"
  }' | jq '.'

# Deposit #3: 10,000 THB
curl -s -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 10000.00,
    "tellerId": 3,
    "description": "Wire transfer deposit"
  }' | jq '.'
```

---

## Authorization & Business Rules

### Authorization
- **Only TELLER role** can perform deposits
- Requires valid JWT token with TELLER role
- Unauthorized attempts will return 403 Forbidden

### Validation Rules
- Amount must be positive (> 0)
- Account must exist
- Teller ID must be valid
- Maximum precision: 2 decimal places

### What Happens Behind the Scenes
1. **Deposit Service** receives the request
2. Validates teller authorization
3. Calls **Account Service** to update the account balance
4. Calls **Transaction Service** to log the transaction
5. Returns success response with updated balance

### Error Responses

**Invalid Role (403 Forbidden):**
```json
{
  "error": "Forbidden",
  "message": "Only tellers are authorized to perform deposits"
}
```

**Account Not Found (404):**
```json
{
  "error": "Not Found",
  "message": "Account not found"
}
```

**Invalid Amount (400):**
```json
{
  "error": "Bad Request",
  "message": "Amount must be positive"
}
```
