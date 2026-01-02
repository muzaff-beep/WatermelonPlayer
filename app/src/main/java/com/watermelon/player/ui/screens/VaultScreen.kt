package com.watermelon.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watermelon.player.viewmodel.VaultViewModel

/**
 * VaultScreen.kt
 * Purpose: UI for encrypted media vault.
 * Features:
 *   - List of vault files
 *   - Add/remove from vault
 *   - Biometric/PIN unlock prompt on access
 *   - Play vault file directly (streaming decryption)
 * TV-safe: Large cards, remote focus.
 */

@Composable
fun VaultScreen(viewModel: VaultViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAuthDialog) {
        // BiometricPrompt or PIN dialog (future)
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Unlock Vault") },
            text = { Text("Authenticate to access encrypted media") },
            confirmButton = {
                Button(onClick = { viewModel.authenticateSuccess() }) {
                    Text("Unlock")
                }
            }
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Encrypted Vault", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
        }

        items(uiState.vaultItems) { item ->
            Card(
                onClick = { viewModel.playVaultItem(item.path) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text("Encrypted • Tap to play") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeFromVault(item.path) }) {
                            Icon(Icons.Default.Delete, "Remove")
                        }
                    }
                )
            }
        }

        item {
            Button(onClick = { viewModel.addToVault() }) {
                Text("Add Media to Vault")
            }
        }
    }
}
