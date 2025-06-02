package com.humboshot.koillection.api.core

import android.util.Log
import com.humboshot.koillection.UserContext
import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.data.login.AuthenticationRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {
    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        runBlocking {
            val jwt = UserContext().jwt
            if (response.request.header("Authorization")?.contains(jwt, true) == false) {
                jwt
            } else {
                requestNewToken()
            }
        }.let { newJWT ->
            return if (newJWT.isNotEmpty()) {
                return response.request.newBuilder().header("Authorization", "Bearer $newJWT").build()
            } else {
                null
            }
        }
    }

    private suspend fun requestNewToken(): String {
        Log.d("TokenAuthenticator", "Requesting new token")
        val userCredentials = UserContext().getCredentials()

        val authApiService = ApiClient.getInstance().create(LoginService::class.java)
        authApiService.login(AuthenticationRequest(userCredentials.username, userCredentials.password))
            .onSuccess {
                Log.d("TokenAuthenticator", "New token received: ${it.token}")
                UserContext().updateJWT(it.token)
                UserContext().jwt = it.token
                return it.token
            }
            .onFailure {
                Log.d("TokenAuthenticator", "Failed to refresh token")
                return ""
            }

        return ""
    }
}
