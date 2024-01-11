package com.humboshot.koillection.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

interface AppDestination {
    val icon: ImageVector
    val route: String
    val title: String
}

object MainCollection : AppDestination {
    override val icon: ImageVector = Icons.Default.Menu
    override val route: String = "main_collection"
    override val title: String = "Collection"
}

object SubCollection : AppDestination {
    override val icon: ImageVector = Icons.AutoMirrored.Filled.List
    override val route: String = "sub_collection"
    override val title: String = "{collection_name}"

}