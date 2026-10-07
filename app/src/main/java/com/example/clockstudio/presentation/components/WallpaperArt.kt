package com.example.clockstudio.presentation.components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.wallpaper.WallpaperRenderer

@Composable
fun WallpaperArt(
    item: WallpaperItem,
    modifier: Modifier = Modifier,
    configuration: WallpaperConfiguration? = null,
) {
    val context = LocalContext.current.applicationContext
    var targetSize by remember(item.id) { mutableStateOf(IntSize.Zero) }
    val bitmap = rememberWallpaperBitmap(item, targetSize)
    val renderer = remember(context) { WallpaperRenderer(context) }
    val visualConfiguration = configuration ?: WallpaperConfiguration.forWallpaper(item).copy(dimAmount = 0f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size -> targetSize = size },
    ) {
        drawIntoCanvas { composeCanvas ->
            val bounds = RectF(0f, 0f, size.width, size.height)
            if (configuration != null) {
                renderer.drawPreviewBackground(composeCanvas.nativeCanvas, bounds, item, bitmap, visualConfiguration)
            } else {
                renderer.drawBackground(composeCanvas.nativeCanvas, bounds, item.artworkIndex, bitmap)
            }
        }
    }
}
