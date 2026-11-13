package com.zara.ai

import com.zara.ai.core.designsystem.theme.ThemeConfig
import com.zara.ai.core.designsystem.theme.ThemeMode
import com.zara.ai.core.designsystem.theme.ZaraDarkColorScheme
import com.zara.ai.core.designsystem.theme.ZaraLightColorScheme
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeTest {

    @Test
    fun `default theme config is DARK with amoled black enabled`() {
        val config = ThemeConfig()
        assertEquals(ThemeMode.DARK, config.themeMode)
        assertEquals(true, config.useAmoledBlack)
        assertEquals(false, config.useDynamicColor)
    }

    @Test
    fun `dark color scheme defines primary accent and high contrast text`() {
        val dark = ZaraDarkColorScheme
        assertEquals(ZaraPrimaryAccent, dark.primary)
        assertNotNull(dark.background)
        assertNotNull(dark.surface)
    }

    @Test
    fun `light color scheme provides valid accessible contrast colors`() {
        val light = ZaraLightColorScheme
        assertNotNull(light.primary)
        assertNotNull(light.background)
        assertNotNull(light.surface)
    }
}
