package com.arcadone.awesomeui.components.gauge

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun NutritionCard(
    title: String,
    score: Int, // 0 to 100
    scoreLabel: String,
    stats: List<NutritionStat>,
    modifier: Modifier = Modifier,
    accentGradient: List<Color> = listOf(Color(0xFF4DB6AC), Color(0xFFD4E157)),
    backgroundColor: Color = Color(0xFF191B20),
    contentColor: Color = Color.White,
    actionIcon: ImageVector? = null,
    animateGradient: Boolean = false,
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(animateGradient, score) {
        if (animateGradient) {
            startAnimation = false
            delay(100)
            startAnimation = true
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animateGradient && startAnimation) {
            score / 100f
        } else if (animateGradient) {
            0f
        } else {
            score / 100f
        },
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "gauge_progress",
    )

    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .semantics {
                contentDescription = "$title: $scoreLabel, $score percent"
            },
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
            )
            actionIcon?.let {
                Icon(it, contentDescription = "Action", tint = contentColor.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Semi-circle Gauge
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            NutritionGauge(
                progress = if (animateGradient) animatedProgress else score / 100f,
                gradient = accentGradient,
                modifier = Modifier.size(280.dp),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 20.dp),
            ) {
                Text(scoreLabel, color = contentColor.copy(alpha = 0.7f), fontSize = 14.sp)
                val displayScore = if (animateGradient) {
                    (animatedProgress * 100).toInt()
                } else {
                    score
                }

                Text(
                    text = "$displayScore%",
                    color = contentColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            stats.forEach { stat ->
                NutritionStatItem(stat, contentColor)
            }
        }
    }
}

@Composable
fun NutritionGauge(
    progress: Float,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    segments: Int = 22,
    innerRadiusDp: Dp = 80.dp,
    outerRadiusDp: Dp = 110.dp,
    cornerRadius: Float = 8f, // Radius for rounding corners
    showGlow: Boolean = true,
    glowAlpha: Float = 0.1f,
) {
    val degreeToRad = (PI / 180f).toFloat()

    Canvas(modifier = modifier) {
        val innerRadius = innerRadiusDp.toPx()
        val outerRadius = outerRadiusDp.toPx()
        val center = Offset(size.width / 2, size.height)

        val startAngle = 180f
        val totalSweep = 180f
        val gapAngle = 3.5f // Recommended for rounded look
        val segmentSweep = (totalSweep / segments) - gapAngle

        for (i in 0 until segments) {
            val angleDeg = startAngle + (i * (segmentSweep + gapAngle)) + (gapAngle / 2)
            val isFilled = (i.toFloat() / (segments - 1)) <= progress

            val baseColor = if (isFilled) {
                lerpColor(gradient, i.toFloat() / (segments - 1))
            } else {
                Color(0xFF2C2C2C)
            }

            val startRad = angleDeg * degreeToRad
            val endRad = (angleDeg + segmentSweep) * degreeToRad

            // Defining key points for the path
            val p1 = Offset(center.x + innerRadius * cos(startRad), center.y + innerRadius * sin(startRad))
            val p2 = Offset(center.x + outerRadius * cos(startRad), center.y + outerRadius * sin(startRad))
            val p4 = Offset(center.x + innerRadius * cos(endRad), center.y + innerRadius * sin(endRad))

            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                arcTo(
                    rect = Rect(
                        center.x - outerRadius,
                        center.y - outerRadius,
                        center.x + outerRadius,
                        center.y + outerRadius,
                    ),
                    startAngleDegrees = angleDeg,
                    sweepAngleDegrees = segmentSweep,
                    forceMoveTo = false,
                )
                lineTo(p4.x, p4.y)
                arcTo(
                    rect = Rect(
                        center.x - innerRadius,
                        center.y - innerRadius,
                        center.x + innerRadius,
                        center.y + innerRadius,
                    ),
                    startAngleDegrees = angleDeg + segmentSweep,
                    sweepAngleDegrees = -segmentSweep,
                    forceMoveTo = false,
                )
                close()
            }

            // Optional Glow effect for active segments
            if (showGlow && isFilled) {
                drawPath(
                    path = path,
                    color = baseColor.copy(alpha = glowAlpha),
                    style = Stroke(
                        width = cornerRadius * 2f,
                        join = StrokeJoin.Round,
                        cap = StrokeCap.Round,
                    ),
                )
            }

            // Main segment rendering
            // 1. Stroke to round the corners
            drawPath(
                path = path,
                color = baseColor,
                style = Stroke(
                    width = cornerRadius,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round,
                ),
            )
            // 2. Fill the body
            drawPath(
                path = path,
                color = baseColor,
                style = Fill,
            )
        }
    }
}

@Composable
private fun NutritionStatItem(
    stat: NutritionStat,
    color: Color,
) {
    Column {
        Text(stat.label, color = color.copy(alpha = 0.6f), fontSize = 14.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                stat.value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alignByBaseline(),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                stat.unit,
                color = color.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

data class NutritionStat(val label: String, val value: String, val unit: String)

// Simple linear interpolation for gradient segments
private fun lerpColor(
    colors: List<Color>,
    fraction: Float,
): Color {
    if (colors.size < 2) return colors.firstOrNull() ?: Color.Gray
    val segment = (colors.size - 1) * fraction
    val index = segment.toInt().coerceIn(0, colors.size - 2)
    val rem = segment - index
    return Color(
        red = colors[index].red + (colors[index + 1].red - colors[index].red) * rem,
        green = colors[index].green + (colors[index + 1].green - colors[index].green) * rem,
        blue = colors[index].blue + (colors[index + 1].blue - colors[index].blue) * rem,
        alpha = 1f,
    )
}

@Preview
@Composable
fun GaugeExample() {
    Box(modifier = Modifier.background(Color.Black).fillMaxSize().padding(16.dp)) {
        NutritionCard(
            title = "Nutrition Overview",
            score = 85,
            scoreLabel = "Progress",
            stats = listOf(
                NutritionStat("Carbs", "149", "g"),
                NutritionStat("Calorie", "45", "g"),
                NutritionStat("Protein", "82", "g"),
            ),
            accentGradient = listOf(Color(0xFF4DB6AC), Color(0xFFD4E157)),
            animateGradient = true,
        )
    }
}
