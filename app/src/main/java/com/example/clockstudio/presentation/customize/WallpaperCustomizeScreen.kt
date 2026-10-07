package com.example.clockstudio.presentation.customize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockstudio.R
import com.example.clockstudio.clock.ClockMath
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.BackgroundScaleMode
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.ClockPalette
import com.example.clockstudio.domain.model.TimeFormatMode
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.presentation.components.ClockOverlayPreview
import com.example.clockstudio.presentation.components.WallpaperArt
import com.example.clockstudio.presentation.components.rememberBatteryInfo
import com.example.clockstudio.presentation.components.rememberCurrentTime
import kotlinx.coroutines.launch
import android.widget.Toast

private val colorChoices = listOf(
    ClockPalette.WHITE,
    ClockPalette.TEAL,
    ClockPalette.CYAN,
    ClockPalette.GOLD,
    ClockPalette.PURPLE,
    0xFFFF6B9AL,
    0xFF93E46AL,
)

@Composable
fun WallpaperCustomizeScreen(
    item: WallpaperItem,
    initialConfiguration: WallpaperConfiguration,
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit,
    onSetWallpaper: (WallpaperConfiguration) -> Unit,
) {
    var configuration by remember(item.id, initialConfiguration) {
        mutableStateOf(initialConfiguration.copy(wallpaperId = item.id).normalized())
    }
    val latestConfiguration by rememberUpdatedState(configuration)
    val currentTime = rememberCurrentTime(configuration.showSeconds || configuration.showSecondHand)
    val battery = rememberBatteryInfo(active = item.category == ClockCategory.SMART)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings by preferencesRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 91.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                    Text(
                        text = stringResource(R.string.customize),
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(item.title, color = Color(0xFFBDBDBD), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 390.dp)
                        .aspectRatio(0.78f)
                        .clip(RoundedCornerShape(20.dp))
                        .pointerInput(item.id) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val current = latestConfiguration
                                val point = ClockMath.normalizedPosition(
                                    current.x + pan.x / size.width.coerceAtLeast(1),
                                    current.y + pan.y / size.height.coerceAtLeast(1),
                                )
                                configuration = current.copy(
                                    x = point.first,
                                    y = point.second,
                                    scale = (current.scale * zoom).coerceIn(0.45f, 1.65f),
                                )
                            }
                        },
                ) {
                    WallpaperArt(item, Modifier.fillMaxSize(), configuration)
                    ClockOverlayPreview(
                        item = item,
                        time = currentTime,
                        batteryPercent = battery?.percent,
                        configuration = configuration,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    stringResource(R.string.drag_clock_hint),
                    color = Color(0xFF9EAAA8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 14.dp),
                )

                SectionTitle(stringResource(R.string.clock))
                ConfigSlider(stringResource(R.string.clock_scale), configuration.scale, 0.45f..1.65f, { configuration = configuration.copy(scale = it) }, "%.2f×".format(configuration.scale))
                ConfigSlider(stringResource(R.string.x_position), configuration.x, 0f..1f, { configuration = configuration.copy(x = it) }, "${(configuration.x * 100).toInt()}%")
                ConfigSlider(stringResource(R.string.y_position), configuration.y, 0f..1f, { configuration = configuration.copy(y = it) }, "${(configuration.y * 100).toInt()}%")
                ConfigSlider(stringResource(R.string.opacity), configuration.opacity, 0.15f..1f, { configuration = configuration.copy(opacity = it) }, "${(configuration.opacity * 100).toInt()}%")

                if (item.category != ClockCategory.ANALOG) {
                    SectionTitle(stringResource(R.string.text_color))
                    ColorPicker(stringResource(R.string.text_color), configuration.textColor) { configuration = configuration.copy(textColor = it) }
                    SectionTitle(stringResource(R.string.accent_color))
                    ColorPicker(stringResource(R.string.accent_color), configuration.accentColor) { configuration = configuration.copy(accentColor = it) }
                }

                if (item.category == ClockCategory.CUSTOM || item.category == ClockCategory.DIGITAL || item.category == ClockCategory.SMART) {
                    if (item.category != ClockCategory.SMART) {
                        ToggleControl(stringResource(R.string.show_seconds), configuration.showSeconds) { configuration = configuration.copy(showSeconds = it) }
                        ToggleControl(stringResource(R.string.show_am_pm), configuration.showAmPm) { configuration = configuration.copy(showAmPm = it) }
                        SectionTitle(stringResource(R.string.time_format))
                        FormatModePicker(configuration.timeFormatMode) { configuration = configuration.copy(timeFormatMode = it, useSystem24HourFormat = settings.useSystem24HourFormat) }
                        SectionTitle(stringResource(R.string.font_style))
                        FontPicker(configuration.fontStyleId) { configuration = configuration.copy(fontStyleId = it) }
                    }
                    ToggleControl(stringResource(R.string.show_date), configuration.showDate) { configuration = configuration.copy(showDate = it) }
                    ToggleControl(stringResource(R.string.show_day), configuration.showDay) { configuration = configuration.copy(showDay = it) }
                    if (item.category == ClockCategory.SMART) {
                        ToggleControl(stringResource(R.string.show_battery), configuration.showBattery) { configuration = configuration.copy(showBattery = it) }
                    }
                }

                if (item.category == ClockCategory.ANALOG) {
                    SectionTitle(stringResource(R.string.hour_hand_color))
                    ColorPicker(stringResource(R.string.hour_hand_color), configuration.hourHandColor) { configuration = configuration.copy(hourHandColor = it) }
                    SectionTitle(stringResource(R.string.minute_hand_color))
                    ColorPicker(stringResource(R.string.minute_hand_color), configuration.minuteHandColor) { configuration = configuration.copy(minuteHandColor = it) }
                    SectionTitle(stringResource(R.string.second_hand_color))
                    ColorPicker(stringResource(R.string.second_hand_color), configuration.secondHandColor) { configuration = configuration.copy(secondHandColor = it) }
                    SectionTitle(stringResource(R.string.marker_color))
                    ColorPicker(stringResource(R.string.marker_color), configuration.markerColor) { configuration = configuration.copy(markerColor = it) }
                    ToggleControl(stringResource(R.string.show_numbers), configuration.showNumbers) { configuration = configuration.copy(showNumbers = it) }
                    ToggleControl(stringResource(R.string.show_second_hand), configuration.showSecondHand) { configuration = configuration.copy(showSecondHand = it) }
                    ToggleControl(stringResource(R.string.smooth_analog_seconds), configuration.smoothSeconds) { configuration = configuration.copy(smoothSeconds = it) }
                }

                SectionTitle(stringResource(R.string.wallpaper))
                ConfigSlider(stringResource(R.string.brightness), configuration.brightness, 0.35f..1.35f, { configuration = configuration.copy(brightness = it) }, "${(configuration.brightness * 100).toInt()}%")
                ConfigSlider(stringResource(R.string.dim_amount), configuration.dimAmount, 0f..0.75f, { configuration = configuration.copy(dimAmount = it) }, "${(configuration.dimAmount * 100).toInt()}%")
                SectionTitle(stringResource(R.string.background_scale))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeButton(
                        text = stringResource(R.string.center_crop),
                        selected = configuration.backgroundScaleMode == BackgroundScaleMode.CENTER_CROP,
                        modifier = Modifier.weight(1f),
                    ) { configuration = configuration.copy(backgroundScaleMode = BackgroundScaleMode.CENTER_CROP) }
                    ModeButton(
                        text = stringResource(R.string.fit),
                        selected = configuration.backgroundScaleMode == BackgroundScaleMode.FIT,
                        modifier = Modifier.weight(1f),
                    ) { configuration = configuration.copy(backgroundScaleMode = BackgroundScaleMode.FIT) }
                }
                Spacer(Modifier.height(22.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF080A0A))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { configuration = WallpaperConfiguration.forWallpaper(item) },
                    modifier = Modifier.height(48.dp),
                ) { Text(stringResource(R.string.reset), color = Color.White) }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.saveConfiguration(configuration)
                            Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                ) { Text(stringResource(R.string.save), color = Color.White) }
                Button(
                    onClick = {
                        scope.launch {
                            preferencesRepository.saveConfiguration(configuration)
                            onSetWallpaper(configuration)
                        }
                    },
                    modifier = Modifier.weight(1.35f).height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009B83)),
                ) { Text(stringResource(R.string.set_wallpaper), maxLines = 1) }
            }
        }
    }
}

