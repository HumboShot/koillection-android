package com.humboshot.koillection.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The page path, as in the web header: "Collections / Books / Fantasy". The last part is bold.
 * Tap an earlier part to go back to it.
 */
@Composable
fun Breadcrumb(labels: List<String>, onClick: (index: Int) -> Unit, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    // Keep the current page visible when the path is wider than the screen.
    LaunchedEffect(labels, scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(scrollState)
    ) {
        labels.forEachIndexed { index, label ->
            val isLast = index == labels.lastIndex
            if (index > 0) {
                Text(
                    text = "/",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
            Text(
                text = label,
                maxLines = 1,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (isLast) Color.Unspecified else MaterialTheme.colorScheme.primary,
                modifier = if (isLast) Modifier else Modifier.clickable { onClick(index) }
            )
        }
    }
}
