package com.humboshot.koillection.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.repository.AlbumRepository
import com.humboshot.koillection.destinations.AlbumDetail
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.common.TopBarActions
import com.humboshot.koillection.ui.common.formatApiDate
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumListViewModel : ViewModel() {
    private val _state = MutableStateFlow(AlbumListViewState())
    val state: StateFlow<AlbumListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            AlbumRepository.getRootAlbums()
                .onSuccess { albums -> _state.update { it.copy(isLoading = false, albums = albums) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load albums") }
                }
        }
    }
}

data class AlbumListViewState(
    val isLoading: Boolean = false,
    val albums: List<Album> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(
    onAlbumClick: (Album) -> Unit,
    onAdd: () -> Unit,
    viewModel: AlbumListViewModel = viewModel()
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
            if (state.albums.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No albums found") }
            }
            items(state.albums, key = { it.id }) { album ->
                AlbumRow(album, onClick = { onAlbumClick(album) })
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add album")
        }
    }
}

class AlbumDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val albumId: String = checkNotNull(savedStateHandle[AlbumDetail.ID_ARG])

    private val _state = MutableStateFlow(AlbumDetailViewState())
    val state: StateFlow<AlbumDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val album = async { AlbumRepository.getAlbum(albumId) }
            val children = async { AlbumRepository.getChildren(albumId) }
            val photos = async { AlbumRepository.getPhotos(albumId) }

            val error = listOf(album.await(), children.await(), photos.await()).firstNotNullOfOrNull { it.exceptionOrNull() }
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load album") }
                return@launch
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    album = album.await().getOrThrow(),
                    children = children.await().getOrThrow(),
                    photos = photos.await().getOrThrow(),
                )
            }
        }
    }

    fun openPhoto(photo: Photo?) = _state.update { it.copy(openPhoto = photo) }

    fun delete() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            AlbumRepository.deleteAlbum(albumId)
                .onSuccess { _state.update { it.copy(isLoading = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to delete album") }
                }
        }
    }
}

data class AlbumDetailViewState(
    val isLoading: Boolean = false,
    val album: Album? = null,
    val children: List<Album> = emptyList(),
    val photos: List<Photo> = emptyList(),
    val error: String? = null,
    /** The photo in the full-screen view, or null. */
    val openPhoto: Photo? = null,
    val isDeleted: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onAlbumClick: (Album) -> Unit,
    onAddSubAlbum: (parentId: String) -> Unit,
    onEdit: (albumId: String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: AlbumDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    state.album?.let { album ->
        TopBarActions {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More actions")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("New sub-album") },
                    onClick = {
                        menuOpen = false
                        onAddSubAlbum(album.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit album") },
                    onClick = {
                        menuOpen = false
                        onEdit(album.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete album") },
                    onClick = {
                        menuOpen = false
                        confirmDelete = true
                    }
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete album?") },
            text = { Text("This deletes \"${state.album?.title}\" with all its sub-albums and photos. You cannot undo this.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            state.error?.let { error ->
                fullWidth { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            val album = state.album ?: return@LazyVerticalGrid

            fullWidth {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp)
                )
            }
            if (state.children.isNotEmpty()) {
                fullWidth { SectionTitle("Albums") }
                state.children.forEach { child ->
                    fullWidth { AlbumRow(child, onClick = { onAlbumClick(child) }) }
                }
            }
            if (state.photos.isNotEmpty()) {
                fullWidth { SectionTitle("Photos") }
                items(state.photos, key = { it.id }) { photo ->
                    KoillectionImage(
                        path = photo.imageSmallThumbnail ?: photo.image,
                        contentDescription = photo.title,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openPhoto(photo) }
                    )
                }
            }
            if (!state.isLoading && state.children.isEmpty() && state.photos.isEmpty()) {
                fullWidth { EmptyState("This album is empty") }
            }
        }
    }

    state.openPhoto?.let { photo ->
        PhotoViewer(photo, onDismiss = { viewModel.openPhoto(null) })
    }
}

/** The photo on a black background, with its title, comment, place and date at the bottom. Tap to close. */
@Composable
fun PhotoViewer(photo: Photo, onDismiss: () -> Unit, actions: @Composable () -> Unit = {}) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            KoillectionImage(
                path = photo.image ?: photo.imageSmallThumbnail,
                contentDescription = photo.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(photo.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                photo.comment?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
                val details = listOfNotNull(
                    photo.place?.takeIf { it.isNotBlank() },
                    photo.takenAt?.let { formatApiDate(it) }?.takeIf { it.isNotBlank() },
                )
                if (details.isNotEmpty()) {
                    Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
                actions()
            }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AlbumRow(album: Album, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        KoillectionImage(
            path = album.image,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(album.title, style = MaterialTheme.typography.titleMedium)
    }
}
