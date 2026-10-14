package com.humboshot.koillection.api.core

import com.humboshot.koillection.auth.Session
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/** Adds the token only to requests for the user's own server. Images from other hosts get no token. */
class AuthInterceptor(private val session: Session) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = session.token
        if (token == null || !session.isServer(request.url)) return chain.proceed(request)

        return chain.proceed(request.withToken(token))
    }
}

internal fun Session.isServer(url: HttpUrl): Boolean {
    val server = serverUrl?.toHttpUrlOrNull() ?: return false
    return url.scheme == server.scheme && url.host == server.host && url.port == server.port
}

internal fun Request.withToken(token: String): Request =
    newBuilder().header("Authorization", "Bearer $token").build()
