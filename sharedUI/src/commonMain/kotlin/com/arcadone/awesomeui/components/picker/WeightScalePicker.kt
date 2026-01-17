package com.arcadone.awesomeui.components.picker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Color palette for WeightScalePicker
 */
object WeightScaleColors {
    val Background = Color(0xFFF8F9FD)
    val AccentPurple = Color(0xFF8B7BF7)
    val MajorTick = Color(0xFF2D3142)
    val MinorTick = Color(0xFFD1D5DB)
    val LabelText = Color(0xFF2D3142)
    val ValueText = Color(0xFF1F2937)
    val UnitText = Color(0xFF6B7280)
}

/**
 * Style configuration for WeightScalePicker
 */
data class WeightScaleStyle(
    val backgroundColor: Color = WeightScaleColors.Background,
    val accentColor: Color = WeightScaleColors.AccentPurple,
    val majorTickColor: Color = WeightScaleColors.MajorTick,
    val minorTickColor: Color = WeightScaleColors.MinorTick,
    val labelColor: Color = WeightScaleColors.LabelText,
    val valueColor: Color = WeightScaleColors.ValueText,
    val unitColor: Color = WeightScaleColors.UnitText,
    val majorTickHeight: Dp = 40.dp,
    val minorTickHeight: Dp = 20.dp,
    val tickWidth: Dp = 1.5.dp,
    val indicatorWidth: Dp = 2.dp,
)

/**
 * Horizontal weight scale picker with snap-to-tick behavior and haptic feedback
 *
 * @param value Current weight value (e.g., 66.2f)
 * @param onValueChange Callback when value changes
 * @param minValue Minimum selectable weight
 * @param maxValue Maximum selectable weight
 * @param step Step between each tick (e.g., 0.1f for 100g increments)
 * @param style Visual style configuration
 * @param onHapticFeedback Callback for haptic feedback (implement platform-specific)
 */
