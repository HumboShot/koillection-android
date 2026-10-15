package com.humboshot.koillection.ui.wishlist

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.repository.WishlistRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.destinations.WishDetail
import com.humboshot.koillection.destinations.WishForm
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.common.TopBarActions
import com.humboshot.koillection.ui.common.VisibilityPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val wishId: String = checkNotNull(savedStateHandle[WishDetail.ID_ARG])

    private val _state = MutableStateFlow(WishDetailViewState())
    val state: StateFlow<WishDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            WishlistRepository.getWish(wishId)
                .onSuccess { wish -> _state.update { it.copy(isLoading = false, wish = wish) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load wish") }
                }
        }
    }

    fun delete() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            WishlistRepository.deleteWish(wishId)
                .onSuccess { _state.update { it.copy(isLoading = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to delete wish") }
                }
        }
    }
}

data class WishDetailViewState(
    val isLoading: Boolean = false,
    val wish: Wish? = null,
    val error: String? = null,
    val isDeleted: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishDetailScreen(
    onEdit: (Wish) -> Unit,
    onDeleted: () -> Unit,
    viewModel: WishDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    var confirmDelete by remember { mutableStateOf(false) }
    var linkError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    state.wish?.let { wish ->
        TopBarActions {
            IconButton(onClick = { onEdit(wish) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit wish")
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete wish")
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            state.error?.let { ErrorState(it, onRetry = { viewModel.refresh() }) }
            val wish = state.wish ?: return@Column

            if (wish.image != null) {
                KoillectionImage(
                    path = wish.image,
                    contentDescription = wish.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
                Text(wish.name, style = MaterialTheme.typography.headlineSmall)
                if (wish.priceText.isNotEmpty()) {
                    Text(wish.priceText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                wish.comment?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                wish.url?.takeIf { it.isNotBlank() }?.let { url ->
                    val link = openableLink(url)
                    Text(
                        text = link?.toUri()?.host ?: url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        linkError = if (link == null || runCatching { uriHandler.openUri(link) }.isFailure) {
                            "Cannot open the link."
                        } else {
                            null
                        }
                    }) { Text("Open link") }
                    linkError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete wish?") },
            text = { Text("This deletes \"${state.wish?.name}\". You cannot undo this.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

class WishFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val wishId: String? = savedStateHandle[WishForm.WISH_ID_ARG]
    private val wishlistId: String? = savedStateHandle[WishForm.WISHLIST_ID_ARG]
    private var original: Wish? = null

    private val _state = MutableStateFlow(WishFormViewState(isEditRoute = wishId != null))
    val state: StateFlow<WishFormViewState> = _state.asStateFlow()

    init {
        if (wishId != null) load(wishId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            WishlistRepository.getWish(id)
                .onSuccess { wish ->
                    original = wish
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = WishFormFields.from(wish),
                            imagePath = wish.imageSmallThumbnail ?: wish.image,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load wish") }
                }
        }
    }

    fun onFieldsChange(fields: WishFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> WishlistRepository.uploadWishImage(target.id, part) }, { Result.failure(it) })
                .onSuccess { updated ->
                    original = updated
                    _state.update {
                        it.copy(isUploading = false, imagePath = updated.imageSmallThumbnail ?: updated.image, imageChanged = true)
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isUploading = false, error = throwable.message ?: "Failed to upload image") }
                }
        }
    }

    fun save() {
        val current = _state.value
        if (current.isSaving || !current.canSave) return
        current.fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val wish = original
        if (wish != null && current.fields.clearsPriceOrCurrency(wish)) {
            _state.update { it.copy(error = "A price or currency cannot be removed. Enter a value.") }
            return
        }
        val patch = wish?.let { current.fields.toPatch(it) }
        if (wish != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (wish != null) {
                WishlistRepository.updateWish(wish.id, checkNotNull(patch))
            } else {
                WishlistRepository.createWish(current.fields.toCreate(checkNotNull(wishlistId)))
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save wish") }
                }
        }
    }
}

data class WishFormViewState(
    val fields: WishFormFields = WishFormFields(),
    val isLoading: Boolean = false,
    val isEditRoute: Boolean = false,
    val isEditing: Boolean = false,
    val imagePath: String? = null,
    val isUploading: Boolean = false,
    val imageChanged: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    /** An edit form may only save after the wish loaded. Otherwise Save would try to create a wish. */
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun WishFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    viewModel: WishFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fields = state.fields
    val contentResolver = LocalContext.current.contentResolver

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }
    LaunchedEffect(state.imageChanged) {
        if (state.imageChanged) onImageChanged()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (state.isEditing) {
            ImagePickerField(
                path = state.imagePath,
                isUploading = state.isUploading,
                onPick = { uri -> viewModel.uploadImage(contentResolver, uri) }
            )
        }
        OutlinedTextField(
            value = fields.name,
            onValueChange = { viewModel.onFieldsChange(fields.copy(name = it)) },
            label = { Text("Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fields.url,
            onValueChange = { viewModel.onFieldsChange(fields.copy(url = it)) },
            label = { Text("Link") },
            placeholder = { Text("https://") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = fields.price,
                onValueChange = { viewModel.onFieldsChange(fields.copy(price = it)) },
                label = { Text("Price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(2f)
            )
            OutlinedTextField(
                value = fields.currency,
                onValueChange = { viewModel.onFieldsChange(fields.copy(currency = it.filter(Char::isLetter).take(3))) },
                label = { Text("Currency") },
                placeholder = { Text("EUR") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = fields.comment,
            onValueChange = { viewModel.onFieldsChange(fields.copy(comment = it)) },
            label = { Text("Comment") },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Visibility", style = MaterialTheme.typography.labelLarge)
        VisibilityPicker(
            selected = fields.visibility,
            onSelect = { viewModel.onFieldsChange(fields.copy(visibility = it)) },
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving && !state.isLoading && !state.isUploading && state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save")
        }
    }
}
