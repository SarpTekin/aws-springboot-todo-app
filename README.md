# AWS Spring Boot Todo App

A microservices-based todo application with a Spring Boot backend and Kotlin frontend.

## Project Structure

```
aws-springboot-todo-app/
├── backend/
│   ├── user-service/          # User management and authentication service
│   └── task-service/          # Task management service
├── ui-kotlin/                 # Kotlin UI frontend (to be implemented)
└── README.md
```

## Services

### User Service
- **Port:** 8081
- **Database:** PostgreSQL (userdb)
- **Features:**
  - User registration
  - JWT-based authentication
  - Password hashing with BCrypt

### Task Service
- **Port:** 8082
- **Database:** PostgreSQL (taskdb)
- **Features:**
  - Task CRUD operations
  - Task filtering by user
  - Task status management

## Getting Started

### Prerequisites
- **Java 17+** (required for Spring Boot 3.3.5)
- **Maven 3.9+** (or use the Maven Wrapper included: `./mvnw`)
- **PostgreSQL 16+** (must be installed and running locally)

### Database Setup

1. **Ensure PostgreSQL is installed and running:**
   ```bash
   # Check if PostgreSQL is running
   psql --version
   ```

2. **Create the databases:**
   ```bash
   # Connect to PostgreSQL
   psql postgres
   
   # Create databases
   CREATE DATABASE userdb;
   CREATE DATABASE taskdb;
   \q
   ```

3. **Update database credentials in `application.properties`:**
   - User Service: `backend/user-service/src/main/resources/application.properties`
   - Task Service: `backend/task-service/src/main/resources/application.properties`
   
   Update these lines:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Running the Services

**Terminal 1 - User Service:**
```bash
cd backend/user-service
./mvnw spring-boot:run
```
User Service will run on: `http://localhost:8081`

**Terminal 2 - Task Service:**
```bash
cd backend/task-service
./mvnw spring-boot:run
```
Task Service will run on: `http://localhost:8082`

### Building the Services

```bash
# Build user-service
cd backend/user-service
./mvnw clean package

# Build task-service
cd backend/task-service
./mvnw clean package
```

## API Endpoints

### User Service
- `POST /api/users` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/users/{id}` - Get user by ID

### Task Service
- `GET /api/tasks?userId={id}` - Get all tasks (optionally filtered by userId)
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update a task
- `DELETE /api/tasks/{id}` - Delete a task

## Tech Stack

- **Backend:** Spring Boot 3.3.5, Spring Security, Spring Data JPA
- **Database:** PostgreSQL 16 (local installation)
- **Build Tool:** Maven 3.9+
- **Authentication:** JWT (JSON Web Tokens)
- **API Documentation:** Swagger/OpenAPI
- **Frontend:** Kotlin (to be implemented in `ui-kotlin/`)

## Development Notes

- Each service has its own PostgreSQL database (`userdb` and `taskdb`)
- Services communicate via REST APIs
- JWT authentication is implemented for secure API access
- Both services are secured with JWT tokens (issued by user-service)
- Services run locally using Maven Spring Boot plugin

## API Documentation

- **User Service Swagger UI:** `http://localhost:8081/swagger-ui/index.html`
- **Task Service Swagger UI:** `http://localhost:8082/swagger-ui/index.html`

## Testing

See `TESTING_GUIDE.md` for comprehensive testing instructions.

