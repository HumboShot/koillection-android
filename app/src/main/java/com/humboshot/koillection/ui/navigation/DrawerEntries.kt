package com.humboshot.koillection.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.humboshot.koillection.R
import com.humboshot.koillection.data.user.Feature
import com.humboshot.koillection.destinations.AlbumList
import com.humboshot.koillection.destinations.ChoiceListList
import com.humboshot.koillection.destinations.History
import com.humboshot.koillection.destinations.InventoryList
import com.humboshot.koillection.destinations.LoanList
import com.humboshot.koillection.destinations.Statistics
import com.humboshot.koillection.destinations.TagList
import com.humboshot.koillection.destinations.TemplateList
import com.humboshot.koillection.destinations.WishlistList

/**
 * The side menu entries, in the order of the web sidebar. Each feature screen adds its entry to [others].
 *
 * - [features] is null when the user is not loaded yet. Then every entry shows.
 * - [open] goes back to the Collections root and then opens the given route. A null route opens Collections.
 * - An entry is selected when the current route starts with one of its route prefixes.
 */
fun drawerEntries(
    features: Set<Feature>?,
    currentRoute: String?,
    open: (route: String?) -> Unit,
): List<DrawerEntry> {
    fun enabled(feature: Feature) = features == null || feature in features
    fun isCurrent(prefix: String) = currentRoute?.startsWith(prefix) == true

    val others = buildList {
        if (enabled(Feature.WISHLISTS)) {
            add(
                DrawerEntry(
                    label = WishlistList.title,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    selected = isCurrent(WishlistList.ROUTE_PREFIX),
                    onClick = { open(WishlistList.createRoute()) },
                )
            )
        }
        if (enabled(Feature.TAGS)) {
            add(
                DrawerEntry(
                    label = TagList.title,
                    icon = { Icon(painterResource(R.drawable.ic_tags), contentDescription = null) },
                    selected = isCurrent(TagList.ROUTE_PREFIX),
                    onClick = { open(TagList.createRoute()) },
                )
            )
        }
        if (enabled(Feature.ALBUMS)) {
            add(
                DrawerEntry(
                    label = AlbumList.title,
                    icon = { Icon(painterResource(R.drawable.ic_albums), contentDescription = null) },
                    selected = isCurrent(AlbumList.ROUTE_PREFIX),
                    onClick = { open(AlbumList.createRoute()) },
                )
            )
        }
        if (enabled(Feature.LOANS)) {
            add(
                DrawerEntry(
                    label = LoanList.title,
                    icon = { Icon(painterResource(R.drawable.ic_loans), contentDescription = null) },
                    selected = isCurrent(LoanList.ROUTE_PREFIX),
                    onClick = { open(LoanList.createRoute()) },
                )
            )
        }
        if (enabled(Feature.TEMPLATES)) {
            add(DrawerEntry(label = null))
            add(
                DrawerEntry(
                    label = TemplateList.title,
                    icon = { Icon(painterResource(R.drawable.ic_templates), contentDescription = null) },
                    selected = isCurrent(TemplateList.ROUTE_PREFIX),
                    onClick = { open(TemplateList.createRoute()) },
                )
            )
            add(
                DrawerEntry(
                    label = ChoiceListList.title,
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    selected = isCurrent(ChoiceListList.ROUTE_PREFIX),
                    onClick = { open(ChoiceListList.createRoute()) },
                )
            )
        }
        // Inventories have no feature switch, so this group always shows.
        add(DrawerEntry(label = null))
        if (enabled(Feature.HISTORY)) {
            add(
                DrawerEntry(
                    label = History.title,
                    icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = null) },
                    selected = isCurrent(History.ROUTE_PREFIX),
                    onClick = { open(History.createRoute()) },
                )
            )
        }
        if (enabled(Feature.STATISTICS)) {
            add(
                DrawerEntry(
                    label = Statistics.title,
                    icon = { Icon(painterResource(R.drawable.ic_statistics), contentDescription = null) },
                    selected = isCurrent(Statistics.ROUTE_PREFIX),
                    onClick = { open(Statistics.createRoute()) },
                )
            )
        }
        add(
            DrawerEntry(
                label = InventoryList.title,
                icon = { Icon(Icons.Default.Done, contentDescription = null) },
                selected = isCurrent(InventoryList.ROUTE_PREFIX),
                onClick = { open(InventoryList.createRoute()) },
            )
        )
    }

    val collections = DrawerEntry(
        label = "Collections",
        icon = { Icon(painterResource(R.drawable.ic_collections), contentDescription = null) },
        selected = others.none { it.selected },
        onClick = { open(null) },
    )
    return listOf(collections) + others
}
