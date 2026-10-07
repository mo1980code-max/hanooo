package com.example.clockstudio.domain.model

object DigitalClockCatalog {
    val styles: List<DigitalClockStyle> = listOf(
        DigitalClockStyle("digital_01", DigitalLayoutType.STACKED_AM_PM, showDate = true, showDay = true, showAmPm = true),
        DigitalClockStyle("digital_02", DigitalLayoutType.INLINE_DATE, primaryColor = ClockPalette.CYAN, showDate = true),
        DigitalClockStyle("digital_03", DigitalLayoutType.SPACE_DAY, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.TEAL, showDay = true),
        DigitalClockStyle("digital_04", DigitalLayoutType.VERTICAL_STACK, primaryColor = ClockPalette.GOLD, accentColor = ClockPalette.WHITE, showDate = true),
        DigitalClockStyle("digital_05", DigitalLayoutType.LARGE_HOUR_MINUTE, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.CYAN, showDate = true),
        DigitalClockStyle("digital_06", DigitalLayoutType.TIME_WITH_SIDE_DATE, primaryColor = ClockPalette.CYAN, accentColor = ClockPalette.TEAL, showDate = true, showDay = true),
        DigitalClockStyle("digital_07", DigitalLayoutType.MINIMAL_LINE, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.TEAL, showDate = false, showDay = true),
        DigitalClockStyle("digital_08", DigitalLayoutType.SPLIT_CARDS, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.PURPLE, showDate = true),
        DigitalClockStyle("digital_09", DigitalLayoutType.SECONDS_MICRO, primaryColor = ClockPalette.CYAN, accentColor = ClockPalette.TEAL, showDate = true, showSeconds = true),
        DigitalClockStyle("digital_10", DigitalLayoutType.RETRO_LED, primaryColor = ClockPalette.GOLD, accentColor = ClockPalette.GOLD, showDate = true, fontStyleId = "mono"),
        DigitalClockStyle("digital_11", DigitalLayoutType.ORBITAL, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.PURPLE, showDay = true),
        DigitalClockStyle("digital_12", DigitalLayoutType.TYPOGRAPHIC, primaryColor = ClockPalette.WHITE, accentColor = ClockPalette.TEAL, showDate = true, showSeconds = true, showAmPm = false),
    )

    fun find(id: String?): DigitalClockStyle = styles.firstOrNull { it.id == id } ?: styles.first()
}

object AnalogClockCatalog {
    val styles: List<AnalogClockStyle> = listOf(
        AnalogClockStyle("analog_01", "Polar Classic", 0xFF121719L, ClockPalette.WHITE, ClockPalette.WHITE, ClockPalette.TEAL, ClockPalette.WHITE),
        AnalogClockStyle("analog_02", "Sundial Gold", 0xFF1B1710L, ClockPalette.GOLD, ClockPalette.GOLD, 0xFFFF795BL, ClockPalette.GOLD, showNumbers = true),
        AnalogClockStyle("analog_03", "Ion Cyan", 0xFF071820L, ClockPalette.CYAN, ClockPalette.CYAN, ClockPalette.WHITE, ClockPalette.CYAN, neon = true),
        AnalogClockStyle("analog_04", "Violet Pulse", 0xFF160E20L, ClockPalette.PURPLE, ClockPalette.WHITE, ClockPalette.PURPLE, ClockPalette.PURPLE, neon = true),
        AnalogClockStyle("analog_05", "Walnut Meridian", 0xFF25170FL, 0xFFE7BD89L, 0xFFE7BD89L, 0xFFFF725EL, 0xFFE7BD89L, showNumbers = true, romanNumerals = true),
        AnalogClockStyle("analog_06", "Glass Current", 0x5531C4B8L, ClockPalette.WHITE, ClockPalette.CYAN, ClockPalette.TEAL, ClockPalette.WHITE, glass = true),
        AnalogClockStyle("analog_07", "Quiet Line", 0xFF111719L, ClockPalette.WHITE, ClockPalette.WHITE, ClockPalette.WHITE, 0xFF8A9A9CL),
        AnalogClockStyle("analog_08", "Roman Night", 0xFF171412L, ClockPalette.GOLD, ClockPalette.GOLD, ClockPalette.WHITE, ClockPalette.GOLD, showNumbers = true, romanNumerals = true),
        AnalogClockStyle("analog_09", "Circuit Arc", 0xFF08161BL, ClockPalette.CYAN, ClockPalette.WHITE, ClockPalette.TEAL, ClockPalette.CYAN, neon = true),
        AnalogClockStyle("analog_10", "Steel Orbit", 0xFF20262BL, 0xFFD8E2E8L, ClockPalette.WHITE, ClockPalette.CYAN, 0xFFBAC8CFL, showNumbers = true),
        AnalogClockStyle("analog_11", "Lunar Halo", 0xFF0A1322L, ClockPalette.WHITE, 0xFF9DB6D6L, ClockPalette.CYAN, 0xFFBBD9F2L, glass = true),
        AnalogClockStyle("analog_12", "Noir Gold", 0xFF0D0B08L, ClockPalette.GOLD, ClockPalette.GOLD, 0xFFFF684C, ClockPalette.GOLD, showNumbers = true, neon = true),
    )

    fun find(id: String?): AnalogClockStyle = styles.firstOrNull { it.id == id } ?: styles.first()
}

object SmartClockCatalog {
    val styles: List<SmartClockStyle> = listOf(
        SmartClockStyle("smart_01", "Halo", SmartClockLayout.RING, ClockPalette.WHITE, ClockPalette.TEAL, ring = true),
        SmartClockStyle("smart_02", "Neon Ring", SmartClockLayout.RING, ClockPalette.CYAN, ClockPalette.PURPLE, ring = true),
        SmartClockStyle("smart_03", "Glass Panel", SmartClockLayout.PANEL, ClockPalette.WHITE, ClockPalette.CYAN, glass = true),
        SmartClockStyle("smart_04", "Quiet Stack", SmartClockLayout.STACKED, ClockPalette.WHITE, ClockPalette.TEAL),
        SmartClockStyle("smart_05", "Gradient Arc", SmartClockLayout.COLOR_GLOW, ClockPalette.WHITE, ClockPalette.PURPLE, ring = true),
        SmartClockStyle("smart_06", "Thin Line", SmartClockLayout.MINIMAL, ClockPalette.WHITE, ClockPalette.CYAN),
        SmartClockStyle("smart_07", "Cyber Panel", SmartClockLayout.CYBERPUNK, ClockPalette.CYAN, ClockPalette.TEAL, glass = true),
        SmartClockStyle("smart_08", "Color Orbit", SmartClockLayout.COLOR_GLOW, ClockPalette.GOLD, ClockPalette.PURPLE, ring = true),
        SmartClockStyle("smart_09", "Soft Glass", SmartClockLayout.PANEL, ClockPalette.WHITE, ClockPalette.TEAL, glass = true, showMonth = true),
        SmartClockStyle("smart_10", "Mono Module", SmartClockLayout.MINIMAL, ClockPalette.WHITE, ClockPalette.GOLD, showMonth = true),
        SmartClockStyle("smart_11", "Aurora Ring", SmartClockLayout.RING, ClockPalette.WHITE, ClockPalette.CYAN, ring = true),
        SmartClockStyle("smart_12", "Prism Data", SmartClockLayout.CYBERPUNK, ClockPalette.WHITE, ClockPalette.PURPLE, glass = true, showMonth = true),
    )

    fun find(id: String?): SmartClockStyle = styles.firstOrNull { it.id == id } ?: styles.first()
}
