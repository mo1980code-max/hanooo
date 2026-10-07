package com.example.clockstudio.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.BackgroundScaleMode
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.ClockPalette
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.TimeFormatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.clockStudioDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "clock_studio_preferences",
)

/** A single DataStore-backed source of truth for app settings, favorites, and wallpaper layout. */
class UserPreferencesRepository(context: Context) {
    private val store = context.applicationContext.clockStudioDataStore

    val settingsFlow: Flow<AppSettings> = store.data
        .recoverFromDiskError()
        .map(::readSettings)

    val configurationFlow: Flow<WallpaperConfiguration> = store.data
        .recoverFromDiskError()
        .map(::readConfiguration)

    val favoritesFlow: Flow<Set<String>> = store.data
        .recoverFromDiskError()
        .map { it[FAVORITES].orEmpty() }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        store.edit { preferences ->
            val previous = readSettings(preferences)
            val transformed = transform(previous)
            val updated = transformed.copy(
                wallpaperDimAmount = transformed.wallpaperDimAmount.coerceIn(0f, 0.75f),
            )
            writeSettings(preferences, updated)
            // A setting only synchronizes its matching live clock field when that preference changes;
            // unrelated settings never erase per-wallpaper customization.
            if (previous.useSystem24HourFormat != updated.useSystem24HourFormat) {
                preferences[CFG_USE_SYSTEM_24] = updated.useSystem24HourFormat
            }
            if (previous.smoothAnalogSeconds != updated.smoothAnalogSeconds) {
                preferences[CFG_SMOOTH_SECONDS] = updated.smoothAnalogSeconds
            }
            if (previous.showSeconds != updated.showSeconds) {
                preferences[CFG_SHOW_SECONDS] = updated.showSeconds
                preferences[CFG_SHOW_SECOND_HAND] = updated.showSeconds
            }
            if (previous.showDate != updated.showDate) preferences[CFG_SHOW_DATE] = updated.showDate
            if (previous.showBatteryPercentage != updated.showBatteryPercentage) {
                preferences[CFG_SHOW_BATTERY] = updated.showBatteryPercentage
            }
            if (previous.wallpaperDimAmount != updated.wallpaperDimAmount) {
                preferences[CFG_DIM] = updated.wallpaperDimAmount
            }
        }
    }

    suspend fun setLanguage(languageTag: String) {
        store.edit { preferences -> preferences[LANGUAGE_TAG] = languageTag.ifBlank { "en" } }
    }

    suspend fun setDefaultCategory(category: ClockCategory) {
        store.edit { preferences -> preferences[DEFAULT_CATEGORY] = category.key }
    }

    suspend fun toggleFavorite(wallpaperId: String) {
        store.edit { preferences ->
            val updated = preferences[FAVORITES].orEmpty().toMutableSet()
            if (!updated.add(wallpaperId)) updated.remove(wallpaperId)
            preferences[FAVORITES] = updated
        }
    }

    suspend fun saveConfiguration(configuration: WallpaperConfiguration) {
        val normalized = configuration.normalized()
        store.edit { preferences ->
            preferences[CFG_WALLPAPER_ID] = normalized.wallpaperId
            preferences[CFG_STYLE_ID] = normalized.clockStyleId
            preferences[CFG_X] = normalized.x
            preferences[CFG_Y] = normalized.y
            preferences[CFG_SCALE] = normalized.scale
            preferences[CFG_OPACITY] = normalized.opacity
            preferences[CFG_SHOW_SECONDS] = normalized.showSeconds
            preferences[CFG_SMOOTH_SECONDS] = normalized.smoothSeconds
            preferences[CFG_SHOW_DATE] = normalized.showDate
            preferences[CFG_SHOW_DAY] = normalized.showDay
            preferences[CFG_SHOW_BATTERY] = normalized.showBattery
            preferences[CFG_SHOW_AM_PM] = normalized.showAmPm
            preferences[CFG_USE_SYSTEM_24] = normalized.useSystem24HourFormat
            preferences[CFG_TIME_FORMAT_MODE] = normalized.timeFormatMode.name
            preferences[CFG_SHOW_NUMBERS] = normalized.showNumbers
            preferences[CFG_SHOW_SECOND_HAND] = normalized.showSecondHand
            preferences[CFG_TEXT_COLOR] = normalized.textColor
            preferences[CFG_ACCENT_COLOR] = normalized.accentColor
            preferences[CFG_HOUR_COLOR] = normalized.hourHandColor
            preferences[CFG_MINUTE_COLOR] = normalized.minuteHandColor
            preferences[CFG_SECOND_COLOR] = normalized.secondHandColor
            preferences[CFG_MARKER_COLOR] = normalized.markerColor
            preferences[CFG_BRIGHTNESS] = normalized.brightness
            preferences[CFG_DIM] = normalized.dimAmount
            preferences[CFG_BLUR] = normalized.blurAmount
            preferences[CFG_SCALE_MODE] = normalized.backgroundScaleMode.name
            preferences[CFG_FONT] = normalized.fontStyleId
            preferences[DIM_AMOUNT] = normalized.dimAmount
        }
    }

    suspend fun resetCustomization() {
        saveConfiguration(WallpaperConfiguration())
    }

    private fun readSettings(preferences: Preferences): AppSettings = AppSettings(
        languageTag = preferences[LANGUAGE_TAG] ?: "en",
        defaultCategory = ClockCategory.fromKey(preferences[DEFAULT_CATEGORY]),
        useSystem24HourFormat = preferences[USE_SYSTEM_24] ?: true,
        smoothAnalogSeconds = preferences[SMOOTH_ANALOG_SECONDS] ?: false,
        showSeconds = preferences[SHOW_SECONDS] ?: false,
        showDate = preferences[SHOW_DATE] ?: true,
        showBatteryPercentage = preferences[SHOW_BATTERY] ?: true,
        hapticFeedback = preferences[HAPTIC_FEEDBACK] ?: true,
        wallpaperDimAmount = (preferences[DIM_AMOUNT] ?: 0.12f).coerceIn(0f, 0.75f),
    )

    private fun writeSettings(preferences: androidx.datastore.preferences.core.MutablePreferences, settings: AppSettings) {
        preferences[LANGUAGE_TAG] = settings.languageTag
        preferences[DEFAULT_CATEGORY] = settings.defaultCategory.key
        preferences[USE_SYSTEM_24] = settings.useSystem24HourFormat
        preferences[SMOOTH_ANALOG_SECONDS] = settings.smoothAnalogSeconds
        preferences[SHOW_SECONDS] = settings.showSeconds
        preferences[SHOW_DATE] = settings.showDate
        preferences[SHOW_BATTERY] = settings.showBatteryPercentage
        preferences[HAPTIC_FEEDBACK] = settings.hapticFeedback
        preferences[DIM_AMOUNT] = settings.wallpaperDimAmount.coerceIn(0f, 0.75f)
    }

    private fun readConfiguration(preferences: Preferences): WallpaperConfiguration {
        val scaleMode = runCatching {
            BackgroundScaleMode.valueOf(preferences[CFG_SCALE_MODE] ?: BackgroundScaleMode.CENTER_CROP.name)
        }.getOrDefault(BackgroundScaleMode.CENTER_CROP)
        return WallpaperConfiguration(
            wallpaperId = preferences[CFG_WALLPAPER_ID] ?: "custom_01",
            clockStyleId = preferences[CFG_STYLE_ID] ?: "digital_01",
            x = preferences[CFG_X] ?: 0.5f,
            y = preferences[CFG_Y] ?: 0.40f,
            scale = preferences[CFG_SCALE] ?: 1f,
            opacity = preferences[CFG_OPACITY] ?: 1f,
            showSeconds = preferences[CFG_SHOW_SECONDS] ?: false,
            smoothSeconds = preferences[CFG_SMOOTH_SECONDS] ?: false,
            showDate = preferences[CFG_SHOW_DATE] ?: true,
            showDay = preferences[CFG_SHOW_DAY] ?: true,
            showBattery = preferences[CFG_SHOW_BATTERY] ?: true,
            showAmPm = preferences[CFG_SHOW_AM_PM] ?: true,
            useSystem24HourFormat = preferences[CFG_USE_SYSTEM_24] ?: true,
            timeFormatMode = runCatching { TimeFormatMode.valueOf(preferences[CFG_TIME_FORMAT_MODE] ?: TimeFormatMode.SYSTEM.name) }.getOrDefault(TimeFormatMode.SYSTEM),
            showNumbers = preferences[CFG_SHOW_NUMBERS] ?: false,
            showSecondHand = preferences[CFG_SHOW_SECOND_HAND] ?: true,
            textColor = preferences[CFG_TEXT_COLOR] ?: ClockPalette.WHITE,
            accentColor = preferences[CFG_ACCENT_COLOR] ?: ClockPalette.TEAL,
            hourHandColor = preferences[CFG_HOUR_COLOR] ?: ClockPalette.WHITE,
            minuteHandColor = preferences[CFG_MINUTE_COLOR] ?: ClockPalette.WHITE,
            secondHandColor = preferences[CFG_SECOND_COLOR] ?: ClockPalette.TEAL,
            markerColor = preferences[CFG_MARKER_COLOR] ?: ClockPalette.WHITE,
            brightness = preferences[CFG_BRIGHTNESS] ?: 1f,
            dimAmount = (preferences[DIM_AMOUNT] ?: preferences[CFG_DIM] ?: 0.12f).coerceIn(0f, 0.75f),
            blurAmount = preferences[CFG_BLUR] ?: 0f,
            backgroundScaleMode = scaleMode,
            fontStyleId = preferences[CFG_FONT] ?: "minimal",
        ).normalized()
    }

    private companion object {
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val USE_SYSTEM_24 = booleanPreferencesKey("use_system_24_hour")
        val SMOOTH_ANALOG_SECONDS = booleanPreferencesKey("smooth_analog_seconds")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery_percentage")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val DIM_AMOUNT = floatPreferencesKey("wallpaper_dim_amount")
        val FAVORITES = stringSetPreferencesKey("favorite_wallpapers")

        val CFG_WALLPAPER_ID = stringPreferencesKey("cfg_wallpaper_id")
        val CFG_STYLE_ID = stringPreferencesKey("cfg_clock_style_id")
        val CFG_X = floatPreferencesKey("cfg_x")
        val CFG_Y = floatPreferencesKey("cfg_y")
        val CFG_SCALE = floatPreferencesKey("cfg_scale")
        val CFG_OPACITY = floatPreferencesKey("cfg_opacity")
        val CFG_SHOW_SECONDS = booleanPreferencesKey("cfg_show_seconds")
        val CFG_SMOOTH_SECONDS = booleanPreferencesKey("cfg_smooth_seconds")
        val CFG_SHOW_DATE = booleanPreferencesKey("cfg_show_date")
        val CFG_SHOW_DAY = booleanPreferencesKey("cfg_show_day")
        val CFG_SHOW_BATTERY = booleanPreferencesKey("cfg_show_battery")
        val CFG_SHOW_AM_PM = booleanPreferencesKey("cfg_show_am_pm")
        val CFG_USE_SYSTEM_24 = booleanPreferencesKey("cfg_use_system_24")
        val CFG_TIME_FORMAT_MODE = stringPreferencesKey("cfg_time_format_mode")
        val CFG_SHOW_NUMBERS = booleanPreferencesKey("cfg_show_numbers")
        val CFG_SHOW_SECOND_HAND = booleanPreferencesKey("cfg_show_second_hand")
        val CFG_TEXT_COLOR = longPreferencesKey("cfg_text_color")
        val CFG_ACCENT_COLOR = longPreferencesKey("cfg_accent_color")
        val CFG_HOUR_COLOR = longPreferencesKey("cfg_hour_hand_color")
        val CFG_MINUTE_COLOR = longPreferencesKey("cfg_minute_hand_color")
        val CFG_SECOND_COLOR = longPreferencesKey("cfg_second_hand_color")
        val CFG_MARKER_COLOR = longPreferencesKey("cfg_marker_color")
        val CFG_BRIGHTNESS = floatPreferencesKey("cfg_brightness")
        val CFG_DIM = floatPreferencesKey("cfg_dim")
        val CFG_BLUR = floatPreferencesKey("cfg_blur")
        val CFG_SCALE_MODE = stringPreferencesKey("cfg_scale_mode")
        val CFG_FONT = stringPreferencesKey("cfg_font")
    }
}

private fun Flow<Preferences>.recoverFromDiskError(): Flow<Preferences> = catch { error ->
    if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
}
