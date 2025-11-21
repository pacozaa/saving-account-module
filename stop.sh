#!/bin/bash

# Banking System - Stop All Services Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

LOG_DIR="logs"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}  Banking System - Stopping Services${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="${LOG_DIR}/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${YELLOW}Stopping ${service_name} (PID: $pid)...${NC}"
            kill $pid
            # Wait for process to stop
            local count=0
            while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done
            if ps -p $pid > /dev/null 2>&1; then
                echo -e "${RED}  Force killing ${service_name}...${NC}"
                kill -9 $pid
            fi
            echo -e "${GREEN}✓ ${service_name} stopped${NC}"
        else
            echo -e "${YELLOW}${service_name} is not running${NC}"
        fi
        rm "$pid_file"
    else
        echo -e "${YELLOW}${service_name} PID file not found${NC}"
    fi
}

# Stop services in reverse order
stop_service "Transfer Service"
stop_service "Deposit Service"
stop_service "Auth Service"
stop_service "Register Service"
stop_service "Transaction Service"
stop_service "Account Service"
stop_service "API Gateway"
stop_service "Eureka Server"

echo ""
echo -e "${GREEN}✓ All services stopped${NC}"
echo ""

# Optional: Clean up log files
read -p "Do you want to clean up log files? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf "$LOG_DIR"
    echo -e "${GREEN}✓ Log files cleaned${NC}"
fi
