# CURL Commands for Banking Microservices

This directory contains comprehensive CURL documentation for all banking microservices. Each file includes complete examples, authorization details, and common use cases.

## Services Documentation

### Core Services
1. **[Auth Service](./auth-service.md)** - Authentication and JWT token management
2. **[Register Service](./register-service.md)** - User registration and management
3. **[Account Service](./account-service.md)** - Bank account management

### Orchestrator Services
4. **[Deposit Service](./deposit-service.md)** - Deposit operations (TELLER only)
5. **[Transfer Service](./transfer-service.md)** - Fund transfer operations
6. **[Transaction Service](./transaction-service.md)** - Transaction history and logging

## Quick Start

### Prerequisites
- Services running on `http://localhost:8080`
- `jq` installed for JSON parsing: `brew install jq` (macOS) or `apt-get install jq` (Linux)

### Basic E2E Flow

```bash
# 1. Register users
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

# 2. Login and get token
ALICE_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice_user", "password": "alicePass123"}')

ALICE_TOKEN=$(echo "$ALICE_LOGIN" | jq -r '.token')
ALICE_USER_ID=$(echo "$ALICE_LOGIN" | jq -r '.userId')

# 3. Create account (requires TELLER token)
curl -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{
    "userId": '$ALICE_USER_ID',
    "citizenId": "3101234567890",
    "accountType": "SAVINGS",
    "initialBalance": 10000.00
  }'

# 4. View account
curl -X GET http://localhost:8080/api/accounts/user/$ALICE_USER_ID \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

## Complete Test Scenario

For a full end-to-end test with validation, see the automated test script:
- **[test-e2e-validated.sh](../../test-e2e-validated.sh)** - Complete automated E2E test with balance validation

Run it with:
```bash
cd /path/to/app-design-backend
./test-e2e-validated.sh
```

## Service Structure

```
┌─────────────────┐
│   API Gateway   │  (Port 8080)
│  (Routing Layer)│
└────────┬────────┘
         │
         ├──────────────────────────────────────────────┐
         │                                              │
         ▼                                              ▼
