package com.humboshot.koillection.destinations

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface AppDestination {
    val icon: ImageVector
    val route: String
    val title: String
}

const val TITLE_ARG = "title"

/** The titles of the pages above this one, for the breadcrumb. */
const val TRAIL_ARG = "trail"
private const val TRAIL_SEPARATOR = "\u001F"

private val trailArgument: NamedNavArgument =
    navArgument(TRAIL_ARG) { type = NavType.StringType; defaultValue = "" }

fun NavBackStackEntry.pageTitle(): String =
    if (destination.route == MainCollection.route) MainCollection.title
    else arguments?.getString(TITLE_ARG).orEmpty()

/** The breadcrumb of this page: the titles above it, then its own title. */
fun NavBackStackEntry.breadcrumb(): List<String> {
    val trail = arguments?.getString(TRAIL_ARG).orEmpty()
    return (if (trail.isEmpty()) emptyList() else trail.split(TRAIL_SEPARATOR)) + pageTitle()
}

/** Adds the breadcrumb of the page we come from to a route, so the new page can show the full path. */
fun withTrail(route: String, from: NavBackStackEntry?): String {
    val trail = from?.breadcrumb().orEmpty().joinToString(TRAIL_SEPARATOR)
    val separator = if ('?' in route) '&' else '?'
    return "$route$separator$TRAIL_ARG=${Uri.encode(trail)}"
}

object MainCollection : AppDestination {
    override val icon: ImageVector = Icons.Default.Menu
    override val route: String = "main_collection"
    override val title: String = "Collections"
}

object CollectionDetail : AppDestination {
    const val ID_ARG = "collectionId"

    override val icon: ImageVector = Icons.AutoMirrored.Filled.List
    override val route: String = "collection/{$ID_ARG}?$TITLE_ARG={$TITLE_ARG}&$TRAIL_ARG={$TRAIL_ARG}"
    override val title: String = "Collection"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(ID_ARG) { type = NavType.StringType },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
        trailArgument,
    )

    fun createRoute(id: String, title: String): String = "collection/$id?$TITLE_ARG=${Uri.encode(title)}"
}

object ItemDetail : AppDestination {
    const val ID_ARG = "itemId"

    override val icon: ImageVector = Icons.Default.Info
    override val route: String = "item/{$ID_ARG}?$TITLE_ARG={$TITLE_ARG}&$TRAIL_ARG={$TRAIL_ARG}"
    override val title: String = "Item"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(ID_ARG) { type = NavType.StringType },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
        trailArgument,
    )

    fun createRoute(id: String, title: String): String = "item/$id?$TITLE_ARG=${Uri.encode(title)}"
}

object ItemForm : AppDestination {
    const val ITEM_ID_ARG = "itemId"
    const val COLLECTION_ID_ARG = "collectionId"

    override val icon: ImageVector = Icons.Default.Edit
    override val route: String =
        "item_form?$ITEM_ID_ARG={$ITEM_ID_ARG}&$COLLECTION_ID_ARG={$COLLECTION_ID_ARG}&$TITLE_ARG={$TITLE_ARG}&$TRAIL_ARG={$TRAIL_ARG}"
    override val title: String = "New item"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(ITEM_ID_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(COLLECTION_ID_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
        trailArgument,
    )

    fun createRoute(collectionId: String): String = "item_form?$COLLECTION_ID_ARG=$collectionId"

    fun editRoute(itemId: String): String = "item_form?$ITEM_ID_ARG=$itemId&$TITLE_ARG=${Uri.encode("Edit item")}"
}

object CollectionForm : AppDestination {
    const val COLLECTION_ID_ARG = "collectionId"
    const val PARENT_ID_ARG = "parentId"

    override val icon: ImageVector = Icons.Default.Edit
    override val route: String =
        "collection_form?$COLLECTION_ID_ARG={$COLLECTION_ID_ARG}&$PARENT_ID_ARG={$PARENT_ID_ARG}&$TITLE_ARG={$TITLE_ARG}&$TRAIL_ARG={$TRAIL_ARG}"
    override val title: String = "New collection"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(COLLECTION_ID_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(PARENT_ID_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
        trailArgument,
    )

    fun createRoute(parentId: String? = null): String =
        if (parentId == null) "collection_form" else "collection_form?$PARENT_ID_ARG=$parentId"

    fun editRoute(collectionId: String): String =
        "collection_form?$COLLECTION_ID_ARG=$collectionId&$TITLE_ARG=${Uri.encode("Edit collection")}"
}
