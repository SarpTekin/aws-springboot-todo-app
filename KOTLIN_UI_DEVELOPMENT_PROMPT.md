# Kotlin UI Development Prompt - Complete Project Guide

**Project:** AWS Spring Boot Todo Application  
**Backend Status:** ✅ Production Ready - 100% Complete  
**Date:** November 18, 2025

---

## 🎯 Project Overview

You are developing a **Kotlin Android UI** for a Spring Boot microservices backend. The backend is **fully complete and production-ready** with JWT authentication, user management, and task management.

### Architecture:
- **Backend:** Spring Boot microservices (user-service + task-service)
- **Database:** PostgreSQL (local development)
- **Authentication:** JWT (JSON Web Tokens)
- **Frontend:** Kotlin Android (Jetpack Compose recommended)
- **API Communication:** RESTful JSON APIs

---

## 📱 Backend Services

### Service URLs (Local Development):
- **User Service:** `http://localhost:8081`
- **Task Service:** `http://localhost:8082`
- **Swagger UI (User Service):** `http://localhost:8081/swagger-ui.html`
- **Swagger UI (Task Service):** `http://localhost:8082/swagger-ui.html`

### Production URLs (Future - AWS):
- Will be provided after AWS deployment
- Currently: Local development only

---

## 🔐 Authentication Flow

### 1. **User Registration**
```
POST http://localhost:8081/api/users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 2. **User Login (Get JWT Token)**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzYzNDI3NDM2LCJleHAiOjE3NjM0MzEwMzZ9...",
  "userId": 1,
  "username": "john_doe"
}
```

**⚠️ IMPORTANT:** Save the `token` from this response! You'll need it for all authenticated requests.

### 3. **Using JWT Token**
For all protected endpoints, include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

**Token Expiration:** 1 hour (3600 seconds)
**Token Format:** JWT (JSON Web Token)

---

## 📋 Complete API Endpoints

### **User Service** (`http://localhost:8081`)

#### **Public Endpoints (No Authentication Required):**

**1. Register User**
```
POST /api/users
Content-Type: application/json

Request Body:
{
  "username": "string" (required, 3-50 chars),
  "email": "string" (required, valid email),
  "password": "string" (required, min 6 chars),
  "firstName": "string" (optional),
  "lastName": "string" (optional)
}

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}

Error Responses:
- 400 Bad Request: Validation errors
- 400 Bad Request: Username already exists
- 400 Bad Request: Email already exists
```

**2. Login**
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "string" (required),
  "password": "string" (required)
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "john_doe"
}

Error Responses:
- 400 Bad Request: Username or password is required
- 401 Unauthorized: Invalid credentials
```

**3. Check Username Availability**
```
GET /api/users/check-username?username=john_doe

Response (200 OK):
{
  "available": true  // or false
}
```

**4. Check Email Availability**
```
GET /api/users/check-email?email=john@example.com

Response (200 OK):
{
  "available": true  // or false
}
```

#### **Protected Endpoints (JWT Token Required):**

**5. Get Current User Profile**
```
GET /api/users/me
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 400 Bad Request: User not found
```

**6. Update Profile**
```
PUT /api/users/me
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "firstName": "John" (optional),
  "lastName": "Doe" (optional)
}

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John Updated",
  "lastName": "Doe Updated"
}
```

**7. Change Password**
```
PATCH /api/users/me/password
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "oldPassword": "string" (required),
  "newPassword": "string" (required, min 6 chars)
}

Response (204 No Content)

Error Responses:
- 400 Bad Request: Validation errors
- 403 Forbidden: Old password incorrect
```

**8. Delete Account**
```
DELETE /api/users/me
Authorization: Bearer <token>

Response (204 No Content)

Error Responses:
- 401 Unauthorized: Missing or invalid token
```

**9. Get User by ID**
```
GET /api/users/{id}
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: Cannot access other users (same-user only)
- 400 Bad Request: User not found
```

---

### **Task Service** (`http://localhost:8082`)

**⚠️ ALL endpoints require JWT authentication!**

**1. Create Task**
```
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "title": "string" (required, 3-200 chars),
  "description": "string" (optional, max 1000 chars),
  "status": "PENDING" | "IN_PROGRESS" | "COMPLETED" (optional, defaults to PENDING)
}

Note: userId is automatically set from JWT token - don't include it!

Response (200 OK):
{
  "id": 1,
  "title": "Complete project",
  "description": "Finish the todo app",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-11-18T10:30:00",
  "updatedAt": "2025-11-18T10:30:00"
}

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 400 Bad Request: Validation errors
```

