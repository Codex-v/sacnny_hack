package com.hackdefence.scanner.api

import com.hackdefence.scanner.data.LoginRequest
import com.hackdefence.scanner.data.LoginResponse
import com.hackdefence.scanner.data.VerifyEntryRequest
import com.hackdefence.scanner.data.VerifyEntryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/scanner/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/admin/entry/verify")
    suspend fun verifyEntry(@Body request: VerifyEntryRequest): Response<VerifyEntryResponse>
}
