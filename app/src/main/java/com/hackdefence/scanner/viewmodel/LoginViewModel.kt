package com.hackdefence.scanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackdefence.scanner.api.RetrofitClient
import com.hackdefence.scanner.data.LoginRequest
import com.hackdefence.scanner.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val staffName: String, val assignedTo: String?) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(scannerCode: String) {
        if (scannerCode.isBlank()) {
            _loginState.value = LoginState.Error("Please enter scanner code")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(scannerCode.trim()))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.scanner != null) {
                        // Save to preferences
                        preferencesManager.saveScannerInfo(
                            code = body.scanner.scanner_code,
                            name = body.scanner.staff_name,
                            assignedTo = body.scanner.assigned_to
                        )
                        _loginState.value = LoginState.Success(
                            body.scanner.staff_name,
                            body.scanner.assigned_to
                        )
                    } else {
                        _loginState.value = LoginState.Error(body.message ?: "Login failed")
                    }
                } else {
                    _loginState.value = LoginState.Error("Scanner not found or inactive")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    "Connection error: ${e.message ?: "Please check your internet connection"}"
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
