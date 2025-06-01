package com.humboshot.koillection.ui.main_collection

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCollectionScreen(viewModel: MainCollectionViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.getCollections() }
    ) {
        LazyColumn {
            if (state.members.isEmpty()) {
                item {
                    Text("No collections found")
                }
            }

            items(state.members.size) { index ->
                val item = state.members[index]
                Text("Collection: ${item.title}")
            }
        }
    }
}