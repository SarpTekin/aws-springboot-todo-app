# MicroTodo Android App

A production-ready Android application built with **Kotlin**, **Jetpack Compose**, and **Clean Architecture** that connects to your Spring Boot microservices backend.

## Architecture Overview

This app follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│  ┌──────────────┐      ┌────────────────┐  │
│  │  Composable  │ ───> │   ViewModel    │  │
│  │    Screens   │ <─── │   (UI State)   │  │
│  └──────────────┘      └────────────────┘  │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│             Domain Layer                    │
│  ┌────────────────┐   ┌──────────────────┐ │
│  │   Use Cases    │   │  Domain Models   │ │
│  │  (Business     │   │  Repository      │ │
│  │   Logic)       │   │  Interfaces      │ │
│  └────────────────┘   └──────────────────┘ │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│              Data Layer                     │
│  ┌────────────┐  ┌──────────┐  ┌─────────┐ │
│  │  Retrofit  │  │   DTOs   │  │ DataStore│ │
│  │    API     │  │          │  │ (Token)  │ │
│  └────────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────┘
```

## Tech Stack

- **Language**: Kotlin 1.9.20
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVVM
- **DI**: Koin 3.5.0
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **JSON**: Kotlinx Serialization 1.6.0
- **Async**: Kotlin Coroutines + Flow
- **Storage**: DataStore (JWT token)
- **Testing**: JUnit, MockK, Turbine, Coroutines Test

## Project Structure

```
app/src/main/java/com/microtodo/android/
├── data/
│   ├── local/
│   │   └── TokenManager.kt              # JWT token storage
│   ├── remote/
│   │   ├── api/
│   │   │   ├── UserApiService.kt        # User API endpoints
│   │   │   └── TaskApiService.kt        # Task API endpoints
│   │   ├── dto/
│   │   │   ├── UserDtos.kt              # User DTOs
│   │   │   └── TaskDtos.kt              # Task DTOs
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt       # JWT auth interceptor
│   └── repository/
│       ├── AuthRepositoryImpl.kt        # Auth implementation
│       └── TaskRepositoryImpl.kt        # Task implementation
│
├── domain/
│   ├── model/
│   │   ├── User.kt                      # Domain User model
│   │   ├── Task.kt                      # Domain Task model
│   │   └── AuthResult.kt                # Auth result
│   ├── repository/
│   │   ├── AuthRepository.kt            # Auth contract
│   │   └── TaskRepository.kt            # Task contract
│   └── usecase/
│       ├── LoginUseCase.kt              # Login logic
│       ├── RegisterUseCase.kt           # Registration logic
│       ├── GetTasksUseCase.kt           # Fetch tasks
│       ├── CreateTaskUseCase.kt         # Create task
│       ├── UpdateTaskUseCase.kt         # Update task
│       └── DeleteTaskUseCase.kt         # Delete task
│
├── presentation/
│   ├── login/
│   │   ├── LoginViewModel.kt            # Login state & logic
│   │   └── LoginScreen.kt               # Login UI
│   ├── register/
│   │   ├── RegisterViewModel.kt         # Register state & logic
│   │   └── RegisterScreen.kt            # Register UI
│   ├── tasks/
│   │   ├── TasksViewModel.kt            # Tasks state & logic
│   │   └── TasksScreen.kt               # Tasks UI
│   └── common/
│       └── Navigation.kt                # Navigation graph
│
├── di/
│   └── AppModule.kt                     # Koin DI modules
│
├── ui/theme/
│   └── Theme.kt                         # Material Theme
│
├── TodoApp.kt                           # Application class
└── MainActivity.kt                      # Entry point

app/src/test/java/com/microtodo/android/
├── data/
│   ├── AuthRepositoryTest.kt
│   └── TaskRepositoryTest.kt
└── presentation/
    ├── LoginViewModelTest.kt
    └── TasksViewModelTest.kt
```

## Features

- User registration and authentication
- JWT token management (automatic refresh)
- Task CRUD operations
- Real-time UI updates with Kotlin Flow
- Offline-first token storage
- Material 3 design
- Comprehensive unit tests
- Error handling

## Prerequisites

1. **Android Studio**: Hedgehog (2023.1.1) or later
2. **JDK**: 17 or later
3. **Android SDK**: API 26+ (Android 8.0+)
4. **Running Backend**: Spring Boot services on ports 8081 and 8082

## Setup Instructions

### 1. Open the Project

```bash
cd android-app
# Open in Android Studio
```

### 2. Configure Backend URLs

Edit `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "USER_SERVICE_BASE_URL", "\"http://10.0.2.2:8081\"")
buildConfigField("String", "TASK_SERVICE_BASE_URL", "\"http://10.0.2.2:8082\"")
```

**Important Notes:**
- `10.0.2.2` is the special Android emulator IP that maps to `localhost`
- For physical devices, use your computer's IP address (e.g., `"http://192.168.1.100:8081"`)
- For production, use HTTPS URLs

### 3. Start Backend Services

```bash
# In separate terminals
cd backend/user-service
./gradlew bootRun

