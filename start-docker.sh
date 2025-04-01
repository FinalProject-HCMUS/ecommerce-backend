#!/bin/bash

# Enable error handling
set -e

# Log file
LOG_FILE="start-docker.log"

# Simple logging functions without colors
info() {
  echo "[INFO] $1"
}

warn() {
  echo "[WARNING] $1"
}

error() {
  echo "[ERROR] $1"
}

# Redirect stdout and stderr to the log file
exec > >(tee -i $LOG_FILE)
exec 2>&1

# Check if .env file exists
if [ ! -f .env ]; then
  error ".env file not found. Creating default .env file..."
  cat > .env << EOF
# PostgreSQL Configuration
POSTGRES_DB=ecommerce
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

# Spring Boot Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_HIBERNATE_DDL_AUTO=update
EOF
  info "Default .env file created"
fi

# Stop any running containers for this project
info "Stopping any running containers..."
docker-compose down

# Optional: Clean up volumes if the -c or --clean flag is provided
if [[ "$1" == "-c" || "$1" == "--clean" ]]; then
  warn "Removing PostgreSQL volume..."
  docker volume rm ecommerce-backend_postgres-data || true
  
  warn "Pruning unused volumes..."
  docker volume prune -f
fi

# Build the services
info "Building services..."
docker-compose build

# Start the services in detached mode
info "Starting services..."
docker-compose up -d

# Check the exit status of the last command
if [ $? -ne 0 ]; then
  error "An error occurred. Check the log file for details: $LOG_FILE"
  exit 1
else
  info "Services started successfully"
  info "E-commerce application is running at http://localhost:${SERVER_PORT:-8080}"
  info "PostgreSQL is available at localhost:${POSTGRES_PORT:-5432}"
fi

# Display container status
info "Container status:"
docker-compose ps