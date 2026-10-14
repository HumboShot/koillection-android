package com.humboshot.koillection.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.destinations.ItemForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val collectionId: String? = savedStateHandle[ItemForm.COLLECTION_ID_ARG]

    private val _state = MutableStateFlow(ItemFormViewState())
    val state: StateFlow<ItemFormViewState> = _state.asStateFlow()

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

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            ItemRepository.createItem(fields.toCreate(checkNotNull(collectionId)))
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save item") }
                }
        }
    }
}

data class ItemFormViewState(
    val fields: ItemFormFields = ItemFormFields(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)
