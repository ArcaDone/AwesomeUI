package com.arcadone.awesomeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.BarData
import com.arcadone.awesomeui.components.chart.RadarChart
import com.arcadone.awesomeui.components.chart.RadarDataPoint
import com.arcadone.awesomeui.components.chart.line.ChartDataPoint
import com.arcadone.awesomeui.components.chart.progreessionchart.FullAnalyticsPreview
import com.arcadone.awesomeui.components.chart.progreessionchart.ProgressionChartGlow
import com.arcadone.awesomeui.components.chart.progreessionchart.TrendDirection
import com.arcadone.awesomeui.components.consistency.ConsistencyHeatmapCardGlow
import com.arcadone.awesomeui.components.consistency.HeatmapData
import com.arcadone.awesomeui.components.consistency.HeatmapGlowColors
import com.arcadone.awesomeui.components.consistency.HeatmapStyle
import com.arcadone.awesomeui.components.deformable.CaloriesCardSample
import com.arcadone.awesomeui.components.deformable.MinutesCardVariant
import com.arcadone.awesomeui.components.deformable.WatchCardSample
import com.arcadone.awesomeui.components.donuts.DonutSegment
import com.arcadone.awesomeui.components.donuts.DonutVariantColors
import com.arcadone.awesomeui.components.donuts.MuscleGroupDonutVariant
import com.arcadone.awesomeui.components.picker.WeightScalePicker
import com.arcadone.awesomeui.components.picker.WeightScaleStyle
import com.arcadone.awesomeui.theme.AppTheme
import com.arcadone.shared.timer.TimerState
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}) = AppTheme(onThemeChanged) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(0.dp),
        content = {
            FullAnalyticsPreview()
        },
    )
}

// ============================================================================
// SHOWCASE SCREEN 1: Cards & Timer Components
// ============================================================================

@Preview
@Composable
fun ShowcaseScreen1_CardsAndTimer() {
    // Animated timer state
    var timerSeconds by remember { mutableIntStateOf(45) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            timerSeconds--
        }
    }

    val weekData = listOf(
        BarData(label = "Mon", progress = 0.7f),
        BarData(label = "Tue", progress = 0.4f),
        BarData(label = "Wed", progress = 0.6f),
        BarData(label = "Thu", progress = 0f),
        BarData(label = "Fri", progress = 0.5f),
        BarData(label = "Sat", progress = 1.0f),
        BarData(label = "Sun", progress = 0.65f),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = "Charts & Cards",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        // Calories Card with Bar Chart
        CaloriesCardSample(
            modifier = Modifier.fillMaxWidth(),
            caloriesValue = "450",
            chartData = weekData,
            selectedIndex = 5,
        )

        // Timer and Minutes Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WatchCardSample(
                modifier = Modifier.weight(1f),
                timerState = TimerState(
                    timeRemaining = timerSeconds,
                    totalTime = 60,
                    isRest = false,
                ),
            )
            MinutesCardVariant(
                modifier = Modifier.weight(1f),
                maxMinutes = 127,
                animationDurationMs = 5000,
            )
        }

        // Progression Chart
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

        // Full Width Minutes Card
        MinutesCardVariant(
            dataPoints = listOf(0.5f, 0.8f, 0.4f, 0.9f, 0.2f, 0.6f, 0.5f),
            modifier = Modifier.fillMaxWidth(),
            maxMinutes = 91,
            animationDurationMs = 3000,
            animateToProgress = 0.85f,
            showTooltipAtEnd = true,
        )
    }
}

// ============================================================================
// SHOWCASE SCREEN 2: Radar, Donut & Heatmap
// ============================================================================

@Preview
@Composable
fun ShowcaseScreen2_ChartsAndHeatmap() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Text(
            text = "Analytics Dashboard",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        // Radar Chart
        RadarChart(
            data = listOf(
                RadarDataPoint("Chest", 0.85f),
                RadarDataPoint("Back", 0.70f),
                RadarDataPoint("Legs", 0.60f),
                RadarDataPoint("Shoulders", 0.45f),
                RadarDataPoint("Arms", 0.80f),
            ),
            title = "Muscle Balance",
        )

        // Donut Chart
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

        // Consistency Heatmap
        ConsistencyHeatmapCardGlow(
            year = 2026,
            month = 1,
            data = HeatmapData(
                dayIntensities = workoutDays,
                currentStreak = 5,
                recordStreak = 12,
            ),
            today = LocalDate(2026, 1, 17),
            style = HeatmapStyle(
                accentColor = HeatmapGlowColors.AccentOrange,
                showNavigation = false,
            ),
        )
    }
}

// ============================================================================
// SHOWCASE SCREEN 3: Heatmap and Weight
// ============================================================================

@Preview
@Composable
fun ShowcaseScreen3_HeatmapAndWeight() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Text(
            text = "Dashboard",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

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
                .fillMaxWidth()
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
}
