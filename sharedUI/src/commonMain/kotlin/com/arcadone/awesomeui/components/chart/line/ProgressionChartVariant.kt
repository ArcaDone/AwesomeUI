package com.arcadone.awesomeui.components.chart.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Color palette for progression charts
 */
object ProgressionGlowColors {
    // Dark theme backgrounds
    val CardBackground = Color(0xFF1A1D24)
    val CardSurface = Color(0xFF22252E)

    // Grid and borders
    val GridColor = Color(0xFF2E3440)
    val BorderColor = Color(0xFF3A3D45)

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B3BA)

    // Trend colors
    val PositiveTrend = Color(0xFF10B981)
    val NegativeTrend = Color(0xFFEF4444)
    val NeutralTrend = Color(0xFFF59E0B)

    // Line colors
    val LineGreen = Color(0xFF10B981)
    val LineOrange = Color(0xFFFF6B35)
    val LinePurple = Color(0xFF8B5CF6)
    val LineBlue = Color(0xFF3B82F6)
}

/**
 * Style configuration for ProgressionChart
 */
data class ProgressionChartStyle(
    val cardBackground: Color = ProgressionGlowColors.CardBackground,
    val lineColor: Color = ProgressionGlowColors.LineGreen,
    val gridColor: Color = ProgressionGlowColors.GridColor,
    val textPrimary: Color = ProgressionGlowColors.TextPrimary,
    val textSecondary: Color = ProgressionGlowColors.TextSecondary,
    val chartHeight: Dp = 160.dp,
    val cornerRadius: Dp = 24.dp,
    val gridLines: Int = 3,
    val showGradient: Boolean = true,
    val showGlow: Boolean = true,
    val glowAlpha: Float = 0.4f,
    val showLastPoint: Boolean = true,
    val lineWidth: Dp = 3.dp,
)

/**
 * Trend indicator type
 */
enum class TrendDirection {
    UP,
    DOWN,
    NEUTRAL,
}

/**
 * Progression Chart with glow effects and dark theme
 *
 * Stateless and fully customizable.
 */
