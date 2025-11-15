# AWS Spring Boot Todo App

A microservices-based todo application with a Spring Boot backend and Kotlin frontend.

## Project Structure

```
aws-springboot-todo-app/
├── backend/
│   ├── user-service/          # User management and authentication service
│   ├── task-service/          # Task management service
│   └── docker-compose.yml     # Docker Compose configuration for all services
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
- Docker and Docker Compose
- Java 21+ (for local development)
- Maven 3.9+

### Running with Docker Compose

1. Navigate to the backend directory:
```bash
cd backend
```

2. Start all services:
```bash
docker-compose up --build
```

This will start:
- `user-service` on port 8081
- `task-service` on port 8082
- PostgreSQL databases for each service

### Running Locally (Development)

1. Start the databases:
```bash
cd backend
docker-compose up user-db task-db
```

2. Run each service separately:
```bash
# Terminal 1 - User Service
cd backend/user-service
./mvnw spring-boot:run

# Terminal 2 - Task Service
cd backend/task-service
./mvnw spring-boot:run
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

- **Backend:** Spring Boot 3.5.7, Spring Security, Spring Data JPA
- **Database:** PostgreSQL 16
- **Build Tool:** Maven
- **Containerization:** Docker
- **Frontend:** Kotlin (to be implemented in `ui-kotlin/`)

## Development Notes

- Each service has its own PostgreSQL database
- Services communicate via REST APIs
- JWT authentication is implemented for secure API access
- Docker Compose is used for local development and testing

