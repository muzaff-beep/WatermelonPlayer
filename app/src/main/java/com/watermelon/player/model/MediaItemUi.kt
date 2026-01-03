package com.watermelon.player.model

import android.net.Uri

data class MediaItemUi(
    val id: String,
    val title: String,
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val duration: Long = 0,
    val path: String,
    val uri: Uri,
    val artworkUri: Uri? = null,
    val dateAdded: Long = 0
)
