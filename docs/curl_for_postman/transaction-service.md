# Transaction Service - CURL Commands

Base URL: `http://localhost:8080/api/transactions`

## 1. Get Transactions by Account

Retrieves transaction history for a specific account (requires PIN validation).

```bash
curl -X GET "http://localhost:8080/api/transactions/account/1234567?pin=111111" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Query Parameters:**
- `pin` (string, required): 6-digit PIN of the account owner for validation

**Response:**
```json
[
  {
    "id": 1001,
    "accountId": "1234567",
    "amount": 10000.0,
    "type": "DEPOSIT",
    "description": "Initial deposit",
    "balanceAfter": 10000.0,
    "transactionDate": "2025-11-22T10:30:00",
    "createdBy": 3
  },
  {
    "id": 2001,
    "accountId": "1234567",
    "amount": -3000.0,
    "type": "TRANSFER_OUT",
    "description": "Payment to Bob",
    "balanceAfter": 7000.0,
    "transactionDate": "2025-11-22T11:00:00",
    "createdBy": 1
  }
]
```

## 2. Get Transaction by ID

Retrieves a specific transaction by its ID.

```bash
curl -X GET http://localhost:8080/api/transactions/1001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1001,
  "accountId": "1234567",
  "amount": 10000.0,
  "type": "DEPOSIT",
  "description": "Initial deposit",
  "balanceAfter": 10000.0,
  "transactionDate": "2025-11-22T10:30:00",
  "createdBy": 3
}
```

## 3. Log Transaction

Records a new transaction (used internally by orchestrator services like Deposit/Transfer).

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 5000.00,
    "type": "DEPOSIT",
    "description": "Cash deposit",
    "balanceAfter": 15000.00,
    "createdBy": 3
  }'
```

**Request Body:**
- `accountId` (string, required): The 7-digit account number
- `amount` (number, required): Transaction amount (positive for credit, negative for debit)
- `type` (string, required): Transaction type (DEPOSIT, TRANSFER_OUT, TRANSFER_IN, WITHDRAWAL)
- `description` (string, optional): Description of the transaction
- `balanceAfter` (number, required): Account balance after the transaction
- `createdBy` (number, optional): User ID who created the transaction

**Response:**
```json
{
  "id": 1002,
  "accountId": "1234567",
  "amount": 5000.0,
  "type": "DEPOSIT",
  "description": "Cash deposit",
  "balanceAfter": 15000.0,
  "transactionDate": "2025-11-22T10:35:00",
  "createdBy": 3
}
```

---

## Complete Transaction History Flow

### Step 1: Login as Alice
```bash
ALICE_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_user",
    "password": "alicePass123"
  }')

ALICE_TOKEN=$(echo "$ALICE_LOGIN" | jq -r '.token')
ALICE_USER_ID=$(echo "$ALICE_LOGIN" | jq -r '.userId')
```

### Step 2: Get Alice's Account ID
```bash
ALICE_ACCOUNTS=$(curl -s -X GET http://localhost:8080/api/accounts/user/$ALICE_USER_ID \
  -H "Authorization: Bearer $ALICE_TOKEN")

ALICE_ACCOUNT_ID=$(echo "$ALICE_ACCOUNTS" | jq -r '.[0].id')
echo "Alice Account ID: $ALICE_ACCOUNT_ID"
```

### Step 3: Get All Transactions for Alice's Account
```bash
ALICE_TRANSACTIONS=$(curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN")

echo "$ALICE_TRANSACTIONS" | jq '.'

# Count total transactions
TRANSACTION_COUNT=$(echo "$ALICE_TRANSACTIONS" | jq 'length')
echo "Total Transactions: $TRANSACTION_COUNT"
```

### Step 4: Get Details of a Specific Transaction
```bash
# Get the first transaction ID
FIRST_TX_ID=$(echo "$ALICE_TRANSACTIONS" | jq -r '.[0].id')

# Fetch detailed information
curl -s -X GET http://localhost:8080/api/transactions/$FIRST_TX_ID \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq '.'
```

### Step 5: Filter Transactions by Type
```bash
# Get all deposits
echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "DEPOSIT")]'

# Get all transfers out
echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_OUT")]'

# Get all transfers in
echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_IN")]'
```

### Step 6: Calculate Total Deposits and Transfers
```bash
# Sum of all deposits
TOTAL_DEPOSITS=$(echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "DEPOSIT") | .amount] | add')
echo "Total Deposits: $TOTAL_DEPOSITS THB"

# Sum of all transfers out (these are negative)
TOTAL_TRANSFERS_OUT=$(echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_OUT") | .amount] | add')
echo "Total Transfers Out: $TOTAL_TRANSFERS_OUT THB"

# Sum of all transfers in
TOTAL_TRANSFERS_IN=$(echo "$ALICE_TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_IN") | .amount] | add')
echo "Total Transfers In: $TOTAL_TRANSFERS_IN THB"
```

---

