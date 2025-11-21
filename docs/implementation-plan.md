# Banking System - 48-Hour Implementation Plan

## Architecture Overview

Based on the microservices architecture with:
- **4 Client Types**: Registration Person, Transfer Customer, Deposit Teller, New Account Teller
- **API Gateway**: Single entry point for all clients
- **Core Services**: Auth, Deposit, Transfer, Register
- **Data Services**: Transaction Service, Account Service
- **Databases**: Transactions DB, Accounts DB, Users DB

## Technology Stack

- **Framework**: Spring Boot
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Inter-service Communication**: Spring Cloud OpenFeign
- **Database**: H2 (in-memory) for rapid development
- **Security**: JWT (JSON Web Tokens)
- **Build Tool**: Maven Multi-Module
- **Containerization**: Docker Compose

---

## Hour-by-Hour Implementation Plan

### **Phase 1: Infrastructure Setup (Hours 0-4)**

#### Hour 0-1: Project Structure ✅ COMPLETED
- [x] Create Maven parent POM with Spring Boot parent
- [x] Define modules:
  - `eureka-server` ✅
  - `api-gateway` ✅
  - `auth-service` ✅
  - `register-service` ✅
  - `account-service` ✅
  - `transaction-service` ✅
  - `deposit-service` ✅
  - `transfer-service` ✅
- [x] Set up shared dependencies in parent POM
- [x] Create basic Spring Boot applications for each module

**Completed:**
- Maven multi-module project structure created
- All 8 services have pom.xml with appropriate dependencies
- Main application classes created with proper annotations
- YAML configuration files set up for all services
- Maven build validated successfully (all modules recognized)

**Technologies Used:**
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Java 17
- H2 in-memory databases for data services
- JWT for authentication

---

#### Hour 1-2: Eureka Server
- [x] Add `spring-cloud-starter-netflix-eureka-server` dependency
- [x] Enable Eureka Server with `@EnableEurekaServer`
- [x] Configure `application.yml`:
  ```yaml
  server:
    port: 8761
  eureka:
    client:
      registerWithEureka: false
      fetchRegistry: false
  ```
- [x] Test server startup on http://localhost:8761

**Status:** Configuration complete, ready for testing

---

#### Hour 2-4: API Gateway
- [x] Add dependencies:
  - `spring-cloud-starter-gateway`
  - `spring-cloud-starter-netflix-eureka-client`
