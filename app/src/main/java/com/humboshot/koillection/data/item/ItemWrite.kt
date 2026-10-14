package com.humboshot.koillection.data.item

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemWrite(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "quantity")
    val quantity: Int? = null,
    @Json(name = "collection")
    val collection: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null
)
