package com.humboshot.koillection.ui.choicelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.repository.ChoiceListRepository
import com.humboshot.koillection.destinations.ChoiceListForm
import com.humboshot.koillection.ui.common.TopBarActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChoiceListFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val choiceListId: String? = savedStateHandle[ChoiceListForm.CHOICE_LIST_ID_ARG]
    private var original: ChoiceList? = null

    private val _state = MutableStateFlow(ChoiceListFormViewState(isEditRoute = choiceListId != null))
    val state: StateFlow<ChoiceListFormViewState> = _state.asStateFlow()

    init {
        if (choiceListId != null) load(choiceListId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ChoiceListRepository.getChoiceList(id)
                .onSuccess { choiceList ->
                    original = choiceList
                    _state.update {
                        it.copy(isLoading = false, isEditing = true, fields = ChoiceListFormFields.from(choiceList))
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load choice list") }
                }
        }
    }

    fun onFieldsChange(fields: ChoiceListFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun save() {
        if (_state.value.isSaving || !_state.value.canSave) return
        val fields = _state.value.fields
        fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val choiceList = original
        val patch = choiceList?.let { fields.toPatch(it) }
        if (choiceList != null && patch == null) {
            _state.update { it.copy(isDone = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (choiceList != null) {
                ChoiceListRepository.updateChoiceList(choiceList.id, checkNotNull(patch)).map { }
            } else {
                ChoiceListRepository.createChoiceList(fields.toCreate()).map { }
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isDone = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save choice list") }
                }
        }
    }

    fun delete() {
        val choiceList = original ?: return
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            ChoiceListRepository.deleteChoiceList(choiceList.id)
                .onSuccess { _state.update { it.copy(isSaving = false, isDone = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to delete choice list") }
                }
        }
    }
}

data class ChoiceListFormViewState(
    val fields: ChoiceListFormFields = ChoiceListFormFields(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    /** True when the screen opened for an existing choice list. Save then waits until that list loads. */
    val isEditRoute: Boolean = false,
    val isSaving: Boolean = false,
    /** True after a save or a delete. The screen then goes back. */
    val isDone: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun ChoiceListFormScreen(
    onDone: () -> Unit,
    viewModel: ChoiceListFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fields = state.fields
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDone) {
        if (state.isDone) onDone()
    }

    if (state.isEditing) {
        TopBarActions {
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete choice list")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = fields.name,
            onValueChange = { viewModel.onFieldsChange(fields.copy(name = it)) },
            label = { Text("Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Choices", style = MaterialTheme.typography.labelLarge)
        fields.choices.forEachIndexed { index, choice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = choice,
                    onValueChange = { viewModel.onFieldsChange(fields.updateChoice(index, it)) },
                    singleLine = true,
                    placeholder = { Text("Choice ${index + 1}") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.onFieldsChange(fields.moveChoice(index, -1)) }, enabled = index > 0) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(
                    onClick = { viewModel.onFieldsChange(fields.moveChoice(index, 1)) },
                    enabled = index < fields.choices.lastIndex
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = { viewModel.onFieldsChange(fields.removeChoice(index)) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove choice")
                }
            }
        }
        TextButton(onClick = { viewModel.onFieldsChange(fields.addChoice()) }) { Text("Add choice") }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving && !state.isLoading && state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save")
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete choice list?") },
            text = {
                Text("This deletes \"${fields.name}\". Template fields that use it lose their choices. You cannot undo this.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}
