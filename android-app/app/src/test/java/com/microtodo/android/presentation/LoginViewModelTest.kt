package com.microtodo.android.presentation

import app.cash.turbine.test
import com.microtodo.android.domain.model.AuthResult
import com.microtodo.android.domain.usecase.LoginUseCase
import com.microtodo.android.presentation.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var loginUseCase: LoginUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.username)
            assertEquals("", state.password)
            assertFalse(state.isLoading)
            assertFalse(state.isSuccess)
            assertNull(state.error)
        }
    }

    @Test
    fun `onUsernameChanged should update username`() = runTest {
        viewModel.onUsernameChanged("testuser")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("testuser", state.username)
        }
    }

    @Test
    fun `onPasswordChanged should update password`() = runTest {
        viewModel.onPasswordChanged("password123")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("password123", state.password)
        }
    }

    @Test
    fun `login success should update state correctly`() = runTest {
        // Given
        val authResult = AuthResult(
            token = "jwt_token",
            userId = 1L,
            username = "testuser"
        )
        coEvery { loginUseCase("testuser", "password123") } returns Result.success(authResult)

        // When
        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSuccess)
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("", state.username) // Should be cleared
            assertEquals("", state.password) // Should be cleared
        }

        coVerify { loginUseCase("testuser", "password123") }
    }

    @Test
    fun `login failure should update error state`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        coEvery { loginUseCase("testuser", "wrongpass") } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("wrongpass")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSuccess)
            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)
        }

        coVerify { loginUseCase("testuser", "wrongpass") }
    }

    @Test
    fun `login should set loading state during execution`() = runTest {
        // Given
        coEvery { loginUseCase(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(AuthResult("token", 1L, "user"))
        }

        // When
        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("password123")

        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            viewModel.login()

            // Should show loading
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `resetSuccessState should clear success flag`() = runTest {
        // Given
        val authResult = AuthResult("token", 1L, "user")
        coEvery { loginUseCase(any(), any()) } returns Result.success(authResult)

        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetSuccessState()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSuccess)
        }
    }
}
