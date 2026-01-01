package com.watermelon.player.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.watermelon.player.player.WatermelonPlayer
import com.watermelon.player.ui.theme.WatermelonTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    mediaUri: Uri? = null,
    mediaTitle: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PlayerViewModel = viewModel()
    val playerState by viewModel.playerState.collectAsState()
    val showControls by viewModel.showControls.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize player if needed
    LaunchedEffect(mediaUri) {
        mediaUri?.let { uri ->
            viewModel.initializePlayer(context, uri)
        }
    }
    
    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.releasePlayer()
        }
    }
    
    WatermelonTheme {
        Scaffold(
            topBar = {
                PlayerTopBar(
                    title = mediaTitle ?: "Watermelon Player",
                    onBack = onBack,
                    showControls = showControls,
                    onToggleControls = { viewModel.toggleControls() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { viewModel.toggleControls() },
                            onDoubleTap = { offset ->
                                // Seek forward/backward on double tap
                                if (offset.x < size.width / 2) {
                                    viewModel.seekBy(-10)
                                } else {
                                    viewModel.seekBy(10)
                                }
                            }
                        )
                    }
            ) {
                // Video Player
                PlayerViewComponent(
                    player = viewModel.watermelonPlayer?.exoPlayer,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Buffering indicator
                if (playerState.isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                
                // Player Controls (animated)
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PlayerControlsOverlay(
                        playerState = playerState,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onSeek = { seconds -> viewModel.seekBy(seconds) },
                        onSeekTo = { position -> viewModel.seekTo(position) },
                        onVolumeChange = { volume -> viewModel.setVolume(volume) },
                        onBack = onBack
                    )
                }
                
                // Error message
                playerState.errorMessage?.let { error ->
                    ErrorOverlay(
                        error = error,
                        onRetry = { mediaUri?.let { uri -> viewModel.initializePlayer(context, uri) } },
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerViewComponent(
    player: androidx.media3.common.Player?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false // We use custom controls
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PlayerTopBar(
    title: String,
    onBack: () -> Unit,
    showControls: Boolean,
    onToggleControls: () -> Unit
) {
    AnimatedVisibility(
        visible = showControls,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onToggleControls) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )
    }
}

@Composable
private fun PlayerControlsOverlay(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // Center play/pause button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            // Progress bar
            PlayerProgressBar(
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                onSeek = onSeekTo
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side
                Row {
                    IconButton(onClick = { onSeek(-10) }) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "Back 10 seconds",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = { onSeek(10) }) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White
                        )
                    }
                }
                
                // Center - time display
                PlayerTimeDisplay(
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration
                )
                
                // Right side
                Row {
                    IconButton(onClick = { /* TODO: Audio tracks */ }) {
                        Icon(Icons.Default.AudioFile, contentDescription = "Audio", tint = Color.White)
                    }
                    
                    IconButton(onClick = { /* TODO: Subtitles */ }) {
                        Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                    }
                    
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            }
        }
        
        // Volume control (right side)
        VolumeControl(
            currentVolume = playerState.volume,
            onVolumeChange = onVolumeChange,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )
    }
}

@Composable
private fun PlayerProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    Column {
        Slider(
            value = progress,
            onValueChange = { newProgress ->
                val newPosition = (newProgress * duration).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun PlayerTimeDisplay(
    currentPosition: Long,
    duration: Long
) {
    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
    
    Text(
        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun VolumeControl(
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when {
                currentVolume == 0f -> Icons.Default.VolumeOff
                currentVolume < 0.5f -> Icons.Default.VolumeDown
                else -> Icons.Default.VolumeUp
            },
            contentDescription = "Volume",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = currentVolume,
            onValueChange = onVolumeChange,
            modifier = Modifier
                .height(150.dp)
                .rotate(-90f),
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun ErrorOverlay(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Playback Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                    
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

// ViewModel for PlayerScreen
class PlayerViewModel : androidx.lifecycle.ViewModel() {
    private val _playerState = mutableStateOf(PlayerState())
    val playerState = _playerState
    
    private val _showControls = mutableStateOf(true)
    val showControls = _showControls
    
    var watermelonPlayer: WatermelonPlayer? = null
        private set
    
    private var controlsTimerJob: kotlinx.coroutines.Job? = null
    
    fun initializePlayer(context: android.content.Context, uri: android.net.Uri) {
        // Initialize player
        // TODO: Implement actual player initialization
        
        // Start controls auto-hide timer
        startControlsTimer()
    }
    
    fun togglePlayPause() {
        watermelonPlayer?.togglePlayPause()
        // Update state
        _playerState.value = _playerState.value.copy(
            isPlaying = !_playerState.value.isPlaying
        )
        resetControlsTimer()
    }
    
    fun seekBy(seconds: Int) {
        watermelonPlayer?.seekBy(seconds)
        resetControlsTimer()
    }
    
    fun seekTo(position: Long) {
        watermelonPlayer?.seekTo(position)
        _playerState.value = _playerState.value.copy(
            currentPosition = position
        )
        resetControlsTimer()
    }
    
    fun setVolume(volume: Float) {
        watermelonPlayer?.setVolume(volume)
        _playerState.value = _playerState.value.copy(
            volume = volume
        )
        resetControlsTimer()
    }
    
    fun toggleControls() {
        _showControls.value = !_showControls.value
        if (_showControls.value) {
            startControlsTimer()
        } else {
            controlsTimerJob?.cancel()
        }
    }
    
    fun clearError() {
        _playerState.value = _playerState.value.copy(
            errorMessage = null
        )
    }
    
    fun releasePlayer() {
        watermelonPlayer?.release()
        watermelonPlayer = null
        controlsTimerJob?.cancel()
    }
    
    private fun startControlsTimer() {
        controlsTimerJob?.cancel()
        controlsTimerJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(3000L) // Hide after 3 seconds
            _showControls.value = false
        }
    }
    
    private fun resetControlsTimer() {
        if (_showControls.value) {
            startControlsTimer()
        }
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val volume: Float = 1.0f,
    val errorMessage: String? = null
)
