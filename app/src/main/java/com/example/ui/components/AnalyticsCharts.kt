package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.VividPurple

@Composable
fun GenrePieChart(
    technoPlayCount: Int,
    psyPlayCount: Int,
    highTechPlayCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (technoPlayCount + psyPlayCount + highTechPlayCount).toFloat()
    val slices = if (total == 0f) {
        listOf(33.3f, 33.3f, 33.4f) // Equal shares default
    } else {
        listOf(
            (technoPlayCount / total) * 360f,
            (psyPlayCount / total) * 360f,
            (highTechPlayCount / total) * 360f
        )
    }

    val colors = listOf(CyberCyan, PsyPink, VividPurple)

    Canvas(
        modifier = modifier
            .size(100.dp)
    ) {
        val chartSize = Size(size.width, size.height)
        var startAngle = -90f

        for (idx in slices.indices) {
            val sweep = slices[idx]
            if (sweep > 0f) {
                drawArc(
                    color = colors[idx],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = chartSize
                )
                startAngle += sweep
            }
        }

        // Draw inner dark center cutout to create a beautiful futuristic donut chart!
        drawCircle(
            color = Color(0xFF14161F),
            radius = size.width * 0.32f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Composable
fun EnergyCurveGraph(
    energyValues: List<Int>, // E.g: list of energies representing set flow
    modifier: Modifier = Modifier
) {
    val values = energyValues.ifEmpty { listOf(5, 7, 6, 8, 9, 6, 8, 10, 7) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val width = size.width
        val height = size.height
        val maxEnergyVal = 10f
        val stepX = width / (values.size - 1)

        val strokePath = Path()
        val fillPath = Path()

        // Generate coordinates and draw curves
        for (i in values.indices) {
            val vx = i * stepX
            val percentageHeight = values[i] / maxEnergyVal
            val vy = height - (percentageHeight * height * 0.85f) // Reserve padding on top

            if (i == 0) {
                strokePath.moveTo(vx, vy)
                fillPath.moveTo(vx, height)
                fillPath.lineTo(vx, vy)
            } else {
                // Control point for quadratic curve smoothing
                val prevX = (i - 1) * stepX
                val prevPercentage = values[i - 1] / maxEnergyVal
                val prevY = height - (prevPercentage * height * 0.85f)
                val cx = (prevX + vx) / 2f

                strokePath.quadraticTo(cx, prevY, vx, vy)
                fillPath.quadraticTo(cx, prevY, vx, vy)
            }
        }

        // Complete fill shape path down to bottom baseline
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        // 1. Draw horizontal scale background grid
        val numGridHorizontal = 4
        for (idx in 1..numGridHorizontal) {
            val gY = height * (idx.toFloat() / (numGridHorizontal + 1))
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, gY),
                end = Offset(width, gY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Render color gradient under curves
        val fillBrush = Brush.verticalGradient(
            colors = listOf(CyberCyan.copy(alpha = 0.25f), Color.Transparent),
            startY = 0f,
            endY = height
        )
        drawPath(
            path = fillPath,
            brush = fillBrush
        )

        // 3. Render exact bezier stroke
        drawPath(
            path = strokePath,
            color = CyberCyan,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // 4. Highlight vertices with glowing node circles
        for (i in values.indices) {
            val vx = i * stepX
            val percentageHeight = values[i] / maxEnergyVal
            val vy = height - (percentageHeight * height * 0.85f)

            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(vx, vy)
            )
            drawCircle(
                color = PsyPink,
                radius = 4.5.dp.toPx(),
                center = Offset(vx, vy),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
