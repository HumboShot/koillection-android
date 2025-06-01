package com.humboshot.koillection.data.collection


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Collection(
    @Json(name = "@context")
    val context: String,
    @Json(name = "member")
    val member: List<Member>,
    @Json(name = "totalItems")
    val totalItems: Int,
)