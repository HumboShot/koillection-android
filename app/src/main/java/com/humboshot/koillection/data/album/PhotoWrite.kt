package com.humboshot.koillection.data.album

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoWrite(
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "comment")
    val comment: String? = null,
    @Json(name = "place")
    val place: String? = null,
    @Json(name = "album")
    val album: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
