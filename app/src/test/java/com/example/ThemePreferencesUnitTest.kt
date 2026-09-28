package com.example

import com.example.data.theme.ColorPalette
import com.example.data.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemePreferencesUnitTest {

    @Test
    fun testThemeModesEnum() {
        assertEquals(3, ThemeMode.values().size)
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
    }

    @Test
    fun testColorPalettesEnum() {
        assertEquals(5, ColorPalette.values().size)
        val blue = ColorPalette.BLUE_MINIMALIST
        assertNotNull(blue.primaryColor)
        assertNotNull(blue.lightContainer)
        assertNotNull(blue.darkPrimary)
    }
}
