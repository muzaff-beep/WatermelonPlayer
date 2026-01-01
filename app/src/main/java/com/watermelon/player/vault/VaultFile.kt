package com.watermelon.player.vault

import java.io.File

data class VaultFile(
    val originalName: String,
    val encryptedFile: File,
    val addedDate: Long = System.currentTimeMillis(),
    val size: Long = encryptedFile.length(),
    val isVideo: Boolean = originalName.endsWith(".mp4") || originalName.endsWith(".mkv")
)
