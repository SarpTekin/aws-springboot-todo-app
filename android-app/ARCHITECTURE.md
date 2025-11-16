# Architecture Documentation

## Clean Architecture Overview

This Android app follows **Clean Architecture** principles, organizing code into three distinct layers with clear separation of concerns.

## Layer Breakdown

### 1. Presentation Layer (`presentation/`)

**Responsibility**: UI and user interactions

**Components**:
- **Composable Screens**: Jetpack Compose UI
- **ViewModels**: UI state management and business logic orchestration
- **Navigation**: Screen routing

**Key Principles**:
- ViewModels expose `StateFlow<UiState>` for reactive UI updates
- No direct dependency on data layer (only domain)
- Unidirectional data flow (UDF)

**Example Flow**:
```
User taps Login
  ↓
LoginScreen emits event
  ↓
LoginViewModel.login()
  ↓
LoginUseCase.invoke()
  ↓
ViewModel updates StateFlow
  ↓
Screen recomposes with new state
```

### 2. Domain Layer (`domain/`)

**Responsibility**: Business logic and rules

**Components**:
- **Models**: Clean, platform-agnostic data classes
- **Repository Interfaces**: Contracts for data operations
- **Use Cases**: Single-responsibility business logic

**Key Principles**:
- No Android framework dependencies
- Pure Kotlin code
- Easily testable
- Defines "what" not "how"

**Use Case Pattern**:
```kotlin
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<AuthResult> {
        // Validation
        if (username.isBlank()) return Result.failure(...)

        // Business logic
        return authRepository.login(username, password)
    }
}
```

### 3. Data Layer (`data/`)

**Responsibility**: Data sources and implementation

**Components**:
- **Remote**: API services, DTOs, interceptors
- **Local**: DataStore, Room (future), preferences
- **Repository Implementations**: Concrete implementations of domain contracts

**Key Principles**:
- DTOs separate from domain models
- Repositories handle data source orchestration
- Error handling and mapping

**Repository Pattern**:
```kotlin
class AuthRepositoryImpl(
    private val api: UserApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun login(...): Result<AuthResult> {
        return try {
            val dto = api.login(...)
            tokenManager.saveToken(dto.token)
            Result.success(dto.toDomain()) // DTO → Domain mapping
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Dependency Flow

```
Presentation → Domain → Data
     ↓           ↓         ↓
 ViewModel   UseCase   Repository
     ↓           ↓         ↓
 UI State   Business   API/DB
              Rules    Access
```

**Dependency Rule**:
- Outer layers depend on inner layers
- Inner layers never depend on outer layers
- Domain layer has NO dependencies

## MVVM Pattern

### ViewModel State Management

```kotlin
// UI State (immutable data class)
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
class LoginViewModel(useCase: LoginUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ... business logic
        }
    }
}
```

### Screen Composition

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // UI recomposes automatically when state changes
    TextField(
        value = state.username,
        onValueChange = viewModel::onUsernameChanged
    )
}
```

## Dependency Injection (Koin)

### Module Organization

```kotlin
val networkModule = module {
    single { /* OkHttp */ }
    single { /* Retrofit */ }
    single<UserApiService> { /* API implementation */ }
}

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}

val domainModule = module {
    factory { LoginUseCase(get()) }  // New instance each time
}

val presentationModule = module {
    viewModel { LoginViewModel(get()) }  // ViewModel scope
}
```

### Dependency Graph

```
LoginScreen
    ↓ inject
LoginViewModel(loginUseCase)
    ↓ inject
LoginUseCase(authRepository)
    ↓ inject
AuthRepositoryImpl(userApiService, tokenManager)
    ↓ inject
UserApiService(retrofit)
    ↓ inject
Retrofit(okHttpClient)
```

## Data Flow Patterns

### 1. Authentication Flow

```
LoginScreen
    → LoginViewModel.login()
    → LoginUseCase.invoke()
    → AuthRepository.login()
    → UserApiService.login() [HTTP POST]
    → Response<LoginResponse>
    → TokenManager.saveToken()
    → Result<AuthResult> (Success)
    → ViewModel updates state
    → Navigation to TasksScreen
```

### 2. Task Creation Flow

