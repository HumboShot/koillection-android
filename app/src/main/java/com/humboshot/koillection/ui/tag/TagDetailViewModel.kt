package com.humboshot.koillection.ui.tag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.repository.TagRepository
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.destinations.TagDetail
import com.humboshot.koillection.extension.iriId
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val tagId: String = checkNotNull(savedStateHandle[TagDetail.ID_ARG])

    private val _state = MutableStateFlow(TagDetailViewState())
    val state: StateFlow<TagDetailViewState> = _state.asStateFlow()

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

            val tag = async { TagRepository.getTag(tagId) }
            val items = async { TagRepository.getTagItems(tagId, page = 1) }

            val error = tag.await().exceptionOrNull() ?: items.await().exceptionOrNull()
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load tag") }
                return@launch
            }

            val loadedTag = tag.await().getOrThrow()
            // The category only adds a label, so the tag still shows when it fails to load.
            val category = loadedTag.category?.iriId()?.let { TagRepository.getCategory(it).getOrNull() }
            val page = items.await().getOrThrow()
            nextItemsPage = 2
            _state.update {
                it.copy(
                    isLoading = false,
                    tag = loadedTag,
                    category = category,
                    items = page.members,
                    totalItems = page.totalItems,
                    hasMoreItems = page.hasNext,
                )
            }
        }
    }

    fun loadMoreItems() {
        val current = _state.value
        if (!current.hasMoreItems || current.isLoading || itemsJob?.isActive == true) return

        itemsJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreItems = true) }
            TagRepository.getTagItems(tagId, nextItemsPage)
                .onSuccess { page ->
                    nextItemsPage++
                    _state.update {
                        it.copy(isLoadingMoreItems = false, items = it.items + page.members, hasMoreItems = page.hasNext)
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoadingMoreItems = false, error = throwable.message ?: "Failed to load items") }
                }
        }
    }
}

data class TagDetailViewState(
    val isLoading: Boolean = false,
    val isLoadingMoreItems: Boolean = false,
    val tag: Tag? = null,
    val category: TagCategory? = null,
    val items: List<Item> = emptyList(),
    val totalItems: Int? = null,
    val hasMoreItems: Boolean = false,
    val error: String? = null,
)
