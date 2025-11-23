# Unit Test Implementation Plan

## Overview
Minimal unit testing strategy for the Banking System microservices, focusing on critical business logic and fast execution within the short time constraint.

---

## Testing Philosophy

**Priority:** Test business logic > Test controllers > Skip infrastructure
**Goal:** 80% coverage of service layer, basic controller validation
**Constraint:** Keep tests fast (< 5 seconds total execution time per service)

---

## Technology Stack

- **JUnit 5** (Jupiter) - Already included in `spring-boot-starter-test`
- **Mockito** - For mocking dependencies
- **AssertJ** - For fluent assertions
- **@SpringBootTest** - Only when absolutely necessary (integration tests)
- **@WebMvcTest** - For controller layer tests

---

## Test Structure per Service

### 1. Account Service

#### Tests to Write:
```
account-service/
└── src/test/java/com/banking/account/
    ├── service/
    │   └── AccountServiceTest.java          ← Priority 1
    └── controller/
        └── AccountControllerTest.java       ← Priority 2
```

**AccountServiceTest.java** (Unit Test)
- ✅ `testCreateAccount_Success()`
- ✅ `testUpdateBalance_Success()`
- ✅ `testUpdateBalance_InsufficientFunds_ThrowsException()`
- ✅ `testGetAccount_NotFound_ThrowsException()`
- ✅ `testGetAccountsByUserId_ReturnsAccounts()`

**AccountControllerTest.java** (@WebMvcTest)
- ✅ `testCreateAccount_ValidRequest_Returns201()`
- ✅ `testCreateAccount_InvalidRequest_Returns400()`
- ✅ `testGetAccount_ValidId_Returns200()`

---

### 2. Transaction Service

#### Tests to Write:
```
transaction-service/
└── src/test/java/com/banking/transaction/
    ├── service/
    │   └── TransactionServiceTest.java      ← Priority 1
    └── controller/
        └── TransactionControllerTest.java   ← Priority 2
```

**TransactionServiceTest.java** (Unit Test)
- ✅ `testLogTransaction_Success()`
- ✅ `testGetTransactionsByAccountId_ReturnsTransactions()`
- ✅ `testGetTransactionById_NotFound_ThrowsException()`

---

### 3. Register Service

#### Tests to Write:
```
register-service/
└── src/test/java/com/banking/register/
    ├── service/
    │   └── RegisterServiceTest.java         ← Priority 1
    └── controller/
        └── RegisterControllerTest.java      ← Priority 2
```

**RegisterServiceTest.java** (Unit Test)
- ✅ `testRegisterUser_Success_HashesPassword()`
- ✅ `testRegisterUser_DuplicateUsername_ThrowsException()`
- ✅ `testRegisterUser_DuplicateEmail_ThrowsException()`
- ✅ `testRegisterUser_CreatesDefaultAccount()`

---

### 4. Auth Service

#### Tests to Write:
```
auth-service/
└── src/test/java/com/banking/auth/
    ├── service/
    │   └── AuthServiceTest.java             ← Priority 1
    ├── util/
    │   └── JwtUtilTest.java                 ← Priority 1
    └── controller/
        └── AuthControllerTest.java          ← Priority 2
```

**JwtUtilTest.java** (Unit Test)
- ✅ `testGenerateToken_Success()`
- ✅ `testValidateToken_ValidToken_ReturnsTrue()`
- ✅ `testValidateToken_InvalidToken_ReturnsFalse()`
- ✅ `testExtractUsername_Success()`
- ✅ `testExtractUserId_Success()`
- ✅ `testExtractRole_Success()`

**AuthServiceTest.java** (Unit Test)
- ✅ `testLogin_ValidCredentials_ReturnsToken()`
- ✅ `testLogin_InvalidPassword_ThrowsException()`
- ✅ `testLogin_UserNotFound_ThrowsException()`

---

### 5. Deposit Service

