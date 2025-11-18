package com.example.todoapp.data.network

import com.example.todoapp.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Authentication Interceptor
 *
 * WHAT IS THIS?
 * - OkHttp interceptor that automatically adds JWT token to requests
 * - Intercepts EVERY HTTP request before it's sent
 * - Adds "Authorization: Bearer <token>" header automatically
 *
 * WHY DO WE NEED THIS?
 * - Without it: We'd have to manually add token to every API call
 * - With it: Token is added automatically to all requests
 * - Cleaner code, less repetition, fewer mistakes
 *
 * HOW IT WORKS:
 * 1. App makes API call (e.g., GET /api/tasks)
 * 2. Interceptor intercepts the request
 * 3. Gets token from TokenManager
 * 4. Adds "Authorization" header with token
 * 5. Sends modified request to server
 * 6. Server sees the token and knows who you are!
 *
 * EXAMPLE:
 * Request WITHOUT interceptor:
 *   GET /api/tasks
 *
 * Request WITH interceptor:
 *   GET /api/tasks
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request
        val originalRequest = chain.request()

        // Get token from TokenManager
        // runBlocking is needed because getToken() is suspend function
        // and intercept() is not a suspend function
        val token = runBlocking {
            tokenManager.getToken()
        }

        // If no token, proceed with original request (public endpoints)
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // Add Authorization header with token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        // Proceed with authenticated request
        val response = chain.proceed(authenticatedRequest)

        // Check if response is 401 Unauthorized (expired/invalid token)
        if (response.code == 401) {
            // Token is invalid/expired
            // Clear token from storage (logout)
            runBlocking {
                tokenManager.clearAuthData()
            }

            // Return the 401 response
            // The app will handle it (show login screen)
            return response
        }

        // Return the response
        return response
    }
}