- [x] Configure routes in `application.yml`:
  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: auth-service
            uri: lb://auth-service
            predicates:
              - Path=/api/auth/**
          - id: register-service
            uri: lb://register-service
            predicates:
              - Path=/api/register/**
          - id: deposit-service
            uri: lb://deposit-service
            predicates:
              - Path=/api/deposit/**
          - id: transfer-service
            uri: lb://transfer-service
            predicates:
              - Path=/api/transactions/**
          - id: account-service
            uri: lb://account-service
            predicates:
              - Path=/api/accounts/**
  ```
- [x] Configure Eureka client settings
- [x] Test gateway routing

**Status:** ✅ COMPLETED - Gateway routing tested and verified

**Testing Results:**
- Eureka Server: Running on port 8761 ✅
- API Gateway: Running on port 8080, registered with Eureka ✅
- Account Service: Running on port 8083, registered with Eureka ✅
- Gateway Routing: Successfully routes requests from Gateway → Account Service ✅
- Test endpoint: `http://localhost:8080/api/accounts/health` returns 200 OK

**Note:** Service names in routes use lowercase to match `spring.application.name` values

---

### **Phase 2: Data Services (Hours 4-16)**

#### Hour 4-8: Account Service
- [x] Add dependencies:
  - Spring Data JPA ✅
  - H2 Database ✅
  - Lombok ✅
  - Spring Web ✅
  - Eureka Client ✅
  - Spring Validation ✅
- [ ] Create `Account` entity:
  ```java
  @Entity
  public class Account {
      @Id @GeneratedValue
      private Long id;
      private Long userId;
      private BigDecimal balance;
      private String accountType;
      private LocalDateTime createdAt;
  }
  ```
- [ ] Create `AccountRepository` (Spring Data JPA)
- [ ] Implement `AccountService`:
  - `createAccount(userId, accountType)`
  - `getAccount(accountId)`
  - `updateBalance(accountId, amount)`
  - `getAccountsByUserId(userId)`
- [ ] Create REST controller with endpoints:
  - `POST /api/accounts/create`
  - `GET /api/accounts/{id}`
  - `PUT /api/accounts/{id}/balance`
  - `GET /api/accounts/user/{userId}`
- [ ] Add validation and error handling
- [ ] Test endpoints with Postman/curl

#### Hour 8-12: Transaction Service
- [x] Add same dependencies as Account Service ✅
- [ ] Create `Transaction` entity:
  ```java
  @Entity
  public class Transaction {
      @Id @GeneratedValue
      private Long id;
      private Long accountId;
      private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
      private BigDecimal amount;
      private Long relatedAccountId; // For transfers
      private LocalDateTime timestamp;
      private String description;
  }
  ```
- [ ] Create `TransactionRepository`
- [ ] Implement `TransactionService`:
  - `logTransaction(transactionDto)`
  - `getTransactionsByAccountId(accountId)`
  - `getTransactionById(transactionId)`
- [ ] Create REST controller with endpoints:
  - `POST /api/transactions/log`
  - `GET /api/transactions/{id}`
  - `GET /api/transactions/account/{accountId}`
- [ ] Test transaction logging

#### Hour 12-16: Register Service
- [x] Add dependencies + Spring Security for BCrypt ✅
- [ ] Create `User` entity:
  ```java
  @Entity
  public class User {
      @Id @GeneratedValue
      private Long id;
      private String username;
      private String password; // BCrypt hashed
      private String email;
      private String role; // PERSON, CUSTOMER, TELLER
      private LocalDateTime registeredAt;
  }
  ```
- [ ] Create `UserRepository`
- [ ] Implement `RegisterService`:
  - Hash passwords with BCrypt
  - Validate unique username/email
  - Create user
  - Call Account Service via Feign to create default account
- [ ] Create Feign client for Account Service
- [ ] Create REST controller:
  - `POST /api/register` (creates user + default account)
- [ ] Test registration flow

---

### **Phase 3: Orchestrator Services (Hours 16-30)**

#### Hour 16-20: Deposit Service
- [x] Add dependencies including OpenFeign ✅
- [x] Enable Feign clients with `@EnableFeignClients` ✅
- [ ] Create Feign clients:
  ```java
  @FeignClient(name = "account-service")
  public interface AccountClient {
      @GetMapping("/api/accounts/{id}")
      AccountDto getAccount(@PathVariable Long id);
      
      @PutMapping("/api/accounts/{id}/balance")
      void updateBalance(@PathVariable Long id, @RequestParam BigDecimal amount);
  }
  
  @FeignClient(name = "transaction-service")
  public interface TransactionClient {
      @PostMapping("/api/transactions/log")
      void logTransaction(@RequestBody TransactionDto dto);
  }
  ```
- [ ] Implement `DepositService` logic:
  1. Validate account exists
  2. Update balance (add amount)
  3. Log transaction
- [ ] Create REST controller:
  - `POST /api/deposit` (accountId, amount, tellerId)
- [ ] Add error handling for invalid accounts
- [ ] Test deposit flow end-to-end

#### Hour 20-26: Transfer Service
- [x] Add same dependencies and Feign clients ✅
- [ ] Implement `TransferService` logic:
  ```java
  public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
      // 1. Validate sender account exists and has sufficient funds
      AccountDto senderAccount = accountClient.getAccount(fromAccountId);
      if (senderAccount.getBalance().compareTo(amount) < 0) {
          throw new InsufficientFundsException();
      }
      
      // 2. Validate receiver account exists
      AccountDto receiverAccount = accountClient.getAccount(toAccountId);
      
      // 3. Deduct from sender
      accountClient.updateBalance(fromAccountId, amount.negate());
      
      // 4. Add to receiver
      accountClient.updateBalance(toAccountId, amount);
      
      // 5. Log transactions (2 records)
      transactionClient.logTransaction(/* sender withdrawal */);
      transactionClient.logTransaction(/* receiver deposit */);
  }
  ```
- [ ] Create REST controller:
  - `POST /api/transfer` (fromAccountId, toAccountId, amount)
- [ ] Add comprehensive error handling:
  - Account not found
  - Insufficient funds
  - Same account transfer
- [ ] Implement rollback strategy (or document limitation)
- [ ] Test transfer scenarios

#### Hour 26-30: Refine Orchestrators
- [ ] Add request/response DTOs
- [ ] Add input validation (@Valid, @NotNull, @Positive)
- [ ] Create custom exceptions
- [ ] Implement `@ControllerAdvice` for clean error responses
- [ ] Add logging
- [ ] Test edge cases

---

### **Phase 4: Security & Auth (Hours 30-38)**

#### Hour 30-34: Auth Service
- [x] Add dependencies:
  - Spring Security ✅
  - JWT library (io.jsonwebtoken:jjwt) ✅
- [ ] Create Feign client to Register Service (to validate users)
- [ ] Implement JWT utility class:
  - `generateToken(username, userId, role)`
  - `validateToken(token)`
  - `extractUserId(token)`
- [ ] Create `AuthService`:
  - `/login` endpoint validates credentials
  - Returns JWT token
- [ ] Create REST controller:
  - `POST /api/auth/login` (username, password)
  - Returns: `{ "token": "...", "expiresIn": 3600 }`
- [ ] Test login flow

#### Hour 34-38: Gateway Security Filter
- [ ] Create `JwtAuthenticationFilter` in Gateway:
  ```java
  @Component
  public class JwtAuthenticationFilter implements GlobalFilter {
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
          // 1. Extract token from Authorization header
          // 2. Validate token
          // 3. Extract userId and role
          // 4. Add headers: X-User-Id, X-User-Role
          // 5. Continue or reject with 401
      }
  }
  ```
- [ ] Configure public routes (no auth needed):
  - `/api/auth/login`
  - `/api/register`
- [ ] Test authenticated requests
- [ ] Test rejection of invalid tokens

---

### **Phase 5: Polish & Integration (Hours 38-48)**

#### Hour 38-42: Docker Compose
- [ ] Create `docker-compose.yml`:
  ```yaml
  version: '3.8'
  services:
    postgres:
      image: postgres:15
      environment:
        POSTGRES_DB: banking
        POSTGRES_USER: admin
        POSTGRES_PASSWORD: password
    
    eureka-server:
      build: ./eureka-server
      ports:
        - "8761:8761"
    
    api-gateway:
      build: ./api-gateway
      ports:
        - "8080:8080"
      depends_on:
        - eureka-server
    
    # Add other services...
  ```
- [ ] Create Dockerfiles for each service
- [ ] Test `docker-compose up`
- [ ] Verify service discovery and routing

#### Hour 42-44: Documentation
- [ ] Add Swagger/OpenAPI to services:
  - Dependency: `springdoc-openapi-starter-webmvc-ui`
  - Access at: `/swagger-ui.html`
- [ ] Create README.md with:
  - Architecture diagram
  - Setup instructions
  - API endpoints documentation
  - Example curl commands
- [ ] Create Postman collection with example requests

#### Hour 44-46: Testing & Bug Fixes
- [ ] Test complete user journey:
  1. Register new user → Creates user + default account
  2. Login → Receives JWT token
  3. Deposit to account → Balance updates, transaction logged
  4. Transfer between accounts → Both balances update
  5. View transaction history
- [ ] Fix any discovered bugs
- [ ] Test error scenarios
- [ ] Verify all services are registered with Eureka

#### Hour 46-48: Final Polish
- [ ] Add consistent logging across services
- [ ] Ensure clean error responses (no stack traces)
- [ ] Add health check endpoints
- [ ] Performance test basic scenarios
- [ ] Create demo script/video
- [ ] Final documentation review

---

## Progress Summary

### ✅ Completed (Phase 1 - Hour 0-1)
- Maven multi-module project structure with 8 microservices
- All POMs configured with appropriate dependencies
- Main application classes with proper Spring annotations
- YAML configuration files for all services
- Service discovery configuration (Eureka)
- API Gateway routing configuration
- H2 database configuration for data services
- Feign client setup for orchestrator services
- JWT dependencies for Auth service
- Maven build validation successful

### 🚧 Next Steps (Hour 1-2)
- **Test Eureka Server startup**
- Verify service registration
- Test API Gateway routing
- Begin Account Service entity and repository implementation

### 📝 Notes
- All services configured to register with Eureka on localhost:8761
- Service names use lowercase (e.g., `auth-service` not `AUTH-SERVICE`)
- Each data service has its own H2 in-memory database
- JWT secret key configured in Auth service application.yml

---

## Service Port Assignments

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Register Service | 8082 |
| Account Service | 8083 |
| Transaction Service | 8084 |
| Deposit Service | 8085 |
| Transfer Service | 8086 |

---

## Critical Success Factors

### Must-Have (Core Requirements)
- ✅ All services running and registered with Eureka
- ✅ Gateway routing requests correctly
- ✅ JWT authentication working
- ✅ Deposit flow: Client → Gateway → Deposit Service → Account Service → Transaction Service
- ✅ Transfer flow: Client → Gateway → Transfer Service → Account Service → Transaction Service
- ✅ Register flow: Client → Gateway → Register Service → Account Service

### Nice-to-Have (Bonus Points)
- 🌟 Docker Compose for one-command startup
- 🌟 Swagger documentation
- 🌟 Comprehensive error handling
- 🌟 Clean API responses
- 🌟 Transaction history queries

### Acceptable Tradeoffs (48-hour constraints)
- ⚠️ **No distributed transactions**: Use synchronous calls with basic error handling instead of Saga pattern
- ⚠️ **No circuit breakers**: Skip Resilience4j for simplicity
- ⚠️ **In-memory database**: H2 instead of PostgreSQL (can switch later)
- ⚠️ **Synchronous communication**: REST instead of async messaging
- ⚠️ **Basic security**: JWT validation without refresh tokens

---

## Testing Strategy

### Unit Tests (If time permits)
- Service layer logic
- JWT token generation/validation

### Integration Tests
- Use Postman collection to test:
  - Complete registration flow
  - Login and token usage
  - Deposit with authentication
  - Transfer between accounts
  - Error scenarios (invalid accounts, insufficient funds)

### Manual Testing Checklist
- [ ] Can register new user as PERSON
- [ ] Registration creates default account
- [ ] Can login and receive JWT
- [ ] Can deposit with valid JWT
- [ ] Cannot deposit without JWT (401)
- [ ] Can transfer with sufficient funds
- [ ] Cannot transfer with insufficient funds (400)
- [ ] Transaction history shows all operations
- [ ] Multiple client types can access appropriate endpoints

---

## Common Pitfalls to Avoid

1. **Service naming inconsistency**: Ensure `spring.application.name` matches Feign client names
2. **Port conflicts**: Use unique ports for each service
3. **Circular dependencies**: Transaction/Account services should not call orchestrators
4. **Missing Eureka registration**: All services must be Eureka clients (except Eureka itself)
5. **JWT secret key**: Use same secret across Gateway and Auth Service
6. **Database conflicts**: Each service uses separate database/schema
7. **CORS issues**: Configure CORS in Gateway if testing with web frontend

---

## Delivery Checklist

- [ ] All 8 services running
- [ ] `docker-compose up` starts entire system
- [ ] README.md with setup instructions
- [ ] Postman collection with example requests
- [ ] Architecture matches provided diagram
- [ ] Demo video showing complete flow
- [ ] Code is on GitHub with clear commit history

---

## Emergency Time-Saving Tips

If running out of time (Hour 40+):

1. **Skip Docker**: Just provide scripts to run each service
2. **Skip Auth**: Use a simple API key instead of JWT
3. **Merge services**: Combine Deposit/Transfer into one "Operations Service"
4. **Use one database**: Remove service isolation temporarily
5. **Skip Swagger**: Provide curl commands in README instead

**Remember**: A working, simple implementation > A broken, complex one.
