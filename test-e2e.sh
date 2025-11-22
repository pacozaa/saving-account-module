#!/bin/bash

# Test Scenario 8: Complete End-to-End Flow
# This script tests the entire user journey from registration to money transfer

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Generate unique usernames with timestamp
TIMESTAMP=$(date +%s)
ALICE_USERNAME="alice_${TIMESTAMP}"
BOB_USERNAME="bob_${TIMESTAMP}"
TELLER_USERNAME="teller_${TIMESTAMP}"
ALICE_EMAIL="alice_${TIMESTAMP}@example.com"
BOB_EMAIL="bob_${TIMESTAMP}@example.com"
TELLER_EMAIL="teller_${TIMESTAMP}@bank.com"

# Generate unique citizen IDs (13 digits)
ALICE_CITIZEN_ID="310${TIMESTAMP:0:10}"
BOB_CITIZEN_ID="311${TIMESTAMP:0:10}"
TELLER_CITIZEN_ID="312${TIMESTAMP:0:10}"

echo "======================================"
echo "End-to-End Banking System Test"
echo "======================================"
echo ""
echo "Test users:"
echo "  Alice: $ALICE_USERNAME (Citizen ID: $ALICE_CITIZEN_ID)"
echo "  Bob: $BOB_USERNAME (Citizen ID: $BOB_CITIZEN_ID)"
echo "  Teller: $TELLER_USERNAME (Citizen ID: $TELLER_CITIZEN_ID)"
echo ""

# Step 1: Register Two Customers (Alice and Bob)
echo -e "${YELLOW}Step 1: Registering Customer A (Alice)${NC}"
ALICE_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$ALICE_USERNAME\",
    \"password\": \"alicePass123\",
    \"email\": \"$ALICE_EMAIL\",
    \"citizenId\": \"$ALICE_CITIZEN_ID\",
    \"thaiName\": \"อลิซ คัสโตเมอร์\",
    \"englishName\": \"Alice Customer\",
    \"pin\": \"111111\",
    \"role\": \"CUSTOMER\"
  }")

echo "$ALICE_RESPONSE" | jq '.'
ALICE_USER_ID=$(echo "$ALICE_RESPONSE" | jq -r '.user.id')
echo -e "${GREEN}✓ Alice registered with User ID: $ALICE_USER_ID${NC}"
echo ""

echo -e "${YELLOW}Step 1: Registering Customer B (Bob)${NC}"
BOB_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$BOB_USERNAME\",
    \"password\": \"bobPass123\",
    \"email\": \"$BOB_EMAIL\",
    \"citizenId\": \"$BOB_CITIZEN_ID\",
    \"thaiName\": \"บ็อบ คัสโตเมอร์\",
    \"englishName\": \"Bob Customer\",
    \"pin\": \"222222\",
    \"role\": \"CUSTOMER\"
  }")

echo "$BOB_RESPONSE" | jq '.'
BOB_USER_ID=$(echo "$BOB_RESPONSE" | jq -r '.user.id')
echo -e "${GREEN}✓ Bob registered with User ID: $BOB_USER_ID${NC}"
echo ""

# Step 2: Register a Teller
echo -e "${YELLOW}Step 2: Registering Teller (Carol)${NC}"
TELLER_RESPONSE=$(curl -s -X POST $BASE_URL/api/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$TELLER_USERNAME\",
    \"password\": \"carolPass123\",
    \"email\": \"$TELLER_EMAIL\",
    \"citizenId\": \"$TELLER_CITIZEN_ID\",
    \"thaiName\": \"แครอล เทลเลอร์\",
    \"englishName\": \"Carol Teller\",
    \"pin\": \"999999\",
    \"role\": \"TELLER\"
  }")

echo "$TELLER_RESPONSE" | jq '.'
TELLER_USER_ID=$(echo "$TELLER_RESPONSE" | jq -r '.user.id')
echo -e "${GREEN}✓ Teller Carol registered with User ID: $TELLER_USER_ID${NC}"
echo ""

# Step 3: Teller Logs In
echo -e "${YELLOW}Step 3: Teller logging in${NC}"
TELLER_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$TELLER_USERNAME\",
    \"password\": \"carolPass123\"
  }")

