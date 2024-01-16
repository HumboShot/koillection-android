package com.humboshot.koillection.api.core

import com.humboshot.koillection.UserContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

object ApiClient {
    private var bassAddress = UserContext().baseAddress
    private var jwt: String = UserContext().jwt

    fun getInstance(): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val headersInterceptor = Interceptor { chain ->
            val request = chain
                .request()
                .newBuilder()

            if (jwt.isNotEmpty()) {
                request.addHeader("Authorization", "JWT $jwt")
            }

            return@Interceptor chain.proceed(request.build())
        }

        val okHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(headersInterceptor)
            .authenticator(TokenAuthenticator())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(UserContext().getCredentials().domain)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}