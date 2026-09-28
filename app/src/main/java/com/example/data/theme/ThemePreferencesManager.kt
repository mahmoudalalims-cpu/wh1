package com.example.data.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String) {
    SYSTEM("تلقائي (حسب النظام)"),
    LIGHT("وضع النهار (فاتح)"),
    DARK("الوضع الليلي (داكن)")
}

enum class ColorPalette(
    val title: String,
    val primaryColor: Color,
    val lightPrimary: Color,
    val lightContainer: Color,
    val onLightContainer: Color,
    val darkPrimary: Color
) {
    BLUE_MINIMALIST(
        title = "أزرق قياسي (عصري)",
        primaryColor = Color(0xFF005FB0),
        lightPrimary = Color(0xFF005FB0),
        lightContainer = Color(0xFFD1E4FF),
        onLightContainer = Color(0xFF001D36),
        darkPrimary = Color(0xFF9ECAFF)
    ),
    ROYAL_NAVY(
        title = "كحلي ملكي (مستودعات)",
        primaryColor = Color(0xFF1E3A8A),
        lightPrimary = Color(0xFF1E3A8A),
        lightContainer = Color(0xFFDBEAFE),
        onLightContainer = Color(0xFF1E293B),
        darkPrimary = Color(0xFF93C5FD)
    ),
    EMERALD_GREEN(
        title = "أخضر زمردي (حيوي)",
        primaryColor = Color(0xFF059669),
        lightPrimary = Color(0xFF059669),
        lightContainer = Color(0xFFD1FAE5),
        onLightContainer = Color(0xFF064E3B),
        darkPrimary = Color(0xFF6EE7B7)
    ),
    AMBER_GOLD(
        title = "عنبري دافئ (فخم)",
        primaryColor = Color(0xFFD97706),
        lightPrimary = Color(0xFFD97706),
        lightContainer = Color(0xFFFEF3C7),
        onLightContainer = Color(0xFF78350F),
        darkPrimary = Color(0xFFFCD34D)
    ),
    PURPLE_ELEGANCE(
        title = "بنفسجي راقي (إداري)",
        primaryColor = Color(0xFF7C3AED),
        lightPrimary = Color(0xFF7C3AED),
        lightContainer = Color(0xFFEDE9FE),
        onLightContainer = Color(0xFF4C1D95),
        darkPrimary = Color(0xFFC4B5FD)
    )
}

class ThemePreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("warehouse_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _colorPalette = MutableStateFlow(loadColorPalette())
    val colorPalette: StateFlow<ColorPalette> = _colorPalette.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }

    private fun loadColorPalette(): ColorPalette {
        val name = prefs.getString("color_palette", ColorPalette.BLUE_MINIMALIST.name) ?: ColorPalette.BLUE_MINIMALIST.name
        return try {
            ColorPalette.valueOf(name)
        } catch (e: Exception) {
            ColorPalette.BLUE_MINIMALIST
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setColorPalette(palette: ColorPalette) {
        prefs.edit().putString("color_palette", palette.name).apply()
        _colorPalette.value = palette
    }
}
