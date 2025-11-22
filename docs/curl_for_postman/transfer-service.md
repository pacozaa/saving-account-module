# Transfer Service - CURL Commands

Base URL: `http://localhost:8080/api/transfer`

## 1. Transfer Funds

Transfers money from one account to another (requires PIN validation).

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 3000.00,
    "pin": "111111",
    "description": "Payment to Bob"
  }'
```

**Request Body:**
- `fromAccountId` (string, required): Source account (7-digit account number)
- `toAccountId` (string, required): Destination account (7-digit account number)
- `amount` (number, required): Amount to transfer (must be positive)
- `pin` (string, required): 6-digit PIN of the account owner
- `description` (string, optional): Description of the transfer

**Response:**
```json
{
  "success": true,
  "message": "Transfer successful",
  "transactionId": 2001,
  "fromAccountId": "1234567",
  "toAccountId": "7654321",
  "amount": 3000.0,
  "fromAccountBalance": 7000.0,
  "toAccountBalance": 8000.0,
  "timestamp": "2025-11-22T11:00:00"
}
```

## 2. Health Check

Check if the transfer service is running.

```bash
curl -X GET http://localhost:8080/api/transfer/health
```

**Response:**
```
Transfer Service is running
```

---

## Complete Transfer Flow

### Prerequisites
1. Have two accounts with sufficient balance in the source account
2. Know the PIN of the source account owner

### Step 1: Login as Alice (Sender)
```bash
ALICE_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_user",
    "password": "alicePass123"
  }')

ALICE_TOKEN=$(echo "$ALICE_LOGIN" | jq -r '.token')
ALICE_USER_ID=$(echo "$ALICE_LOGIN" | jq -r '.userId')
echo "Alice Token: $ALICE_TOKEN"
echo "Alice User ID: $ALICE_USER_ID"
```

### Step 2: Check Alice's Account Balance Before Transfer
```bash
ALICE_ACCOUNTS=$(curl -s -X GET http://localhost:8080/api/accounts/user/$ALICE_USER_ID \
  -H "Authorization: Bearer $ALICE_TOKEN")

ALICE_ACCOUNT_ID=$(echo "$ALICE_ACCOUNTS" | jq -r '.[0].id')
ALICE_BALANCE_BEFORE=$(echo "$ALICE_ACCOUNTS" | jq -r '.[0].balance')

echo "Alice Account ID: $ALICE_ACCOUNT_ID"
echo "Alice Balance Before: $ALICE_BALANCE_BEFORE THB"
```

### Step 3: Transfer Money from Alice to Bob
```bash
TRANSFER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "'$ALICE_ACCOUNT_ID'",
    "toAccountId": "7654321",
    "amount": 3000.00,
    "pin": "111111",
    "description": "Payment to Bob for dinner"
  }')

echo "$TRANSFER_RESPONSE" | jq '.'

# Extract transaction details
TRANSACTION_ID=$(echo "$TRANSFER_RESPONSE" | jq -r '.transactionId')
FROM_BALANCE=$(echo "$TRANSFER_RESPONSE" | jq -r '.fromAccountBalance')
TO_BALANCE=$(echo "$TRANSFER_RESPONSE" | jq -r '.toAccountBalance')

echo "Transaction ID: $TRANSACTION_ID"
echo "Alice's New Balance: $FROM_BALANCE THB"
echo "Bob's New Balance: $TO_BALANCE THB"
```

### Step 4: Verify Alice's Balance After Transfer
```bash
ALICE_ACCOUNTS_AFTER=$(curl -s -X GET http://localhost:8080/api/accounts/user/$ALICE_USER_ID \
  -H "Authorization: Bearer $ALICE_TOKEN")

ALICE_BALANCE_AFTER=$(echo "$ALICE_ACCOUNTS_AFTER" | jq -r '.[0].balance')
echo "Alice Balance After: $ALICE_BALANCE_AFTER THB"
echo "Amount Transferred: 3000 THB"
echo "Balance Change: $(echo "$ALICE_BALANCE_BEFORE - $ALICE_BALANCE_AFTER" | bc) THB"
```

### Step 5: Login as Bob and Verify Receipt
```bash
BOB_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_user",
    "password": "bobPass123"
  }')

