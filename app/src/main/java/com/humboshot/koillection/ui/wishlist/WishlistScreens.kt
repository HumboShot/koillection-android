package com.humboshot.koillection.ui.wishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.repository.WishlistRepository
import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.Wishlist
import com.humboshot.koillection.destinations.WishlistDetail
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.KoillectionImage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WishlistListViewModel : ViewModel() {
    private val _state = MutableStateFlow(WishlistListViewState())
    val state: StateFlow<WishlistListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            WishlistRepository.getRootWishlists()
                .onSuccess { wishlists -> _state.update { it.copy(isLoading = false, wishlists = wishlists) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load wishlists") }
                }
        }
    }
}

data class WishlistListViewState(
    val isLoading: Boolean = false,
    val wishlists: List<Wishlist> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistListScreen(
    onWishlistClick: (Wishlist) -> Unit,
    viewModel: WishlistListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            if (state.wishlists.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No wishlists found") }
            }
            items(state.wishlists, key = { it.id }) { wishlist ->
                ImageRow(image = wishlist.image, title = wishlist.name, subtitle = null) { onWishlistClick(wishlist) }
            }
        }
    }
}

class WishlistDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val wishlistId: String = checkNotNull(savedStateHandle[WishlistDetail.ID_ARG])

    private val _state = MutableStateFlow(WishlistDetailViewState())
    val state: StateFlow<WishlistDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val wishlist = async { WishlistRepository.getWishlist(wishlistId) }
            val children = async { WishlistRepository.getChildren(wishlistId) }
            val wishes = async { WishlistRepository.getWishes(wishlistId) }

            val error = listOf(wishlist.await(), children.await(), wishes.await()).firstNotNullOfOrNull { it.exceptionOrNull() }
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load wishlist") }
                return@launch
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    wishlist = wishlist.await().getOrThrow(),
                    children = children.await().getOrThrow(),
                    wishes = wishes.await().getOrThrow(),
                )
            }
        }
    }
}

data class WishlistDetailViewState(
    val isLoading: Boolean = false,
    val wishlist: Wishlist? = null,
    val children: List<Wishlist> = emptyList(),
    val wishes: List<Wish> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    onWishlistClick: (Wishlist) -> Unit,
    onWishClick: (Wish) -> Unit,
    viewModel: WishlistDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            val wishlist = state.wishlist ?: return@LazyColumn

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (wishlist.image != null) {
                        KoillectionImage(
                            path = wishlist.image,
                            contentDescription = wishlist.name,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Column {
                        Text(wishlist.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = if (state.wishes.size == 1) "1 wish" else "${state.wishes.size} wishes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.children.isNotEmpty()) {
                item { SectionTitle("Wishlists") }
                items(state.children, key = { "list-${it.id}" }) { child ->
                    ImageRow(image = child.image, title = child.name, subtitle = null) { onWishlistClick(child) }
                }
            }

            if (state.wishes.isNotEmpty()) {
                item { SectionTitle("Wishes") }
                items(state.wishes, key = { "wish-${it.id}" }) { wish ->
                    ImageRow(
                        image = wish.imageSmallThumbnail ?: wish.image,
                        title = wish.name,
                        subtitle = wish.priceText.ifEmpty { null }
                    ) { onWishClick(wish) }
                }
            }

            if (!state.isLoading && state.children.isEmpty() && state.wishes.isEmpty()) {
                item { EmptyState("This wishlist is empty") }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun ImageRow(image: String?, title: String, subtitle: String?, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        KoillectionImage(
            path = image,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
