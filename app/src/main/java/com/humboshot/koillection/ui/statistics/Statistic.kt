package com.humboshot.koillection.ui.statistics

import com.humboshot.koillection.data.user.Feature

/**
 * One statistics tile. [resource] is the API list path below `/api/`. A null [resource] means the
 * app counts the value itself. [feature] hides the tile when the user switched that feature off.
 */
enum class Statistic(val label: String, val resource: String?, val feature: Feature? = null) {
    COLLECTIONS("Collections", "collections"),
    ITEMS("Items", "items"),
    TAGS("Tags", "tags", Feature.TAGS),
    WISHLISTS("Wishlists", "wishlists", Feature.WISHLISTS),
    WISHES("Wishes", "wishes", Feature.WISHLISTS),
    ALBUMS("Albums", "albums", Feature.ALBUMS),
    PHOTOS("Photos", "photos", Feature.ALBUMS),
    TEMPLATES("Templates", "templates", Feature.TEMPLATES),
    LOANS("Loans", "loans", Feature.LOANS),
    OPEN_LOANS("Items lent now", null, Feature.LOANS),
}

/** The tiles to show. A null [features] (user not loaded) shows every tile. */
fun visibleStatistics(features: Set<Feature>?): List<Statistic> =
    Statistic.entries.filter { stat -> features == null || stat.feature == null || stat.feature in features }
