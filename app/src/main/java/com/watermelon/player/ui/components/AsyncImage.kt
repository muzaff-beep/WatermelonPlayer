package com.watermelon.player.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.watermelon.player.util.PerformanceMonitor

/**
 * AsyncImage.kt
 * Wrapper to enforce global low-RAM Coil config.
 * All thumbnails use this instead of direct AsyncImage.
 */

@Composable
fun LowRamAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        imageLoader = PerformanceMonitor.getCoilImageLoader() // Future: cached instance
    )
}
