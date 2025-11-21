This is a classic microservices challenge. To build this within **48 hours**, you must prioritize **velocity and simplicity** over enterprise-grade perfection. You need to cut corners on complexity (like distributed tracing or complex Sagas) while maintaining a clean, working architecture.

Architecture Overview is [here](.docs/main.mmd)
Here is a strategic roadmap to implementing this architecture using Spring Boot.

---

### 1. The Strategy: "KISS" (Keep It Simple, Stupid)

*   **Architecture Style:** Synchronous REST communication (easier to debug/write than Event-Driven).
*   **Service Discovery:** **Spring Cloud Netflix Eureka** (Standard, easy setup).
*   **Gateway:** **Spring Cloud Gateway**.
*   **Communication:** **Spring Cloud OpenFeign** (Cleaner than RestTemplate/WebClient).
*   **Database:** Use **H2 (In-memory)** for speed of development
*   **Repo Structure:** Use a **Maven Multi-Module** project. This allows you to share DTOs and run everything easily from one IDE window.

---

### 2. Project Structure (Maven Multi-Module)

Create a parent `pom.xml`. Inside it, create modules for each box in your diagram.

```text
banking-system/
├── pom.xml (Parent)
├── eureka-server/         (Service Discovery)
├── api-gateway/           (Entry Point)
├── auth-service/          (JWT handling)
├── register-service/      (User onboarding)
├── account-service/       (Balance mgmt - Owns Accounts DB)
├── transaction-service/   (History - Owns Transactions DB)
├── deposit-service/       (Orchestrator)
└── transfer-service/      (Orchestrator)
```

---

### 3. Infrastructure Setup (Hours 0-4)

**A. Eureka Server**
*   Dependencies: `spring-cloud-starter-netflix-eureka-server`
*   App Class: Add `@EnableEurekaServer`
*   Config (`application.yml`):
    ```yaml
    server:
      port: 8761
    eureka:
      client:
        registerWithEureka: false
        fetchRegistry: false
    ```

**B. API Gateway**
*   Dependencies: `spring-cloud-starter-gateway`, `spring-cloud-starter-netflix-eureka-client`
*   Config: Route traffic based on paths.
    ```yaml
    spring:
      cloud:
        gateway:
          routes:
            - id: auth-service
              uri: lb://AUTH-SERVICE
              predicates:
                - Path=/api/auth/**
            - id: transfer-service
              uri: lb://TRANSFER-SERVICE
              predicates:
                - Path=/api/transfer/**
            # Repeat for other services
    ```

---

### 4. Core Data Services (Hours 4-16)

Implement the services that own data first. These are the foundation.

**A. Account Service & Transaction Service**
*   **Tech:** Spring Data JPA, H2/Postgres, Lombok.
*   **Logic:** Simple CRUD.
    *   `AccountService`: `POST /create`, `GET /{id}`, `PUT /update-balance`.
    *   `TransactionService`: `POST /log-transaction`.
*   **Database:** Keep them strictly separate. `AccountService` cannot touch `Transactions DB`.

**B. Register Service (Users DB)**
*   **Logic:** Creates User entities. Encrypts passwords (BCrypt).
*   **Communication:** Once a user is created, it might need to call `Account Service` to create a default wallet/account.

---

### 5. The Orchestrator Services (Hours 16-30)

This is where the "Business Logic" logic happens (The Deposit and Transfer services). These services **do not own databases** (based on your diagram arrows); they coordinate calls.

**Use Feign Clients** to talk to the backend services.

**Example: Transfer Service**

1.  **Dependency:** `spring-cloud-starter-openfeign`
2.  **Feign Interface:**
    ```java
    @FeignClient(name = "account-service")
    public interface AccountClient {
        @GetMapping("/accounts/{id}")
        AccountDto getAccount(@PathVariable Long id);
        
        @PutMapping("/accounts/{id}/balance")
        void updateBalance(@PathVariable Long id, @RequestParam BigDecimal amount);
    }
    ```
3.  **Business Logic (The "Transfer"):**
    *   *Note: In a real app, you need Distributed Transactions (Saga Pattern). For 48h, keep it linear.*
    *   Step 1: Validate Sender has funds (Call Account Service).
    *   Step 2: Deduct from Sender (Call Account Service).
    *   Step 3: Add to Receiver (Call Account Service).
    *   Step 4: Log Transaction (Call Transaction Service).

---

### 6. Security / Auth Service (Hours 30-38)

*   **Strategy:** Use **JWT (JSON Web Tokens)**.
*   **Auth Service:**
    *   Endpoint: `/login`. Validates credentials against `Users DB`. Returns a signed JWT.
*   **Gateway Logic:**
    *   Create a `GlobalFilter` in the Gateway.
    *   Intercept every request. Check for `Authorization: Bearer <token>`.
    *   Validate the token signature. If valid, pass the request downstream. If not, return 401.
    *   *Pro Tip:* Pass the `userId` extracted from the token to downstream services via a header (e.g., `X-User-Id`).

---

### 7. "Bonus Points" for the Interview (Hours 38-48)

If you have time left, add these to stand out:

1.  **Docker Compose:**
    Create a `docker-compose.yml` at the root. Define services for Postgres, RabbitMQ (if used), and Zipkin.
    *   *Impressive factor:* "I can start the whole architecture with one command."

2.  **Swagger/OpenAPI:**
    Add `springdoc-openapi-starter-webmvc-ui` to your services.
    *   *Impressive factor:* "Here is the live documentation for my API."

3.  **Centralized Configuration:**
    Instead of a Config Server (too complex for 48h), just use `application.yml` in each service but keep them clean.

4.  **Error Handling:**
    Create a `@ControllerAdvice` in your services to return clean JSON errors (`{ "code": "INSUFFICIENT_FUNDS", "message": "..." }`) instead of Java stack traces.

### Summary Checklist

1.  [ ] **Eureka Server** (Running on 8761)
2.  [ ] **Gateway** (Routing to services)
3.  [ ] **Register/Auth** (issuing JWTs)
4.  [ ] **Account Service** (Updating balances in DB)
5.  [ ] **Transfer Service** (Calling Account Service via Feign)
6.  [ ] **Docker Compose** (to spin it all up)

**Don't panic about "Perfect Microservices."** In a 48-hour challenge, a clean, working synchronous system that *looks* like the diagram is better than a complex, broken asynchronous one. Good luck!