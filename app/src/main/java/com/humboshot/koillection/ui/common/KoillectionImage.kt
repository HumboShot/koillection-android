package com.humboshot.koillection.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.humboshot.koillection.extension.buildUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads a server-relative image path (e.g. `uploads/...`). The shared OkHttp client adds the token. */
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
    if (ScreenshotMode.enabled) {
        ScreenshotMode.ImagePlaceholder(path, modifier)
        return
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(path.buildUrl())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Removes cached images, which can show private items, after the user signs out. */
@OptIn(ExperimentalCoilApi::class)
suspend fun Context.clearImageCache() {
    imageLoader.memoryCache?.clear()
    withContext(Dispatchers.IO) { imageLoader.diskCache?.clear() }
}
