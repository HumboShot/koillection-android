package com.humboshot.koillection.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The page path, as in the web header: "Collections / Books / Fantasy". The last part is bold.
 * Tap an earlier part to go back to it.
 *
 * The current title gets up to 70% of the width and ends with "…" when it is too long. The earlier
 * parts use the rest and scroll, so the nearest parent stays visible.
 */
@Composable
fun Breadcrumb(labels: List<String>, onClick: (index: Int) -> Unit, modifier: Modifier = Modifier) {
    val parents = labels.dropLast(1)
    val current = labels.lastOrNull().orEmpty()

    BoxWithConstraints(modifier) {
        val maxTitleWidth: Dp = if (parents.isEmpty()) maxWidth else maxWidth * 0.7f

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (parents.isNotEmpty()) {
                val scrollState = rememberScrollState()
                LaunchedEffect(parents, scrollState.maxValue) {
                    scrollState.scrollTo(scrollState.maxValue)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .horizontalScroll(scrollState)
                ) {
                    parents.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(role = Role.Button) { onClick(index) }
                                .padding(vertical = 12.dp)
                        )
                        Separator()
                    }
                }
            }
            Text(
                text = current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(max = maxTitleWidth)
            )
        }
    }
}

@Composable
private fun Separator() {
    Text(
        text = "/",
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(horizontal = 6.dp)
    )
}
