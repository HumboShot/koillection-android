package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.data.login.AuthenticationResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("/api/authentication_token")
    suspend fun login(@Body model: AuthenticationRequest): Result<AuthenticationResult>

    /** Blocking variant for the OkHttp authenticator, which runs on an OkHttp thread. */
    @POST("/api/authentication_token")
    fun loginCall(@Body model: AuthenticationRequest): Call<AuthenticationResult>
}
