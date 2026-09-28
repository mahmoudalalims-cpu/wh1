package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.theme.ColorPalette
import com.example.data.theme.ThemeMode

@Composable
fun warehouseTextFieldColors(
    containerColor: Color? = null,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color? = null,
    textColor: Color? = null,
    labelColor: Color? = null,
    placeholderColor: Color? = null
): TextFieldColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // High contrast container and text colors to ensure text never disappears in light or dark mode
    val effectiveContainer = containerColor ?: if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val effectiveText = textColor ?: if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val effectiveBorder = unfocusedBorderColor ?: if (isDark) Color(0xFF64748B) else Color(0xFF64748B)
    val effectiveLabel = labelColor ?: if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val effectivePlaceholder = placeholderColor ?: if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = effectiveText,
        unfocusedTextColor = effectiveText,
        disabledTextColor = effectiveText.copy(alpha = 0.85f),
        focusedContainerColor = effectiveContainer,
        unfocusedContainerColor = effectiveContainer,
        disabledContainerColor = effectiveContainer.copy(alpha = 0.7f),
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = effectiveBorder,
        disabledBorderColor = effectiveBorder.copy(alpha = 0.5f),
        focusedLabelColor = focusedBorderColor,
        unfocusedLabelColor = effectiveLabel,
        disabledLabelColor = effectiveLabel.copy(alpha = 0.65f),
        focusedPlaceholderColor = effectivePlaceholder,
        unfocusedPlaceholderColor = effectivePlaceholder,
        disabledPlaceholderColor = effectivePlaceholder.copy(alpha = 0.5f),
        cursorColor = focusedBorderColor
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    colorPalette: ColorPalette = ColorPalette.BLUE_MINIMALIST,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = colorPalette.darkPrimary,
            onPrimary = Color(0xFF0F172A),
            primaryContainer = colorPalette.lightPrimary.copy(alpha = 0.35f),
            onPrimaryContainer = Color(0xFFE2E8F0),
            secondary = colorPalette.darkPrimary,
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFFF1F5F9),
            tertiary = Color(0xFF34D399),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            surfaceVariant = Color(0xFF334155),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF64748B),
            outlineVariant = Color(0xFF475569),
            error = Color(0xFFF87171),
            errorContainer = Color(0xFF450A0A),
            onErrorContainer = Color(0xFFFEE2E2)
        )
    } else {
        lightColorScheme(
            primary = colorPalette.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = colorPalette.lightContainer,
            onPrimaryContainer = colorPalette.onLightContainer,
            secondary = colorPalette.lightPrimary,
            onSecondary = Color.White,
            secondaryContainer = colorPalette.lightContainer,
            onSecondaryContainer = colorPalette.onLightContainer,
            tertiary = EmeraldTertiary,
            onTertiary = EmeraldOnTertiary,
            tertiaryContainer = EmeraldContainer,
            background = BackgroundLight,
            surface = SurfaceLight,
            surfaceVariant = Color(0xFFF1F5F9),
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A),
            onSurfaceVariant = Color(0xFF334155),
            outline = Color(0xFF64748B),
            outlineVariant = Color(0xFF94A3B8),
            error = DangerRed,
            errorContainer = DangerContainer,
            onErrorContainer = DangerOnContainer
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
