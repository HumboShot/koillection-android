package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.data.login.AuthenticationResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    /** Blocking, because the OkHttp authenticator runs it on an OkHttp thread. The login screen runs it on IO. */
    @POST("/api/authentication_token")
    fun loginCall(@Body model: AuthenticationRequest): Call<AuthenticationResult>
}
