package com.humboshot.koillection.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.ui.tag.filterTags

/** The tags of an item in the item form: one chip per tag, and a chip that opens the tag picker. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemTagsField(selected: List<Tag>, allTags: List<Tag>, onAdd: (Tag) -> Unit, onRemove: (Tag) -> Unit) {
    var picking by remember { mutableStateOf(false) }

    Column {
        Text("Tags", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            selected.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = { onRemove(tag) },
                    label = { Text(tag.label) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove ${tag.label}") }
                )
            }
            AssistChip(
                onClick = { picking = true },
                label = { Text("Add tag") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    }

    if (picking) {
        TagPickerDialog(
            tags = allTags.filterNot { tag -> selected.any { it.id == tag.id } },
            onPick = {
                onAdd(it)
                picking = false
            },
            onDismiss = { picking = false }
        )
    }
}

@Composable
private fun TagPickerDialog(tags: List<Tag>, onPick: (Tag) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val visible = filterTags(tags, query)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add tag") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (visible.isEmpty()) {
                    Text(
                        text = if (tags.isEmpty()) "There are no other tags." else "No tags match.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(visible, key = { it.id }) { tag ->
                        Text(
                            text = tag.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(tag) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
