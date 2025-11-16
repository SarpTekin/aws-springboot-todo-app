package com.microtodo.android.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.presentation.login.LoginScreen
import com.microtodo.android.presentation.register.RegisterScreen
import com.microtodo.android.presentation.tasks.TasksScreen
import org.koin.compose.koinInject

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Tasks : Screen("tasks")
}

/**
 * Main navigation graph
 */
@Composable
fun AppNavigation(
    authRepository: AuthRepository = koinInject()
) {
    val navController = rememberNavController()
    val isAuthenticated by authRepository.isAuthenticated().collectAsState(initial = false)

    // Determine start destination based on auth state
    val startDestination = if (isAuthenticated) Screen.Tasks.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
