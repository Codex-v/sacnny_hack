package com.hackdefence.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.hackdefence.scanner.api.RetrofitClient
import com.hackdefence.scanner.ui.navigation.AppNavigation
import com.hackdefence.scanner.ui.theme.ScannerTheme
import com.hackdefence.scanner.utils.PreferencesManager
import com.hackdefence.scanner.viewmodel.LoginViewModel
import com.hackdefence.scanner.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var scannerViewModel: ScannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize preferences manager
        preferencesManager = PreferencesManager(applicationContext)

        // Initialize ViewModels
        loginViewModel = LoginViewModel(preferencesManager)
        scannerViewModel = ScannerViewModel(preferencesManager)

        // Restore auth token from saved preferences
        lifecycleScope.launch {
            val savedToken = preferencesManager.authToken.first()
            if (savedToken != null) {
                RetrofitClient.authToken = savedToken
            }
        }

        setContent {
            ScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        preferencesManager = preferencesManager,
                        loginViewModel = loginViewModel,
                        scannerViewModel = scannerViewModel
                    )
                }
            }
        }
    }
}
