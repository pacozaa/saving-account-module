# Microservices Architecture Diagram

## Complete System Architecture

This diagram shows how all services in the banking system are wired together, including service discovery, API gateway routing, inter-service communication via Feign clients, and database dependencies.

```mermaid
graph TB
    %% External Clients
    Client[External Clients<br/>Web/Mobile Apps]
    
    %% Core Infrastructure
    subgraph Infrastructure["Infrastructure Layer"]
        Eureka[Eureka Server<br/>Service Discovery<br/>:8761]
        Gateway[API Gateway<br/>:8080<br/>Routes & JWT Validation]
    end
    
    %% Business Services
    subgraph BusinessServices["Business Services Layer"]
        Auth[Auth Service<br/>:8081<br/>Authentication & JWT]
        Register[Register Service<br/>:8082<br/>User Registration]
        Deposit[Deposit Service<br/>:8085<br/>Deposit Operations]
        Transfer[Transfer Service<br/>:8086<br/>Transfer Operations]
    end
    
    %% Data Services
    subgraph DataServices["Data Services Layer"]
        Account[Account Service<br/>:8083<br/>Account Management]
        Transaction[Transaction Service<br/>:8084<br/>Transaction Records]
    end
    
    %% Databases - Only data services have DBs
    subgraph Databases["Persistence Layer - Separate Databases"]
        RegisterDB[(H2 Database<br/>register-service/data/<br/>bankingdb.h2.db<br/><br/>Table: users)]
        AccountDB[(H2 Database<br/>account-service/data/<br/>bankingdb.h2.db<br/><br/>Table: accounts)]
        TransactionDB[(H2 Database<br/>transaction-service/data/<br/>bankingdb.h2.db<br/><br/>Table: transactions)]
    end
    
    %% Client to Gateway
    Client -->|HTTP Requests| Gateway
    
    %% Gateway Routes
    Gateway -->|/api/auth/**| Auth
    Gateway -->|/api/register/**| Register
    Gateway -->|/api/deposit/**| Deposit
    Gateway -->|/api/transfer/**| Transfer
    Gateway -->|/api/accounts/**| Account
    Gateway -->|/api/transactions/**| Transaction
    
    %% Service Discovery
    Gateway -.->|Register & Discover| Eureka
    Auth -.->|Register| Eureka
    Register -.->|Register| Eureka
    Deposit -.->|Register| Eureka
    Transfer -.->|Register| Eureka
    Account -.->|Register| Eureka
    Transaction -.->|Register| Eureka
    
    %% Inter-Service Communication (Feign Clients)
    Auth -->|UserClient<br/>Get User Info| Register
    Register -->|AccountClient<br/>Create Account| Account
    Deposit -->|AccountClient<br/>Get/Update Balance| Account
    Deposit -->|TransactionClient<br/>Record Transaction| Transaction
    Transfer -->|AccountClient<br/>Get/Update Balance| Account
    Transfer -->|TransactionClient<br/>Record Transaction| Transaction
    Transaction -->|AccountClient<br/>Verify Account| Account
    
    %% Service to Database (Each has own DB)
    Register -->|JPA: users table| RegisterDB
    Account -->|JPA: accounts table| AccountDB
    Transaction -->|JPA: transactions table| TransactionDB
    
    %% Styling
    classDef infrastructure fill:#e1f5ff,stroke:#0288d1,stroke-width:2px
    classDef business fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef data fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef database fill:#e8f5e9,stroke:#388e3c,stroke-width:2px
    classDef client fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class Eureka,Gateway infrastructure
    class Auth,Register,Deposit,Transfer business
    class Account,Transaction data
    class RegisterDB,AccountDB,TransactionDB database
    class Client client
```

## Service Communication Patterns

### 1. Service Discovery Pattern
```mermaid
sequenceDiagram
    participant Service
    participant Eureka
    participant Gateway
    
    Note over Service,Eureka: Service Registration
    Service->>Eureka: Register on startup
    Eureka-->>Service: Registration confirmed
    Service->>Eureka: Send heartbeat (every 30s)
    
    Note over Gateway,Eureka: Service Discovery
    Gateway->>Eureka: Lookup service by name
    Eureka-->>Gateway: Return service instances
    Gateway->>Service: Route request to instance
```

### 2. API Gateway Routing Pattern
```mermaid
graph LR
    Client[Client Request]
    Gateway{API Gateway<br/>JWT Filter}
    
    Client -->|POST /api/auth/login| Gateway
    Client -->|POST /api/register/users| Gateway
    Client -->|POST /api/deposit| Gateway
    Client -->|POST /api/transfer| Gateway
    Client -->|GET /api/accounts/:id| Gateway
    Client -->|GET /api/transactions| Gateway
    
    Gateway -->|Strip /api prefix<br/>Route to service| Services[Microservices]
    
    style Gateway fill:#e1f5ff,stroke:#0288d1,stroke-width:3px
```

