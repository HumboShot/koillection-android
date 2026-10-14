package com.humboshot.koillection.data.datum

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Datum(
    @Json(name = "id")
    val id: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "value")
    val value: String?,
    @Json(name = "position")
    val position: Int?,
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "image")
    val image: String?,
    @Json(name = "imageSmallThumbnail")
    val imageSmallThumbnail: String?,
    @Json(name = "imageLargeThumbnail")
    val imageLargeThumbnail: String?,
    @Json(name = "file")
    val file: String?,
    @Json(name = "video")
    val video: String?,
    @Json(name = "originalFilename")
    val originalFilename: String?
)
