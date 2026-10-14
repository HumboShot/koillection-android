package com.humboshot.koillection.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.humboshot.koillection.UserContext
import com.humboshot.koillection.extension.buildUrl

/** Loads a server-relative image path (e.g. `uploads/...`) with the user's bearer token. */
@Composable
fun KoillectionImage(
    path: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (path.isNullOrEmpty()) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
        return
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .addHeader("Authorization", "Bearer ${UserContext.instance.jwt}")
            .data(path.buildUrl())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