#### Tests to Write:
```
deposit-service/
└── src/test/java/com/banking/deposit/
    ├── service/
    │   └── DepositServiceTest.java          ← Priority 1
    └── controller/
        └── DepositControllerTest.java       ← Priority 2
```

**DepositServiceTest.java** (Unit Test with Mocked Feign Clients)
- ✅ `testDeposit_Success_UpdatesBalanceAndLogsTransaction()`
- ✅ `testDeposit_AccountNotFound_ThrowsException()`
- ✅ `testDeposit_InvalidAmount_ThrowsException()`
- ✅ `testDeposit_FeignClientError_ThrowsException()`

---

### 6. Transfer Service ✅ COMPLETED

#### Tests Implemented:
```
transfer-service/
└── src/test/java/com/banking/transfer/
    ├── service/
    │   └── TransferServiceTest.java         ✅ Completed
    └── controller/
        └── TransferControllerTest.java      ✅ Completed
```

**TransferServiceTest.java** (Unit Test with Mocked Feign Clients)
- ✅ `testTransfer_Success_UpdatesBothAccountsAndLogsTransactions()` - Implemented
- ✅ `testTransfer_InsufficientFunds_ThrowsException()` - Implemented
- ✅ `testTransfer_SameAccount_ThrowsException()` - Implemented
- ✅ `testTransfer_SenderAccountNotFound_ThrowsException()` - Implemented
- ✅ `testTransfer_ReceiverAccountNotFound_ThrowsException()` - Implemented

**TransferControllerTest.java** (@WebMvcTest)
- ✅ `testTransfer_ValidRequest_Returns200()` - Implemented
- ✅ `testTransfer_InvalidRequest_MissingFromAccountId_Returns400()` - Implemented
- ✅ `testTransfer_InvalidRequest_MissingToAccountId_Returns400()` - Implemented
- ✅ `testTransfer_InvalidRequest_MissingAmount_Returns400()` - Implemented
- ✅ `testTransfer_InvalidRequest_NegativeAmount_Returns400()` - Implemented
- ✅ `testTransfer_InvalidRequest_ZeroAmount_Returns400()` - Implemented
- ✅ `testTransfer_SameAccountTransfer_Returns400()` - Implemented
- ✅ `testTransfer_InsufficientFunds_Returns400()` - Implemented
- ✅ `testTransfer_AccountNotFound_Returns404()` - Implemented
- ✅ `testHealth_ReturnsHealthyStatus()` - Implemented

**Test Results:** ✅ All 15 tests passing (5 service + 10 controller)

---

### 7. API Gateway

#### Tests to Write:
```
api-gateway/
└── src/test/java/com/banking/gateway/
    ├── filter/
    │   └── JwtAuthenticationFilterTest.java ← Priority 1
    └── util/
        └── JwtUtilTest.java                 ← Priority 1
```

**JwtAuthenticationFilterTest.java** (Unit Test)
- ✅ `testFilter_ValidToken_AddsHeadersAndProceedsRequest()`
- ✅ `testFilter_InvalidToken_Returns401()`
- ✅ `testFilter_MissingToken_Returns401()`
- ✅ `testFilter_PublicRoute_SkipsAuthentication()`

---

## Test Dependencies (Add to Parent POM)

