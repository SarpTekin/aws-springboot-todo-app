package com.microtodo.android.data.remote.interceptor

import com.microtodo.android.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds JWT Bearer token to all authenticated requests
 * Format: Authorization: Bearer <token>
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token for public endpoints (login, register)
        val isPublicEndpoint = originalRequest.url.encodedPath.let { path ->
            path.contains("/api/auth/login") ||
            (path.contains("/api/users") && originalRequest.method == "POST")
        }

        if (isPublicEndpoint) {
            return chain.proceed(originalRequest)
        }

        // Get token and add to request header
        val token = runBlocking {
            tokenManager.getToken().first()
        }

        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authenticatedRequest)
    }
}
