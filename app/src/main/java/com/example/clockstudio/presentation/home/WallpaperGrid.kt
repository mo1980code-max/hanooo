package com.example.clockstudio.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockstudio.R
import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.presentation.components.ClockOverlayPreview
import com.example.clockstudio.presentation.components.WallpaperArt
import java.time.ZonedDateTime

@Composable
fun WallpaperGrid(
    category: ClockCategory,
    wallpapers: List<WallpaperItem>,
    time: ZonedDateTime,
    batteryPercent: Int?,
    appSettings: AppSettings,
    onWallpaperClick: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        if (wallpapers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_favorites), color = Color(0xFFBDBDBD))
            }
        } else {
            val columns = when {
                category == ClockCategory.CUSTOM && maxWidth < 600.dp -> GridCells.Fixed(3)
                category == ClockCategory.CUSTOM -> GridCells.Adaptive(minSize = 150.dp)
                else -> GridCells.Adaptive(minSize = 154.dp)
            }
            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 9.dp, end = 9.dp, top = 5.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(items = wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        item = wallpaper,
                        time = time,
                        batteryPercent = batteryPercent,
                        appSettings = appSettings,
                        onClick = { onWallpaperClick(wallpaper) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    item: WallpaperItem,
    time: ZonedDateTime,
    batteryPercent: Int?,
    appSettings: AppSettings,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.wallpaper_name_description, item.title)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (item.category == ClockCategory.CUSTOM) 0.68f else 0.72f)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(0.6.dp, Color(0xFF2B2B2B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp)),
        ) {
            WallpaperArt(item = item)
            if (item.category != ClockCategory.CUSTOM) {
                ClockOverlayPreview(
                    item = item,
                    time = time,
                    batteryPercent = batteryPercent,
                    configuration = WallpaperConfiguration.forWallpaper(item).copy(
                        useSystem24HourFormat = appSettings.useSystem24HourFormat,
                        showSeconds = appSettings.showSeconds,
                        smoothSeconds = appSettings.smoothAnalogSeconds,
                        showDate = appSettings.showDate,
                        showBattery = item.category == ClockCategory.SMART && appSettings.showBatteryPercentage,
                        showSecondHand = item.category == ClockCategory.ANALOG && appSettings.showSeconds,
                        dimAmount = appSettings.wallpaperDimAmount,
                    ),
                    compact = true,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000))))
                    .padding(horizontal = 7.dp, vertical = 7.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
