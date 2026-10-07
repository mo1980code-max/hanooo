package com.example.clockstudio.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsDefaultsTest {
    @Test fun defaultsAreSafeAndDarkWallpaperReady() {
        val settings = AppSettings()
        assertEquals("en", settings.languageTag)
        assertEquals(ClockCategory.CUSTOM, settings.defaultCategory)
        assertTrue(settings.useSystem24HourFormat)
        assertTrue(settings.showDate)
        assertTrue(settings.showBatteryPercentage)
        assertFalse(settings.showSeconds)
        assertFalse(settings.smoothAnalogSeconds)
        assertEquals(0.12f, settings.wallpaperDimAmount, 0.0001f)
    }

    @Test fun configurationClampsNormalizedPositionAndScale() {
        val configuration = WallpaperConfiguration(x = -0.2f, y = 1.4f, scale = 9f).normalized()
        assertEquals(0f, configuration.x, 0f)
        assertEquals(1f, configuration.y, 0f)
        assertEquals(1.65f, configuration.scale, 0f)
    }
}
