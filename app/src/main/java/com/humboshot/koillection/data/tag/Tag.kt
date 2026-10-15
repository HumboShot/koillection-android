package com.humboshot.koillection.data.tag

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tag(
    @Json(name = "id")
    val id: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "description")
    val description: String?,
    @Json(name = "image")
    val image: String? = null,
    @Json(name = "imageSmallThumbnail")
    val imageSmallThumbnail: String?,
    @Json(name = "category")
    val category: String?,
    @Json(name = "visibility")
    val visibility: String? = null,
)
