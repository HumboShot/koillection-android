package com.humboshot.koillection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.user.Feature
import com.humboshot.koillection.destinations.ChoiceListForm
import com.humboshot.koillection.destinations.ChoiceListList
import com.humboshot.koillection.destinations.CollectionDetail
import com.humboshot.koillection.destinations.CollectionForm
import com.humboshot.koillection.destinations.ItemDetail
import com.humboshot.koillection.destinations.ItemForm
import com.humboshot.koillection.destinations.LoanList
import com.humboshot.koillection.destinations.MainCollection
import com.humboshot.koillection.destinations.Statistics
import com.humboshot.koillection.destinations.TagDetail
import com.humboshot.koillection.destinations.TagForm
import com.humboshot.koillection.destinations.TagList
import com.humboshot.koillection.destinations.TemplateDetail
import com.humboshot.koillection.destinations.TemplateList
import com.humboshot.koillection.destinations.withTrail
import com.humboshot.koillection.extension.iriId
import com.humboshot.koillection.ui.choicelist.ChoiceListFormScreen
import com.humboshot.koillection.ui.choicelist.ChoiceListListScreen
import com.humboshot.koillection.ui.choicelist.ChoiceListListViewModel
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
import com.humboshot.koillection.ui.loan.LoanListScreen
import com.humboshot.koillection.ui.loan.LoanListViewModel
import com.humboshot.koillection.ui.statistics.StatisticsScreen
import com.humboshot.koillection.ui.statistics.visibleStatistics
import com.humboshot.koillection.ui.tag.TagDetailScreen
import com.humboshot.koillection.ui.tag.TagDetailViewModel
import com.humboshot.koillection.ui.tag.TagFormScreen
import com.humboshot.koillection.ui.tag.TagListScreen
import com.humboshot.koillection.ui.tag.TagListViewModel
import com.humboshot.koillection.ui.template.TemplateDetailScreen
import com.humboshot.koillection.ui.template.TemplateListScreen
import com.humboshot.koillection.ui.template.TemplateListViewModel

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    appState: AppState,
    features: Set<Feature>?,
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
                showLoans = features == null || Feature.LOANS in features,
                onLoansChanged = { navHostController.markPreviousForRefresh() },
                viewModel = viewModel
            )
        }
        composable(route = TagList.route, arguments = TagList.arguments) { entry ->
            val viewModel: TagListViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            TagListScreen(
                onTagClick = { navHostController.navigateDown(TagDetail.createRoute(it.id, it.label)) },
                onAdd = { navHostController.navigateDown(TagForm.createRoute()) },
                viewModel = viewModel
            )
        }
        composable(route = TagDetail.route, arguments = TagDetail.arguments) { entry ->
            val viewModel: TagDetailViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            TagDetailScreen(
                onItemClick = openItem,
                onEdit = { navHostController.navigateDown(TagForm.editRoute(it.id)) },
                onDeleted = { navHostController.popWithRefresh() },
                viewModel = viewModel
            )
        }
        composable(route = TagForm.route, arguments = TagForm.arguments) {
            TagFormScreen(
                onSaved = { navHostController.popWithRefresh() },
                onImageChanged = { navHostController.markPreviousForRefresh() }
            )
        }
        composable(route = LoanList.route, arguments = LoanList.arguments) { entry ->
            val viewModel: LoanListViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            LoanListScreen(
                onLoanClick = { loan, itemName ->
                    loan.item?.let { navHostController.navigateDown(ItemDetail.createRoute(it.iriId(), itemName)) }
                },
                viewModel = viewModel
            )
        }
        composable(route = ChoiceListList.route, arguments = ChoiceListList.arguments) { entry ->
            val viewModel: ChoiceListListViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            ChoiceListListScreen(
                onChoiceListClick = { navHostController.navigateDown(ChoiceListForm.editRoute(it.id, it.name)) },
                onAdd = { navHostController.navigateDown(ChoiceListForm.createRoute()) },
                viewModel = viewModel
            )
        }
        composable(route = ChoiceListForm.route, arguments = ChoiceListForm.arguments) {
            ChoiceListFormScreen(onDone = { navHostController.popWithRefresh() })
        }
        composable(route = TemplateList.route, arguments = TemplateList.arguments) { entry ->
            val viewModel: TemplateListViewModel = viewModel()
            RefreshOnResult(entry, viewModel::refresh)
            TemplateListScreen(
                onTemplateClick = { navHostController.navigateDown(TemplateDetail.createRoute(it.id, it.name)) },
                viewModel = viewModel
            )
        }
        composable(route = TemplateDetail.route, arguments = TemplateDetail.arguments) {
            TemplateDetailScreen(
                onDeleted = { navHostController.popWithRefresh() },
                onChanged = { navHostController.markPreviousForRefresh() }
            )
        }
        composable(route = Statistics.route, arguments = Statistics.arguments) {
            StatisticsScreen(statistics = visibleStatistics(features))
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
