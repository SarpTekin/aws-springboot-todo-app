# UI & Backend Integration Guide

## 📐 Current Architecture

### **Backend Services Run Locally (No Docker)**

Your local development setup includes:
- ✅ `user-service` - Spring Boot API running locally (Port 8081)
- ✅ `task-service` - Spring Boot API running locally (Port 8082)
- ✅ `userdb` - PostgreSQL database (localhost:5432)
- ✅ `taskdb` - PostgreSQL database (localhost:5432)

**Kotlin UI is a separate project** - Runs independently and communicates with backend via REST APIs.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│           Kotlin UI (Separate Project)             │
│   - Android App                                     │
│   - OR Desktop App (Compose)                        │
│   - OR KMP (Android + iOS)                          │
│                                                     │
│   Location: Different IDE/Project                   │
└───────────────────┬─────────────────────────────────┘
                    │
                    │ HTTP/REST API Calls
                    │ (from your device/emulator)
                    ↓
┌─────────────────────────────────────────────────────┐
│           Local Backend Services                    │
│                                                     │
│  ┌──────────────┐      ┌──────────────┐           │
│  │ user-service │      │ task-service │           │
│  │   Port 8081  │      │   Port 8082  │           │
│  └──────┬───────┘      └──────┬───────┘           │
│         │                     │                     │
│         ↓                     ↓                     │
│  ┌──────────────┐      ┌──────────────┐           │
│  │  userdb      │      │  taskdb      │           │
│  │ PostgreSQL   │      │ PostgreSQL   │           │
│  │ localhost    │      │ localhost    │           │
│  └──────────────┘      └──────────────┘           │
└─────────────────────────────────────────────────────┘
```

---

## 🔌 How UI Connects to Backend

### **Local Development Setup**

When developing locally:

1. **Ensure PostgreSQL is running and databases exist:**
   ```bash
   # Create databases if needed
   psql postgres
   CREATE DATABASE userdb;
   CREATE DATABASE taskdb;
   \q
   ```

2. **Start Backend Services:**
   
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
   
   This starts:
   - `user-service` at `http://localhost:8081`
   - `task-service` at `http://localhost:8082`

2. **Kotlin UI (in your other IDE):**
   - Configure API base URLs:
     ```kotlin
     // In your Kotlin UI project
     val USER_SERVICE_URL = "http://localhost:8081"
     val TASK_SERVICE_URL = "http://localhost:8082"
     ```
   
   - Make API calls from UI:
     ```kotlin
     // Example: Login
     suspend fun login(username: String, password: String): LoginResponse {
         val response = httpClient.post("$USER_SERVICE_URL/api/auth/login") {
             contentType(ContentType.Application.Json)
             body = LoginRequest(username, password)
         }
         return response.body()
     }
     
     // Example: Get Tasks
     suspend fun getTasks(userId: Long): List<TaskResponse> {
         val response = httpClient.get("$TASK_SERVICE_URL/api/tasks?userId=$userId") {
             header("Authorization", "Bearer $token")
         }
         return response.body()
     }
     ```

### **Communication Flow:**

```
Kotlin UI (Device/Emulator)
  ↓ HTTP Request
localhost:8081 or localhost:8082
  ↓ (Direct HTTP call)
Spring Boot Service (user-service or task-service)
  ↓ (Processes request, queries database)
Response back to UI
```

---

## 🚀 Deployment Architecture (AWS)

### **Option 1: Static UI Hosting (Recommended for Portfolio)**

```
┌─────────────────────────────────────────┐
│         Internet Users                  │
└────────────┬────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────┐
│      Amazon CloudFront (CDN)            │
│      - Serves static files              │
│      - HTTPS enabled                    │
└────────────┬────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────┐
│      Amazon S3 Bucket                   │
│      - Stores Kotlin UI static files    │
│      - Android APK (if needed)          │
└─────────────────────────────────────────┘
             │
             ↓ (API Calls)
┌─────────────────────────────────────────┐
│      Application Load Balancer (ALB)    │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    ↓                 ↓
┌─────────┐      ┌─────────┐
│ ECS     │      │ ECS     │
│ user-   │      │ task-   │
│ service │      │ service │
└─────────┘      └─────────┘
```

**Steps:**
1. Build Kotlin UI (Android APK or static web build)
2. Upload to S3 bucket
3. Configure CloudFront to serve from S3
4. Update UI API URLs to point to ALB endpoint
5. Deploy backend to ECS (as planned)

---

### **Option 2: Containerized UI (Alternative)**

