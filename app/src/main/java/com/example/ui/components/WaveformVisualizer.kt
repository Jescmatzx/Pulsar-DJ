package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink

@Composable
fun WaveformVisualizer(
    progress: Float,
    waveformPoints: List<Float>,
    isDeckA: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isDeckA) CyberCyan else PsyPink
    val inactiveColor = activeColor.copy(alpha = 0.25f)
    val playheadColor = Color.White

    val points = if (waveformPoints.isEmpty()) {
        // Fallback default mock waveform lines if empty
        List(100) { (30..85).random().toFloat() / 100f }
    } else {
        waveformPoints
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val numBars = points.size
        val barSpacing = width / numBars

        // Draw background grid lines for beat alignment
        val numGridLines = 16
        val gridSpacing = width / numGridLines
        for (i in 0 until numGridLines) {
            val gx = (i * gridSpacing - (progress * width)) % width
            val adjustedX = if (gx < 0) gx + width else gx
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(adjustedX, 0f),
                end = Offset(adjustedX, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw wave bars
        for (i in 0 until numBars) {
            val barX = i * barSpacing
            val barProgress = i.toFloat() / numBars
            val isPlayed = barProgress <= progress

            val amplitude = points[i] * (height * 0.8f)
            val topY = centerY - (amplitude / 2)
            val bottomY = centerY + (amplitude / 2)

            drawLine(
                color = if (isPlayed) activeColor else inactiveColor,
                start = Offset(barX, topY),
                end = Offset(barX, bottomY),
                strokeWidth = (barSpacing * 0.7f).coerceAtLeast(1.dp.toPx())
            )
        }

        // Draw central vertical current playhead marker
        val playheadX = progress * width
        drawLine(
            color = playheadColor,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}
