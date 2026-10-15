package com.humboshot.koillection.ui.item

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.data.repository.TagRepository
import com.humboshot.koillection.data.repository.readImagePart
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.destinations.ItemForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val itemId: String? = savedStateHandle[ItemForm.ITEM_ID_ARG]
    private val collectionId: String? = savedStateHandle[ItemForm.COLLECTION_ID_ARG]
    private var original: Item? = null
    private var originalTagIds: List<String> = emptyList()

    private val _state = MutableStateFlow(ItemFormViewState(isEditRoute = itemId != null))
    val state: StateFlow<ItemFormViewState> = _state.asStateFlow()

    init {
        loadAllTags()
        if (itemId != null) load(itemId)
    }

    /** The tags to pick from. The form still works without them, so a failure only leaves the list empty. */
    private fun loadAllTags() {
        viewModelScope.launch {
            TagRepository.getTags().onSuccess { tags -> _state.update { it.copy(allTags = tags) } }
        }
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val itemTags = async { ItemRepository.getTags(id) }
            ItemRepository.getItem(id)
                .onSuccess { item ->
                    original = item
                    val tagsResult = itemTags.await()
                    val tags = tagsResult.getOrDefault(emptyList())
                    originalTagIds = tags.map { it.id }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = ItemFormFields.from(item, originalTagIds),
                            itemTags = tags,
                            // Without the current tags, a save would replace them. So the tag field stays off.
                            tagsLoadFailed = tagsResult.isFailure,
                            imagePath = item.imageSmallThumbnail ?: item.image
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load item") }
                }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(fields = it.fields.copy(name = name), error = null) }

    fun onQuantityChange(quantity: String) =
        _state.update { it.copy(fields = it.fields.copy(quantity = quantity.filter(Char::isDigit)), error = null) }

    fun onVisibilityChange(visibility: String) =
        _state.update { it.copy(fields = it.fields.copy(visibility = visibility)) }

    fun addTag(tag: Tag) = _state.update { it.copy(fields = it.fields.addTag(tag.id)) }

    fun removeTag(tagId: String) = _state.update { it.copy(fields = it.fields.removeTag(tagId)) }

    fun uploadImage(contentResolver: ContentResolver, uri: Uri) {
        val target = original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            withContext(Dispatchers.IO) { runCatching { readImagePart(contentResolver, uri) } }
                .fold({ part -> ItemRepository.uploadImage(target.id, part) }, { Result.failure(it) })
                .onSuccess { updated ->
                    original = updated
                    _state.update { it.copy(isUploading = false, imagePath = updated.imageSmallThumbnail ?: updated.image, imageChanged = true) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isUploading = false, error = throwable.message ?: "Failed to upload image") }
                }
        }
    }

    fun save() {
        if (_state.value.isSaving || !_state.value.canSave) return
        val fields = _state.value.fields
        fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val item = original
        val tagBase = if (_state.value.tagsLoadFailed) fields.tagIds else originalTagIds
        val patch = item?.let { fields.toPatch(it, tagBase) }
        if (item != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (item != null) {
                ItemRepository.updateItem(item.id, checkNotNull(patch))
            } else {
                ItemRepository.createItem(fields.toCreate(checkNotNull(collectionId)))
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save item") }
                }
        }
    }
}

data class ItemFormViewState(
    val fields: ItemFormFields = ItemFormFields(),
    val allTags: List<Tag> = emptyList(),
    /** The tags the item had when the form loaded. They name the chips if [allTags] failed to load. */
    val itemTags: List<Tag> = emptyList(),
    val tagsLoadFailed: Boolean = false,
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
    /** An edit form may only save after the item loaded. Otherwise Save would try to create an item. */
    val canSave: Boolean get() = !isEditRoute || isEditing

    /** The tags on the item, in the order the user added them. */
    val selectedTags: List<Tag>
        get() {
            val known = (itemTags + allTags).associateBy { it.id }
            return fields.tagIds.mapNotNull(known::get)
        }
}
