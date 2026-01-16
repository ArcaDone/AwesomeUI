package com.arcadone.awesomeui.components.donuts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI

/**
 * Variant color palette for donut charts
 */
object DonutVariantColors {
    // Dark theme backgrounds
    val CardBackground = Color(0xFF1A1D24)

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B3BA)

    // Segment colors with glow potential
    val Purple = Color(0xFF8B5CF6)
    val Orange = Color(0xFFFF6B35)
    val Teal = Color(0xFF2DD4BF)
    val Yellow = Color(0xFFF59E0B)
    val Blue = Color(0xFF3B82F6)
}

/**
 * Data for a single donut segment
 */
data class DonutSegment(
    val label: String,
    val value: Float,
    val percentage: Float, // 0.0 to 1.0
    val color: Color,
    val subtitle: String? = null, // Optional secondary info like "5k kg"
)

/**
 * Style configuration for MuscleGroupDonutVariant
 */
data class DonutVariantStyle(
    val size: Dp = 180.dp,
    val strokeWidth: Dp = 32.dp,
    val glowEnabled: Boolean = true,
    val glowAlpha: Float = 0.4f,
    val glowWidth: Dp = 20.dp,
    val gap: Dp = 3.dp,
    val cardBackground: Color = DonutVariantColors.CardBackground,
    val textPrimary: Color = DonutVariantColors.TextPrimary,
    val textSecondary: Color = DonutVariantColors.TextSecondary,
    val showLegend: Boolean = true,
    val cornerRadius: Dp = 24.dp,
)

/**
 * Variant Donut Chart with glow effects and dark theme
 *
 * Stateless and fully customizable.
 * Glow appears behind each segment proportional to its size.
 */
@Composable
fun MuscleGroupDonutVariant(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    style: DonutVariantStyle = DonutVariantStyle(),
    centerContent: @Composable () -> Unit = {},
    emptyMessage: String = "No data available",
) {
    if (segments.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(style.size)
                .background(style.cardBackground, RoundedCornerShape(style.cornerRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyMessage,
                color = style.textSecondary,
                fontSize = 14.sp,
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(style.cardBackground, RoundedCornerShape(style.cornerRadius))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Donut chart with glow
        Box(
            modifier = Modifier.size(style.size),
            contentAlignment = Alignment.Center,
        ) {
            // Glow layer (behind the donut)
            if (style.glowEnabled) {
                Canvas(
                    modifier = Modifier
                        .size(style.size + style.glowWidth * 2)
                        .blur(style.glowWidth / 2),
                ) {
                    val strokeWidth = style.strokeWidth.toPx()
                    val glowStrokeWidth = strokeWidth + style.glowWidth.toPx()
                    val radius = (size.minDimension - glowStrokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    var startAngle = -90f
                    val gapAngle = (style.gap.toPx() / (2 * PI * radius) * 360).toFloat()

                    segments.forEach { segment ->
                        val sweepAngle = (segment.percentage * 360f) - gapAngle

                        if (sweepAngle > 0) {
                            drawArc(
                                color = segment.color.copy(alpha = style.glowAlpha),
                                startAngle = startAngle + gapAngle / 2,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius,
                                ),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = glowStrokeWidth, cap = StrokeCap.Round),
                            )
                        }
                        startAngle += segment.percentage * 360f
                    }
                }
            }

            // Main donut chart
            Canvas(modifier = Modifier.size(style.size)) {
                val strokeWidth = style.strokeWidth.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f
                val gapAngle = (style.gap.toPx() / (2 * PI * radius) * 360).toFloat()

                segments.forEach { segment ->
                    val sweepAngle = (segment.percentage * 360f) - gapAngle

                    if (sweepAngle > 0) {
                        drawArc(
                            color = segment.color,
                            startAngle = startAngle + gapAngle / 2,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - radius,
                                center.y - radius,
                            ),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                    startAngle += segment.percentage * 360f
                }
            }

            // Center content slot
            centerContent()
        }

        // Legend
        if (style.showLegend) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                segments.forEach { segment ->
                    DonutLegendItem(
                        segment = segment,
                        textPrimary = style.textPrimary,
                        textSecondary = style.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutLegendItem(
    segment: DonutSegment,
    textPrimary: Color,
    textSecondary: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Color indicator with subtle glow
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                segment.color,
                                segment.color.copy(alpha = 0.6f),
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
            Text(
                text = segment.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary,
                modifier = Modifier.weight(0.8f),
            )
            Text(
                text = "${(segment.percentage * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = segment.color,
            )
            segment.subtitle?.let {
                Text(
                    modifier = Modifier.defaultMinSize(40.dp),
                    text = it,
                    fontSize = 12.sp,
                    color = textSecondary,
                    textAlign = TextAlign.End,
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
private fun MuscleGroupDonutVariantPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        MuscleGroupDonutVariant(
            segments = listOf(
                DonutSegment("Chest", 5000f, 0.32f, DonutVariantColors.Purple, "5k kg"),
                DonutSegment("Legs", 4500f, 0.28f, DonutVariantColors.Orange, "4.5k kg"),
                DonutSegment("Back", 4000f, 0.25f, DonutVariantColors.Teal, "4k kg"),
                DonutSegment("Shoulders", 1500f, 0.10f, DonutVariantColors.Yellow, "1.5k kg"),
                DonutSegment("Arms", 800f, 0.05f, DonutVariantColors.Blue, "0.8k kg"),
            ),
            centerContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "15.8k",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        "Total kg",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            },
        )
    }
}
