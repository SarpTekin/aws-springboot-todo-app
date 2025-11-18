# Kotlin UI Development Prompt for Todo Application

## Project Overview

You are building a modern Kotlin UI application (choose: Android, Desktop with Compose, or Kotlin Multiplatform) that communicates with a Spring Boot microservices backend. The backend consists of two services: **user-service** for authentication and user management, and **task-service** for task management.

---

## Backend Architecture

### Base URLs
- **User Service:** `http://localhost:8081`
- **Task Service:** `http://localhost:8082`

### Authentication
- **Type:** JWT (JSON Web Token) based, stateless authentication
- **Flow:** 
  1. User registers via `POST /api/users`
  2. User logs in via `POST /api/auth/login` → receives JWT token
  3. All protected endpoints require `Authorization: Bearer <token>` header
  4. Token contains `userId` and `username` claims
  5. Token expiration: 1 hour (3600000 ms)

---

## User Service API (http://localhost:8081)

### Public Endpoints (No Authentication Required)

#### 1. Register New User
- **Endpoint:** `POST /api/users`
- **Request Body:**
```json
{
  "username": "string",        // Required, 3-50 chars
  "email": "string",            // Required, valid email format
  "password": "string",         // Required, min 8 chars
  "firstName": "string",        // Optional
  "lastName": "string"          // Optional
}
```
- **Success Response (200):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "createdAt": "2025-11-16T12:00:00",
  "updatedAt": "2025-11-16T12:00:00"
}
```
- **Error Responses:**
  - `400 Bad Request` - Validation errors (fields with messages)
  - `409 Conflict` - Username or email already exists

#### 2. Check Username Availability
- **Endpoint:** `GET /api/users/check-username?username=foo`
- **Success Response (200):**
```json
{
  "available": true
}
```

#### 3. Check Email Availability
- **Endpoint:** `GET /api/users/check-email?email=foo@example.com`
- **Success Response (200):**
```json
{
  "available": true
}
```

#### 4. Login
- **Endpoint:** `POST /api/auth/login`
- **Request Body:**
```json
{
  "username": "string",    // Required
  "password": "string"     // Required
}
```
- **Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "string"
}
```
- **Error Responses:**
  - `401 Unauthorized` - Invalid credentials
  - `400 Bad Request` - Validation errors

---

### Protected Endpoints (JWT Token Required)

**All endpoints below require header:** `Authorization: Bearer <token>`

#### 5. Get Current User Profile
- **Endpoint:** `GET /api/users/me`
- **Success Response (200):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string"
}
```
- **Error Responses:**
  - `401 Unauthorized` - No token or invalid token
  - `404 Not Found` - User not found

#### 6. Update Current User Profile
- **Endpoint:** `PUT /api/users/me`
- **Request Body:**
```json
{
  "firstName": "NewName",    // Optional, max 50 chars
  "lastName": "NewSurname"   // Optional, max 50 chars
}
```
- **Success Response (200):** Same as `GET /api/users/me`
- **Error Responses:**
  - `401 Unauthorized` - No token or invalid token
  - `400 Bad Request` - Validation errors

#### 7. Change Password
- **Endpoint:** `PATCH /api/users/me/password`
- **Request Body:**
```json
{
  "oldPassword": "string",    // Required
  "newPassword": "string"     // Required, min 8 chars
}
```
- **Success Response (204):** No Content
- **Error Responses:**
  - `401 Unauthorized` - No token or invalid token
  - `403 Forbidden` - Old password incorrect
  - `400 Bad Request` - Validation errors

#### 8. Delete Account
- **Endpoint:** `DELETE /api/users/me`
- **Success Response (204):** No Content
- **Error Responses:**
  - `401 Unauthorized` - No token or invalid token

#### 9. Get User by ID
- **Endpoint:** `GET /api/users/{id}`
- **Success Response (200):** Same as `GET /api/users/me`
- **Error Responses:**
  - `401 Unauthorized` - No token or invalid token
  - `403 Forbidden` - Cannot access other users (only own user allowed)
  - `404 Not Found` - User not found

---

## Task Service API (http://localhost:8082)

**Note:** Currently task-service endpoints are not JWT-protected, but **treat them as if they require JWT authentication** for future-proofing. Send `Authorization: Bearer <token>` header with all requests.

### Protected Endpoints (JWT Token Required)

**All endpoints require header:** `Authorization: Bearer <token>`

#### 1. Get All Tasks (Filtered by User)
- **Endpoint:** `GET /api/tasks?userId={id}`
- **Query Parameters:**
  - `userId` (optional) - Filter tasks by user ID
- **Success Response (200):**
```json
[
  {
    "id": 1,
    "title": "Buy groceries",
    "description": "Milk, bread, eggs",
    "status": "PENDING",
    "userId": 1,
    "createdAt": "2025-11-16T12:00:00",
    "updatedAt": "2025-11-16T12:00:00"
  }
]
```
- **Task Status Values:** `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

