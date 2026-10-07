package com.example.clockstudio.wallpaper

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.data.repository.LocalWallpaperRepository
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.util.batteryInfoFrom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.Locale

/** Battery-aware Android live wallpaper. No frames are posted while the system hides the wallpaper. */
class ClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockWallpaperEngine()

    inner class ClockWallpaperEngine : Engine() {
        private val renderThread = HandlerThread("ClockStudioWallpaper").apply { start() }
        private val renderHandler = Handler(renderThread.looper)
        private val mainHandler = Handler(Looper.getMainLooper())
        private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val preferences = UserPreferencesRepository(applicationContext)
        private val repository = LocalWallpaperRepository(applicationContext)
        private val renderer = WallpaperRenderer(applicationContext)

        @Volatile private var isVisible = false
        @Volatile private var destroyed = false
        @Volatile private var surfaceReady = false
        @Volatile private var configuration = WallpaperConfiguration()
        @Volatile private var wallpaper = repository.getById(configuration.wallpaperId)!!
        @Volatile private var wallpaperBitmap: android.graphics.Bitmap? = null
        @Volatile private var batteryPercent: Int? = null
        @Volatile private var activeLocale: Locale = Locale.getDefault()
        @Volatile private var surfaceWidth = 0
        @Volatile private var surfaceHeight = 0
        private var timeReceiverRegistered = false
        private var batteryReceiverRegistered = false

        private val frameTask = object : Runnable {
            override fun run() {
                if (!isVisible || !surfaceReady) return
                renderOneFrame()
                scheduleNextFrame()
            }
        }

        private val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_TIMEZONE_CHANGED || intent?.action == Intent.ACTION_TIME_CHANGED ||
                    intent?.action == Intent.ACTION_DATE_CHANGED || intent?.action == Intent.ACTION_TIME_TICK
                ) {
                    requestImmediateFrame()
                }
            }
        }

        private val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != Intent.ACTION_BATTERY_CHANGED || intent == null) return
                batteryPercent = batteryInfoFrom(
                    level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                    scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1),
                    status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1),
                )?.percent
                requestImmediateFrame()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
            ioScope.launch {
                combine(preferences.configurationFlow, preferences.settingsFlow) { updated, settings ->
                    updated to settings.languageTag
                }.collectLatest { (updated, languageTag) ->
                    configuration = updated.normalized()
                    activeLocale = Locale.forLanguageTag(languageTag.ifBlank { Locale.getDefault().toLanguageTag() })
                    wallpaper = repository.getById(updated.wallpaperId) ?: repository.getById("custom_01")!!
                    loadWallpaperBitmap(wallpaper, surfaceWidth, surfaceHeight)
                    mainHandler.post {
                        if (!destroyed) {
                            updateBatteryReceiver()
                            requestImmediateFrame()
                        }
                    }
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                registerTimeReceiver()
                updateBatteryReceiver()
                requestImmediateFrame()
            } else {
                renderHandler.removeCallbacks(frameTask)
                unregisterReceivers()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            surfaceReady = width > 0 && height > 0
            loadWallpaperBitmap(wallpaper, width, height)
            requestImmediateFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            renderHandler.removeCallbacks(frameTask)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            destroyed = true
            isVisible = false
            surfaceReady = false
            mainHandler.removeCallbacksAndMessages(null)
            renderHandler.removeCallbacksAndMessages(null)
            unregisterReceivers()
            ioScope.cancel(CancellationException("Wallpaper engine destroyed"))
            renderThread.quitSafely()
            super.onDestroy()
        }

        private fun loadWallpaperBitmap(item: WallpaperItem, width: Int, height: Int) {
            val targetWidth = width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
            val targetHeight = height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
            ioScope.launch {
                val decoded = WallpaperBitmapLoader.load(applicationContext, item, targetWidth, targetHeight)
                if (wallpaper.id == item.id && configuration.wallpaperId == item.id) {
                    wallpaperBitmap = decoded
                    requestImmediateFrame()
                }
            }
        }

        private fun requestImmediateFrame() {
            if (destroyed || !isVisible || !surfaceReady) return
            renderHandler.removeCallbacks(frameTask)
            renderHandler.post(frameTask)
        }

        private fun scheduleNextFrame() {
            if (destroyed || !isVisible || !surfaceReady) return
            val current = configuration
            val interval = when {
                wallpaper.category == ClockCategory.ANALOG && current.showSecondHand && current.smoothSeconds -> 33L
                current.showSeconds || (wallpaper.category == ClockCategory.ANALOG && current.showSecondHand) -> 1_000L
                else -> (60_000L - System.currentTimeMillis().mod(60_000L) + 35L).coerceAtLeast(250L)
            }
            renderHandler.postDelayed(frameTask, interval)
        }

        private fun renderOneFrame() {
            if (destroyed || !isVisible || !surfaceReady) return
            val holder = surfaceHolder
            var canvas: android.graphics.Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderer.draw(
                        canvas = canvas,
                        item = wallpaper,
                        configuration = configuration,
                        time = ZonedDateTime.now(),
                        batteryPercent = batteryPercent,
                        bitmap = wallpaperBitmap,
                        locale = activeLocale,
                    )
                }
            } catch (_: IllegalArgumentException) {
                // Surface can be invalidated between the visibility check and lockCanvas.
            } catch (_: IllegalStateException) {
                // System wallpaper host can destroy/recreate the surface while a frame is queued.
            } finally {
                if (canvas != null) {
                    runCatching { holder.unlockCanvasAndPost(canvas) }
                }
            }
        }

        private fun registerTimeReceiver() {
            if (timeReceiverRegistered) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
            }
            ContextCompat.registerReceiver(
                this@ClockWallpaperService,
                timeReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            timeReceiverRegistered = true
        }

        private fun updateBatteryReceiver() {
            if (isVisible && wallpaper.category == ClockCategory.SMART && configuration.showBattery) {
                if (batteryReceiverRegistered) return
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val sticky = ContextCompat.registerReceiver(
                    this@ClockWallpaperService,
                    batteryReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
                batteryReceiverRegistered = true
                if (sticky != null) batteryReceiver.onReceive(this@ClockWallpaperService, sticky)
            } else if (batteryReceiverRegistered) {
                runCatching { unregisterReceiver(batteryReceiver) }
                batteryReceiverRegistered = false
                batteryPercent = null
            }
        }

        private fun unregisterReceivers() {
            if (timeReceiverRegistered) {
                runCatching { unregisterReceiver(timeReceiver) }
                timeReceiverRegistered = false
            }
            if (batteryReceiverRegistered) {
                runCatching { unregisterReceiver(batteryReceiver) }
                batteryReceiverRegistered = false
            }
            batteryPercent = null
        }
    }
}
