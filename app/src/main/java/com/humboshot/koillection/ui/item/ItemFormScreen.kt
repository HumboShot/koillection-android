package com.humboshot.koillection.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.ui.common.ImagePickerField
import com.humboshot.koillection.ui.common.VisibilityPicker

@Composable
fun ItemFormScreen(
    onSaved: () -> Unit,
    onImageChanged: () -> Unit,
    showTags: Boolean = true,
    viewModel: ItemFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contentResolver = LocalContext.current.contentResolver

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }
    LaunchedEffect(state.imageChanged) {
        if (state.imageChanged) onImageChanged()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (state.isEditing) {
            ImagePickerField(
                path = state.imagePath,
                isUploading = state.isUploading,
                onPick = { uri -> viewModel.uploadImage(contentResolver, uri) }
            )
        }
        OutlinedTextField(
            value = state.fields.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.fields.quantity,
            onValueChange = viewModel::onQuantityChange,
            label = { Text("Quantity") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        if (showTags && state.tagsLoadFailed) {
            Text(
                text = "The tags could not load. Saving keeps the tags as they are.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (showTags) {
            ItemTagsField(
                selected = state.selectedTags,
                allTags = state.allTags,
                onAdd = viewModel::addTag,
                onRemove = { viewModel.removeTag(it.id) }
            )
        }
        ItemDataSection(
            state = state,
            onOpen = viewModel::openDatumEditor,
        )
        Text("Visibility", style = MaterialTheme.typography.labelLarge)
        VisibilityPicker(
            selected = state.fields.visibility,
            onSelect = viewModel::onVisibilityChange,
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving && !state.isLoading && !state.isUploading && state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save")
        }
    }

    state.datumEditor?.let { editor ->
        DatumDialog(
            editor = editor,
            onFieldsChange = viewModel::onDatumFieldsChange,
            onDone = viewModel::applyDatumEditor,
            onDelete = viewModel::removeDatum,
            onDismiss = viewModel::closeDatumEditor
        )
    }
}

/** The fields of the item. Changes go to the server when the user saves the item. */
@Composable
private fun ItemDataSection(state: ItemFormViewState, onOpen: (DatumDraft?) -> Unit) {
    Column {
        Text("Fields", style = MaterialTheme.typography.labelLarge)
        if (state.dataLoadFailed) {
            Text(
                text = "The fields could not load. Saving keeps the fields as they are.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            return
        }
        state.drafts.forEach { draft ->
            Column(Modifier.fillMaxWidth().clickable { onOpen(draft) }) {
                DatumRow(draft.preview())
            }
        }
        if (state.otherData.isNotEmpty()) {
            Text(
                text = "You can change these fields only on the web:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            state.otherData.forEach { DatumRow(it) }
        }
        TextButton(onClick = { onOpen(null) }) { Text("Add field") }
    }
}