┌────────────────┐                            ┌────────────────┐
│  Auth Service  │                            │ Register Svc   │
│  /api/auth/*   │                            │ /api/register/*│
└────────────────┘                            └────────────────┘
         │                                              │
         │                                              │
         ▼                                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Account Service                          │
│                    /api/accounts/*                          │
└────────────────────────────┬────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│ Deposit Svc    │  │ Transfer Svc   │  │Transaction Svc │
│ /api/deposit   │  │ /api/transfer  │  │/api/transactions│
└────────────────┘  └────────────────┘  └────────────────┘
```

## Authorization Matrix

| Service | Endpoint | CUSTOMER | TELLER | PIN Required |
|---------|----------|----------|--------|--------------|
| Auth | POST /auth/login | ✅ | ✅ | ❌ |
| Auth | GET /auth/validate | ✅ | ✅ | ❌ |
| Register | POST /register | ✅ (self) | ✅ | ❌ |
| Register | GET /register/user/* | ✅ | ✅ | ❌ |
| Register | POST /register/validate-pin | ✅ | ✅ | ✅ |
| Account | POST /accounts/create | ❌ | ✅ | ❌ |
| Account | GET /accounts/{id} | ✅ (own) | ✅ | ❌ |
| Account | GET /accounts/user/{userId} | ✅ (own) | ✅ | ❌ |
| Account | PUT /accounts/{id}/balance | Internal | Internal | ❌ |
| Deposit | POST /deposit | ❌ | ✅ | ❌ |
| Transfer | POST /transfer | ✅ (own) | ✅ | ✅ |
| Transaction | GET /transactions/account/{id} | ✅ (own) | ✅ | ✅ |
| Transaction | GET /transactions/{id} | ✅ (own) | ✅ | ❌ |
| Transaction | POST /transactions | Internal | Internal | ❌ |

**Legend:**
- ✅ = Allowed
- ❌ = Not allowed
- (own) = Only for own resources
- Internal = Used internally by orchestrator services

## Common Patterns

### 1. Authentication Flow
```bash
# Login
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "USERNAME", "password": "PASSWORD"}')

# Extract token and user info
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.userId')
ROLE=$(echo "$LOGIN_RESPONSE" | jq -r '.role')
```

### 2. Using JWT Token
```bash
# Add token to Authorization header
curl -X GET http://localhost:8080/api/accounts/user/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### 3. PIN Validation
```bash
# Validate PIN before sensitive operations
curl -X POST "http://localhost:8080/api/register/validate-pin?userId=$USER_ID&pin=111111"
```

### 4. Error Handling
```bash
# Capture HTTP status code
HTTP_STATUS=$(curl -s -o response.json -w "%{http_code}" \
  http://localhost:8080/api/accounts/123456 \
  -H "Authorization: Bearer $TOKEN")

if [ "$HTTP_STATUS" -eq 200 ]; then
  echo "Success"
  cat response.json | jq '.'
else
  echo "Error: HTTP $HTTP_STATUS"
  cat response.json | jq '.'
fi
```

## Testing Tips

### Using Variables for Account IDs
```bash
# Create and store account ID
ACCOUNT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d '{"userId": 1, "citizenId": "3101234567890", "accountType": "SAVINGS", "initialBalance": 10000}')

ACCOUNT_ID=$(echo "$ACCOUNT_RESPONSE" | jq -r '.id')

# Use in subsequent requests
curl -X GET http://localhost:8080/api/accounts/$ACCOUNT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Pretty Print JSON Responses
```bash
curl -s http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

### Extract Specific Fields
```bash
# Get only balance
curl -s http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[0].balance'

# Get account numbers
curl -s http://localhost:8080/api/accounts/user/1 \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[].id'
```

### Loop Through Results
```bash
# Get all transactions and process each
TRANSACTIONS=$(curl -s http://localhost:8080/api/transactions/account/$ACCOUNT_ID?pin=111111 \
  -H "Authorization: Bearer $TOKEN")

echo "$TRANSACTIONS" | jq -r '.[] | "\(.transactionDate) - \(.type): \(.amount) THB"'
```

## Environment Variables

For convenience, set up environment variables:

```bash
# Base configuration
export BASE_URL="http://localhost:8080"
export API_PREFIX="/api"

# User credentials (for testing)
export ALICE_USERNAME="alice_user"
export ALICE_PASSWORD="alicePass123"
export ALICE_PIN="111111"

export BOB_USERNAME="bob_user"
export BOB_PASSWORD="bobPass123"
export BOB_PIN="222222"

export TELLER_USERNAME="teller_user"
export TELLER_PASSWORD="tellerPass123"

# Tokens (set after login)
export ALICE_TOKEN=""
export BOB_TOKEN=""
export TELLER_TOKEN=""

# Account IDs (set after account creation)
export ALICE_ACCOUNT_ID=""
export BOB_ACCOUNT_ID=""
```

## Postman Collection

To import these examples into Postman:

1. Create a new collection called "Banking Microservices"
2. Set up environment variables:
   - `base_url`: `http://localhost:8080`
   - `alice_token`: (set after login)
   - `teller_token`: (set after login)
3. Create requests for each endpoint from the service documentation files
4. Use `{{base_url}}` and `{{alice_token}}` in your requests

## Troubleshooting

### Common Issues

**403 Forbidden**
- Check if you have the correct role (TELLER vs CUSTOMER)
- Verify you're accessing your own resources

**401 Unauthorized**
- Token may be expired or invalid
- Login again to get a fresh token
- Check PIN is correct for operations requiring PIN

**404 Not Found**
- Verify the account ID or user ID exists
- Check the URL path is correct

**400 Bad Request**
- Review the request body JSON format
- Check required fields are present
- Validate data types (numbers vs strings)

### Health Checks

Check if services are running:
```bash
# Check all services
for service in auth register accounts deposit transfer transactions; do
  echo "Checking $service..."
  curl -s http://localhost:8080/api/$service/health
  echo ""
done
```

## Additional Resources

- **[Architecture Documentation](../main.mmd)** - System architecture diagrams
- **[API Documentation](../../SWAGGER_GUIDE.md)** - Swagger UI access guide
- **[Test Validation Guide](../../TEST_VALIDATION_GUIDE.md)** - Testing strategies
- **[Requirements](../REQUIREMENT.md)** - Original project requirements

## Contributing

When adding new endpoints:
1. Update the corresponding service markdown file
2. Add examples with realistic data
3. Document authorization requirements
4. Include error scenarios
5. Update this README if needed

---

**Last Updated:** November 22, 2025
**Base URL:** `http://localhost:8080`
**API Gateway Port:** 8080
**Eureka Server:** Not exposed externally
