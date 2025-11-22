# Running the Banking System with Docker Compose

This guide explains how to build, run, and test the microservices using Docker Compose.

## Prerequisites

- Docker and Docker Compose installed
- Java 17 and Maven installed (for building JARs)
- Ports 8080-8086 and 8761 available

## Quick Start

### Option 1: Using Helper Scripts (Recommended)

```bash
# 1. Build everything (JARs + Docker images)
./docker-build.sh

# 2. Start all services
./docker-start.sh

# 3. Check status
docker-compose ps

# 4. View logs
docker-compose logs -f

# 5. Stop all services
./docker-stop.sh
```

### Option 2: Manual Commands

```bash
# 1. Build all Maven modules
./mvnw clean package -DskipTests

# 2. Build Docker images
docker-compose build

# 3. Start all services in detached mode
docker-compose up -d

# 4. Stop all services
docker-compose down

# 5. Stop and remove volumes (clean slate)
docker-compose down -v
```

## Service Startup Order

Docker Compose handles the startup sequence automatically:

1. **Eureka Server** (8761) - Starts first with health check
2. **API Gateway** (8080) - Waits for Eureka to be healthy
3. **Auth Service** (8081) - Waits for Eureka
4. **Register Service** (8082) - Waits for Eureka
5. **Account Service** (8083) - Waits for Eureka
6. **Transaction Service** (8084) - Waits for Eureka
7. **Deposit Service** (8085) - Waits for Eureka, Account, and Transaction
8. **Transfer Service** (8086) - Waits for Eureka, Account, and Transaction

## Verifying the Services are Running

### 1. Check Container Status

```bash
# View all running containers
docker-compose ps

# Should show all 8 services as "Up"
```

### 2. Check Eureka Dashboard

Open your browser and navigate to:
```
http://localhost:8761
```

You should see all services registered:
- API-GATEWAY
- AUTH-SERVICE
- REGISTER-SERVICE
- ACCOUNT-SERVICE
- TRANSACTION-SERVICE
- DEPOSIT-SERVICE
- TRANSFER-SERVICE

### 3. Check API Gateway Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 4. Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

You should see all service APIs listed.

## Testing the System

### Test 1: Register a New User (Customer)

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "citizenId": "1234567890123",
    "thaiName": "จอห์น โด",
    "englishName": "John Doe",
    "pin": "123456",
    "role": "CUSTOMER"
  }'
```

Expected response: `201 Created` with user details

### Test 2: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

Expected response: `200 OK` with JWT token
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "role": "CUSTOMER"
}
```

**Save the token for subsequent requests!**

### Test 3: Create a Savings Account

```bash
# Replace YOUR_JWT_TOKEN with the actual token from login
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "accountType": "SAVINGS",
    "initialBalance": 1000.00
  }'
```

Expected response: `201 Created` with account details

### Test 4: Get Account Details

```bash
# Replace ACCOUNT_ID and YOUR_JWT_TOKEN
curl -X GET http://localhost:8080/api/accounts/ACCOUNT_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Expected response: `200 OK` with account information

### Test 5: Make a Deposit

```bash
# Replace ACCOUNT_ID and YOUR_JWT_TOKEN
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "accountId": "ACCOUNT_ID",
    "amount": 500.00
  }'
```

Expected response: `200 OK` with transaction details

### Test 6: Make a Transfer

```bash
# First, create another account to transfer to
# Then use this command (replace IDs and token)
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "fromAccountId": "SOURCE_ACCOUNT_ID",
    "toAccountId": "TARGET_ACCOUNT_ID",
    "amount": 100.00
  }'
```

Expected response: `200 OK` with transaction details

### Test 7: Get Transaction History

```bash
# Replace ACCOUNT_ID and YOUR_JWT_TOKEN
curl -X GET "http://localhost:8080/api/transactions?accountId=ACCOUNT_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Expected response: `200 OK` with list of transactions

## Monitoring and Debugging

### View Logs for All Services

```bash
docker-compose logs -f
```

### View Logs for a Specific Service

```bash
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
docker-compose logs -f account-service
# etc.
```

