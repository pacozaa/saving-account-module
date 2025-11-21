#!/bin/bash

# Start all microservices using Docker Compose
echo "Starting all microservices with Docker Compose..."

docker-compose up -d

echo ""
echo "Services are starting up. Please wait 1-2 minutes for all services to be ready."
echo ""
echo "Service URLs:"
echo "  Eureka Server:  http://localhost:8761"
echo "  API Gateway:    http://localhost:8080"
echo "  Auth Service:   http://localhost:8081"
echo "  Register:       http://localhost:8082"
echo "  Account:        http://localhost:8083"
echo "  Transaction:    http://localhost:8084"
echo "  Deposit:        http://localhost:8085"
echo "  Transfer:       http://localhost:8086"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop all services: docker-compose down"
