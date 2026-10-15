package com.humboshot.koillection.data.album

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "parent")
    val parent: String? = null,
    @Json(name = "image")
    val image: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)

@JsonClass(generateAdapter = true)
data class Photo(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "comment")
    val comment: String? = null,
    @Json(name = "place")
    val place: String? = null,
    @Json(name = "album")
    val album: String? = null,
    @Json(name = "image")
    val image: String? = null,
    @Json(name = "imageSmallThumbnail")
    val imageSmallThumbnail: String? = null,
    @Json(name = "takenAt")
    val takenAt: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