**2. Get All Tasks (Current User Only)**
```
GET /api/tasks
Authorization: Bearer <token>

Response (200 OK):
[
  {
    "id": 1,
    "title": "Complete project",
    "description": "Finish the todo app",
    "status": "PENDING",
    "userId": 1,
    "createdAt": "2025-11-18T10:30:00",
    "updatedAt": "2025-11-18T10:30:00"
  },
  {
    "id": 2,
    "title": "Review code",
    "description": "Code review session",
    "status": "IN_PROGRESS",
    "userId": 1,
    "createdAt": "2025-11-18T11:00:00",
    "updatedAt": "2025-11-18T11:15:00"
  }
]

Note: Only returns tasks for the authenticated user (automatic filtering)

Error Responses:
- 401 Unauthorized: Missing or invalid token
```

**3. Get Task by ID**
```
GET /api/tasks/{id}
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "title": "Complete project",
  "description": "Finish the todo app",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-11-18T10:30:00",
  "updatedAt": "2025-11-18T10:30:00"
}

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: Cannot access other user's tasks
- 400 Bad Request: Task not found
```

**4. Update Task**
```
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "title": "string" (required, 3-200 chars),
  "description": "string" (optional, max 1000 chars),
  "status": "PENDING" | "IN_PROGRESS" | "COMPLETED" (optional)
}

Response (200 OK):
{
  "id": 1,
  "title": "Updated title",
  "description": "Updated description",
  "status": "COMPLETED",
  "userId": 1,
  "createdAt": "2025-11-18T10:30:00",
  "updatedAt": "2025-11-18T12:00:00"
}

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: Cannot modify other user's tasks
- 400 Bad Request: Validation errors or task not found
```

**5. Delete Task**
```
DELETE /api/tasks/{id}
Authorization: Bearer <token>

Response (204 No Content)

Error Responses:
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: Cannot delete other user's tasks
- 400 Bad Request: Task not found
```

---

## 📊 Data Models

### **User Model:**
```kotlin
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
)
// Note: password is never returned in responses
```

### **Task Model:**
```kotlin
data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val userId: Long,
    val createdAt: String,  // ISO 8601 format
    val updatedAt: String    // ISO 8601 format
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
```

### **Login Request:**
```kotlin
data class LoginRequest(
    val username: String,
    val password: String
)
```

### **Login Response:**
```kotlin
data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String
)
```

### **Register Request:**
```kotlin
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null
)
```

---

## 🔒 Security & Authentication

### **JWT Token Management:**
1. **Store token securely:** Use Android's EncryptedSharedPreferences or KeyStore
2. **Token expiration:** Tokens expire after 1 hour - implement refresh logic
3. **Token format:** `Bearer <token>` in Authorization header
4. **Auto-logout:** If token expires, redirect to login screen

### **Error Handling:**
- **401 Unauthorized:** Token missing/invalid → Redirect to login
- **403 Forbidden:** User trying to access other's data → Show error message
- **400 Bad Request:** Validation errors → Show field-specific errors
- **500 Server Error:** Network/server issues → Show generic error

### **User Isolation:**
- Users can ONLY see/modify their own tasks
- Backend automatically filters tasks by authenticated user
- No need to pass userId in requests (extracted from JWT)

---

## 🎨 UI Requirements

### **Screens Needed:**

1. **Authentication Screens:**
   - Login Screen
   - Registration Screen
   - Forgot Password (optional - not implemented in backend yet)

