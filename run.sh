#!/bin/bash

# Banking System - Multi-Service Startup Script
# This script starts all microservices in the correct order

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Log file directory
LOG_DIR="logs"
mkdir -p "$LOG_DIR"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}  Banking System - Microservices Startup${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local wait_time=$3
    
    echo -e "${YELLOW}Starting ${service_name}...${NC}"
    cd "$service_dir"
    mvn spring-boot:run > "../${LOG_DIR}/${service_name}.log" 2>&1 &
    local pid=$!
    echo $pid > "../${LOG_DIR}/${service_name}.pid"
    cd ..
    echo -e "${GREEN}✓ ${service_name} started (PID: $pid)${NC}"
    
    if [ $wait_time -gt 0 ]; then
        echo -e "${BLUE}  Waiting ${wait_time}s for ${service_name} to initialize...${NC}"
        sleep $wait_time
    fi
}

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${RED}✗ Port $port is already in use!${NC}"
        echo -e "${RED}  Please stop the service using this port and try again.${NC}"
        exit 1
    fi
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ Maven is not installed or not in PATH${NC}"
    exit 1
fi

echo -e "${BLUE}Checking ports availability...${NC}"
check_port 8761
check_port 8080
check_port 8081
check_port 8082
check_port 8083
check_port 8084
check_port 8085
check_port 8086
echo -e "${GREEN}✓ All ports are available${NC}"
echo ""

# Build all services
echo -e "${BLUE}Building all services...${NC}"
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful${NC}"
    echo ""
else
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
fi

# Start services in order
echo -e "${BLUE}Starting services...${NC}"
echo ""

# 1. Start Eureka Server (wait 30s for full startup)
start_service "Eureka Server" "eureka-server" 30

# 2. Start API Gateway (wait 20s)
start_service "API Gateway" "api-gateway" 20

# 3. Start Data Services (wait 15s each for DB initialization)
start_service "Account Service" "account-service" 15
start_service "Transaction Service" "transaction-service" 15
start_service "Register Service" "register-service" 15

# 4. Start Auth Service (wait 10s)
start_service "Auth Service" "auth-service" 10

# 5. Start Orchestrator Services (wait 10s each)
start_service "Deposit Service" "deposit-service" 10
start_service "Transfer Service" "transfer-service" 10

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✓ All services started successfully!${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "${YELLOW}Service URLs:${NC}"
echo -e "  Eureka Server:      ${GREEN}http://localhost:8761${NC}"
echo -e "  API Gateway:        ${GREEN}http://localhost:8080${NC}"
echo -e "  Auth Service:       ${GREEN}http://localhost:8081${NC}"
echo -e "  Register Service:   ${GREEN}http://localhost:8082${NC}"
echo -e "  Account Service:    ${GREEN}http://localhost:8083${NC}"
echo -e "  Transaction Service: ${GREEN}http://localhost:8084${NC}"
echo -e "  Deposit Service:    ${GREEN}http://localhost:8085${NC}"
echo -e "  Transfer Service:   ${GREEN}http://localhost:8086${NC}"
echo ""
echo -e "${YELLOW}Logs:${NC}"
echo -e "  Log files are available in: ${GREEN}./${LOG_DIR}/${NC}"
echo -e "  To view logs: ${GREEN}tail -f ${LOG_DIR}/<service-name>.log${NC}"
echo ""
echo -e "${YELLOW}Process Management:${NC}"
echo -e "  To stop all services: ${GREEN}./stop.sh${NC}"
echo -e "  To check status: ${GREEN}./status.sh${NC}"
echo ""
echo -e "${BLUE}Waiting for all services to register with Eureka...${NC}"
echo -e "${BLUE}This may take 30-60 seconds.${NC}"
echo ""
echo -e "${GREEN}You can now access the API Gateway at: http://localhost:8080${NC}"
echo -e "${GREEN}Check service registration at: http://localhost:8761${NC}"
