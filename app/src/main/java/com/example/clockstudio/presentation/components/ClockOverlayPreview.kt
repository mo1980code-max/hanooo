package com.example.clockstudio.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.clockstudio.clock.analog.AnalogClockPreview
import com.example.clockstudio.clock.digital.DigitalClockPreview
import com.example.clockstudio.clock.smart.SmartClockPreview
import com.example.clockstudio.domain.model.AnalogClockCatalog
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.DigitalClockCatalog
import com.example.clockstudio.domain.model.SmartClockCatalog
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import java.time.ZonedDateTime

@Composable
fun ClockOverlayPreview(
    item: WallpaperItem,
    time: ZonedDateTime,
    batteryPercent: Int?,
    configuration: WallpaperConfiguration,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (item.category) {
            ClockCategory.CUSTOM, ClockCategory.DIGITAL -> {
                val base = DigitalClockCatalog.find(item.clockStyleId)
                DigitalClockPreview(
                    style = base.copy(
                        primaryColor = configuration.textColor,
                        accentColor = configuration.accentColor,
                        showDate = configuration.showDate,
                        showDay = configuration.showDay,
                        showSeconds = configuration.showSeconds,
                        showAmPm = configuration.showAmPm,
                        fontStyleId = configuration.fontStyleId,
                    ),
                    time = time,
                    modifier = Modifier.fillMaxSize(),
                    configuration = configuration,
                    compact = compact,
                )
            }
            ClockCategory.ANALOG -> {
                val base = AnalogClockCatalog.find(item.clockStyleId)
                AnalogClockPreview(
                    style = base.copy(
                        showNumbers = configuration.showNumbers,
                        hourHandColor = configuration.hourHandColor,
                        minuteHandColor = configuration.minuteHandColor,
                        secondHandColor = configuration.secondHandColor,
                        markerColor = configuration.markerColor,
                    ),
                    time = time,
                    modifier = Modifier.fillMaxSize(),
                    configuration = configuration,
                    compact = compact,
                )
            }
            ClockCategory.SMART -> {
                SmartClockPreview(
                    style = SmartClockCatalog.find(item.clockStyleId),
                    time = time,
                    batteryPercent = batteryPercent,
                    modifier = Modifier.fillMaxSize(),
                    configuration = configuration,
                    compact = compact,
                )
            }
        }
    }
}
