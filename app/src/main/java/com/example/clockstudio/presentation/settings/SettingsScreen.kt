package com.example.clockstudio.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockstudio.R
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.presentation.components.PreferenceSwitchRow
import com.example.clockstudio.presentation.components.ScreenTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
) {
    val settings by preferencesRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var dimDraft by remember(settings.wallpaperDimAmount) { mutableStateOf(settings.wallpaperDimAmount) }

    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = { ScreenTopBar(stringResource(R.string.settings_title), onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
        ) {
            SectionHeading(stringResource(R.string.language))
            ClickSettingRow(
                title = stringResource(R.string.language),
                subtitle = languageName(settings.languageTag),
                onClick = onLanguage,
            )

            SectionHeading(stringResource(R.string.clock))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.default_clock_category), color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.set_default_category), color = Color(0xFFBDBDBD), style = MaterialTheme.typography.bodySmall)
                }
                androidx.compose.foundation.layout.Box {
                    androidx.compose.material3.TextButton(onClick = { categoryMenuExpanded = true }) {
                        Text(stringResource(settings.defaultCategory.labelRes()), color = Color(0xFF00BFA5), maxLines = 1)
                        androidx.compose.material3.Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF00BFA5))
                    }
                    DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        ClockCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(stringResource(category.labelRes())) },
                                onClick = {
                                    categoryMenuExpanded = false
                                    scope.launch { preferencesRepository.setDefaultCategory(category) }
                                },
                            )
                        }
                    }
                }
            }
            PreferenceSwitchRow(stringResource(R.string.use_system_24_hour), settings.useSystem24HourFormat) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(useSystem24HourFormat = checked) } }
            }
            PreferenceSwitchRow(stringResource(R.string.smooth_analog_seconds), settings.smoothAnalogSeconds) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(smoothAnalogSeconds = checked) } }
            }
            PreferenceSwitchRow(stringResource(R.string.show_seconds), settings.showSeconds) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(showSeconds = checked) } }
            }
            PreferenceSwitchRow(stringResource(R.string.show_date), settings.showDate) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(showDate = checked) } }
            }
            PreferenceSwitchRow(stringResource(R.string.show_battery_percentage), settings.showBatteryPercentage) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(showBatteryPercentage = checked) } }
            }
            PreferenceSwitchRow(stringResource(R.string.haptic_feedback), settings.hapticFeedback) { checked ->
                scope.launch { preferencesRepository.updateSettings { it.copy(hapticFeedback = checked) } }
            }

            SectionHeading(stringResource(R.string.wallpaper))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.wallpaper_dim_amount), modifier = Modifier.weight(1f), color = Color.White)
                    Text("${(dimDraft * 100).toInt()}%", color = Color(0xFFBDBDBD), style = MaterialTheme.typography.labelMedium)
                }
                Slider(
                    value = dimDraft,
                    onValueChange = { dimDraft = it },
                    onValueChangeFinished = {
                        scope.launch { preferencesRepository.updateSettings { it.copy(wallpaperDimAmount = dimDraft) } }
                    },
                    valueRange = 0f..0.75f,
                )
            }
            Button(
                onClick = { scope.launch { preferencesRepository.resetCustomization() } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text(stringResource(R.string.reset_customization)) }

            SectionHeading(stringResource(R.string.privacy_title))
            ClickSettingRow(stringResource(R.string.privacy_policy), null, onPrivacy)
            ClickSettingRow(stringResource(R.string.about_title), null, onAbout)
            ClickSettingRow(stringResource(R.string.app_version), versionName(), onClick = null)
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        color = Color(0xFF00BFA5),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 5.dp),
    )
}

@Composable
private fun ClickSettingRow(title: String, subtitle: String?, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, color = Color(0xFFBDBDBD), style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) {
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}

@Composable
private fun versionName(): String = try {
    val context = androidx.compose.ui.platform.LocalContext.current
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
} catch (_: Exception) {
    "1.0.0"
}

private fun ClockCategory.labelRes(): Int = when (this) {
    ClockCategory.CUSTOM -> R.string.category_custom
    ClockCategory.DIGITAL -> R.string.category_digital
    ClockCategory.ANALOG -> R.string.category_analog
    ClockCategory.SMART -> R.string.category_smart
}

@Composable
private fun languageName(tag: String): String = when (tag.substringBefore('-')) {
    "ar" -> stringResource(R.string.language_arabic)
    "tr" -> stringResource(R.string.language_turkish)
    "es" -> stringResource(R.string.language_spanish)
    "ru" -> stringResource(R.string.language_russian)
    "fr" -> stringResource(R.string.language_french)
    "pt" -> stringResource(R.string.language_portuguese)
    "id", "in" -> stringResource(R.string.language_indonesian)
    else -> stringResource(R.string.language_english)
}