#### 2. Get Task by ID
- **Endpoint:** `GET /api/tasks/{id}`
- **Success Response (200):**
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, bread, eggs",
  "status": "PENDING",
  "userId": 1,
  "createdAt": "2025-11-16T12:00:00",
  "updatedAt": "2025-11-16T12:00:00"
}
```
- **Error Responses:**
  - `404 Not Found` - Task not found

#### 3. Create Task
- **Endpoint:** `POST /api/tasks`
- **Request Body:**
```json
{
  "title": "string",              // Required, 3-200 chars
  "description": "string",         // Optional, max 1000 chars
  "status": "PENDING",             // Optional, enum: PENDING|IN_PROGRESS|COMPLETED|CANCELLED
  "userId": 1                      // Required
}
```
- **Success Response (200):** Same as `GET /api/tasks/{id}`
- **Error Responses:**
  - `400 Bad Request` - Validation errors

#### 4. Update Task
- **Endpoint:** `PUT /api/tasks/{id}`
- **Request Body:** Same as `POST /api/tasks` (all fields optional except userId)
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "userId": 1
}
```
- **Success Response (200):** Same as `GET /api/tasks/{id}`
- **Error Responses:**
  - `404 Not Found` - Task not found
  - `400 Bad Request` - Validation errors

#### 5. Delete Task
- **Endpoint:** `DELETE /api/tasks/{id}`
- **Success Response (204):** No Content
- **Error Responses:**
  - `404 Not Found` - Task not found

---

## Error Response Format

### Validation Errors (400)
```json
{
  "error": "validation_error",
  "details": [
    {
      "field": "username",
      "message": "Username is required"
    }
  ]
}
```

### General Errors
```json
{
  "error": "Error message here"
}
```

### HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Resource created (if applicable)
- `204 No Content` - Success with no body
- `400 Bad Request` - Validation/request errors
- `401 Unauthorized` - Authentication required/invalid token
- `403 Forbidden` - Not authorized (e.g., accessing other user's data)
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists (e.g., duplicate username/email)

---

## Data Models (Kotlin)

### User Models
```kotlin
// Registration Request
data class UserRequest(
    val username: String,      // 3-50 chars
    val email: String,          // Valid email
    val password: String,       // Min 8 chars
    val firstName: String? = null,
    val lastName: String? = null
)

// Registration/User Response
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val createdAt: String,      // ISO 8601 format
    val updatedAt: String       // ISO 8601 format
)

// Profile Response (from /me endpoint)
data class UserProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
)

// Update Profile Request
data class UpdateProfileRequest(
    val firstName: String? = null,  // Max 50 chars
    val lastName: String? = null    // Max 50 chars
)

// Change Password Request
data class ChangePasswordRequest(
    val oldPassword: String,    // Required
    val newPassword: String     // Required, min 8 chars
)

// Login Request
data class LoginRequest(
    val username: String,       // Required
    val password: String        // Required
)

// Login Response
data class LoginResponse(
    val token: String,          // JWT token
    val userId: Long,
    val username: String
)

// Availability Check Response
data class AvailabilityResponse(
    val available: Boolean
)
```

### Task Models
```kotlin
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

// Task Request (Create/Update)
data class TaskRequest(
    val title: String,              // Required, 3-200 chars
    val description: String? = null, // Optional, max 1000 chars
    val status: TaskStatus? = null,  // Optional
    val userId: Long                 // Required
)

// Task Response
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

### Error Models
```kotlin
data class ErrorResponse(
    val error: String,
    val details: List<FieldError>? = null
)

data class FieldError(
    val field: String,
    val message: String
)
```

---

## Technical Requirements

### HTTP Client
- Use **Ktor Client** (recommended for Kotlin) or **Retrofit** if you prefer
- Implement base URL configuration (user-service and task-service)
- Automatic token injection via interceptors
- Error handling (401 → logout, 403 → show error, 400 → show validation messages)

### Token Management
- Store JWT token securely:
  - **Android:** Use `EncryptedSharedPreferences` or `DataStore`
  - **Desktop:** Use encrypted storage or secure keychain
  - **KMP:** Use `Settings` or platform-specific secure storage
- Store `userId` and `username` alongside token for quick access
- Implement token refresh logic (if needed later)
- Auto-logout on 401 Unauthorized

### Network Layer Architecture
```
api/
  ├── client/
  │   ├── ApiClient.kt          // Base HTTP client setup
  │   ├── AuthInterceptor.kt    // Inject Authorization header
  │   └── ErrorHandler.kt       // Handle errors globally
  ├── user/
  │   ├── UserApi.kt            // User service endpoints
  │   └── UserRepository.kt     // Repository pattern
  ├── task/
  │   ├── TaskApi.kt            // Task service endpoints
  │   └── TaskRepository.kt     // Repository pattern
  └── models/                   // All data classes
```

### State Management
- Use **ViewModel** (Android) or **StateFlow/SharedFlow** (KMP/Desktop)
- Implement proper loading states
- Handle error states
- Manage authentication state globally

---

## UI/UX Requirements

### Screens to Implement

#### 1. Authentication Flow
- **Registration Screen:**
  - Form fields: Username, Email, Password, Confirm Password, First Name, Last Name
  - Real-time username/email availability checking (optional but recommended)
  - Form validation (show errors inline)
  - Navigate to login after successful registration
  - Error handling (show backend validation errors)

- **Login Screen:**
  - Form fields: Username, Password
  - "Remember me" option (optional)
  - "Forgot password" link (for future implementation)
  - Navigate to task list after successful login
  - Error handling (invalid credentials)

#### 2. Task Management Flow
- **Task List Screen:**
  - Display all tasks for current user
  - Filter by status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
  - Search/filter functionality (optional)
  - Pull-to-refresh
  - Empty state when no tasks
  - Loading indicator
  - Each task shows: title, status, creation date

- **Task Detail Screen:**
  - View full task details
  - Edit button (navigate to edit screen)
  - Delete button (with confirmation dialog)
  - Status indicator
  - Created/Updated timestamps

- **Create/Edit Task Screen:**
  - Form fields: Title (required), Description (optional), Status (dropdown)
  - Form validation
  - Save/Cancel buttons
  - Error handling

#### 3. Profile Management
- **Profile Screen:**
  - Display current user info (from `/api/users/me`)
  - Edit profile button
  - Change password button
  - Delete account button (with confirmation)
  - Logout button

- **Edit Profile Screen:**
  - Form: First Name, Last Name
  - Save/Cancel buttons
  - Error handling

- **Change Password Screen:**
  - Form: Old Password, New Password, Confirm New Password
  - Validation (password match)
  - Save/Cancel buttons
  - Error handling (old password incorrect)

#### 4. Navigation
- Bottom navigation or drawer menu:
  - Tasks (default after login)
  - Profile
  - Logout
- Back button handling
- Deep linking support (optional)

### Design Requirements
- **Modern, Clean UI:**
  - Use Material Design 3 (Android) or Material Design for Desktop
  - Consistent color scheme
  - Clear typography hierarchy
  - Proper spacing and padding

- **User Experience:**
  - Loading indicators for async operations
  - Success/error snackbars/toasts
  - Confirmation dialogs for destructive actions (delete task, delete account)
  - Smooth navigation transitions
  - Pull-to-refresh where applicable

- **Accessibility:**
  - Proper labels for screen readers
  - Color contrast compliance
  - Keyboard navigation support (Desktop)

---

## Implementation Checklist

### Phase 1: Project Setup
- [ ] Choose Kotlin UI framework (Android/Desktop/KMP)
- [ ] Set up project structure
- [ ] Add HTTP client dependency (Ktor/Retrofit)
- [ ] Add JSON serialization library (kotlinx.serialization)
- [ ] Add dependency injection framework (Koin/Hilt)
- [ ] Create base URL configuration

### Phase 2: Network Layer
- [ ] Create data models (matching backend DTOs)
- [ ] Implement ApiClient with base configuration
- [ ] Create AuthInterceptor to inject JWT token
- [ ] Implement UserApi interface/service
- [ ] Implement TaskApi interface/service
- [ ] Create UserRepository
- [ ] Create TaskRepository
- [ ] Implement global error handler

### Phase 3: Authentication
- [ ] Implement secure token storage
- [ ] Create AuthManager/AuthRepository
- [ ] Build Registration screen + ViewModel
- [ ] Build Login screen + ViewModel
- [ ] Implement authentication state management
- [ ] Add auto-logout on 401

### Phase 4: Task Management
- [ ] Build Task List screen + ViewModel
- [ ] Build Task Detail screen + ViewModel
- [ ] Build Create/Edit Task screen + ViewModel
- [ ] Implement task filtering by status
- [ ] Add pull-to-refresh
- [ ] Implement task CRUD operations

### Phase 5: Profile Management
- [ ] Build Profile screen + ViewModel
- [ ] Build Edit Profile screen + ViewModel
- [ ] Build Change Password screen + ViewModel
- [ ] Implement account deletion

### Phase 6: Polish
- [ ] Add loading states everywhere
- [ ] Implement error handling UI
- [ ] Add confirmation dialogs
- [ ] Improve navigation flow
- [ ] Add empty states
- [ ] Testing (unit tests for ViewModels/Repositories)

---

## Example API Client Implementation (Ktor)

```kotlin
// Example structure - implement this pattern
class ApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
        }
    }
    
    // Add auth interceptor that injects token
    // Handle errors globally
}

