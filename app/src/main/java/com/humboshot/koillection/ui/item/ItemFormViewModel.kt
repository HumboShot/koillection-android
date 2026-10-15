package com.humboshot.koillection.ui.item

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.DatumRepository
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
    /** The editable data on the server. It changes while a save runs, so a retry sends only what is left. */
    private var originalData: List<Datum> = emptyList()
    private var nextDraftKey = 0

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
            val itemData = async { ItemRepository.getData(id) }
            ItemRepository.getItem(id)
                .onSuccess { item ->
                    original = item
                    val tagsResult = itemTags.await()
                    val tags = tagsResult.getOrDefault(emptyList())
                    originalTagIds = tags.map { it.id }
                    val dataResult = itemData.await()
                    val data = dataResult.getOrDefault(emptyList())
                    originalData = data.filter { it.type in EDITABLE_DATUM_TYPES }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            fields = ItemFormFields.from(item, originalTagIds),
                            itemTags = tags,
                            // Without the current tags, a save would replace them. So the tag field stays off.
                            tagsLoadFailed = tagsResult.isFailure,
                            imagePath = item.imageSmallThumbnail ?: item.image,
                            drafts = originalData.map { datum -> DatumDraft(nextDraftKey++, datum, DatumFormFields.from(datum)) },
                            otherData = data.filter { datum -> datum.type !in EDITABLE_DATUM_TYPES },
                            // Without the current fields, the form cannot show or change them.
                            dataLoadFailed = dataResult.isFailure,
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

    fun openDatumEditor(draft: DatumDraft?) = _state.update {
        it.copy(
            datumEditor = DatumEditorState(
                key = draft?.key,
                canChangeType = draft?.original == null,
                fields = draft?.fields ?: DatumFormFields(),
            )
        )
    }

    fun onDatumFieldsChange(fields: DatumFormFields) =
        _state.update { it.copy(datumEditor = it.datumEditor?.copy(fields = fields, error = null)) }

    fun closeDatumEditor() = _state.update { it.copy(datumEditor = null) }

    /** Keeps the field in the form. The server gets it when the user saves the item. */
    fun applyDatumEditor() {
        val editor = _state.value.datumEditor ?: return
        editor.fields.validate()?.let { error ->
            _state.update { it.copy(datumEditor = editor.copy(error = error)) }
            return
        }
        _state.update { state ->
            val drafts = if (editor.key == null) {
                state.drafts + DatumDraft(nextDraftKey++, null, editor.fields)
            } else {
                state.drafts.map { if (it.key == editor.key) it.copy(fields = editor.fields) else it }
            }
            state.copy(drafts = drafts, datumEditor = null, error = null)
        }
    }

    fun removeDatum() {
        val key = _state.value.datumEditor?.key ?: return
        _state.update { state -> state.copy(drafts = state.drafts.filterNot { it.key == key }, datumEditor = null) }
    }

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

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val item = saveItem(fields).getOrElse { throwable ->
                _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save item") }
                return@launch
            }
            saveData(item.id)
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isSaving = false, error = "The item is saved, but a field failed: ${throwable.message ?: "unknown error"}")
                    }
                }
        }
    }

    /** Creates or updates the item. After a create, the form works as an edit form, so a retry does not create it again. */
    private suspend fun saveItem(fields: ItemFormFields): Result<Item> {
        val item = original
        if (item == null) {
            return ItemRepository.createItem(fields.toCreate(checkNotNull(collectionId))).onSuccess { created ->
                original = created
                originalTagIds = fields.tagIds
            }
        }
        val tagBase = if (_state.value.tagsLoadFailed) fields.tagIds else originalTagIds
        val patch = fields.toPatch(item, tagBase) ?: return Result.success(item)
        return ItemRepository.updateItem(item.id, patch).onSuccess { updated ->
            original = updated
            originalTagIds = fields.tagIds
        }
    }

    private suspend fun saveData(itemId: String): Result<Unit> {
        if (_state.value.dataLoadFailed) return Result.success(Unit)
        val changes = datumChanges(originalData, _state.value.drafts)
        for (datum in changes.deletes) {
            DatumRepository.deleteDatum(datum.id).onFailure { return Result.failure(it) }
            originalData = originalData - datum
        }
        for ((datum, patch) in changes.patches) {
            val updated = DatumRepository.updateDatum(datum.id, patch).getOrElse { return Result.failure(it) }
            originalData = originalData.map { if (it.id == datum.id) updated else it }
            replaceDraftOriginal(datum.id, updated)
        }
        for (draft in changes.creates) {
            val position = (originalData + _state.value.otherData).nextPosition()
            val created = DatumRepository.createDatum(draft.fields.toCreate(itemId, position)).getOrElse { return Result.failure(it) }
            originalData = originalData + created
            _state.update { state ->
                state.copy(drafts = state.drafts.map { if (it.key == draft.key) it.copy(original = created) else it })
            }
        }
        return Result.success(Unit)
    }

    private fun replaceDraftOriginal(id: String, datum: Datum) = _state.update { state ->
        state.copy(drafts = state.drafts.map { if (it.original?.id == id) it.copy(original = datum) else it })
    }
}

data class ItemFormViewState(
    val fields: ItemFormFields = ItemFormFields(),
    val allTags: List<Tag> = emptyList(),
    /** The tags the item had when the form loaded. They name the chips if [allTags] failed to load. */
    val itemTags: List<Tag> = emptyList(),
    val tagsLoadFailed: Boolean = false,
    /** The text and number fields, in order. */
    val drafts: List<DatumDraft> = emptyList(),
    /** Fields of other types. The app only shows them. */
    val otherData: List<Datum> = emptyList(),
    val dataLoadFailed: Boolean = false,
    val datumEditor: DatumEditorState? = null,
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

/** The add or edit dialog for one field. A null [key] means a new field. */
data class DatumEditorState(
    val key: Int?,
    /** The API cannot change the type of a saved field. */
    val canChangeType: Boolean,
    val fields: DatumFormFields,
    val error: String? = null,
)