If you want to containerize your Kotlin UI (e.g., if it's a web app):

```
┌─────────────────────────────────────────┐
│      Application Load Balancer (ALB)    │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    ↓                 ↓
┌─────────┐      ┌─────────┐
│ ECS     │      │ ECS     │
│ UI      │      │ user-   │
│ (Nginx) │      │ service │
│         │      └─────────┘
│ Serves  │
│ Static  │      ┌─────────┐
│ Files   │      │ ECS     │
└─────────┘      │ task-   │
                 │ service │
                 └─────────┘
```

**Steps:**
1. Build Kotlin UI to static files (HTML/CSS/JS)
2. Upload to S3 bucket
3. Configure CloudFront to serve from S3
4. Update UI API URLs to point to ALB endpoint

---

### **Option 3: Android App (No Container Needed)**

If your UI is an Android app:

```
┌─────────────────────────────────────────┐
│      Android Device/Emulator            │
│      - Your Kotlin UI APK installed     │
└────────────┬────────────────────────────┘
             │
             │ HTTPS API Calls
             ↓
┌─────────────────────────────────────────┐
│      Application Load Balancer (ALB)    │
│      https://your-api.example.com       │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    ↓                 ↓
┌─────────┐      ┌─────────┐
│ ECS     │      │ ECS     │
│ user-   │      │ task-   │
│ service │      │ service │
└─────────┘      └─────────┘
```

**Steps:**
1. Build Android APK in your Kotlin UI project
2. Configure API base URL in app:
   ```kotlin
   // Production config
   const val API_BASE_URL = "https://your-alb-endpoint.amazonaws.com"
   ```
3. Deploy backend to ECS (as planned)
4. Users install APK directly (or via Play Store)

---

## 📝 Configuration for Your Kotlin UI

### **Environment-Specific API URLs**

```kotlin
// In your Kotlin UI project
object ApiConfig {
    // Local Development
    val LOCAL_USER_SERVICE = "http://localhost:8081"
    val LOCAL_TASK_SERVICE = "http://localhost:8082"
    
    // AWS Production
    val AWS_USER_SERVICE = "https://your-alb-endpoint.amazonaws.com"
    val AWS_TASK_SERVICE = "https://your-alb-endpoint.amazonaws.com"
    
    // Choose based on build variant/flavor
    val USER_SERVICE_URL = if (BuildConfig.DEBUG) {
        LOCAL_USER_SERVICE
    } else {
        AWS_USER_SERVICE
    }
    
    val TASK_SERVICE_URL = if (BuildConfig.DEBUG) {
        LOCAL_TASK_SERVICE
    } else {
        AWS_TASK_SERVICE
    }
}
```

---

## 🔧 CORS Configuration (Important!)

Since your UI is separate from backend, you need to enable CORS in Spring Boot:

**For user-service:**
```java
// Add to SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(
        "http://localhost:8080",        // Local UI dev server
        "http://localhost:3000",        // Alternative local port
        "https://your-ui-domain.com"    // Production UI domain
    ));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

// In SecurityFilterChain:
.httpBasic().and()
.cors(Customizer.withDefaults())
```

**For task-service:**
- Same CORS configuration needed

---

## 🧪 Testing the Integration

### **Local Development Testing:**

1. **Start Backend Services:**
   ```bash
   # Terminal 1
   cd backend/user-service
   ./mvnw spring-boot:run
   
   # Terminal 2
   cd backend/task-service
   ./mvnw spring-boot:run
   ```
   
2. **Verify Backend is Running:**
   ```bash
   # Test user-service
   curl http://localhost:8081/api/users/check-username?username=test
   
   # Test task-service
   curl http://localhost:8082/api/tasks
   ```

3. **In Your Kotlin UI Project:**
   - Configure API URLs to `localhost:8081` and `localhost:8082`
   - Run your UI (Android app, desktop app, etc.)
   - Make API calls from UI
   - Verify responses

### **Production Testing (After AWS Deployment):**

1. **Deploy Backend to AWS:**
   - Follow the AWS roadmap
   - Note the ALB endpoint (e.g., `my-app-123456789.us-east-1.elb.amazonaws.com`)

2. **Update UI API URLs:**
   - Change from `localhost` to ALB endpoint
   - Rebuild and test

3. **Test End-to-End:**
   - Run UI on device/emulator
   - Make API calls to AWS backend
   - Verify authentication, CRUD operations, etc.

---

## 📦 What Goes Where

### **Backend Services (Local Development):**
- ✅ Spring Boot application JAR files
- ✅ Java runtime (JRE 17)
- ✅ Application configuration (application.properties)
- ✅ Runs locally using Maven Spring Boot plugin
- ❌ NOT: Kotlin UI code
- ❌ NOT: Frontend assets

### **Kotlin UI Project Includes:**
- ✅ UI code (Kotlin/Compose)
- ✅ API client code (Ktor/Retrofit)
- ✅ Data models (matching backend DTOs)
- ✅ State management (ViewModel/StateFlow)
- ❌ NOT: Backend Spring Boot code
- ❌ NOT: Database connections (UI doesn't connect directly to DB)

---

## 🎯 Summary

**Backend services run locally (no Docker needed for development):**

1. **Current Setup:**
   - Backend: Local Spring Boot services (user-service, task-service)
   - Databases: Local PostgreSQL (userdb, taskdb)
   - UI: Separate project (different IDE)
   - Communication: HTTP/REST API calls

2. **Local Development:**
   - UI calls `http://localhost:8081` and `http://localhost:8082`
   - Services run locally using `./mvnw spring-boot:run`

3. **AWS Production:**
   - Backend: Will be deployed to ECS (as planned in roadmap)
   - UI: 
     - Option A: Android APK (direct install)
     - Option B: Static files on S3 + CloudFront
     - Option C: Containerized web app on ECS
   - Communication: UI calls ALB endpoint (HTTPS)

4. **Key Points:**
   - ✅ UI and backend are completely separate projects
   - ✅ UI communicates with backend via REST APIs only
   - ✅ No database connection from UI (backend handles all DB access)
   - ✅ CORS must be enabled in Spring Boot for web UIs
   - ✅ JWT tokens are stored in UI and sent with each request

---

## 🔗 Example API Integration in Kotlin UI

```kotlin
// API Client Setup
class ApiClient {
    private val userServiceUrl = "http://localhost:8081"  // Local
    // private val userServiceUrl = "https://your-alb.amazonaws.com"  // Production
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // Load JWT token from secure storage
                    BearerTokens(accessToken = tokenStorage.getToken(), "")
                }
            }
        }
    }
    
    suspend fun login(username: String, password: String): LoginResponse {
        return client.post("$userServiceUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
    }
    
    suspend fun getCurrentUser(): UserProfileResponse {
        return client.get("$userServiceUrl/api/users/me") {
            // Authorization header added automatically by Auth plugin
        }.body()
    }
}
```

---

**Your setup is correct! Keep UI and backend separate. They communicate via HTTP APIs only.**

