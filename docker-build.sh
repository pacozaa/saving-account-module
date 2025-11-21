#!/bin/bash

# Build script for Docker images
echo "Building Docker images for all microservices..."

# Build all services
docker-compose build

echo "Docker images built successfully!"
echo ""
echo "To start all services, run:"
echo "  docker-compose up -d"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop all services:"
echo "  docker-compose down"
