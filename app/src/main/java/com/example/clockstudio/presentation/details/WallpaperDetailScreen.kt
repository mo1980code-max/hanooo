package com.example.clockstudio.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockstudio.R
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.presentation.components.ClockOverlayPreview
import com.example.clockstudio.presentation.components.WallpaperArt
import com.example.clockstudio.presentation.components.rememberBatteryInfo
import com.example.clockstudio.presentation.components.rememberCurrentTime
import com.example.clockstudio.util.BatteryInfo
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailScreen(
    item: WallpaperItem,
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit,
    onCustomize: (WallpaperConfiguration) -> Unit,
    onSetWallpaper: (WallpaperConfiguration) -> Unit,
) {
    val savedConfiguration by preferencesRepository.configurationFlow.collectAsStateWithLifecycle(
        initialValue = WallpaperConfiguration(),
    )
    val settings by preferencesRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
    val configuration = remember(item.id, savedConfiguration, settings) {
        if (savedConfiguration.wallpaperId == item.id) savedConfiguration
        else {
            val defaults = WallpaperConfiguration.forWallpaper(item)
            defaults.copy(
                useSystem24HourFormat = settings.useSystem24HourFormat,
                showSeconds = settings.showSeconds,
                smoothSeconds = settings.smoothAnalogSeconds,
                showDate = settings.showDate,
                showDay = defaults.showDay,
                showBattery = defaults.showBattery && settings.showBatteryPercentage,
                showSecondHand = defaults.showSecondHand && settings.showSeconds,
                dimAmount = settings.wallpaperDimAmount,
            )
        }
    }
    val favorites by preferencesRepository.favoritesFlow.collectAsStateWithLifecycle(initialValue = emptySet())
    val isFavorite = item.id in favorites
    val scope = rememberCoroutineScope()
    val tickSeconds = configuration.showSeconds || (item.category == ClockCategory.ANALOG && configuration.showSecondHand)
    val time = rememberCurrentTime(tickSeconds)
    val battery: BatteryInfo? = rememberBatteryInfo(active = item.category == ClockCategory.SMART)

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        WallpaperArt(item = item, modifier = Modifier.fillMaxSize(), configuration = configuration)
        ClockOverlayPreview(
            item = item,
            time = time,
            batteryPercent = battery?.percent,
            configuration = configuration,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.FilledTonalIconButton(
                onClick = onBack,
                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0x990B0F11),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    androidx.compose.material.icons.automirrored.filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = item.title,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xFF000000)),
                        startY = 0f,
                    ),
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.wallpaper_set_hint),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 3.dp, bottom = 12.dp),
            )
            Button(
                onClick = { onSetWallpaper(configuration) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009B83), contentColor = Color.White),
            ) {
                Text(stringResource(R.string.set_wallpaper), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onCustomize(configuration) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.customize), color = Color.White)
                }
                OutlinedButton(
                    onClick = { scope.launch { preferencesRepository.toggleFavorite(item.id) } },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(if (isFavorite) R.string.unfavorite else R.string.favorite),
                        tint = if (isFavorite) Color(0xFFFF5C7A) else Color.White,
                    )
                }
            }
        }
    }
}
