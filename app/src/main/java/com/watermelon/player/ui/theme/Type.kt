package com.watermelon.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.watermelon.player.R

val PersianFontFamily = FontFamily(
    Font(R.font.yekan, FontWeight.Normal),
    Font(R.font.vazir, FontWeight.Medium),
    Font(R.font.yekan, FontWeight.Bold)
)

val PersianTypography = Typography().copy(
    displayLarge = Typography().displayLarge.copy(fontFamily = PersianFontFamily),
    displayMedium = Typography().displayMedium.copy(fontFamily = PersianFontFamily),
    displaySmall = Typography().displaySmall.copy(fontFamily = PersianFontFamily),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = PersianFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = PersianFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = PersianFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = PersianFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = PersianFontFamily),
    titleSmall = Typography().titleSmall.copy(fontFamily = PersianFontFamily),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = PersianFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = PersianFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = PersianFontFamily),
    labelLarge = Typography().labelLarge.copy(fontFamily = PersianFontFamily),
    labelMedium = Typography().labelMedium.copy(fontFamily = PersianFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = PersianFontFamily)
)
