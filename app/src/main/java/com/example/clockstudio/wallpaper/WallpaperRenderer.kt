package com.example.clockstudio.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import com.example.clockstudio.clock.analog.AnalogClockRenderer
import com.example.clockstudio.clock.digital.ClockTime
import com.example.clockstudio.domain.model.AnalogClockCatalog
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.DigitalClockCatalog
import com.example.clockstudio.domain.model.BackgroundScaleMode
import com.example.clockstudio.domain.model.DigitalLayoutType
import com.example.clockstudio.domain.model.SmartClockCatalog
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.domain.model.WallpaperItem
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Native Canvas renderer shared by system wallpaper surfaces and generated art previews. */
class WallpaperRenderer(private val context: Context) {
    @Volatile private var drawingLocale: Locale = Locale.getDefault()
    private val backgroundCacheLock = Any()
    private val cachedBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    private var cachedBackgroundKey: BackgroundCacheKey? = null
    private var cachedBackgroundBitmap: Bitmap? = null
    private var failedBackgroundKey: BackgroundCacheKey? = null

    fun draw(
        canvas: Canvas,
        item: WallpaperItem,
        configuration: WallpaperConfiguration,
        time: ZonedDateTime,
        batteryPercent: Int?,
        bitmap: Bitmap? = null,
        locale: Locale? = null,
    ) {
        drawingLocale = locale ?: appLocale()
        val bounds = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
        drawCachedBackground(canvas, bounds, item, configuration, bitmap)
        when (item.category) {
            ClockCategory.CUSTOM, ClockCategory.DIGITAL -> drawDigital(canvas, item, configuration, time, drawingLocale)
            ClockCategory.ANALOG -> drawAnalog(canvas, item, configuration, time)
            ClockCategory.SMART -> drawSmart(canvas, item, configuration, time, batteryPercent, drawingLocale)
        }
    }

