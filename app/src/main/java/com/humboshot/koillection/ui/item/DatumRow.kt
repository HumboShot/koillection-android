package com.humboshot.koillection.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.extension.buildUrl
import com.humboshot.koillection.ui.common.KoillectionImage
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/** Renders a single Koillection custom field ("datum") according to its type. */
@Composable
fun DatumRow(datum: Datum, onImageClick: (String) -> Unit = {}) {
    when (datum.type) {
        "section" -> {
            Text(
                text = datum.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            HorizontalDivider()
        }

        "blank-line" -> Spacer(Modifier.height(16.dp))

        "image", "sign" -> {
            val path = datum.imageLargeThumbnail ?: datum.image
            LabeledValue(datum.label) {
                KoillectionImage(
                    path = datum.imageSmallThumbnail ?: path,
                    contentDescription = datum.label,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = path != null) { path?.let(onImageClick) }
                )
            }
        }

        "file", "video" -> {
            val path = datum.file ?: datum.video
            val uriHandler = LocalUriHandler.current
            LabeledValue(datum.label) {
                Text(
                    text = datum.originalFilename ?: path?.substringAfterLast('/') ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(enabled = path != null) {
                        path?.let { uriHandler.openUri(it.buildUrl()) }
                    }
                )
            }
        }

        "link" -> {
            val uriHandler = LocalUriHandler.current
            val url = datum.value.orEmpty()
            LabeledValue(datum.label) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(enabled = url.isNotEmpty()) {
                        runCatching { uriHandler.openUri(url) }
                    }
                )
            }
        }

        "checkbox" -> {
            val checked = datum.value == "1" || datum.value.equals("true", ignoreCase = true)
            LabeledValue(datum.label) {
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (checked) "Yes" else "No",
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            val text = formatValue(datum)
            if (text.isNotEmpty()) {
                LabeledValue(datum.label) {
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        value()
    }
}

internal fun formatValue(datum: Datum): String {
    val value = datum.value?.trim().orEmpty()
    if (value.isEmpty()) return ""

    return when (datum.type) {
        "date" -> formatDate(value)
        "price" -> listOfNotNull(value, datum.currency).joinToString(" ")
        "country" -> Locale("", value).displayCountry.ifEmpty { value }
        "rating" -> formatRating(value)
        "list", "choice-list" -> parseList(value).joinToString(", ")
        else -> value
    }
}

private fun formatDate(value: String): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value.take(10))!!
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(parsed)
}.getOrDefault(value)

/** Koillection ratings go from 0 to 10 and are shown as five stars. */
private fun formatRating(value: String): String {
    val rating = value.toIntOrNull()?.coerceIn(0, 10) ?: return value
    val full = rating / 2
    val half = rating % 2 == 1
    return "★".repeat(full) + (if (half) "½" else "") + "☆".repeat(5 - full - if (half) 1 else 0)
}

private fun parseList(value: String): List<String> {
    if (!value.startsWith("[")) return listOf(value)
    return runCatching {
        val array = JSONArray(value)
        List(array.length()) { array.getString(it) }
    }.getOrDefault(listOf(value))
}
