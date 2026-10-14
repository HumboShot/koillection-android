package com.humboshot.koillection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.destinations.CollectionDetail
import com.humboshot.koillection.destinations.ItemDetail
import com.humboshot.koillection.destinations.MainCollection
import com.humboshot.koillection.ui.collection.CollectionDetailScreen
import com.humboshot.koillection.ui.collection.CollectionScreen
import com.humboshot.koillection.ui.item.ItemScreen

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val openCollection: (KCollection) -> Unit = { collection ->
        navHostController.navigate(CollectionDetail.createRoute(collection.id, collection.title))
    }
    val openItem: (Item) -> Unit = { item ->
        navHostController.navigate(ItemDetail.createRoute(item.id, item.name))
    }

    NavHost(
        navController = navHostController,
        startDestination = MainCollection.route,
        modifier
    ) {
        composable(route = MainCollection.route) {
            CollectionScreen(onCollectionClick = openCollection)
        }
        composable(route = CollectionDetail.route, arguments = CollectionDetail.arguments) {
            CollectionDetailScreen(onCollectionClick = openCollection, onItemClick = openItem)
        }
        composable(route = ItemDetail.route, arguments = ItemDetail.arguments) {
            ItemScreen(onItemClick = openItem)
        }
    }
}
