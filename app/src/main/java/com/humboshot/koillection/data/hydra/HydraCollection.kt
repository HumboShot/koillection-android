package com.humboshot.koillection.data.hydra

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HydraCollection<T>(
    @Json(name = "member")
    val member: List<T> = emptyList(),
    @Json(name = "totalItems")
    val totalItems: Int? = null,
    @Json(name = "view")
    val view: HydraView? = null,
)

@JsonClass(generateAdapter = true)
data class HydraView(
    @Json(name = "next")
    val next: String? = null,
)
