package com.humboshot.koillection.ui.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.template.FIELD_TYPES
import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.fieldTypeLabel
import com.humboshot.koillection.extension.iriId
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.NameDialog
import com.humboshot.koillection.ui.common.OptionDropdown
import com.humboshot.koillection.ui.common.TopBarActions
import com.humboshot.koillection.ui.common.VisibilityPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    onDeleted: () -> Unit,
    viewModel: TemplateDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    state.template?.let {
        TopBarActions {
            IconButton(onClick = viewModel::openRenameDialog) {
                Icon(Icons.Default.Edit, contentDescription = "Rename template")
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete template")
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            state.template?.let { template ->
                item {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                if (state.fields.isEmpty() && !state.isLoading) {
                    item { EmptyState("This template has no fields") }
                }
                itemsIndexed(state.fields, key = { _, field -> field.id }) { index, field ->
                    FieldRow(
                        field = field,
                        choiceListName = state.choiceLists.firstOrNull { it.id == field.choiceList?.iriId() }?.name,
                        canMoveUp = index > 0 && !state.isBusy,
                        canMoveDown = index < state.fields.lastIndex && !state.isBusy,
                        onMoveUp = { viewModel.moveField(index, -1) },
                        onMoveDown = { viewModel.moveField(index, 1) },
                        onClick = { viewModel.openFieldEditor(field) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                item {
                    TextButton(
                        onClick = { viewModel.openFieldEditor(null) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Add field")
                    }
                }
            }
        }
    }

    state.fieldEditor?.let { editor ->
        FieldDialog(
            editor = editor,
            choiceLists = state.choiceLists,
            onChange = viewModel::onFieldEditorChange,
            onSave = viewModel::saveField,
            onDelete = viewModel::deleteField,
            onDismiss = viewModel::closeFieldEditor
        )
    }

    if (state.isRenaming) {
        NameDialog(
            title = "Rename template",
            initialName = state.template?.name.orEmpty(),
            isSaving = state.isBusy,
            error = state.renameError,
            onConfirm = viewModel::rename,
            onDismiss = viewModel::closeRenameDialog
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete template?") },
            text = {
                Text("This deletes \"${state.template?.name}\" and its fields. Items keep their data. You cannot undo this.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteTemplate()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FieldRow(
    field: Field,
    choiceListName: String?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(field.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = listOfNotNull(fieldTypeLabel(field.type), choiceListName).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onMoveUp, enabled = canMoveUp) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
        }
    }
}

@Composable
private fun FieldDialog(
    editor: FieldEditorState,
    choiceLists: List<ChoiceList>,
    onChange: (FieldFormFields) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val fields = editor.fields

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editor.target == null) "Add field" else "Edit field") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fields.name,
                    onValueChange = { onChange(fields.copy(name = it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OptionDropdown(
                    label = "Type",
                    options = FIELD_TYPES.map { it to fieldTypeLabel(it) },
                    selected = fields.type,
                    onSelect = { onChange(fields.copy(type = it)) }
                )
                if (fields.type == "choice-list") {
                    OptionDropdown(
                        label = "Choice list",
                        options = choiceLists.map { it.id to it.name },
                        selected = fields.choiceListId,
                        onSelect = { onChange(fields.copy(choiceListId = it)) }
                    )
                }
                VisibilityPicker(
                    selected = fields.visibility,
                    onSelect = { onChange(fields.copy(visibility = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                editor.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !editor.isSaving) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (editor.target != null) {
                    TextButton(onClick = onDelete, enabled = !editor.isSaving) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
