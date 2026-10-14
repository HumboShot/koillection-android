package com.humboshot.koillection.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainCollectionViewState())
    val state: StateFlow<MainCollectionViewState> = _state.asStateFlow()

    init {
        getCollections()
    }

    fun getCollections() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            CollectionRepository.getRootCollections()
                .onSuccess { collections ->
                    _state.update { it.copy(isLoading = false, collections = collections) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load collections") }
                }
        }
    }
}

data class MainCollectionViewState(
    val isLoading: Boolean = false,
    val collections: List<KCollection> = emptyList(),
    val error: String? = null,
)
