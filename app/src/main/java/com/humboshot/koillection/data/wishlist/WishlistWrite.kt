package com.humboshot.koillection.data.wishlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WishlistWrite(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "parent")
    val parent: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
