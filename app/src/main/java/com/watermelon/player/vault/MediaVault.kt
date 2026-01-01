package com.watermelon.player.vault

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.watermelon.player.WatermelonApp
import com.watermelon.player.vault.EncryptionManager
import java.io.File

class MediaVault(private val context: Context) {
    private val vaultDir = File(context.filesDir, "vault")
    private val encryptionManager = EncryptionManager()

    init {
        if (!vaultDir.exists()) vaultDir.mkdirs()
    }

    fun addToVault(sourceFile: File, move: Boolean = false, password: String? = null): Boolean {
        return try {
            val encryptedFile = encryptionManager.encryptFile(sourceFile, password)
            val vaultFile = File(vaultDir, "${sourceFile.name}.vault")
            encryptedFile.copyTo(vaultFile, overwrite = true)

            if (move) sourceFile.delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeFromVault(vaultFile: File, password: String? = null): File? {
        return try {
            val decrypted = encryptionManager.decryptFile(vaultFile, password)
            vaultFile.delete()
            decrypted
        } catch (e: Exception) {
            null
        }
    }

    fun listVaultFiles(): List<File> {
        return vaultDir.listFiles { _, name -> name.endsWith(".vault") }?.toList() ?: emptyList()
    }

    fun authenticateWithBiometric(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Use fingerprint or PIN")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                onFail()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFail()
            }
        })

        biometricPrompt.authenticate(promptInfo)
    }
}
