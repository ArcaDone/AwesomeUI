package com.arcadone.awesomeui.components.consistency

/**
 * Data model for Consistency Heatmap with proper calendar info
 */
data class ConsistencyHeatmapData(
    val monthName: String, // "Gennaio 2026"
    val daysInMonth: Int, // 31
    val firstDayOfWeek: Int, // 0=Lunedì, ... 6=Domenica (for Jan 1 2026 = 3 (Giovedì))
    val dayIntensities: Map<Int, Float>, // day number (1-31) -> intensity (0f-1f)
    val currentStreak: Int,
    val recordStreak: Int,
    val todayDayNumber: Int, // Which day is today (1-31)
)
