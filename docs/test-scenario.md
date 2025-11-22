# Test Scenarios for Saving Account Module

This document provides comprehensive test scenarios with curl commands to test all business requirements of the banking system.

**Base URL:** `http://localhost:8080`

## Prerequisites

Ensure all services are running:

- Eureka Server (port 8761)
- API Gateway (port 8080)
- Auth Service
- Register Service
- Account Service
- Transaction Service
- Deposit Service
- Transfer Service

---

## Test Scenario 1: Online Registration (Requirement 1)

### Description

A new PERSON registers online by providing email, password, and personal information.

### Test Case 1.1: Successful Registration as PERSON ✅

**Explanation:** New person creates an account with valid credentials.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123",
    "email": "john.doe@example.com",
    "role": "PERSON"
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "PERSON",
    "registeredAt": "2025-11-22T10:30:00"
  },
  "defaultAccountId": 1234567,
  "message": "User registered successfully with default account"
}
```

### Test Case 1.2: Registration with Duplicate Username ✅

**Explanation:** Attempting to register with an already-used username should fail.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "anotherPass456",
    "email": "different.email@example.com",
    "role": "PERSON"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "code": "400",
  "message": "Username 'john_doe' is already taken",
  "timestamp": "2025-11-22T10:35:00Z"
}
```

### Test Case 1.3: Registration with Invalid Email ✅

**Explanation:** Registration should fail with invalid email format.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_smith",
    "password": "password123",
    "email": "invalid-email",
    "role": "PERSON"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "code": "400",
  "message": "Email must be valid",
  "timestamp": "2025-11-22T10:40:00Z"
}
```

### Test Case 1.4: Register TELLER User ✅

**Explanation:** Register a teller who can perform deposits and create accounts.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_alice",
    "password": "tellerPass123",
    "email": "alice.teller@bank.com",
    "role": "TELLER"
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "user": {
    "id": 2,
    "username": "teller_alice",
    "email": "alice.teller@bank.com",
    "role": "TELLER",
    "registeredAt": "2025-11-22T10:45:00"
  },
  "defaultAccountId": 7654321,
  "message": "User registered successfully with default account"
}
```

---

## Test Scenario 2: User Authentication

### Description

Users must authenticate to receive a JWT token for subsequent operations.

### Test Case 2.1: Successful Login ✅

**Explanation:** User logs in with valid credentials to receive JWT token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123"
  }'
```

**Expected Response:** HTTP 200 OK

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEVSU09OIiwidXNlcklkIjoxLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMDYzODAwMCwiZXhwIjoxNzAwNzI0NDAwfQ.abc123...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "role": "PERSON"
}
```

**Note:** Save the token for subsequent requests. Export it as environment variable:

```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEVSU09OIiwidXNlcklkIjoxLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMDYzODAwMCwiZXhwIjoxNzAwNzI0NDAwfQ.abc123..."
```

### Test Case 2.2: Login with Invalid Credentials ✅

**Explanation:** Login should fail with incorrect password.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "wrongPassword"
  }'
```

**Expected Response:** HTTP 401 Unauthorized

```json
{
  "code": "401",
  "message": "Invalid username or password",
  "timestamp": "2025-11-22T11:00:00Z"
}
```

### Test Case 2.3: Validate Token ✅

**Explanation:** Verify that the JWT token is valid and retrieve user information.

```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
{
  "valid": true,
  "userId": 1,
  "username": "john_doe",
  "role": "PERSON"
}
```

---

## Test Scenario 3: Creating a New Account (Requirement 2)

### Description

Any user can create new accounts. The system automatically creates a default SAVINGS account during user registration.

### Test Case 3.1: User Gets Their Default Account ✅

**Explanation:** After registration, a user already has a default SAVINGS account created automatically.

**Step 1:** Register a new user

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_customer",
    "password": "alicePass123",
    "email": "alice@example.com",
    "role": "PERSON"
  }'
```

Save the response - it includes `defaultAccountId`.

**Step 2:** User logs in (save token as $TOKEN)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_customer",
    "password": "alicePass123"
  }'
