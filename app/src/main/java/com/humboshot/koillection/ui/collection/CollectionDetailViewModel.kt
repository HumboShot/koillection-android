package com.humboshot.koillection.ui.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.CollectionRepository
import com.humboshot.koillection.destinations.CollectionDetail
import kotlinx.coroutines.Job
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

    private var nextItemsPage = 1
    private var itemsJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        itemsJob?.cancel()
        nextItemsPage = 1

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val collection = async { CollectionRepository.getCollection(collectionId) }
            val children = async { CollectionRepository.getChildren(collectionId) }
            val data = async { CollectionRepository.getData(collectionId) }
            val items = async { CollectionRepository.getItems(collectionId, page = 1) }

            val results = listOf(collection.await(), children.await(), data.await(), items.await())
            val error = results.firstNotNullOfOrNull { it.exceptionOrNull() }
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load collection") }
                return@launch
            }

            val itemsPage = items.await().getOrThrow()
            nextItemsPage = 2
            _state.update {
                it.copy(
                    isLoading = false,
                    collection = collection.await().getOrThrow(),
                    children = children.await().getOrThrow(),
                    data = data.await().getOrThrow(),
                    items = itemsPage.members,
                    totalItems = itemsPage.totalItems,
                    hasMoreItems = itemsPage.hasNext,
                )
            }
        }
    }

    fun loadMoreItems() {
        val current = _state.value
        if (!current.hasMoreItems || current.isLoading || itemsJob?.isActive == true) return

        itemsJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreItems = true) }

            CollectionRepository.getItems(collectionId, nextItemsPage)
                .onSuccess { page ->
                    nextItemsPage++
                    _state.update {
                        it.copy(
                            isLoadingMoreItems = false,
                            items = it.items + page.members,
                            hasMoreItems = page.hasNext,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoadingMoreItems = false, error = throwable.message ?: "Failed to load items")
                    }
                }
        }
    }
}

data class CollectionDetailViewState(
    val isLoading: Boolean = false,
    val isLoadingMoreItems: Boolean = false,
    val collection: KCollection? = null,
    val children: List<KCollection> = emptyList(),
    val data: List<Datum> = emptyList(),
    val items: List<Item> = emptyList(),
    val totalItems: Int? = null,
    val hasMoreItems: Boolean = false,
    val error: String? = null,
)
