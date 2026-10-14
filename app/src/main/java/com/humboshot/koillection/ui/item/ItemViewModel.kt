package com.humboshot.koillection.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.destinations.ItemDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val itemId: String = checkNotNull(savedStateHandle[ItemDetail.ID_ARG])

    private val _state = MutableStateFlow(ItemViewState())
    val state: StateFlow<ItemViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val item = async { ItemRepository.getItem(itemId) }
            val data = async { ItemRepository.getData(itemId) }
            val tags = async { ItemRepository.getTags(itemId) }
            val related = async { ItemRepository.getRelatedItems(itemId) }

            item.await()
                .onSuccess { loadedItem ->
                    // Tags, data and related items are secondary; show the item even if one of them fails.
                    _state.update {
                        it.copy(
                            isLoading = false,
                            item = loadedItem,
                            data = data.await().getOrDefault(emptyList()),
                            tags = tags.await().getOrDefault(emptyList()),
                            relatedItems = related.await().getOrDefault(emptyList()),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load item") }
                }
        }
    }
}

data class ItemViewState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val data: List<Datum> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val relatedItems: List<Item> = emptyList(),
    val error: String? = null,
)
