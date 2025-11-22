# Sequence Diagrams - Banking System

This folder contains detailed sequence diagrams for all business features of the Banking System microservices architecture.

## Diagrams Overview

### 1. Online Registration (`1-online-registration.mmd`)
**Actor:** PERSON (new user)
**Flow:**
- PERSON submits registration form (email, password, personal info, PIN)
- Register Service validates and creates user account with hashed credentials
- Automatically creates a default savings account
- Returns userId and account number

**Key Points:**
- Password and PIN are hashed using BCrypt
- Validates email and citizenId uniqueness
- Creates default account automatically
- Role assigned: PERSON

---

### 2. Creating a New Account (`2-create-new-account.mmd`)
**Actor:** TELLER (authenticated)
**Customer:** CUSTOMER or PERSON
**Flow:**
- Customer visits branch and provides identification (citizenId, names)
- TELLER logs in with JWT authentication
- TELLER creates account with generated 7-digit account number
- **Option 1:** Account with 0 balance
- **Option 2:** Account with initial deposit (logs transaction)

**Key Points:**
- TELLER must be authenticated (role=TELLER)
- System generates unique 7-digit account number
- Initial deposit must be >= 1 THB (if provided)
- Transaction logged for deposits

---

### 3. Money Deposit (`3-money-deposit.mmd`)
**Actor:** TELLER (authenticated)
**Customer:** CUSTOMER
**Flow:**
1. TELLER authenticates with JWT token
2. Validates account number exists
3. Validates deposit amount >= 1 THB
4. Updates account balance (adds amount)
5. Logs transaction in Transaction Service

**Key Points:**
- Only TELLER can perform deposits
- Minimum deposit: 1 THB
- Transaction logged with teller ID
- Account validation before processing

---

### 4. Account Information (`4-account-information.mmd`)
**Actor:** CUSTOMER (authenticated)
**Flow:**
1. CUSTOMER logs in with email/password
2. Receives JWT token containing userId
3. Requests account information
4. System fetches all accounts belonging to the user
5. Returns account details (number, balance, type, creation date)

**Key Points:**
- Only authenticated CUSTOMER can view accounts
- userId extracted from JWT token
- Shows all accounts owned by the user
- No PIN required for viewing

---

### 5. Money Transfer (`5-money-transfer.mmd`)
**Actor:** CUSTOMER (authenticated)
**Flow:**
1. CUSTOMER logs in and receives JWT token
2. Initiates transfer with PIN confirmation
3. System validates PIN via Auth Service
4. Validates sender account ownership and sufficient funds
5. Validates receiver account exists
6. Deducts from sender account
7. Adds to receiver account
8. Logs two transactions (TRANSFER_OUT, TRANSFER_IN)

**Key Points:**
- Requires PIN confirmation
- Validates account ownership
- Checks sufficient funds
- Prevents same-account transfers
- Creates two transaction records
- **Note:** Simplified synchronous flow (no Saga pattern for 48h constraint)

---

### 6. Bank Statement (`6-bank-statement.mmd`)
**Actor:** CUSTOMER (authenticated)
**Flow:**
1. CUSTOMER logs in with JWT token
2. Requests statement for specific month with PIN confirmation
3. System verifies PIN
4. Validates account ownership
5. Fetches transactions for the specified month
6. Returns transactions ordered from **past to present**

**Key Points:**
- Requires PIN confirmation
- Month format: YYYY-MM (e.g., "2025-11")
- Validates account ownership
- Transactions sorted chronologically (ASC)
- Shows starting and ending balance
- Includes transaction type, amount, balance after each transaction

---

## Architecture Components

All diagrams follow the microservices architecture:

```
Client → API Gateway → Service Layer → Data Services → Databases
```

### Services:
- **API Gateway:** Entry point, JWT validation, routing
- **Auth Service:** Login, JWT generation, PIN verification
- **Register Service:** User registration, default account creation
- **Account Service:** Account CRUD, balance management
- **Transaction Service:** Transaction logging, statement generation
- **Deposit Service:** Orchestrates deposit operations
- **Transfer Service:** Orchestrates transfer operations

### Databases:
- **Users DB:** User credentials, personal info, roles
- **Accounts DB:** Account details, balances
- **Transactions DB:** Transaction history

---

## Security Features

### Authentication & Authorization:
1. **JWT Tokens:** Bearer token authentication for all protected endpoints
2. **Role-Based Access:**
   - PERSON: Registration, login
   - CUSTOMER: View accounts, transfer money, view statements
   - TELLER: Create accounts, deposit money
3. **PIN Verification:** Required for sensitive operations (transfer, statement)
4. **BCrypt Hashing:** For passwords and PINs

### Validation:
- Account ownership verification
- Sufficient funds checking
- Minimum amount validation
- Unique identifier checks (email, citizenId)

---

## How to View Diagrams

### Option 1: VS Code with Mermaid Extension
Install: [Markdown Preview Mermaid Support](https://marketplace.visualstudio.com/items?itemName=bierner.markdown-mermaid)

### Option 2: Mermaid Live Editor
Visit: https://mermaid.live/
Copy and paste the diagram content

### Option 3: GitHub
GitHub natively renders Mermaid diagrams in Markdown files

---

## Notes on Implementation

- **Synchronous Communication:** All services use REST APIs via Feign clients
- **No Distributed Transactions:** Simplified flow for 48-hour constraint (acceptable tradeoff)
- **Service Discovery:** All services registered with Eureka Server
- **Error Handling:** Each diagram shows error paths and validation failures
- **Database Isolation:** Each service owns its database (no cross-database queries)

---

## Testing Scenarios

Use these diagrams to create test cases:

1. ✅ Happy path for each feature
2. ❌ Invalid credentials / authentication failures
3. ❌ Invalid PIN verification
4. ❌ Insufficient funds (transfer)
5. ❌ Invalid account numbers
6. ❌ Unauthorized access attempts
7. ❌ Missing required fields
8. ❌ Amount validation failures

---

## Future Enhancements (Not in 48h scope)

- [ ] Distributed transaction handling (Saga pattern)
- [ ] Circuit breakers (Resilience4j)
- [ ] Event-driven architecture (Kafka/RabbitMQ)
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] Advanced security (refresh tokens, MFA)
- [ ] Rate limiting
- [ ] Caching layer (Redis)
- [ ] Audit logging

---

Last Updated: November 22, 2025
