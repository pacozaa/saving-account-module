#!/bin/bash

# Banking System - Service Management Script
# This script allows you to start/stop individual services interactively

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# Service definitions: "Display Name|Directory|Port|Wait Time"
declare -a SERVICES=(
    "Eureka Server|eureka-server|8761|1"
    "API Gateway|api-gateway|8080|1"
    "Auth Service|auth-service|8081|1"
    "Register Service|register-service|8082|1"
    "Account Service|account-service|8083|1"
    "Transaction Service|transaction-service|8084|1"
    "Deposit Service|deposit-service|8085|1"
    "Transfer Service|transfer-service|8086|1"
)

# Function to display menu
show_main_menu() {
    echo ""
    echo -e "${BLUE}================================================${NC}"
    echo -e "${CYAN}  Banking System - Service Manager${NC}"
    echo -e "${BLUE}================================================${NC}"
    echo ""
    echo -e "${YELLOW}Select an action:${NC}"
    echo -e "  ${GREEN}1${NC} - Start a service"
    echo -e "  ${GREEN}2${NC} - Stop a service"
    echo -e "  ${GREEN}3${NC} - Restart a service"
    echo -e "  ${GREEN}4${NC} - View service status"
    echo -e "  ${GREEN}5${NC} - View service logs"
    echo -e "  ${GREEN}6${NC} - Exit"
    echo ""
}

# Function to display service list
show_service_menu() {
    echo ""
    echo -e "${YELLOW}Select a service:${NC}"
    local i=1
    for service in "${SERVICES[@]}"; do
        IFS='|' read -r display_name dir port wait_time <<< "$service"
        printf "  ${GREEN}%-2d${NC} - %s\n" "$i" "$display_name"
        i=$((i + 1))
    done
    echo -e "  ${GREEN}0${NC} - Back to main menu"
    echo ""
}

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local wait_time=$4
    
    # Check if service is already running
    local pid_file="${LOG_DIR}/${service_name}.pid"
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${YELLOW}✗ ${service_name} is already running (PID: $pid)${NC}"
            return 1
        fi
    fi
    
    # Check if port is in use
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}✗ Port $port is already in use!${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Starting ${service_name}...${NC}"
    
    # Build the service first
    echo -e "${BLUE}  Building ${service_name}...${NC}"
    cd "$service_dir"
    mvn clean package -DskipTests > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Build failed for ${service_name}${NC}"
        cd ..
        return 1
    fi
    
    # Start the service
    mvn spring-boot:run > "../${LOG_DIR}/${service_name}.log" 2>&1 &
    local pid=$!
    echo $pid > "../${LOG_DIR}/${service_name}.pid"
    cd ..
    
    echo -e "${GREEN}✓ ${service_name} started (PID: $pid)${NC}"
    
    if [ $wait_time -gt 0 ]; then
        echo -e "${BLUE}  Waiting ${wait_time}s for ${service_name} to initialize...${NC}"
        sleep $wait_time
    fi
    
    return 0
}

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="${LOG_DIR}/${service_name}.pid"
    
    if [ ! -f "$pid_file" ]; then
        echo -e "${YELLOW}✗ ${service_name} is not running (no PID file)${NC}"
        return 1
    fi
    
    local pid=$(cat "$pid_file")
    
    if ! ps -p $pid > /dev/null 2>&1; then
        echo -e "${YELLOW}✗ ${service_name} is not running (PID: $pid not found)${NC}"
        rm "$pid_file"
        return 1
    fi
    
    echo -e "${YELLOW}Stopping ${service_name} (PID: $pid)...${NC}"
    kill $pid
    
    # Wait for process to stop gracefully
    local count=0
    while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    # Force kill if necessary
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${YELLOW}  Force killing ${service_name}...${NC}"
        kill -9 $pid
    fi
    
    rm "$pid_file"
    echo -e "${GREEN}✓ ${service_name} stopped${NC}"
    return 0
}

