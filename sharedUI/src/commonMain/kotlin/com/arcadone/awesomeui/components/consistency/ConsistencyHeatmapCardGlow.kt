package com.arcadone.awesomeui.components.consistency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

/**
 * Alternative color palette for heatmap
 */
object HeatmapGlowColors {
    // Dark theme backgrounds
    val CardBackground = Color(0xFF1A1D24)
    val CardSurface = Color(0xFF22252E)

    // Cell colors
    val CellEmpty = Color(0xFF2E3440)
    val CellFuture = Color(0xFF2E3440).copy(alpha = 0.4f)

    // Intensity gradient (from low to high)
    val IntensityLow = Color(0xFF3B82F6).copy(alpha = 0.3f)
    val IntensityMedium = Color(0xFF3B82F6).copy(alpha = 0.6f)
    val IntensityHigh = Color(0xFF3B82F6)

    // Accent colors
    val AccentOrange = Color(0xFFFF6B35)
    val AccentGold = Color(0xFFEAB308)
    val AccentBlue = Color(0xFF3B82F6)

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B3BA)
}

/**
 * Style configuration for ConsistencyHeatmapCardGlow
 */
data class HeatmapStyle(
    val cardBackground: Color = HeatmapGlowColors.CardBackground,
    val accentColor: Color = HeatmapGlowColors.AccentBlue,
    val streakColor: Color = HeatmapGlowColors.AccentGold,
    val textPrimary: Color = HeatmapGlowColors.TextPrimary,
    val textSecondary: Color = HeatmapGlowColors.TextSecondary,
    val cellEmpty: Color = HeatmapGlowColors.CellEmpty,
    val cornerRadius: Dp = 24.dp,
    val cellCornerRadius: Dp = 8.dp,
    val showGlow: Boolean = true,
    val glowAlpha: Float = 0.5f,
    val showTooltip: Boolean = true,
    val showNavigation: Boolean = true,
    val dayLabels: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val monthNames: List<String> = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    ),
)

/**
 * Data for heatmap - only the workout intensities
 */
data class HeatmapData(
    val dayIntensities: Map<LocalDate, Float>, // date -> intensity (0.0 to 1.0, -1 for future)
    val currentStreak: Int = 0,
    val recordStreak: Int = 0,
)

/**
 * Alternative Consistency Heatmap with glow effects and dark theme.
 *
 * Receives month and year as input and automatically builds the calendar structure.
 * Stateless and fully customizable.
 *
 * @param year The year to display (e.g., 2026)
 * @param month The month to display (1-12)
 * @param data The heatmap data containing workout intensities by date
 * @param today Optional today's date for highlighting (defaults to current date)
 * @param modifier Modifier for the component
 * @param style Style configuration
 * @param onDayClick Callback when a day is clicked
 * @param onMonthChange Callback when month navigation is used (receives new year, month)
 */