// Example usage in Repository
class TaskRepository(private val apiClient: ApiClient) {
    suspend fun getTasks(userId: Long): Result<List<TaskResponse>> {
        return try {
            val response = apiClient.get("http://localhost:8082/api/tasks?userId=$userId")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Testing the UI

### Manual Testing Flow
1. **Registration:**
   - Register new user
   - Check username availability
   - Verify validation errors
   - Confirm successful registration

2. **Login:**
   - Login with registered credentials
   - Verify token is stored
   - Confirm navigation to task list

3. **Tasks:**
   - Create new task
   - View task list
   - Edit task
   - Change task status
   - Delete task
   - Verify all operations work correctly

4. **Profile:**
   - View profile
   - Update first/last name
   - Change password
   - Verify token still works after password change
   - Test account deletion

5. **Error Handling:**
   - Test with invalid token (401)
   - Test with expired token
   - Test validation errors
   - Test network errors

---

## Additional Notes

- **Backend URLs:** Use environment-specific configuration (dev/staging/prod)
- **Token Expiration:** Handle token expiration gracefully (redirect to login)
- **Offline Support:** Consider implementing local caching for tasks (optional)
- **Security:** Never log JWT tokens in production code
- **Performance:** Implement pagination if task list grows large (backend may need updates)
- **Real-time Updates:** Consider WebSocket for real-time task updates (future enhancement)

---

## Deliverables

Create a complete Kotlin UI application with:
1. All screens and navigation flow
2. Complete API integration
3. Authentication flow with JWT
4. Error handling and validation
5. Modern, user-friendly UI
6. Clean architecture (MVVM/MVI recommended)
7. Proper state management
8. Documentation/comments in code

---

**Ready to start building!** Use this prompt with Claude AI or any other AI assistant to generate the Kotlin UI code for your todo application backend.

