package com.humboshot.koillection.ui.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.CollectionRepository
import com.humboshot.koillection.destinations.CollectionDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val collectionId: String = checkNotNull(savedStateHandle[CollectionDetail.ID_ARG])

    private val _state = MutableStateFlow(CollectionDetailViewState())
    val state: StateFlow<CollectionDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val collection = async { CollectionRepository.getCollection(collectionId) }
            val children = async { CollectionRepository.getChildren(collectionId) }
            val data = async { CollectionRepository.getData(collectionId) }
            val items = async { CollectionRepository.getAllItems(collectionId) }

            val results = listOf(collection.await(), children.await(), data.await(), items.await())
            val error = results.firstNotNullOfOrNull { it.exceptionOrNull() }
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load collection") }
                return@launch
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    collection = collection.await().getOrThrow(),
                    children = children.await().getOrThrow(),
                    data = data.await().getOrThrow(),
                    items = items.await().getOrThrow(),
                )
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            CollectionRepository.deleteCollection(collectionId)
                .onSuccess { _state.update { it.copy(isLoading = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to delete collection") }
                }
        }
    }
}

data class CollectionDetailViewState(
    val isLoading: Boolean = false,
    val collection: KCollection? = null,
    val children: List<KCollection> = emptyList(),
    val data: List<Datum> = emptyList(),
    val items: List<Item> = emptyList(),
    val error: String? = null,
    val isDeleted: Boolean = false,
)
