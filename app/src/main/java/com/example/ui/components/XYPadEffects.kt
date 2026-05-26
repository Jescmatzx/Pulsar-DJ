package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink

@Composable
fun XYPadEffects(
    activeEffect: String,
    xVal: Float,
    yVal: Float,
    onValueChange: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var padSize by remember { mutableStateOf(Offset.Zero) }
    var isPressing by remember { mutableStateOf(false) }

    // corner labels based on active DSP
    val (labelTopLeft, labelBottomRight) = when (activeEffect) {
        "Delay" -> Pair("TIME (SHORT)", "FEEDBACK (MAX)")
        "Reverb" -> Pair("ROOM SIZE (MIN)", "DAMPING (MAX)")
        "Flanger" -> Pair("RATE (SLOW)", "FEEDBACK (HIGH)")
        "Filter" -> Pair("CUTOFF (LOW)", "RESONANCE (HIGH)")
        else -> Pair("PARAMETER X", "PARAMETER Y")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0E1017))
            .onGloballyPositioned { coordinates ->
                padSize = Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressing = true
                        if (padSize.x > 0 && padSize.y > 0) {
                            onValueChange(
                                (offset.x / padSize.x).coerceIn(0f, 1f),
                                (1f - (offset.y / padSize.y)).coerceIn(0f, 1f) // invert Y for standard Cartesian graph
                            )
                        }
                        tryAwaitRelease()
                        isPressing = false
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressing = true },
                    onDragEnd = { isPressing = false },
                    onDragCancel = { isPressing = false },
                    onDrag = { change, _ ->
                        if (padSize.x > 0 && padSize.y > 0) {
                            onValueChange(
                                (change.position.x / padSize.x).coerceIn(0f, 1f),
                                (1f - (change.position.y / padSize.y)).coerceIn(0f, 1f)
                            )
                        }
                        change.consume()
                    }
                )
            }
            .testTag("xy_pad_effects")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw grid backdrop lines
            val rows = 5
            val cols = 8
            for (idx in 1 until rows) {
                val rY = height * (idx.toFloat() / rows)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, rY),
                    end = Offset(width, rY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (idx in 1 until cols) {
                val cX = width * (idx.toFloat() / cols)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(cX, 0f),
                    end = Offset(cX, height),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw active XY target crosshairs
            val targetX = xVal * width
            val targetY = (1f - yVal) * height // revert cartesian inversion

            // Horizontal crosshair helper line
            drawLine(
                color = CyberCyan.copy(alpha = 0.15f),
                start = Offset(0f, targetY),
                end = Offset(width, targetY),
                strokeWidth = 1.dp.toPx()
            )
            // Vertical crosshair helper line
            drawLine(
                color = PsyPink.copy(alpha = 0.15f),
                start = Offset(targetX, 0f),
                end = Offset(targetX, height),
                strokeWidth = 1.dp.toPx()
            )

            // 3. Render glowing puck target
            val puckRadius = if (isPressing) 14.dp.toPx() else 9.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberCyan, Color.Transparent),
                    center = Offset(targetX, targetY),
                    radius = puckRadius * 2.5f
                ),
                radius = puckRadius * 2.5f,
                center = Offset(targetX, targetY)
            )
            drawCircle(
                color = Color.White,
                radius = puckRadius,
                center = Offset(targetX, targetY)
            )
            drawCircle(
                color = PsyPink,
                radius = puckRadius,
                center = Offset(targetX, targetY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 4. Grid overlay typography guides
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Screen center info badge
            Text(
                text = "${activeEffect.uppercase()} MAPPING (X:${String.format("%.2f", xVal)}, Y:${String.format("%.2f", yVal)})",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top-left label: min X state
            Text(
                text = labelTopLeft,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Bottom-right label: max Y state
            Text(
                text = labelBottomRight,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}
