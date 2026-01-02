package com.watermelon.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watermelon.player.subtitle.SubtitleStyler

/**
 * SubtitleSettingsScreen.kt
 * Purpose: Dedicated page in Settings for subtitle customization.
 * Added as sub-page from main Settings.
 * Features:
 *   - Color picker (white, yellow, green, cyan, red)
 *   - Size slider (80% - 200%)
 *   - Toggle background box
 *   - Toggle outline
 */

@Composable
fun SubtitleSettingsScreen() {
    var sizeScale by remember { mutableStateOf(1.0f) }
    var backgroundOn by remember { mutableStateOf(true) }
    var outlineOn by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("تنظیمات زیرنویس", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Text("رنگ متن")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(
                androidx.compose.ui.graphics.Color.White,
                androidx.compose.ui.graphics.Color.Yellow,
                androidx.compose.ui.graphics.Color.Green,
                androidx.compose.ui.graphics.Color.Cyan,
                androidx.compose.ui.graphics.Color.Red
            ).forEach { color ->
                Button(
                    onClick = { SubtitleStyler.setTextColor(color) },
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) { }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("اندازه فونت: ${ (sizeScale * 100).toInt() }%")
        Slider(
            value = sizeScale,
            onValueChange = {
                sizeScale = it
                SubtitleStyler.setFontSizeScale(it)
            },
            valueRange = 0.8f..2.0f
        )

        Spacer(Modifier.height(16.dp))

        SwitchSetting("پس‌زمینه مشکی", backgroundOn) {
            backgroundOn = it
            SubtitleStyler.setBackgroundEnabled(it)
        }

        SwitchSetting("حاشیه مشکی (خط دور)", outlineOn) {
            outlineOn = it
            SubtitleStyler.setOutlineEnabled(it)
        }
    }
}
