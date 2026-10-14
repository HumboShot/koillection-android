package com.humboshot.koillection.api.core

import com.humboshot.koillection.BuildConfig
import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.auth.AuthStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

object ApiClient {
    private var retrofit: Retrofit? = null
    private var retrofitDomain: String? = null

    private val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /** Has no token handling, so a failed login cannot start another login. */
    private val loginClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addDebugLogging()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /** Shared with Coil, so image requests also get the token and a new token after a 401. */
    val okHttpClient: OkHttpClient by lazy {
        loginClient.newBuilder()
            .addInterceptor(AuthInterceptor(AuthStore))
            .authenticator(TokenAuthenticator(AuthStore, ::loginService))
            .build()
    }

    private val apiClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("Accept", "application/ld+json").build())
            }
            .build()
    }

    @Synchronized
    fun getInstance(): Retrofit {
        // A request that starts just after a sign-out uses the old client. It has no token, so it fails with 401.
        val domain = AuthStore.serverUrl
            ?: return checkNotNull(retrofit) { "No server. The user is not logged in." }
        retrofit?.takeIf { retrofitDomain == domain }?.let { return it }

        return build(domain, apiClient).also {
            retrofit = it
            retrofitDomain = domain
        }
    }

    fun loginService(serverUrl: String): LoginService = build(serverUrl, loginClient).create(LoginService::class.java)

    private fun build(domain: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(domain)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()

    // BASIC logs only the request line and status. BODY would log the password in the login request.
    private fun OkHttpClient.Builder.addDebugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                }
            )
        }
    }
}
