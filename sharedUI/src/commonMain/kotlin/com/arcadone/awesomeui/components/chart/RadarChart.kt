package com.arcadone.awesomeui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.utils.clickableNoOverlay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *  color palette for radar charts
 */
object RadarColors {
    // Dark theme backgrounds
    val CardBackground = Color(0xFF1A1D24)
    val CardSurface = Color(0xFF22252E)

    // Grid and axis colors
    val GridColor = Color.White
    val AxisColor = Color.White

    // Data colors
    val DataPrimary = Color(0xFF3B82F6)
    val DataSecondary = Color(0xFF10B981)
    val DataWarning = Color(0xFFFF6B35)
    val DataDanger = Color(0xFFEF4444)
    val DataPurple = Color(0xFF8B5CF6)
    val DataPink = Color(0xFFEC4899)

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B3BA)
    val TextMuted = Color(0xFF6B7280)

    // Tooltip colors
    val TooltipBackground = Color(0xFF2A2D35)
    val TooltipBorder = Color(0xFF3A3D45)
}

/**
 * A single data series for the radar chart
 */
data class RadarSeries(val name: String, val values: List<Float>, val color: Color = RadarColors.DataPrimary, val fillAlpha: Float = 0.25f, val showPoints: Boolean = true)

/**
 * Labels for the radar chart axes
 */
data class RadarAxisLabel(val label: String, val isHighlighted: Boolean = false, val highlightColor: Color = RadarColors.DataDanger)

/**
 * Selected point information
 */
data class SelectedPoint(val seriesIndex: Int, val pointIndex: Int, val value: Float, val label: String, val seriesName: String, val color: Color)

/**
 * Style configuration for RadarChart
 */
data class RadarChartStyle(
    val cardBackground: Color = RadarColors.CardBackground,
    val gridColor: Color = RadarColors.GridColor,
    val axisColor: Color = RadarColors.AxisColor,
    val textPrimary: Color = RadarColors.TextPrimary,
    val textSecondary: Color = RadarColors.TextSecondary,
    val cornerRadius: Dp = 24.dp,
    val chartSize: Dp = 180.dp,
    val labelDistance: Dp = 110.dp,
    val gridLevels: Int = 4,
    val showGlow: Boolean = true,
    val glowAlpha: Float = 0.4f,
    val strokeWidth: Dp = 2.5.dp,
    val pointRadius: Dp = 5.dp,
    val showLegend: Boolean = true,
    val showCard: Boolean = true,
    val showAxisLabels: Boolean = true,
    val hitRadius: Dp = 20.dp,
    val selectedStrokeWidth: Dp = 4.dp,
    val unselectedAlpha: Float = 0.3f,
)

/**
 *  Radar Chart with multiple overlapping data series
 *
 * Supports multiple datasets rendered on top of each other for comparison.
 * Click on a point to highlight the series and show a tooltip with the value.
 *
 * @param axisLabels Labels for each axis of the radar
 * @param series List of data series to display (can overlay multiple)
 * @param modifier Modifier for the component
 * @param title Optional chart title
 * @param style Style configuration
 * @param onPointClick Callback when a point is clicked
 */
