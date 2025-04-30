#!/bin/bash
set -e

# First connect to the default postgres database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    SELECT 'CREATE DATABASE ecommerce' 
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce');
EOSQL

echo "Database 'ecommerce' created or already exists."