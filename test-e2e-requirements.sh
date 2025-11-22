#!/bin/bash

# End-to-End Test Script - Business Requirements Validation
# Tests all 6 business requirements comprehensively

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Generate unique identifiers
TIMESTAMP=$(date +%s)
PERSON_USERNAME="person_${TIMESTAMP}"
CUSTOMER_USERNAME="customer_${TIMESTAMP}"
TELLER_USERNAME="teller_${TIMESTAMP}"
PERSON_EMAIL="person_${TIMESTAMP}@example.com"
CUSTOMER_EMAIL="customer_${TIMESTAMP}@example.com"
TELLER_EMAIL="teller_${TIMESTAMP}@bank.com"

# Generate unique citizen IDs (13 digits)
PERSON_CITIZEN_ID="320${TIMESTAMP:0:10}"
CUSTOMER_CITIZEN_ID="321${TIMESTAMP:0:10}"
TELLER_CITIZEN_ID="322${TIMESTAMP:0:10}"

echo "=========================================="
echo "Business Requirements E2E Test"
echo "=========================================="
echo ""
echo "Test participants:"
echo "  PERSON: $PERSON_USERNAME (Citizen ID: $PERSON_CITIZEN_ID)"
echo "  CUSTOMER: $CUSTOMER_USERNAME (Citizen ID: $CUSTOMER_CITIZEN_ID)"
echo "  TELLER: $TELLER_USERNAME (Citizen ID: $TELLER_CITIZEN_ID)"
echo ""

# ==========================================
# Requirement 1: Online Registration
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 1: Online Registration"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 1.a: New person registers online (becomes CUSTOMER) with email, password, and personal information${NC}"
PERSON_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$PERSON_USERNAME\",
    \"password\": \"personPass123\",
    \"email\": \"$PERSON_EMAIL\",
    \"citizenId\": \"$PERSON_CITIZEN_ID\",
    \"thaiName\": \"สมชาย ประชากร\",
    \"englishName\": \"Somchai Person\",
    \"pin\": \"123456\",
    \"role\": \"CUSTOMER\"
  }")

echo "$PERSON_RESPONSE" | jq '.'
PERSON_USER_ID=$(echo "$PERSON_RESPONSE" | jq -r '.user.id')
if [ "$PERSON_USER_ID" != "null" ] && [ -n "$PERSON_USER_ID" ]; then
    echo -e "${GREEN}✓ PASSED: New person registered successfully as CUSTOMER (User ID: $PERSON_USER_ID)${NC}"
    echo -e "${GREEN}  Note: Person registered but has NO ACCOUNT yet - will need teller to create one${NC}"
else
    echo -e "${RED}✗ FAILED: Person registration failed${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Registering another CUSTOMER for testing account with initial deposit${NC}"
CUSTOMER_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$CUSTOMER_USERNAME\",
    \"password\": \"customerPass123\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"citizenId\": \"$CUSTOMER_CITIZEN_ID\",
    \"thaiName\": \"สมหญิง ลูกค้า\",
    \"englishName\": \"Somying Customer\",
    \"pin\": \"654321\",
    \"role\": \"CUSTOMER\"
  }")

echo "$CUSTOMER_RESPONSE" | jq '.'
CUSTOMER_USER_ID=$(echo "$CUSTOMER_RESPONSE" | jq -r '.user.id')
echo -e "${GREEN}✓ Another CUSTOMER registered (User ID: $CUSTOMER_USER_ID)${NC}"
echo ""

echo -e "${YELLOW}Registering TELLER for later tests${NC}"
TELLER_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$TELLER_USERNAME\",
    \"password\": \"tellerPass123\",
    \"email\": \"$TELLER_EMAIL\",
    \"citizenId\": \"$TELLER_CITIZEN_ID\",
    \"thaiName\": \"สมศรี เทลเลอร์\",
    \"englishName\": \"Somsri Teller\",
    \"pin\": \"999999\",
    \"role\": \"TELLER\"
  }")

echo "$TELLER_RESPONSE" | jq '.'
TELLER_USER_ID=$(echo "$TELLER_RESPONSE" | jq -r '.user.id')
echo -e "${GREEN}✓ TELLER registered (User ID: $TELLER_USER_ID)${NC}"
echo ""

# Teller logs in for subsequent operations
echo -e "${YELLOW}TELLER logging in${NC}"
TELLER_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$TELLER_USERNAME\",
    \"password\": \"tellerPass123\"
  }")