@Composable
fun RadarChart(
    axisLabels: List<RadarAxisLabel>,
    series: List<RadarSeries>,
    modifier: Modifier = Modifier,
    title: String? = null,
    style: RadarChartStyle = RadarChartStyle(),
    onPointClick: ((SelectedPoint?) -> Unit)? = null,
) {
    if (axisLabels.isEmpty() || series.isEmpty()) return

    var selectedPoint by remember { mutableStateOf<SelectedPoint?>(null) }

    val content: @Composable () -> Unit = {
        Column(
            modifier = if (style.showCard) {
                Modifier
                    .fillMaxWidth()
                    .background(style.cardBackground, RoundedCornerShape(style.cornerRadius))
                    .padding(20.dp)
            } else {
                Modifier.fillMaxWidth()
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Optional title
            title?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            // Radar chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.chartSize + style.labelDistance - 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Glow layer behind the chart (for each series)
                if (style.showGlow) {
                    series.forEachIndexed { index, dataSeries ->
                        val isSelected = selectedPoint?.seriesIndex == index
                        val alpha = if (selectedPoint != null && !isSelected) {
                            style.glowAlpha * style.unselectedAlpha
                        } else {
                            style.glowAlpha
                        }
                        RadarSeriesGlow(
                            axisCount = axisLabels.size,
                            series = dataSeries,
                            style = style,
                            alpha = alpha,
                        )
                    }
                }

                // Main radar chart canvas with touch detection
                RadarChartMultiCanvas(
                    axisLabels = axisLabels,
                    series = series,
                    style = style,
                    selectedSeriesIndex = selectedPoint?.seriesIndex,
                    selectedPointIndex = selectedPoint?.pointIndex,
                    onPointTap = { seriesIndex, pointIndex ->
                        val newSelection = if (selectedPoint?.seriesIndex == seriesIndex &&
                            selectedPoint?.pointIndex == pointIndex
                        ) {
                            null // Deselect on second tap
                        } else {
                            SelectedPoint(
                                seriesIndex = seriesIndex,
                                pointIndex = pointIndex,
                                value = series[seriesIndex].values[pointIndex],
                                label = axisLabels[pointIndex].label,
                                seriesName = series[seriesIndex].name,
                                color = series[seriesIndex].color,
                            )
                        }
                        selectedPoint = newSelection
                        onPointClick?.invoke(newSelection)
                    },
                )

                // Labels around the chart
                if (style.showAxisLabels) {
                    RadarAxisLabels(
                        labels = axisLabels,
                        style = style,
                    )
                }

                // Tooltip for selected point
                selectedPoint?.let { point ->
                    val angleStep = 2 * PI / axisLabels.size
                    val angle = -PI / 2 + angleStep * point.pointIndex
                    val tooltipDistance = style.chartSize.value * 0.35f
                    val xOffset = (tooltipDistance * cos(angle)).dp
                    val yOffset = (tooltipDistance * sin(angle)).dp

                    PointTooltip(
                        point = point,
                        modifier = Modifier.offset(x = xOffset, y = yOffset),
                    )
                }
            }

            // Legend for series
            if (style.showLegend && series.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    series.forEachIndexed { index, dataSeries ->
                        val isSelected = selectedPoint?.seriesIndex == index
                        SeriesLegendItem(
                            series = dataSeries,
                            isSelected = isSelected || selectedPoint == null,
                            onClick = {
                                if (selectedPoint?.seriesIndex == index) {
                                    selectedPoint = null
                                    onPointClick?.invoke(null)
                                }
                            },
                        )
                        if (index < series.lastIndex) {
                            Spacer(Modifier.width(24.dp))
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        content()
    }
}

/**
 * Simplified single-series API (backwards compatible)
 */
@Composable
fun RadarChart(
    data: List<RadarDataPoint>,
    modifier: Modifier = Modifier,
    title: String? = null,
    style: RadarChartStyle = RadarChartStyle(),
) {
    val axisLabels = data.map { RadarAxisLabel(it.label, it.isHighlighted, it.color) }
    val series = listOf(
        RadarSeries(
            name = "Data",
            values = data.map { it.value },
            color = dataColor,
        ),
    )

    RadarChart(
        axisLabels = axisLabels,
        series = series,
        modifier = modifier,
        title = title,
        style = style,
    )
}

// For backwards compatibility
data class RadarDataPoint(val label: String, val value: Float, val color: Color = RadarColors.DataPrimary, val isHighlighted: Boolean = false)

val dataColor: Color
    get() = RadarColors.DataPrimary

@Composable
private fun PointTooltip(
    point: SelectedPoint,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = RadarColors.TooltipBackground,
                shape = RoundedCornerShape(8.dp),
            )
            .drawBehind {
                // Border
                drawRoundRect(
                    color = point.color,
                    style = Stroke(width = 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = point.label,
                fontSize = 10.sp,
                color = RadarColors.TextSecondary,
            )
            Text(
                text = "${(point.value * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = point.color,
            )
            if (point.seriesName.isNotEmpty() && point.seriesName != "Data") {
                Text(
                    text = point.seriesName,
                    fontSize = 9.sp,
                    color = RadarColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun RadarSeriesGlow(
    axisCount: Int,
    series: RadarSeries,
    style: RadarChartStyle,
    alpha: Float,
) {
    val angleStep = 2 * PI / axisCount

    Box(
        modifier = Modifier
            .size(style.chartSize)
            .blur(12.dp)
            .drawBehind {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2 * 0.85f

                val path = buildSeriesPath(
                    values = series.values,
                    axisCount = axisCount,
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    angleStep = angleStep,
                )

                drawPath(
                    path = path,
                    color = series.color.copy(alpha = alpha),
                )
            },
    )
}

@Composable
private fun RadarChartMultiCanvas(
    axisLabels: List<RadarAxisLabel>,
    series: List<RadarSeries>,
    style: RadarChartStyle,
    selectedSeriesIndex: Int?,
    selectedPointIndex: Int?,
    onPointTap: (Int, Int) -> Unit,
) {
    val axisCount = axisLabels.size
    val angleStep = 2 * PI / axisCount
    val density = LocalDensity.current
    val hitRadiusPx = with(density) { style.hitRadius.toPx() }
    val chartSizePx = with(density) { style.chartSize.toPx() }

    Box(
        modifier = Modifier
            .size(style.chartSize)
            .pointerInput(series, axisLabels) {
                detectTapGestures { tapOffset ->
                    val centerX = chartSizePx / 2
                    val centerY = chartSizePx / 2
                    val radius = chartSizePx / 2 * 0.85f

                    // Check each series and point for hit
                    for (seriesIndex in series.indices.reversed()) {
                        val values = series[seriesIndex].values
                        for (pointIndex in 0 until minOf(values.size, axisCount)) {
                            val angle = -PI / 2 + angleStep * pointIndex
                            val value = values[pointIndex].coerceIn(0f, 1f)
                            val r = radius * value
                            val pointX = centerX + r * cos(angle).toFloat()
                            val pointY = centerY + r * sin(angle).toFloat()

                            val distance = sqrt(
                                (tapOffset.x - pointX).pow(2) +
                                    (tapOffset.y - pointY).pow(2),
                            )

                            if (distance <= hitRadiusPx) {
                                onPointTap(seriesIndex, pointIndex)
                                return@detectTapGestures
                            }
                        }
                    }
                    // Tap outside any point - deselect
                    onPointTap(-1, -1)
                }
            }
            .drawBehind {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2 * 0.85f

                // Draw grid circles
                for (i in 1..style.gridLevels) {
                    val r = radius * i / style.gridLevels
                    drawCircle(
                        color = style.gridColor.copy(alpha = 0.15f),
                        radius = r,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1f),
                    )
                }

                // Draw axis lines
                for (i in 0 until axisCount) {
                    val angle = -PI / 2 + angleStep * i
                    val x = centerX + radius * cos(angle).toFloat()
                    val y = centerY + radius * sin(angle).toFloat()
                    drawLine(
                        color = style.axisColor.copy(alpha = 0.2f),
                        start = Offset(centerX, centerY),
                        end = Offset(x, y),
                        strokeWidth = 1f,
                    )
                }

                // Draw each series (back to front for proper layering)
                series.forEachIndexed { seriesIndex, dataSeries ->
                    val isSelected = selectedSeriesIndex == seriesIndex
                    val hasSelection = selectedSeriesIndex != null && selectedSeriesIndex >= 0

                    val alpha = when {
                        !hasSelection -> 1f
                        isSelected -> 1f
                        else -> style.unselectedAlpha
                    }

                    val strokeWidth = if (isSelected) {
                        style.selectedStrokeWidth.toPx()
                    } else {
                        style.strokeWidth.toPx()
                    }

                    drawSeries(
                        values = dataSeries.values,
                        axisCount = axisCount,
                        centerX = centerX,
                        centerY = centerY,
                        radius = radius,
                        angleStep = angleStep,
                        color = dataSeries.color,
                        fillAlpha = dataSeries.fillAlpha * alpha,
                        strokeWidth = strokeWidth,
                        strokeAlpha = alpha,
                        showPoints = dataSeries.showPoints,
                        pointRadius = style.pointRadius.toPx(),
                        showGlow = style.showGlow && (isSelected || !hasSelection),
                        selectedPointIndex = if (isSelected) selectedPointIndex else null,
                    )
                }
            },
    )
}

private fun DrawScope.drawSeries(
    values: List<Float>,
    axisCount: Int,
    centerX: Float,
    centerY: Float,
    radius: Float,
    angleStep: Double,
    color: Color,
    fillAlpha: Float,
    strokeWidth: Float,
    strokeAlpha: Float,
    showPoints: Boolean,
    pointRadius: Float,
    showGlow: Boolean,
    selectedPointIndex: Int?,
) {
    val path = buildSeriesPath(
        values = values,
        axisCount = axisCount,
        centerX = centerX,
        centerY = centerY,
        radius = radius,
        angleStep = angleStep,
    )

    // Gradient fill for the polygon
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = fillAlpha),
                color.copy(alpha = fillAlpha * 0.4f),
            ),
            center = Offset(centerX, centerY),
            radius = radius,
        ),
        style = Fill,
    )

    // Stroke for the polygon
    drawPath(
        path = path,
        color = color.copy(alpha = strokeAlpha),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )

    // Draw data points
    if (showPoints) {
        for (i in 0 until minOf(values.size, axisCount)) {
            val angle = -PI / 2 + angleStep * i
            val value = values[i].coerceIn(0f, 1f)
            val r = radius * value
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()

            val isPointSelected = selectedPointIndex == i
            val actualPointRadius = if (isPointSelected) pointRadius * 1.5f else pointRadius

            // Point glow (enhanced for selected point)
            if (showGlow || isPointSelected) {
                val glowRadius = if (isPointSelected) actualPointRadius * 3f else actualPointRadius * 2.5f
                val glowAlpha = if (isPointSelected) 0.6f else 0.4f
                drawCircle(
                    color = color.copy(alpha = glowAlpha),
                    radius = glowRadius,
                    center = Offset(x, y),
                )
            }

            // Outer ring
            drawCircle(
                color = color,
                radius = actualPointRadius,
                center = Offset(x, y),
            )

            // Inner dot
            drawCircle(
                color = Color.White,
                radius = actualPointRadius * 0.5f,
                center = Offset(x, y),
            )
        }
    }
}

private fun buildSeriesPath(
    values: List<Float>,
    axisCount: Int,
    centerX: Float,
    centerY: Float,
    radius: Float,
    angleStep: Double,
): Path {
    val path = Path()
    val count = minOf(values.size, axisCount)

    for (i in 0 until count) {
        val angle = -PI / 2 + angleStep * i
        val value = values[i].coerceIn(0f, 1f)
        val r = radius * value
        val x = centerX + r * cos(angle).toFloat()
        val y = centerY + r * sin(angle).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

@Composable
private fun RadarAxisLabels(
    labels: List<RadarAxisLabel>,
    style: RadarChartStyle,
) {
    val n = labels.size
    val angleStep = 2 * PI / n

    labels.forEachIndexed { index, axisLabel ->
        val angle = -PI / 2 + angleStep * index
        val xOffset = (style.labelDistance.value * cos(angle)).dp
        val yOffset = (style.labelDistance.value * sin(angle)).dp

        Box(
            modifier = Modifier
                .offset(x = xOffset, y = yOffset)
                .wrapContentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = axisLabel.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (axisLabel.isHighlighted) {
                    axisLabel.highlightColor
                } else {
                    style.textSecondary
                },
            )
        }
    }
}

@Composable
private fun SeriesLegendItem(
    series: RadarSeries,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .padding(4.dp)
            .clickableNoOverlay {
                onClick()
            },
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = series.color.copy(alpha = if (isSelected) 1f else 0.4f),
                    shape = CircleShape,
                ),
        )
        Text(
            text = series.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                RadarColors.TextSecondary
            } else {
                RadarColors.TextMuted
            },
        )
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun RadarChartSingleSeriesPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        RadarChart(
            data = listOf(
                RadarDataPoint("Chest", 0.85f),
                RadarDataPoint("Back", 0.70f),
                RadarDataPoint("Legs", 0.60f),
                RadarDataPoint("Shoulders", 0.45f),
                RadarDataPoint("Arms", 0.80f),
            ),
            title = "Muscle Balance",
        )
    }
}

