# E-Commerce Backend

A Spring Boot based backend service for an e-commerce application. This project provides RESTful APIs for managing products, orders, users, and more.

## Table of Contents

- [Requirements](#requirements)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Development with Docker](#development-with-docker)
  - [Development with IDE](#development-with-ide)
- [Contributing](#contributing)

## Requirements

- Java 17+
- Maven 3.8+
- Docker (for containerized development)
- PostgreSQL 15+

## Tech Stack

- **Spring Boot 3.4.3**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data persistence
- **PostgreSQL**: Database
- **MapStruct**: Java bean mappings
- **JWT**: Token-based authentication
- **SpringDoc OpenAPI**: API documentation

## Project Structure

```
ecommerce-backend/
├── src/
│   ├── main/
│   │   ├── java/com/hcmus/ecommerce_backend/
│   │   │   ├── common/           # Common utilities, base models, and exceptions
│   │   │   ├── config/           # Application configuration
│   │   │   ├── [feature]/        # Feature packages (products, orders, users, etc.)
│   │   ├── resources/
│   │       ├── application.yml    # Main application configuration
│   ├── test/                      # Test files
├── docker-compose.yml             # Production Docker configuration
├── docker-compose.dev.yml         # Development Docker configuration
├── Dockerfile                     # Application container definition
├── .env.example                   # Example environment variables
└── .mvn/wrapper/                  # Maven wrapper
```

## Getting Started

### Development with Docker

The easiest way to get started is by using Docker, which sets up the PostgreSQL database and the Spring Boot application.

#### Prerequisites

- Docker and Docker Compose installed
- Git (to clone the repository)

#### Setup Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/FinalProject-HCMUS/ecommerce-backend.git
   cd ecommerce-backend
   ```

2. Create a `.env` file from the template:
   ```bash
   cp .env.example .env
   ```

3. Run with Docker:
   ```bash
   ./start-docker.sh
   ```
   This will build the application and start both the database and application containers.

4. Test database connectivity:
   ```bash
   curl http://localhost:8080/api/system/db-test
   ```

### Development with IDE

#### Prerequisites

- JDK 17+
- Maven
- An IDE like IntelliJ IDEA or Eclipse
- PostgreSQL database (can be run with Docker)

#### Setup Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/FinalProject-HCMUS/ecommerce-backend.git
   cd ecommerce-backend
   ```

2. Create a `.env` file from template:
   ```bash
   cp .env.example .env
   ```

3. Start only the PostgreSQL database with Docker:
   ```bash
   ./start-dev-db.sh
   ```

4. Import the project into your IDE:
   - **IntelliJ IDEA**: File → Open → select the project's `pom.xml`
   - **Eclipse**: File → Import → Maven → Existing Maven Projects

5. Run the application:
   - From IDE: Run `EcommerceBackendApplication.java` as a Java application
   - From command line: `./mvnw spring-boot:run`

6. Access the application at http://localhost:8080


## Contributing

1. Create a feature branch
2. Develop your feature or fix
3. Add appropriate tests
4. Ensure all tests pass with `./mvnw test`
5. Submit a pull request

---

## Troubleshooting
### Database Connection Issues

If you encounter database connection issues, make sure:
1. PostgreSQL container is running: `docker ps`
2. Environment variables are correctly loaded
3. Database service is accessible on localhost:5432
4. Credentials in `.env` match your database settings

You can test database connectivity directly:
```bash
curl http://localhost:8080/api/system/db-test
```

### Common Errors

- "Failed to obtain JDBC Connection" - Check database settings and ensure PostgreSQL is running
- Port conflicts - Check if ports 8080 or 5432 are already in use