### 3. Deposit Flow
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Deposit
    participant Account
    participant Transaction
    participant DB
    
    Client->>Gateway: POST /api/deposit
    Gateway->>Gateway: Validate JWT
    Gateway->>Deposit: Forward request
    
    Deposit->>Account: AccountClient.getAccount(id)
    Account->>DB: Query account
    DB-->>Account: Account data
    Account-->>Deposit: AccountDto
    
    Deposit->>Account: AccountClient.updateBalance(id, amount)
    Account->>DB: Update balance
    DB-->>Account: Updated account
    Account-->>Deposit: Updated AccountDto
    
    Deposit->>Transaction: TransactionClient.createTransaction()
    Transaction->>DB: Insert transaction
    DB-->>Transaction: Transaction record
    Transaction-->>Deposit: TransactionDto
    
    Deposit-->>Gateway: Success response
    Gateway-->>Client: 200 OK
```

### 4. Transfer Flow
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Transfer
    participant Account
    participant Transaction
    
    Client->>Gateway: POST /api/transfer
    Gateway->>Gateway: Validate JWT
    Gateway->>Transfer: Forward request
    
    Transfer->>Account: Get source account
    Account-->>Transfer: Source AccountDto
    Transfer->>Account: Get target account
    Account-->>Transfer: Target AccountDto
    
    Transfer->>Transfer: Validate balances
    
    Transfer->>Account: Update source balance (debit)
    Account-->>Transfer: Updated source account
    Transfer->>Account: Update target balance (credit)
    Account-->>Transfer: Updated target account
    
    Transfer->>Transaction: Record debit transaction
    Transaction-->>Transfer: Debit record
    Transfer->>Transaction: Record credit transaction
    Transaction-->>Transfer: Credit record
    
    Transfer-->>Gateway: Success response
    Gateway-->>Client: 200 OK
```

### 5. User Registration Flow
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Register
    participant Account
    participant DB
    
    Client->>Gateway: POST /api/register/users
    Gateway->>Register: Forward request
    
    Register->>DB: Check if username exists (users table)
    DB-->>Register: Not found
    
    Register->>DB: Create user record (users table)
    DB-->>Register: User created
    
    Register->>Account: AccountClient.createAccount()
    Account->>DB: Create account (accounts table)
    DB-->>Account: Account created
    Account-->>Register: AccountDto
    
    Register-->>Gateway: Success with user & account
    Gateway-->>Client: 201 Created
```

### 6. Authentication Flow
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Auth
    participant Register
    participant AuthDB
    
    Client->>Gateway: POST /api/auth/login
    Gateway->>Auth: Forward credentials
    
    Auth->>Register: UserClient.getUserByUsername()
    Register->>Register: Query users table
    Register-->>Auth: UserDto
    
    Auth->>Auth: Verify password
    Auth->>Auth: Generate JWT token
    
    Auth-->>Gateway: Token response
    Gateway-->>Client: 200 OK with JWT
    
    Note over Client,Gateway: Subsequent requests include JWT
    Client->>Gateway: Request with Authorization header
    Gateway->>Gateway: Validate JWT signature
    Gateway->>Gateway: Extract user info
    Gateway->>Auth: Forward with validated token
```

## Technology Stack

### Communication Patterns
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway with JWT filter
- **Inter-Service Communication**: OpenFeign clients (synchronous REST)
- **Load Balancing**: Client-side load balancing via Eureka

### Data Management
- **Database**: Data services have their own H2 file-based database at `./data/bankingdb.h2.db` (relative to service directory)
- **Database per Service**: 
  - `register-service/data/bankingdb.h2.db` - Table: `users` (user profiles & credentials)
  - `account-service/data/bankingdb.h2.db` - Table: `accounts`
  - `transaction-service/data/bankingdb.h2.db` - Table: `transactions`
- **Stateless Services**: Auth, Deposit, and Transfer services have no database - they use Feign clients to access data
- **ORM**: Spring Data JPA / Hibernate
- **Transaction Management**: Local transactions per service

### Key Design Decisions
1. **Database per Service**: Each service has its own isolated H2 database
   - Follows microservices principle of data autonomy
   - Each service owns its data and schema
   - Services communicate via Feign clients, never direct DB access
2. **Synchronous Communication**: Using Feign for simplicity in short time constraint
3. **Stateless Services**: JWT tokens for authentication, no session management
4. **API Gateway Pattern**: Single entry point with routing and security
5. **Service Registry**: Centralized service discovery with Eureka

## Port Mapping

| Service | Port | Purpose |
|---------|------|---------|
| Eureka Server | 8761 | Service discovery and registry |
| API Gateway | 8080 | External-facing API endpoint |
| Auth Service | 8081 | Authentication and JWT generation |
| Register Service | 8082 | User registration |
| Account Service | 8083 | Account management |
| Transaction Service | 8084 | Transaction records |
| Deposit Service | 8085 | Deposit operations |
| Transfer Service | 8086 | Transfer operations |

## API Gateway Routes

All external requests go through the gateway at `http://localhost:8080`:

- `/api/auth/**` → Auth Service
- `/api/register/**` → Register Service  
- `/api/accounts/**` → Account Service
- `/api/transactions/**` → Transaction Service
- `/api/deposit/**` → Deposit Service
- `/api/transfer/**` → Transfer Service

The gateway strips the `/api` prefix before routing to services.
