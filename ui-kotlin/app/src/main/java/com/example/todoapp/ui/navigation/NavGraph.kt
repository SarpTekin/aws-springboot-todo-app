package com.example.todoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.screens.CreateTaskScreen
import com.example.todoapp.ui.screens.HomeScreen
import com.example.todoapp.ui.screens.LoginScreen
import com.example.todoapp.ui.screens.TaskListScreen
import com.example.todoapp.ui.screens.UsernameCheckScreen

/**
 * Navigation Routes
 *
 * Defines all screen routes in the app
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val TASK_LIST = "task_list"
    const val CREATE_TASK = "create_task"
    const val USERNAME_CHECK = "username_check"
}

/**
 * Navigation Graph
 *
 * Defines the navigation structure of the app:
 * - Login screen (start destination)
 * - Task List screen (main app screen after login)
 * - Create Task screen (form to add new task)
 * - Home screen (profile/testing JWT)
 * - Username check screen (for testing)
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { userId, username ->
                    // Navigate to task list screen (main app screen)
                    navController.navigate(Routes.TASK_LIST) {
                        // Clear login screen from back stack
                        // (user can't press back to go back to login)
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Task List Screen (Main App Screen)
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onNavigateToCreateTask = {
                    // Navigate to create task screen
                    navController.navigate(Routes.CREATE_TASK)
                },
                onLogout = {
                    // Navigate back to login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TASK_LIST) { inclusive = true }
                    }
                }
            )
        }

        // Create Task Screen
        composable(Routes.CREATE_TASK) {
            CreateTaskScreen(
                onNavigateBack = {
                    // Navigate back to task list
                    // Task list will automatically refresh and show new task!
                    navController.popBackStack()
                }
            )
        }

        // Home Screen (Profile / JWT Testing)
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    // Token is cleared in HomeViewModel.logout()
                    // Navigate back to login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Username Check Screen (for testing/demo)
        composable(Routes.USERNAME_CHECK) {
            UsernameCheckScreen()
        }
    }
}
