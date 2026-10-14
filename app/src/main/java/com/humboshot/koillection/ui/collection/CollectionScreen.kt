package com.humboshot.koillection.ui.collection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    onCollectionClick: (KCollection) -> Unit,
    viewModel: CollectionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.getCollections() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            state.error?.let { error ->
                item {
                    ErrorState(error, onRetry = { viewModel.getCollections() })
                }
            }

            if (state.collections.isEmpty() && !state.isLoading && state.error == null) {
                item {
                    EmptyState("No collections found")
                }
            }

            items(state.collections, key = { it.id }) { collection ->
                CollectionRow(collection, onClick = { onCollectionClick(collection) })
            }
        }
    }
}
