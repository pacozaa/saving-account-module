# H2 Database Access

## Shared Database
All services now use a single shared H2 file-based database for simplified development.

**Database File Location:** `./data/bankingdb.h2.db` (relative to where each service is started from)

**Services Connected:**
- account-service (port 8083)
- register-service (port 8082)
- transaction-service (port 8084)
- auth-service (port 8081)

**Connection Details:**
- JDBC URL: `jdbc:h2:file:./data/bankingdb`
- Username: `sa`
- Password: (leave blank)
- Driver: `org.h2.Driver`

## H2 Console Access
H2 Web Console is available on all database-connected services:
- Account Service: `http://localhost:8083/h2-console`
- Register Service: `http://localhost:8082/h2-console`
- Transaction Service: `http://localhost:8084/h2-console`
- Auth Service: `http://localhost:8081/h2-console`

Use the JDBC URL above to connect in the H2 console.