@Composable
fun WeightScalePicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 30f,
    maxValue: Float = 200f,
    step: Float = 0.1f,
    style: WeightScaleStyle = WeightScaleStyle(),
    onHapticFeedback: (() -> Unit)? = null,
) {
    val density = LocalDensity.current

    // Pixels per tick
    val tickSpacingDp = 8.dp
    val tickSpacingPx = with(density) { tickSpacingDp.toPx() }

    // Drag state
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Calculate the offset for the current value
    val centerOffset = ((value - minValue) / step) * tickSpacingPx

    // Animated offset for smooth snapping (use subtraction for visual)
    val animatedOffset by animateFloatAsState(
        targetValue = if (isDragging) centerOffset - dragOffset else centerOffset,
        animationSpec = tween(durationMillis = if (isDragging) 0 else 200),
        label = "offsetAnimation",
    )

    // Track last snapped value for haptic feedback
    var lastSnappedValue by remember { mutableFloatStateOf(value) }

    // Calculate display value from offset
    val displayValue = if (isDragging) {
        val tickIndex = ((centerOffset - dragOffset) / tickSpacingPx).roundToInt()
        val rawValue = minValue + tickIndex * step
        (rawValue * 10).roundToInt() / 10f
    } else {
        value
    }.coerceIn(minValue, maxValue)

    // Trigger haptic feedback when crossing ticks
    LaunchedEffect(displayValue) {
        if (displayValue != lastSnappedValue && isDragging) {
            onHapticFeedback?.invoke()
            lastSnappedValue = displayValue
        }
    }

    Column(
        modifier = modifier
            .background(style.backgroundColor)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Weight display
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.valueColor,
                    ),
                ) {
                    val formattedValue = (round(displayValue * 10) / 10).toString()
                    append(formattedValue)
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = style.unitColor,
                    ),
                ) {
                    append(" kg")
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scale ruler with indicator
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .pointerInput(value) {
                    // Key on value so gesture updates when value changes
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffset = 0f
                            println("🟢 DRAG START - value: $value, centerOffset: $centerOffset")
                        },
                        onDragEnd = {
                            isDragging = false
                            // Snap to nearest tick (use subtraction for correct direction)
                            val tickIndex = ((centerOffset - dragOffset) / tickSpacingPx).roundToInt()
                            val snappedValue = (minValue + tickIndex * step).coerceIn(minValue, maxValue)
                            val roundedValue = (snappedValue * 10).roundToInt() / 10f
                            println("🔴 DRAG END - dragOffset: $dragOffset, tickIndex: $tickIndex, snappedValue: $snappedValue, roundedValue: $roundedValue")
                            onValueChange(roundedValue)
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffset = 0f
                            println("⚪ DRAG CANCEL")
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                            val currentTickIndex = ((centerOffset - dragOffset) / tickSpacingPx).roundToInt()
                            val currentValue = minValue + currentTickIndex * step
                            println("🔵 DRAGGING - dragAmount: $dragAmount, dragOffset: $dragOffset, currentValue: $currentValue")
                        },
                    )
                },
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            val centerX = widthPx / 2f
            val currentOffset = if (isDragging) centerOffset - dragOffset else animatedOffset

            // Center indicator (line + dot)
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val indicatorX = size.width / 2f

                // Vertical line
                drawLine(
                    color = style.accentColor,
                    start = Offset(indicatorX, 0f),
                    end = Offset(indicatorX, size.height - 45.dp.toPx()),
                    strokeWidth = style.indicatorWidth.toPx(),
                    cap = StrokeCap.Round,
                )

                // Dot at bottom of line
                drawCircle(
                    color = style.accentColor,
                    radius = 5.dp.toPx(),
                    center = Offset(indicatorX, size.height - 45.dp.toPx()),
                )
            }

            // Ticks
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
            ) {
                val rulerY = size.height
                val majorTickHeightPx = style.majorTickHeight.toPx()
                val minorTickHeightPx = style.minorTickHeight.toPx()
                val totalTicks = ((maxValue - minValue) / step).toInt()

                for (i in 0..totalTicks) {
                    val tickValue = minValue + i * step
                    val tickOffset = i * tickSpacingPx
                    val tickX = centerX - currentOffset + tickOffset

                    // Only draw if visible (with margin)
                    if (tickX < -20 || tickX > size.width + 20) continue

                    val roundedTick = (tickValue * 10).roundToInt()
                    val isMajorTick = roundedTick % 10 == 0
                    val tickHeight = if (isMajorTick) majorTickHeightPx else minorTickHeightPx
                    val tickColor = if (isMajorTick) style.majorTickColor else style.minorTickColor

                    drawLine(
                        color = tickColor,
                        start = Offset(tickX, rulerY - tickHeight),
                        end = Offset(tickX, rulerY),
                        strokeWidth = style.tickWidth.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            val totalTicks = ((maxValue - minValue) / step).toInt()
            for (i in 0..totalTicks) {
                val tickValue = minValue + i * step
                val roundedTick = (tickValue * 10).roundToInt()

                // Only labels for whole numbers
                if (roundedTick % 10 != 0) continue

                val tickOffset = i * tickSpacingPx
                val tickXPx = centerX - currentOffset + tickOffset

                // Only visible labels
                if (tickXPx < -30 || tickXPx > widthPx + 30) continue

                val tickXDp = with(density) { tickXPx.toDp() }

                Text(
                    text = "${roundedTick / 10}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = style.labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = tickXDp - 15.dp)
                        .align(Alignment.BottomStart)
                        .width(30.dp),
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
private fun WeightScalePickerPreview() {
    var weight by remember { mutableFloatStateOf(66.2f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeightScaleColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        WeightScalePicker(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun WeightScalePickerDarkPreview() {
    var weight by remember { mutableFloatStateOf(75.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D24)),
        contentAlignment = Alignment.Center,
    ) {
        WeightScalePicker(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
            style = WeightScaleStyle(
                backgroundColor = Color(0xFF1A1D24),
                accentColor = Color(0xFFFF6B35),
                majorTickColor = Color.White,
                minorTickColor = Color.White.copy(alpha = 0.3f),
                labelColor = Color.White,
                valueColor = Color.White,
                unitColor = Color.White.copy(alpha = 0.7f),
            ),
        )
    }
}

@Preview
@Composable
private fun WeightScalePickerCompactPreview() {
    var weight by remember { mutableFloatStateOf(80f) }

    println("🟣 PREVIEW RECOMPOSE - weight: $weight")

    WeightScalePicker(
        value = weight,
        onValueChange = { newValue ->
            println("🟠 PREVIEW onValueChange CALLED - newValue: $newValue, old weight: $weight")
            weight = newValue
            println("🟡 PREVIEW weight UPDATED - weight now: $weight")
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(WeightScaleColors.Background),
        minValue = 50f,
        maxValue = 120f,
    )
}
