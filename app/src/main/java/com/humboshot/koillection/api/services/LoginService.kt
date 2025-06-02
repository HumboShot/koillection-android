package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.data.login.AuthenticationResult
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("/api/authentication_token")
    suspend fun login(@Body model: AuthenticationRequest): Result<AuthenticationResult>
}