package com.arcadone.awesomeui.components.chart.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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

    // Guide line color
    val GuideLineColor = Color(0xFF3A3D45)

    // Tooltip
    val TooltipBackground = Color(0xFF2A2D35)
}

/**
 * Style configuration for ProgressionChart
 */
data class ProgressionChartStyle(
    val cardBackground: Color = ProgressionGlowColors.CardBackground,
    val lineColor: Color = ProgressionGlowColors.LineGreen,
    val guideLineColor: Color = ProgressionGlowColors.GuideLineColor,
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
    val guideLineWidth: Dp = 2.dp,
    val animationDurationMs: Int = 1500,
    val showGuideLine: Boolean = true,
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
 * Selected point info for tooltip
 */
private data class SelectedChartPoint(val label: String, val value: Float, val progress: Float, val screenPosition: Offset)

/**
 * Progression Chart with glow effects and dark theme
 *
 * @param animate If true, animates the line drawing from left to right with a dot following along
 * @param animateToProgress Target progress for animation (0.0 to 1.0). Default is 1.0 (full line)
 * @param interactive If true, allows clicking/dragging to reposition the line
 * @param showTooltip If true, shows tooltip with value when interacting
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
    animate: Boolean = false,
    animateToProgress: Float = 1f,
    interactive: Boolean = true,
    showTooltip: Boolean = true,
) {
    val trendColor = when (trendDirection) {
        TrendDirection.UP -> ProgressionGlowColors.PositiveTrend
        TrendDirection.DOWN -> ProgressionGlowColors.NegativeTrend
        TrendDirection.NEUTRAL -> ProgressionGlowColors.NeutralTrend
    }

    val trendIcon: ImageVector = when (trendDirection) {
        TrendDirection.UP -> Icons.AutoMirrored.Filled.TrendingUp
        TrendDirection.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
        TrendDirection.NEUTRAL -> Icons.AutoMirrored.Filled.TrendingUp
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
                    animate = animate,
                    animateToProgress = animateToProgress,
                    interactive = interactive,
                    showTooltip = showTooltip,
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
    animate: Boolean,
    animateToProgress: Float,
    interactive: Boolean,
    showTooltip: Boolean,
) {
    if (data.isEmpty()) return

    val targetProgress = animateToProgress.coerceIn(0f, 1f)

    // Animation progress (0.0 to targetProgress)
    val animationProgress = remember { Animatable(if (animate) 0f else targetProgress) }

    // User-controlled progress via tap/drag
    var userProgress by remember { mutableFloatStateOf(-1f) }
    var selectedPoint by remember { mutableStateOf<SelectedChartPoint?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    LaunchedEffect(animate, data, targetProgress) {
        if (animate && userProgress < 0f) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = targetProgress,
                animationSpec = tween(
                    durationMillis = (style.animationDurationMs * targetProgress).toInt(),
                    easing = FastOutSlowInEasing,
                ),
            )
        } else if (!animate) {
            animationProgress.snapTo(targetProgress)
        }
    }

    // Use user progress if set, otherwise use animation progress
    val currentProgress = if (userProgress >= 0f) userProgress else animationProgress.value

    // Helper function to update selection
    fun updateSelection(
        tapX: Float,
        width: Float,
        height: Float,
    ) {
        val tapProgress = (tapX / width).coerceIn(0f, 1f)

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
            val fullPath = buildSmoothPath(dataPoints)
            val pointOnPath = getPointAtProgress(fullPath, tapProgress)

            val closestIndex = findClosestDataPointIndex(tapProgress, data.size)
            val closestData = data[closestIndex]
            val interpolatedValue = interpolateValueAtProgress(data, tapProgress)

            userProgress = tapProgress
            selectedPoint = SelectedChartPoint(
                label = closestData.label,
                value = interpolatedValue,
                progress = tapProgress,
                screenPosition = pointOnPath,
            )
        }
    }

    Box(modifier = modifier) {
        // Main chart canvas
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(data, interactive) {
                    if (!interactive) return@pointerInput
                    detectTapGestures { tapOffset ->
                        updateSelection(
                            tapX = tapOffset.x,
                            width = size.width.toFloat(),
                            height = size.height.toFloat(),
                        )
                    }
                }
                .pointerInput(data, interactive) {
                    if (!interactive) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            updateSelection(
                                tapX = offset.x,
                                width = size.width.toFloat(),
                                height = size.height.toFloat(),
                            )
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            updateSelection(
                                tapX = change.position.x,
                                width = size.width.toFloat(),
                                height = size.height.toFloat(),
                            )
                        },
                    )
                }
                .drawWithContent {
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
                        drawContent()
                        return@drawWithContent
                    }

                    val fullPath = buildSmoothPath(dataPoints)

                    // 1. Draw guide line (full path in neutral color)
                    if (style.showGuideLine) {
                        drawPath(
                            path = fullPath,
                            color = style.guideLineColor,
                            style = Stroke(
                                width = style.guideLineWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }

                    // 2. Draw animated/interactive colored line on top
                    val animatedPath = getAnimatedPath(fullPath, currentProgress)

                    // Gradient fill (animated)
                    if (style.showGradient && currentProgress > 0f) {
                        drawAnimatedGradient(
                            fullPath = fullPath,
                            progress = currentProgress,
                            height = height,
                            firstX = dataPoints.first().x,
                            lineColor = style.lineColor,
                        )
                    }

                    // Main colored line
                    if (currentProgress > 0f) {
                        drawPath(
                            path = animatedPath,
                            color = style.lineColor,
                            style = Stroke(
                                width = style.lineWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }

                    // Animated point that follows the line
                    if (currentProgress > 0f) {
                        val currentPoint = getPointAtProgress(fullPath, currentProgress)

                        // Glow behind point
                        drawCircle(
                            color = style.lineColor.copy(alpha = 0.3f),
                            radius = 12.dp.toPx(),
                            center = currentPoint,
                        )

                        // Outer ring
                        drawCircle(
                            color = style.lineColor,
                            radius = 8.dp.toPx(),
                            center = currentPoint,
                        )

                        // Inner dot
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = currentPoint,
                        )
                    }

                    drawContent()
                },
        )

        // Glow layer (behind everything)
        if (style.showGlow && currentProgress > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(12.dp)
                    .drawBehind {
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
                            val fullPath = buildSmoothPath(dataPoints)
                            val animatedPath = getAnimatedPath(fullPath, currentProgress)

                            drawPath(
                                path = animatedPath,
                                color = style.lineColor.copy(alpha = style.glowAlpha),
                                style = Stroke(
                                    width = style.lineWidth.toPx() * 4,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                ),
                            )
                        }
                    },
            )
        }

        // Tooltip for selected point
        if (showTooltip && selectedPoint != null) {
            val point = selectedPoint!!
            val tooltipOffsetX = with(density) { point.screenPosition.x.toDp() - 40.dp }
            val tooltipOffsetY = with(density) { point.screenPosition.y.toDp() - 70.dp }

            Box(
                modifier = Modifier
                    .offset(x = tooltipOffsetX, y = tooltipOffsetY)
                    .background(
                        color = ProgressionGlowColors.TooltipBackground,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = style.lineColor,
                            style = Stroke(width = 2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = point.label,
                        fontSize = 10.sp,
                        color = ProgressionGlowColors.TextSecondary,
                    )
                    Text(
                        text = "${point.value.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.lineColor,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAnimatedGradient(
    fullPath: Path,
    progress: Float,
    height: Float,
    firstX: Float,
    lineColor: Color,
) {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress

    val gradientPath = Path().apply {
        val steps = 50
        var started = false
        for (i in 0..steps) {
            val distance = (targetLength * i / steps).coerceAtMost(targetLength)
            val pos = pathMeasure.getPosition(distance)
            if (!started) {
                moveTo(pos.x, pos.y)
                started = true
            } else {
                lineTo(pos.x, pos.y)
            }
        }

        val currentPoint = pathMeasure.getPosition(targetLength)
        lineTo(currentPoint.x, height)
        lineTo(firstX, height)
        close()
    }

    drawPath(
        path = gradientPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.25f),
                lineColor.copy(alpha = 0.05f),
                Color.Transparent,
            ),
        ),
        style = Fill,
    )
}

private fun buildSmoothPath(dataPoints: List<Offset>): Path {
    return Path().apply {
        if (dataPoints.isEmpty()) return@apply
        moveTo(dataPoints[0].x, dataPoints[0].y)
        for (i in 1 until dataPoints.size) {
            val prev = dataPoints[i - 1]
            val curr = dataPoints[i]
            val controlX = (prev.x + curr.x) / 2
            cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
        }
    }
}

private fun getAnimatedPath(
    fullPath: Path,
    progress: Float,
): Path {
    if (progress >= 1f) return fullPath
    if (progress <= 0f) return Path()

    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress

    val animatedPath = Path()
    pathMeasure.getSegment(0f, targetLength, animatedPath, true)
    return animatedPath
}

private fun getPointAtProgress(
    fullPath: Path,
    progress: Float,
): Offset {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress.coerceIn(0f, 1f)
    return pathMeasure.getPosition(targetLength)
}

private fun findClosestDataPointIndex(
    progress: Float,
    dataSize: Int,
): Int {
    if (dataSize <= 1) return 0
    val index = (progress * (dataSize - 1)).toInt()
    return index.coerceIn(0, dataSize - 1)
}

private fun interpolateValueAtProgress(
    data: List<ChartDataPoint>,
    progress: Float,
): Float {
    if (data.isEmpty()) return 0f
    if (data.size == 1) return data[0].value

    val exactIndex = progress * (data.size - 1)
    val lowerIndex = exactIndex.toInt().coerceIn(0, data.size - 2)
    val upperIndex = (lowerIndex + 1).coerceIn(0, data.size - 1)

    val lowerValue = data[lowerIndex].value
    val upperValue = data[upperIndex].value

    val fraction = exactIndex - lowerIndex
    return lowerValue + (upperValue - lowerValue) * fraction
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun ProgressionChartGlowAnimatedPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Full animation with drag & tooltip",
            color = Color.White,
            fontSize = 12.sp,
        )

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
            animate = true,
            animateToProgress = 1f,
            interactive = true,
            showTooltip = true,
        )

        Text(
            text = "Animate to 60% only",
            color = Color.White,
            fontSize = 12.sp,
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
            animate = true,
            animateToProgress = 0.6f,
            interactive = true,
            showTooltip = true,
        )
    }
}

@Preview
@Composable
private fun ProgressionChartGlowNoTooltipPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Interactive but NO tooltip",
            color = Color.White,
            fontSize = 12.sp,
        )

        ProgressionChartGlow(
            data = listOf(
                ChartDataPoint(60f, "Jan"),
                ChartDataPoint(75f, "Feb"),
                ChartDataPoint(70f, "Mar"),
                ChartDataPoint(90f, "Apr"),
                ChartDataPoint(85f, "May"),
                ChartDataPoint(95f, "Jun"),
            ),
            title = "Monthly Progress",
            value = "95 kg",
            trend = "+58%",
            trendDirection = TrendDirection.UP,
            style = ProgressionChartStyle(
                lineColor = ProgressionGlowColors.LinePurple,
            ),
            animate = false,
            interactive = true,
            showTooltip = false,
        )
    }
}

@Preview
@Composable
private fun ProgressionChartGlowPartialAnimationPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Animate to 30%",
            color = Color.White,
            fontSize = 12.sp,
        )

        ProgressionChartGlow(
            data = listOf(
                ChartDataPoint(50f, "Q1"),
                ChartDataPoint(65f, "Q2"),
                ChartDataPoint(80f, "Q3"),
                ChartDataPoint(100f, "Q4"),
            ),
            title = "Revenue Growth",
            value = "$100k",
            style = ProgressionChartStyle(
                lineColor = ProgressionGlowColors.LineBlue,
            ),
            animate = true,
            animateToProgress = 0.3f,
            interactive = false,
            showTooltip = false,
        )

        Text(
            text = "Animate to 75%",
            color = Color.White,
            fontSize = 12.sp,
        )

        ProgressionChartGlow(
            data = listOf(
                ChartDataPoint(50f, "Q1"),
                ChartDataPoint(65f, "Q2"),
                ChartDataPoint(80f, "Q3"),
                ChartDataPoint(100f, "Q4"),
            ),
            title = "Revenue Growth",
            value = "$100k",
            style = ProgressionChartStyle(
                lineColor = ProgressionGlowColors.LineGreen,
            ),
            animate = true,
            animateToProgress = 0.75f,
            interactive = true,
            showTooltip = true,
        )
    }
}