@Composable
private fun SectionTitle(value: String) {
    Text(
        text = value,
        color = Color(0xFF00BFA5),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 15.dp, bottom = 3.dp),
    )
}

@Composable
private fun ConfigSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    valueLabel: String,
) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(valueLabel, color = Color(0xFFBDBDBD), style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value.coerceIn(valueRange), onValueChange = onChange, valueRange = valueRange)
    }
}

@Composable
private fun ToggleControl(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.bodyMedium)
        androidx.compose.material3.Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorPicker(label: String, selectedColor: Long, onSelect: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colorChoices.forEachIndexed { index, color ->
            val selected = color == selectedColor
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "$label ${index + 1}" }
                    .clickable(role = Role.RadioButton) { onSelect(color) },
                shape = CircleShape,
                color = Color(color.toInt()),
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Color.White else Color(0xFF666666)),
            ) {
                if (selected) Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = if (color == ClockPalette.WHITE) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FormatModePicker(selected: TimeFormatMode, onSelect: (TimeFormatMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            TimeFormatMode.SYSTEM to R.string.system_default,
            TimeFormatMode.TWELVE_HOUR to R.string.twelve_hour,
            TimeFormatMode.TWENTY_FOUR_HOUR to R.string.twenty_four_hour,
        ).forEach { (mode, label) ->
            ModeButton(stringResource(label), mode == selected, Modifier.weight(1f)) { onSelect(mode) }
        }
    }
}

@Composable
private fun FontPicker(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(
            "minimal" to R.string.font_minimal,
            "rounded" to R.string.font_rounded,
            "mono" to R.string.font_mono,
        ).forEach { (key, label) ->
            ModeButton(stringResource(label), selected == key, Modifier.weight(1f)) { onSelect(key) }
        }
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) Color(0xFF006B5B) else Color(0xFF181818),
        border = BorderStroke(1.dp, if (selected) Color(0xFF00BFA5) else Color(0xFF343434)),
        contentColor = Color.White,
    ) {
        Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