```

**Step 3:** Get user's default account

```bash
curl -X GET http://localhost:8080/api/accounts/user/3 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
[
  {
    "id": "8152356",
    "userId": 3,
    "balance": 1000.0,
    "accountType": "SAVINGS",
    "createdAt": "2025-11-22T13:16:42.142169"
  }
]
```

---

## Test Scenario 4: Money Deposit (Requirement 3)

### Description

Only TELLER can deposit money to an existing account. The amount must be 1 THB or more.

### Test Case 4.1: Successful Deposit by TELLER ✅

**Explanation:** Teller deposits 1000 THB into an existing account.

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "[accountId]",
    "amount": 1000.00,
    "tellerId": [tellerId],
    "description": "Cash deposit"
  }'
```

**Expected Response:** HTTP 200 OK

```json
{
  "transactionId": 1,
  "accountId": "8152356",
  "amount": 1000.0,
  "newBalance": 1000.0,
  "message": "Deposit successful"
}
```

### Test Case 4.2: Deposit with Minimum Amount (1 THB) ✅

**Explanation:** Verify that minimum deposit of 1 THB is accepted.

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "8152356",
    "amount": 1.00,
    "tellerId": 3,
    "description": "Minimum deposit test"
  }'
```

**Expected Response:** HTTP 200 OK

```json
{
  "transactionId": 2,
  "accountId": "8152356",
  "amount": 1.0,
  "newBalance": 1001.0,
  "message": "Deposit successful"
}
```

### Test Case 4.3: Deposit with Invalid Amount (Less than 1 THB) ✅

**Explanation:** Deposit should fail if amount is less than 1 THB.

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 0.50,
    "tellerId": 2,
    "description": "Invalid deposit amount"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "error": "Amount must be greater than zero"
}
```

### Test Case 4.4: Deposit to Non-Existent Account ✅

**Explanation:** Deposit should fail if account doesn't exist.

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "9999999",
    "amount": 500.00,
    "tellerId": 2,
    "description": "Deposit to invalid account"
  }'
```

**Expected Response:** HTTP 404 Not Found

```json
{
  "status": 404,
  "message": "Account or resource not found",
  "timestamp": "2025-11-22T13:35:56.856604"
}
```

### Test Case 4.5: Non-TELLER User Tries to Deposit ✅

**Explanation:** Regular customer should not be able to perform deposits (only tellers can).

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "accountId": "1234567",
    "amount": 500.00,
    "description": "Unauthorized deposit"
  }'
```

**Expected Response:** HTTP 403 Forbidden

---

## Test Scenario 5: Account Information (Requirement 4)

### Description

Only CUSTOMERS can login and view their account information.

### Test Case 5.1: View Own Account Information ✅

**Explanation:** Customer logs in and views their account details.

**Step 1:** Login as customer

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123"
  }'
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEVSU09OIiwidXNlcklkIjoxLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc2Mzc5MzYwMywiZXhwIjoxNzYzODgwMDAzfQ.hqMUjNuokawWcA7L4VPUTgRRHfSLffBzfbG4G9gkguY",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "role": "PERSON"
}
```

**Step 2:** Get account by account ID (save token as $CUSTOMER_TOKEN)

```bash
curl -X GET http://localhost:8080/api/accounts/[account id] \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
{
  "id": "2230070",
  "userId": 1,
  "balance": 0.0,
  "accountType": "SAVINGS",
  "createdAt": "2025-11-22T13:15:29.010087"
}
```

### Test Case 5.2: View All Accounts by User ID ✅

**Explanation:** Customer views all accounts associated with their user ID.

```bash
curl -X GET http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
[
  {
    "id": "2230070",
    "userId": 1,
    "balance": 0.0,
    "accountType": "SAVINGS",
    "createdAt": "2025-11-22T13:15:29.010087"
  }
]
```

### Test Case 5.3: Unauthorized Access to Another User's Account ✅

**Explanation:** Customer should not be able to access another customer's account (if authorization is properly implemented).

```bash
curl -X GET http://localhost:8080/api/accounts/7654321 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden (if proper authorization is implemented)

---

## Test Scenario 6: Money Transfer (Requirement 5)

### Description

Only CUSTOMER can transfer money from their own account to any other existing account after login with PIN confirmation.

### Test Case 6.1: Successful Money Transfer ✅

**Explanation:** Customer transfers 500 THB from their account to another account.

**Prerequisites:**