### Check Service Health Endpoints

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# Account Service
curl http://localhost:8083/actuator/health

# Transaction Service
curl http://localhost:8084/actuator/health
```

### Access H2 Console (if needed for debugging)

Services with databases have H2 console enabled:

- Auth Service: http://localhost:8081/h2-console
- Register Service: http://localhost:8082/h2-console
- Account Service: http://localhost:8083/h2-console
- Transaction Service: http://localhost:8084/h2-console

**JDBC URL:** `jdbc:h2:file:/data/[servicename]db`  
**Username:** `sa`  
**Password:** (leave empty)

### Execute Commands Inside Containers

```bash
# Access a service container
docker exec -it api-gateway sh

# Check network connectivity between containers
docker exec -it api-gateway ping eureka-server
```

## Troubleshooting

### Services Won't Start

```bash
# Check if ports are already in use
lsof -i :8080
lsof -i :8761

# Stop any conflicting services
./stop.sh  # Stop local services if running
```

### Services Not Registering with Eureka

```bash
# Check Eureka logs
docker-compose logs eureka-server

# Restart a specific service
docker-compose restart auth-service

# Wait 30-60 seconds for registration
```

### Database Issues

```bash
# Remove all volumes and start fresh
docker-compose down -v
./docker-build.sh
./docker-start.sh
```

### Out of Memory

```bash
# Increase Docker memory allocation in Docker Desktop
# Recommended: At least 4GB for all services
```

### Cannot Build Images

```bash
# Ensure JARs are built first
./mvnw clean package -DskipTests

# Check target directories
ls -la */target/*.jar

# Rebuild specific service
docker-compose build --no-cache auth-service
```

## Complete Test Workflow Example

Here's a complete example to test the full workflow:

```bash
# 1. Register a customer user
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "pass123",
    "email": "alice@example.com",
    "citizenId": "1111111111111",
    "thaiName": "อลิซ วันเดอร์แลนด์",
    "englishName": "Alice Wonderland",
    "pin": "111111",
    "role": "CUSTOMER"
  }'

# 2. Login and save the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123"}' | jq -r '.token')

echo "Token: $TOKEN"

# 3. Create a savings account
ACCOUNT=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"accountType":"SAVINGS","initialBalance":5000.00}')

ACCOUNT_ID=$(echo $ACCOUNT | jq -r '.id')
echo "Account ID: $ACCOUNT_ID"

# 4. Check account balance
curl -X GET "http://localhost:8080/api/accounts/$ACCOUNT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq

# 5. Make a deposit
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"accountId\":\"$ACCOUNT_ID\",\"amount\":1000.00}" | jq

# 6. Check updated balance
curl -X GET "http://localhost:8080/api/accounts/$ACCOUNT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq

# 7. View transaction history
curl -X GET "http://localhost:8080/api/transactions?accountId=$ACCOUNT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Cleanup

### Stop Services (Keep Data)

```bash
docker-compose down
```

### Stop Services and Remove Data

```bash
docker-compose down -v
```

### Remove All Images

```bash
docker-compose down --rmi all -v
```

## Performance Tips

- First startup takes longer due to service discovery and registration (30-60 seconds)
- Subsequent requests are faster once all services are registered
- Check Eureka dashboard to confirm all services are UP before testing

## Data Persistence

Database data is stored in Docker volumes:
- `auth-data` - User authentication data
- `register-data` - User registration data
- `account-data` - Account information
- `transaction-data` - Transaction history

Data persists across container restarts unless you use `docker-compose down -v`.

## Next Steps

- Review the Swagger documentation at http://localhost:8080/swagger-ui.html
- Check the service architecture in `docs/diagrams/`
- Read the test scenarios in `docs/test-scenario.md`
- View the implementation plan in `docs/implementation-plan.md`

## Support

If you encounter issues:
1. Check the logs: `docker-compose logs -f`
2. Verify Eureka dashboard: http://localhost:8761
3. Ensure all JARs are built: `ls -la */target/*.jar`
4. Review `DOCKER_FIXES.md` for configuration details