# Function to check service status
check_service_status() {
    local service_name=$1
    local port=$2
    local pid_file="${LOG_DIR}/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
                echo -e "${GREEN}✓ RUNNING${NC} (PID: $pid, Port: $port)"
            else
                echo -e "${YELLOW}⚠ STARTING${NC} (PID: $pid, Port: $port not ready)"
            fi
        else
            echo -e "${RED}✗ STOPPED${NC} (PID file exists but process not found)"
        fi
    else
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
            echo -e "${YELLOW}⚠ UNKNOWN${NC} (Port $port in use, not managed by scripts)"
        else
            echo -e "${RED}✗ STOPPED${NC}"
        fi
    fi
}

# Function to handle service selection
handle_service_selection() {
    local action=$1
    
    show_service_menu
    read -p "Enter your choice: " choice
    
    if [ "$choice" == "0" ]; then
        return 0
    fi
    
    if ! [[ "$choice" =~ ^[0-9]+$ ]] || [ "$choice" -lt 1 ] || [ "$choice" -gt ${#SERVICES[@]} ]; then
        echo -e "${RED}Invalid choice!${NC}"
        return 1
    fi
    
    # Get selected service (array is 0-indexed)
    local service_index=$((choice - 1))
    local service="${SERVICES[$service_index]}"
    
    IFS='|' read -r display_name dir port wait_time <<< "$service"
    
    echo ""
    
    case $action in
        1)
            start_service "$display_name" "$dir" "$port" "$wait_time"
            ;;
        2)
            stop_service "$display_name"
            ;;
        3)
            stop_service "$display_name"
            sleep 2
            start_service "$display_name" "$dir" "$port" "$wait_time"
            ;;
        5)
            view_logs "$display_name"
            ;;
    esac
    
    read -p "Press Enter to continue..."
}

# Function to view all services status
view_all_status() {
    echo ""
    echo -e "${BLUE}================================================${NC}"
    echo -e "${CYAN}  Service Status${NC}"
    echo -e "${BLUE}================================================${NC}"
    echo ""
    
    for service in "${SERVICES[@]}"; do
        IFS='|' read -r display_name dir port wait_time <<< "$service"
        printf "%-25s" "$display_name:"
        check_service_status "$display_name" "$port"
    done
    
    echo ""
    read -p "Press Enter to continue..."
}

# Function to view service logs
view_logs() {
    local service_name=$1
    local log_file="${LOG_DIR}/${service_name}.log"
    
    if [ ! -f "$log_file" ]; then
        echo -e "${RED}✗ Log file not found: $log_file${NC}"
        return 1
    fi
    
    echo ""
    echo -e "${BLUE}Last 50 lines of ${service_name} log:${NC}"
    echo -e "${BLUE}================================================${NC}"
    tail -n 50 "$log_file"
    echo -e "${BLUE}================================================${NC}"
}

# Function to view logs interactively
view_logs_interactive() {
    show_service_menu
    read -p "Enter your choice: " choice
    
    if [ "$choice" == "0" ]; then
        return 0
    fi
    
    if ! [[ "$choice" =~ ^[0-9]+$ ]] || [ "$choice" -lt 1 ] || [ "$choice" -gt ${#SERVICES[@]} ]; then
        echo -e "${RED}Invalid choice!${NC}"
        return 1
    fi
    
    local service_index=$((choice - 1))
    local service="${SERVICES[$service_index]}"
    
    IFS='|' read -r display_name dir port wait_time <<< "$service"
    
    view_logs "$display_name"
    read -p "Press Enter to continue..."
}

# Main loop
main() {
    while true; do
        show_main_menu
        read -p "Enter your choice: " choice
        
        case $choice in
            1)
                handle_service_selection 1
                ;;
            2)
                handle_service_selection 2
                ;;
            3)
                handle_service_selection 3
                ;;
            4)
                view_all_status
                ;;
            5)
                view_logs_interactive
                ;;
            6)
                echo -e "${BLUE}Exiting Service Manager...${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}Invalid choice! Please try again.${NC}"
                read -p "Press Enter to continue..."
                ;;
        esac
    done
}

# Run main function
main
