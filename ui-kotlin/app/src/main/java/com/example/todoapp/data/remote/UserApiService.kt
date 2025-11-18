package com.example.todoapp.data.remote

import com.example.todoapp.data.model.AvailabilityResponse
import com.example.todoapp.data.model.LoginRequest
import com.example.todoapp.data.model.LoginResponse
import com.example.todoapp.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * User Service API Interface
 *
 * Defines all endpoints for user-service (http://localhost:8081)
 *
 * Retrofit automatically implements this interface at runtime.
 * You just define WHAT to call, Retrofit handles HOW to call it.
 */
interface UserApiService {

    /**
     * Check if a username is available
     *
     * Endpoint: GET /api/users/check-username?username={username}
     * Auth: None (public endpoint)
     *
     * @param username The username to check
     * @return AvailabilityResponse with available=true/false
     */
    @GET("api/users/check-username")
    suspend fun checkUsername(
        @Query("username") username: String
    ): AvailabilityResponse

    /**
     * Check if an email is available
     *
     * Endpoint: GET /api/users/check-email?email={email}
     * Auth: None (public endpoint)
     *
     * @param email The email to check
     * @return AvailabilityResponse with available=true/false
     */
    @GET("api/users/check-email")
    suspend fun checkEmail(
        @Query("email") email: String
    ): AvailabilityResponse

    /**
     * Register a new user
     *
     * Endpoint: POST /api/users
     * Auth: None (public endpoint)
     *
     * @param request UserRequest with username, email, password, etc.
     * @return UserResponse with user details (no password)
     */
    @POST("api/users")
    suspend fun register(
        @Body request: com.example.todoapp.data.model.UserRequest
    ): UserResponse

    /**
     * Login with username and password
     *
     * Endpoint: POST /api/auth/login
     * Auth: None (public endpoint)
     *
     * @param request LoginRequest with username and password
     * @return LoginResponse with JWT token, userId, and username
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    /**
     * Get current user profile
     *
     * Endpoint: GET /api/users/me
     * Auth: Required (JWT token)
     *
     * @return UserResponse with current user's details
     *
     * NOTE: AuthInterceptor automatically adds "Authorization: Bearer <token>" header
     * No need to manually pass token!
     */
    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponse

    // TODO: Add more protected endpoints (PUT /api/users/me, DELETE /api/users/me, etc.)
}
