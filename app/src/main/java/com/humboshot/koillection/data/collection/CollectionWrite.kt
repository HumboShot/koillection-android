package com.humboshot.koillection.data.collection

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionWrite(
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "parent")
    val parent: String? = null,
    @Json(name = "color")
    val color: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null
)
