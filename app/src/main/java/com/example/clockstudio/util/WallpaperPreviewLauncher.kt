package com.example.clockstudio.util

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.clockstudio.R
import com.example.clockstudio.wallpaper.ClockWallpaperService

/** Launch Android's real live-wallpaper preview/confirmation surface, with a picker fallback. */
fun launchLiveWallpaperPreview(context: Context): Boolean {
    val changeIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, ClockWallpaperService::class.java),
        )
    }
    val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
    if (context !is Activity) {
        changeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(changeIntent)
        true
    } catch (_: Exception) {
        try {
            context.startActivity(chooserIntent)
            true
        } catch (_: Exception) {
            Toast.makeText(context, R.string.wallpaper_action_unavailable, Toast.LENGTH_LONG).show()
            false
        }
    }
}