@Composable
fun ConsistencyHeatmapCardGlow(
    year: Int,
    month: Int,
    data: HeatmapData,
    modifier: Modifier = Modifier,
    today: LocalDate,
    style: HeatmapStyle = HeatmapStyle(),
    onDayClick: ((LocalDate, Float) -> Unit)? = null,
    onMonthChange: ((Int, Int) -> Unit)? = null,
) {
    // Calculate calendar structure from month/year
    val calendarInfo = remember(year, month) {
        calculateCalendarInfo(year, month)
    }

    var selectedDayInfo by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(style.cardBackground, RoundedCornerShape(style.cornerRadius))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Month header with navigation
        if (style.showNavigation && onMonthChange != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        val (prevYear, prevMonth) = getPreviousMonth(year, month)
                        onMonthChange(prevYear, prevMonth)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = style.textSecondary,
                    )
                }

                Text(
                    text = "${style.monthNames.getOrElse(month) { "" }} $year",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textPrimary,
                )

                IconButton(
                    onClick = {
                        val (nextYear, nextMonth) = getNextMonth(year, month)
                        onMonthChange(nextYear, nextMonth)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = style.textSecondary,
                    )
                }
            }
        } else {
            // Static header
            Text(
                text = "${style.monthNames.getOrElse(month) { "" }} $year",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = style.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        // Streak badges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StreakBadgeItem(
                label = "Current Streak",
                value = data.currentStreak,
                isRecord = data.currentStreak >= data.recordStreak && data.currentStreak > 0,
                style = style,
            )
            StreakBadgeItem(
                label = "Record",
                value = data.recordStreak,
                isRecord = true,
                style = style,
            )
        }

        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            style.dayLabels.forEach { day ->
                Text(
                    text = day,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Calendar grid
        CalendarGridVariant(
            calendarInfo = calendarInfo,
            data = data,
            today = today,
            style = style,
            onDayClick = { date, intensity ->
                if (style.showTooltip) {
                    selectedDayInfo = when {
                        intensity < 0 -> null
                        intensity == 0f -> "Day ${date.day}: Rest"
                        intensity < 0.5f -> "Day ${date.day}: Light workout"
                        intensity < 1f -> "Day ${date.day}: Moderate workout"
                        else -> "Day ${date.day}: Intense workout! 💪"
                    }
                }
                onDayClick?.invoke(date, intensity)
            },
        )

        // Tooltip
        if (style.showTooltip) {
            selectedDayInfo?.let { info ->
                Text(
                    text = info,
                    fontSize = 12.sp,
                    color = style.accentColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Intensity: ",
                fontSize = 11.sp,
                color = style.textSecondary,
            )
            LegendItemVariant(color = style.cellEmpty, label = "Rest")
            Spacer(Modifier.width(8.dp))
            LegendItemVariant(color = style.accentColor.copy(alpha = 0.3f), label = "Light")
            Spacer(Modifier.width(8.dp))
            LegendItemVariant(color = style.accentColor.copy(alpha = 0.6f), label = "Mod")
            Spacer(Modifier.width(8.dp))
            LegendItemVariant(color = style.accentColor, label = "High")
        }
    }
}

// ============================================================================
// CALENDAR CALCULATION UTILITIES
// ============================================================================

/**
 * Calendar information for a specific month
 */
private data class CalendarInfo(
    val year: Int,
    val month: Int,
    val daysInMonth: Int,
    val firstDayOfWeek: Int, // 0 = Monday, 6 = Sunday
    val dates: List<LocalDate>, // All dates in the month
)

/**
 * Calculate calendar information from year and month
 */
private fun calculateCalendarInfo(
    year: Int,
    month: Int,
): CalendarInfo {
    val firstDay = LocalDate(year, month, 1)

    // Calculate days in month by going to next month and subtracting
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    // Get day of week for first day (1 = Monday, 7 = Sunday in kotlinx.datetime)
    // We want 0 = Monday, 6 = Sunday
    val firstDayOfWeek = firstDay.dayOfWeek.ordinal // Already 0-6 with Monday = 0

    // Generate all dates in the month
    val dates = (1..daysInMonth).map { day ->
        LocalDate(year, month, day)
    }

    return CalendarInfo(
        year = year,
        month = month,
        daysInMonth = daysInMonth,
        firstDayOfWeek = firstDayOfWeek,
        dates = dates,
    )
}

private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun getPreviousMonth(
    year: Int,
    month: Int,
): Pair<Int, Int> = if (month == 1) {
    Pair(year - 1, 12)
} else {
    Pair(year, month - 1)
}

private fun getNextMonth(
    year: Int,
    month: Int,
): Pair<Int, Int> = if (month == 12) {
    Pair(year + 1, 1)
} else {
    Pair(year, month + 1)
}

// ============================================================================
// UI COMPONENTS
// ============================================================================

@Composable
private fun StreakBadgeItem(
    label: String,
    value: Int,
    isRecord: Boolean,
    style: HeatmapStyle,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (isRecord && value > 0) {
                Box {
                    if (style.showGlow) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colorStops = arrayOf(
                                            0.0f to style.streakColor.copy(alpha = 0.5f),
                                            0.4f to style.streakColor.copy(alpha = 0.3f),
                                            0.7f to style.streakColor.copy(alpha = 0.1f),
                                            0.85f to Color.Transparent,
                                            1.0f to Color.Transparent,
                                        ),
                                    ),
                                    shape = CircleShape,
                                )
                                .align(Alignment.Center),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = style.streakColor,
                        modifier = Modifier.size(20.dp)
                            .align(Alignment.Center),
                    )
                }
            }
            Text(
                text = "$value",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRecord && value > 0) style.streakColor else style.textPrimary,
            )
            Text(
                text = "days",
                fontSize = 12.sp,
                color = style.textSecondary,
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = style.textSecondary,
        )
    }
}