2. **Main Screens:**
   - Task List Screen (shows all user's tasks)
   - Task Detail Screen
   - Create/Edit Task Screen
   - Profile Screen
   - Settings Screen

3. **Components:**
   - Task Item (reusable card/list item)
   - Status Badge (PENDING/IN_PROGRESS/COMPLETED)
   - Loading Indicators
   - Error Messages
   - Empty State (no tasks)

### **Navigation Flow:**
```
Splash Screen
  ↓
Login Screen (if not authenticated)
  ↓
Registration Screen (optional)
  ↓
Main Screen (if authenticated)
  ├── Task List
  ├── Task Detail
  ├── Create/Edit Task
  ├── Profile
  └── Settings
```

---

## 🛠️ Implementation Guidelines

### **Network Layer:**
- Use **Retrofit** for API calls
- Use **OkHttp** for HTTP client
- Add **Interceptor** for JWT token injection
- Handle network errors gracefully

### **Example Retrofit Setup:**
```kotlin
// Add JWT token to all requests
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenManager.getToken()}")
            .build()
        return chain.proceed(request)
    }
}
```

### **State Management:**
- Use **ViewModel** for business logic
- Use **StateFlow/Flow** for reactive data
- Use **Repository Pattern** for data access
- Cache user data and tasks locally (Room database recommended)

### **Dependency Injection:**
- Use **Hilt** or **Koin** for DI
- Inject API services, repositories, ViewModels

### **UI Framework:**
- **Recommended:** Jetpack Compose (modern, declarative)
- **Alternative:** XML Layouts (traditional)

---

## 📝 API Testing

### **Test the Backend:**
1. Start backend services locally:
   ```bash
   # Terminal 1
   cd backend/user-service
   ./mvnw spring-boot:run
   
   # Terminal 2
   cd backend/task-service
   ./mvnw spring-boot:run
   ```

2. Use Swagger UI:
   - User Service: http://localhost:8081/swagger-ui.html
   - Task Service: http://localhost:8082/swagger-ui.html

3. Test with Postman/curl:
   - Register user
   - Login to get token
   - Use token for protected endpoints

### **Test Script Available:**
```bash
./run_tests.sh
```
This script tests the complete authentication and task flow.

---

## ✅ Backend Status

### **Completed Features:**
- ✅ User registration and authentication
- ✅ JWT token generation and validation
- ✅ User profile management
- ✅ Task CRUD operations
- ✅ User isolation (users only see their tasks)
- ✅ Authorization checks
- ✅ Input validation
- ✅ Error handling
- ✅ Swagger/OpenAPI documentation
- ✅ Comprehensive testing

### **Backend is 100% Ready for UI Integration!**

---

## 🚨 Important Notes

1. **CORS:** Not needed for Android (only for web browsers)
2. **Base URLs:** Use `http://10.0.2.2:8081` and `http://10.0.2.2:8082` for Android Emulator
   - Use `http://localhost:8081` for physical device (if on same network)
3. **Token Storage:** Store JWT token securely (EncryptedSharedPreferences)
4. **Error Handling:** Always handle 401 errors (redirect to login)
5. **Offline Support:** Consider caching tasks locally (Room database)
6. **Network Timeout:** Set appropriate timeouts (30 seconds recommended)

---

## 📚 Additional Resources

### **Backend Documentation:**
- `API_DOCUMENTATION_FOR_UI_TEAM.md` - Complete API reference
- `TESTING_GUIDE.md` - How to test all endpoints
- `UI_BACKEND_INTEGRATION.md` - Integration guide
- `STATUS_REPORT.md` - Current backend status

### **Backend Code:**
- GitHub: https://github.com/SarpTekin/aws-springboot-todo-app
- Branch: main
- Services: `backend/user-service` and `backend/task-service`

---

## 🎯 Development Checklist

### **Phase 1: Setup**
- [ ] Create Kotlin Android project
- [ ] Add dependencies (Retrofit, OkHttp, Hilt/Koin, Room, Compose)
- [ ] Set up network layer
- [ ] Create data models (User, Task, etc.)
- [ ] Set up dependency injection

### **Phase 2: Authentication**
- [ ] Create Login Screen
- [ ] Create Registration Screen
- [ ] Implement JWT token storage
- [ ] Implement auto-login (if token exists)
- [ ] Handle token expiration

### **Phase 3: Task Management**
- [ ] Create Task List Screen
- [ ] Create Task Detail Screen
- [ ] Create Create/Edit Task Screen
- [ ] Implement task filtering by status
- [ ] Implement task search (optional)

### **Phase 4: User Profile**
- [ ] Create Profile Screen
- [ ] Implement profile update
- [ ] Implement password change
- [ ] Implement logout

### **Phase 5: Polish**
- [ ] Add loading states
- [ ] Add error handling
- [ ] Add empty states
- [ ] Add pull-to-refresh
- [ ] Add offline support (optional)

---

## 💡 Example API Calls (Kotlin)

### **Login Example:**
```kotlin
@POST("/api/auth/login")
suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

// Usage:
val response = apiService.login(LoginRequest("john_doe", "password123"))
if (response.isSuccessful) {
    val token = response.body()?.token
    tokenManager.saveToken(token)
}
```

### **Get Tasks Example:**
```kotlin
@GET("/api/tasks")
suspend fun getTasks(
    @Header("Authorization") token: String
): Response<List<Task>>

// Usage:
val token = "Bearer ${tokenManager.getToken()}"
val response = apiService.getTasks(token)
val tasks = response.body() ?: emptyList()
```

### **Create Task Example:**
```kotlin
@POST("/api/tasks")
suspend fun createTask(
    @Header("Authorization") token: String,
    @Body request: CreateTaskRequest
): Response<Task>

// Usage:
val request = CreateTaskRequest(
    title = "New Task",
    description = "Task description",
    status = TaskStatus.PENDING
)
val response = apiService.createTask("Bearer $token", request)
```

---

## 🎉 Ready to Start!

The backend is **100% complete and production-ready**. You can start developing the Kotlin UI immediately. All APIs are tested and working.

**Key Points to Remember:**
1. Always include JWT token in Authorization header for protected endpoints
2. Handle 401 errors (token expired/invalid) by redirecting to login
3. Users can only see/modify their own tasks (automatic filtering)
4. Use `http://10.0.2.2:8081` for Android Emulator
5. Store JWT token securely
6. Test with Swagger UI first to understand API behavior

**Good luck with the UI development! 🚀**

