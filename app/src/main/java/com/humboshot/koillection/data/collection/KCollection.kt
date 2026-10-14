package com.humboshot.koillection.data.collection


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Member(
    @Json(name = "color")
    val color: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "finalVisibility")
    val finalVisibility: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "image")
    val image: String?,
    @Json(name = "itemsDefaultTemplate")
    val itemsDefaultTemplate: String?,
    @Json(name = "owner")
    val owner: String?,
    @Json(name = "parent")
    val parent: String?,
    @Json(name = "parentVisibility")
    val parentVisibility: String?,
    @Json(name = "scrapedFromUrl")
    val scrapedFromUrl: String?,
    @Json(name = "seenCounter")
    val seenCounter: Int,
    @Json(name = "title")
    val title: String,
    @Json(name = "updatedAt")
    val updatedAt: String?,
    @Json(name = "visibility")
    val visibility: String
)