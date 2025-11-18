package com.example.todoapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Token Manager - Secure Local Storage for Authentication Data
 *
 * WHAT IS THIS?
 * - Manages JWT token storage using DataStore
 * - Stores: token, userId, username
 * - Persists data even when app is closed
 * - Type-safe, coroutine-based storage
 *
 * WHY DO WE NEED THIS?
 * - User shouldn't login every time they open the app
 * - Token needed for authenticated API calls
 * - Secure, encrypted storage (DataStore + Android's security)
 *
 * WHAT IS DATASTORE?
 * - Modern replacement for SharedPreferences
 * - Kotlin Coroutines support (async operations)
 * - Type-safe (no casting needed)
 * - Data consistency guarantees
 */

// Extension property to create DataStore instance
// This is created once per app and reused
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    // Keys for storing data
    // Think of these as "variable names" in the storage
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    /**
     * Save authentication data after successful login
     *
     * @param token JWT token from backend
     * @param userId User's ID
     * @param username User's username
     */
    suspend fun saveAuthData(token: String, userId: Long, username: String) {
        context.dataStore.edit { preferences ->
            // edit {} block allows us to modify stored data
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
        }
    }

    /**
     * Get the JWT token (if exists)
     *
     * @return JWT token or null if not logged in
     */
    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    /**
     * Get the user ID (if exists)
     *
     * @return User ID or null if not logged in
     */
    suspend fun getUserId(): Long? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_ID_KEY]
    }

    /**
     * Get the username (if exists)
     *
     * @return Username or null if not logged in
     */
    suspend fun getUsername(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USERNAME_KEY]
    }

    /**
     * Check if user is logged in
     *
     * @return true if token exists, false otherwise
     */
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Observe token changes as Flow
     *
     * This is reactive - when token changes, observers get notified
     * Useful for automatically updating UI when login state changes
     *
     * @return Flow of token (null if not logged in)
     */
    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    /**
     * Clear all authentication data (logout)
     *
     * Removes token, userId, and username from storage
     */
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()  // Remove everything
        }
    }

    /**
     * Get all auth data at once
     *
     * @return AuthData object or null if not logged in
     */
    suspend fun getAuthData(): AuthData? {
        val preferences = context.dataStore.data.first()
        val token = preferences[TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]

        return if (token != null && userId != null && username != null) {
            AuthData(token, userId, username)
        } else {
            null
        }
    }
}

/**
 * Data class to hold all authentication data
 */
data class AuthData(
    val token: String,
    val userId: Long,
    val username: String
)
