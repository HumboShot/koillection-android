package com.humboshot.koillection.data.login


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticationResult(
    @Json(name = "token")
    val token: String
)