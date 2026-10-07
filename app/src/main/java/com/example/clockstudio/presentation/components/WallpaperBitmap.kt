package com.example.clockstudio.presentation.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.wallpaper.WallpaperBitmapLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberWallpaperBitmap(item: WallpaperItem, targetSize: IntSize): Bitmap? {
    val context = LocalContext.current.applicationContext
    var bitmap by remember(item.id, targetSize) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(item.id, item.assetFileName, item.backgroundResource, targetSize) {
        bitmap = if (targetSize.width > 0 && targetSize.height > 0) {
            withContext(Dispatchers.IO) {
                WallpaperBitmapLoader.load(context, item, targetSize.width, targetSize.height)
            }
        } else null
    }
    return bitmap
}

fun Bitmap?.toComposeImageBitmapOrNull() = this?.asImageBitmap()
