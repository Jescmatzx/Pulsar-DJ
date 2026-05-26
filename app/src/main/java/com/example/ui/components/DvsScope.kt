package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DvsScope(
    isCalibrated: Boolean,
    modifier: Modifier = Modifier
) {
    // Generate an animated phase offset inside scope so signal moves in real-time
    var phaseOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(isCalibrated) {
        while (isActive) {
            delay(50)
            phaseOffset += 0.20f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension * 0.42f

        // Draw coordinate axis cross hairs behind signal
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 1.dp.toPx()
        )

        // Draw reference grid rings
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = maxRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.03f),
            radius = maxRadius * 0.5f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Build 120-point digital Lissajous phase circle orbit
        val numPoints = 120
        val scopeColor = if (isCalibrated) ToxicGreen else PsyPink.copy(alpha = 0.75f)

        for (i in 0 until numPoints) {
            val theta = (i * 2 * Math.PI / numPoints).toFloat()
            
            // If not calibrated, introduce sinusoid timecode phase fuz/noise ripples!
            val fuzzMultiplier = if (!isCalibrated) {
                1.0f + 0.12f * sin(theta * 8f + phaseOffset * 2f)
            } else {
                1.0f // Static clean unit circle
            }

            // Offset orbit signal so it rotates slightly
            val animatedTheta = theta + phaseOffset

            val rx = maxRadius * cos(animatedTheta) * fuzzMultiplier
            val ry = maxRadius * sin(animatedTheta) * fuzzMultiplier

            val pointOffset = Offset(center.x + rx, center.y + ry)

            // Draw circular orbit dots
            drawCircle(
                color = scopeColor,
                radius = if (isCalibrated) 1.5.dp.toPx() else 2.5.dp.toPx(),
                center = pointOffset
            )
        }

        // Concentric glowing beam
        if (isCalibrated) {
            drawCircle(
                color = ToxicGreen.copy(alpha = 0.18f),
                radius = maxRadius + 3.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
