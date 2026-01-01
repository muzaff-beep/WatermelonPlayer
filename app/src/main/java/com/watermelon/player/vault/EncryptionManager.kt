package com.watermelon.player.vault

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

class EncryptionManager {
    fun encryptFile(sourceFile: File, password: String? = null): File {
        val context = WatermelonApp.instance
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedFile = EncryptedFile.Builder(
            context,
            File(context.cacheDir, "${sourceFile.name}.encrypted"),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        sourceFile.inputStream().use { input ->
            encryptedFile.openFileOutput().use { output ->
                input.copyTo(output)
            }
        }

        return encryptedFile.file
    }

    fun decryptFile(encryptedFile: File, password: String? = null): File? {
        val context = WatermelonApp.instance
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val decryptedFile = EncryptedFile.Builder(
            context,
            File(context.cacheDir, "${encryptedFile.nameWithoutExtension}.decrypted"),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return try {
            encryptedFile.inputStream().use { input ->
                decryptedFile.openFileOutput().use { output ->
                    input.copyTo(output)
                }
            }
            decryptedFile.file
        } catch (e: Exception) {
            null
        }
    }
}
