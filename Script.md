# Banking System - Shell Scripts Guide

This document explains all the shell scripts used to manage the microservices in the banking system.

## Overview

The banking system uses several shell scripts to automate the lifecycle management of microservices. These scripts handle starting, stopping, monitoring, and managing individual services with proper initialization sequences and error handling.

---

## Scripts Summary

| Script | Purpose | Usage |
|--------|---------|-------|
| `run.sh` | Start all services in correct order | `./run.sh` |
| `stop.sh` | Stop all services gracefully | `./stop.sh` |
| `status.sh` | Check status of all services | `./status.sh` |
| `manage-service.sh` | Interactive service management | `./manage-service.sh` |

---

## 1. run.sh - Start All Services

### Purpose
Builds and starts all microservices in the correct dependency order with appropriate initialization delays.

### Usage
```bash
./run.sh
```

### What It Does

1. **Prerequisite Checks**
   - Verifies Maven is installed
   - Checks that all required ports (8761, 8080-8086) are available
   - Exits with error if ports are in use

2. **Build Phase**
   - Runs `mvn clean install -DskipTests`
   - Compiles all services and packages them

3. **Service Startup (in order)**
   - **Eureka Server** (port 8761) - waits 30s
   - **API Gateway** (port 8080) - waits 20s
   - **Account Service** (port 8083) - waits 15s
   - **Transaction Service** (port 8084) - waits 15s
   - **Register Service** (port 8082) - waits 15s
   - **Auth Service** (port 8081) - waits 10s
   - **Deposit Service** (port 8085) - waits 10s
   - **Transfer Service** (port 8086) - waits 10s

4. **Process Management**
   - Each service runs as a background process
   - PID is saved to `logs/<service-name>.pid`
   - Logs are written to `logs/<service-name>.log`

### Key Features

✅ **Ordered Startup** - Eureka starts first so services can register
✅ **Wait Times** - Each service waits for initialization before next starts
✅ **Error Handling** - Exits on build failure
✅ **Logging** - All output captured to separate log files
✅ **Port Validation** - Prevents port conflicts upfront

### Example Output
```
================================================
  Banking System - Microservices Startup
================================================

Checking ports availability...
✓ All ports are available

Building all services...
✓ Build successful

Starting services...

Starting Eureka Server...
✓ Eureka Server started (PID: 12345)
  Waiting 30s for Eureka Server to initialize...

Starting API Gateway...
✓ API Gateway started (PID: 12346)
  Waiting 20s for API Gateway to initialize...

...

================================================
✓ All services started successfully!
================================================

Service URLs:
  Eureka Server:      http://localhost:8761
  API Gateway:        http://localhost:8080
  ...

Logs:
  Log files are available in: ./logs/
  To view logs: tail -f logs/<service-name>.log
```

---

## 2. stop.sh - Stop All Services

### Purpose
Gracefully stops all running microservices in reverse order.

### Usage
```bash
./stop.sh
```

### What It Does

1. **Service Shutdown (in reverse order)**
   - **Transfer Service** (port 8086)
   - **Deposit Service** (port 8085)
   - **Auth Service** (port 8081)
   - **Register Service** (port 8082)
   - **Transaction Service** (port 8084)
   - **Account Service** (port 8083)
   - **API Gateway** (port 8080)
   - **Eureka Server** (port 8761)

2. **Graceful Termination**
   - Sends SIGTERM signal to each process (PID from `.pid` file)
   - Waits up to 10 seconds for graceful shutdown
   - Force kills with SIGKILL if process doesn't stop

3. **Cleanup**
   - Removes `.pid` files after stopping
   - Optionally cleans up log files

### Key Features

✅ **Reverse Order Shutdown** - Dependencies stop before the services that depend on them
✅ **Graceful + Forceful** - Tries graceful shutdown first, then force kills if necessary
✅ **PID Tracking** - Uses stored PIDs for reliable process termination
✅ **Optional Log Cleanup** - Asks if you want to delete log files

### Interactive Prompt
```bash
Do you want to clean up log files? (y/N):
```

### Example Output
```
================================================
  Banking System - Stopping Services
================================================

Stopping Transfer Service (PID: 12352)...
✓ Transfer Service stopped

Stopping Deposit Service (PID: 12351)...
✓ Deposit Service stopped

...

✓ All services stopped

Do you want to clean up log files? (y/N): n

```

---

## 3. status.sh - Check Service Status

### Purpose
Displays the current status of all microservices and their Eureka registration.

