# Places to Fix: Replace `PERSON` with `CUSTOMER`

Based on analysis of the entire repository, here's a comprehensive list of where `PERSON` needs to be changed to `CUSTOMER`:

---

## **1. Core Enum Definition**
**File:** `register-service/src/main/java/com/banking/register/entity/UserRole.java`
- **Line 4:** Remove `PERSON` enum value entirely
- **Current:** `PERSON, CUSTOMER, TELLER`
- **Fix:** Should only have `CUSTOMER, TELLER`

---

## **2. Transaction Service (Business Logic)**
**File:** `transaction-service/src/main/java/com/banking/transaction/service/TransactionService.java`

### Location 1: Line 53-55
- **Change comment:** `"Only PERSON (customers) need ownership validation"` → `"Only CUSTOMER needs ownership validation"`
- **Change condition:** `if ("PERSON".equals(userRole))` → `if ("CUSTOMER".equals(userRole))`

### Location 2: Line 79-80
- **Change comment:** `"Only PERSON (customers) need ownership validation"` → `"Only CUSTOMER needs ownership validation"`
- **Change condition:** `if ("PERSON".equals(userRole))` → `if ("CUSTOMER".equals(userRole))`

---

## **3. Register Service Tests**

### File: `register-service/src/test/java/com/banking/register/service/RegisterServiceTest.java`
- **Line 53:** `.role(UserRole.PERSON)` → `.role(UserRole.CUSTOMER)`
- **Line 65:** `.role(UserRole.PERSON)` → `.role(UserRole.CUSTOMER)`
- **Line 194:** `assertThat(response.getUser().getRole()).isEqualTo(UserRole.PERSON)` → `...isEqualTo(UserRole.CUSTOMER)`

### File: `register-service/src/test/java/com/banking/register/controller/RegisterControllerTest.java`
- **Line 52:** `.role(UserRole.PERSON)` → `.role(UserRole.CUSTOMER)`
- **Line 60:** `.role(UserRole.PERSON)` → `.role(UserRole.CUSTOMER)`
- **Line 85:** `.andExpect(jsonPath("$.user.role").value("PERSON"))` → `.value("CUSTOMER")`
- **Line 116:** `.andExpect(jsonPath("$.role").value("PERSON"))` → `.value("CUSTOMER")`
- **Line 131:** `.andExpect(jsonPath("$.role").value("PERSON"))` → `.value("CUSTOMER")`

---

## **4. Transaction Service Tests**

### File: `transaction-service/src/test/java/com/banking/transaction/service/TransactionServiceTest.java`
- **Line 131:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 197:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 229:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 259:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 275:** `String role = "PERSON";` → `String role = "CUSTOMER";`

### File: `transaction-service/src/test/java/com/banking/transaction/controller/TransactionControllerTest.java`
- **Line 139:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 162:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 182:** `String role = "PERSON";` → `String role = "CUSTOMER";`
- **Line 226:** `String role = "PERSON";` → `String role = "CUSTOMER";`

---

## **5. Account Service Tests**

### File: `account-service/src/test/java/com/banking/account/controller/AccountControllerTest.java`

#### Test 1: Around Line 301-309
- **Line 301:** Test method name: `testGetAccount_WithPersonRole_Returns403` → `testGetAccount_WithNonCustomerRole_Returns403`
- **Line 302:** Comment: `// Given - PERSON tries to access` → `// Given - Non-CUSTOMER role tries to access`
- **Line 309:** `.header("X-User-Role", "PERSON")` → Change to `"TELLER"` (to test that non-CUSTOMER is rejected)

#### Test 2: Around Line 345-353
- **Line 345:** Test method name: `testGetAccountsByUserId_WithPersonRole_Returns403` → `testGetAccountsByUserId_WithNonCustomerRole_Returns403`
- **Line 346:** Comment: `// Given - PERSON tries to access` → `// Given - Non-CUSTOMER role tries to access`
- **Line 353:** `.header("X-User-Role", "PERSON")` → Change to `"TELLER"`

---

## **6. Auth Service Tests**

### File: `auth-service/src/test/java/com/banking/auth/util/JwtUtilTest.java`
- **Line 144:** Comment: `// Test PERSON role` → `// Test CUSTOMER role`
- **Line 145:** `String personToken = jwtUtil.generateToken("person_user", 1L, "PERSON");`
  - Change to: `String customerToken = jwtUtil.generateToken("customer_user", 1L, "CUSTOMER");`
- **Line 146:** `assertThat(jwtUtil.extractRole(personToken)).isEqualTo("PERSON");`
  - Change to: `assertThat(jwtUtil.extractRole(customerToken)).isEqualTo("CUSTOMER");`
- **Line 147:** `assertThat(jwtUtil.validateToken(personToken)).isTrue();`
  - Change to: `assertThat(jwtUtil.validateToken(customerToken)).isTrue();`

