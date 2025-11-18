# API Documentation for Todo Application

**Version:** 1.0  
**Last Updated:** Current  
**Status:** ✅ All endpoints implemented and tested

---

## 📋 Table of Contents

1. [Base URLs](#base-urls)
2. [Authentication](#authentication)
3. [User Service APIs](#user-service-apis)
4. [Task Service APIs](#task-service-apis)
5. [Error Responses](#error-responses)
6. [Data Models](#data-models)
7. [Testing Instructions](#testing-instructions)

---

## 🌐 Base URLs

### Local Development
- **User Service:** `http://localhost:8081`
- **Task Service:** `http://localhost:8082`

### Production (AWS) - To be provided
- **API Gateway/ALB:** `https://your-api-endpoint.amazonaws.com`

**Note for Android Emulator:** Use `http://10.0.2.2:8081` instead of `localhost` to access your host machine.

---

## 🔐 Authentication

### Authentication Type
**JWT (JSON Web Token) Bearer Authentication**

### How It Works

1. **User registers** → `POST /api/users`
2. **User logs in** → `POST /api/auth/login`
3. **Server returns JWT token** in response
4. **Store token securely** in your app (SharedPreferences/EncryptedSharedPreferences)
5. **Include token in all protected requests:**
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Token Format
```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMsInN1YiI6InN0cmluZyIsImlhdCI6MTc2MzMxMjIyNCwiZXhwIjoxNzYzMzE1ODI0fQ...
```

### Token Lifetime
- **Expiration:** 1 hour (3600000 ms)
- **Action on expiration:** Redirect user to login screen

### Protected vs Public Endpoints

**Public Endpoints** (No token required):
- `POST /api/users` - Registration
- `POST /api/auth/login` - Login
- `GET /api/users/check-username` - Username availability
- `GET /api/users/check-email` - Email availability

**Protected Endpoints** (Token required):
- All `/api/users/me/**` endpoints
- All `/api/tasks/**` endpoints

---

## 👤 User Service APIs

**Base URL:** `http://localhost:8081` (local) or `https://your-api-endpoint.amazonaws.com` (production)

---

### 1. Register New User

**Endpoint:** `POST /api/users`

**Authentication:** ❌ Not required (Public)

**Request Body:**
```json
{
  "username": "johndoe",           // Required, 3-50 characters
  "email": "john@example.com",     // Required, valid email format
  "password": "password123",       // Required, min 8 characters
  "firstName": "John",             // Optional
  "lastName": "Doe"                // Optional
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2025-01-16T10:30:00",
  "updatedAt": "2025-01-16T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors
  ```json
  {
    "error": "Validation failed",
    "details": [
      {
        "field": "username",
        "message": "Username is required"
      }
    ]
  }
  ```
- `409 Conflict` - Username or email already exists
  ```json
  {
    "error": "Username already exists"
  }
  ```

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Authentication:** ❌ Not required (Public)

**Request Body:**
```json
{
  "username": "johndoe",        // Required
  "password": "password123"     // Required
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "johndoe"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
  ```json
  {
    "error": "Invalid username or password"
  }
  ```
- `400 Bad Request` - Validation errors
  ```json
  {
    "error": "Username is required"
  }
  ```

**⚠️ Important:** Store the `token` and `userId` from this response for authenticated requests.

**Example Request (cURL):**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

---

### 3. Check Username Availability

**Endpoint:** `GET /api/users/check-username?username={username}`

**Authentication:** ❌ Not required (Public)

**Query Parameters:**
- `username` (required) - Username to check

**Response:** `200 OK`
```json
{
  "available": true
}
```

**Response if taken:**
```json
{
  "available": false
}
```

**Example Request:**
```bash
curl "http://localhost:8081/api/users/check-username?username=johndoe"
```

**Use Case:** Call this endpoint while user is typing username in registration form for real-time feedback.

---

### 4. Check Email Availability

**Endpoint:** `GET /api/users/check-email?email={email}`

**Authentication:** ❌ Not required (Public)

**Query Parameters:**
- `email` (required) - Email to check

**Response:** `200 OK`
```json
{
  "available": true
}
```

**Example Request:**
```bash
curl "http://localhost:8081/api/users/check-email?email=john@example.com"
```

---

### 5. Get Current User Profile

**Endpoint:** `GET /api/users/me`

**Authentication:** ✅ Required (Protected)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
  ```json
  {
    "error": "Unauthorized"
  }
  ```
- `404 Not Found` - User not found

**Example Request:**
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### 6. Update Current User Profile

**Endpoint:** `PUT /api/users/me`

**Authentication:** ✅ Required (Protected)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Jane",      // Optional, max 50 characters
  "lastName": "Smith"       // Optional, max 50 characters
}
```

**Note:** Both fields are optional. Only include fields you want to update.

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `400 Bad Request` - Validation errors

**Example Request:**
```bash
curl -X PUT http://localhost:8081/api/users/me \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith"
  }'
```

---

### 7. Change Password

**Endpoint:** `PATCH /api/users/me/password`

**Authentication:** ✅ Required (Protected)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "oldPassword": "password123",    // Required
  "newPassword": "newpassword456"  // Required, min 8 characters
}
```

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `403 Forbidden` - Old password incorrect
  ```json
  {
    "error": "Old password incorrect"
  }
  ```
- `400 Bad Request` - Validation errors
  ```json
  {
    "error": "Password must be at least 8 characters"
  }
  ```

**Example Request:**
```bash
curl -X PATCH http://localhost:8081/api/users/me/password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "password123",
    "newPassword": "newpassword456"
  }'
```

---

### 8. Delete Account

**Endpoint:** `DELETE /api/users/me`

**Authentication:** ✅ Required (Protected)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - No token or invalid token

**⚠️ Warning:** This permanently deletes the user account. Show confirmation dialog in UI.

**Example Request:**
```bash
curl -X DELETE http://localhost:8081/api/users/me \
  -H "Authorization: Bearer <token>"
```

---

### 9. Get User by ID

**Endpoint:** `GET /api/users/{id}`

**Authentication:** ✅ Required (Protected)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Path Parameters:**
- `id` (required) - User ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `403 Forbidden` - Cannot access other users (only own user allowed)
  ```json
  {
    "error": "Forbidden"
  }
  ```
- `404 Not Found` - User not found

**Note:** Currently, users can only access their own profile (same user ID as authenticated user).

**Example Request:**
```bash
curl -X GET http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer <token>"
```

---

## 📝 Task Service APIs

**Base URL:** `http://localhost:8082` (local) or `https://your-api-endpoint.amazonaws.com` (production)

**⚠️ Important Note:** Task service currently has JWT protection planned but not yet implemented. For now, treat all endpoints as if they require JWT authentication (same token from user-service login).

**All task endpoints will require:**
```
Authorization: Bearer <your-jwt-token>
```

---

### 1. Get All Tasks

**Endpoint:** `GET /api/tasks?userId={userId}`

**Authentication:** ✅ Required (Protected - Planned)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `userId` (optional) - Filter tasks by user ID
  - **Recommended:** Always include authenticated user's ID to get only their tasks
  - If omitted, returns all tasks (not recommended for production)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Buy groceries",
    "description": "Milk, bread, eggs",
    "status": "PENDING",
    "userId": 1,
    "createdAt": "2025-01-16T10:30:00",
    "updatedAt": "2025-01-16T10:30:00"
  },
  {
    "id": 2,
    "title": "Finish project",
    "description": "Complete the backend API",
    "status": "IN_PROGRESS",
    "userId": 1,
    "createdAt": "2025-01-16T11:00:00",
    "updatedAt": "2025-01-16T14:30:00"
  }
]
```

**Empty Response:**
```json
[]
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token (when JWT is implemented)

**Example Request:**
```bash
curl -X GET "http://localhost:8082/api/tasks?userId=1" \
  -H "Authorization: Bearer <token>"
```

**⚠️ UI Implementation Note:**
- Use the `userId` from login response (`LoginResponse.userId`)
- Call this endpoint on app startup or when task list screen loads
- Store tasks locally for offline support (optional)

---

### 2. Get Task by ID

**Endpoint:** `GET /api/tasks/{id}`

**Authentication:** ✅ Required (Protected - Planned)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Path Parameters:**
- `id` (required) - Task ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, bread, eggs",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-01-16T10:30:00",
  "updatedAt": "2025-01-16T10:30:00"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `404 Not Found` - Task not found
  ```json
  {
    "error": "Task not found"
  }
  ```
- `403 Forbidden` - Cannot access other user's task (when authorization is implemented)

**Example Request:**
```bash
curl -X GET http://localhost:8082/api/tasks/1 \
  -H "Authorization: Bearer <token>"
```

---

### 3. Create Task

**Endpoint:** `POST /api/tasks`

**Authentication:** ✅ Required (Protected - Planned)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Buy groceries",           // Required, 3-200 characters
  "description": "Milk, bread, eggs", // Optional, max 1000 characters
  "status": "PENDING",                // Optional, enum: PENDING|IN_PROGRESS|COMPLETED|CANCELLED
  "userId": 1                          // Required (use authenticated user's ID)
}
```

**Note:** 
- Use the `userId` from login response
- If `status` is not provided, defaults to `PENDING`

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, bread, eggs",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-01-16T10:30:00",
  "updatedAt": "2025-01-16T10:30:00"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `400 Bad Request` - Validation errors
  ```json
  {
    "error": "Title is required"
  }
  ```

**Example Request:**
```bash
curl -X POST http://localhost:8082/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries",
    "description": "Milk, bread, eggs",
    "status": "PENDING",
    "userId": 1
  }'
```

---

### 4. Update Task

**Endpoint:** `PUT /api/tasks/{id}`

**Authentication:** ✅ Required (Protected - Planned)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Path Parameters:**
- `id` (required) - Task ID

**Request Body:**
```json
{
  "title": "Buy groceries updated",      // Required, 3-200 characters
  "description": "Milk, bread, eggs, butter", // Optional
  "status": "IN_PROGRESS",               // Optional
  "userId": 1                            // Required (must match authenticated user)
}
```

**Note:** All fields except `userId` can be updated. `userId` must match the authenticated user.

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Buy groceries updated",
  "description": "Milk, bread, eggs, butter",
  "status": "IN_PROGRESS",
  "userId": 1,
  "createdAt": "2025-01-16T10:30:00",
  "updatedAt": "2025-01-16T15:00:00"
}
```

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `404 Not Found` - Task not found
- `403 Forbidden` - Cannot modify other user's task (when authorization is implemented)
- `400 Bad Request` - Validation errors

**Example Request:**
```bash
curl -X PUT http://localhost:8082/api/tasks/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries updated",
    "description": "Milk, bread, eggs, butter",
    "status": "IN_PROGRESS",
    "userId": 1
  }'
```

---

### 5. Delete Task

**Endpoint:** `DELETE /api/tasks/{id}`

**Authentication:** ✅ Required (Protected - Planned)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Path Parameters:**
- `id` (required) - Task ID

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - No token or invalid token
- `404 Not Found` - Task not found
- `403 Forbidden` - Cannot delete other user's task (when authorization is implemented)

**⚠️ Warning:** This permanently deletes the task. Show confirmation dialog in UI.

**Example Request:**
```bash
curl -X DELETE http://localhost:8082/api/tasks/1 \
  -H "Authorization: Bearer <token>"
```

---

## 🔴 Error Responses

### Standard Error Format

```json
{
  "error": "Error message here"
}
```

### Validation Error Format

```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "username",
      "message": "Username is required"
    },
    {
      "field": "email",
      "message": "Email should be valid"
    }
  ]
}
```

### HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| `200 OK` | Success | Request completed successfully |
| `204 No Content` | Success (no body) | Delete operations, password change |
| `400 Bad Request` | Client error | Validation errors, malformed request |
| `401 Unauthorized` | Authentication required | No token, invalid token, expired token |
| `403 Forbidden` | Authorization denied | Valid token but not allowed (e.g., accessing other user's data) |
| `404 Not Found` | Resource not found | User/task doesn't exist |
| `409 Conflict` | Resource conflict | Username/email already exists |
| `500 Internal Server Error` | Server error | Backend issues |

### Error Handling Recommendations

1. **401 Unauthorized:**
   - Clear stored token
   - Redirect to login screen
   - Show message: "Session expired. Please login again."

2. **403 Forbidden:**
   - Show error message: "You don't have permission to perform this action."

3. **404 Not Found:**
   - Show message: "Resource not found."
   - Navigate back or show empty state

4. **400 Bad Request:**
   - Show validation errors near form fields
   - Highlight invalid fields

5. **Network Errors:**
   - Show offline message
   - Retry automatically or provide retry button
   - Cache requests for offline support (optional)

---

## 📊 Data Models

### User Models

#### `UserRequest` (Registration)
```kotlin
data class UserRequest(
    val username: String,      // 3-50 chars, required
    val email: String,          // Valid email, required
    val password: String,       // Min 8 chars, required
    val firstName: String? = null,  // Optional
    val lastName: String? = null    // Optional
)
```

#### `UserResponse` (Registration Response)
```kotlin
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val createdAt: String,      // ISO 8601 format
    val updatedAt: String       // ISO 8601 format
)
```

#### `LoginRequest`
```kotlin
data class LoginRequest(
    val username: String,    // Required
    val password: String     // Required
)
```

#### `LoginResponse`
```kotlin
data class LoginResponse(
    val token: String,       // JWT token - STORE THIS!
    val userId: Long,        // User ID - STORE THIS!
    val username: String
)
```

#### `UserProfileResponse` (Profile endpoints)
```kotlin
data class UserProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
)
```

#### `UpdateProfileRequest`
```kotlin
data class UpdateProfileRequest(
    val firstName: String? = null,  // Optional, max 50 chars
    val lastName: String? = null    // Optional, max 50 chars
)
```

#### `ChangePasswordRequest`
```kotlin
data class ChangePasswordRequest(
    val oldPassword: String,    // Required
    val newPassword: String     // Required, min 8 chars
)
```

#### `AvailabilityResponse` (Username/Email check)
```kotlin
data class AvailabilityResponse(
    val available: Boolean
)
```

---

### Task Models

#### `TaskStatus` (Enum)
```kotlin
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
```

#### `TaskRequest` (Create/Update)
```kotlin
data class TaskRequest(
    val title: String,              // Required, 3-200 chars
    val description: String? = null, // Optional, max 1000 chars
    val status: TaskStatus? = null,  // Optional, defaults to PENDING
    val userId: Long                 // Required (use authenticated user's ID)
)
```

#### `TaskResponse`
```kotlin
data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val userId: Long,
    val createdAt: String,          // ISO 8601 format
    val updatedAt: String           // ISO 8601 format
)
```

---

## 🧪 Testing Instructions

### Prerequisites

1. **Backend must be running:**
   ```bash
   cd backend
   docker-compose up
   ```

2. **Services should be available at:**
   - User Service: `http://localhost:8081`
   - Task Service: `http://localhost:8082`

3. **For Android Emulator:**
   - Use `http://10.0.2.2:8081` and `http://10.0.2.2:8082`

### Testing Flow

#### 1. Test Registration
```
POST /api/users
→ Store user credentials
```

#### 2. Test Login
```
POST /api/auth/login
→ Store token and userId
```

#### 3. Test Protected Endpoints
```
GET /api/users/me (with Authorization header)
→ Verify token works
```

#### 4. Test Task Operations
```
POST /api/tasks (create task)
GET /api/tasks?userId={id} (list tasks)
GET /api/tasks/{id} (get task)
PUT /api/tasks/{id} (update task)
DELETE /api/tasks/{id} (delete task)
```

### Postman Collection

**Import these requests into Postman for testing:**

1. **Register User**
   - Method: POST
   - URL: `http://localhost:8081/api/users`
   - Body: JSON (UserRequest)

2. **Login**
   - Method: POST
   - URL: `http://localhost:8081/api/auth/login`
   - Body: JSON (LoginRequest)
   - **Save token from response to environment variable**

3. **Get Profile**
   - Method: GET
   - URL: `http://localhost:8081/api/users/me`
   - Headers: `Authorization: Bearer {{token}}`

4. **Create Task**
   - Method: POST
   - URL: `http://localhost:8082/api/tasks`
   - Headers: `Authorization: Bearer {{token}}`
   - Body: JSON (TaskRequest)

5. **Get Tasks**
   - Method: GET
   - URL: `http://localhost:8082/api/tasks?userId={{userId}}`
   - Headers: `Authorization: Bearer {{token}}`

### Swagger UI (Interactive API Docs)

**Access Swagger UI:**
- User Service: `http://localhost:8081/swagger-ui/index.html`
- Task Service: `http://localhost:8082/swagger-ui/index.html` (when implemented)

**Features:**
- Interactive API testing
- "Authorize" button to add JWT token
- All endpoints documented with schemas

---

## 📱 UI Implementation Recommendations

### 1. Token Storage

**Android:**
```kotlin
// Use EncryptedSharedPreferences or DataStore
val token = "your-jwt-token"
val userId = 1L

// Store securely
preferences.edit()
    .putString("jwt_token", token)
    .putLong("user_id", userId)
    .apply()
```

### 2. HTTP Client Setup

**Recommended: Ktor Client**

```kotlin
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(
                    accessToken = tokenStorage.getToken() ?: "",
                    ""
                )
            }
        }
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
        header("Content-Type", "application/json")
    }
}
```

### 3. API Client Implementation

```kotlin
class ApiClient {
    private val userServiceUrl = "http://10.0.2.2:8081"  // Android emulator
    private val taskServiceUrl = "http://10.0.2.2:8082"
    
    suspend fun login(username: String, password: String): LoginResponse {
        return httpClient.post("$userServiceUrl/api/auth/login") {
            setBody(LoginRequest(username, password))
        }.body()
    }
    
    suspend fun getCurrentUser(): UserProfileResponse {
        return httpClient.get("$userServiceUrl/api/users/me").body()
    }
    
    suspend fun getTasks(userId: Long): List<TaskResponse> {
        return httpClient.get("$taskServiceUrl/api/tasks?userId=$userId").body()
    }
    
    suspend fun createTask(task: TaskRequest): TaskResponse {
        return httpClient.post("$taskServiceUrl/api/tasks") {
            setBody(task)
        }.body()
    }
}
```

### 4. Error Handling

```kotlin
try {
    val response = apiClient.getTasks(userId)
    // Handle success
} catch (e: ClientRequestException) {
    when (e.response.status.value) {
        401 -> {
            // Clear token, redirect to login
            clearAuth()
            navigateToLogin()
        }
        403 -> {
            // Show forbidden message
            showError("You don't have permission")
        }
        404 -> {
            // Show not found
            showError("Resource not found")
        }
        400 -> {
            // Show validation errors
            val error = e.response.body<ErrorResponse>()
            showValidationErrors(error)
        }
    }
} catch (e: IOException) {
    // Network error
    showError("No internet connection")
}
```

### 5. State Management

**Recommended: ViewModel + StateFlow**

```kotlin
class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskResponse>>(emptyList())
    val tasks: StateFlow<List<TaskResponse>> = _tasks.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    fun loadTasks(userId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = apiClient.getTasks(userId)
                _tasks.value = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }
}
```

---

## 🔄 Authentication Flow in UI

### Recommended Flow:

1. **App Startup:**
   - Check if token exists in storage
   - If yes → Validate token (call `GET /api/users/me`)
     - Success → Navigate to home screen
     - 401 → Clear token, navigate to login
   - If no → Navigate to login screen

2. **Login:**
   - User enters credentials
   - Call `POST /api/auth/login`
   - Store token and userId
   - Navigate to home screen

3. **Logout:**
   - Clear token from storage
   - Navigate to login screen

4. **Protected API Calls:**
   - Always include `Authorization: Bearer <token>` header
   - Handle 401 → Redirect to login
   - Handle 403 → Show forbidden message

---

## 📝 Notes for UI Team

### Important Points:

1. **Token Management:**
   - Store JWT token securely (EncryptedSharedPreferences/DataStore)
   - Token expires after 1 hour
   - Handle token expiration gracefully (redirect to login)

2. **User ID:**
   - Get `userId` from login response
   - Use this `userId` for all task operations
   - Don't let user manually enter `userId`

3. **Task Status:**
   - Use enum values: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
   - Default status is `PENDING`

4. **DateTime Format:**
   - All dates are in ISO 8601 format: `2025-01-16T10:30:00`
   - Parse with your JSON library automatically

5. **Error Handling:**
   - Always handle network errors
   - Show user-friendly error messages
   - Handle 401 by redirecting to login

6. **Loading States:**
   - Show loading indicators during API calls
   - Handle empty states (no tasks, no users)

7. **Confirmation Dialogs:**
   - Show confirmation for destructive actions:
     - Delete task
     - Delete account
     - Change password

8. **Offline Support (Optional):**
   - Cache tasks locally
   - Sync when connection restored
   - Show offline indicator

---

## 🚧 Known Limitations & Future Changes

### Current Status:

1. **Task Service JWT Protection:**
   - ⚠️ **Planned but not yet implemented**
   - Currently endpoints may work without JWT, but treat as if they require JWT
   - JWT protection will be added soon

2. **Task Authorization:**
   - ⚠️ **Authorization checks planned**
   - Users will only be able to access their own tasks
   - 403 Forbidden will be returned for unauthorized access

### Future Enhancements:

1. **Pagination:**
   - Task list endpoints may add pagination later
   - `?page=1&size=20` parameters (to be confirmed)

2. **Search/Filter:**
   - Task search by title
   - Filter by status
   - Sort options

3. **Refresh Token:**
   - Token refresh endpoint (planned)
   - Longer session management

---

## 📞 Contact & Support

**Backend Team:** Contact for API issues, questions, or clarifications

**Swagger Documentation:**
- User Service: `http://localhost:8081/swagger-ui/index.html`
- Task Service: `http://localhost:8082/swagger-ui/index.html` (when available)

---

## ✅ Quick Reference Checklist

### Before Starting Development:

- [ ] Read this entire document
- [ ] Set up HTTP client (Ktor/Retrofit)
- [ ] Configure API base URLs (local development)
- [ ] Set up token storage mechanism
- [ ] Implement error handling
- [ ] Test endpoints with Postman/Swagger

### During Development:

- [ ] Always include `Authorization` header for protected endpoints
- [ ] Use `userId` from login response for task operations
- [ ] Handle 401 errors by redirecting to login
- [ ] Show loading states during API calls
- [ ] Validate inputs on client side before sending requests

### Testing:

- [ ] Test all endpoints
- [ ] Test error scenarios (401, 403, 404, 400)
- [ ] Test with expired token
- [ ] Test network errors
- [ ] Test offline scenarios (optional)

---

**Good luck with the UI development! 🚀**

For any questions or clarifications, contact the backend team.

