package com.microtodo.android.data

import com.microtodo.android.data.local.TokenManager
import com.microtodo.android.data.remote.api.UserApiService
import com.microtodo.android.data.remote.dto.LoginRequest
import com.microtodo.android.data.remote.dto.LoginResponse
import com.microtodo.android.data.remote.dto.UserRequest
import com.microtodo.android.data.remote.dto.UserResponse
import com.microtodo.android.data.repository.AuthRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var userApiService: UserApiService
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        userApiService = mockk()
        tokenManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(userApiService, tokenManager)
    }

    @Test
    fun `login success should save token and return AuthResult`() = runTest {
        // Given
        val loginRequest = LoginRequest("testuser", "password123")
        val loginResponse = LoginResponse(
            token = "jwt_token",
            userId = 1L,
            username = "testuser"
        )

        coEvery { userApiService.login(loginRequest) } returns loginResponse
        coEvery { tokenManager.saveAuthData(any(), any(), any()) } just Runs

        // When
        val result = repository.login("testuser", "password123")

        // Then
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertEquals("jwt_token", authResult.token)
        assertEquals(1L, authResult.userId)
        assertEquals("testuser", authResult.username)

        coVerify { tokenManager.saveAuthData("jwt_token", 1L, "testuser") }
    }

    @Test
    fun `login failure should return error`() = runTest {
        // Given
        val exception = Exception("Invalid credentials")
        coEvery { userApiService.login(any()) } throws exception

        // When
        val result = repository.login("testuser", "wrongpass")

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `register success should return User`() = runTest {
        // Given
        val userRequest = UserRequest(
            username = "newuser",
            email = "newuser@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )
        val userResponse = UserResponse(
            id = 1L,
            username = "newuser",
            email = "newuser@example.com",
            firstName = "John",
            lastName = "Doe",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )

        coEvery { userApiService.registerUser(userRequest) } returns userResponse

        // When
        val result = repository.register(
            username = "newuser",
            email = "newuser@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertEquals("newuser", user.username)
        assertEquals("newuser@example.com", user.email)
    }

    @Test
    fun `logout should clear auth data`() = runTest {
        // Given
        coEvery { tokenManager.clearAuthData() } just Runs

        // When
        repository.logout()

        // Then
        coVerify { tokenManager.clearAuthData() }
    }

    @Test
    fun `isAuthenticated should return token manager flow`() = runTest {
        // Given
        every { tokenManager.isAuthenticated() } returns flowOf(true)

        // When
        val flow = repository.isAuthenticated()

        // Then
        verify { tokenManager.isAuthenticated() }
    }
}
