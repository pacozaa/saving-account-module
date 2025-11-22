#!/bin/bash

# Build script for Docker images
echo "Building JAR files for all microservices..."

# Build all Maven modules
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven build failed!"
    exit 1
fi

echo ""
echo "Building Docker images for all microservices..."

# Build all services
docker compose build

echo ""
echo "Docker images built successfully!"
echo ""
echo "To start all services, run:"
echo "  docker compose up -d"
echo ""
echo "To view logs:"
echo "  docker compose logs -f"
echo ""
echo "To stop all services:"
echo "  docker compose down"