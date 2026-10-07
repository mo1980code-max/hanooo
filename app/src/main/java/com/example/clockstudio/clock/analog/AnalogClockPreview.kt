package com.example.clockstudio.clock.analog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.clockstudio.domain.model.AnalogClockStyle
import com.example.clockstudio.domain.model.WallpaperConfiguration
import java.time.ZonedDateTime
import kotlin.math.min

@Composable
fun AnalogClockPreview(
    style: AnalogClockStyle,
    time: ZonedDateTime,
    modifier: Modifier = Modifier,
    configuration: WallpaperConfiguration? = null,
    compact: Boolean = false,
) {
    val config = configuration ?: WallpaperConfiguration(
        clockStyleId = style.id,
        x = 0.5f,
        y = 0.43f,
        scale = 1f,
        opacity = 0.92f,
        showNumbers = style.showNumbers,
        showSecondHand = true,
        hourHandColor = style.hourHandColor,
        minuteHandColor = style.minuteHandColor,
        secondHandColor = style.secondHandColor,
        markerColor = style.markerColor,
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width * config.x
        val centerY = size.height * config.y
        val baseDiameter = min(size.width, size.height) * if (compact) 0.44f else 0.50f
        val diameter = baseDiameter * config.scale
        drawIntoCanvas { canvas ->
            AnalogClockRenderer.draw(
                canvas = canvas.nativeCanvas,
                centerX = centerX,
                centerY = centerY,
                diameter = diameter,
                time = time,
                style = style.copy(
                    showNumbers = config.showNumbers,
                    hourHandColor = config.hourHandColor,
                    minuteHandColor = config.minuteHandColor,
                    secondHandColor = config.secondHandColor,
                    markerColor = config.markerColor,
                ),
                configuration = config,
            )
        }
    }
}
