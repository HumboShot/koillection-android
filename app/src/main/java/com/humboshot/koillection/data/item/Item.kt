package com.humboshot.koillection.data.item

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Item(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "quantity")
    val quantity: Int = 1,
    @Json(name = "collection")
    val collection: String?,
    @Json(name = "image")
    val image: String?,
    @Json(name = "imageSmallThumbnail")
    val imageSmallThumbnail: String?,
    @Json(name = "imageLargeThumbnail")
    val imageLargeThumbnail: String?,
    @Json(name = "visibility")
    val visibility: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String?
)
