# Local Setup Guide

**Quick guide to run the backend services locally without Docker.**

---

## Prerequisites

1. **Java 17+**
   ```bash
   java -version
   ```

2. **Maven 3.9+** (or use Maven Wrapper included: `./mvnw`)
   ```bash
   ./mvnw --version
   ```

3. **PostgreSQL 16+** installed and running
   ```bash
   psql --version
   ```

---

## Setup Steps

### 1. Create Databases

```bash
# Connect to PostgreSQL
psql postgres

# Create databases
CREATE DATABASE userdb;
CREATE DATABASE taskdb;

# Exit
\q
```

### 2. Update Database Credentials

**User Service:**
Edit `backend/user-service/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Task Service:**
Edit `backend/task-service/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Start Services

**Terminal 1 - User Service:**
```bash
cd backend/user-service
./mvnw spring-boot:run
```

**Terminal 2 - Task Service:**
```bash
cd backend/task-service
./mvnw spring-boot:run
```

### 4. Verify Services Are Running

- **User Service:** `http://localhost:8081`
  - Swagger UI: `http://localhost:8081/swagger-ui/index.html`
  - Health Check: `http://localhost:8081/actuator/health`

- **Task Service:** `http://localhost:8082`
  - Swagger UI: `http://localhost:8082/swagger-ui/index.html`
  - Health Check: `http://localhost:8082/actuator/health`

---

## Quick Test

```bash
# Test user-service
curl http://localhost:8081/api/users/check-username?username=test

# Expected: {"available":true}
```

---

## Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8081
lsof -i :8082

# Kill the process if needed
kill -9 <PID>
```

### Database Connection Error
```bash
# Check PostgreSQL is running
psql -h localhost -U your_username -d postgres

# Verify database exists
psql -h localhost -U your_username -d userdb
psql -h localhost -U your_username -d taskdb
```

### Maven Build Errors
```bash
# Clean and rebuild
cd backend/user-service
./mvnw clean compile

cd ../task-service
./mvnw clean compile
```

---

## Development Workflow

1. **Make code changes**
2. **Services auto-reload** (if using Spring Boot DevTools) or restart manually:
   ```bash
   # Stop: Ctrl+C
   # Restart: ./mvnw spring-boot:run
   ```
3. **Test via Swagger UI** or your API client

---

**That's it! You're ready to develop locally without Docker.**