    /** Rasterize artwork and its lighting once per wallpaper/config/surface, not on every clock tick. */
    private fun drawCachedBackground(
        canvas: Canvas,
        bounds: RectF,
        item: WallpaperItem,
        configuration: WallpaperConfiguration,
        sourceBitmap: Bitmap?,
    ) {
        if (canvas.width <= 0 || canvas.height <= 0) return
        val key = BackgroundCacheKey(
            wallpaperId = item.id,
            artworkIndex = item.artworkIndex,
            width = canvas.width,
            height = canvas.height,
            sourceBitmap = sourceBitmap?.takeUnless { it.isRecycled },
            brightness = configuration.brightness,
            dimAmount = configuration.dimAmount,
            scaleMode = configuration.backgroundScaleMode,
        )
        val cached = synchronized(backgroundCacheLock) {
            when {
                cachedBackgroundKey == key && cachedBackgroundBitmap?.isRecycled == false -> cachedBackgroundBitmap
                failedBackgroundKey == key -> null
                else -> {
                    cachedBackgroundBitmap?.takeUnless { it.isRecycled }?.recycle()
                    cachedBackgroundBitmap = null
                    cachedBackgroundKey = null
                    val source = key.sourceBitmap
                    val scale = min(1f, MAX_BACKGROUND_DIMENSION.toFloat() / max(canvas.width, canvas.height))
                    val bitmapWidth = (canvas.width * scale).roundToInt().coerceAtLeast(1)
                    val bitmapHeight = (canvas.height * scale).roundToInt().coerceAtLeast(1)
                    val replacement = try {
                        Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
                    } catch (_: OutOfMemoryError) {
                        null
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                    if (replacement == null) {
                        failedBackgroundKey = key
                        null
                    } else {
                        try {
                            val backgroundCanvas = Canvas(replacement)
                            val backgroundBounds = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
                            drawBackground(backgroundCanvas, backgroundBounds, item.artworkIndex, source, configuration)
                            drawBrightnessAndDim(backgroundCanvas, backgroundBounds, configuration)
                            cachedBackgroundBitmap?.takeUnless { it.isRecycled }?.recycle()
                            cachedBackgroundBitmap = replacement
                            cachedBackgroundKey = key
                            failedBackgroundKey = null
                            replacement
                        } catch (_: OutOfMemoryError) {
                            replacement.recycle()
                            failedBackgroundKey = key
                            null
                        } catch (_: RuntimeException) {
                            replacement.recycle()
                            failedBackgroundKey = key
                            null
                        }
                    }
                }
            }
        }

        if (cached != null && !cached.isRecycled) {
            canvas.drawBitmap(
                cached,
                null,
                bounds,
                cachedBackgroundPaint,
            )
        } else {
            // Graceful fallback if a low-memory device cannot allocate the cached surface bitmap.
            drawBackground(canvas, bounds, item.artworkIndex, sourceBitmap, configuration)
            drawBrightnessAndDim(canvas, bounds, configuration)
        }
    }

    fun clearBackgroundCache() {
        synchronized(backgroundCacheLock) {
            cachedBackgroundBitmap?.takeUnless { it.isRecycled }?.recycle()
            cachedBackgroundBitmap = null
            cachedBackgroundKey = null
            failedBackgroundKey = null
        }
    }

    fun drawBackground(
        canvas: Canvas,
        bounds: RectF,
        artworkIndex: Int,
        bitmap: Bitmap? = null,
        configuration: WallpaperConfiguration? = null,
    ) {
        canvas.save()
        canvas.clipRect(bounds)
        val background = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        if (bitmap != null && !bitmap.isRecycled) {
            drawBitmap(canvas, bitmap, bounds, configuration)
        } else {
            drawProceduralArtwork(canvas, bounds, artworkIndex)
        }
        // A subtle edge vignette adds depth without modifying the source art.
        background.shader = RadialGradient(
            bounds.centerX(),
            bounds.centerY(),
            max(bounds.width(), bounds.height()) * 0.72f,
            intArrayOf(Color.TRANSPARENT, 0x77000000),
            floatArrayOf(0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(bounds, background)
        canvas.restore()
    }

    fun drawPreviewBackground(
        canvas: Canvas,
        bounds: RectF,
        item: WallpaperItem,
        bitmap: Bitmap?,
        configuration: WallpaperConfiguration,
    ) {
        drawBackground(canvas, bounds, item.artworkIndex, bitmap, configuration)
        drawBrightnessAndDim(canvas, bounds, configuration)
    }

    private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, bounds: RectF, configuration: WallpaperConfiguration?) {
        val mode = configuration?.backgroundScaleMode
        val scale = if (mode == com.example.clockstudio.domain.model.BackgroundScaleMode.FIT) {
            min(bounds.width() / bitmap.width, bounds.height() / bitmap.height)
        } else {
            max(bounds.width() / bitmap.width, bounds.height() / bitmap.height)
        }
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        val destination = RectF(
            bounds.centerX() - width / 2f,
            bounds.centerY() - height / 2f,
            bounds.centerX() + width / 2f,
            bounds.centerY() + height / 2f,
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bitmap, null, destination, paint)
    }

    private fun drawProceduralArtwork(canvas: Canvas, bounds: RectF, artworkIndex: Int) {
        val theme = themes[Math.floorMod(artworkIndex, themes.size)]
        val width = bounds.width()
        val height = bounds.height()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        paint.shader = LinearGradient(
            bounds.left,
            bounds.top,
            bounds.right * 0.86f,
            bounds.bottom,
            intArrayOf(theme.top, theme.mid, theme.bottom),
            floatArrayOf(0f, 0.54f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(bounds, paint)
        paint.shader = null

        val focusX = bounds.left + width * (0.25f + (Math.floorMod(artworkIndex, 5) * 0.12f))
        val focusY = bounds.top + height * 0.37f
        paint.shader = RadialGradient(
            focusX,
            focusY,
            max(width, height) * 0.62f,
            intArrayOf(theme.glow, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(bounds, paint)
        paint.shader = null
        paint.style = Paint.Style.FILL

        when (Math.floorMod(artworkIndex, 12)) {
            0 -> drawFoldedRibbons(canvas, bounds, theme, paint)
            1 -> drawAurora(canvas, bounds, theme, paint)
            2 -> drawPrismWings(canvas, bounds, theme, paint)
            3 -> drawFoxSignal(canvas, bounds, theme, paint)
            4 -> drawNightCity(canvas, bounds, theme, paint)
            5 -> drawSpiral(canvas, bounds, theme, paint)
            6 -> drawLuminousFlowers(canvas, bounds, theme, paint)
            7 -> drawGeometry(canvas, bounds, theme, paint)
            8 -> drawNeonOrbits(canvas, bounds, theme, paint)
            9 -> drawDeepSpace(canvas, bounds, theme, paint)
            10 -> drawFlowingWaves(canvas, bounds, theme, paint)
            else -> drawNightLandscape(canvas, bounds, theme, paint)
        }
        drawDust(canvas, bounds, theme.accent, artworkIndex, paint)
    }

    private fun drawFoldedRibbons(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val path = Path().apply {
            moveTo(b.left - b.width() * 0.1f, b.height() * 0.55f)
            cubicTo(b.width() * 0.22f, b.height() * 0.08f, b.width() * 0.47f, b.height() * 0.94f, b.right + b.width() * 0.1f, b.height() * 0.33f)
        }
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0..7) {
            p.color = if (i % 2 == 0) t.accent.withAlpha(28 + i * 4) else 0xFF090D10.toInt().withAlpha(80)
            p.strokeWidth = b.width() * (0.11f - i * 0.008f)
            val shift = Path(path).apply { offset(0f, (i - 3) * b.height() * 0.035f) }
            canvas.drawPath(shift, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun drawAurora(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0..6) {
            p.color = if (i % 2 == 0) t.accent.withAlpha(70 - i * 6) else t.glow.withAlpha(38)
            p.strokeWidth = b.width() * (0.035f - i * 0.002f)
            val path = Path().apply {
                moveTo(b.left - 30f, b.height() * (0.20f + i * 0.035f))
                cubicTo(b.width() * 0.25f, b.height() * (0.06f + i * 0.02f), b.width() * 0.55f, b.height() * (0.48f + i * 0.025f), b.right + 30f, b.height() * (0.13f + i * 0.055f))
            }
            canvas.drawPath(path, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun drawPrismWings(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val cx = b.centerX()
        val cy = b.height() * 0.44f
        for (side in listOf(-1f, 1f)) {
            for (layer in 0..3) {
                val path = Path().apply {
                    moveTo(cx, cy)
                    cubicTo(cx + side * b.width() * (0.08f + layer * 0.015f), cy - b.height() * 0.22f, cx + side * b.width() * (0.44f - layer * 0.04f), cy - b.height() * 0.29f, cx + side * b.width() * (0.37f - layer * 0.035f), cy - b.height() * 0.02f)
                    cubicTo(cx + side * b.width() * 0.32f, cy + b.height() * 0.08f, cx + side * b.width() * 0.19f, cy + b.height() * 0.12f, cx, cy)
                    close()
                }
                p.color = if (layer % 2 == 0) t.accent.withAlpha(80 - layer * 12) else t.glow.withAlpha(75 - layer * 10)
                canvas.drawPath(path, p)
            }
        }
        p.color = 0xFF090D14.toInt().withAlpha(210)
        canvas.drawRoundRect(RectF(cx - b.width() * 0.018f, cy - b.height() * 0.13f, cx + b.width() * 0.018f, cy + b.height() * 0.16f), b.width() * 0.02f, b.width() * 0.02f, p)
    }

    private fun drawFoxSignal(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val cx = b.centerX()
        val cy = b.height() * 0.46f
        val silhouette = Path().apply {
            moveTo(cx - b.width() * 0.27f, cy - b.height() * 0.06f)
            lineTo(cx - b.width() * 0.31f, cy - b.height() * 0.29f)
            lineTo(cx - b.width() * 0.12f, cy - b.height() * 0.20f)
            cubicTo(cx, cy - b.height() * 0.27f, cx + b.width() * 0.12f, cy - b.height() * 0.20f, cx + b.width() * 0.31f, cy - b.height() * 0.29f)
            lineTo(cx + b.width() * 0.27f, cy - b.height() * 0.06f)
            cubicTo(cx + b.width() * 0.28f, cy + b.height() * 0.18f, cx + b.width() * 0.12f, cy + b.height() * 0.26f, cx, cy + b.height() * 0.28f)
            cubicTo(cx - b.width() * 0.12f, cy + b.height() * 0.26f, cx - b.width() * 0.28f, cy + b.height() * 0.18f, cx - b.width() * 0.27f, cy - b.height() * 0.06f)
            close()
        }
        p.color = t.accent.withAlpha(165)
        p.setShadowLayer(b.width() * 0.035f, 0f, 0f, t.glow.withAlpha(150))
        canvas.drawPath(silhouette, p)
        p.clearShadowLayer()
        p.color = t.bottom.withAlpha(210)
        canvas.drawCircle(cx - b.width() * 0.095f, cy - b.height() * 0.015f, b.width() * 0.025f, p)
        canvas.drawCircle(cx + b.width() * 0.095f, cy - b.height() * 0.015f, b.width() * 0.025f, p)
    }

    private fun drawNightCity(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val ground = b.height() * 0.64f
        p.color = 0xFF071016.toInt().withAlpha(245)
        val widths = floatArrayOf(0.15f, 0.12f, 0.18f, 0.11f, 0.16f, 0.14f, 0.18f)
        var x = b.left
        widths.forEachIndexed { index, fraction ->
            val w = b.width() * fraction
            val h = b.height() * (0.16f + ((index * 19) % 23) / 100f)
            val top = ground - h
            canvas.drawRect(x, top, x + w, b.bottom, p)
            for (row in 0..4) for (col in 0..1) {
                if ((row + col + index) % 3 != 0) {
                    p.color = t.accent.withAlpha(40 + ((row * 11 + col * 13 + index) % 80))
                    val wx = x + w * (0.25f + col * 0.35f)
                    val wy = top + h * (0.17f + row * 0.15f)
                    canvas.drawRoundRect(wx, wy, wx + w * 0.09f, wy + h * 0.045f, 2f, 2f, p)
                }
            }
            p.color = 0xFF071016.toInt().withAlpha(245)
            x += w * 0.88f
        }
    }

    private fun drawSpiral(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val cx = b.width() * 0.52f
        val cy = b.height() * 0.43f
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0..18) {
            p.color = if (i % 2 == 0) t.accent.withAlpha(120 - i * 3) else t.glow.withAlpha(110 - i * 3)
            p.strokeWidth = b.width() * (0.018f - i * 0.00035f)
            val radius = b.width() * (0.07f + i * 0.032f)
            canvas.drawArc(RectF(cx - radius, cy - radius, cx + radius, cy + radius), i * 13f, 235f, false, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun drawLuminousFlowers(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val centers = listOf(b.width() * 0.32f to b.height() * 0.36f, b.width() * 0.69f to b.height() * 0.59f)
        centers.forEachIndexed { flowerIndex, (cx, cy) ->
            val radius = b.width() * if (flowerIndex == 0) 0.12f else 0.095f
            for (petal in 0 until 8) {
                canvas.save()
                canvas.rotate(petal * 45f, cx, cy)
                p.color = if (petal % 2 == 0) t.accent.withAlpha(90) else t.glow.withAlpha(80)
                canvas.drawOval(RectF(cx - radius * 0.31f, cy - radius * 1.15f, cx + radius * 0.31f, cy + radius * 0.05f), p)
                canvas.restore()
            }
            p.color = t.accent.withAlpha(190)
            canvas.drawCircle(cx, cy, radius * 0.26f, p)
        }
    }

    private fun drawGeometry(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = b.width() * 0.007f
        for (i in -2..8) {
            val path = Path().apply {
                moveTo(b.width() * (i * 0.19f), b.top)
                lineTo(b.width() * (i * 0.19f + 0.4f), b.bottom)
                lineTo(b.width() * (i * 0.19f + 0.8f), b.top)
            }
            p.color = if (i % 2 == 0) t.accent.withAlpha(50) else t.glow.withAlpha(34)
            canvas.drawPath(path, p)
        }
        p.style = Paint.Style.FILL
        p.color = t.accent.withAlpha(70)
        canvas.drawCircle(b.width() * 0.75f, b.height() * 0.28f, b.width() * 0.15f, p)
    }

    private fun drawNeonOrbits(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val cx = b.width() * 0.5f
        val cy = b.height() * 0.43f
        p.style = Paint.Style.STROKE
        for (i in 0..5) {
            p.color = if (i % 2 == 0) t.accent.withAlpha(125 - i * 11) else t.glow.withAlpha(96 - i * 8)
            p.strokeWidth = b.width() * 0.009f
            canvas.drawOval(RectF(cx - b.width() * (0.15f + i * 0.036f), cy - b.height() * (0.12f + i * 0.028f), cx + b.width() * (0.15f + i * 0.036f), cy + b.height() * (0.12f + i * 0.028f)), p)
        }
        p.style = Paint.Style.FILL
        p.color = t.accent
        canvas.drawCircle(cx + b.width() * 0.27f, cy - b.height() * 0.11f, b.width() * 0.022f, p)
    }

    private fun drawDeepSpace(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        val cx = b.width() * 0.68f
        val cy = b.height() * 0.40f
        p.shader = RadialGradient(cx, cy, b.width() * 0.34f, intArrayOf(t.accent.withAlpha(210), t.glow.withAlpha(110), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, b.width() * 0.34f, p)
        p.shader = null
        p.color = 0xFF131A25.toInt()
        canvas.drawCircle(cx, cy, b.width() * 0.14f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = b.width() * 0.006f
        p.color = t.accent.withAlpha(165)
        canvas.drawCircle(cx, cy, b.width() * 0.19f, p)
        p.style = Paint.Style.FILL
    }

    private fun drawFlowingWaves(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        for (i in 0..5) {
            val baseline = b.height() * (0.42f + i * 0.09f)
            val path = Path().apply {
                moveTo(b.left, baseline)
                cubicTo(b.width() * 0.25f, baseline - b.height() * 0.22f, b.width() * 0.62f, baseline + b.height() * 0.18f, b.right, baseline - b.height() * 0.09f)
                lineTo(b.right, b.bottom)
                lineTo(b.left, b.bottom)
                close()
            }
            p.color = if (i % 2 == 0) t.accent.withAlpha(50 + i * 8) else t.glow.withAlpha(34 + i * 7)
            canvas.drawPath(path, p)
        }
    }

    private fun drawNightLandscape(canvas: Canvas, b: RectF, t: ArtTheme, p: Paint) {
        p.color = t.glow.withAlpha(155)
        canvas.drawCircle(b.width() * 0.72f, b.height() * 0.28f, b.width() * 0.095f, p)
        val back = Path().apply {
            moveTo(b.left, b.height() * 0.67f)
            lineTo(b.width() * 0.26f, b.height() * 0.43f)
            lineTo(b.width() * 0.48f, b.height() * 0.68f)
            lineTo(b.width() * 0.75f, b.height() * 0.39f)
            lineTo(b.right, b.height() * 0.68f)
            lineTo(b.right, b.bottom)
            lineTo(b.left, b.bottom)
            close()
        }
        p.color = t.accent.withAlpha(100)
        canvas.drawPath(back, p)
        val front = Path().apply {
            moveTo(b.left, b.height() * 0.76f)
            cubicTo(b.width() * 0.25f, b.height() * 0.64f, b.width() * 0.68f, b.height() * 0.92f, b.right, b.height() * 0.70f)
            lineTo(b.right, b.bottom)
            lineTo(b.left, b.bottom)
            close()
        }
        p.color = 0xFF05090D.toInt().withAlpha(230)
        canvas.drawPath(front, p)
    }

    private fun drawDust(canvas: Canvas, b: RectF, color: Int, seed: Int, p: Paint) {
        p.style = Paint.Style.FILL
        for (i in 0 until 42) {
            val xUnit = ((i * 73 + seed * 37) % 997) / 997f
            val yUnit = ((i * 191 + seed * 61) % 991) / 991f
            val radius = if (i % 7 == 0) 2.2f else 1.1f
            p.color = color.withAlpha(if (i % 3 == 0) 105 else 55)
            canvas.drawCircle(b.left + b.width() * xUnit, b.top + b.height() * yUnit, radius, p)
        }
    }

    private fun drawBrightnessAndDim(canvas: Canvas, bounds: RectF, configuration: WallpaperConfiguration) {
        val paint = Paint()
        val brightness = configuration.brightness.coerceIn(0.35f, 1.35f)
        val amount = if (brightness < 1f) (1f - brightness) + configuration.dimAmount else configuration.dimAmount
        if (amount > 0f) {
            paint.color = Color.BLACK.withAlpha((amount.coerceIn(0f, 0.88f) * 255).toInt())
            canvas.drawRect(bounds, paint)
        }
        if (brightness > 1f) {
            paint.color = Color.WHITE.withAlpha(((brightness - 1f).coerceIn(0f, 0.3f) * 100).toInt())
            canvas.drawRect(bounds, paint)
        }
    }

    private fun drawAnalog(canvas: Canvas, item: WallpaperItem, config: WallpaperConfiguration, time: ZonedDateTime) {
        val style = AnalogClockCatalog.find(item.clockStyleId).copy(
            showNumbers = config.showNumbers,
            hourHandColor = config.hourHandColor,
            minuteHandColor = config.minuteHandColor,
            secondHandColor = config.secondHandColor,
            markerColor = config.markerColor,
        )
        val diameter = min(canvas.width.toFloat(), canvas.height.toFloat()) * 0.50f * config.scale
        AnalogClockRenderer.draw(
            canvas = canvas,
            centerX = canvas.width * config.x,
            centerY = canvas.height * config.y,
            diameter = diameter,
            time = time,
            style = style,
            configuration = config,
        )
    }

    private fun drawDigital(canvas: Canvas, item: WallpaperItem, config: WallpaperConfiguration, time: ZonedDateTime, locale: Locale) {
        val style = DigitalClockCatalog.find(item.clockStyleId)
        val use24 = ClockTime.uses24HourFormat(context, config.useSystem24HourFormat, config.timeFormatMode)
        val showSeconds = config.showSeconds
        val timeText = ClockTime.time(time, use24, showSeconds, locale)
        val dateText = ClockTime.date(time, locale)
        val dayText = ClockTime.day(time, locale)
        val amPmText = ClockTime.amPm(time, locale)
        val cx = canvas.width * config.x
        val cy = canvas.height * config.y
        val textSize = canvas.width * 0.105f * config.scale
        val accent = config.accentColor.toInt().withAlpha(config.opacity)
        val primary = config.textColor.toInt().withAlpha(config.opacity)
        val paint = textPaint(primary, textSize, config.fontStyleId, accent)

        when (style.layoutType) {
            DigitalLayoutType.VERTICAL_STACK -> {
                val parts = timeText.split(":")
                drawCentered(canvas, parts.getOrNull(0).orEmpty(), cx, cy - textSize * 0.08f, paint)
                paint.color = accent
                drawCentered(canvas, parts.getOrNull(1).orEmpty(), cx, cy + textSize * 0.88f, paint)
                drawDigitalDate(canvas, dateText, cx, cy + textSize * 1.65f, textSize * 0.24f, config, primary)
            }
            DigitalLayoutType.LARGE_HOUR_MINUTE -> {
                val parts = timeText.split(":")
                val left = cx - textSize * 0.42f
                drawCentered(canvas, parts.getOrNull(0).orEmpty(), left, cy, paint)
                paint.textSize = textSize * 0.72f
                paint.color = accent
                drawCentered(canvas, parts.getOrNull(1).orEmpty(), cx + textSize * 0.48f, cy + textSize * 0.11f, paint)
                if (config.showAmPm && !use24) drawDigitalDate(canvas, amPmText, cx + textSize * 0.48f, cy + textSize * 0.53f, textSize * 0.20f, config, accent)
                drawDigitalDate(canvas, dateText, cx, cy + textSize * 0.82f, textSize * 0.22f, config, primary)
            }
            DigitalLayoutType.TIME_WITH_SIDE_DATE -> {
                paint.textSize = textSize * 0.78f
                drawCentered(canvas, timeText, cx - textSize * 0.35f, cy, paint)
                paint.textSize = textSize * 0.22f
                paint.color = accent
                var y = cy - textSize * 0.25f
                if (config.showDay) { drawCentered(canvas, dayText, cx + textSize * 1.25f, y, paint); y += textSize * 0.30f }
                if (config.showDate) drawCentered(canvas, dateText, cx + textSize * 1.25f, y, paint)
            }
            DigitalLayoutType.SPLIT_CARDS -> {
                val parts = timeText.split(":")
                val boxWidth = textSize * 1.15f
                val boxHeight = textSize * 1.05f
                val gap = textSize * 0.15f
                val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAA10191B.toInt() }
                canvas.drawRoundRect(RectF(cx - boxWidth - gap, cy - boxHeight / 2, cx - gap, cy + boxHeight / 2), textSize * 0.12f, textSize * 0.12f, boxPaint)
                canvas.drawRoundRect(RectF(cx + gap, cy - boxHeight / 2, cx + boxWidth + gap, cy + boxHeight / 2), textSize * 0.12f, textSize * 0.12f, boxPaint)
                paint.color = primary
                drawCentered(canvas, parts.getOrNull(0).orEmpty(), cx - boxWidth / 2 - gap, cy + textSize * 0.28f, paint)
                paint.color = accent
                drawCentered(canvas, parts.getOrNull(1).orEmpty(), cx + boxWidth / 2 + gap, cy + textSize * 0.28f, paint)
                drawDigitalDate(canvas, dateText, cx, cy + boxHeight * 0.95f, textSize * 0.21f, config, accent)
            }
            DigitalLayoutType.SPACE_DAY, DigitalLayoutType.MINIMAL_LINE, DigitalLayoutType.ORBITAL -> {
                val shown = if (style.layoutType == DigitalLayoutType.SPACE_DAY) timeText.replace(":", "   ") else timeText
                drawCentered(canvas, shown, cx, cy, paint)
                val below = when {
                    config.showDate && config.showDay -> dateText
                    config.showDay -> dayText
                    config.showDate -> dateText
                    else -> ""
                }
                if (below.isNotEmpty()) drawDigitalDate(canvas, below, cx, cy + textSize * 0.52f, textSize * 0.22f, config, accent)
            }
            else -> {
                if (style.layoutType == DigitalLayoutType.RETRO_LED) paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                drawCentered(canvas, timeText, cx, cy, paint)
                if (style.layoutType == DigitalLayoutType.STACKED_AM_PM && config.showAmPm && !use24) {
                    paint.textSize = textSize * 0.23f
                    paint.color = accent
                    drawCentered(canvas, amPmText, cx + textSize * 1.7f, cy - textSize * 0.15f, paint)
                }
                if (config.showDate) drawDigitalDate(canvas, dateText, cx, cy + textSize * 0.55f, textSize * 0.22f, config, accent)
            }
        }
    }

    private fun drawDigitalDate(canvas: Canvas, value: String, x: Float, baseline: Float, size: Float, config: WallpaperConfiguration, color: Int) {
        if (!config.showDate && value.length > 8) return
        val paint = textPaint(color, size, "minimal", color)
        paint.letterSpacing = 0.08f
        drawCentered(canvas, value, x, baseline, paint)
    }

    private fun drawSmart(canvas: Canvas, item: WallpaperItem, config: WallpaperConfiguration, time: ZonedDateTime, batteryPercent: Int?, locale: Locale) {
        val style = SmartClockCatalog.find(item.clockStyleId)
        val cx = canvas.width * config.x
        val cy = canvas.height * config.y
        val radius = min(canvas.width, canvas.height) * 0.19f * config.scale
        val primary = config.textColor.toInt().withAlpha(config.opacity)
        val accent = config.accentColor.toInt().withAlpha(config.opacity)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = max(2f, canvas.width * 0.006f)
        }
        if (style.ring) {
            canvas.drawCircle(cx, cy, radius, ringPaint)
            ringPaint.color = primary.withAlpha(config.opacity * 0.75f)
            ringPaint.strokeWidth *= 0.55f
            canvas.drawArc(RectF(cx - radius * 1.1f, cy - radius * 1.1f, cx + radius * 1.1f, cy + radius * 1.1f), -90f, 235f, false, ringPaint)
        }
        if (style.glass || style.layoutType == com.example.clockstudio.domain.model.SmartClockLayout.PANEL || style.layoutType == com.example.clockstudio.domain.model.SmartClockLayout.CYBERPUNK) {
            val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99121B20.toInt().withAlpha(config.opacity) }
            val panelWidth = canvas.width * 0.74f * config.scale
            val panelHeight = canvas.height * 0.19f * config.scale
            canvas.drawRoundRect(RectF(cx - panelWidth / 2f, cy - panelHeight / 2f, cx + panelWidth / 2f, cy + panelHeight / 2f), panelHeight * 0.14f, panelHeight * 0.14f, panel)
        }
        val use24 = ClockTime.uses24HourFormat(context, config.useSystem24HourFormat, config.timeFormatMode)
        val timeText = ClockTime.time(time, use24, config.showSeconds, locale)
        val textSize = canvas.width * 0.082f * config.scale
        val paint = textPaint(primary, textSize, "minimal", accent)
        drawCentered(canvas, timeText, cx, cy - textSize * 0.05f, paint)
        if (config.showAmPm && !use24) {
            val direction = if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) -1f else 1f
            paint.textSize = textSize * 0.23f
            paint.color = accent
            drawCentered(canvas, ClockTime.amPm(time, locale), cx + direction * textSize * 1.55f, cy - textSize * 0.05f, paint)
        }
        paint.textSize = textSize * 0.22f
        paint.color = accent
        var y = cy + textSize * 0.40f
        if (config.showDay) {
            drawCentered(canvas, ClockTime.day(time, locale), cx, y, paint)
            y += textSize * 0.28f
        }
        if (config.showDate) {
            val date = if (style.showMonth) ClockTime.date(time, locale) else ClockTime.dayAndDate(time, locale)
            drawCentered(canvas, date, cx, y, paint)
            y += textSize * 0.32f
        }
        if (config.showBattery && batteryPercent != null) {
            val label = "${batteryPercent.coerceIn(0, 100)}%"
            paint.textSize = textSize * 0.26f
            drawCentered(canvas, label, cx, y, paint)
        }
    }

    private fun textPaint(color: Int, size: Float, font: String, glow: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textAlign = Paint.Align.CENTER
        textSize = size
        typeface = if (font == "mono") Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) else Typeface.create("sans-serif-light", Typeface.NORMAL)
        textLocale = drawingLocale
        setShadowLayer(size * 0.13f, 0f, 0f, glow.withAlpha(120))
    }

    private fun drawCentered(canvas: Canvas, text: String, x: Float, baseline: Float, paint: Paint) {
        if (TextUtils.getLayoutDirectionFromLocale(drawingLocale) == View.LAYOUT_DIRECTION_RTL) {
            val oldAlignment = paint.textAlign
            paint.textAlign = Paint.Align.LEFT
            val left = x - paint.measureText(text) / 2f
            canvas.drawTextRun(text, 0, text.length, 0, text.length, left, baseline, true, paint)
            paint.textAlign = oldAlignment
        } else {
            canvas.drawText(text, x, baseline, paint)
        }
    }

    private fun appLocale(): Locale = if (android.os.Build.VERSION.SDK_INT >= 24) {
        context.resources.configuration.locales[0] ?: Locale.getDefault()
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale ?: Locale.getDefault()
    }

    private fun Int.withAlpha(alpha: Int): Int = (this and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    private fun Int.withAlpha(factor: Float): Int = withAlpha((Color.alpha(this) * factor.coerceIn(0f, 1f)).toInt())

    private data class BackgroundCacheKey(
        val wallpaperId: String,
        val artworkIndex: Int,
        val width: Int,
        val height: Int,
        val sourceBitmap: Bitmap?,
        val brightness: Float,
        val dimAmount: Float,
        val scaleMode: BackgroundScaleMode,
    )

    private data class ArtTheme(val top: Int, val mid: Int, val bottom: Int, val accent: Int, val glow: Int)

    private companion object {
        const val MAX_BACKGROUND_DIMENSION = 2_400
    }

    private val themes = listOf(
        ArtTheme(0xFF050709.toInt(), 0xFF10191D.toInt(), 0xFF010203.toInt(), 0xFF39C7B5.toInt(), 0xFF0A817C.toInt()),
        ArtTheme(0xFF06111A.toInt(), 0xFF10282F.toInt(), 0xFF050B12.toInt(), 0xFF40E8B4.toInt(), 0xFF1E91AA.toInt()),
        ArtTheme(0xFF0C071A.toInt(), 0xFF27163B.toInt(), 0xFF080B17.toInt(), 0xFFFF66B9.toInt(), 0xFF8B56FF.toInt()),
        ArtTheme(0xFF080B1B.toInt(), 0xFF25213C.toInt(), 0xFF080A15.toInt(), 0xFFFFA444.toInt(), 0xFF904CE5.toInt()),
        ArtTheme(0xFF07121E.toInt(), 0xFF102C42.toInt(), 0xFF03070E.toInt(), 0xFF57C3FA.toInt(), 0xFF1C7ECB.toInt()),
        ArtTheme(0xFF130A20.toInt(), 0xFF3A1945.toInt(), 0xFF070812.toInt(), 0xFFFF5C96.toInt(), 0xFFAC36DD.toInt()),
        ArtTheme(0xFF0B1420.toInt(), 0xFF153A3A.toInt(), 0xFF050B11.toInt(), 0xFFB5F56D.toInt(), 0xFF20CFAE.toInt()),
        ArtTheme(0xFF131018.toInt(), 0xFF30232B.toInt(), 0xFF09090C.toInt(), 0xFFE4A86A.toInt(), 0xFFB76150.toInt()),
        ArtTheme(0xFF070C18.toInt(), 0xFF152B45.toInt(), 0xFF040711.toInt(), 0xFF36DBF2.toInt(), 0xFF486BFF.toInt()),
        ArtTheme(0xFF030611.toInt(), 0xFF101C38.toInt(), 0xFF02030A.toInt(), 0xFF8D7AFF.toInt(), 0xFF287EC4.toInt()),
        ArtTheme(0xFF09121D.toInt(), 0xFF18333C.toInt(), 0xFF04080E.toInt(), 0xFF37C8DB.toInt(), 0xFF1A7DB0.toInt()),
        ArtTheme(0xFF10121A.toInt(), 0xFF293238.toInt(), 0xFF06080B.toInt(), 0xFFD3D9C2.toInt(), 0xFF809A8A.toInt()),
    )
}