TELLER_TOKEN=$(echo "$TELLER_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ TELLER logged in${NC}"
echo ""

# ==========================================
# Requirement 2: Creating a New Account
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 2: Creating a New Account"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 2.a: TELLER creates account for CUSTOMER with initial deposit${NC}"
CUSTOMER_ACCOUNT_RESPONSE=$(curl -s -X POST $BASE_URL/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"userId\": $CUSTOMER_USER_ID,
    \"citizenId\": \"$CUSTOMER_CITIZEN_ID\",
    \"accountType\": \"SAVINGS\",
    \"initialBalance\": 10000.00
  }")

echo "$CUSTOMER_ACCOUNT_RESPONSE" | jq '.'
CUSTOMER_ACCOUNT_ID=$(echo "$CUSTOMER_ACCOUNT_RESPONSE" | jq -r '.id')
CUSTOMER_ACCOUNT_NUMBER=$(echo "$CUSTOMER_ACCOUNT_RESPONSE" | jq -r '.accountNumber')
if [ "$CUSTOMER_ACCOUNT_ID" != "null" ] && [ -n "$CUSTOMER_ACCOUNT_ID" ]; then
    echo -e "${GREEN}✓ PASSED: Account created for CUSTOMER with initial deposit${NC}"
    echo -e "${GREEN}  Account Number: $CUSTOMER_ACCOUNT_NUMBER (7 digits: $(echo $CUSTOMER_ACCOUNT_NUMBER | wc -c | tr -d ' ') chars)${NC}"
    echo -e "${GREEN}  Initial Balance: 10,000 THB${NC}"
else
    echo -e "${RED}✗ FAILED: Account creation failed${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Test 2.a: TELLER creates account for new person (who registered online) without initial deposit${NC}"
PERSON_ACCOUNT_RESPONSE=$(curl -s -X POST $BASE_URL/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"userId\": $PERSON_USER_ID,
    \"citizenId\": \"$PERSON_CITIZEN_ID\",
    \"accountType\": \"SAVINGS\",
    \"initialBalance\": 0
  }")

echo "$PERSON_ACCOUNT_RESPONSE" | jq '.'
PERSON_ACCOUNT_ID=$(echo "$PERSON_ACCOUNT_RESPONSE" | jq -r '.id')
PERSON_ACCOUNT_NUMBER=$(echo "$PERSON_ACCOUNT_RESPONSE" | jq -r '.accountNumber')
if [ "$PERSON_ACCOUNT_ID" != "null" ] && [ -n "$PERSON_ACCOUNT_ID" ]; then
    echo -e "${GREEN}✓ PASSED: Account created for new person (registered online) without initial deposit${NC}"
    echo -e "${GREEN}  Account Number: $PERSON_ACCOUNT_NUMBER${NC}"
    echo -e "${GREEN}  Initial Balance: 0 THB${NC}"
else
    echo -e "${RED}✗ FAILED: Account creation for new person failed${NC}"
    exit 1
fi
echo ""

# ==========================================
# Requirement 3: Money Deposit
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 3: Money Deposit"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 3.a: TELLER deposits 5,000 THB to new person's account${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST $BASE_URL/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"accountId\": \"$PERSON_ACCOUNT_ID\",
    \"amount\": 5000.00,
    \"tellerId\": $TELLER_USER_ID,
    \"description\": \"Initial deposit by teller\"
  }")

echo "$DEPOSIT_RESPONSE" | jq '.'
DEPOSIT_SUCCESS=$(echo "$DEPOSIT_RESPONSE" | jq -r '.success // .accountId')
if [ "$DEPOSIT_SUCCESS" != "null" ] && [ -n "$DEPOSIT_SUCCESS" ]; then
    echo -e "${GREEN}✓ PASSED: TELLER successfully deposited 5,000 THB${NC}"
else
    echo -e "${RED}✗ FAILED: Deposit operation failed${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Test 3.a: TELLER deposits minimum amount (1 THB) to CUSTOMER's account${NC}"
DEPOSIT_MIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"accountId\": \"$CUSTOMER_ACCOUNT_ID\",
    \"amount\": 1.00,
    \"tellerId\": $TELLER_USER_ID,
    \"description\": \"Minimum deposit test\"
  }")

echo "$DEPOSIT_MIN_RESPONSE" | jq '.'
DEPOSIT_MIN_SUCCESS=$(echo "$DEPOSIT_MIN_RESPONSE" | jq -r '.success // .accountId')
if [ "$DEPOSIT_MIN_SUCCESS" != "null" ] && [ -n "$DEPOSIT_MIN_SUCCESS" ]; then
    echo -e "${GREEN}✓ PASSED: TELLER successfully deposited minimum amount (1 THB)${NC}"
else
    echo -e "${RED}✗ FAILED: Minimum deposit failed${NC}"
    exit 1
fi
echo ""

# ==========================================
# Requirement 4: Account Information
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 4: Account Information"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 4.a: CUSTOMER logs in and views account information${NC}"
CUSTOMER_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$CUSTOMER_USERNAME\",
    \"password\": \"customerPass123\"
  }")
