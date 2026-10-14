package com.humboshot.koillection.ui.main_collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.CollectionService
import com.humboshot.koillection.data.collection.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainCollectionViewModel : ViewModel() {
    private val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val members: MutableStateFlow<List<Member>> = MutableStateFlow(emptyList())

    private val _state = MutableStateFlow(MainCollectionViewState())
    val state: StateFlow<MainCollectionViewState>
        get() = _state

    init {
        viewModelScope.launch {
            combine(
                isLoading,
                members
            ) { isLoading, collections ->
                MainCollectionViewState(
                    isLoading,
                    collections
                )
            }.catch { throwable ->
                throw throwable
            }.collect {
                _state.value = it
            }
        }

        getCollections()
    }

    fun getCollections() {
        viewModelScope.launch {
            isLoading.value = true

            val apiClient = ApiClient.getInstance().create(CollectionService::class.java)
            apiClient.getCollections()
                .onSuccess {
                    members.value = it.member
                    isLoading.value = false
                }
                .onFailure {
                    isLoading.value = false
                }
        }
    }
}

data class MainCollectionViewState(
    val isLoading: Boolean = false,
    val members: List<Member> = emptyList()
)