package com.humboshot.koillection.data.hydra

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** A Hydra list page where only the total count matters. The members are not parsed. */
@JsonClass(generateAdapter = true)
data class HydraTotal(
    @Json(name = "totalItems")
    val totalItems: Int? = null,
)
