package com.humboshot.koillection.ui.wishlist

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.repository.WishlistRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.data.wishlist.Wishlist
import com.humboshot.koillection.destinations.WishlistForm
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.VisibilityPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val wishlistId: String? = savedStateHandle[WishlistForm.WISHLIST_ID_ARG]
    private val parentId: String? = savedStateHandle[WishlistForm.PARENT_ID_ARG]
    private var original: Wishlist? = null

    private val _state = MutableStateFlow(WishlistFormViewState(isEditRoute = wishlistId != null))
    val state: StateFlow<WishlistFormViewState> = _state.asStateFlow()

    init {
        if (wishlistId != null) load(wishlistId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            WishlistRepository.getWishlist(id)
                .onSuccess { wishlist ->
                    original = wishlist
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = WishlistFormFields.from(wishlist),
                            imagePath = wishlist.image,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load wishlist") }
                }
        }
    }

    fun onFieldsChange(fields: WishlistFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> WishlistRepository.uploadImage(target.id, part) }, { Result.failure(it) })
                .onSuccess { updated ->
                    original = updated
                    _state.update { it.copy(isUploading = false, imagePath = updated.image, imageChanged = true) }
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

        val wishlist = original
        val patch = wishlist?.let { current.fields.toPatch(it) }
        if (wishlist != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (wishlist != null) {
                WishlistRepository.updateWishlist(wishlist.id, checkNotNull(patch))
            } else {
                WishlistRepository.createWishlist(current.fields.toCreate(parentId))
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save wishlist") }
                }
        }
    }
}

data class WishlistFormViewState(
    val fields: WishlistFormFields = WishlistFormFields(),
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
    /** An edit form may only save after the wishlist loaded. Otherwise Save would create a new wishlist. */
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun WishlistFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    viewModel: WishlistFormViewModel = viewModel()
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