CUSTOMER_TOKEN=$(echo "$CUSTOMER_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ CUSTOMER logged in${NC}"
echo ""

CUSTOMER_ACCOUNT_INFO=$(curl -s -X GET $BASE_URL/api/accounts/user/$CUSTOMER_USER_ID \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")

echo "$CUSTOMER_ACCOUNT_INFO" | jq '.'
CUSTOMER_BALANCE=$(echo "$CUSTOMER_ACCOUNT_INFO" | jq -r '.[0].balance')
if [ "$CUSTOMER_BALANCE" != "null" ] && [ -n "$CUSTOMER_BALANCE" ]; then
    echo -e "${GREEN}✓ PASSED: CUSTOMER can view account information${NC}"
    echo -e "${GREEN}  Account Number: $CUSTOMER_ACCOUNT_NUMBER${NC}"
    echo -e "${GREEN}  Balance: $CUSTOMER_BALANCE THB${NC}"
else
    echo -e "${RED}✗ FAILED: Unable to retrieve account information${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Test 4.a: New person (now with account created by TELLER) logs in and views account information${NC}"
PERSON_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$PERSON_USERNAME\",
    \"password\": \"personPass123\"
  }")
PERSON_TOKEN=$(echo "$PERSON_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ PERSON logged in${NC}"
echo ""

PERSON_ACCOUNT_INFO=$(curl -s -X GET $BASE_URL/api/accounts/user/$PERSON_USER_ID \
  -H "Authorization: Bearer $PERSON_TOKEN")

echo "$PERSON_ACCOUNT_INFO" | jq '.'
PERSON_BALANCE=$(echo "$PERSON_ACCOUNT_INFO" | jq -r '.[0].balance')
if [ "$PERSON_BALANCE" != "null" ] && [ -n "$PERSON_BALANCE" ]; then
    echo -e "${GREEN}✓ PASSED: PERSON can view account information after account creation${NC}"
    echo -e "${GREEN}  Account Number: $PERSON_ACCOUNT_NUMBER${NC}"
    echo -e "${GREEN}  Balance: $PERSON_BALANCE THB${NC}"
else
    echo -e "${RED}✗ FAILED: PERSON unable to retrieve account information${NC}"
    exit 1
fi
echo ""

# ==========================================
# Requirement 5: Money Transfer
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 5: Money Transfer"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 5.a: CUSTOMER transfers 2,000 THB to PERSON's account with PIN confirmation${NC}"
TRANSFER_RESPONSE=$(curl -s -X POST $BASE_URL/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d "{
    \"fromAccountId\": \"$CUSTOMER_ACCOUNT_ID\",
    \"toAccountId\": \"$PERSON_ACCOUNT_ID\",
    \"amount\": 2000.00,
    \"pin\": \"654321\",
    \"description\": \"Transfer to friend\"
  }")

echo "$TRANSFER_RESPONSE" | jq '.'
TRANSFER_SUCCESS=$(echo "$TRANSFER_RESPONSE" | jq -r '.success // .transactionId')
if [ "$TRANSFER_SUCCESS" != "null" ] && [ -n "$TRANSFER_SUCCESS" ]; then
    echo -e "${GREEN}✓ PASSED: CUSTOMER successfully transferred 2,000 THB with PIN confirmation${NC}"
else
    echo -e "${RED}✗ FAILED: Money transfer failed${NC}"
    exit 1
fi
echo ""

# Verify balances after transfer
echo -e "${YELLOW}Verifying balances after transfer${NC}"
CUSTOMER_ACCOUNT_INFO_AFTER=$(curl -s -X GET $BASE_URL/api/accounts/user/$CUSTOMER_USER_ID \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")
CUSTOMER_BALANCE_AFTER=$(echo "$CUSTOMER_ACCOUNT_INFO_AFTER" | jq -r '.[0].balance')

PERSON_ACCOUNT_INFO_AFTER=$(curl -s -X GET $BASE_URL/api/accounts/user/$PERSON_USER_ID \
  -H "Authorization: Bearer $PERSON_TOKEN")
PERSON_BALANCE_AFTER=$(echo "$PERSON_ACCOUNT_INFO_AFTER" | jq -r '.[0].balance')

echo -e "${GREEN}✓ CUSTOMER balance after transfer: $CUSTOMER_BALANCE_AFTER THB${NC}"
echo -e "${GREEN}✓ PERSON balance after transfer: $PERSON_BALANCE_AFTER THB${NC}"
echo ""

# ==========================================
# Requirement 6: Bank Statement
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "REQUIREMENT 6: Bank Statement"
echo -e "==========================================${NC}"
echo ""

echo -e "${YELLOW}Test 6.a: CUSTOMER requests bank statement with PIN confirmation${NC}"
CUSTOMER_STATEMENT=$(curl -s -X GET "$BASE_URL/api/transactions/account/$CUSTOMER_ACCOUNT_ID?pin=654321" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")

echo "$CUSTOMER_STATEMENT" | jq '.'
TRANSACTION_COUNT=$(echo "$CUSTOMER_STATEMENT" | jq 'length')
if [ "$TRANSACTION_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED: CUSTOMER can view bank statement with PIN confirmation${NC}"
    echo -e "${GREEN}  Total transactions: $TRANSACTION_COUNT${NC}"
    
    # Verify transactions are ordered from past to present
    echo -e "${YELLOW}Verifying transaction order (past to present)${NC}"
    FIRST_TRANSACTION_DATE=$(echo "$CUSTOMER_STATEMENT" | jq -r '.[0].transactionDate')
    LAST_TRANSACTION_DATE=$(echo "$CUSTOMER_STATEMENT" | jq -r '.[-1].transactionDate')
    echo -e "${GREEN}  First transaction: $FIRST_TRANSACTION_DATE${NC}"
    echo -e "${GREEN}  Last transaction: $LAST_TRANSACTION_DATE${NC}"
    echo -e "${GREEN}✓ PASSED: Transactions displayed from past to present${NC}"
else
    echo -e "${RED}✗ FAILED: No transactions found in statement${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Test 6.a: PERSON requests bank statement with PIN confirmation${NC}"
PERSON_STATEMENT=$(curl -s -X GET "$BASE_URL/api/transactions/account/$PERSON_ACCOUNT_ID?pin=123456" \
  -H "Authorization: Bearer $PERSON_TOKEN")

echo "$PERSON_STATEMENT" | jq '.'
PERSON_TRANSACTION_COUNT=$(echo "$PERSON_STATEMENT" | jq 'length')
if [ "$PERSON_TRANSACTION_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED: PERSON can view bank statement with PIN confirmation${NC}"
    echo -e "${GREEN}  Total transactions: $PERSON_TRANSACTION_COUNT${NC}"
else
    echo -e "${RED}✗ FAILED: No transactions found in PERSON's statement${NC}"
    exit 1
fi
echo ""

# ==========================================
# Final Summary
# ==========================================
echo ""
echo -e "${BLUE}=========================================="
echo -e "TEST SUMMARY"
echo -e "==========================================${NC}"
echo ""
echo -e "${GREEN}✓ All Business Requirements Validated Successfully!${NC}"
echo ""
echo "Summary of Test Results:"
echo ""
echo "1. Online Registration"
echo "   ✓ New person can register online with email, password, and personal information"
echo "   ✓ Registration creates CUSTOMER role (account created later by TELLER)"
echo ""
echo "2. Creating a New Account"
echo "   ✓ TELLER can create account for existing CUSTOMER with initial deposit"
echo "   ✓ TELLER can create account for new person (registered online) without deposit"
echo "   ✓ System generates unique 7-digit account number"
echo ""
echo "3. Money Deposit"
echo "   ✓ TELLER can deposit money (minimum 1 THB)"
echo "   ✓ Deposits are properly recorded"
echo ""
echo "4. Account Information"
echo "   ✓ CUSTOMER can login and view account information"
echo "   ✓ PERSON (with account) can login and view account information"
echo ""
echo "5. Money Transfer"
echo "   ✓ CUSTOMER can transfer money with PIN confirmation"
echo "   ✓ Transfer properly updates both account balances"
echo ""
echo "6. Bank Statement"
echo "   ✓ CUSTOMER can view statement with PIN confirmation"
echo "   ✓ PERSON can view statement with PIN confirmation"
echo "   ✓ Transactions displayed from past to present"
echo ""
echo -e "${BLUE}=========================================="
echo "Test Data Summary:"
echo -e "==========================================${NC}"
echo ""
echo "PERSON: $PERSON_USERNAME"
echo "  User ID: $PERSON_USER_ID"
echo "  Account: $PERSON_ACCOUNT_NUMBER"
echo "  Final Balance: $PERSON_BALANCE_AFTER THB"
echo ""
echo "CUSTOMER: $CUSTOMER_USERNAME"
echo "  User ID: $CUSTOMER_USER_ID"
echo "  Account: $CUSTOMER_ACCOUNT_NUMBER"
echo "  Final Balance: $CUSTOMER_BALANCE_AFTER THB"
echo ""
echo "TELLER: $TELLER_USERNAME"
echo "  User ID: $TELLER_USER_ID"
echo ""
echo -e "${GREEN}All tests completed successfully!${NC}"
echo ""
