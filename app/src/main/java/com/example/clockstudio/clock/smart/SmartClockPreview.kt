package com.example.clockstudio.clock.smart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockstudio.clock.digital.ClockTime
import com.example.clockstudio.domain.model.SmartClockLayout
import com.example.clockstudio.domain.model.SmartClockStyle
import com.example.clockstudio.domain.model.WallpaperConfiguration
import java.time.ZonedDateTime

@Composable
fun SmartClockPreview(
    style: SmartClockStyle,
    time: ZonedDateTime,
    batteryPercent: Int?,
    modifier: Modifier = Modifier,
    configuration: WallpaperConfiguration? = null,
    compact: Boolean = false,
) {
    val primary = Color((configuration?.textColor ?: style.primaryColor).toInt())
    val accent = Color((configuration?.accentColor ?: style.accentColor).toInt())
    val config = configuration
    val showDate = config?.showDate ?: true
    val showDay = config?.showDay ?: true
    val showBattery = config?.showBattery ?: true
    val timeFontSize = if (compact) 28.sp else 55.sp
    val context = androidx.compose.ui.platform.LocalContext.current
    val use24Hour = ClockTime.uses24HourFormat(
        context,
        config?.useSystem24HourFormat ?: true,
        config?.timeFormatMode ?: com.example.clockstudio.domain.model.TimeFormatMode.SYSTEM,
    )
    val locale = ClockTime.locale(context)
    val timeText = ClockTime.time(time, use24Hour = use24Hour, showSeconds = config?.showSeconds == true, locale = locale)
    val dateText = if (style.showMonth) ClockTime.date(time, locale) else ClockTime.dayAndDate(time, locale)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val offsetX = maxWidth * ((config?.x ?: 0.5f) - 0.5f)
        val offsetY = maxHeight * ((config?.y ?: 0.40f) - 0.5f)
        if (style.ring) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * (config?.x ?: 0.5f), size.height * (config?.y ?: 0.40f))
                val radius = minOf(size.width, size.height) * if (compact) 0.34f else 0.29f
                drawCircle(
                    color = accent.copy(alpha = 0.15f),
                    radius = radius * 1.16f,
                    center = center,
                )
                drawCircle(
                    color = accent.copy(alpha = 0.75f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (compact) 2.dp.toPx() else 3.dp.toPx()),
                )
                drawArc(
                    color = primary.copy(alpha = 0.86f),
                    startAngle = -90f,
                    sweepAngle = 218f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 1.10f, center.y - radius * 1.10f),
                    size = Size(radius * 2.2f, radius * 2.2f),
                    style = Stroke(width = if (compact) 1.dp.toPx() else 2.dp.toPx()),
                )
            }
        }

        val panelModifier = Modifier
            .align(Alignment.Center)
            .offset(x = offsetX, y = offsetY)
            .graphicsLayer {
                val scale = config?.scale ?: 1f
                scaleX = scale
                scaleY = scale
                alpha = config?.opacity ?: 1f
            }
            .then(
                if (style.glass || style.layoutType == SmartClockLayout.PANEL || style.layoutType == SmartClockLayout.CYBERPUNK) {
                    Modifier.clip(RoundedCornerShape(if (compact) 12.dp else 22.dp))
                        .background(Color(0x99121B20))
                        .padding(horizontal = if (compact) 9.dp else 20.dp, vertical = if (compact) 8.dp else 16.dp)
                } else Modifier.padding(horizontal = if (compact) 6.dp else 14.dp, vertical = 8.dp),
            )

        Column(
            modifier = panelModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = timeText,
                color = primary,
                fontSize = timeFontSize,
                fontFamily = if (style.layoutType == SmartClockLayout.CYBERPUNK) FontFamily.Monospace else FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
            if (showDay || showDate) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (showDay) SmallInfo(ClockTime.day(time, locale), accent, compact)
                    if (showDay && showDate) Spacer(Modifier.width(if (compact) 5.dp else 8.dp))
                    if (showDate) SmallInfo(dateText, primary.copy(alpha = 0.86f), compact)
                }
            }
            if (showBattery && batteryPercent != null) {
                Spacer(Modifier.height(if (compact) 3.dp else 7.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(
                        Modifier
                            .width(if (compact) 17.dp else 25.dp)
                            .height(if (compact) 8.dp else 11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .drawBehind {
                                drawRoundRect(color = accent, style = Stroke(width = 1.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                                val inset = 2.dp.toPx()
                                drawRoundRect(
                                    color = accent,
                                    topLeft = Offset(inset, inset),
                                    size = Size((size.width - inset * 2) * (batteryPercent.coerceIn(0, 100) / 100f), size.height - inset * 2),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()),
                                )
                            },
                    )
                    Text("$batteryPercent%", color = accent, fontSize = if (compact) 9.sp else 12.sp, modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun SmallInfo(value: String, color: Color, compact: Boolean) {
    Text(
        text = value,
        color = color,
        fontSize = if (compact) 8.sp else 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
        maxLines = 1,
        softWrap = false,
    )
}
