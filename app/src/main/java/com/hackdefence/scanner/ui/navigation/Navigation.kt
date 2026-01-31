package com.hackdefence.scanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hackdefence.scanner.ui.screens.LoginScreen
import com.hackdefence.scanner.ui.screens.ScannerScreen
import com.hackdefence.scanner.utils.PreferencesManager
import com.hackdefence.scanner.viewmodel.LoginViewModel
import com.hackdefence.scanner.viewmodel.ScannerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Scanner : Screen("scanner")
}

@Composable
fun AppNavigation(
    preferencesManager: PreferencesManager,
    loginViewModel: LoginViewModel,
    scannerViewModel: ScannerViewModel
) {
    val navController = rememberNavController()
    val scannerCode by preferencesManager.scannerCode.collectAsState(initial = null)

    // Determine start destination based on saved scanner code
    val startDestination = if (scannerCode != null) {
        Screen.Scanner.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                viewModel = scannerViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