@Composable
private fun CalendarGridVariant(
    calendarInfo: CalendarInfo,
    data: HeatmapData,
    today: LocalDate,
    style: HeatmapStyle,
    onDayClick: (LocalDate, Float) -> Unit,
) {
    val totalCells = calendarInfo.firstDayOfWeek + calendarInfo.daysInMonth
    val numWeeks = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(numWeeks) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = weekIndex * 7 + dayOfWeek
                    val dayNumber = cellIndex - calendarInfo.firstDayOfWeek + 1

                    if (dayNumber < 1 || dayNumber > calendarInfo.daysInMonth) {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = calendarInfo.dates[dayNumber - 1]
                        val intensity = data.dayIntensities[date]
                            ?: if (date > today) -1f else 0f
                        val isToday = date == today

                        HeatmapCellVariant(
                            dayNumber = dayNumber,
                            intensity = intensity,
                            isToday = isToday,
                            style = style,
                            onClick = { onDayClick(date, intensity) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCellVariant(
    dayNumber: Int,
    intensity: Float,
    isToday: Boolean,
    style: HeatmapStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        intensity < 0 -> style.cellEmpty.copy(alpha = 0.3f) // Future
        intensity == 0f -> style.cellEmpty
        else -> style.accentColor.copy(alpha = intensity.coerceIn(0.3f, 1f))
    }

    val textColor = when {
        intensity < 0 -> style.textSecondary.copy(alpha = 0.4f)
        intensity >= 0.5f -> style.textPrimary
        intensity > 0f -> style.accentColor
        else -> style.textSecondary
    }

    val showCellGlow = style.showGlow && intensity >= 0.7f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (isToday) {
                    Modifier
                        .clip(RoundedCornerShape(style.cellCornerRadius))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    style.streakColor.copy(alpha = 0.4f),
                                    style.streakColor.copy(alpha = 0.1f),
                                ),
                            ),
                        )
                        .padding(2.dp)
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(style.cellCornerRadius))
            .clickable(enabled = intensity >= 0) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (showCellGlow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(6.dp)
                    .background(
                        color = style.accentColor.copy(alpha = style.glowAlpha * intensity),
                        shape = RoundedCornerShape(style.cellCornerRadius),
                    ),
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(style.cellCornerRadius),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayNumber.toString(),
                fontSize = 11.sp,
                fontWeight = if (intensity >= 0.5f || isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) style.streakColor else textColor,
            )
        }
    }
}

@Composable
private fun LegendItemVariant(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = HeatmapGlowColors.TextSecondary,
        )
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun ConsistencyHeatmapCardGlowPreview() {
    // Create workout data using LocalDate keys
    val workoutDays = mapOf(
        LocalDate(2026, 1, 2) to 0.4f,
        LocalDate(2026, 1, 5) to 1f,
        LocalDate(2026, 1, 7) to 0.7f,
        LocalDate(2026, 1, 9) to 1f,
        LocalDate(2026, 1, 12) to 0.4f,
        LocalDate(2026, 1, 14) to 1f,
        LocalDate(2026, 1, 16) to 0.7f,
        LocalDate(2026, 1, 20) to 1f,
        LocalDate(2026, 1, 22) to 0.4f,
        LocalDate(2026, 1, 26) to 0.7f,
        LocalDate(2026, 1, 27) to 1f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        ConsistencyHeatmapCardGlow(
            year = 2026,
            month = 1,
            data = HeatmapData(
                dayIntensities = workoutDays,
                currentStreak = 2,
                recordStreak = 12,
            ),
            today = LocalDate(2026, 1, 16),
            onMonthChange = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun ConsistencyHeatmapCardGlowOrangePreview() {
    // Generate some workout data for February
    val workoutDays = (1..28).filter { it % 2 == 0 || it % 3 == 0 }.associate { day ->
        LocalDate(2026, 2, day) to when {
            day % 3 == 0 -> 1f
            else -> 0.5f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        ConsistencyHeatmapCardGlow(
            year = 2026,
            month = 2,
            data = HeatmapData(
                dayIntensities = workoutDays,
                currentStreak = 5,
                recordStreak = 8,
            ),
            today = LocalDate(2026, 2, 15),
            style = HeatmapStyle(
                accentColor = HeatmapGlowColors.AccentOrange,
                showNavigation = false,
            ),
        )
    }
}