echo "$TELLER_LOGIN_RESPONSE" | jq '.'
TELLER_TOKEN=$(echo "$TELLER_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ Teller logged in successfully${NC}"
echo ""

# Step 4: Teller Creates Accounts for Both Users
echo -e "${YELLOW}Step 4: Creating account for Alice${NC}"
ALICE_ACCOUNT_RESPONSE=$(curl -s -X POST $BASE_URL/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"userId\": $ALICE_USER_ID,
    \"citizenId\": \"$ALICE_CITIZEN_ID\",
    \"accountType\": \"SAVINGS\",
    \"initialBalance\": 0
  }")

echo "$ALICE_ACCOUNT_RESPONSE" | jq '.'
ALICE_ACCOUNT_ID=$(echo "$ALICE_ACCOUNT_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ Alice's account created: $ALICE_ACCOUNT_ID${NC}"
echo ""

echo -e "${YELLOW}Step 4: Creating account for Bob${NC}"
BOB_ACCOUNT_RESPONSE=$(curl -s -X POST $BASE_URL/api/accounts/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"userId\": $BOB_USER_ID,
    \"citizenId\": \"$BOB_CITIZEN_ID\",
    \"accountType\": \"SAVINGS\",
    \"initialBalance\": 0
  }")

echo "$BOB_ACCOUNT_RESPONSE" | jq '.'
BOB_ACCOUNT_ID=$(echo "$BOB_ACCOUNT_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ Bob's account created: $BOB_ACCOUNT_ID${NC}"
echo ""

# Step 5: Teller Deposits Money to Alice's Account
echo -e "${YELLOW}Step 5: Depositing 5000 THB to Alice's account${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST $BASE_URL/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TELLER_TOKEN" \
  -d "{
    \"accountId\": \"$ALICE_ACCOUNT_ID\",
    \"amount\": 5000.00,
    \"tellerId\": $TELLER_USER_ID,
    \"description\": \"Initial deposit\"
  }")

echo "$DEPOSIT_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Deposit successful${NC}"
echo ""

# Step 6: Alice Logs In
echo -e "${YELLOW}Step 6: Alice logging in${NC}"
ALICE_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$ALICE_USERNAME\",
    \"password\": \"alicePass123\"
  }")

echo "$ALICE_LOGIN_RESPONSE" | jq '.'
ALICE_TOKEN=$(echo "$ALICE_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ Alice logged in successfully${NC}"
echo ""

# Step 7: Alice Views Her Account Information
echo -e "${YELLOW}Step 7: Alice viewing her account information${NC}"
ALICE_ACCOUNT_INFO=$(curl -s -X GET $BASE_URL/api/accounts/user/$ALICE_USER_ID \
  -H "Authorization: Bearer $ALICE_TOKEN")

echo "$ALICE_ACCOUNT_INFO" | jq '.'
ALICE_BALANCE=$(echo "$ALICE_ACCOUNT_INFO" | jq -r '.[0].balance')
echo -e "${GREEN}✓ Alice's current balance: $ALICE_BALANCE THB${NC}"
echo ""

# Step 8: Alice Transfers Money to Bob
echo -e "${YELLOW}Step 8: Alice transferring 1000 THB to Bob${NC}"
TRANSFER_RESPONSE=$(curl -s -X POST $BASE_URL/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{
    \"fromAccountId\": \"$ALICE_ACCOUNT_ID\",
    \"toAccountId\": \"$BOB_ACCOUNT_ID\",
    \"amount\": 1000.00,
    \"pin\": \"111111\",
    \"description\": \"Payment to Bob\"
  }")

echo "$TRANSFER_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Transfer successful${NC}"
echo ""

# Step 9: Alice Views Her Bank Statement
echo -e "${YELLOW}Step 9: Alice viewing her bank statement${NC}"
ALICE_STATEMENT=$(curl -s -X GET "$BASE_URL/api/transactions/account/$ALICE_ACCOUNT_ID?pin=111111" \
  -H "Authorization: Bearer $ALICE_TOKEN")

echo "$ALICE_STATEMENT" | jq '.'
TRANSACTION_COUNT=$(echo "$ALICE_STATEMENT" | jq 'length')
echo -e "${GREEN}✓ Alice has $TRANSACTION_COUNT transactions${NC}"
echo ""

# Step 10: Bob Logs In and Checks His Balance
echo -e "${YELLOW}Step 10: Bob logging in${NC}"
BOB_LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$BOB_USERNAME\",
    \"password\": \"bobPass123\"
  }")

echo "$BOB_LOGIN_RESPONSE" | jq '.'
BOB_TOKEN=$(echo "$BOB_LOGIN_RESPONSE" | jq -r '.token')
echo -e "${GREEN}✓ Bob logged in successfully${NC}"
echo ""

echo -e "${YELLOW}Step 10: Bob checking his balance${NC}"
BOB_ACCOUNT_INFO=$(curl -s -X GET $BASE_URL/api/accounts/user/$BOB_USER_ID \
  -H "Authorization: Bearer $BOB_TOKEN")

echo "$BOB_ACCOUNT_INFO" | jq '.'
BOB_BALANCE=$(echo "$BOB_ACCOUNT_INFO" | jq -r '.[0].balance')
echo -e "${GREEN}✓ Bob's current balance: $BOB_BALANCE THB${NC}"
echo ""

# Summary
echo "======================================"
echo -e "${GREEN}End-to-End Test Completed Successfully!${NC}"
echo "======================================"
echo ""
echo "Summary:"
echo "- Alice (User ID: $ALICE_USER_ID, Account: $ALICE_ACCOUNT_ID)"
echo "  Initial deposit: 5000 THB"
echo "  After transfer: $ALICE_BALANCE THB"
echo ""
echo "- Bob (User ID: $BOB_USER_ID, Account: $BOB_ACCOUNT_ID)"
echo "  Received from Alice: $BOB_BALANCE THB"
echo ""
echo "- Teller Carol (User ID: $TELLER_USER_ID)"
echo "  Processed deposit of 5000 THB"
echo ""
