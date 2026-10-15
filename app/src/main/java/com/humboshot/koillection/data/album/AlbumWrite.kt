package com.humboshot.koillection.data.album

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumWrite(
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "parent")
    val parent: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
