# Deployment Diagram

## Service Deployment Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Clients<br/>Web/Mobile/Desktop]
    end

    subgraph "Infrastructure - localhost"
        subgraph "API Gateway Layer - Port 8080"
            Gateway[API Gateway<br/>localhost:8080<br/>/api/*]
            SwaggerUI[Swagger UI<br/>localhost:8080/swagger-ui.html]
        end

        subgraph "Service Discovery - Port 8761"
            Eureka[Eureka Server<br/>localhost:8761<br/>/eureka/]
        end

        subgraph "Business Services Layer"
            Auth[Auth Service<br/>localhost:8081<br/>→ /api/auth/**]
            Register[Register Service<br/>localhost:8082<br/>→ /api/register/**]
            Account[Account Service<br/>localhost:8083<br/>→ /api/accounts/**]
            Transaction[Transaction Service<br/>localhost:8084<br/>→ /api/transactions/**]
            Deposit[Deposit Service<br/>localhost:8085<br/>→ /api/deposit/**]
            Transfer[Transfer Service<br/>localhost:8086<br/>→ /api/transfer/**]
        end

        subgraph "Data Layer - Independent Databases"
            DB1[(H2 Database<br/>register-service/data/<br/>bankingdb.h2.db<br/><br/>Table: users)]
            DB2[(H2 Database<br/>account-service/data/<br/>bankingdb.h2.db<br/><br/>Table: accounts)]
            DB3[(H2 Database<br/>transaction-service/data/<br/>bankingdb.h2.db<br/><br/>Table: transactions)]
        end
    end

    %% Client connections
    Client -->|HTTP Request| Gateway
    Client -->|View API Docs| SwaggerUI

    %% Gateway routing
    Gateway -->|Route + Load Balance| Auth
    Gateway -->|Route + Load Balance| Register
    Gateway -->|Route + Load Balance| Account
    Gateway -->|Route + Load Balance| Transaction
    Gateway -->|Route + Load Balance| Deposit
    Gateway -->|Route + Load Balance| Transfer

    %% Service discovery
    Auth -.->|Register| Eureka
    Register -.->|Register| Eureka
    Account -.->|Register| Eureka
    Transaction -.->|Register| Eureka
    Deposit -.->|Register| Eureka
    Transfer -.->|Register| Eureka
    Gateway -.->|Discover Services| Eureka

    %% Inter-service communication
    Deposit -->|Feign Client| Account
    Deposit -->|Feign Client| Transaction
    Transfer -->|Feign Client| Account
    Transfer -->|Feign Client| Transaction

    %% Database connections
    Register -->|JDBC| DB1
    Account -->|JDBC| DB2
    Transaction -->|JDBC| DB3
    
    %% Auth service has no DB - uses Register service
    Auth -->|Feign Client| Register

    style Gateway fill:#4CAF50,stroke:#333,stroke-width:3px,color:#fff
    style Eureka fill:#2196F3,stroke:#333,stroke-width:3px,color:#fff
    style Client fill:#FF9800,stroke:#333,stroke-width:2px,color:#fff
```

## Port Allocation Table

| Service | Port | Address | Purpose | External Access |
|---------|------|---------|---------|-----------------|
| **Eureka Server** | 8761 | `http://localhost:8761` | Service Discovery | Yes - Dashboard |
| **API Gateway** | 8080 | `http://localhost:8080` | Entry Point for all APIs | Yes - Main Entry |
| **Auth Service** | 8081 | `http://localhost:8081` | Authentication & JWT | Via Gateway |
| **Register Service** | 8082 | `http://localhost:8082` | User Registration | Via Gateway |
| **Account Service** | 8083 | `http://localhost:8083` | Account Management | Via Gateway |
| **Transaction Service** | 8084 | `http://localhost:8084` | Transaction Records | Via Gateway |
| **Deposit Service** | 8085 | `http://localhost:8085` | Deposit Operations | Via Gateway |
| **Transfer Service** | 8086 | `http://localhost:8086` | Transfer Operations | Via Gateway |

## API Gateway Route Mapping

| External Path | Internal Service | Internal Path | Port |
|--------------|------------------|---------------|------|
| `/api/auth/**` | auth-service | `/auth/**` → `/**` | 8081 |
| `/api/register/**` | register-service | `/register/**` → `/**` | 8082 |
| `/api/accounts/**` | account-service | `/accounts/**` → `/**` | 8083 |
| `/api/transactions/**` | transaction-service | `/transactions/**` → `/**` | 8084 |
| `/api/deposit/**` | deposit-service | `/deposit/**` → `/**` | 8085 |
| `/api/transfer/**` | transfer-service | `/transfer/**` → `/**` | 8086 |

## Service Endpoints

### Client Entry Points

- **Main API Gateway**: `http://localhost:8080`
- **Swagger UI (All Services)**: `http://localhost:8080/swagger-ui.html`
- **Eureka Dashboard**: `http://localhost:8761`

### Service-Specific Swagger Docs (via Gateway)

- Auth Service: `http://localhost:8080/api/auth/v3/api-docs`
- Register Service: `http://localhost:8080/api/register/v3/api-docs`
- Account Service: `http://localhost:8080/api/accounts/v3/api-docs`
- Transaction Service: `http://localhost:8080/api/transactions/v3/api-docs`
- Deposit Service: `http://localhost:8080/api/deposit/v3/api-docs`
- Transfer Service: `http://localhost:8080/api/transfer/v3/api-docs`

## Docker Deployment Architecture

```mermaid
graph TB
    subgraph "Docker Network: banking-network"
        subgraph "Container: eureka-server"
            ES[Eureka Server<br/>Port: 8761<br/>Health Check: /actuator/health]
        end

        subgraph "Container: api-gateway"
            GW[API Gateway<br/>Port: 8080<br/>Depends on: eureka-server]
        end

        subgraph "Container: auth-service"
            AS[Auth Service<br/>Port: 8081<br/>Volume: ./data]
        end

        subgraph "Container: register-service"
            RS[Register Service<br/>Port: 8082<br/>Volume: ./data]
        end

        subgraph "Container: account-service"
            ACS[Account Service<br/>Port: 8083<br/>Volume: ./data]
        end

        subgraph "Container: transaction-service"
            TS[Transaction Service<br/>Port: 8084<br/>Volume: ./data]
        end

        subgraph "Container: deposit-service"
            DS[Deposit Service<br/>Port: 8085<br/>Depends on: account, transaction]
        end

        subgraph "Container: transfer-service"
            TRS[Transfer Service<br/>Port: 8086<br/>Depends on: account, transaction]
        end
    end

    Host[Host Machine] -->|8761:8761| ES
    Host -->|8080:8080| GW
    Host -->|8081:8081| AS
    Host -->|8082:8082| RS
    Host -->|8083:8083| ACS
    Host -->|8084:8084| TS
    Host -->|8085:8085| DS
    Host -->|8086:8086| TRS

    GW -.->|Service Discovery| ES
    AS -.->|Register| ES
    RS -.->|Register| ES
    ACS -.->|Register| ES
    TS -.->|Register| ES
    DS -.->|Register| ES
    TRS -.->|Register| ES

    DS -->|HTTP/Feign| ACS
    DS -->|HTTP/Feign| TS
    TRS -->|HTTP/Feign| ACS
    TRS -->|HTTP/Feign| TS

    style ES fill:#2196F3,stroke:#333,stroke-width:2px,color:#fff
    style GW fill:#4CAF50,stroke:#333,stroke-width:2px,color:#fff
    style Host fill:#FF9800,stroke:#333,stroke-width:2px,color:#fff
```

## Database Storage Locations

| Service | Database Type | Storage Path | Database File | Table(s) |
|---------|--------------|--------------|---------------|----------|
| Auth Service | Stateless | N/A | N/A | Uses Feign to call Register Service |
| Register Service | H2 (File) | `./register-service/data/` | `bankingdb.h2.db` | `users` |
| Account Service | H2 (File) | `./account-service/data/` | `bankingdb.h2.db` | `accounts` |
| Transaction Service | H2 (File) | `./transaction-service/data/` | `bankingdb.h2.db` | `transactions` |
| Deposit Service | Stateless | N/A | N/A | Uses Feign to access Account/Transaction |
| Transfer Service | Stateless | N/A | N/A | Uses Feign to access Account/Transaction |

**Note**: Only Register, Account, and Transaction services have databases. Each has its own separate H2 database file at `./data/bankingdb.h2.db` relative to the service's working directory. Auth, Deposit, and Transfer services are stateless and access data via Feign clients.

## Service Communication Patterns

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway<br/>(8080)
    participant E as Eureka<br/>(8761)
    participant D as Deposit Service<br/>(8085)
    participant A as Account Service<br/>(8083)
    participant T as Transaction Service<br/>(8084)

    Note over E: All services register<br/>on startup
    
    C->>GW: POST /api/deposit/deposit
    GW->>E: Resolve deposit-service
    E-->>GW: localhost:8085
    GW->>D: POST /deposit
    D->>E: Resolve account-service
    E-->>D: localhost:8083
    D->>A: GET /accounts/{id}
    A-->>D: Account data
    D->>E: Resolve transaction-service
    E-->>D: localhost:8084
    D->>T: POST /transactions
    T-->>D: Transaction created
    D->>A: PUT /accounts/{id}
    A-->>D: Account updated
    D-->>GW: Deposit successful
    GW-->>C: 200 OK
```

## Health Check Endpoints

All services expose health check endpoints via Spring Boot Actuator:

- Eureka Server: `http://localhost:8761/actuator/health`
- API Gateway: `http://localhost:8080/actuator/health`
- Auth Service: `http://localhost:8081/actuator/health`
- Register Service: `http://localhost:8082/actuator/health`
- Account Service: `http://localhost:8083/actuator/health`
- Transaction Service: `http://localhost:8084/actuator/health`
- Deposit Service: `http://localhost:8085/actuator/health`
- Transfer Service: `http://localhost:8086/actuator/health`

## Environment Configuration

### Local Development
- All services run on localhost
- H2 databases in file mode (./data/)
- H2 Console enabled for debugging
- Eureka self-preservation disabled

### Docker Deployment
- Services communicate via `banking-network` bridge network
- Container-to-container communication uses service names
- Port mapping from container to host (e.g., 8080:8080)
- Eureka URL: `http://eureka-server:8761/eureka/`
- Health checks with retry logic

## Deployment Commands

### Local Development
```bash
# Start all services
./run.sh

# Stop all services
./stop.sh

# Check service status
./status.sh
```

### Docker Deployment
```bash
# Build all services
./docker-build.sh

# Start containers
./docker-start.sh

# Stop containers
./docker-stop.sh

# Or use docker-compose directly
docker-compose up -d
docker-compose down
```

## Network Flow Summary

1. **Client → API Gateway (8080)**: Single entry point for all requests
2. **API Gateway → Eureka (8761)**: Service discovery and load balancing
3. **API Gateway → Services (8081-8086)**: Routed requests with prefix stripping
4. **Services → Eureka (8761)**: Service registration and heartbeat
5. **Business Services → Core Services**: Inter-service communication via Feign
   - Deposit Service → Account Service (8083)
   - Deposit Service → Transaction Service (8084)
   - Transfer Service → Account Service (8083)
   - Transfer Service → Transaction Service (8084)
6. **Services → H2 Databases**: JDBC connections to file-based databases
