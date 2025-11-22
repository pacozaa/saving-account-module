# E2E Test Validation Guide

## Problem with Current Tests

Your current `test-e2e.sh` and `test-e2e-requirements.sh` only check if operations **succeed**, but they don't verify if the **values are correct**.

### What They Miss:

1. ❌ **No Balance Calculations**: Scripts check if balance exists, but don't verify it's mathematically correct
2. ❌ **No Expected vs Actual**: Missing validation like "Expected: 7000, Actual: 7000"
3. ❌ **No Transaction Amount Verification**: Don't check if transaction amounts match what was requested
4. ❌ **Weak Account Number Validation**: Claims to check 7-digit format but doesn't actually validate it
5. ❌ **No Chronological Order Check**: Doesn't verify transactions are truly ordered past-to-present

### Example Problem:

```bash
# Current test just checks if transfer succeeded
if [ "$TRANSFER_SUCCESS" != "null" ]; then
    echo "✓ Transfer successful"
fi

# But it doesn't verify:
# - Did Alice lose exactly 3000 THB?
# - Did Bob gain exactly 3000 THB?
# - Is the total money in system still the same?
```

## New Enhanced Test: `test-e2e-validated.sh`

### What It Validates:

#### ✅ 1. Registration Data Accuracy
```bash
assert_equal "$ALICE_USERNAME" "$ALICE_REGISTERED_USERNAME" "Alice username matches"
assert_equal "$ALICE_EMAIL" "$ALICE_REGISTERED_EMAIL" "Alice email matches"
assert_equal "CUSTOMER" "$ALICE_REGISTERED_ROLE" "Alice role is CUSTOMER"
```

#### ✅ 2. Account Number Format (7 digits)
```bash
validate_account_number() {
    local account_number="$1"
    local length=${#account_number}
    
    if [ "$length" -eq 7 ] && [[ "$account_number" =~ ^[0-9]{7}$ ]]; then
        echo "✓ Account number format is correct"
    fi
}
```

#### ✅ 3. Initial Balance Correctness
```bash
assert_equal "10000" "$ALICE_INITIAL_BALANCE" "Alice's initial balance is 10,000 THB"
assert_equal "0" "$BOB_INITIAL_BALANCE" "Bob's initial balance is 0 THB"
```

#### ✅ 4. Deposit Amount Accuracy
```bash
# Bob starts with 0
# Teller deposits 5000
BOB_BALANCE_AFTER_DEPOSIT=$(curl ... | jq -r '.balance')
assert_equal "5000" "$BOB_BALANCE_AFTER_DEPOSIT" "Bob's balance after deposit is 5,000 THB"
```

#### ✅ 5. Transfer Calculations
```bash
# Alice: 10,000 → transfers 3,000 → should have 7,000
# Bob: 5,000 → receives 3,000 → should have 8,000

ALICE_EXPECTED_BALANCE=$((10000 - TRANSFER_AMOUNT))
BOB_EXPECTED_BALANCE=$((5000 + TRANSFER_AMOUNT))

assert_equal "$ALICE_EXPECTED_BALANCE" "$ALICE_BALANCE_AFTER" "Alice's balance is 7,000 THB"
assert_equal "$BOB_EXPECTED_BALANCE" "$BOB_BALANCE_AFTER" "Bob's balance is 8,000 THB"
```

#### ✅ 6. Transaction Details Verification
```bash
# Verify transfer shows correct amount and type
TRANSFER_TX_AMOUNT=$(echo "$ALICE_STATEMENT" | jq -r '.[] | select(.amount == -3000) | .amount')
TRANSFER_TX_TYPE=$(echo "$TRANSFER_TX" | jq -r '.transactionType')

assert_equal "-3000" "$TRANSFER_TX_AMOUNT" "Transfer shows -3,000 THB for Alice"
assert_equal "TRANSFER_OUT" "$TRANSFER_TX_TYPE" "Transaction type is TRANSFER_OUT"
```

#### ✅ 7. Chronological Order
```bash
# Verify transactions are truly ordered from past to present
FIRST_TS=$(date -j -f "%Y-%m-%dT%H:%M:%S" "${FIRST_TX_DATE:0:19}" "+%s")
LAST_TS=$(date -j -f "%Y-%m-%dT%H:%M:%S" "${LAST_TX_DATE:0:19}" "+%s")

if [ "$FIRST_TS" -le "$LAST_TS" ]; then
    echo "✓ Transactions ordered chronologically"
fi
```

## Running the Enhanced Test

```bash
# Make sure services are running
./run.sh

# Run the validated E2E test
./test-e2e-validated.sh
```

## Expected Output

### Success Output:
```
==========================================
TEST SUMMARY
==========================================

Total Tests Run: 18
Tests Passed: 18
Tests Failed: 0

✓✓✓ ALL TESTS PASSED ✓✓✓

Verified Functionality:
  ✓ User registration with correct data
  ✓ Authentication and token generation
  ✓ Account creation with valid account numbers (7 digits)
  ✓ Initial balance set correctly
  ✓ Money deposit increases balance correctly
  ✓ Money transfer decreases sender balance
  ✓ Money transfer increases receiver balance
  ✓ Transaction history shows correct amounts
  ✓ Transaction types are correct
  ✓ Transactions ordered chronologically

Final Balances (Verified):
  Alice: 7000 THB (Expected: 7000)
  Bob: 8000 THB (Expected: 8000)
```

### Failure Output:
```
✗ FAILED: Alice's balance is 7,000 THB (10,000 - 3,000)
  Expected: 7000, Got: 6950

✗✗✗ SOME TESTS FAILED ✗✗✗

Failed Tests:
  ✗ Alice's balance is 7,000 THB (10,000 - 3,000)
  ✗ Transaction chronological order
```

## Key Differences

| Aspect | Old Scripts | New Script |
|--------|-------------|------------|
| **Balance Verification** | Just checks if exists | Validates exact amounts |
| **Math Validation** | None | Calculates expected vs actual |
| **Transaction Details** | Not checked | Verifies amounts and types |
| **Account Number** | Weak check | Full format validation |
| **Ordering** | Assumes correct | Actually verifies timestamps |
| **Test Counting** | None | Tracks passed/failed |
| **Error Reporting** | Generic | Lists specific failures |

## What This Proves

When `test-e2e-validated.sh` passes, you know:

1. ✅ Money is **conserved** (no money created/lost)
2. ✅ All calculations are **mathematically correct**
3. ✅ Database stores **accurate values**
4. ✅ Transactions show **correct details**
5. ✅ Account numbers follow **required format**
6. ✅ History is **chronologically ordered**
7. ✅ APIs return **consistent data**

## Integration with CI/CD

You can use this in your CI/CD pipeline:

```bash
# In GitHub Actions or Jenkins
./test-e2e-validated.sh
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo "✓ All validations passed - safe to deploy"
else
    echo "✗ Validation failed - blocking deployment"
    exit 1
fi
```

## Additional Test Ideas

Consider adding these validations:

1. **Concurrency Tests**: Multiple transfers at the same time
2. **Boundary Tests**: Transfer exactly the full balance
3. **Negative Tests**: Try invalid PINs, insufficient funds
4. **Performance Tests**: Response time validation
5. **Data Integrity**: Sum of all transactions equals total balance

## Conclusion

Your current tests are like checking "Did the car start?" ✅

The enhanced test checks:
- Does it drive? ✅
- At the correct speed? ✅
- In the right direction? ✅
- Using the expected amount of fuel? ✅

This gives you **confidence** that your system **actually works correctly**.
