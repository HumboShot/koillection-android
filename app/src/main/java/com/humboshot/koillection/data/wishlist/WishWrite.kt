package com.humboshot.koillection.data.wishlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WishWrite(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "url")
    val url: String? = null,
    @Json(name = "price")
    val price: String? = null,
    @Json(name = "currency")
    val currency: String? = null,
    @Json(name = "wishlist")
    val wishlist: String? = null,
    @Json(name = "comment")
    val comment: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)
