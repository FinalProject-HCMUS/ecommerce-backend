#!/bin/bash

# Enable error handling
set -e

# Log file
LOG_FILE="start-dev-db.log"

# Function to display colored output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
  echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

# Redirect stdout and stderr to the log file
exec > >(tee -i $LOG_FILE)
exec 2>&1

# Check if .env file exists
if [ ! -f .env ]; then
  info "Using default PostgreSQL settings (no .env file found)"
else
  info "Using PostgreSQL settings from .env file"
fi

# Stop any running postgres container from this project
info "Stopping any running PostgreSQL container..."
docker-compose -f docker-compose.dev.yml down

# Optional: Clean up volumes if the -c or --clean flag is provided
if [[ "$1" == "-c" || "$1" == "--clean" ]]; then
  warn "Removing PostgreSQL volume..."
  docker volume rm ecommerce-backend_postgres-data || true
  info "PostgreSQL data has been cleaned."
fi

# Start the postgres container in detached mode
info "Starting PostgreSQL database..."
docker-compose -f docker-compose.dev.yml up -d

# Check the exit status of the last command
if [ $? -ne 0 ]; then
  error "An error occurred. Check the log file for details: $LOG_FILE"
  exit 1
else
  info "PostgreSQL started successfully"
  info "Database is available at localhost:${POSTGRES_PORT:-5432}"
  info "Database: ${POSTGRES_DB:-ecommerce}"
  info "Username: ${POSTGRES_USER:-postgres}"
  info "Password: ${POSTGRES_PASSWORD:-postgres}"
fi

# Display container status
info "Container status:"
docker-compose -f docker-compose.dev.yml ps