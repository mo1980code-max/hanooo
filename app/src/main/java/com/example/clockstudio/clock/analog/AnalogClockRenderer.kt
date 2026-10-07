package com.example.clockstudio.clock.analog

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.clockstudio.clock.ClockMath
import com.example.clockstudio.domain.model.AnalogClockStyle
import com.example.clockstudio.domain.model.WallpaperConfiguration
import java.time.ZonedDateTime
import kotlin.math.min

/** Shared native-canvas face used by gallery previews and the live-wallpaper surface. */
object AnalogClockRenderer {
    fun draw(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        diameter: Float,
        time: ZonedDateTime,
        style: AnalogClockStyle,
        configuration: WallpaperConfiguration,
        smoothSeconds: Boolean = configuration.smoothSeconds,
    ) {
        val radius = min(diameter * 0.5f, min(canvas.width.toFloat(), canvas.height.toFloat()) * 0.48f)
            .coerceAtLeast(1f)
        val alpha = configuration.opacity.coerceIn(0.15f, 1f)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val marker = configuration.markerColor.toInt().withOpacity(alpha)
        val dial = style.dialColor.toInt().withOpacity(alpha)
        val handHour = configuration.hourHandColor.toInt().withOpacity(alpha)
        val handMinute = configuration.minuteHandColor.toInt().withOpacity(alpha)
        val handSecond = configuration.secondHandColor.toInt().withOpacity(alpha)

        facePaint.reset()
        facePaint.isAntiAlias = true
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = (radius * 0.012f).coerceAtLeast(1f)
        facePaint.color = marker
        if (style.neon) facePaint.setShadowLayer(radius * 0.055f, 0f, 0f, style.markerColor.toInt().withOpacity(alpha * 0.65f))
        canvas.drawCircle(centerX, centerY, radius * 0.96f, facePaint)
        facePaint.clearShadowLayer()

        if (style.glass) {
            facePaint.style = Paint.Style.FILL
            facePaint.color = dial.withOpacity(alpha * 0.40f)
            canvas.drawCircle(centerX, centerY, radius * 0.94f, facePaint)
        }

        linePaint.reset()
        linePaint.isAntiAlias = true
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.color = marker
        for (mark in 0 until 60) {
            val major = mark % 5 == 0
            linePaint.strokeWidth = if (major) radius * 0.022f else radius * 0.008f
            val outside = radius * 0.89f
            val inside = if (major) radius * 0.76f else radius * 0.83f
            canvas.save()
            canvas.rotate(mark * 6f, centerX, centerY)
            canvas.drawLine(centerX, centerY - outside, centerX, centerY - inside, linePaint)
            canvas.restore()
        }

        if (configuration.showNumbers || style.showNumbers) {
            textPaint.textSize = radius * 0.15f
            textPaint.color = marker
            textPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            val numberRadius = radius * 0.66f
            for (hour in 1..12) {
                val angle = Math.toRadians(hour * 30.0)
                val x = centerX + (kotlin.math.sin(angle) * numberRadius).toFloat()
                val y = centerY - (kotlin.math.cos(angle) * numberRadius).toFloat() + textPaint.textSize * 0.34f
                val label = if (style.romanNumerals) roman(hour) else hour.toString()
                canvas.drawText(label, x, y, textPaint)
            }
        }

        drawHand(canvas, centerX, centerY, radius, ClockMath.hourAngle(time), radius * 0.49f, radius * 0.075f, handHour, linePaint)
        drawHand(canvas, centerX, centerY, radius, ClockMath.minuteAngle(time), radius * 0.70f, radius * 0.048f, handMinute, linePaint)
        if (configuration.showSecondHand) {
            val milliseconds = if (smoothSeconds) time.nano / 1_000_000f else 0f
            drawHand(canvas, centerX, centerY, radius, ClockMath.secondAngle(time.second, milliseconds), radius * 0.78f, radius * 0.014f, handSecond, linePaint, tail = radius * 0.17f)
        }

        facePaint.reset()
        facePaint.isAntiAlias = true
        facePaint.style = Paint.Style.FILL
        facePaint.color = marker
        canvas.drawCircle(centerX, centerY, radius * 0.043f, facePaint)
        facePaint.color = handSecond
        canvas.drawCircle(centerX, centerY, radius * 0.018f, facePaint)
    }

    private fun drawHand(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        angle: Float,
        length: Float,
        stroke: Float,
        color: Int,
        paint: Paint,
        tail: Float = radius * 0.06f,
    ) {
        paint.reset()
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color
        paint.strokeWidth = stroke.coerceAtLeast(1f)
        canvas.save()
        canvas.rotate(angle, centerX, centerY)
        canvas.drawLine(centerX, centerY + tail, centerX, centerY - length, paint)
        canvas.restore()
    }

    private fun roman(value: Int): String = when (value) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        9 -> "IX"
        10 -> "X"
        11 -> "XI"
        else -> "XII"
    }

    private fun Int.withOpacity(opacity: Float): Int {
        val sourceAlpha = Color.alpha(this)
        val alpha = (sourceAlpha * opacity.coerceIn(0f, 1f)).toInt().coerceIn(0, 255)
        return (this and 0x00FFFFFF) or (alpha shl 24)
    }
}
