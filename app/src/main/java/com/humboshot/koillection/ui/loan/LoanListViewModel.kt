package com.humboshot.koillection.ui.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.data.repository.LoanRepository
import com.humboshot.koillection.extension.iriId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoanListViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoanListViewState())
    val state: StateFlow<LoanListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            LoanRepository.getLoans()
                .onSuccess { loans ->
                    // A loan only links to its item, so load each item once for its name.
                    val names = loans.map { it.item.iriId() }.distinct()
                        .map { id -> async { id to ItemRepository.getItem(id).getOrNull()?.name } }
                        .awaitAll()
                        .mapNotNull { (id, name) -> name?.let { id to it } }
                        .toMap()
                    _state.update { it.copy(isLoading = false, loans = loans, itemNames = names) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load loans") }
                }
        }
    }

    fun onShowAllChange(showAll: Boolean) = _state.update { it.copy(showAll = showAll) }
}

data class LoanListViewState(
    val isLoading: Boolean = false,
    val loans: List<Loan> = emptyList(),
    val itemNames: Map<String, String> = emptyMap(),
    val showAll: Boolean = false,
    val error: String? = null,
) {
    val visibleLoans: List<Loan> get() = filterLoans(loans, showAll)

    fun itemName(loan: Loan): String = itemNames[loan.item.iriId()] ?: "Item"
}

/** "Lent" shows only loans that are not returned. "All" shows every loan. */
fun filterLoans(loans: List<Loan>, showAll: Boolean): List<Loan> =
    if (showAll) loans else loans.filterNot { it.isReturned }
