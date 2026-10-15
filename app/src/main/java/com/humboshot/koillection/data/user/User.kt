package com.humboshot.koillection.data.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id")
    val id: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "email")
    val email: String? = null,
    @Json(name = "avatar")
    val avatar: String? = null,
    @Json(name = "currency")
    val currency: String? = null,
    @Json(name = "locale")
    val locale: String? = null,
    @Json(name = "timezone")
    val timezone: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
    @Json(name = "wishlistsFeatureEnabled")
    val wishlistsFeatureEnabled: Boolean = true,
    @Json(name = "tagsFeatureEnabled")
    val tagsFeatureEnabled: Boolean = true,
    @Json(name = "signsFeatureEnabled")
    val signsFeatureEnabled: Boolean = true,
    @Json(name = "albumsFeatureEnabled")
    val albumsFeatureEnabled: Boolean = true,
    @Json(name = "loansFeatureEnabled")
    val loansFeatureEnabled: Boolean = true,
    @Json(name = "templatesFeatureEnabled")
    val templatesFeatureEnabled: Boolean = true,
    @Json(name = "historyFeatureEnabled")
    val historyFeatureEnabled: Boolean = true,
    @Json(name = "statisticsFeatureEnabled")
    val statisticsFeatureEnabled: Boolean = true,
    @Json(name = "scrapingFeatureEnabled")
    val scrapingFeatureEnabled: Boolean = true,
)

/** The optional Koillection features. The user can switch each one off in the web settings. */
enum class Feature { WISHLISTS, TAGS, SIGNS, ALBUMS, LOANS, TEMPLATES, HISTORY, STATISTICS, SCRAPING }

fun User.enabledFeatures(): Set<Feature> = buildSet {
    if (wishlistsFeatureEnabled) add(Feature.WISHLISTS)
    if (tagsFeatureEnabled) add(Feature.TAGS)
    if (signsFeatureEnabled) add(Feature.SIGNS)
    if (albumsFeatureEnabled) add(Feature.ALBUMS)
    if (loansFeatureEnabled) add(Feature.LOANS)
    if (templatesFeatureEnabled) add(Feature.TEMPLATES)
    if (historyFeatureEnabled) add(Feature.HISTORY)
    if (statisticsFeatureEnabled) add(Feature.STATISTICS)
    if (scrapingFeatureEnabled) add(Feature.SCRAPING)
}

/** An admin sees every user in `/api/users`, so match the logged-in username. */
fun List<User>.currentUser(username: String?): User? =
    if (username == null) null else firstOrNull { it.username.equals(username, ignoreCase = true) }