### Usage
```bash
./status.sh
```

### What It Does

1. **Individual Service Status**
   - Checks each service's PID file
   - Verifies if process is running
   - Confirms port is listening
   - Shows PID and port number

2. **Eureka Registration Check**
   - Connects to Eureka Server (http://localhost:8761)
   - Fetches list of registered services
   - Displays all registered microservices

### Status Indicators

| Indicator | Meaning |
|-----------|---------|
| `✓ RUNNING` | Service is active and port is listening |
| `⚠ STARTING` | Process exists but port not ready yet |
| `✗ STOPPED` | Service is not running |
| `⚠ UNKNOWN` | Port in use but not managed by scripts |

### Example Output
```
================================================
  Banking System - Service Status
================================================

Eureka Server:        ✓ RUNNING (PID: 12345, Port: 8761)
API Gateway:          ✓ RUNNING (PID: 12346, Port: 8080)
Auth Service:         ✓ RUNNING (PID: 12347, Port: 8081)
Register Service:     ✓ RUNNING (PID: 12348, Port: 8082)
Account Service:      ✓ RUNNING (PID: 12349, Port: 8083)
Transaction Service:  ✓ RUNNING (PID: 12350, Port: 8084)
Deposit Service:      ✓ RUNNING (PID: 12351, Port: 8085)
Transfer Service:     ✓ RUNNING (PID: 12352, Port: 8086)

Quick Links:
  Eureka Dashboard: http://localhost:8761
  API Gateway:      http://localhost:8080

Checking Eureka registrations...
(This may take a moment)

Services registered with Eureka:
  - AUTH-SERVICE
  - ACCOUNT-SERVICE
  - DEPOSIT-SERVICE
  - TRANSFER-SERVICE
  - API-GATEWAY
  - REGISTER-SERVICE
  - TRANSACTION-SERVICE
```

---

## 4. manage-service.sh - Interactive Service Manager

### Purpose
Provides an interactive menu to start, stop, restart, and manage individual services without affecting others.

### Usage
```bash
./manage-service.sh
```

### Main Menu

```
================================================
  Banking System - Service Manager
================================================

Select an action:
  1 - Start a service
  2 - Stop a service
  3 - Restart a service
  4 - View service status
  5 - View service logs
  6 - Exit
```

### Action Details

#### Option 1: Start a Service
- Shows list of all 8 services
- Checks if service is already running
- Verifies port is available
- Builds the service with Maven
- Starts the service with appropriate wait time
- Creates `.pid` file and log file

```
Select a service:
  1 - Eureka Server
  2 - API Gateway
  3 - Auth Service
  ...
  0 - Back to main menu
```

#### Option 2: Stop a Service
- Shows list of all 8 services
- Gracefully stops selected service
- Removes `.pid` file

#### Option 3: Restart a Service
- Stops the service first
- Waits 2 seconds
- Starts the service again

#### Option 4: View Service Status
- Shows status of all services in one view
- Displays running state, PID, and port information

```
Service Status
================================================

Eureka Server:        ✓ RUNNING (PID: 12345, Port: 8761)
API Gateway:          ✓ RUNNING (PID: 12346, Port: 8080)
Auth Service:         ✓ RUNNING (PID: 12347, Port: 8081)
...
```

#### Option 5: View Service Logs
- Shows list of all 8 services
- Displays last 50 lines of selected service's log
- Useful for debugging service issues

```
Last 50 lines of Auth Service log:
================================================
[startup messages...]
2025-11-22 10:15:23.456 INFO  Starting AuthService
2025-11-22 10:15:24.789 INFO  Service registered with Eureka
================================================
```

### Key Features

✅ **Interactive Menu** - Easy navigation with numbered choices
✅ **Individual Control** - Start/stop services independently
✅ **Smart Building** - Only builds the selected service
✅ **Log Viewing** - Quick access to service logs for debugging
✅ **Status Monitoring** - Real-time service status check
✅ **Back Navigation** - Always option to go back to previous menu

---

## Common Workflows

### Workflow 1: Start Everything Fresh

```bash
# Start all services in correct order
./run.sh

# Check if everything is running
./status.sh
```

### Workflow 2: Stop Everything

```bash
# Stop all services gracefully
./stop.sh
```

### Workflow 3: Restart a Single Service

```bash
# Start the interactive manager
./manage-service.sh

# Select: 3 - Restart a service
# Choose the service you want to restart
```

### Workflow 4: Debug a Service

```bash
# Start the interactive manager
./manage-service.sh

# Select: 5 - View service logs
# Choose the service to debug
# View the last 50 lines of logs
```

### Workflow 5: Troubleshoot Port Conflict

```bash
# Check service status
./status.sh

# If a service shows UNKNOWN status (port in use but not managed)
# Find the process using that port:
lsof -i :8080  # Check port 8080

# Kill the process if needed
kill -9 <PID>

# Try starting the service again
./manage-service.sh
```

---

## File Structure

```
project-root/
├── run.sh                 # Start all services
├── stop.sh                # Stop all services
├── status.sh              # Check service status
├── manage-service.sh      # Interactive service manager
├── logs/                  # Service logs and PID files (created at runtime)
│   ├── Eureka Server.pid
│   ├── Eureka Server.log
│   ├── API Gateway.pid
│   ├── API Gateway.log
│   └── ... (for each service)
├── eureka-server/
├── api-gateway/
├── auth-service/
├── register-service/
├── account-service/
├── transaction-service/
├── deposit-service/
└── transfer-service/
```

---

## Port Mapping

| Service | Port | Purpose |
|---------|------|---------|
| Eureka Server | 8761 | Service registry and discovery |
| API Gateway | 8080 | Main entry point for all API calls |
| Auth Service | 8081 | Authentication and authorization |
| Register Service | 8082 | User registration |
| Account Service | 8083 | Account management |
| Transaction Service | 8084 | Transaction history and records |
| Deposit Service | 8085 | Deposit operations |
| Transfer Service | 8086 | Money transfer operations |

---

## Troubleshooting

### Issue: "Port X is already in use"

**Solution 1**: Stop all services
```bash
./stop.sh
```

**Solution 2**: Check what's using the port
```bash
lsof -i :8080  # Replace 8080 with the problematic port
```

**Solution 3**: Force kill the process
```bash
kill -9 <PID>
```

### Issue: Service not starting/showing STARTING status

**Solution**: Wait a bit longer
- Some services need time to initialize
- Check the logs: `tail -f logs/Service\ Name.log`
- Use manage-service.sh option 5 to view logs

### Issue: Services not registering with Eureka

**Checklist**:
1. Is Eureka Server running? `./status.sh`
2. Are services able to reach Eureka on port 8761?
3. Check service logs for connection errors
4. Wait 30-60 seconds (Eureka registration takes time)

### Issue: Build fails during startup

**Solution**: 
- Ensure Java 11+ and Maven are installed
- Check individual service directories for compilation errors
- Run `mvn clean install -DskipTests` manually to see detailed errors

---

## Environment Requirements

- **Java**: Version 11 or higher
- **Maven**: Version 3.6.0 or higher
- **Bash**: Standard Unix shell
- **Available Ports**: 8761, 8080-8086 (not in use)
- **System**: macOS, Linux, or WSL on Windows

### Check Prerequisites

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check available ports
lsof -i :8761  # Should show nothing if available
```

---

## Best Practices

1. **Always use `run.sh` to start everything fresh** - Ensures correct startup order
2. **Use `manage-service.sh` for development** - Better control during debugging
3. **Monitor logs during development** - `tail -f logs/Service\ Name.log`
4. **Use `status.sh` after making changes** - Verify all services are running
5. **Clean shutdown with `stop.sh`** - Prevents lingering processes
6. **Check Eureka dashboard** - http://localhost:8761 for service health

---

## Additional Commands

### View live logs
```bash
tail -f logs/API\ Gateway.log
```

### Check if all services are listening
```bash
for port in 8761 8080 8081 8082 8083 8084 8085 8086; do
  echo -n "Port $port: "
  (timeout 1 bash -c "echo >/dev/tcp/127.0.0.1/$port" 2>/dev/null && echo "OPEN") || echo "CLOSED"
done
```

### Restart everything
```bash
./stop.sh && sleep 5 && ./run.sh
```

---

## Quick Reference

```bash
# Start everything
./run.sh

# Check status
./status.sh

# Stop everything
./stop.sh

# Manage individual services
./manage-service.sh

# View logs
tail -f logs/Service\ Name.log
```

---

## Summary

| Script | When to Use | Time Required |
|--------|------------|---------------|
| `run.sh` | First startup, full system restart | 3-5 minutes |
| `stop.sh` | Graceful shutdown, cleanup | 30 seconds |
| `status.sh` | Quick health check | 10 seconds |
| `manage-service.sh` | Development, debugging, single service restart | Varies (seconds to minutes) |