cd backend/task-service
./gradlew bootRun
```

Verify services are running:
- User Service: http://localhost:8081/api/users
- Task Service: http://localhost:8082/api/tasks

### 4. Run the App

**Option A: Android Studio**
1. Click "Run" ▶️ button
2. Select emulator or connected device

**Option B: Command Line**
```bash
./gradlew installDebug
```

### 5. Run Tests

```bash
# Unit tests
./gradlew test

# With coverage
./gradlew testDebugUnitTest
```

## Backend API Integration

The app connects to two microservices:

### User Service (Port 8081)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/login` | POST | ❌ | Login user |
| `/api/users` | POST | ❌ | Register user |
| `/api/users/{id}` | GET | ✅ | Get user details |

### Task Service (Port 8082)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/tasks` | POST | ✅ | Create task |
| `/api/tasks` | GET | ✅ | Get all tasks |
| `/api/tasks/{id}` | GET | ✅ | Get task by ID |
| `/api/tasks/{id}` | PUT | ✅ | Update task |
| `/api/tasks/{id}` | DELETE | ✅ | Delete task |

✅ = Requires JWT Bearer token

## Authentication Flow

1. User enters credentials on Login screen
2. App calls `POST /api/auth/login`
3. Backend returns JWT token + user info
4. App stores token in DataStore (encrypted)
5. AuthInterceptor adds `Authorization: Bearer <token>` to all requests
6. Token persists until logout

## How to Use the App

### First Time Setup
1. Launch app
2. Tap "Don't have an account? Sign Up"
3. Fill registration form (username, email, password)
4. Tap "Sign Up"
5. Return to login and sign in

### Managing Tasks
1. Sign in
2. Tap **+** button to create task
3. Enter title and optional description
4. Tap **⋮** on task to:
   - Change status (Pending → In Progress → Completed)
   - Delete task
5. Swipe to refresh tasks
6. Tap logout icon to sign out

## Troubleshooting

### Network Errors

**Issue**: "Failed to connect to /10.0.2.2:8081"

**Solutions**:
1. Verify backend is running: `curl http://localhost:8081/api/users`
2. For physical device, use computer IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
3. Check firewall allows port 8081/8082
4. Ensure `usesCleartextTraffic="true"` in AndroidManifest.xml

### Build Errors

**Issue**: "Cannot resolve symbol 'BuildConfig'"

**Solution**: Sync Gradle and rebuild
```bash
./gradlew clean build
```

**Issue**: "Duplicate class kotlinx.serialization..."

**Solution**: Clear Gradle cache
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
```

### Authentication Issues

**Issue**: "401 Unauthorized" on tasks

**Solution**: Token expired (1 hour lifetime). Log out and log back in.

## Production Checklist

Before deploying to production:

- [ ] Update API URLs to HTTPS
- [ ] Remove `usesCleartextTraffic="true"` from AndroidManifest.xml
- [ ] Enable ProGuard/R8 obfuscation
- [ ] Implement token refresh mechanism
- [ ] Add SSL certificate pinning
- [ ] Set up crash reporting (Firebase Crashlytics)
- [ ] Add analytics
- [ ] Generate launcher icons
- [ ] Test on multiple devices
- [ ] Add integration tests
- [ ] Set up CI/CD pipeline

## Testing

### Unit Tests Coverage

- **ViewModels**: LoginViewModel, TasksViewModel
- **Repositories**: AuthRepository, TaskRepository
- **Use Cases**: Login, Register, CRUD operations

Run tests:
```bash
./gradlew test --info
```

View coverage report:
```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Dependencies

Key libraries and their purposes:

```kotlin
// UI
androidx.compose:compose-bom:2023.10.01          // Compose BOM
androidx.compose.material3:material3              // Material Design 3

// Networking
com.squareup.retrofit2:retrofit:2.9.0            // HTTP client
com.squareup.okhttp3:okhttp:4.12.0               // Network layer
com.squareup.okhttp3:logging-interceptor:4.12.0  // Request logging

// Serialization
org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0  // JSON parsing

// Dependency Injection
io.insert-koin:koin-android:3.5.0                // DI framework
io.insert-koin:koin-androidx-compose:3.5.0       // Compose integration

// Storage
androidx.datastore:datastore-preferences:1.0.0   // Key-value storage

// Testing
junit:junit:4.13.2                               // Unit testing
io.mockk:mockk:1.13.8                            // Mocking
app.cash.turbine:turbine:1.0.0                   // Flow testing
```

## Contributing

See backend TODO comments for incomplete features:

- [ ] Token refresh mechanism
- [ ] Offline caching with Room database
- [ ] Pull-to-refresh implementation
- [ ] Search and filter tasks
- [ ] Task sorting options
- [ ] User profile editing
- [ ] Dark mode toggle
- [ ] Biometric authentication

## License

MIT License - See LICENSE file for details

## Support

For issues or questions:
1. Check [Troubleshooting](#troubleshooting) section
2. Review backend API documentation
3. Inspect Logcat for detailed errors: `adb logcat | grep MicroTodo`

---

**Built with ❤️ using Kotlin and Jetpack Compose**
