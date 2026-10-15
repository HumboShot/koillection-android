package com.humboshot.koillection.ui.template

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.repository.ChoiceListRepository
import com.humboshot.koillection.data.repository.TemplateRepository
import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.FieldWrite
import com.humboshot.koillection.data.template.Template
import com.humboshot.koillection.destinations.TemplateDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TemplateDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val templateId: String = checkNotNull(savedStateHandle[TemplateDetail.ID_ARG])

    private val _state = MutableStateFlow(TemplateDetailViewState())
    val state: StateFlow<TemplateDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = load(errorAfterLoad = null)

    /** Reloads the template. [errorAfterLoad] shows after the reload, so a failed change stays visible. */
    private fun load(errorAfterLoad: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val template = async { TemplateRepository.getTemplate(templateId) }
            val fields = async { TemplateRepository.getFields(templateId) }
            // Choice lists only name the "choice-list" fields, so the screen still works when they fail.
            val choiceLists = async { ChoiceListRepository.getChoiceLists() }

            val error = template.await().exceptionOrNull() ?: fields.await().exceptionOrNull()
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load template") }
                return@launch
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    template = template.await().getOrThrow(),
                    fields = fields.await().getOrThrow(),
                    choiceLists = choiceLists.await().getOrDefault(emptyList()),
                    error = errorAfterLoad,
                )
            }
        }
    }

    fun openFieldEditor(field: Field?) = _state.update {
        it.copy(fieldEditor = FieldEditorState(target = field, fields = field?.let(FieldFormFields::from) ?: FieldFormFields()))
    }

    fun onFieldEditorChange(fields: FieldFormFields) =
        _state.update { it.copy(fieldEditor = it.fieldEditor?.copy(fields = fields, error = null)) }

    fun closeFieldEditor() = _state.update { it.copy(fieldEditor = null) }

    fun saveField() {
        val editor = _state.value.fieldEditor ?: return
        if (editor.isSaving) return
        editor.fields.validate()?.let { error ->
            _state.update { it.copy(fieldEditor = editor.copy(error = error)) }
            return
        }

        val target = editor.target
        val patch = target?.let { editor.fields.toPatch(it) }
        if (target != null && patch == null) {
            closeFieldEditor()
            return
        }
        val nextPosition = (_state.value.fields.maxOfOrNull { it.position } ?: 0) + 1

        runFieldEditorChange("Failed to save field") {
            if (target != null) {
                TemplateRepository.updateField(target.id, checkNotNull(patch)).map { }
            } else {
                TemplateRepository.createField(editor.fields.toCreate(templateId, nextPosition)).map { }
            }
        }
    }

    fun deleteField() {
        val target = _state.value.fieldEditor?.target ?: return
        if (_state.value.fieldEditor?.isSaving == true) return
        runFieldEditorChange("Failed to delete field") { TemplateRepository.deleteField(target.id) }
    }

    private fun runFieldEditorChange(errorText: String, change: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _state.update { it.copy(fieldEditor = it.fieldEditor?.copy(isSaving = true, error = null)) }
            change()
                .onSuccess {
                    closeFieldEditor()
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(fieldEditor = it.fieldEditor?.copy(isSaving = false, error = throwable.message ?: errorText))
                    }
                }
        }
    }

    fun moveField(index: Int, delta: Int) {
        if (_state.value.isBusy) return
        val changes = swapPositions(_state.value.fields, index, delta)
        if (changes.isEmpty()) return
        runTemplateChange("Failed to move field") {
            changes.map { (id, position) ->
                TemplateRepository.updateField(id, FieldWrite(position = position))
            }.firstOrNull { it.isFailure }?.map { } ?: Result.success(Unit)
        }
    }

    fun openRenameDialog() = _state.update { it.copy(isRenaming = true, renameError = null) }

    fun closeRenameDialog() = _state.update { it.copy(isRenaming = false, renameError = null) }

    fun rename(name: String) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, renameError = null) }
            TemplateRepository.renameTemplate(templateId, name)
                .onSuccess { template ->
                    _state.update {
                        it.copy(isBusy = false, isRenaming = false, template = template, changeCount = it.changeCount + 1)
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isBusy = false, renameError = throwable.message ?: "Failed to rename template") }
                }
        }
    }

    fun deleteTemplate() = runTemplateChange("Failed to delete template", onSuccess = {
        _state.update { it.copy(isDeleted = true) }
    }) { TemplateRepository.deleteTemplate(templateId) }

    private fun runTemplateChange(
        errorText: String,
        onSuccess: () -> Unit = { refresh() },
        change: suspend () -> Result<Unit>,
    ) {
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, error = null) }
            change()
                .onSuccess {
                    _state.update { it.copy(isBusy = false) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isBusy = false) }
                    load(errorAfterLoad = throwable.message ?: errorText)
                }
        }
    }
}

data class TemplateDetailViewState(
    val isLoading: Boolean = false,
    val template: Template? = null,
    val fields: List<Field> = emptyList(),
    val choiceLists: List<ChoiceList> = emptyList(),
    val error: String? = null,
    /** True while a move, rename or delete runs. */
    val isBusy: Boolean = false,
    val isRenaming: Boolean = false,
    val renameError: String? = null,
    val isDeleted: Boolean = false,
    val fieldEditor: FieldEditorState? = null,
    /** Goes up after each change that the list screen below must show, such as a rename. */
    val changeCount: Int = 0,
)

/** The add or edit dialog for one field. A null [target] means a new field. */
data class FieldEditorState(
    val target: Field?,
    val fields: FieldFormFields,
    val isSaving: Boolean = false,
    val error: String? = null,
)
