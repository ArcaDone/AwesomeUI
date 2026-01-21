package com.arcadone.awesomeui.components.chart.progreessionchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.line.ChartDataPoint

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
    initialSelectionRange: ClosedFloatingPointRange<Float>? = null,
    onRangeSelection: ((RangeSelectionData?) -> Unit)? = null,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    if (title.isNotEmpty()) {
                        Text(title, color = style.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                    }
                    if (value.isNotEmpty()) {
                        Text(value, color = style.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (trend.isNotEmpty()) {
                    TrendBadge(trend, trendColor, trendIcon)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Chart Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (data.isNotEmpty()) {
                    val minValue = data.minOfOrNull { it.value } ?: 0f
                    val maxValue = data.maxOfOrNull { it.value } ?: 100f
                    val midValue = (minValue + maxValue) / 2

                    Column(
                        modifier = Modifier.height(style.chartHeight),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${maxValue.toInt()}", fontSize = 10.sp, color = style.textSecondary)
                        Text("${midValue.toInt()}", fontSize = 10.sp, color = style.textSecondary)
                        Text("${minValue.toInt()}", fontSize = 10.sp, color = style.textSecondary)
                    }
                }

                LineChartItem(
                    data = data,
                    modifier = Modifier.weight(1f).height(style.chartHeight),
                    style = style,
                    animate = animate,
                    animateToProgress = animateToProgress,
                    interactive = interactive,
                    showTooltip = showTooltip,
                    initialSelectionRange = initialSelectionRange,
                    onRangeSelection = onRangeSelection,
                )
            }

            Spacer(Modifier.height(12.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                data.forEach { point ->
                    Text(point.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = style.textSecondary)
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
    initialSelectionRange: ClosedFloatingPointRange<Float>? = null,
    onRangeSelection: ((RangeSelectionData?) -> Unit)?,
) {
    if (data.isEmpty()) return

    val targetProgress = animateToProgress.coerceIn(0f, 1f)
    val animationProgress = remember { Animatable(if (animate) 0f else targetProgress) }

    // States for selection
    // -1f means no selection
    var selectionStartProgress by remember {
        mutableFloatStateOf(initialSelectionRange?.start ?: -1f)
    }
    var selectionEndProgress by remember {
        mutableFloatStateOf(initialSelectionRange?.endInclusive ?: -1f)
    }

    // For single point selection (scrubbing)
    var selectedSinglePoint by remember { mutableStateOf<SelectedChartPoints?>(null) }

    val density = LocalDensity.current

    LaunchedEffect(animate, data, targetProgress) {
        if (animate && selectionStartProgress < 0f) {
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

    val isRangeSelection = selectionStartProgress >= 0f && selectionEndProgress >= 0f
    val drawStartProgress: Float
    val drawEndProgress: Float

    if (isRangeSelection) {
        drawStartProgress = selectionStartProgress
        drawEndProgress = selectionEndProgress
    } else {
        drawStartProgress = 0f
        drawEndProgress = if (selectionStartProgress >= 0f) selectionStartProgress else animationProgress.value
    }

    // Determine current drawing progress
    if (selectionStartProgress >= 0f && selectionEndProgress < 0f) {
        // If the user is scrubbing with 1 finger, "scrub" the animation up to that point (optional)
        // Or just keep the animation at its final state. Here we use the user input.
        selectionStartProgress
    } else {
        animationProgress.value
    }

    // Helper function to calculate data at a specific point
    fun calculateDataAt(
        progress: Float,
        width: Float,
        height: Float,
    ): SelectedChartPoints {
        val safeProgress = progress.coerceIn(0f, 1f)
        val minValue = data.minOf { it.value }
        val maxValue = data.maxOf { it.value }
        val valueRange = maxValue - minValue
        val dataPoints = data.mapIndexed { index, point ->
            val x = if (data.size == 1) width / 2 else (index.toFloat() / (data.size - 1)) * width
            val normalizedValue = if (valueRange > 0) (point.value - minValue) / valueRange else 0.5f
            val y = height * (1 - normalizedValue)
            Offset(x, y)
        }
        val fullPath = buildSmoothPath(dataPoints)
        val pointOnPath = getPointAtProgress(fullPath, safeProgress)
        val closestIndex = findClosestDataPointIndex(safeProgress, data.size)
        val interpolatedValue = interpolateValueAtProgress(data, safeProgress)
        return SelectedChartPoints(data[closestIndex].label, interpolatedValue, safeProgress, pointOnPath)
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(data, interactive) {
                    if (!interactive) return@pointerInput

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes.filter { it.pressed }
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            if (changes.isEmpty()) {
                                // No finger: Reset selection
                                // Option: You can decide to keep the last selection even if no finger is touched
                                selectionStartProgress = -1f
                                selectionEndProgress = -1f
                                selectedSinglePoint = null
                                onRangeSelection?.invoke(null)
                            } else if (changes.size == 1) {
                                // 1 Finger: Tooltip single
                                val x = changes.first().position.x
                                val progress = (x / width).coerceIn(0f, 1f)

                                selectionStartProgress = progress
                                selectionEndProgress = -1f // Deactivate range selection

                                selectedSinglePoint = calculateDataAt(progress, width, height)
                                onRangeSelection?.invoke(null) // Reset callback range
                            } else {
                                // 2+ Fingers: Range Selection
                                val x1 = changes[0].position.x
                                val x2 = changes[1].position.x

                                // Touch ordered (left -> right)
                                val leftX = minOf(x1, x2)
                                val rightX = maxOf(x1, x2)

                                val startP = (leftX / width).coerceIn(0f, 1f)
                                val endP = (rightX / width).coerceIn(0f, 1f)

                                selectionStartProgress = startP
                                selectionEndProgress = endP
                                selectedSinglePoint = null // Deactivate single selection

                                val startData = calculateDataAt(startP, width, height)
                                val endData = calculateDataAt(endP, width, height)

                                onRangeSelection?.invoke(
                                    RangeSelectionData(
                                        startValue = startData.value,
                                        endValue = endData.value,
                                        startLabel = startData.label,
                                        endLabel = endData.label,
                                        deltaValue = endData.value - startData.value,
                                    ),
                                )
                            }
                        }
                    }
                }
                .drawWithContent {
                    val width = size.width
                    val height = size.height

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
                        val x = if (data.size == 1) width / 2 else (index.toFloat() / (data.size - 1)) * width
                        val normalizedValue = if (valueRange > 0) (point.value - minValue) / valueRange else 0.5f
                        val y = height * (1 - normalizedValue)
                        Offset(x, y)
                    }

                    if (dataPoints.size < 2) {
                        drawContent()
                        return@drawWithContent
                    }

                    val fullPath = buildSmoothPath(dataPoints)

                    // Draw guide line
                    if (style.showGuideLine || isRangeSelection) {
                        drawPath(
                            path = fullPath,
                            color = style.guideLineColor,
                            style = Stroke(width = style.guideLineWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }

                    // Draw animated line
                    val activePath = getPathSegment(fullPath = fullPath, fromProgress = drawStartProgress, toProgress = drawEndProgress)

                    // Gradient fill
                    if (style.showGradient && drawEndProgress > drawStartProgress) {
                        drawSegmentGradient(
                            fullPath = fullPath,
                            fromProgress = drawStartProgress,
                            toProgress = drawEndProgress,
                            height = height,
                            lineColor = style.lineColor,
                        )
                    }
                    // Main colored line
                    if (drawEndProgress > drawStartProgress) {
                        drawPath(
                            path = activePath,
                            color = style.lineColor,
                            style = Stroke(width = style.lineWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }

                    if (isRangeSelection) {
                        val startPoint = getPointAtProgress(fullPath, selectionStartProgress)
                        val endPoint = getPointAtProgress(fullPath, selectionEndProgress)

                        drawRect(
                            color = style.lineColor.copy(alpha = 0.1f),
                            topLeft = Offset(startPoint.x, 0f),
                            size = Size(endPoint.x - startPoint.x, height),
                        )

                        val verticalLineColor = style.lineColor.copy(alpha = 0.5f)
                        drawLine(verticalLineColor, Offset(startPoint.x, 0f), Offset(startPoint.x, height), 1.dp.toPx())
                        drawLine(verticalLineColor, Offset(endPoint.x, 0f), Offset(endPoint.x, height), 1.dp.toPx())

                        drawCircle(style.cardBackground, radius = 6.dp.toPx(), center = startPoint)
                        drawCircle(style.lineColor, radius = 4.dp.toPx(), center = startPoint)

                        drawCircle(style.cardBackground, radius = 6.dp.toPx(), center = endPoint)
                        drawCircle(style.lineColor, radius = 4.dp.toPx(), center = endPoint)
                    } else if (drawEndProgress > 0f) {
                        val currentPoint = getPointAtProgress(fullPath, drawEndProgress)

                        if (selectionStartProgress >= 0f) {
                            drawLine(
                                color = style.guideLineColor,
                                start = Offset(currentPoint.x, 0f),
                                end = Offset(currentPoint.x, height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                            )
                        }

                        drawCircle(style.lineColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = currentPoint)
                        drawCircle(style.lineColor, radius = 8.dp.toPx(), center = currentPoint)
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = currentPoint)
                    }

                    drawContent()
                },
        )

        // Glow Layer (Background blur)
        if (style.showGlow && (drawEndProgress > drawStartProgress)) {
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
                            val x = if (data.size == 1) width / 2 else (index.toFloat() / (data.size - 1)) * width
                            val normalizedValue = if (valueRange > 0) (point.value - minValue) / valueRange else 0.5f
                            val y = height * (1 - normalizedValue)
                            Offset(x, y)
                        }

                        if (dataPoints.size >= 2) {
                            val fullPath = buildSmoothPath(dataPoints)

                            val activePath = getPathSegment(fullPath, drawStartProgress, drawEndProgress)

                            drawPath(
                                path = activePath,
                                color = style.lineColor.copy(alpha = style.glowAlpha),
                                style = Stroke(width = style.lineWidth.toPx() * 4, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    },
            )
        }

        if (showTooltip && selectedSinglePoint != null && selectionEndProgress < 0f) {
            val point = selectedSinglePoint!!
            val tooltipOffsetX = with(density) { point.screenPosition.x.toDp() - 40.dp }
            val tooltipOffsetY = with(density) { point.screenPosition.y.toDp() - 70.dp }

            Box(
                modifier = Modifier
                    .offset(x = tooltipOffsetX, y = tooltipOffsetY)
                    .background(ProgressionGlowColors.TooltipBackground, RoundedCornerShape(8.dp))
                    .drawBehind {
                        drawRoundRect(
                            color = style.lineColor,
                            style = Stroke(width = 2f),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(point.label, fontSize = 10.sp, color = ProgressionGlowColors.TextSecondary)
                    Text("${point.value.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = style.lineColor)
                }
            }
        }
    }
}

data class RangeSelectionData(val startValue: Float, val endValue: Float, val startLabel: String, val endLabel: String, val deltaValue: Float, val horizontalValue: Float = endValue - startValue)

/**
 * Selected point info for tooltip
 */
private data class SelectedChartPoints(val label: String, val value: Float, val progress: Float, val screenPosition: Offset)

@Preview(name = "Selection Range Active", group = "Interactions", widthDp = 400, heightDp = 500)
@Composable
private fun ProgressionChartRangeSelectionPreview() {
    val sampleData = listOf(
        ChartDataPoint(50f, "Lun"),
        ChartDataPoint(80f, "Mar"),
        ChartDataPoint(40f, "Mer"),
        ChartDataPoint(60f, "Gio"),
        ChartDataPoint(90f, "Ven"),
        ChartDataPoint(100f, "Sab"),
        ChartDataPoint(70f, "Dom"),
    )

    val startVal = 40f
    val endVal = 95f
    val delta = endVal - startVal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = ProgressionGlowColors.LineBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Interval Analysis",
                        color = ProgressionGlowColors.LineBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Mer - Sab",
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Delta",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "+${delta.toInt()}",
                        color = ProgressionGlowColors.PositiveTrend,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        ProgressionChartGlow(
            data = sampleData,
            title = "Activity",
            value = "Total",
            trend = "+12%",
            trendDirection = TrendDirection.UP,
            style = ProgressionChartStyle(
                lineColor = ProgressionGlowColors.LineBlue,
                chartHeight = 200.dp,
            ),
            initialSelectionRange = 0.3f..0.75f,
            animate = false,
        )

        Text(
            text = "Preview Range Selection (2 fingers)",
            color = Color.Gray,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Preview(name = "Premium Analytics Preview", showBackground = true)
@Composable
fun FullAnalyticsPreview() {
    val analyticsData = listOf(
        ChartDataPoint(45f, "Jan 01"),
        ChartDataPoint(52f, "Jan 05"),
        ChartDataPoint(48f, "Jan 10"),
        ChartDataPoint(65f, "Jan 15"),
        ChartDataPoint(82f, "Jan 20"),
        ChartDataPoint(78f, "Jan 25"),
        ChartDataPoint(95f, "Jan 30"),
    )

    var currentRange by remember {
        mutableStateOf<RangeSelectionData?>(RangeSelectionData(65f, 78f, "Jan 15", "Jan 25", 13f))
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0D0F)).padding(16.dp),
    ) {
        Text("Performance Report", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 24.dp))

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF1A1D24)).padding(20.dp),
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (currentRange != null) "SELECTED RANGE" else "OVERVIEW",
                            color = ProgressionGlowColors.LinePurple.copy(0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = currentRange?.let { "${it.startLabel} — ${it.endLabel}" } ?: "Last 30 days",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (currentRange != null) {
                        val isPos = currentRange!!.deltaValue >= 0
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Growth", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                "${if (isPos) "+" else ""}${currentRange!!.deltaValue.toInt()}%",
                                color = if (isPos) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProgressionChartGlow(
                    data = analyticsData,
                    style = ProgressionChartStyle(lineColor = ProgressionGlowColors.LinePurple, chartHeight = 220.dp, glowAlpha = 0.5f, lineWidth = 4.dp),
                    initialSelectionRange = 0.45f..0.82f,
                    animate = false,
                    onRangeSelection = { currentRange = it },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().alpha(0.6f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Touch with two fingers to compare periods", color = Color.White, fontSize = 12.sp)
        }
    }
}
