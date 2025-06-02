package com.humboshot.koillection.ui.main_collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.humboshot.koillection.UserContext

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
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .addHeader("Authorization", "Bearer ${UserContext().jwt}")
                                .data(buildUrl(item.image))
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )

                    Text("Collection: ${item.title}")
                }
            }
        }
    }
}

private fun buildUrl(imagePath: String?): String {
    if (imagePath == null || imagePath.isEmpty()) return ""
    val url = UserContext().baseAddress + "/" + imagePath
    return url
}