- Customer account (1234567) has sufficient balance
- Destination account (7654321) exists

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "fromAccountId": 1234567,
    "toAccountId": 7654321,
    "amount": 500.00,
    "description": "Payment for services"
  }'
```

**Expected Response:** HTTP 200 OK

```json
{
  "transactionId": 1002,
  "fromAccountId": 1234567,
  "toAccountId": 7654321,
  "amount": 500.0,
  "sourceNewBalance": 501.0,
  "destinationNewBalance": 5500.0,
  "timestamp": "2025-11-22T12:00:00",
  "message": "Transfer successful"
}
```

### Test Case 6.2: Transfer with Insufficient Funds ✅

**Explanation:** Transfer should fail if source account has insufficient balance.

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "fromAccountId": 1234567,
    "toAccountId": 7654321,
    "amount": 10000.00,
    "description": "Large transfer"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "error": "Insufficient funds"
}
```

### Test Case 6.3: Transfer to Non-Existent Account ✅

**Explanation:** Transfer should fail if destination account doesn't exist.

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "fromAccountId": 1234567,
    "toAccountId": 9999999,
    "amount": 100.00,
    "description": "Transfer to invalid account"
  }'
```

**Expected Response:** HTTP 404 Not Found

```json
{
  "error": "Destination account not found"
}
```

### Test Case 6.4: Create Additional Account for User ✅

**Explanation:** User can create additional SAVINGS accounts beyond the default one.

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": 3,
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "id": "7654321",
  "userId": 3,
  "accountType": "SAVINGS",
  "balance": 0.0,
  "status": "ACTIVE",
  "createdAt": "2025-11-22T10:40:00"
}
```

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "fromAccountId": 1234567,
    "toAccountId": 1234567,
    "amount": 100.00,
    "description": "Self transfer"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "error": "Cannot transfer to the same account"
}
```

### Test Case 6.5: Transfer with Zero or Negative Amount ✅

**Explanation:** Transfer should fail with invalid amount.

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "fromAccountId": 1234567,
    "toAccountId": 7654321,
    "amount": 0.00,
    "description": "Zero amount transfer"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "error": "Amount must be greater than zero"
}
```

### Test Case 6.6: Transfer by Non-Owner of Source Account ✅

---

## Test Scenario 7: Bank Statement (Requirement 6)

### Description

Only CUSTOMER can request their bank statement for a specific month after login with PIN confirmation. Transactions must be displayed from past to present.

### Test Case 7.1: Get All Transactions for an Account ✅

**Explanation:** Customer retrieves all transaction history for their account, sorted chronologically.

```bash
curl -X GET http://localhost:8080/api/transactions/account/2230070 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
[
  {
    "id": 7,
    "accountId": 2230070,
    "type": "TRANSFER_OUT",
    "amount": 500.0,
    "relatedAccountId": 8152356,
    "description": "Payment for services",
    "timestamp": "2025-11-22T13:51:04.974163",
    "status": "COMPLETED"
  },
  {
    "id": 6,
    "accountId": 2230070,
    "type": "TRANSFER_IN",
    "amount": 500.0,
    "relatedAccountId": 8152356,
    "description": "Payment for services",
    "timestamp": "2025-11-22T13:50:06.9551",
    "status": "COMPLETED"
  }
]
```

### Test Case 7.2: Get Specific Transaction by ID ✅

**Explanation:** Customer views details of a specific transaction.

```bash
curl -X GET http://localhost:8080/api/transactions/1001 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
{
  "transactionId": 1001,
  "accountId": "1234567",
  "transactionType": "DEPOSIT",
  "amount": 1000.0,
  "balanceAfter": 1000.0,
  "description": "Cash deposit",
  "timestamp": "2025-11-22T11:00:00",
  "relatedAccountId": null
}
```

### Test Case 7.3: Unauthorized Access to Another Account's Transactions ✅

**Explanation:** Customer should not be able to view another customer's transactions.

```bash
curl -X GET http://localhost:8080/api/transactions/account/7654321 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden (if proper authorization is implemented)

---

## Test Scenario 8: Complete End-to-End Flow

### Description

A comprehensive test that covers the entire user journey from registration to money transfer.

### Step 1: Register Two Users (Person A and Person B)

**Person A Registration:**

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_customer",
    "password": "alicePass123",
    "email": "alice@example.com",
    "role": "PERSON"
  }'
```