```xml
<dependencies>
    <!-- Already included in spring-boot-starter-test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Note:** `spring-boot-starter-test` already includes:
- JUnit 5
- Mockito
- AssertJ
- Hamcrest
- JSONassert
- Spring Test

---

## Common Test Patterns

### Pattern 1: Service Layer Test (with Mocked Repository)

```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    @Test
    void testCreateAccount_Success() {
        // Given
        CreateAccountRequest request = ...
        Account account = ...
        when(accountRepository.save(any())).thenReturn(account);
        
        // When
        AccountDto result = accountService.createAccount(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(accountRepository).save(any());
    }
}
```

### Pattern 2: Controller Test (with MockMvc)

```java
@WebMvcTest(AccountController.class)
class AccountControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AccountService accountService;
    
    @Test
    void testCreateAccount_ValidRequest_Returns201() throws Exception {
        // Given
        AccountDto accountDto = ...
        when(accountService.createAccount(any())).thenReturn(accountDto);
        
        // When & Then
        mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").exists());
    }
}
```

### Pattern 3: Orchestrator Service Test (with Mocked Feign Clients)

```java
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {
    
    @Mock
    private AccountClient accountClient;
    
    @Mock
    private TransactionClient transactionClient;
    
    @InjectMocks
    private DepositService depositService;
    
    @Test
    void testDeposit_Success() {
        // Given
        when(accountClient.getAccount(anyLong())).thenReturn(accountDto);
        when(accountClient.updateBalance(anyLong(), any())).thenReturn(updatedAccount);
        when(transactionClient.logTransaction(any())).thenReturn(transactionDto);
        
        // When
        DepositResponse response = depositService.deposit(request);
        
        // Then
        assertThat(response.getNewBalance()).isEqualTo(expectedBalance);
        verify(accountClient).updateBalance(anyLong(), any());
        verify(transactionClient).logTransaction(any());
    }
}
```

---

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run All Tests in Parallel
```bash
mvn -T 1C test
# -T 1C = 1 thread per CPU core
# or specify exact thread count: mvn -T 4 test
```

### Run Tests for Specific Service
```bash
cd account-service
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AccountServiceTest
```

### Run with Coverage Report (JaCoCo)
```bash
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

---

## Test Execution Order (Priority)

**Phase 1 (Critical - Hour 42-44):**
1. Auth Service: JwtUtilTest + AuthServiceTest
2. Account Service: AccountServiceTest
3. Transaction Service: TransactionServiceTest
4. Deposit Service: DepositServiceTest
5. Transfer Service: TransferServiceTest

**Phase 2 (Nice-to-have - Hour 44-46):**
6. Register Service: RegisterServiceTest
7. Gateway: JwtAuthenticationFilterTest
8. All Controller tests

---

## What NOT to Test

❌ **Skip These (Low Value / High Cost):**
- Getters/Setters (Lombok-generated)
- Entity constructors
- DTO mapping logic (unless complex)
- Spring Boot auto-configuration
- Feign client interfaces (test mocked behavior instead)
- Database queries (covered by integration tests)

---

## Code Coverage Goals

| Component | Target Coverage |
|-----------|----------------|
| Service Layer | 80%+ |
| Controller Layer | 70%+ |
| Util Classes (JwtUtil) | 90%+ |
| Overall Project | 70%+ |

---

## Testing Checklist

- [ ] Add test dependencies (already in spring-boot-starter-test)
- [ ] Create test directory structure for each service
- [ ] Write JwtUtil tests (Auth Service + Gateway)
- [ ] Write service layer tests for all data services
- [ ] Write service layer tests for orchestrator services
- [ ] Write controller tests (@WebMvcTest)
- [ ] Run `mvn test` and verify all pass

---

## Time Estimates

| Task | Time Estimate |
|------|---------------|
| Set up test structure | 30 minutes |
| Write Auth/JWT tests | 1 hour |
| Write Account + Transaction tests | 1.5 hours |
| Write Deposit + Transfer tests | 1.5 hours |
| Write Register tests | 30 minutes |
| Write Controller tests | 1 hour |
| Fix failing tests | 30 minutes |
| **Total** | **~6 hours** |

---

## Notes

- Keep tests **fast** (no external dependencies, mock everything)
- Use **descriptive test names** (given-when-then pattern)
- Test **business logic**, not Spring framework behavior
- Mock Feign clients to avoid inter-service dependencies
- Use `@WebMvcTest` instead of `@SpringBootTest` for controllers (faster)
- Don't test generated code (Lombok, MapStruct)