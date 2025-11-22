# Test Scenarios for Saving Account Module

This document provides comprehensive test scenarios with curl commands to test all business requirements of the banking system.

**Base URL:** `http://localhost:8080`

## Table of Contents

- [Prerequisites](#prerequisites)
- [Test Scenario 1: Online Registration (Requirement 1)](#test-scenario-1-online-registration-requirement-1)
  - [Test Case 1.1: Successful Registration as CUSTOMER ✅](#test-case-11-successful-registration-as-person-)
  - [Test Case 1.2: Registration with Duplicate Username ✅](#test-case-12-registration-with-duplicate-username-)
  - [Test Case 1.3: Registration with Invalid Email ✅](#test-case-13-registration-with-invalid-email-)
  - [Test Case 1.4: Registration with Duplicate Citizen ID ✅](#test-case-14-registration-with-duplicate-citizen-id-)
  - [Test Case 1.5: Registration with Invalid Citizen ID Format ✅](#test-case-15-registration-with-invalid-citizen-id-format-)
  - [Test Case 1.6: Registration with Invalid PIN Format ✅](#test-case-16-registration-with-invalid-pin-format-)
  - [Test Case 1.7: Register TELLER User ✅](#test-case-17-register-teller-user-)
- [Test Scenario 2: User Authentication](#test-scenario-2-user-authentication)
  - [Test Case 2.1: Successful Login ✅](#test-case-21-successful-login-)
  - [Test Case 2.2: Login with Invalid Credentials ✅](#test-case-22-login-with-invalid-credentials-)
  - [Test Case 2.3: Validate Token ✅](#test-case-23-validate-token-)
- [Test Scenario 3: Creating a New Account (Requirement 2)](#test-scenario-3-creating-a-new-account-requirement-2)
  - [Test Case 3.1: Teller Creates Account for User ✅](#test-case-31-teller-creates-account-for-user-)
  - [Test Case 3.2: Create Account with Invalid Citizen ID ✅](#test-case-32-create-account-with-invalid-citizen-id-)
  - [Test Case 3.3: Create Account without Citizen ID ✅](#test-case-33-create-account-without-citizen-id-)
- [Test Scenario 4: Money Deposit (Requirement 3)](#test-scenario-4-money-deposit-requirement-3)
  - [Test Case 4.1: Successful Deposit by TELLER ✅](#test-case-41-successful-deposit-by-teller-)
  - [Test Case 4.2: Deposit with Minimum Amount (1 THB) ✅](#test-case-42-deposit-with-minimum-amount-1-thb-)
  - [Test Case 4.3: Deposit with Invalid Amount (Less than 1 THB) ✅](#test-case-43-deposit-with-invalid-amount-less-than-1-thb-)
  - [Test Case 4.4: Deposit to Non-Existent Account ✅](#test-case-44-deposit-to-non-existent-account-)
  - [Test Case 4.5: Non-TELLER User Tries to Deposit ✅](#test-case-45-non-teller-user-tries-to-deposit-)
- [Test Scenario 5: Account Information (Requirement 4)](#test-scenario-5-account-information-requirement-4)
  - [Test Case 5.1: View Own Account Information ✅](#test-case-51-view-own-account-information-)
  - [Test Case 5.2: View All Accounts by User ID ✅](#test-case-52-view-all-accounts-by-user-id-)
  - [Test Case 5.3: Unauthorized Access to Another User's Account ✅](#test-case-53-unauthorized-access-to-another-users-account-)
  - [Test Case 5.4: TELLER Tries to View Account Information ✅](#test-case-54-teller-tries-to-view-account-information-)
  - [Test Case 5.5: CUSTOMER Without Account Tries to View Account Information ✅](#test-case-55-person-tries-to-view-account-information-)
- [Test Scenario 6: Money Transfer (Requirement 5)](#test-scenario-6-money-transfer-requirement-5)
  - [Test Case 6.1: Successful Money Transfer ✅](#test-case-61-successful-money-transfer-)
  - [Test Case 6.2: Transfer with Insufficient Funds ✅](#test-case-62-transfer-with-insufficient-funds-)
  - [Test Case 6.3: Transfer to Non-Existent Account ✅](#test-case-63-transfer-to-non-existent-account-)
  - [Test Case 6.4: Create Additional Account for User ✅](#test-case-64-create-additional-account-for-user-)
  - [Test Case 6.5: Transfer with Zero or Negative Amount ✅](#test-case-65-transfer-with-zero-or-negative-amount-)
  - [Test Case 6.6: Transfer by Non-Owner of Source Account ✅](#test-case-66-transfer-by-non-owner-of-source-account-)
- [Test Scenario 7: Bank Statement (Requirement 6)](#test-scenario-7-bank-statement-requirement-6)
  - [Test Case 7.1: Get All Transactions for an Account ✅](#test-case-71-get-all-transactions-for-an-account-)
  - [Test Case 7.2: Get Specific Transaction by ID ✅](#test-case-72-get-specific-transaction-by-id-)
  - [Test Case 7.3: Unauthorized Access to Another Account's Transactions ✅](#test-case-73-unauthorized-access-to-another-accounts-transactions-)
  - [Test Case 7.4: Get Transactions with Invalid PIN ✅](#test-case-74-get-transactions-with-invalid-pin-)
  - [Test Case 7.5: Get Transactions without PIN ✅](#test-case-75-get-transactions-without-pin-)
- [Test Scenario 8: Complete End-to-End Flow](#test-scenario-8-complete-end-to-end-flow)
- [Test Scenario 9: Health Check Endpoints](#test-scenario-9-health-check-endpoints)
- [Test Scenario 10: Error Handling](#test-scenario-10-error-handling)
  - [Test Case 10.1: Request Without Authentication ⏳](#test-case-101-request-without-authentication-)
  - [Test Case 10.2: Request With Expired Token ⏳](#test-case-102-request-with-expired-token-)
  - [Test Case 10.3: Malformed Request Body ⏳](#test-case-103-malformed-request-body-)
- [Notes](#notes)
- [Tips for Testing](#tips-for-testing)

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

A new CUSTOMER registers online by providing email, password, and personal information.

### Test Case 1.1: Successful Registration as CUSTOMER ✅

**Explanation:** New person creates an account with valid credentials including Thai citizen ID, Thai name, English name, and 6-digit PIN.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123",
    "email": "john.doe@example.com",
    "citizenId": "1234567890123",
    "thaiName": "สมชาย ใจดี",
    "englishName": "Somchai Jaidee",
    "pin": "123456",
    "role": "CUSTOMER"
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "citizenId": "1234567******",
    "thaiName": "สมชาย ใจดี",
    "englishName": "Somchai Jaidee",
    "role": "CUSTOMER",
    "password": "$2a$10$Z9EYUUAaP/zI6zxVO3OU0.xpoqvnsv70ObfX56ySna8X2NwniWkta",
    "registeredAt": "2025-11-22T21:55:55.012532"
  },
  "defaultAccountId": null,
  "message": "User registered successfully"
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
    "citizenId": "9876543210987",
    "thaiName": "สมหญิง ดีใจ",
    "englishName": "Somying Deejai",
    "pin": "654321",
    "role": "CUSTOMER"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T22:15:24.037484",
  "status": 409,
  "error": "Conflict",
  "message": "Username 'john_doe' is already taken"
}
```

### Test Case 1.4: Registration with Duplicate Citizen ID ✅

**Explanation:** Registration should fail when citizen ID is already registered.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "another_user",
    "password": "password123",
    "email": "another@example.com",
    "citizenId": "1234567890123",
    "thaiName": "คนอื่น อื่น",
    "englishName": "Another Person",
    "pin": "888888",
    "role": "CUSTOMER"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T22:15:40.58861",
  "status": 409,
  "error": "Conflict",
  "message": "Citizen ID '1234567890123' is already registered"
}
```

### Test Case 1.5: Registration with Invalid Citizen ID Format ✅

**Explanation:** Registration should fail when citizen ID is not exactly 13 digits.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "password123",
    "email": "test@example.com",
    "citizenId": "12345",
    "thaiName": "ทดสอบ ระบบ",
    "englishName": "Test System",
    "pin": "777777",
    "role": "CUSTOMER"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T22:09:49.935817",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Citizen ID must be exactly 13 digits"
}
```

### Test Case 1.6: Registration with Invalid PIN Format ✅

**Explanation:** Registration should fail when PIN is not exactly 6 digits.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "pin_test",
    "password": "password123",
    "email": "pintest@example.com",
    "citizenId": "7777777777777",
    "thaiName": "ทดสอบ พิน",
    "englishName": "Test Pin",
    "pin": "123",
    "role": "CUSTOMER"
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T22:16:08.699064",
  "status": 500,
  "error": "Internal Server Error",
  "message": "PIN must be exactly 6 digits"
}
```

### Test Case 1.7: Register TELLER User ✅

**Explanation:** Register a teller who can perform deposits and create accounts.

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_alice",
    "password": "tellerPass123",
    "email": "alice.teller@bank.com",
    "citizenId": "1111111111111",
    "thaiName": "อลิซ เทลเลอร์",
    "englishName": "Alice Teller",
    "pin": "999999",
    "role": "TELLER"
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "user": {
    "id": 3,
    "username": "teller_alice",
    "email": "alice.teller@bank.com",
    "citizenId": "1111111******",
    "thaiName": "อลิซ เทลเลอร์",
    "englishName": "Alice Teller",
    "role": "TELLER",
    "password": "$2a$10$6SFv59d9rYE2Yqcj4oJYt.AvbcEsje83.q9pKj3wOFgBVEtTUWE1q",
    "registeredAt": "2025-11-22T22:16:31.718679"
  },
  "defaultAccountId": null,
  "message": "User registered successfully"
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
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEVSU09OIiwidXNlcklkIjoxLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc2MzgyNDY3NywiZXhwIjoxNzYzOTExMDc3fQ.8YHoQuAQYsutprTby29MsxBLK5x0bINS43eTd8uRBTA",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "role": "CUSTOMER"
}
```

**Note:** Save the token for subsequent requests. Export it as environment variable:

```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEVSU09OIiwidXNlcklkIjoxLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc2MzgyNDY3NywiZXhwIjoxNzYzOTExMDc3fQ.8YHoQuAQYsutprTby29MsxBLK5x0bINS43eTd8uRBTA"
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
  "role": "CUSTOMER"
}
```

---

## Test Scenario 3: Creating a New Account (Requirement 2)

### Description

Tellers can create new accounts for users. Accounts are not automatically created during registration.

### Test Case 3.1: Teller Creates Account for User ✅

**Explanation:** A teller creates a new SAVINGS account for a registered user. The citizen ID must be provided and validated against the user ID.

**Step 1:** Teller logs in

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_alice",
    "password": "tellerPass123"
  }'
```

**Step 2:** Teller creates account for user (must provide citizen ID for verification)

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 1,
    "citizenId": "1234567890123",
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

**Expected Response:** HTTP 201 Created

```json
{
  "id": "8152356",
  "userId": 1,
  "accountType": "SAVINGS",
  "balance": 0.0,
  "status": "ACTIVE",
  "createdAt": "2025-11-22T10:40:00"
}
```

**Step 3:** User logs in and views their account

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123"
  }'
```

```bash
curl -X GET http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** HTTP 200 OK

```json
[
  {
    "id": "8152356",
    "userId": 1,
    "balance": 0.0,
    "accountType": "SAVINGS",
    "createdAt": "2025-11-22T13:16:42.142169"
  }
]
```

### Test Case 3.2: Create Account with Invalid Citizen ID ✅

**Explanation:** Account creation should fail if the provided citizen ID doesn't match the user's registered citizen ID.

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 1,
    "citizenId": "9999999999999",
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T10:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Citizen ID does not match the user ID. Please verify your citizen ID.",
  "path": "/api/accounts/create"
}
```

### Test Case 3.3: Create Account without Citizen ID ✅

**Explanation:** Account creation should fail if citizen ID is not provided.

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 1,
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "timestamp": "2025-11-22T10:46:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Citizen ID is required",
  "path": "/api/accounts/create"
}
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

**Explanation:** Customer logs in and views their account details. **IMPORTANT: Only users with CUSTOMER role can access account information.**

**Step 1:** Login as customer (user must have CUSTOMER role)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePass123"
  }'
```

Example response (note the role must be "CUSTOMER"):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQ1VTVE9NRVIiLCJ1c2VySWQiOjEsInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzYzNzkzNjAzLCJleHAiOjE3NjM4ODAwMDN9.hqMUjNuokawWcA7L4VPUTgRRHfSLffBzfbG4G9gkguY",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "role": "CUSTOMER"
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

**Explanation:** Customer views all accounts associated with their user ID. The token must be from a user with CUSTOMER role.

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

**Explanation:** Customer should not be able to access another customer's account.

```bash
curl -X GET http://localhost:8080/api/accounts/7654321 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden

```json
{
  "status": 403,
  "message": "You are not authorized to access this account",
  "timestamp": "2025-11-22T13:20:00"
}
```

### Test Case 5.4: TELLER Tries to View Account Information ✅

**Explanation:** TELLER role should not be able to view account information (only CUSTOMER can).

```bash
# Login as teller
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teller_alice",
    "password": "tellerPass123"
  }'

# Try to access account (save teller token as $TELLER_TOKEN)
curl -X GET http://localhost:8080/api/accounts/1234567 \
  -H "Authorization: Bearer $TELLER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden

```json
{
  "status": 403,
  "message": "Only customers are authorized to view account information",
  "timestamp": "2025-11-22T13:20:00"
}
```

### Test Case 5.5: CUSTOMER Without Account Tries to View Account Information ✅

**Explanation:** Users without CUSTOMER accounts should not be able to view account information (only CUSTOMER can).

```bash
# Login as customer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_customer",
    "password": "customerPass123"
  }'

# Try to access account (save person token as $CUSTOMER_TOKEN)
curl -X GET http://localhost:8080/api/accounts/1234567 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden

```json
{
  "status": 403,
  "message": "Only customers are authorized to view account information",
  "timestamp": "2025-11-22T13:20:00"
}
```

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

**Explanation:** Teller can create additional SAVINGS accounts for users beyond the first one. Citizen ID must be provided for verification.

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 3,
    "citizenId": "3101234567890",
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

Only CUSTOMER can request their bank statement for a specific month after login with PIN confirmation. **The PIN must be provided as a query parameter** to verify the user's identity before displaying sensitive transaction information. Transactions must be displayed from past to present.

### Test Case 7.1: Get All Transactions for an Account ✅

**Explanation:** Customer retrieves all transaction history for their account, sorted chronologically. **PIN is required** for security verification.

```bash
curl -X GET "http://localhost:8080/api/transactions/account/2230070?pin=111111" \
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
curl -X GET "http://localhost:8080/api/transactions/account/7654321?pin=111111" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 403 Forbidden (if proper authorization is implemented)

### Test Case 7.4: Get Transactions with Invalid PIN ✅

**Explanation:** Request should fail if PIN is incorrect.

```bash
curl -X GET "http://localhost:8080/api/transactions/account/2230070?pin=999999" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 401 Unauthorized

```json
{
  "status": 401,
  "message": "Invalid PIN",
  "timestamp": "2025-11-22T13:55:00"
}
```

### Test Case 7.5: Get Transactions without PIN ✅

**Explanation:** Request should fail if PIN parameter is missing.

```bash
curl -X GET "http://localhost:8080/api/transactions/account/2230070" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Expected Response:** HTTP 400 Bad Request

```json
{
  "status": 400,
  "message": "Required request parameter 'pin' is not present",
  "timestamp": "2025-11-22T13:56:00"
}
```

---

## Test Scenario 8: Complete End-to-End Flow

### Description

A comprehensive test that covers the entire user journey from registration to money transfer.

### Step 1: Register Two Users (Customer A and Customer B)

**Customer A Registration:**

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_customer",
    "password": "alicePass123",
    "email": "alice@example.com",
    "citizenId": "3101234567890",
    "thaiName": "อลิซ คัสโตเมอร์",
    "englishName": "Alice Customer",
    "pin": "111111",
    "role": "CUSTOMER"
  }'
```

**Customer B Registration:**

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_customer",
    "password": "bobPass123",
    "email": "bob@example.com",
    "citizenId": "3109876543210",
    "thaiName": "บ็อบ คัสโตเมอร์",
    "englishName": "Bob Customer",
    "pin": "222222",
    "role": "CUSTOMER"
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
    "citizenId": "3105555555555",
    "thaiName": "แครอล เทลเลอร์",
    "englishName": "Carol Teller",
    "pin": "999999",
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

**Note:** Users must have accounts created by a teller before they can perform banking operations.

**Create account for Alice (must provide her citizen ID for verification):**

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 3,
    "citizenId": "3101234567890",
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

Save the account ID from the response (e.g., "ALICE_ACCOUNT_ID").

**Create account for Bob (must provide his citizen ID for verification):**

```bash
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": 4,
    "citizenId": "3109876543210",
    "accountType": "SAVINGS",
    "initialBalance": 0
  }'
```

Save the account ID from the response (e.g., "BOB_ACCOUNT_ID").

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

**Note:** PIN is required to view transaction history for security verification.

```bash
curl -X GET "http://localhost:8080/api/transactions/account/ALICE_ACCOUNT_ID?pin=111111" \
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
2. **Roles:** The system has two roles: CUSTOMER and TELLER
3. **Authentication:** Most operations require a valid JWT token in the Authorization header
4. **Transaction Ordering:** Bank statements show transactions from past to present (chronologically)
5. **Minimum Deposit:** Deposits must be at least 1 THB
6. **API Gateway:** All requests go through the API gateway at port 8080 with `/api` prefix
7. **Account Creation:** Accounts are NOT automatically created during registration. A teller must create accounts for users and must provide the user's citizen ID for verification.
8. **Citizen ID Validation:** When creating a new account, the provided citizen ID must match the user's registered citizen ID to ensure proper identity verification.
9. **Registration Requirements:** All users must provide:
   - Email (valid format)
   - Password (minimum 6 characters)
   - Citizen ID (exactly 13 digits, Thai national ID)
   - Thai Name (full name in Thai)
   - English Name (full name in English)
   - PIN (exactly 6 digits, used for transactions)
10. **Security:**

- Passwords and PINs are hashed using BCrypt
- Citizen ID is masked in responses (shows only first 7 digits + "**\*\***")
- PIN is never returned in API responses
- Citizen ID validation prevents unauthorized account creation by verifying identity
- **PIN verification required** for viewing transaction history - users must provide their 6-digit PIN as a query parameter (`?pin=123456`) when accessing transaction records
- TELLERs can view any transaction without PIN requirement (for customer service purposes)

11. **Role-Based Authorization:**

- **CUSTOMER** role: Can view their own account information (`GET /accounts/{id}`, `GET /accounts/user/{userId}`) and transfer money
- **TELLER** role: Can create accounts and make deposits (cannot view account information)
- **CUSTOMER** role: Registration and account access (cannot view accounts, transfer, or deposit)
- Each endpoint enforces role requirements - attempting to access with wrong role returns HTTP 403 Forbidden

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