### File: `auth-service/src/test/java/com/banking/auth/service/AuthServiceTest.java`
- **Line 214:** Comment: `// Test PERSON role` → `// Test CUSTOMER role`
- **Line 215-216:** 
  ```java
  UserDto personUser = new UserDto(1L, "person_user", 
          passwordEncoder.encode("password123"), "person@example.com", "PERSON");
  ```
  Change to:
  ```java
  UserDto customerUser = new UserDto(1L, "customer_user", 
          passwordEncoder.encode("password123"), "customer@example.com", "CUSTOMER");
  ```
- **Line 217:** `LoginRequest personRequest = new LoginRequest("person_user", "password123");`
  - Change to: `LoginRequest customerRequest = new LoginRequest("customer_user", "password123");`
- **Line 219:** `when(userClient.getUserByUsername("person_user")).thenReturn(personUser);`
  - Change to: `when(userClient.getUserByUsername("customer_user")).thenReturn(customerUser);`
- **Line 220:** `when(jwtUtil.generateToken("person_user", 1L, "PERSON")).thenReturn("token_person");`
  - Change to: `when(jwtUtil.generateToken("customer_user", 1L, "CUSTOMER")).thenReturn("token_customer");`
- **Line 222:** `LoginResponse personResponse = authService.login(personRequest);`
  - Change to: `LoginResponse customerResponse = authService.login(customerRequest);`
- **Line 223:** `assertThat(personResponse.getRole()).isEqualTo("PERSON");`
  - Change to: `assertThat(customerResponse.getRole()).isEqualTo("CUSTOMER");`

---

## **7. Documentation/Schema Annotations**

### File: `register-service/src/main/java/com/banking/register/dto/RegisterRequest.java`
- **Line 57:** `@Schema(description = "User role", example = "PERSON", allowableValues = {"PERSON", "CUSTOMER", "TELLER"})`
  - Change to: `example = "CUSTOMER", allowableValues = {"CUSTOMER", "TELLER"}`

### File: `register-service/src/main/java/com/banking/register/dto/UserDto.java`
- **Line 37:** `@Schema(description = "User role", example = "PERSON")`
  - Change to: `example = "CUSTOMER"`

### File: `register-service/src/main/java/com/banking/register/entity/User.java`
- **Line 46:** Comment: `private UserRole role; // PERSON, CUSTOMER, TELLER`
  - Change to: `// CUSTOMER, TELLER`

### File: `auth-service/src/main/java/com/banking/auth/util/JwtUtil.java`
- **Line 31:** Comment: `@param role User role (PERSON, CUSTOMER, TELLER)`
  - Change to: `@param role User role (CUSTOMER, TELLER)`

---

## **8. Documentation/Test Scenario Files**

### File: `docs/test-scenario.md`
- **Multiple occurrences:** Replace all `"role": "PERSON"` with `"role": "CUSTOMER"` in test examples
- **Headers/Titles:** All instances of "Test Case X.X: ... PERSON ..." should reference `CUSTOMER`
- **Test descriptions:** All mentions of PERSON role should be changed to CUSTOMER
- **Example on Line 96, 111, 135, 165, 195, 225, 308, 357, 1086, 1103:** `"role": "PERSON"` → `"role": "CUSTOMER"`
- **Line 753, 1323, 1350:** PERSON role descriptions → CUSTOMER role descriptions

### File: `docs/REQUIREMENT.md`
- **Line 5:** `"Only new PERSON can register"` → `"Only new CUSTOMER can register"`
- **Line 9:** `"Both CUSTOMER and PERSON can go to the bank"` → `"CUSTOMER can go to the bank"` (remove mention of PERSON)

### File: `docs/implementation-plan.md`
- **Line 217:** `User entity with UserRole enum (PERSON, CUSTOMER, TELLER)` → Remove `PERSON`
- **Line 604:** `Can register new user as PERSON` → `Can register new user as CUSTOMER`

### File: `RUN_DOCKER_COMPOSE.md`
- **Multiple test examples:** Change all `"role": "PERSON"` → `"role": "CUSTOMER"`
- **Line 125, 147, 346:** `"role": "PERSON"` → `"role": "CUSTOMER"`

---

## **Summary**

### Total Changes Required:
1. **Enum definition** - 1 file
2. **Business logic role checks** - 1 file, 2 locations
3. **Service tests** - 2 files in register-service, 2 files in transaction-service (~17 changes)
4. **Controller tests** - 2 files (~8 changes)
5. **Auth service tests** - 2 files (~14 changes)
6. **API documentation/schema** - 4 files (~5 changes)
7. **Documentation files** - 4 files (multiple occurrences in test-scenario.md)

### Root Cause:
The system was incorrectly designed with `PERSON` as a third role. The architecture should only have:
- **CUSTOMER**: Users who register and can perform transfers, view accounts
- **TELLER**: Bank employees who can create accounts and perform deposits

There was no need for a separate `PERSON` role since registration should directly create `CUSTOMER` accounts.
