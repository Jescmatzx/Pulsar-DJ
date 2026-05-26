package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun VinylTurntable(
    rotationAngle: Float,
    isPlaying: Boolean,
    trackTitle: String?,
    isDeckA: Boolean,
    onScratch: (deltaDegrees: Float, active: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonColor = if (isDeckA) CyberCyan else PsyPink
    var center by remember { mutableStateOf(Offset.Zero) }
    var previousAngle by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag(if (isDeckA) "turntable_a" else "turntable_b")
            .onGloballyPositioned { coordinates ->
                center = Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        previousAngle = atan2(offset.y - center.y, offset.x - center.x) * (180f / PI.toFloat())
                        onScratch(0f, true)
                    },
                    onDragEnd = {
                        onScratch(0f, false)
                    },
                    onDragCancel = {
                        onScratch(0f, false)
                    },
                    onDrag = { change, _ ->
                        val currentOffset = change.position
                        val currentAngle = atan2(currentOffset.y - center.y, currentOffset.x - center.x) * (180f / PI.toFloat())
                        
                        var angleDelta = currentAngle - previousAngle
                        
                        // Handle transition wrap-around (e.g. from -180 to 180 degrees)
                        if (angleDelta > 180f) angleDelta -= 360f
                        if (angleDelta < -180f) angleDelta += 360f

                        previousAngle = currentAngle
                        onScratch(angleDelta, true)
                        change.consume()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f

            // 1. Draw outer rim (rubber casing and dark vinyl base)
            drawCircle(
                color = Color(0xFF0F1015),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color(0xFF222533),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // 2. Vinyl circular grooves (metallic radial gradient)
            val grooveBrush = Brush.radialGradient(
                colors = listOf(Color(0xFF14161F), Color(0xFF0E0F14), Color(0xFF1A1C29), Color(0xFF10111A)),
                center = center,
                radius = radius
            )
            drawCircle(
                brush = grooveBrush,
                radius = radius - 4.dp.toPx(),
                center = center
            )

            // 3. Render vinyl grooves concentric rings
            val numGrooves = 8
            for (i in 1..numGrooves) {
                val grooveRadius = (radius * 0.4f) + (radius * 0.5f * (i.toFloat() / numGrooves))
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 4. Center sticker record label (rotates with playback or scratch)
            rotate(degrees = rotationAngle, pivot = center) {
                // Sticker background
                drawCircle(
                    color = neonColor.copy(alpha = 0.15f),
                    radius = radius * 0.38f,
                    center = center
                )
                drawCircle(
                    color = neonColor,
                    radius = radius * 0.38f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Render decorative strobe markers
                val numStrobeMarkers = 24
                for (i in 0 until numStrobeMarkers) {
                    val angleRad = (i * 2 * PI / numStrobeMarkers).toFloat()
                    val startRadius = radius * 0.35f
                    val endRadius = radius * 0.38f
                    val startOffset = Offset(
                        center.x + startRadius * kotlin.math.cos(angleRad),
                        center.y + startRadius * kotlin.math.sin(angleRad)
                    )
                    val endOffset = Offset(
                        center.x + endRadius * kotlin.math.cos(angleRad),
                        center.y + endRadius * kotlin.math.sin(angleRad)
                    )
                    drawLine(
                        color = neonColor.copy(alpha = 0.7f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Inner vinyl central plastic rim
                drawCircle(
                    color = Color(0xFF090A0E),
                    radius = radius * 0.12f,
                    center = center
                )
                // Center spindle pin hole
                drawCircle(
                    color = Color(0xFFB0BEC5),
                    radius = 3.dp.toPx(),
                    center = center
                )

                // Direction / Pitch slip line indicator (helps visualize rotation spinning)
                val slipLineEnd = Offset(
                    center.x + (radius * 0.32f),
                    center.y
                )
                drawLine(
                    color = Color.White,
                    start = center,
                    end = slipLineEnd,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 5. Stylus needle arm representation (static, hovering over top-right outer edge)
            val needleBase = Offset(center.x + radius * 0.95f, center.y - radius * 0.95f)
            val needleJoint = Offset(center.x + radius * 0.7f, center.y - radius * 0.5f)
            val needleCartridge = Offset(center.x + radius * 0.38f * kotlin.math.cos(-PI / 6).toFloat(), center.y + radius * 0.38f * kotlin.math.sin(-PI / 6).toFloat())

            // Needle Arm lines
            drawLine(
                color = Color(0xFF78909C),
                start = needleBase,
                end = needleJoint,
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color(0xFFCFD8DC),
                start = needleJoint,
                end = needleCartridge,
                strokeWidth = 2.dp.toPx()
            )
            // Needle cartridge block
            drawCircle(
                color = if (isPlaying) neonColor else Color(0xFF90A4AE),
                radius = 5.dp.toPx(),
                center = needleCartridge
            )
        }
    }
}
