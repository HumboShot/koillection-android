package com.humboshot.koillection.api.core

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
        val jwt = UserContext.instance.jwt
        if (response.request.header("Authorization")?.contains(jwt, true) == false) {
            return response.request.newBuilder().header("Authorization", "JWT $jwt").build()
        }

        runBlocking {
            requestNewToken()
        }.let { newJwt ->
            return if (newJwt.isNotEmpty()) {
                // Save the new token to the UserContext
                UserContext().updateJWT(newJwt)
                // Reconfigure call with new JWT token
                response.request.newBuilder().header("Authorization", "JWT $newJwt").build()
            } else {
                null
            }
        }
    }

    private suspend fun requestNewToken(): String {
        val userCredentials = UserContext().getCredentials()

        val result = ApiClient.getInstance().create(LoginService::class.java).login(
            AuthenticationRequest(userCredentials.username, userCredentials.password)
        )

        result.body()?.let {
            return it.token
        }

        return ""
    }
}
