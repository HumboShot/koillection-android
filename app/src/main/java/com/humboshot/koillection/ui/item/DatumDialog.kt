package com.humboshot.koillection.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DatumDialog(
    editor: DatumEditorState,
    onFieldsChange: (DatumFormFields) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val fields = editor.fields
    val isNew = editor.target == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add field" else "Edit field") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isNew) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EDITABLE_DATUM_TYPES.forEach { type ->
                            FilterChip(
                                selected = fields.type == type,
                                onClick = { onFieldsChange(fields.copy(type = type)) },
                                label = { Text(type.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = fields.label,
                    onValueChange = { onFieldsChange(fields.copy(label = it)) },
                    label = { Text("Label") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fields.value,
                    onValueChange = { onFieldsChange(fields.copy(value = it)) },
                    label = { Text("Value") },
                    singleLine = fields.type == "number",
                    keyboardOptions = if (fields.type == "number") {
                        KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    } else {
                        KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                editor.error?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !editor.isSaving) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(onClick = onDelete, enabled = !editor.isSaving) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
