package com.example.clockstudio.domain.model

/** Gallery families. Their ordinal is stable for the four synchronized home pages. */
enum class ClockCategory(val key: String) {
    CUSTOM("custom"),
    DIGITAL("digital"),
    ANALOG("analog"),
    SMART("smart");

    companion object {
        fun fromKey(key: String?): ClockCategory = entries.firstOrNull { it.key == key } ?: CUSTOM
    }
}

enum class DigitalLayoutType {
    STACKED_AM_PM,
    INLINE_DATE,
    SPACE_DAY,
    VERTICAL_STACK,
    LARGE_HOUR_MINUTE,
    TIME_WITH_SIDE_DATE,
    MINIMAL_LINE,
    SPLIT_CARDS,
    SECONDS_MICRO,
    RETRO_LED,
    ORBITAL,
    TYPOGRAPHIC,
}

data class WallpaperItem(
    val id: String,
    val category: ClockCategory,
    val previewResource: Int? = null,
    val backgroundResource: Int? = null,
    val clockStyleId: String? = null,
    val title: String,
    val artworkIndex: Int = 0,
    /** Optional filename in app/src/main/assets/wallpapers/. */
    val assetFileName: String? = null,
)

data class DigitalClockStyle(
    val id: String,
    val layoutType: DigitalLayoutType,
    val primaryColor: Long = ClockPalette.WHITE,
    val accentColor: Long = ClockPalette.TEAL,
    val showDate: Boolean = true,
    val showDay: Boolean = true,
    val showSeconds: Boolean = false,
    val showAmPm: Boolean = true,
    val fontStyleId: String = "minimal",
)

data class AnalogClockStyle(
    val id: String,
    val title: String,
    val dialColor: Long,
    val hourHandColor: Long,
    val minuteHandColor: Long,
    val secondHandColor: Long,
    val markerColor: Long,
    val showNumbers: Boolean = false,
    val romanNumerals: Boolean = false,
    val neon: Boolean = false,
    val glass: Boolean = false,
)

data class SmartClockStyle(
    val id: String,
    val title: String,
    val layoutType: SmartClockLayout,
    val primaryColor: Long,
    val accentColor: Long,
    val ring: Boolean = false,
    val glass: Boolean = false,
    val showMonth: Boolean = false,
)

enum class SmartClockLayout { STACKED, RING, PANEL, MINIMAL, CYBERPUNK, COLOR_GLOW }

enum class BackgroundScaleMode { CENTER_CROP, FIT }
enum class TimeFormatMode { SYSTEM, TWELVE_HOUR, TWENTY_FOUR_HOUR }

data class WallpaperConfiguration(
    val wallpaperId: String = "custom_01",
    val clockStyleId: String = "digital_01",
    /** Clock position in normalized screen coordinates. */
    val x: Float = 0.5f,
    val y: Float = 0.40f,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val showSeconds: Boolean = false,
    val smoothSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showDay: Boolean = true,
    val showBattery: Boolean = true,
    val showAmPm: Boolean = true,
    val useSystem24HourFormat: Boolean = true,
    val timeFormatMode: TimeFormatMode = TimeFormatMode.SYSTEM,
    val showNumbers: Boolean = false,
    val showSecondHand: Boolean = true,
    val textColor: Long = ClockPalette.WHITE,
    val accentColor: Long = ClockPalette.TEAL,
    val hourHandColor: Long = ClockPalette.WHITE,
    val minuteHandColor: Long = ClockPalette.WHITE,
    val secondHandColor: Long = ClockPalette.TEAL,
    val markerColor: Long = ClockPalette.WHITE,
    val brightness: Float = 1f,
    val dimAmount: Float = 0.12f,
    val blurAmount: Float = 0f,
    val backgroundScaleMode: BackgroundScaleMode = BackgroundScaleMode.CENTER_CROP,
    val fontStyleId: String = "minimal",
) {
    fun normalized(): WallpaperConfiguration = copy(
        x = x.coerceIn(0f, 1f),
        y = y.coerceIn(0f, 1f),
        scale = scale.coerceIn(0.45f, 1.65f),
        opacity = opacity.coerceIn(0.15f, 1f),
        brightness = brightness.coerceIn(0.35f, 1.35f),
        dimAmount = dimAmount.coerceIn(0f, 0.75f),
        blurAmount = blurAmount.coerceIn(0f, 1f),
    )

    companion object {
        fun forWallpaper(item: WallpaperItem): WallpaperConfiguration {
            val styleId = item.clockStyleId ?: when (item.category) {
                ClockCategory.CUSTOM, ClockCategory.DIGITAL -> "digital_01"
                ClockCategory.ANALOG -> "analog_01"
                ClockCategory.SMART -> "smart_01"
            }
            val digital = DigitalClockCatalog.find(styleId)
            val analog = AnalogClockCatalog.find(styleId)
            val smart = SmartClockCatalog.find(styleId)
            return WallpaperConfiguration(
                wallpaperId = item.id,
                clockStyleId = styleId,
                showSeconds = if (item.category == ClockCategory.DIGITAL || item.category == ClockCategory.CUSTOM) digital.showSeconds else false,
                showDate = when (item.category) {
                    ClockCategory.CUSTOM, ClockCategory.DIGITAL -> digital.showDate
                    ClockCategory.SMART -> true
                    ClockCategory.ANALOG -> false
                },
                showDay = when (item.category) {
                    ClockCategory.CUSTOM, ClockCategory.DIGITAL -> digital.showDay
                    ClockCategory.SMART -> true
                    ClockCategory.ANALOG -> false
                },
                showAmPm = if (item.category == ClockCategory.DIGITAL || item.category == ClockCategory.CUSTOM) digital.showAmPm else false,
                showSecondHand = item.category == ClockCategory.ANALOG,
                showNumbers = analog.showNumbers,
                showBattery = item.category == ClockCategory.SMART,
                textColor = when (item.category) {
                    ClockCategory.CUSTOM, ClockCategory.DIGITAL -> digital.primaryColor
                    ClockCategory.SMART -> smart.primaryColor
                    ClockCategory.ANALOG -> analog.markerColor
                },
                accentColor = when (item.category) {
                    ClockCategory.CUSTOM, ClockCategory.DIGITAL -> digital.accentColor
                    ClockCategory.SMART -> smart.accentColor
                    ClockCategory.ANALOG -> analog.secondHandColor
                },
                hourHandColor = analog.hourHandColor,
                minuteHandColor = analog.minuteHandColor,
                secondHandColor = analog.secondHandColor,
                markerColor = analog.markerColor,
                fontStyleId = digital.fontStyleId,
                y = if (item.category == ClockCategory.CUSTOM) 0.38f else 0.40f,
            )
        }
    }
}

data class AppSettings(
    val languageTag: String = "en",
    val defaultCategory: ClockCategory = ClockCategory.CUSTOM,
    val useSystem24HourFormat: Boolean = true,
    val smoothAnalogSeconds: Boolean = false,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showBatteryPercentage: Boolean = true,
    val hapticFeedback: Boolean = true,
    val wallpaperDimAmount: Float = 0.12f,
)

object ClockPalette {
    const val WHITE: Long = 0xFFFFFFFFL
    const val MUTED_WHITE: Long = 0xFFBDBDBDL
    const val TEAL: Long = 0xFF00BFA5L
    const val CYAN: Long = 0xFF37D9F2L
    const val GOLD: Long = 0xFFFFC857L
    const val PURPLE: Long = 0xFFBB86FCL
    const val BLACK: Long = 0xFF000000L
}
