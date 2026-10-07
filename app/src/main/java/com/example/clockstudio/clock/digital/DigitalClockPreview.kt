package com.example.clockstudio.clock.digital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockstudio.domain.model.DigitalClockStyle
import com.example.clockstudio.domain.model.DigitalLayoutType
import com.example.clockstudio.domain.model.WallpaperConfiguration
import java.time.ZonedDateTime

@Composable
fun DigitalClockPreview(
    style: DigitalClockStyle,
    time: ZonedDateTime,
    modifier: Modifier = Modifier,
    configuration: WallpaperConfiguration? = null,
    compact: Boolean = false,
) {
    val context = LocalContext.current
    val config = configuration
    val use24Hour = ClockTime.uses24HourFormat(context, config?.useSystem24HourFormat ?: true, config?.timeFormatMode ?: com.example.clockstudio.domain.model.TimeFormatMode.SYSTEM)
    val showSeconds = config?.showSeconds ?: style.showSeconds
    val showDate = config?.showDate ?: style.showDate
    val showDay = config?.showDay ?: style.showDay
    val showAmPm = (config?.showAmPm ?: style.showAmPm) && !use24Hour
    val mainColor = Color((config?.textColor ?: style.primaryColor).toInt())
    val accentColor = Color((config?.accentColor ?: style.accentColor).toInt())
    val locale = ClockTime.locale(context)
    val clockTime = ClockTime.time(time, use24Hour, showSeconds, locale)
    val date = ClockTime.date(time, locale)
    val day = ClockTime.day(time, locale)
    val amPm = ClockTime.amPm(time, locale)
    val mainSize = when {
        compact -> 30.sp
        style.layoutType == DigitalLayoutType.TYPOGRAPHIC -> 70.sp
        else -> 56.sp
    }
    val smallSize = if (compact) 10.sp else 13.sp
    val fontFamily = when (config?.fontStyleId ?: style.fontStyleId) {
        "mono" -> FontFamily.Monospace
        "rounded" -> FontFamily.SansSerif
        else -> FontFamily.SansSerif
    }
    val scale = config?.scale ?: 1f
    val positionX = config?.x ?: 0.5f
    val positionY = config?.y ?: 0.40f

    BoxWithConstraints(modifier = modifier) {
        val offsetX = maxWidth * (positionX - 0.5f)
        val offsetY = maxHeight * (positionY - 0.5f)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offsetX, y = offsetY)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = config?.opacity ?: 1f
                }
                .widthIn(max = if (compact) 210.dp else 340.dp)
                .padding(horizontal = if (compact) 4.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (style.layoutType) {
                DigitalLayoutType.STACKED_AM_PM -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        TimeText(clockTime, mainColor, mainSize, fontFamily)
                        if (showAmPm) Text(amPm, color = accentColor, fontSize = smallSize, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 5.dp))
                    }
                    if (showDate) DateText(date, accentColor, smallSize)
                }
                DigitalLayoutType.INLINE_DATE -> {
                    TimeText(clockTime, mainColor, mainSize, fontFamily)
                    if (showDate) DateText(date, accentColor, smallSize)
                }
                DigitalLayoutType.SPACE_DAY -> {
                    TimeText(clockTime.replace(":", "  "), mainColor, mainSize, fontFamily)
                    if (showDay) DateText(day, accentColor, smallSize)
                }
                DigitalLayoutType.VERTICAL_STACK -> {
                    val parts = clockTime.split(":")
                    TimeText(parts.getOrNull(0).orEmpty(), mainColor, mainSize, fontFamily)
                    TimeText(parts.getOrNull(1).orEmpty(), accentColor, mainSize, fontFamily)
                    if (showDate) DateText(date, mainColor, smallSize)
                }
                DigitalLayoutType.LARGE_HOUR_MINUTE -> {
                    val parts = clockTime.split(":")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        TimeText(parts.getOrNull(0).orEmpty(), mainColor, mainSize * 1.15f, fontFamily)
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 8.dp)) {
                            TimeText(parts.getOrNull(1).orEmpty(), accentColor, mainSize * 0.78f, fontFamily)
                            if (showAmPm) Text(amPm, color = accentColor, fontSize = smallSize)
                        }
                    }
                    if (showDate) DateText(date, mainColor, smallSize)
                }
                DigitalLayoutType.TIME_WITH_SIDE_DATE -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        TimeText(clockTime, mainColor, mainSize * 0.82f, fontFamily)
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 8.dp)) {
                            if (showDay) DateText(day, accentColor, smallSize)
                            if (showDate) DateText("%02d".format(time.dayOfMonth), mainColor, smallSize)
                            if (showDate) DateText(ClockTime.month(time, locale), accentColor, smallSize)
                        }
                    }
                }
                DigitalLayoutType.MINIMAL_LINE -> {
                    TimeText(clockTime, mainColor, mainSize, fontFamily)
                    Spacer(Modifier.width(24.dp).height(2.dp).clip(RoundedCornerShape(50)).background(accentColor))
                    if (showDay) DateText(day, accentColor, smallSize)
                }
                DigitalLayoutType.SPLIT_CARDS -> {
                    val parts = clockTime.split(":")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        DigitCard(parts.getOrNull(0).orEmpty(), mainColor, compact)
                        Text(":", color = accentColor, fontSize = mainSize * 0.72f, modifier = Modifier.padding(horizontal = 2.dp))
                        DigitCard(parts.getOrNull(1).orEmpty(), mainColor, compact)
                    }
                    if (showDate) DateText(date, accentColor, smallSize)
                }
                DigitalLayoutType.SECONDS_MICRO -> {
                    TimeText(clockTime, mainColor, mainSize, fontFamily)
                    if (showDate) DateText(date, accentColor, smallSize)
                }
                DigitalLayoutType.RETRO_LED -> {
                    Text(clockTime, color = accentColor, fontSize = mainSize, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
                    if (showDate) DateText(date, mainColor, smallSize)
                }
                DigitalLayoutType.ORBITAL -> {
                    Text(clockTime, color = mainColor, fontSize = mainSize, fontFamily = fontFamily, fontWeight = FontWeight.ExtraLight, textAlign = TextAlign.Center, maxLines = 1)
                    if (showDay) DateText("—  $day  —", accentColor, smallSize)
                }
                DigitalLayoutType.TYPOGRAPHIC -> {
                    val parts = clockTime.split(":")
                    Text(parts.getOrNull(0).orEmpty(), color = mainColor, fontSize = mainSize, fontWeight = FontWeight.ExtraLight, fontFamily = fontFamily, textAlign = TextAlign.Center, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(parts.getOrNull(1).orEmpty(), color = accentColor, fontSize = mainSize * 0.65f, fontWeight = FontWeight.Light, fontFamily = fontFamily)
                        if (showAmPm) Text(amPm, color = mainColor, fontSize = smallSize, modifier = Modifier.padding(start = 6.dp))
                    }
                    if (showDate) DateText(date, mainColor, smallSize)
                }
            }
        }
    }
}

@Composable
private fun TimeText(value: String, color: Color, size: androidx.compose.ui.unit.TextUnit, family: FontFamily) {
    Text(
        text = value,
        color = color,
        fontSize = size,
        fontWeight = FontWeight.Light,
        fontFamily = family,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun DateText(value: String, color: Color, size: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = value,
        color = color,
        fontSize = size,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun DigitCard(value: String, color: Color, compact: Boolean) {
    Surface(
        color = Color(0xAA11191B),
        shape = RoundedCornerShape(if (compact) 8.dp else 14.dp),
        modifier = Modifier.padding(horizontal = 2.dp),
    ) {
        Text(
            value,
            color = color,
            fontSize = if (compact) 27.sp else 45.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 12.dp, vertical = 4.dp),
        )
    }
}
