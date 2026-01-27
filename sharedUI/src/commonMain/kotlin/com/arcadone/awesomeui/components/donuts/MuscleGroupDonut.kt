package com.arcadone.awesomeui.components.donuts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arcadone.awesomeui.components.deformable.VariantColors

/**
 * Total volume distribution per muscle group (for donut chart)
 */
data class MuscleGroupVolume(val muscleGroup: String, val volume: Double, val percentage: Float, val color: Color)

/**
 * Donut chart for muscle group volume distribution
 */
@Composable
fun MuscleGroupDonut(
    distribution: List<MuscleGroupVolume>,
    modifier: Modifier = Modifier,
) {
    if (distribution.isEmpty()) {
        Box(
            modifier = modifier.height(240.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Completa allenamenti per vedere la distribuzione muscolare",
                style = MaterialTheme.typography.bodyMedium,
                color = VariantColors.AccentOrange,
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Donut chart
        Canvas(
            modifier = Modifier.size(160.dp).padding(top = 24.dp),
        ) {
            val strokeWidth = 40.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = -90f

            distribution.forEach { group ->
                val sweepAngle = group.percentage * 360f

                drawArc(
                    color = group.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius - strokeWidth / 2,
                        center.y - radius - strokeWidth / 2,
                    ),
                    size = Size(
                        (radius + strokeWidth / 2) * 2,
                        (radius + strokeWidth / 2) * 2,
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )

                startAngle += sweepAngle
            }
        }

        // Legend with percentages
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            distribution.forEach { group ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(group.color, CircleShape),
                        )
                        Text(
                            text = group.muscleGroup,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${(group.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = group.color,
                        )
                        Text(
                            text = "${(group.volume / 1000).toInt()}k kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = VariantColors.AccentOrange,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MuscleGroupDonutPreview() {
    MuscleGroupDonut(
        distribution = listOf(
            MuscleGroupVolume("PETTO", 5000.0, 0.32f, Color(0xFF6B5CE7)),
            MuscleGroupVolume("GAMBE", 4500.0, 0.28f, Color(0xFFF59E0B)),
            MuscleGroupVolume("SCHIENA", 4000.0, 0.25f, Color(0xFF2DD4BF)),
            MuscleGroupVolume("SPALLE", 1500.0, 0.10f, Color(0xFFEC4899)),
            MuscleGroupVolume("BRACCIA", 800.0, 0.05f, Color(0xFF8B5CF6)),
        ),
    )
}
