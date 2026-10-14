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
    private var retrofit: Retrofit? = null
    private var retrofitDomain: String? = null

    @Synchronized
    fun getInstance(): Retrofit {
        val domain = UserContext.instance.getCredentials().domain
        retrofit?.takeIf { retrofitDomain == domain }?.let { return it }

        return build(domain).also {
            retrofit = it
            retrofitDomain = domain
        }
    }

    private fun build(domain: String): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val headersInterceptor = Interceptor { chain ->
            val request = chain
                .request()
                .newBuilder()
                .header("Accept", "application/ld+json")

            val jwt = UserContext.instance.jwt
            if (jwt.isNotEmpty()) {
                request.header("Authorization", "Bearer $jwt")
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
            .baseUrl(domain)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
    }
}