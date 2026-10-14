package com.humboshot.koillection.ui.common

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Visibility values the Koillection API accepts. */
val VISIBILITIES = listOf("public", "internal", "private")

@Composable
fun VisibilityPicker(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        VISIBILITIES.forEachIndexed { index, visibility ->
            SegmentedButton(
                selected = visibility == selected,
                onClick = { onSelect(visibility) },
                shape = SegmentedButtonDefaults.itemShape(index, VISIBILITIES.size)
            ) {
                Text(visibility.replaceFirstChar { it.uppercase() })
            }
        }
    }
}
