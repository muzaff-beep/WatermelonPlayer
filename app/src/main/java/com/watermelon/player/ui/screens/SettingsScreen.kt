package com.watermelon.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watermelon.player.config.EditionManager
import com.watermelon.player.viewmodel.SettingsViewModel

/**
 * SettingsScreen.kt
 * Purpose: User configuration hub.
 * Key Iran-first settings:
 *   - Independent UI language selector (Persian/Arabic/English/Urdu/Kurdish)
 *   - Independent subtitle language selector (same list)
 *   - Performance mode (low-RAM toggle, VHS on/off)
 *   - Manual edition override (Iran/Global)
 *   - Vault management
 *   - Manual update check (sideload guide)
 */

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // UI Language selector
            SettingDropdown(
                title = "UI Language",
                options = listOf("فارسی", "العربية", "English", "اردو", "Kurdî"),
                selected = uiState.uiLanguage,
                onSelect = { viewModel.setUiLanguage(it) }
            )
        }

        item {
            // Subtitle Language selector (independent)
            SettingDropdown(
                title = "Subtitle Language",
                options = listOf("فارسی", "العربية", "English", "اردو", "Kurdî", "Auto"),
                selected = uiState.subtitleLanguage,
                onSelect = { viewModel.setSubtitleLanguage(it) }
            )
        }

        item {
            // Performance section
            SectionTitle("Performance")
            SwitchSetting(
                title = "Low-RAM Mode (TV/A23)",
                checked = uiState.lowRamMode,
                onChecked = { viewModel.toggleLowRamMode(it) }
            )
            SwitchSetting(
                title = "VHS Retro Effect (Rewind)",
                checked = uiState.vhsEnabled,
                onChecked = { viewModel.toggleVhsEffect(it) }
            )
        }

        item {
            // Edition override
            SectionTitle("Edition")
            Text("Current: ${if (EditionManager.isIranEdition) "Iran (Offline)" else "Global"}")
            Button(onClick = { viewModel.showEditionDialog() }) {
                Text("Manual Override")
            }
        }

        item {
            // Vault
            SectionTitle("Security")
            Button(onClick = { /* Navigate to Vault manager */ }) {
                Text("Manage Encrypted Vault")
            }
        }

        item {
            // Manual update
            SectionTitle("Updates")
            Button(onClick = { viewModel.openUpdateGuide() }) {
                Text("Check for Manual Update (APK)")
            }
        }
    }
}

@Composable
private fun SettingDropdown(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchSetting(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
}
