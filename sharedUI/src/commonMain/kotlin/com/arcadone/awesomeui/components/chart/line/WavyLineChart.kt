package com.arcadone.awesomeui.components.chart.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.progreessionchart.getAnimatedPath
import com.arcadone.awesomeui.components.chart.progreessionchart.getPointAtProgress

/**
 * Color palette for wavy charts
 */
object WavyChartColors {
    val CardBackground = Color(0xFF1A1D24)
    val LineOrange = Color(0xFFFF6B35)
    val LineGreen = Color(0xFF10B981)
    val LinePurple = Color(0xFF8B5CF6)
    val LineBlue = Color(0xFF3B82F6)
    val GuideLineColor = Color(0xFF3A3D45)
    val TooltipBackground = Color(0xFF2A2D35)
}

/**
 * Animation type for WavyLineChart
 */
enum class WavyAnimationType {
    /** Standard left-to-right animation */
    LEFT_TO_RIGHT,

    /** Accordion/spring animation - expands from center to both sides */
    ACCORDION,
}

/**
 * Style configuration for WavyLineChart
 */
data class WavyChartStyle(
    val lineColor: Color = WavyChartColors.LineOrange,
    val fillColor: Color = WavyChartColors.LineOrange.copy(alpha = 0.3f),
    val guideLineColor: Color = WavyChartColors.GuideLineColor,
    val strokeWidth: Dp = 2.5.dp,
    val guideStrokeWidth: Dp = 1.5.dp,
    val showGuideLine: Boolean = true,
    val showGlow: Boolean = true,
    val glowAlpha: Float = 0.4f,
    val showGradientFill: Boolean = true,
)

/**
 * Selected point info for tooltip
 */
private data class WavySelectedPoint(val value: Float, val progress: Float, val screenPosition: Offset)

/**
 * Data class for progress update callback
 * @param progress Current progress along the X axis (0.0 to 1.0)
 * @param value Interpolated Y value at current progress (0.0 to 1.0)
 */
data class WavyChartProgress(val progress: Float, val value: Float)

/**
 * Wavy Line Chart with spring animation and interactive gestures
 *
 * @param dataPoints List of values (0.0 to 1.0)
 * @param animate If true, applies animation
 * @param animationType Type of animation (LEFT_TO_RIGHT or ACCORDION)
 * @param animationDurationMs Duration of animation in milliseconds
 * @param useSpringAnimation If true, uses spring physics (bouncy), otherwise uses tween
 * @param animateToProgress Target progress for animation (0.0 to 1.0)
 * @param interactive If true, allows tap/drag to explore values
 * @param showTooltip If true, shows tooltip with value when interacting
 * @param valueFormatter Formats the value for tooltip display
 */
