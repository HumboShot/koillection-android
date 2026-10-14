package com.humboshot.koillection.api.core

import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.auth.Session
import com.humboshot.koillection.data.login.AuthenticationRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Gets a new token when the server answers 401. The server has no refresh token,
 * so this logs in again with the saved username and password.
 *
 * [loginService] must use a client without this authenticator, or a failed login would loop.
 */
class TokenAuthenticator(
    private val session: Session,
    private val loginService: (serverUrl: String) -> LoginService,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null || !session.isServer(response.request.url)) return null

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        synchronized(this) {
            val current = session.token ?: return null
            if (current != failedToken) return response.request.withToken(current)

            val serverUrl = session.serverUrl ?: return null
            val username = session.username ?: return null
            val password = session.password ?: return null

            // OkHttp rethrows a non-IO exception from an authenticator on its own thread, which crashes
            // the app. A bad login body or a Keystore error must therefore only fail this request.
            val login = try {
                loginService(serverUrl).loginCall(AuthenticationRequest(username, password)).execute()
            } catch (e: Exception) {
                return null
            }

            val token = login.body()?.token
            return when {
                login.isSuccessful && token != null -> {
                    try {
                        session.updateToken(token)
                    } catch (e: Exception) {
                        return null
                    }
                    response.request.withToken(token)
                }
                // Only a rejected password ends the session. A 5xx is a server problem, so keep the credentials.
                login.code() == 401 -> {
                    session.signOut()
                    null
                }
                else -> null
            }
        }
    }
}
