package com.humboshot.koillection.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.humboshot.koillection.R
import com.humboshot.koillection.data.user.Feature

/**
 * The side menu entries, in the order of the web sidebar. Each feature screen adds its entry to [others].
 *
 * - [features] is null when the user is not loaded yet. Then every entry shows.
 * - [open] goes back to the Collections root and then opens the given route. A null route opens Collections.
 * - An entry is selected when the current route starts with one of its route prefixes.
 */
@Suppress("UNUSED_PARAMETER")
fun drawerEntries(
    features: Set<Feature>?,
    currentRoute: String?,
    open: (route: String?) -> Unit,
): List<DrawerEntry> {
    val others = listOf<DrawerEntry>()

    val collections = DrawerEntry(
        label = "Collections",
        icon = { Icon(painterResource(R.drawable.ic_collections), contentDescription = null) },
        selected = others.none { it.selected },
        onClick = { open(null) },
    )
    return listOf(collections) + others
}
