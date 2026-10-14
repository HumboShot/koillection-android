package com.humboshot.koillection.destinations

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface AppDestination {
    val icon: ImageVector
    val route: String
    val title: String
}

const val TITLE_ARG = "title"

object MainCollection : AppDestination {
    override val icon: ImageVector = Icons.Default.Menu
    override val route: String = "main_collection"
    override val title: String = "Collections"
}

object CollectionDetail : AppDestination {
    const val ID_ARG = "collectionId"

    override val icon: ImageVector = Icons.AutoMirrored.Filled.List
    override val route: String = "collection/{$ID_ARG}?$TITLE_ARG={$TITLE_ARG}"
    override val title: String = "Collection"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(ID_ARG) { type = NavType.StringType },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
    )

    fun createRoute(id: String, title: String): String = "collection/$id?$TITLE_ARG=${Uri.encode(title)}"
}

object ItemDetail : AppDestination {
    const val ID_ARG = "itemId"

    override val icon: ImageVector = Icons.Default.Info
    override val route: String = "item/{$ID_ARG}?$TITLE_ARG={$TITLE_ARG}"
    override val title: String = "Item"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(ID_ARG) { type = NavType.StringType },
        navArgument(TITLE_ARG) { type = NavType.StringType; defaultValue = title },
    )

    fun createRoute(id: String, title: String): String = "item/$id?$TITLE_ARG=${Uri.encode(title)}"
}
