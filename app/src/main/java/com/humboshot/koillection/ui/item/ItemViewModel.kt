package com.humboshot.koillection.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.loan.todayUtcMillis
import com.humboshot.koillection.data.repository.DatumRepository
import com.humboshot.koillection.data.repository.ItemRepository
import com.humboshot.koillection.data.repository.LoanRepository
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
            val loans = async { LoanRepository.getItemLoans(itemId) }

            item.await()
                .onSuccess { loadedItem ->
                    // Tags, data, related items and loans are secondary; show the item even if one of them fails.
                    _state.update {
                        it.copy(
                            isLoading = false,
                            item = loadedItem,
                            data = data.await().getOrDefault(emptyList()),
                            tags = tags.await().getOrDefault(emptyList()),
                            relatedItems = related.await().getOrDefault(emptyList()),
                            loans = loans.await().getOrDefault(emptyList()),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load item") }
                }
        }
    }

    fun openDatumEditor(datum: Datum?) = _state.update {
        it.copy(datumEditor = DatumEditorState(target = datum, fields = datum?.let(DatumFormFields::from) ?: DatumFormFields()))
    }

    fun onDatumFieldsChange(fields: DatumFormFields) =
        _state.update { it.copy(datumEditor = it.datumEditor?.copy(fields = fields, error = null)) }

    fun closeDatumEditor() = _state.update { it.copy(datumEditor = null) }

    fun saveDatum() {
        val editor = _state.value.datumEditor ?: return
        if (editor.isSaving) return
        editor.fields.validate()?.let { error ->
            _state.update { it.copy(datumEditor = editor.copy(error = error)) }
            return
        }

        val target = editor.target
        val patch = target?.let { editor.fields.toPatch(it) }
        if (target != null && patch == null) {
            closeDatumEditor()
            return
        }

        val nextPosition = (_state.value.data.maxOfOrNull { it.position ?: 0 } ?: 0) + 1
        runDatumChange {
            if (target != null) {
                DatumRepository.updateDatum(target.id, checkNotNull(patch)).map { }
            } else {
                DatumRepository.createDatum(editor.fields.toCreate(itemId, nextPosition)).map { }
            }
        }
    }

    fun deleteDatum() {
        val editor = _state.value.datumEditor ?: return
        if (editor.isSaving) return
        val target = editor.target ?: return
        runDatumChange { DatumRepository.deleteDatum(target.id) }
    }

    private fun runDatumChange(change: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _state.update { it.copy(datumEditor = it.datumEditor?.copy(isSaving = true, error = null)) }
            change()
                .onSuccess {
                    closeDatumEditor()
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(datumEditor = it.datumEditor?.copy(isSaving = false, error = throwable.message ?: "Failed to save field"))
                    }
                }
        }
    }

    fun openLendDialog() = _state.update { it.copy(lendDialog = LendDialogState(dateMillis = todayUtcMillis())) }

    fun onLendDialogChange(dialog: LendDialogState) = _state.update { it.copy(lendDialog = dialog.copy(error = null)) }

    fun closeLendDialog() = _state.update { it.copy(lendDialog = null) }

    fun lend() {
        val dialog = _state.value.lendDialog ?: return
        if (dialog.isSaving) return
        if (dialog.lentTo.isBlank()) {
            _state.update { it.copy(lendDialog = dialog.copy(error = "Enter who you lend the item to")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(lendDialog = dialog.copy(isSaving = true, error = null)) }
            LoanRepository.lend(itemId, dialog.lentTo.trim(), dialog.dateMillis)
                .onSuccess {
                    closeLendDialog()
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(lendDialog = it.lendDialog?.copy(isSaving = false, error = throwable.message ?: "Failed to lend item"))
                    }
                }
        }
    }

    fun markReturned(loan: Loan) = runLoanChange("Failed to mark the loan as returned") {
        LoanRepository.markReturned(loan.id, todayUtcMillis()).map { }
    }

    fun deleteLoan(loan: Loan) = runLoanChange("Failed to delete the loan") { LoanRepository.deleteLoan(loan.id) }

    private fun runLoanChange(errorText: String, change: suspend () -> Result<Unit>) {
        if (_state.value.isChangingLoan) return
        viewModelScope.launch {
            _state.update { it.copy(isChangingLoan = true, error = null) }
            change()
                .onSuccess {
                    _state.update { it.copy(isChangingLoan = false) }
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isChangingLoan = false, error = throwable.message ?: errorText) }
                }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ItemRepository.deleteItem(itemId)
                .onSuccess { _state.update { it.copy(isLoading = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to delete item") }
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
    val isDeleted: Boolean = false,
    val datumEditor: DatumEditorState? = null,
    val loans: List<Loan> = emptyList(),
    val lendDialog: LendDialogState? = null,
    val isChangingLoan: Boolean = false,
)

/** The "Lend" dialog. [dateMillis] is midnight UTC of the chosen day, as the date picker gives it. */
data class LendDialogState(
    val lentTo: String = "",
    val dateMillis: Long,
    val isSaving: Boolean = false,
    val error: String? = null,
)

/** The add or edit dialog for one datum. A null [target] means a new datum. */
data class DatumEditorState(
    val target: Datum?,
    val fields: DatumFormFields,
    val isSaving: Boolean = false,
    val error: String? = null,
)