@Preview
@Composable
private fun RadarChartMultiSeriesPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        RadarChart(
            axisLabels = listOf(
                RadarAxisLabel("Chest"),
                RadarAxisLabel("Back"),
                RadarAxisLabel("Legs"),
                RadarAxisLabel("Shoulders"),
                RadarAxisLabel("Arms"),
            ),
            series = listOf(
                RadarSeries(
                    name = "Current",
                    values = listOf(0.85f, 0.70f, 0.60f, 0.65f, 0.80f),
                    color = RadarColors.DataPrimary,
                ),
                RadarSeries(
                    name = "Previous",
                    values = listOf(0.65f, 0.55f, 0.75f, 0.45f, 0.60f),
                    color = RadarColors.DataWarning,
                    fillAlpha = 0.15f,
                ),
            ),
            title = "Progress Comparison",
        )
    }
}

@Preview
@Composable
private fun RadarChartThreeSeriesPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F14))
                .padding(16.dp),
        ) {
            RadarChart(
                axisLabels = listOf(
                    RadarAxisLabel("Strength"),
                    RadarAxisLabel("Speed"),
                    RadarAxisLabel("Endurance"),
                    RadarAxisLabel("Power"),
                    RadarAxisLabel("Agility"),
                    RadarAxisLabel("Flexibility"),
                ),
                series = listOf(
                    RadarSeries(
                        name = "Athlete A",
                        values = listOf(0.9f, 0.6f, 0.7f, 0.85f, 0.5f, 0.4f),
                        color = RadarColors.DataPrimary,
                        fillAlpha = 0.2f,
                    ),
                    RadarSeries(
                        name = "Athlete B",
                        values = listOf(0.6f, 0.85f, 0.8f, 0.5f, 0.9f, 0.7f),
                        color = RadarColors.DataSecondary,
                        fillAlpha = 0.2f,
                    ),
                    RadarSeries(
                        name = "Athlete C",
                        values = listOf(0.7f, 0.7f, 0.5f, 0.7f, 0.6f, 0.9f),
                        color = RadarColors.DataPurple,
                        fillAlpha = 0.2f,
                    ),
                ),
                title = "Athlete Comparison",
                style = RadarChartStyle(
                    chartSize = 200.dp,
                    labelDistance = 120.dp,
                ),
            )
        }
    }
}