BOB_TOKEN=$(echo "$BOB_LOGIN" | jq -r '.token')
BOB_USER_ID=$(echo "$BOB_LOGIN" | jq -r '.userId')

# Check Bob's account balance
BOB_ACCOUNTS=$(curl -s -X GET http://localhost:8080/api/accounts/user/$BOB_USER_ID \
  -H "Authorization: Bearer $BOB_TOKEN")

echo "$BOB_ACCOUNTS" | jq '.'
```

### Step 6: View Transaction History
```bash
# Alice's transactions (showing outgoing transfer)
curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq '.'

# Bob's transactions (showing incoming transfer)
BOB_ACCOUNT_ID=$(echo "$BOB_ACCOUNTS" | jq -r '.[0].id')
curl -s -X GET "http://localhost:8080/api/transactions/account/$BOB_ACCOUNT_ID?pin=222222" \
  -H "Authorization: Bearer $BOB_TOKEN" | jq '.'
```

---

## Multiple Transfers Example

```bash
# Transfer #1: Alice sends 1,000 THB to Bob
curl -s -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 1000.00,
    "pin": "111111",
    "description": "Lunch money"
  }' | jq '.'

# Transfer #2: Alice sends 2,500 THB to Bob
curl -s -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 2500.00,
    "pin": "111111",
    "description": "Rent payment"
  }' | jq '.'

# Transfer #3: Bob sends 500 THB back to Alice
curl -s -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -d '{
    "fromAccountId": "7654321",
    "toAccountId": "1234567",
    "amount": 500.00,
    "pin": "222222",
    "description": "Paying back"
  }' | jq '.'
```

---

## Authorization & Business Rules

### Authorization
- **Any authenticated user** can transfer from their own accounts
- User must own the source account (fromAccountId)
- Valid PIN required for the source account owner
- Requires valid JWT token

### Validation Rules
- Amount must be positive (> 0)
- Source account must have sufficient balance
- Source and destination accounts cannot be the same
- Both accounts must exist and be active
- PIN must match the source account owner's PIN
- Maximum precision: 2 decimal places

### What Happens Behind the Scenes
1. **Transfer Service** receives the request
2. Validates user owns the source account
3. Validates PIN with **Register Service**
4. Calls **Account Service** to check source account balance
5. Deducts amount from source account
6. Credits amount to destination account
7. Logs TWO transactions in **Transaction Service**:
   - TRANSFER_OUT for source account (negative amount)
   - TRANSFER_IN for destination account (positive amount)
8. Returns success response with both account balances

### Error Responses

**Invalid PIN (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Invalid PIN"
}
```

**Insufficient Funds (400 Bad Request):**
```json
{
  "error": "Bad Request",
  "message": "Insufficient funds"
}
```

**Not Account Owner (403 Forbidden):**
```json
{
  "error": "Forbidden",
  "message": "You can only transfer from your own accounts"
}
```

**Account Not Found (404):**
```json
{
  "error": "Not Found",
  "message": "Account not found"
}
```

**Same Account Transfer (400):**
```json
{
  "error": "Bad Request",
  "message": "Cannot transfer to the same account"
}
```

**Invalid Amount (400):**
```json
{
  "error": "Bad Request",
  "message": "Transfer amount must be positive"
}
```

---

## Common Use Cases

### Scenario 1: Monthly Rent Payment
```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 15000.00,
    "pin": "111111",
    "description": "November rent payment"
  }'
```

### Scenario 2: Split Bill
```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 750.00,
    "pin": "111111",
    "description": "Restaurant bill split"
  }'
```

### Scenario 3: Emergency Transfer
```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 5000.00,
    "pin": "111111",
    "description": "Emergency fund transfer"
  }'
```
