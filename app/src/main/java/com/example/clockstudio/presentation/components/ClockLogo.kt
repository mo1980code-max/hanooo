package com.example.clockstudio.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ClockLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 120.dp,
    contentDescription: String? = null,
) {
    Canvas(
        modifier = modifier
            .size(logoSize)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) * 0.39f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x6637D9F2), Color.Transparent),
                center = center,
                radius = radius * 1.45f,
            ),
            radius = radius * 1.45f,
            center = center,
        )
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(Color(0xFF00D7B4), Color(0xFF17B7DD), Color(0xFF007F8B), Color(0xFF00D7B4)),
                center = center,
            ),
            radius = radius,
            center = center,
            style = Stroke(width = radius * 0.11f),
        )
        drawCircle(
            color = Color(0xAAFFFFFF),
            radius = radius * 0.83f,
            center = center,
            style = Stroke(width = radius * 0.018f),
        )
        for (mark in 0 until 12) {
            val angle = Math.toRadians(mark * 30.0 - 90.0)
            val outer = radius * 0.75f
            val inner = radius * if (mark % 3 == 0) 0.64f else 0.69f
            val start = Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner)
            val end = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer)
            drawLine(Color(0xCCFFFFFF), start, end, strokeWidth = if (mark % 3 == 0) radius * 0.035f else radius * 0.018f, cap = StrokeCap.Round)
        }
        drawLine(Color.White, center, Offset(center.x, center.y - radius * 0.48f), strokeWidth = radius * 0.055f, cap = StrokeCap.Round)
        drawLine(Color(0xFFB8FFF1), center, Offset(center.x + radius * 0.43f, center.y + radius * 0.20f), strokeWidth = radius * 0.043f, cap = StrokeCap.Round)
        drawCircle(Color(0xFF00D7B4), radius * 0.07f, center)
        // A tiny charge/time mark makes this mark distinct from a conventional clock glyph.
        drawRoundRect(Color(0xFF00BFA5), topLeft = Offset(center.x - radius * 0.43f, center.y + radius * 1.03f), size = androidx.compose.ui.geometry.Size(radius * 0.86f, radius * 0.07f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.05f))
    }
}