@Composable
fun ProgressionChartGlow(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    title: String = "",
    value: String = "",
    trend: String = "",
    trendDirection: TrendDirection = TrendDirection.UP,
    style: ProgressionChartStyle = ProgressionChartStyle(),
) {
    val trendColor = when (trendDirection) {
        TrendDirection.UP -> ProgressionGlowColors.PositiveTrend
        TrendDirection.DOWN -> ProgressionGlowColors.NegativeTrend
        TrendDirection.NEUTRAL -> ProgressionGlowColors.NeutralTrend
    }

    val trendIcon: ImageVector = when (trendDirection) {
        TrendDirection.UP -> Icons.Default.TrendingUp
        TrendDirection.DOWN -> Icons.Default.TrendingDown
        TrendDirection.NEUTRAL -> Icons.Default.TrendingUp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(style.cardBackground, RoundedCornerShape(style.cornerRadius))
            .padding(20.dp),
    ) {
        Column {
            // Header row with title, value, and trend badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            color = style.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    if (value.isNotEmpty()) {
                        Text(
                            text = value,
                            color = style.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                if (trend.isNotEmpty()) {
                    TrendBadge(
                        trend = trend,
                        trendColor = trendColor,
                        trendIcon = trendIcon,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Chart area with Y-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Y-axis labels
                if (data.isNotEmpty()) {
                    val minValue = data.minOfOrNull { it.value } ?: 0f
                    val maxValue = data.maxOfOrNull { it.value } ?: 100f
                    val midValue = (minValue + maxValue) / 2

                    Column(
                        modifier = Modifier.height(style.chartHeight),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${maxValue.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = style.textSecondary,
                        )
                        Text(
                            text = "${midValue.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = style.textSecondary,
                        )
                        Text(
                            text = "${minValue.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = style.textSecondary,
                        )
                    }
                }

                // Line Chart with glow
                LineChartItem(
                    data = data,
                    modifier = Modifier
                        .weight(1f)
                        .height(style.chartHeight),
                    style = style,
                )
            }

            Spacer(Modifier.height(12.dp))

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                data.forEach { point ->
                    Text(
                        text = point.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = style.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendBadge(
    trend: String,
    trendColor: Color,
    trendIcon: ImageVector,
) {
    Box(
        modifier = Modifier
            .background(
                color = trendColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = trendIcon,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = trend,
                color = trendColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LineChartItem(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    style: ProgressionChartStyle,
) {
    if (data.isEmpty()) return

    Box(modifier = modifier) {
        // Glow layer (behind the chart)
        if (style.showGlow) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .blur(12.dp),
            ) {
                val width = size.width
                val height = size.height

                val minValue = data.minOf { it.value }
                val maxValue = data.maxOf { it.value }
                val valueRange = maxValue - minValue

                val dataPoints = data.mapIndexed { index, point ->
                    val x = if (data.size == 1) {
                        width / 2
                    } else {
                        (index.toFloat() / (data.size - 1)) * width
                    }
                    val normalizedValue = if (valueRange > 0) {
                        (point.value - minValue) / valueRange
                    } else {
                        0.5f
                    }
                    val y = height * (1 - normalizedValue)
                    Offset(x, y)
                }

                if (dataPoints.size >= 2) {
                    val glowPath = Path().apply {
                        moveTo(dataPoints[0].x, dataPoints[0].y)
                        for (i in 1 until dataPoints.size) {
                            val prev = dataPoints[i - 1]
                            val curr = dataPoints[i]
                            val controlX = (prev.x + curr.x) / 2
                            cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                        }
                    }

                    drawPath(
                        path = glowPath,
                        color = style.lineColor.copy(alpha = style.glowAlpha),
                        style = Stroke(
                            width = style.lineWidth.toPx() * 4,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }
        }

        // Main chart
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // Grid lines
            for (i in 1..style.gridLines) {
                val y = height * (i.toFloat() / (style.gridLines + 1))
                drawLine(
                    color = style.gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
            }

            val minValue = data.minOf { it.value }
            val maxValue = data.maxOf { it.value }
            val valueRange = maxValue - minValue

            val dataPoints = data.mapIndexed { index, point ->
                val x = if (data.size == 1) {
                    width / 2
                } else {
                    (index.toFloat() / (data.size - 1)) * width
                }
                val normalizedValue = if (valueRange > 0) {
                    (point.value - minValue) / valueRange
                } else {
                    0.5f
                }
                val y = height * (1 - normalizedValue)
                Offset(x, y)
            }

            if (dataPoints.size < 2) {
                if (style.showLastPoint) {
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = dataPoints.first(),
                    )
                    drawCircle(
                        color = style.lineColor,
                        radius = 4.dp.toPx(),
                        center = dataPoints.first(),
                    )
                }
                return@Canvas
            }

            // Gradient fill
            if (style.showGradient) {
                val gradientPath = Path().apply {
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
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            style.lineColor.copy(alpha = 0.25f),
                            style.lineColor.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                    style = Fill,
                )
            }

            // Main line
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
                color = style.lineColor,
                style = Stroke(
                    width = style.lineWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            // Last point with glow
            if (style.showLastPoint) {
                val lastPoint = dataPoints.last()

                // Glow behind point
                drawCircle(
                    color = style.lineColor.copy(alpha = 0.3f),
                    radius = 12.dp.toPx(),
                    center = lastPoint,
                )

                // Outer ring
                drawCircle(
                    color = style.lineColor,
                    radius = 8.dp.toPx(),
                    center = lastPoint,
                )

                // Inner dot
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = lastPoint,
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun ProgressionChartGlowNegativePositivePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProgressionChartGlow(
            data = listOf(
                ChartDataPoint(100f, "Nov 1"),
                ChartDataPoint(90f, "Nov 15"),
                ChartDataPoint(100f, "Dec 1"),
                ChartDataPoint(103f, "Dec 15"),
                ChartDataPoint(105f, "Today"),
            ),
            title = "Bench Press 1RM",
            value = "105 kg",
            trend = "+5%",
            trendDirection = TrendDirection.UP,
        )

        ProgressionChartGlow(
            data = listOf(
                ChartDataPoint(80f, "Week 1"),
                ChartDataPoint(78f, "Week 2"),
                ChartDataPoint(75f, "Week 3"),
                ChartDataPoint(72f, "Week 4"),
            ),
            title = "Body Weight",
            value = "72 kg",
            trend = "-10%",
            trendDirection = TrendDirection.DOWN,
            style = ProgressionChartStyle(
                lineColor = ProgressionGlowColors.LineOrange,
            ),
        )
    }
}
