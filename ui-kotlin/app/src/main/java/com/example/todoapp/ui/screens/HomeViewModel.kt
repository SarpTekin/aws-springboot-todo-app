package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.TokenManager
import com.example.todoapp.data.model.UserResponse
import com.example.todoapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Home ViewModel
 *
 * RESPONSIBILITY:
 * - Fetch current user profile from backend
 * - Test that JWT interceptor works (automatic token injection)
 * - Handle logout
 */
class HomeViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Fetch user profile when ViewModel is created
        fetchUserProfile()
    }

    /**
     * Fetch current user profile
     *
     * This tests the JWT interceptor:
     * - Makes call to protected endpoint (GET /api/users/me)
     * - AuthInterceptor automatically adds token
     * - No manual token handling needed!
     */
    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Call protected endpoint
            // AuthInterceptor will automatically add: Authorization: Bearer <token>
            val result = repository.getCurrentUser()

            result
                .onSuccess { user ->
                    _uiState.value = HomeUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to load profile"
                    )
                }
        }
    }

    /**
     * Logout - Clear token and return to login
     */
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAuthData()
            _uiState.value = HomeUiState.LoggedOut
        }
    }
}

/**
 * Home UI State
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val user: UserResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    object LoggedOut : HomeUiState()
}
