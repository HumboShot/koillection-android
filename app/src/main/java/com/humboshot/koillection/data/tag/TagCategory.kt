package com.humboshot.koillection.data.tag

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagCategory(
    @Json(name = "id")
    val id: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "color")
    val color: String? = null,
)

@JsonClass(generateAdapter = true)
data class TagCategoryWrite(
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "color")
    val color: String? = null,
)
