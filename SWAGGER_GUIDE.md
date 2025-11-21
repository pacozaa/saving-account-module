# Swagger/OpenAPI Documentation Guide

## Overview

This project includes comprehensive Swagger/OpenAPI 3 documentation for all microservices. The documentation provides:

- Interactive API testing
- Detailed request/response schemas
- Parameter descriptions
- Authentication configuration
- Example values for all endpoints

## Quick Access

### Primary Access (Recommended)
**API Gateway Aggregated View:** http://localhost:8080/swagger-ui.html

This provides a unified view of all service APIs. Use the dropdown in the top-right to switch between services.

### Individual Service Access

| Service | Port | Swagger UI | OpenAPI JSON |
|---------|------|------------|--------------|
| Auth Service | 8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Register Service | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Account Service | 8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Transaction Service | 8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| Deposit Service | 8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| Transfer Service | 8086 | http://localhost:8086/swagger-ui.html | http://localhost:8086/v3/api-docs |

## How to Use Swagger UI

### 1. Testing Public Endpoints (No Authentication)

**Example: Register a New User**

1. Navigate to http://localhost:8080/swagger-ui.html
2. Select "Register Service" from the dropdown
3. Find `POST /register` endpoint
4. Click "Try it out"
5. Edit the request body with your data
6. Click "Execute"
7. View the response below

**Example: Login**

1. Select "Auth Service" from the dropdown
2. Find `POST /auth/login` endpoint
3. Click "Try it out"
4. Enter credentials in the request body
5. Click "Execute"
6. Copy the `token` from the response (you'll need this for protected endpoints)

### 2. Testing Protected Endpoints (Requires Authentication)

Most endpoints require JWT authentication. Here's how to set it up:

1. **Get a JWT Token** (if you don't have one):
   - Use the Auth Service login endpoint as shown above
   - Copy the token value from the response

2. **Authorize Swagger UI**:
   - Click the **"Authorize"** button (🔓 icon) at the top of the page
   - In the dialog, enter: `Bearer <your-token-here>`
     - Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - Click "Authorize"
   - Click "Close"

3. **Now you can test protected endpoints**:
   - All subsequent requests will include the JWT token
   - The lock icon (🔒) will appear closed for authenticated requests

### 3. Example Workflows

#### Complete Registration and Account Check Flow

1. **Register**: `POST /register` - Create new user
2. **Login**: `POST /auth/login` - Get JWT token
3. **Authorize**: Click Authorize button and add token
4. **Check Account**: `GET /accounts/user/{userId}` - View your accounts

#### Deposit Flow

1. Ensure you're authenticated (see step 2-3 above)
2. Navigate to Deposit Service
3. Use `POST /deposit` endpoint
4. Fill in accountId and amount
5. Execute to make a deposit
6. Check Transaction Service to see the logged transaction

#### Transfer Flow

1. Ensure you're authenticated
2. Navigate to Transfer Service
3. Use `POST /transfer` endpoint
4. Fill in fromAccountId, toAccountId, and amount
5. Execute to transfer funds
6. Check both accounts to see updated balances

## API Documentation Features

### Request Schemas
Every endpoint shows:
- Required vs optional fields
- Data types (string, number, etc.)
- Validation rules (min/max values, patterns)
- Example values

### Response Codes
Documentation includes all possible HTTP responses:
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid JWT)
- `404` - Not Found
- `409` - Conflict (duplicate username/email)

### Security Schemes
All protected endpoints show the lock icon (🔒) and document the JWT Bearer authentication requirement.

## Tips and Best Practices

1. **Use the Gateway**: Access all services through http://localhost:8080/swagger-ui.html for convenience

2. **Keep Your Token Handy**: JWT tokens expire after 24 hours. Copy and save your token for testing sessions

3. **Check Examples**: Every DTO field includes example values to help you construct valid requests

4. **Export API Spec**: Download the OpenAPI JSON from `/v3/api-docs` endpoints for use with other tools like Postman

5. **Development Mode**: The controllers include mock implementations with TODO comments. Replace these with actual business logic as needed

## Architecture Notes

### Service Organization

- **Auth Service**: No database, handles JWT generation/validation
- **Register Service**: Owns Users DB, creates accounts via Account Service
- **Account Service**: Owns Accounts DB, manages balances
- **Transaction Service**: Owns Transactions DB, logs all operations
- **Deposit Service**: Orchestrator, coordinates Account + Transaction services
- **Transfer Service**: Orchestrator, coordinates Account + Transaction services

### API Gateway Routes

All services are accessible through the gateway with these path prefixes:
- `/api/auth/**` → Auth Service
- `/api/register/**` → Register Service
- `/api/accounts/**` → Account Service
- `/api/transactions/**` → Transaction Service
- `/api/deposit/**` → Deposit Service
- `/api/transfer/**` → Transfer Service

## Troubleshooting

### Cannot Access Swagger UI

1. Ensure the service is running: `curl http://localhost:<port>/actuator/health`
2. Wait 30-60 seconds for services to register with Eureka
3. Check logs for any startup errors

### 401 Unauthorized Errors

1. Make sure you've clicked the "Authorize" button
2. Verify your token starts with "Bearer " prefix
3. Check if your token has expired (24-hour lifetime)
4. Try logging in again to get a fresh token

### Validation Errors

- Read the error messages in the response
- Check the schema documentation for field requirements
- Ensure all required fields are provided
- Verify data types match (numbers as numbers, not strings)

## Additional Resources

- **Eureka Dashboard**: http://localhost:8761 - View registered services
- **API Gateway Actuator**: http://localhost:8080/actuator - Gateway health and metrics
- **H2 Consoles**: http://localhost:808{2,3,4}/h2-console - Database access for data services

## Next Steps

1. Start all services (see README.md Quick Start section)
2. Open http://localhost:8080/swagger-ui.html in your browser
3. Try the example workflows above
4. Explore the API documentation for all available endpoints
5. Use the interactive testing features to understand the API behavior

Happy API testing! 🚀
