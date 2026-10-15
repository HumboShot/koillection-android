package com.humboshot.koillection.data.wishlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Wishlist(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "parent")
    val parent: String? = null,
    @Json(name = "image")
    val image: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)

@JsonClass(generateAdapter = true)
data class Wish(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
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
    @Json(name = "image")
    val image: String? = null,
    @Json(name = "imageSmallThumbnail")
    val imageSmallThumbnail: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
) {
    /** For example "12.50 EUR". Empty when the wish has no price. */
    val priceText: String
        get() = if (price.isNullOrBlank()) "" else listOfNotNull(price, currency?.takeIf { it.isNotBlank() }).joinToString(" ")
}