@Preview
@Composable
fun RadarChartInteractivePreview() {
    var selectedInfo by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadarChart(
            axisLabels = listOf(
                RadarAxisLabel("Chest"),
                RadarAxisLabel("Back"),
                RadarAxisLabel("Legs"),
                RadarAxisLabel("Shoulders"),
                RadarAxisLabel("Arms"),
            ),
            series = listOf(
                RadarSeries(
                    name = "Current",
                    values = listOf(0.85f, 0.70f, 0.60f, 0.65f, 0.80f),
                    color = RadarColors.DataPrimary,
                ),
                RadarSeries(
                    name = "Target",
                    values = listOf(0.90f, 0.85f, 0.80f, 0.75f, 0.85f),
                    color = RadarColors.DataSecondary,
                    fillAlpha = 0.15f,
                ),
            ),
            title = "Tap a point to see details",
            onPointClick = { point ->
                selectedInfo = point?.let {
                    "${it.seriesName}: ${it.label} = ${(it.value * 100).toInt()}%"
                }
            },
        )

        selectedInfo?.let {
            Text(
                text = it,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F14))
                .padding(16.dp),
        ) {
            RadarChart(
                axisLabels = listOf(
                    RadarAxisLabel("Chest"),
                    RadarAxisLabel("Back"),
                    RadarAxisLabel("Legs"),
                    RadarAxisLabel("Shoulders"),
                    RadarAxisLabel("Arms"),
                ),
                series = listOf(
                    RadarSeries(
                        name = "Current",
                        values = listOf(0.85f, 0.70f, 0.60f, 0.65f, 0.80f),
                        color = RadarColors.DataPrimary,
                    ),
                    RadarSeries(
                        name = "Previous",
                        values = listOf(0.65f, 0.55f, 0.75f, 0.45f, 0.60f),
                        color = RadarColors.DataWarning,
                        fillAlpha = 0.15f,
                    ),
                ),
                title = "Progress Comparison",
            )
        }
    }
}
