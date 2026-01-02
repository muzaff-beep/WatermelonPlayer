package com.watermelon.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.watermelon.player.viewmodel.GalleryViewModel
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * GalleryScreen.kt
 * Purpose: TV-only full-screen image slideshow (JPG/PNG/GIF/WEBP/HEIC/BMP).
 * Features:
 *   - Auto-play with configurable duration (2-60s per image)
 *   - Transitions: fade, zoom, pan
 *   - Background music overlay (from local folder)
 *   - Remote navigation: LEFT/RIGHT = prev/next, CENTER = pause/resume
 *   - Shown only on TV leanback devices
 */

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        if (uiState.images.isEmpty()) {
            Text(
                text = "No images found. Insert USB/SD.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Current image with transition
            AnimatedContent(
                targetState = uiState.currentImage,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 1.1f) with
                            fadeOut() + scaleOut(targetScale = 0.9f)
                },
                label = "ImageTransition"
            ) { image ->
                Image(
                    painter = rememberAsyncImagePainter(model = image.path),
                    contentDescription = image.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Optional info overlay (filename, index)
            if (uiState.showInfo) {
                Text(
                    text = "${uiState.currentIndex + 1} / ${uiState.images.size} - ${uiState.currentImage.title}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                )
            }
        }
    }

    // Auto-advance timer
    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying && uiState.images.isNotEmpty()) {
            delay(uiState.slideDurationSeconds * 1000L)
            viewModel.nextImage()
        }
    }
}
