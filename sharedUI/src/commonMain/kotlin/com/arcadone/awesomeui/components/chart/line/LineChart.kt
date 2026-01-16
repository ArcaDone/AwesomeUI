package com.arcadone.awesomeui.components.chart.line

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val borderSurfaceColor = Color(0xFF374151) // Standard borders
private val surfaceColor = Color(0xFF1C2333)

@Composable
fun LineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    gridLines: Int = 3,
    showGradient: Boolean = true,
    showLastPoint: Boolean = true,
) {
    if (data.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val width = size.width
        val height = size.height

        // Find min and max values for scaling
        val minValue = data.minOf { it.value }
        val maxValue = data.maxOf { it.value }
        val valueRange = maxValue - minValue

        // Grid lines
        val gridColor = borderSurfaceColor
        for (i in 1..gridLines) {
            val y = height * (i.toFloat() / (gridLines + 1))
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )
        }

        // Calculate data points positions
        val dataPoints = data.mapIndexed { index, point ->
            val x = if (data.size == 1) {
                width / 2
            } else {
                (index.toFloat() / (data.size - 1)) * width
            }

            // Normalize value to 0-1 range, then invert for y-axis (0 at top)
            val normalizedValue = if (valueRange > 0) {
                (point.value - minValue) / valueRange
            } else {
                0.5f
            }
            val y = height * (1 - normalizedValue) // Invert: higher values = lower y position

            Offset(x, y)
        }

        if (dataPoints.size < 2) {
            // Just draw a point if we have only one data point
            if (showLastPoint) {
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = dataPoints.first(),
                )
                drawCircle(
                    color = color,
                    radius = 3.5.dp.toPx(),
                    center = dataPoints.first(),
                )
            }
            return@Canvas
        }

        // Draw gradient area
        if (showGradient) {
            val path = Path().apply {
                moveTo(dataPoints[0].x, dataPoints[0].y)
                for (i in 1 until dataPoints.size) {
                    val prev = dataPoints[i - 1]
                    val curr = dataPoints[i]
                    val controlX = (prev.x + curr.x) / 2
                    cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                }
                lineTo(dataPoints.last().x, height)
                lineTo(dataPoints.first().x, height)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.3f),
                        color.copy(alpha = 0f),
                    ),
                ),
            )
        }

        // Draw line with smooth curves
        val linePath = Path().apply {
            moveTo(dataPoints[0].x, dataPoints[0].y)
            for (i in 1 until dataPoints.size) {
                val prev = dataPoints[i - 1]
                val curr = dataPoints[i]
                val controlX = (prev.x + curr.x) / 2
                cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Draw last point
        if (showLastPoint) {
            drawCircle(
                color = color,
                radius = 8.dp.toPx(),
                center = dataPoints.last(),
            )
            drawCircle(
                color = color,
                radius = 6.dp.toPx(),
                center = dataPoints.last(),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderSurfaceColor),
    ) {
        val chartData = remember {
            listOf(
                ChartDataPoint(100f, "Nov 1"),
                ChartDataPoint(90f, "Nov 15"),
                ChartDataPoint(100f, "Dec 1"),
                ChartDataPoint(103f, "Dec 15"),
                ChartDataPoint(95f, "Today"),
            )
        }
        LineChart(chartData)
    }
}
