package com.hackdefence.scanner.data

data class LoginRequest(
    val scanner_code: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val scanner: ScannerInfo?
)

data class ScannerInfo(
    val scanner_code: String,
    val staff_name: String,
    val assigned_to: String?
)

data class VerifyEntryRequest(
    val qr_data: String,
    val verified_by: String
)

data class VerifyEntryResponse(
    val success: Boolean,
    val message: String?,
    val error: String?,
    val warning: Boolean?,
    val details: AttendeeDetails?
)

data class AttendeeDetails(
    val registration_id: String,
    val full_name: String,
    val email: String,
    val ticket_type: String?,
    val user_type: String?,
    val college: String?,
    val company: String?,
    val final_price: Double?,
    val participation_mode: String?,
    val verified_at: String?,
    val verified_by: String?,
    val entry_verified_at: String?,
    val entry_verified_by: String?
)
