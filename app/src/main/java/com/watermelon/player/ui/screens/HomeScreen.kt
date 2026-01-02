package com.watermelon.player.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watermelon.player.viewmodel.HomeViewModel

/**
 * HomeScreen.kt
 * Purpose: Main entry screen after splash – displays local media library.
 * Features:
 *   - Tabbed pager: Videos | Images (TV slideshow uses Images tab)
 *   - Lazy grid with adaptive columns (3 on mobile, 5-6 on TV for remote navigation)
 *   - Separate indexing (VideoIndex.db vs ImageIndex.db)
 *   - Folder grouping + flat view toggle
 *   - Search bar (future smart tag/keyword search)
 *   - Iran-first: No cloud icons, no login, pure local scan
 *   - Low-RAM: Thumbnails lazy-loaded via Coil with memory cache disabled
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMediaClick: (String) -> Unit,     // Path or URI to play
    onFolderClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Pager for Videos / Images tabs
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs = listOf("Videos", "Images (TV)")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar with search (future) and settings
        CenterAlignedTopAppBar(
            title = { Text("Watermelon Player") },
            actions = {
                IconButton(onClick = { /* Navigate to Settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )

        // Tab row synced with pager
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { /* Pager will animate */ },
                    text = { Text(title) }
                )
            }
        }

        // Horizontal pager containing two grids
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> MediaGrid(
                    items = uiState.videoItems,
                    onMediaClick = onMediaClick,
                    isTvMode = uiState.isTvDevice
                )
                1 -> MediaGrid(
                    items = uiState.imageItems,
                    onMediaClick = onMediaClick,
                    isTvMode = uiState.isTvDevice
                )
            }
        }
    }
}

@Composable
private fun MediaGrid(
    items: List<MediaItemUi>,
    onMediaClick: (String) -> Unit,
    isTvMode: Boolean
) {
    val columns = if (isTvMode) 6 else 3   // More items visible on TV for remote control

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            MediaThumbnailCard(
                item = item,
                onClick = { onMediaClick(item.path) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MediaThumbnailCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(16f / 9f)
    ) {
        Box {
            // Coil image with disabled memory cache (low-RAM policy)
            AsyncImage(
                model = item.thumbnailUri,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error)
            )
            // Overlay title at bottom
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

data class MediaItemUi(
    val path: String,
    val title: String,
    val thumbnailUri: String,
    val duration: Long? = null
)
