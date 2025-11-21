#!/bin/bash

# Banking System - Status Check Script

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

LOG_DIR="logs"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}  Banking System - Service Status${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Function to check service status
check_service() {
    local service_name=$1
    local port=$2
    local pid_file="${LOG_DIR}/${service_name}.pid"
    
    printf "%-25s" "$service_name:"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            # Check if port is listening
            if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
                echo -e "${GREEN}✓ RUNNING${NC} (PID: $pid, Port: $port)"
            else
                echo -e "${YELLOW}⚠ STARTING${NC} (PID: $pid, Port: $port not ready)"
            fi
        else
            echo -e "${RED}✗ STOPPED${NC} (PID file exists but process not found)"
        fi
    else
        # Check if something is on the port
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
            echo -e "${YELLOW}⚠ UNKNOWN${NC} (Port $port in use, but not managed by scripts)"
        else
            echo -e "${RED}✗ STOPPED${NC}"
        fi
    fi
}

# Check all services
check_service "Eureka Server" 8761
check_service "API Gateway" 8080
check_service "Auth Service" 8081
check_service "Register Service" 8082
check_service "Account Service" 8083
check_service "Transaction Service" 8084
check_service "Deposit Service" 8085
check_service "Transfer Service" 8086

echo ""
echo -e "${BLUE}Quick Links:${NC}"
echo -e "  Eureka Dashboard: ${GREEN}http://localhost:8761${NC}"
echo -e "  API Gateway:      ${GREEN}http://localhost:8080${NC}"
echo ""

# Check Eureka registration
if lsof -Pi :8761 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${BLUE}Checking Eureka registrations...${NC}"
    echo -e "${YELLOW}(This may take a moment)${NC}"
    echo ""
    
    # Try to get registered services from Eureka
    response=$(curl -s http://localhost:8761/eureka/apps 2>/dev/null || echo "")
    
    if [ ! -z "$response" ]; then
        echo -e "${GREEN}Services registered with Eureka:${NC}"
        echo "$response" | grep -o '<name>[^<]*</name>' | sed 's/<name>//;s/<\/name>//' | sed 's/^/  - /' || echo -e "${YELLOW}  No services registered yet${NC}"
    else
        echo -e "${YELLOW}Could not fetch Eureka registration info${NC}"
    fi
else
    echo -e "${YELLOW}Eureka Server is not running${NC}"
fi

echo ""
