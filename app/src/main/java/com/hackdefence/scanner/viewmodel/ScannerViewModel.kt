package com.hackdefence.scanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackdefence.scanner.api.RetrofitClient
import com.hackdefence.scanner.data.AttendeeDetails
import com.hackdefence.scanner.data.VerifyEntryRequest
import com.hackdefence.scanner.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Verifying : ScanState()
    data class Success(val details: AttendeeDetails) : ScanState()
    data class AlreadyEntered(val details: AttendeeDetails) : ScanState()
    data class PaymentNotVerified(val details: AttendeeDetails) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScannerViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private val _staffName = MutableStateFlow<String>("")
    val staffName: StateFlow<String> = _staffName

    private val _scanCount = MutableStateFlow(0)
    val scanCount: StateFlow<Int> = _scanCount

    init {
        viewModelScope.launch {
            preferencesManager.staffName.collect { name ->
                _staffName.value = name ?: ""
            }
        }
    }

    fun verifyEntry(qrData: String) {
        viewModelScope.launch {
            _scanState.value = ScanState.Verifying

            try {
                val scannerCode = preferencesManager.scannerCode.first() ?: ""

                val response = RetrofitClient.apiService.verifyEntry(
                    VerifyEntryRequest(
                        qr_data = qrData,
                        verified_by = scannerCode
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    when {
                        body.success && body.details != null -> {
                            _scanCount.value++
                            _scanState.value = ScanState.Success(body.details)
                        }
                        body.warning == true && body.details != null -> {
                            _scanState.value = ScanState.AlreadyEntered(body.details)
                        }
                        body.error?.contains("Payment not verified", ignoreCase = true) == true
                            && body.details != null -> {
                            _scanState.value = ScanState.PaymentNotVerified(body.details)
                        }
                        else -> {
                            _scanState.value = ScanState.Error(
                                body.error ?: body.message ?: "Verification failed"
                            )
                        }
                    }
                } else {
                    _scanState.value = ScanState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(
                    "Connection error: ${e.message ?: "Please check your internet connection"}"
                )
            }
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Scanning
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearScannerInfo()
            _scanState.value = ScanState.Idle
            _scanCount.value = 0
        }
    }
}