@Composable
fun WavyLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    style: WavyChartStyle = WavyChartStyle(),
    animate: Boolean = false,
    animationType: WavyAnimationType = WavyAnimationType.LEFT_TO_RIGHT,
    animationDurationMs: Int = 2400,
    useSpringAnimation: Boolean = false,
    animateToProgress: Float = 1f,
    interactive: Boolean = true,
    showTooltip: Boolean = true,
    showTooltipAtEnd: Boolean = false,
    valueFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
    onProgressUpdate: ((WavyChartProgress) -> Unit)? = null,
) {
    if (dataPoints.isEmpty()) return

    val targetProgress = animateToProgress.coerceIn(0f, 1f)

    // Animation progress (0 = compressed/hidden, 1 = fully expanded)
    val animationProgress = remember { Animatable(if (animate) 0f else targetProgress) }

    // User-controlled progress via tap/drag
    var userProgress by remember { mutableFloatStateOf(-1f) }
    var selectedPoint by remember { mutableStateOf<WavySelectedPoint?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    // Animation
    LaunchedEffect(animate, dataPoints, targetProgress, animationType, animationDurationMs) {
        if (animate && userProgress < 0f) {
            animationProgress.snapTo(0f)
            if (useSpringAnimation) {
                animationProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            } else {
                animationProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(
                        durationMillis = animationDurationMs,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        } else if (!animate) {
            animationProgress.snapTo(targetProgress)
        }
    }

    // Show tooltip at end of animation
    var showEndTooltip by remember { mutableStateOf(false) }
    LaunchedEffect(showTooltipAtEnd, animationProgress.value, targetProgress) {
        if (showTooltipAtEnd && animationProgress.value >= targetProgress && userProgress < 0f) {
            showEndTooltip = true
        }
    }

    val currentProgress = if (userProgress >= 0f) userProgress else animationProgress.value

    // Notify progress updates
    LaunchedEffect(currentProgress, dataPoints) {
        if (dataPoints.isNotEmpty()) {
            val currentValue = wavyInterpolateValueAtProgress(dataPoints, currentProgress)
            onProgressUpdate?.invoke(WavyChartProgress(progress = currentProgress, value = currentValue))
        }
    }

    // Helper to update selection
    fun updateSelection(
        tapX: Float,
        width: Float,
        height: Float,
    ) {
        val tapProgress = (tapX / width).coerceIn(0f, 1f)

        val size = Size(width, height)
        val fullPath = generateSmoothPath(dataPoints, size)
        val pointOnPath = getPointAtProgress(fullPath, tapProgress)
        val interpolatedValue = wavyInterpolateValueAtProgress(dataPoints, tapProgress)

        userProgress = tapProgress
        selectedPoint = WavySelectedPoint(
            value = interpolatedValue,
            progress = tapProgress,
            screenPosition = pointOnPath,
        )
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(dataPoints, interactive) {
                    if (!interactive) return@pointerInput
                    detectTapGestures { tapOffset ->
                        updateSelection(
                            tapX = tapOffset.x,
                            width = size.width.toFloat(),
                            height = size.height.toFloat(),
                        )
                    }
                }
                .pointerInput(dataPoints, interactive) {
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
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
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
                    val fullPath = generateSmoothPath(dataPoints, size)

                    // 1. Draw guide line (full path)
                    if (style.showGuideLine) {
                        drawPath(
                            path = fullPath,
                            color = style.guideLineColor,
                            style = Stroke(
                                width = style.guideStrokeWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }

                    // 2. Draw animated portion based on animation type
                    // User interaction always uses LEFT_TO_RIGHT, accordion only for initial animation
                    val isUserControlled = userProgress >= 0f
                    val effectiveAnimationType = if (isUserControlled) {
                        WavyAnimationType.LEFT_TO_RIGHT
                    } else {
                        animationType
                    }

                    if (currentProgress > 0f) {
                        val animatedPath = when (effectiveAnimationType) {
                            WavyAnimationType.LEFT_TO_RIGHT -> getAnimatedPath(fullPath, currentProgress)
                            WavyAnimationType.ACCORDION -> getAccordionAnimatedPath(dataPoints, size, currentProgress)
                        }

                        // Get current point for the dot
                        val currentPoint = when (effectiveAnimationType) {
                            WavyAnimationType.LEFT_TO_RIGHT -> getPointAtProgress(fullPath, currentProgress)
                            WavyAnimationType.ACCORDION -> {
                                // For accordion, show dot at the rightmost visible point
                                val rightProgress = 0.5f + (currentProgress * 0.5f)
                                getPointAtProgress(fullPath, rightProgress)
                            }
                        }

                        // Gradient fill
                        if (style.showGradientFill && currentProgress > 0f) {
                            val fillPath = when (effectiveAnimationType) {
                                WavyAnimationType.LEFT_TO_RIGHT -> {
                                    Path().apply {
                                        addPath(animatedPath)
                                        lineTo(currentPoint.x, size.height)
                                        lineTo(0f, size.height)
                                        close()
                                    }
                                }
                                WavyAnimationType.ACCORDION -> {
                                    val leftProgress = 0.5f - (currentProgress * 0.5f)
                                    val rightProgress = 0.5f + (currentProgress * 0.5f)
                                    val leftX = size.width * leftProgress
                                    val rightX = size.width * rightProgress
                                    Path().apply {
                                        addPath(animatedPath)
                                        lineTo(rightX, size.height)
                                        lineTo(leftX, size.height)
                                        close()
                                    }
                                }
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        style.fillColor,
                                        Color.Transparent,
                                    ),
                                    startY = 0f,
                                    endY = size.height,
                                ),
                                style = Fill,
                            )
                        }

                        // Main line
                        drawPath(
                            path = animatedPath,
                            color = style.lineColor,
                            style = Stroke(
                                width = style.strokeWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )

                        // Animated point (only for LEFT_TO_RIGHT or when fully expanded)
                        if (effectiveAnimationType == WavyAnimationType.LEFT_TO_RIGHT || currentProgress >= 1f) {
                            // Glow
                            drawCircle(
                                color = style.lineColor.copy(alpha = 0.3f),
                                radius = 10.dp.toPx(),
                                center = currentPoint,
                            )
                            // Outer
                            drawCircle(
                                color = style.lineColor,
                                radius = 6.dp.toPx(),
                                center = currentPoint,
                            )
                            // Inner
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = currentPoint,
                            )
                        }
                    }

                    drawContent()
                },
        )

        // Glow layer
        // Need to determine effectiveAnimationType for glow too
        val glowAnimationType = if (userProgress >= 0f) {
            WavyAnimationType.LEFT_TO_RIGHT
        } else {
            animationType
        }

        if (style.showGlow && currentProgress > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(8.dp)
                    .drawBehind {
                        val fullPath = generateSmoothPath(dataPoints, size)
                        val animatedPath = when (glowAnimationType) {
                            WavyAnimationType.LEFT_TO_RIGHT -> getAnimatedPath(fullPath, currentProgress)
                            WavyAnimationType.ACCORDION -> getAccordionAnimatedPath(dataPoints, size, currentProgress)
                        }

                        drawPath(
                            path = animatedPath,
                            color = style.lineColor.copy(alpha = style.glowAlpha),
                            style = Stroke(
                                width = style.strokeWidth.toPx() * 3,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    },
            )
        }

        // Tooltip - show either from user interaction or at end of animation
        val shouldShowTooltip = showTooltip && (selectedPoint != null || showEndTooltip)

        if (shouldShowTooltip) {
            // Calculate position and value for tooltip
            val tooltipProgress = selectedPoint?.progress ?: targetProgress
            val tooltipValue = selectedPoint?.value ?: wavyInterpolateValueAtProgress(dataPoints, targetProgress)

            // We need to get the position on the chart
            // Use BoxWithConstraints approach or calculate from known size
            val tooltipPosition = selectedPoint?.screenPosition

            if (tooltipPosition != null || showEndTooltip) {
                // For end tooltip, we'll render it in a measured way
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            drawContent()
                            // This is just to trigger recomposition, actual tooltip below
                        },
                )

                // Calculate end position if needed
                val endPointValue = wavyInterpolateValueAtProgress(dataPoints, targetProgress)
                val displayValue = selectedPoint?.value ?: endPointValue

                // For simplicity, position tooltip relative to the chart end
                if (selectedPoint != null) {
                    val point = selectedPoint!!
                    val tooltipOffsetX = with(density) { point.screenPosition.x.toDp() - 30.dp }
                    val tooltipOffsetY = with(density) { point.screenPosition.y.toDp() - 50.dp }

                    TooltipBox(
                        offsetX = tooltipOffsetX,
                        offsetY = tooltipOffsetY,
                        value = valueFormatter(point.value),
                        lineColor = style.lineColor,
                    )
                } else if (showEndTooltip) {
                    // Show tooltip at end position - we need to measure the box
                    // For now, show at approximate end position (right side)
                    EndPositionTooltip(
                        dataPoints = dataPoints,
                        targetProgress = targetProgress,
                        valueFormatter = valueFormatter,
                        lineColor = style.lineColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun TooltipBox(
    offsetX: Dp,
    offsetY: Dp,
    value: String,
    lineColor: Color,
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .background(
                color = WavyChartColors.TooltipBackground,
                shape = RoundedCornerShape(6.dp),
            )
            .drawBehind {
                drawRoundRect(
                    color = lineColor,
                    style = Stroke(width = 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = lineColor,
        )
    }
}

@Composable
private fun EndPositionTooltip(
    dataPoints: List<Float>,
    targetProgress: Float,
    valueFormatter: (Float) -> String,
    lineColor: Color,
) {
    val endValue = wavyInterpolateValueAtProgress(dataPoints, targetProgress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val fullPath = generateSmoothPath(dataPoints, size)
                val endPoint = getPointAtProgress(fullPath, targetProgress)

                // Tooltip will be drawn at calculated position
            },
    ) {
        // Use layout to position the tooltip at the end point
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopEnd,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp, top = 16.dp)
                    .background(
                        color = WavyChartColors.TooltipBackground,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = lineColor,
                            style = Stroke(width = 2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = valueFormatter(endValue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor,
                )
            }
        }
    }
}

/**
 * Generates a smooth Bézier path through the data points
 */
fun generateSmoothPath(
    data: List<Float>,
    size: Size,
): Path {
    val path = Path()
    if (data.isEmpty()) return path

    val stepX = size.width / (data.size - 1).coerceAtLeast(1)

    fun getPoint(index: Int): Offset {
        val value = data[index].coerceIn(0f, 1f)
        val x = index * stepX
        val y = size.height * (1 - value)
        return Offset(x, y)
    }

    val startPoint = getPoint(0)
    path.moveTo(startPoint.x, startPoint.y)

    for (i in 0 until data.size - 1) {
        val p0 = getPoint(i)
        val p1 = getPoint(i + 1)

        val controlPoint1 = Offset(
            x = (p0.x + p1.x) / 2f,
            y = p0.y,
        )

        val controlPoint2 = Offset(
            x = (p0.x + p1.x) / 2f,
            y = p1.y,
        )

        path.cubicTo(
            controlPoint1.x,
            controlPoint1.y,
            controlPoint2.x,
            controlPoint2.y,
            p1.x,
            p1.y,
        )
    }

    return path
}

/**
 * Gets accordion-animated path that expands from center to both sides
 */
private fun getAccordionAnimatedPath(
    data: List<Float>,
    size: Size,
    progress: Float,
): Path {
    if (progress <= 0f) return Path()
    if (progress >= 1f) return generateSmoothPath(data, size)

    // Calculate visible range from center
    val centerProgress = 0.5f
    val halfSpread = progress * 0.5f
    val leftProgress = (centerProgress - halfSpread).coerceAtLeast(0f)
    val rightProgress = (centerProgress + halfSpread).coerceAtMost(1f)

    val fullPath = generateSmoothPath(data, size)
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length

    val startLength = pathLength * leftProgress
    val endLength = pathLength * rightProgress

    val animatedPath = Path()
    pathMeasure.getSegment(startLength, endLength, animatedPath, true)
    return animatedPath
}

private fun wavyInterpolateValueAtProgress(
    data: List<Float>,
    progress: Float,
): Float {
    if (data.isEmpty()) return 0f
    if (data.size == 1) return data[0]

    val exactIndex = progress * (data.size - 1)
    val lowerIndex = exactIndex.toInt().coerceIn(0, data.size - 2)
    val upperIndex = (lowerIndex + 1).coerceIn(0, data.size - 1)

    val lowerValue = data[lowerIndex]
    val upperValue = data[upperIndex]

    val fraction = exactIndex - lowerIndex
    return lowerValue + (upperValue - lowerValue) * fraction
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun WavyLineChartAccordionPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WavyChartColors.CardBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "Accordion animation (expands from center)",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        WavyLineChart(
            dataPoints = listOf(0.3f, 0.7f, 0.4f, 0.9f, 0.5f, 0.8f, 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            animate = true,
            animationType = WavyAnimationType.ACCORDION,
            animationDurationMs = 1500,
            useSpringAnimation = true,
            interactive = true,
            showTooltip = true,
        )
    }
}

@Preview
@Composable
private fun WavyLineChartLeftToRightPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WavyChartColors.CardBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "Left-to-right animation",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        WavyLineChart(
            dataPoints = listOf(0.3f, 0.7f, 0.4f, 0.9f, 0.5f, 0.8f, 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            style = WavyChartStyle(
                lineColor = WavyChartColors.LineGreen,
                fillColor = WavyChartColors.LineGreen.copy(alpha = 0.3f),
            ),
            animate = true,
            animationType = WavyAnimationType.LEFT_TO_RIGHT,
            animationDurationMs = 1000,
            useSpringAnimation = false,
            interactive = true,
            showTooltip = true,
        )
    }
}

@Preview
@Composable
private fun WavyLineChartTweenAnimationPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WavyChartColors.CardBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "Accordion with tween (800ms, no bounce)",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        WavyLineChart(
            dataPoints = listOf(0.2f, 0.5f, 0.3f, 0.8f, 0.4f, 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            style = WavyChartStyle(
                lineColor = WavyChartColors.LinePurple,
                fillColor = WavyChartColors.LinePurple.copy(alpha = 0.3f),
            ),
            animate = true,
            animationType = WavyAnimationType.ACCORDION,
            animationDurationMs = 800,
            useSpringAnimation = false,
            interactive = true,
            showTooltip = true,
        )
    }
}

@Preview
@Composable
private fun WavyLineChartInteractivePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WavyChartColors.CardBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "Drag to explore values",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        WavyLineChart(
            dataPoints = listOf(0.5f, 0.2f, 0.8f, 0.35f, 0.9f, 0.4f, 0.7f, 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            style = WavyChartStyle(
                lineColor = WavyChartColors.LineBlue,
                fillColor = WavyChartColors.LineBlue.copy(alpha = 0.25f),
            ),
            animate = false,
            interactive = true,
            showTooltip = true,
            valueFormatter = { "${(it * 200).toInt()} cal" },
        )
    }
}
