package com.humboshot.koillection.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.destinations.ItemForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val itemId: String? = savedStateHandle[ItemForm.ITEM_ID_ARG]
    private val collectionId: String? = savedStateHandle[ItemForm.COLLECTION_ID_ARG]
    private var original: Item? = null

    private val _state = MutableStateFlow(ItemFormViewState())
    val state: StateFlow<ItemFormViewState> = _state.asStateFlow()

    init {
        if (itemId != null) load(itemId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ItemRepository.getItem(id)
                .onSuccess { item ->
                    original = item
                    _state.update { it.copy(isLoading = false, fields = ItemFormFields.from(item)) }
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

    fun save() {
        val fields = _state.value.fields
        fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val item = original
        val patch = item?.let { fields.toPatch(it) }
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
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)