**Person B Registration:**

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_customer",
    "password": "bobPass123",
    "email": "bob@example.com",
    "role": "PERSON"
  }'
```

### Step 2: Register a Teller

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_carol",
    "password": "carolPass123",
    "email": "carol@bank.com",
    "role": "TELLER"
  }'
```

### Step 3: Teller Logs In

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_carol",
    "password": "carolPass123"
  }'
```

### Step 4: Teller Creates Accounts for Both Users

**Create account for Alice:**

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 3,
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

**Create account for Bob:**

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 4,
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

### Step 5: Teller Deposits Money to Alice's Account

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "accountId": "ALICE_ACCOUNT_ID",
    "amount": 5000.00,
    "tellerId": 5,
    "description": "Initial deposit"
  }'
```

### Step 6: Alice Logs In

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_customer",
    "password": "alicePass123"
  }'
```

### Step 7: Alice Views Her Account Information

```bash
curl -X GET http://localhost:8080/api/accounts/user/3 \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

### Step 8: Alice Transfers Money to Bob

```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{
    "fromAccountId": ALICE_ACCOUNT_ID,
    "toAccountId": BOB_ACCOUNT_ID,
    "amount": 1000.00,
    "description": "Payment to Bob"
  }'
```

### Step 9: Alice Views Her Bank Statement

```bash
curl -X GET http://localhost:8080/api/transactions/account/ALICE_ACCOUNT_ID \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

### Step 10: Bob Logs In and Checks His Balance

**Bob Login:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_customer",
    "password": "bobPass123"
  }'
```

**Check Balance:**

```bash
curl -X GET http://localhost:8080/api/accounts/user/4 \
  -H "Authorization: Bearer $BOB_TOKEN"
```

---

## Test Scenario 9: Health Check Endpoints

### Description

Verify all services are running correctly.

### Test All Service Health Endpoints

```bash
# Auth Service
curl http://localhost:8080/api/auth/health

# Register Service
curl http://localhost:8080/api/register/health

# Account Service
curl http://localhost:8080/api/accounts/health

# Transaction Service
curl http://localhost:8080/api/transactions/health

# Deposit Service
curl http://localhost:8080/api/deposit/health

# Transfer Service
curl http://localhost:8080/api/transfer/health

# Eureka Server
curl http://localhost:8761/actuator/health
```

**Expected Response for each:** HTTP 200 OK

---

## Test Scenario 10: Error Handling

### Test Case 10.1: Request Without Authentication ⏳

**Explanation:** Protected endpoints should reject requests without JWT token.

```bash
curl -X GET http://localhost:8080/api/accounts/user/1
```

**Expected Response:** HTTP 401 Unauthorized

### Test Case 10.2: Request With Expired Token ⏳

**Explanation:** System should reject expired JWT tokens.

```bash
curl -X GET http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer EXPIRED_TOKEN_HERE"
```

**Expected Response:** HTTP 401 Unauthorized

### Test Case 10.3: Malformed Request Body ⏳

**Explanation:** API should handle malformed JSON gracefully.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{invalid json}'
```

**Expected Response:** HTTP 400 Bad Request

---

## Notes

1. **Account ID Format:** The system generates 7-digit numeric account numbers (e.g., "1234567")
2. **Roles:** The system has three roles: PERSON, CUSTOMER, and TELLER
3. **Authentication:** Most operations require a valid JWT token in the Authorization header
4. **Transaction Ordering:** Bank statements show transactions from past to present (chronologically)
5. **Minimum Deposit:** Deposits must be at least 1 THB
6. **API Gateway:** All requests go through the API gateway at port 8080 with `/api` prefix

## Tips for Testing

1. **Save tokens as environment variables** to reuse them:

   ```bash
   export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"john_doe","password":"securePass123"}' | jq -r '.token')
   ```

2. **Pretty print JSON responses** using jq:

   ```bash
   curl http://localhost:8080/api/accounts/user/1 \
     -H "Authorization: Bearer $TOKEN" | jq .
   ```

3. **Check HTTP status code** only:

   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/health
   ```

4. **Verbose output** for debugging:
   ```bash
   curl -v http://localhost:8080/api/auth/health
   ```