```
TasksScreen (+ button)
    → TasksViewModel.showCreateDialog()
    → User fills form
    → TasksViewModel.createTask()
    → CreateTaskUseCase.invoke()
    → TaskRepository.createTask()
    → TaskApiService.createTask() [HTTP POST with JWT]
    → Response<TaskResponse>
    → Result<Task>
    → ViewModel adds task to state
    → UI recomposes with new task
```

### 3. JWT Authentication Flow

```
Any authenticated request
    → Retrofit call initiated
    → AuthInterceptor.intercept()
    → Check if public endpoint
    → If not: TokenManager.getToken()
    → Add "Authorization: Bearer <token>" header
    → Proceed with request
```

## Error Handling Strategy

### Repository Level

```kotlin
suspend fun login(...): Result<AuthResult> {
    return try {
        val response = api.login(...)
        Result.success(response.toDomain())
    } catch (e: HttpException) {
        Result.failure(e) // Network errors
    } catch (e: Exception) {
        Result.failure(e) // Other errors
    }
}
```

### ViewModel Level

```kotlin
useCase().fold(
    onSuccess = { data ->
        _uiState.update { it.copy(data = data, isLoading = false) }
    },
    onFailure = { error ->
        _uiState.update { it.copy(error = error.message, isLoading = false) }
    }
)
```

### UI Level

```kotlin
if (state.error != null) {
    Text(state.error, color = MaterialTheme.colorScheme.error)
}
```

## Testing Strategy

### 1. Unit Tests (ViewModels)

```kotlin
@Test
fun `login success should update state`() = runTest {
    // Given
    coEvery { loginUseCase(...) } returns Result.success(mockAuth)

    // When
    viewModel.login()

    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue(state.isSuccess)
    }
}
```

### 2. Unit Tests (Repositories)

```kotlin
@Test
fun `login should save token on success`() = runTest {
    // Given
    coEvery { api.login(...) } returns mockResponse

    // When
    repository.login(...)

    // Then
    coVerify { tokenManager.saveToken(mockToken) }
}
```

### 3. Integration Tests (Future)

Would test full flows with real API calls using test server.

## Navigation Architecture

### Navigation Graph

```kotlin
NavHost(startDestination = "login") {
    composable("login") {
        LoginScreen(
            onNavigateToRegister = { navController.navigate("register") },
            onLoginSuccess = { navController.navigate("tasks") }
        )
    }
    composable("register") { RegisterScreen(...) }
    composable("tasks") { TasksScreen(...) }
}
```

### Navigation Rules

- Login/Register are public routes
- Tasks screen requires authentication
- Navigation clears backstack on logout
- Deep linking support (future)

## State Management Principles

### 1. Unidirectional Data Flow (UDF)

```
User Action → Event → ViewModel → State Update → UI Recomposition
     ↑                                                    ↓
     └─────────────────────────────────────────────────┘
```

### 2. Immutable State

All UI state is immutable. Updates create new copies:

```kotlin
_uiState.update { currentState ->
    currentState.copy(isLoading = true)  // New instance
}
```

### 3. Single Source of Truth

ViewModel holds the single source of truth. UI never modifies data directly.

## Security Considerations

### 1. Token Storage

- JWT stored in DataStore (encrypted at rest)
- Never logged or exposed in UI
- Cleared on logout

### 2. Network Security

- HTTPS required for production
- Certificate pinning (TODO)
- No sensitive data in URLs

### 3. Input Validation

- Client-side validation in Use Cases
- Server-side validation as backup
- Sanitize user input

## Performance Optimizations

### 1. Lazy Loading

```kotlin
val username: Flow<String> = authRepository.getCurrentUsername()
    .map { it ?: "User" }
    .stateIn(viewModelScope, SharingStarted.Lazily, "")
```

### 2. Coroutine Scoping

- `viewModelScope` automatically cancels on ViewModel clear
- Structured concurrency prevents leaks

### 3. Compose Optimization

- `remember` for expensive calculations
- `derivedStateOf` for computed values
- `LaunchedEffect` for side effects

## Future Improvements

### 1. Offline Support

- Room database for local caching
- Sync strategy when network available

### 2. Pagination

- Paging 3 library integration
- Infinite scroll for task list

### 3. Real-time Updates

- WebSocket connection
- Server-sent events (SSE)

### 4. Advanced State Management

- Consider MVI architecture
- Event/Effect pattern for one-time events

## References

- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/mental-model)
