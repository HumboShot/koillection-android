package com.humboshot.koillection.ui.album

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.repository.AlbumRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.destinations.PhotoForm
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.VisibilityPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val photoId: String? = savedStateHandle[PhotoForm.PHOTO_ID_ARG]
    private val albumId: String? = savedStateHandle[PhotoForm.ALBUM_ID_ARG]
    private var original: Photo? = null

    private val _state = MutableStateFlow(PhotoFormViewState(isEditRoute = photoId != null))
    val state: StateFlow<PhotoFormViewState> = _state.asStateFlow()

    init {
        if (photoId != null) load(photoId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            AlbumRepository.getPhoto(id)
                .onSuccess { photo -> showSaved(photo) }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load photo") }
                }
        }
    }

    private fun showSaved(photo: Photo) {
        original = photo
        _state.update {
            it.copy(
                isLoading = false,
                isEditing = true,
                fields = PhotoFormFields.from(photo),
                imagePath = photo.imageSmallThumbnail ?: photo.image,
            )
        }
    }

    fun onFieldsChange(fields: PhotoFormFields) = _state.update { it.copy(fields = fields, error = null) }

    /** In create mode the image waits until the photo exists. */
    fun onImagePicked(uri: Uri) = _state.update { it.copy(pendingImage = uri, error = null) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            upload(contentResolver, target.id, uri)
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

    private suspend fun upload(contentResolver: ContentResolver, photoId: String, uri: Uri): Result<Photo> =
        withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
            .fold({ part -> AlbumRepository.uploadPhotoImage(photoId, part) }, { Result.failure(it) })

    fun save(contentResolver: ContentResolver) {
        val current = _state.value
        if (current.isSaving || !current.canSave) return
        current.fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val photo = original
        if (photo == null && current.pendingImage == null) {
            _state.update { it.copy(error = "Pick an image") }
            return
        }
        val patch = photo?.let { current.fields.toPatch(it) }
        if (photo != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            if (photo != null) {
                AlbumRepository.updatePhoto(photo.id, checkNotNull(patch))
                    .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                    .onFailure { throwable ->
                        _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save photo") }
                    }
                return@launch
            }

            val created = AlbumRepository.createPhoto(current.fields.toCreate(checkNotNull(albumId))).getOrElse { throwable ->
                _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save photo") }
                return@launch
            }
            upload(contentResolver, created.id, checkNotNull(current.pendingImage))
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    // The photo exists now, so the form becomes an edit form for it. The album below must show it.
                    showSaved(created)
                    _state.update {
                        it.copy(
                            isSaving = false,
                            pendingImage = null,
                            imageChanged = true,
                            error = "The photo is saved, but the image did not upload. Pick the image again.",
                        )
                    }
                }
        }
    }
}

data class PhotoFormViewState(
    val fields: PhotoFormFields = PhotoFormFields(),
    val isLoading: Boolean = false,
    val isEditRoute: Boolean = false,
    val isEditing: Boolean = false,
    val imagePath: String? = null,
    val pendingImage: Uri? = null,
    val isUploading: Boolean = false,
    val imageChanged: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    /** An edit form may only save after the photo loaded. Otherwise Save would try to create a photo. */
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun PhotoFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    viewModel: PhotoFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fields = state.fields
    val contentResolver = LocalContext.current.contentResolver
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

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
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(if (state.pendingImage == null) "Pick image" else "Pick another image")
                }
                Text(
                    text = if (state.pendingImage == null) "No image yet" else "Image picked",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedTextField(
            value = fields.title,
            onValueChange = { viewModel.onFieldsChange(fields.copy(title = it)) },
            label = { Text("Title") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fields.comment,
            onValueChange = { viewModel.onFieldsChange(fields.copy(comment = it)) },
            label = { Text("Comment") },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fields.place,
            onValueChange = { viewModel.onFieldsChange(fields.copy(place = it)) },
            label = { Text("Place") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
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
            onClick = { viewModel.save(contentResolver) },
            enabled = !state.isSaving && !state.isLoading && !state.isUploading && state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save")
        }
    }
}
