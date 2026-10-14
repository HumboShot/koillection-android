package com.humboshot.koillection.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.common.TopBarActions
import com.humboshot.koillection.ui.item.DatumRow
import com.humboshot.koillection.ui.item.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    onCollectionClick: (KCollection) -> Unit,
    onItemClick: (Item) -> Unit,
    onAddItem: (collectionId: String) -> Unit,
    onAddSubCollection: (parentId: String) -> Unit,
    onEdit: (collectionId: String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: CollectionDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    state.collection?.let { collection ->
        TopBarActions {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More actions")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("New sub-collection") },
                    onClick = {
                        menuOpen = false
                        onAddSubCollection(collection.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit collection") },
                    onClick = {
                        menuOpen = false
                        onEdit(collection.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete collection") },
                    onClick = {
                        menuOpen = false
                        confirmDelete = true
                    }
                )
            }
        }
    }
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= gridState.layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore) viewModel.loadMoreItems()
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            state = gridState,
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let { error ->
                fullWidth { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }

            state.collection?.let { collection ->
                fullWidth { CollectionHeader(collection, state.totalItems ?: state.items.size) }
            }

            if (state.data.isNotEmpty()) {
                fullWidth {
                    Column(Modifier.padding(horizontal = 4.dp)) {
                        state.data.forEach { DatumRow(it) }
                    }
                }
            }

            if (state.children.isNotEmpty()) {
                fullWidth { SectionTitle("Collections") }
                state.children.forEach { child ->
                    fullWidth {
                        CollectionRow(
                            collection = child,
                            onClick = { onCollectionClick(child) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (state.items.isNotEmpty()) {
                fullWidth { SectionTitle("Items") }
                items(state.items, key = { it.id }) { item ->
                    ItemCard(item = item, onClick = { onItemClick(item) })
                }
            }

            if (state.isLoadingMoreItems) {
                fullWidth {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!state.isLoading && state.error == null && state.collection != null &&
                state.children.isEmpty() && state.items.isEmpty()
            ) {
                fullWidth { EmptyState("This collection is empty") }
            }
        }

        state.collection?.let { collection ->
            FloatingActionButton(
                onClick = { onAddItem(collection.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete collection?") },
            text = {
                Text(
                    "This deletes \"${state.collection?.title}\" with all its sub-collections and items. " +
                        "You cannot undo this."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun CollectionHeader(collection: KCollection, itemCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KoillectionImage(
            path = collection.image,
            contentDescription = collection.title,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Column {
            Text(collection.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = if (itemCount == 1) "1 item" else "$itemCount items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 16.dp)
    )
}
