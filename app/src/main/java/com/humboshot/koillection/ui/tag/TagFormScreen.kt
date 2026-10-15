package com.humboshot.koillection.ui.tag

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
import com.humboshot.koillection.data.repository.TagRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.destinations.TagForm
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.OptionDropdown
import com.humboshot.koillection.ui.common.VisibilityPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val tagId: String? = savedStateHandle[TagForm.TAG_ID_ARG]
    private var original: Tag? = null

    private val _state = MutableStateFlow(TagFormViewState(isEditRoute = tagId != null))
    val state: StateFlow<TagFormViewState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val categories = async { TagRepository.getCategories() }
            val tag = tagId?.let { id -> async { TagRepository.getTag(id) } }

            val loaded = tag?.await()
            val loadError = loaded?.exceptionOrNull()
            loaded?.getOrNull()?.let { original = it }
            _state.update {
                it.copy(
                    isLoading = false,
                    categories = categories.await().getOrDefault(emptyList()),
                    isEditing = original != null,
                    fields = original?.let(TagFormFields::from) ?: it.fields,
                    imagePath = original?.let { tag -> tag.imageSmallThumbnail ?: tag.image },
                    error = loadError?.let { error -> error.message ?: "Failed to load tag" },
                )
            }
        }
    }

    fun onFieldsChange(fields: TagFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> TagRepository.uploadImage(target.id, part) }, { Result.failure(it) })
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

        val tag = original
        val patch = tag?.let { current.fields.toPatch(it) }
        if (tag != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (tag != null) {
                TagRepository.updateTag(tag.id, checkNotNull(patch))
            } else {
                TagRepository.createTag(current.fields.toCreate())
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save tag") }
                }
        }
    }
}

data class TagFormViewState(
    val fields: TagFormFields = TagFormFields(),
    val categories: List<TagCategory> = emptyList(),
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
    /** An edit form may only save after the tag loaded. Otherwise Save would create a new tag. */
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun TagFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    viewModel: TagFormViewModel = viewModel()
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
            value = fields.label,
            onValueChange = { viewModel.onFieldsChange(fields.copy(label = it)) },
            label = { Text("Label") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fields.description,
            onValueChange = { viewModel.onFieldsChange(fields.copy(description = it)) },
            label = { Text("Description") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OptionDropdown(
            label = "Category",
            options = listOf<Pair<String?, String>>(null to "No category") + state.categories.map { it.id to it.label },
            selected = fields.categoryId,
            onSelect = { viewModel.onFieldsChange(fields.copy(categoryId = it)) },
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
