package com.example.clockstudio.clock.digital

import android.content.Context
import android.text.format.DateFormat
import com.example.clockstudio.domain.model.TimeFormatMode
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClockTime {
    fun locale(context: Context): Locale = if (android.os.Build.VERSION.SDK_INT >= 24) {
        context.resources.configuration.locales[0] ?: Locale.getDefault()
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale ?: Locale.getDefault()
    }

    fun uses24HourFormat(
        context: Context,
        useSystemFormat: Boolean,
        mode: TimeFormatMode = TimeFormatMode.SYSTEM,
    ): Boolean = when (mode) {
        TimeFormatMode.SYSTEM -> if (useSystemFormat) DateFormat.is24HourFormat(context) else false
        TimeFormatMode.TWELVE_HOUR -> false
        TimeFormatMode.TWENTY_FOUR_HOUR -> true
    }

    fun time(
        time: ZonedDateTime,
        use24Hour: Boolean,
        showSeconds: Boolean = false,
        locale: Locale = Locale.getDefault(),
    ): String {
        val pattern = when {
            use24Hour && showSeconds -> "HH:mm:ss"
            use24Hour -> "HH:mm"
            showSeconds -> "hh:mm:ss"
            else -> "hh:mm"
        }
        return DateTimeFormatter.ofPattern(pattern, locale).format(time)
    }

    fun date(time: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern("EEE dd MMM", locale).format(time).uppercase(locale)

    fun day(time: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern("EEE", locale).format(time).uppercase(locale)

    fun month(time: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern("MMM", locale).format(time).uppercase(locale)

    fun dayAndDate(time: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern("EEE dd", locale).format(time).uppercase(locale)

    fun amPm(time: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern("a", locale).format(time).uppercase(locale)
}
