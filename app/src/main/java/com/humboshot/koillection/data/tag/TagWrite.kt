package com.humboshot.koillection.data.tag

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagWrite(
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "category")
    val category: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
