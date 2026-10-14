package com.humboshot.koillection.data.datum

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatumWrite(
    @Json(name = "item")
    val item: String? = null,
    @Json(name = "collection")
    val collection: String? = null,
    @Json(name = "type")
    val type: String? = null,
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "value")
    val value: String? = null,
    @Json(name = "position")
    val position: Int? = null,
    @Json(name = "currency")
    val currency: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null
)
