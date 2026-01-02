package com.watermelon.player.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * MediaThumbnailCard.kt
 * Reusable card for HomeScreen grids.
 * Low-RAM: Coil memory cache disabled globally.
 */

@Composable
fun MediaThumbnailCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(16f / 9f)
    ) {
        Box {
            AsyncImage(
                model = item.thumbnailUri,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = colorResource(R.color.placeholder),
                error = colorResource(R.color.error)
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    .padding(8.dp)
            )
        }
    }
}
