package com.humboshot.koillection.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.repository.TagRepository
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.extension.iriId
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagListViewModel : ViewModel() {
    private val _state = MutableStateFlow(TagListViewState())
    val state: StateFlow<TagListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val tags = async { TagRepository.getTags() }
            // Categories only add a label, so the list still shows when they fail to load.
            val categories = async { TagRepository.getCategories() }

            tags.await()
                .onSuccess { loaded ->
                    val byId = categories.await().getOrDefault(emptyList()).associateBy { it.id }
                    _state.update { it.copy(isLoading = false, tags = loaded, categories = byId) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load tags") }
                }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }
}

data class TagListViewState(
    val isLoading: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val categories: Map<String, TagCategory> = emptyMap(),
    val query: String = "",
    val error: String? = null,
) {
    val visibleTags: List<Tag> get() = filterTags(tags, query, categories)

    fun categoryOf(tag: Tag): TagCategory? = tag.category?.iriId()?.let(categories::get)
}

/** Keeps the tags whose label or category label contains [query], ignoring case. */
fun filterTags(tags: List<Tag>, query: String, categories: Map<String, TagCategory> = emptyMap()): List<Tag> {
    val needle = query.trim()
    if (needle.isEmpty()) return tags
    return tags.filter { tag ->
        tag.label.contains(needle, ignoreCase = true) ||
            tag.category?.iriId()?.let(categories::get)?.label?.contains(needle, ignoreCase = true) == true
    }
}