## Transaction Analysis Examples

### Example 1: Monthly Statement
```bash
# Get Alice's transactions with PIN
ALICE_STATEMENT=$(curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN")

# Format as a statement
echo "======================================"
echo "       BANK STATEMENT"
echo "======================================"
echo "Account: $ALICE_ACCOUNT_ID"
echo "======================================"
echo ""

echo "$ALICE_STATEMENT" | jq -r '.[] | "\(.transactionDate) | \(.type) | \(.amount) THB | \(.description) | Balance: \(.balanceAfter) THB"'
```

### Example 2: Recent Transactions (Last 5)
```bash
curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq '.[-5:]'
```

### Example 3: Large Transactions (> 5,000 THB)
```bash
curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | \
  jq '[.[] | select((.amount > 5000) or (.amount < -5000))]'
```

### Example 4: Transactions with Specific Description
```bash
curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | \
  jq '[.[] | select(.description | contains("payment"))]'
```

---

## Verify E2E Transaction Flow

### Complete Flow: Deposit → Transfer → Check History

```bash
# 1. Login as Teller
TELLER_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "teller_user", "password": "tellerPass123"}')
TELLER_TOKEN=$(echo "$TELLER_LOGIN" | jq -r '.token')

# 2. Make a deposit to Bob's account
curl -s -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "7654321",
    "amount": 10000.00,
    "tellerId": 3,
    "description": "Initial deposit for Bob"
  }' | jq '.'

# 3. Login as Alice
ALICE_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice_user", "password": "alicePass123"}')
ALICE_TOKEN=$(echo "$ALICE_LOGIN" | jq -r '.token')

# 4. Alice transfers to Bob
curl -s -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": "1234567",
    "toAccountId": "7654321",
    "amount": 3000.00,
    "pin": "111111",
    "description": "Payment to Bob"
  }' | jq '.'

# 5. Check Alice's transaction history
curl -s -X GET "http://localhost:8080/api/transactions/account/1234567?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq '.'

# 6. Login as Bob and check his transaction history
BOB_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "bob_user", "password": "bobPass123"}')
BOB_TOKEN=$(echo "$BOB_LOGIN" | jq -r '.token')

curl -s -X GET "http://localhost:8080/api/transactions/account/7654321?pin=222222" \
  -H "Authorization: Bearer $BOB_TOKEN" | jq '.'
```

---

## Authorization & Business Rules

### Authorization for Get Transactions
- User must be authenticated
- User must be the account owner OR have TELLER role
- Valid PIN required for the account
- Returns 401 if PIN is invalid
- Returns 403 if user doesn't own the account (and is not TELLER)

### Transaction Types
- `DEPOSIT`: Money deposited by teller (positive amount)
- `TRANSFER_OUT`: Money transferred out (negative amount)
- `TRANSFER_IN`: Money transferred in (positive amount)
- `WITHDRAWAL`: Money withdrawn (negative amount)

### Transaction Ordering
- Transactions are typically ordered chronologically
- Most recent transactions may appear first (descending order)
- Check the `transactionDate` field for exact timing

### Data Integrity
- Each transaction records the `balanceAfter` to maintain audit trail
- Transactions are immutable once created
- Every deposit/transfer creates corresponding transaction records

### Error Responses

**Invalid PIN (401):**
```json
{
  "error": "Unauthorized",
  "message": "Invalid PIN"
}
```

**Not Account Owner (403):**
```json
{
  "error": "Forbidden",
  "message": "You can only view transactions for your own accounts"
}
```

**Account Not Found (404):**
```json
{
  "error": "Not Found",
  "message": "Account not found"
}
```

**Transaction Not Found (404):**
```json
{
  "error": "Not Found",
  "message": "Transaction not found"
}
```

---

## Advanced Query Examples

### Get Transaction Summary
```bash
TRANSACTIONS=$(curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN")

echo "Transaction Summary for Account: $ALICE_ACCOUNT_ID"
echo "================================================"

# Count by type
DEPOSITS=$(echo "$TRANSACTIONS" | jq '[.[] | select(.type == "DEPOSIT")] | length')
TRANSFERS_OUT=$(echo "$TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_OUT")] | length')
TRANSFERS_IN=$(echo "$TRANSACTIONS" | jq '[.[] | select(.type == "TRANSFER_IN")] | length')

echo "Deposits: $DEPOSITS"
echo "Transfers Out: $TRANSFERS_OUT"
echo "Transfers In: $TRANSFERS_IN"

# Get current balance (from last transaction)
CURRENT_BALANCE=$(echo "$TRANSACTIONS" | jq -r '.[-1].balanceAfter')
echo "Current Balance: $CURRENT_BALANCE THB"
```

### Export Transactions to CSV Format
```bash
curl -s -X GET "http://localhost:8080/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN" | \
  jq -r '["Date","Type","Amount","Description","Balance"], (.[] | [.transactionDate, .type, .amount, .description, .balanceAfter]) | @csv'
```
