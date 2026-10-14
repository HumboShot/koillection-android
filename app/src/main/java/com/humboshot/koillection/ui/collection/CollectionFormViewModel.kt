package com.humboshot.koillection.ui.collection

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.repository.CollectionRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.destinations.CollectionForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val collectionId: String? = savedStateHandle[CollectionForm.COLLECTION_ID_ARG]
    private val parentId: String? = savedStateHandle[CollectionForm.PARENT_ID_ARG]
    private var original: KCollection? = null

    private val _state = MutableStateFlow(CollectionFormViewState())
    val state: StateFlow<CollectionFormViewState> = _state.asStateFlow()

    init {
        if (collectionId != null) load(collectionId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            CollectionRepository.getCollection(id)
                .onSuccess { collection ->
                    original = collection
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = CollectionFormFields.from(collection),
                            imagePath = collection.image
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load collection") }
                }
        }
    }

    fun onTitleChange(title: String) = _state.update { it.copy(fields = it.fields.copy(title = title), error = null) }

    fun onVisibilityChange(visibility: String) =
        _state.update { it.copy(fields = it.fields.copy(visibility = visibility)) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> CollectionRepository.uploadImage(target.id, part) }, { Result.failure(it) })
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
        val fields = _state.value.fields
        fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val collection = original
        val patch = collection?.let { fields.toPatch(it) }
        if (collection != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (collection != null) {
                CollectionRepository.updateCollection(collection.id, checkNotNull(patch))
            } else {
                CollectionRepository.createCollection(fields.toCreate(parentId))
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save collection") }
                }
        }
    }
}

data class CollectionFormViewState(
    val fields: CollectionFormFields = CollectionFormFields(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val imagePath: String? = null,
    val isUploading: Boolean = false,
    val imageChanged: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)
