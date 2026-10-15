package com.humboshot.koillection.ui.album

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
import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.repository.AlbumRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.destinations.AlbumForm
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.VisibilityPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val albumId: String? = savedStateHandle[AlbumForm.ALBUM_ID_ARG]
    private val parentId: String? = savedStateHandle[AlbumForm.PARENT_ID_ARG]
    private var original: Album? = null

    private val _state = MutableStateFlow(AlbumFormViewState(isEditRoute = albumId != null))
    val state: StateFlow<AlbumFormViewState> = _state.asStateFlow()

    init {
        if (albumId != null) load(albumId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            AlbumRepository.getAlbum(id)
                .onSuccess { album ->
                    original = album
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = AlbumFormFields.from(album),
                            imagePath = album.image,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load album") }
                }
        }
    }

    fun onFieldsChange(fields: AlbumFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> AlbumRepository.uploadImage(target.id, part) }, { Result.failure(it) })
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

        val album = original
        val patch = album?.let { current.fields.toPatch(it) }
        if (album != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (album != null) {
                AlbumRepository.updateAlbum(album.id, checkNotNull(patch))
            } else {
                AlbumRepository.createAlbum(current.fields.toCreate(parentId))
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save album") }
                }
        }
    }
}

data class AlbumFormViewState(
    val fields: AlbumFormFields = AlbumFormFields(),
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
    /** An edit form may only save after the album loaded. Otherwise Save would create a new album. */
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun AlbumFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    viewModel: AlbumFormViewModel = viewModel()
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
            value = fields.title,
            onValueChange = { viewModel.onFieldsChange(fields.copy(title = it)) },
            label = { Text("Title") },
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
