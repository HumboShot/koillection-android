package com.humboshot.koillection.ui.tag

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.item.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDetailScreen(
    onItemClick: (Item) -> Unit,
    viewModel: TagDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let { error ->
                fullWidth { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            state.tag?.let { tag ->
                fullWidth { TagHeader(tag, state.category, state.totalItems ?: state.items.size) }
            }
            items(state.items, key = { it.id }) { item ->
                ItemCard(item = item, onClick = { onItemClick(item) })
            }
            if (state.isLoadingMoreItems) {
                fullWidth {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            if (!state.isLoading && state.error == null && state.tag != null && state.items.isEmpty()) {
                fullWidth { EmptyState("No items have this tag") }
            }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun TagHeader(tag: Tag, category: TagCategory?, itemCount: Int) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (tag.image != null || tag.imageSmallThumbnail != null) {
                KoillectionImage(
                    path = tag.image ?: tag.imageSmallThumbnail,
                    contentDescription = tag.label,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Column {
                Text(tag.label, style = MaterialTheme.typography.headlineSmall)
                category?.let { CategoryLabel(it) }
                Text(
                    text = if (itemCount == 1) "1 item" else "$itemCount items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        tag.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** The category name, with a dot in the category color when it has a valid hex color. */
@Composable
fun CategoryLabel(category: TagCategory) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        parseHexColor(category.color)?.let { color ->
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            text = category.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
