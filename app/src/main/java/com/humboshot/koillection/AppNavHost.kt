package com.humboshot.koillection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.destinations.CollectionDetail
import com.humboshot.koillection.destinations.CollectionForm
import com.humboshot.koillection.destinations.ItemDetail
import com.humboshot.koillection.destinations.ItemForm
import com.humboshot.koillection.destinations.MainCollection
import com.humboshot.koillection.destinations.withTrail
import com.humboshot.koillection.ui.collection.CollectionDetailScreen
import com.humboshot.koillection.ui.collection.CollectionDetailViewModel
import com.humboshot.koillection.ui.collection.CollectionFormScreen
import com.humboshot.koillection.ui.collection.CollectionScreen
import com.humboshot.koillection.ui.collection.CollectionViewModel
import com.humboshot.koillection.ui.common.RefreshOnResult
import com.humboshot.koillection.ui.common.markPreviousForRefresh
import com.humboshot.koillection.ui.common.popWithRefresh
import com.humboshot.koillection.ui.item.ItemFormScreen
import com.humboshot.koillection.ui.item.ItemScreen
import com.humboshot.koillection.ui.item.ItemViewModel

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val openCollection: (KCollection) -> Unit = { collection ->
        navHostController.navigateDown(CollectionDetail.createRoute(collection.id, collection.title))
    }
    val openItem: (Item) -> Unit = { item ->
        navHostController.navigateDown(ItemDetail.createRoute(item.id, item.name))
    }

    NavHost(
        navController = navHostController,
        startDestination = MainCollection.route,
        modifier
    ) {
        composable(route = MainCollection.route) { entry ->
            val viewModel: CollectionViewModel = viewModel()
            RefreshOnResult(entry, viewModel::getCollections)
            CollectionScreen(
                onCollectionClick = openCollection,
                onAddCollection = { navHostController.navigateDown(CollectionForm.createRoute()) },
                viewModel = viewModel
            )
        }
        composable(route = CollectionDetail.route, arguments = CollectionDetail.arguments) { entry ->
            val viewModel: CollectionDetailViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            CollectionDetailScreen(
                onCollectionClick = openCollection,
                onItemClick = openItem,
                onAddItem = { collectionId -> navHostController.navigateDown(ItemForm.createRoute(collectionId)) },
                onAddSubCollection = { parentId -> navHostController.navigateDown(CollectionForm.createRoute(parentId)) },
                onEdit = { collectionId -> navHostController.navigateDown(CollectionForm.editRoute(collectionId)) },
                onDeleted = { navHostController.popWithRefresh() },
                viewModel = viewModel
            )
        }
        composable(route = ItemDetail.route, arguments = ItemDetail.arguments) { entry ->
            val viewModel: ItemViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            ItemScreen(
                onItemClick = openItem,
                onEdit = { item -> navHostController.navigateDown(ItemForm.editRoute(item.id)) },
                onDeleted = { navHostController.popWithRefresh() },
                viewModel = viewModel
            )
        }
        composable(route = ItemForm.route, arguments = ItemForm.arguments) {
            ItemFormScreen(
                onSaved = { navHostController.popWithRefresh() },
                onImageChanged = { navHostController.markPreviousForRefresh() }
            )
        }
        composable(route = CollectionForm.route, arguments = CollectionForm.arguments) {
            CollectionFormScreen(
                onSaved = { navHostController.popWithRefresh() },
                onImageChanged = { navHostController.markPreviousForRefresh() }
            )
        }
    }
}

private fun NavHostController.navigateDown(route: String) = navigate(withTrail(route, currentBackStackEntry))
