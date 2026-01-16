package com.arcadone.awesomeui.components.consistency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val textSecondary = Color(0xFFB0B7C3) // Less important text
private val surfaceColor = Color(0xFF1C2333) // Card/surface background
private val primaryColor = Color(0xFF0D59F2)
private val borderSurfaceColor = Color(0xFF374151) // Standard borders
private val goldColor = Color(0xFFEAB308)

// Gold/all-out

/**
 * Enhanced Consistency Heatmap Card with proper calendar alignment
 */
@Composable
fun ConsistencyHeatmapCard(
    data: ConsistencyHeatmapData,
    modifier: Modifier = Modifier,
) {
    var selectedDayInfo by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Streak Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StreakBadge(
                    label = "Streak Attuale",
                    value = data.currentStreak,
                    isRecord = data.currentStreak >= data.recordStreak && data.currentStreak > 0,
                )
                StreakBadge(
                    label = "Record",
                    value = data.recordStreak,
                    isRecord = true,
                )
            }

            // Day headers (L M M G V S D)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("L", "M", "M", "G", "V", "S", "D").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Calendar Grid
            CalendarGrid(
                data = data,
                onDayClick = { dayNumber, intensity ->
                    selectedDayInfo = when {
                        intensity < 0 -> null
                        intensity == 0f -> "$dayNumber ${data.monthName.split(" ")[0]}: Riposo"
                        intensity < 0.5f -> "$dayNumber ${data.monthName.split(" ")[0]}: Allenamento leggero"
                        intensity < 1f -> "$dayNumber ${data.monthName.split(" ")[0]}: Allenamento moderato"
                        else -> "$dayNumber ${data.monthName.split(" ")[0]}: Allenamento intenso! 💪"
                    }
                },
            )

            // Selected day tooltip
            selectedDayInfo?.let { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Intensità: ",
                    fontSize = 11.sp,
                    color = textSecondary,
                )
                LegendItem(color = borderSurfaceColor, label = "Riposo")
                Spacer(Modifier.width(8.dp))
                LegendItem(color = primaryColor.copy(alpha = 0.3f), label = "Leggero")
                Spacer(Modifier.width(8.dp))
                LegendItem(color = primaryColor.copy(alpha = 0.6f), label = "Medio")
                Spacer(Modifier.width(8.dp))
                LegendItem(color = primaryColor, label = "Intenso")
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    data: ConsistencyHeatmapData,
    onDayClick: (Int, Float) -> Unit,
) {
    // Calculate number of weeks needed
    val totalCells = data.firstDayOfWeek + data.daysInMonth
    val numWeeks = (totalCells + 6) / 7

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(numWeeks) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = weekIndex * 7 + dayOfWeek
                    val dayNumber = cellIndex - data.firstDayOfWeek + 1

                    if (dayNumber < 1 || dayNumber > data.daysInMonth) {
                        // Empty cell before month start or after month end
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        )
                    } else {
                        val intensity = data.dayIntensities[dayNumber] ?: 0f
                        val isToday = dayNumber == data.todayDayNumber

                        HeatmapCell(
                            dayNumber = dayNumber,
                            intensity = intensity,
                            isToday = isToday,
                            onClick = { onDayClick(dayNumber, intensity) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakBadge(
    label: String,
    value: Int,
    isRecord: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (isRecord && value > 0) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = goldColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRecord && value > 0) goldColor else Color.White,
            )
            Text(
                text = "giorni",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textSecondary,
        )
    }
}

@Composable
private fun HeatmapCell(
    dayNumber: Int,
    intensity: Float,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        intensity < 0 -> borderSurfaceColor.copy(alpha = 0.2f) // Future days
        intensity == 0f -> borderSurfaceColor
        else -> primaryColor.copy(alpha = intensity.coerceIn(0.2f, 1f))
    }

    val textColor = when {
        intensity < 0 -> Color.Gray.copy(alpha = 0.4f)
        intensity >= 0.5f -> Color.White
        intensity > 0f -> primaryColor
        else -> Color.Gray
    }

    val borderModifier = if (isToday) {
        Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(2.dp)
            .clip(CircleShape)
            .background(goldColor.copy(alpha = 0.3f))
    } else {
        Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .clickable(enabled = intensity >= 0) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayNumber.toString(),
            fontSize = 10.sp,
            fontWeight = if (intensity >= 0.5f || isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) goldColor else textColor,
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = textSecondary,
        )
    }
}

@Preview
@Composable
private fun ConsistencyHeatmapCardPreview() {
    // January 2026: 1st is Thursday (3), 31 days
    val dayIntensities = mapOf(
        1 to 0f, 2 to 0.4f, 3 to 0f, 4 to 0f, // Thu, Fri, Sat, Sun
        5 to 1f, 6 to 0f, 7 to 0.7f, 8 to 0f, 9 to 1f, 10 to 0f, 11 to 0f, // Week 2
        12 to 0.4f, 13 to 0f, 14 to 1f, 15 to 0f, 16 to 0.7f, 17 to 0f, 18 to 0f,
        19 to 0f, 20 to 1f, 21 to 0f, 22 to 0.4f, 23 to 0f, 24 to 0f, 25 to 0f,
        26 to 0.7f, 27 to 1f, 28 to 0f, // Last workout days
        29 to -1f, 30 to -1f, 31 to -1f, // Future days
    )

    ConsistencyHeatmapCard(
        data = ConsistencyHeatmapData(
            monthName = "Dicembre 2026",
            daysInMonth = 31,
            firstDayOfWeek = 3, // Thursday
            dayIntensities = dayIntensities,
            currentStreak = 2,
            recordStreak = 12,
            todayDayNumber = 28,
        ),
        modifier = Modifier.padding(16.dp),
    )
}
