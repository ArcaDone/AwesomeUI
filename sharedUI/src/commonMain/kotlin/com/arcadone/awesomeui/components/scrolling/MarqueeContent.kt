package com.arcadone.awesomeui.components.scrolling

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.RadarChart
import com.arcadone.awesomeui.components.chart.RadarDataPoint
import com.arcadone.awesomeui.components.consistency.ConsistencyHeatmapCardGlow
import com.arcadone.awesomeui.components.consistency.HeatmapData
import com.arcadone.awesomeui.components.consistency.HeatmapGlowColors
import com.arcadone.awesomeui.components.consistency.HeatmapStyle
import com.arcadone.awesomeui.components.donuts.DonutSegment
import com.arcadone.awesomeui.components.donuts.DonutVariantColors
import com.arcadone.awesomeui.components.donuts.MuscleGroupDonutVariant
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

enum class MarqueeDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    BOTTOM_TO_TOP,
    TOP_TO_BOTTOM
}

@Composable
fun MarqueeContent(
    content: @Composable () -> Unit,
    direction: MarqueeDirection = MarqueeDirection.RIGHT_TO_LEFT,
    speed: Float = 220f,
    fadeEdgeColor: Color = MaterialTheme.colorScheme.surface,
    fadeEdgeWidth: Dp = 40.dp,
    delayBetweenLoops: Long = 200,
    modifier: Modifier = Modifier,
) {
    val contentOffset = remember { Animatable(0f) }
    val contentSize = remember { mutableIntStateOf(0) }
    val containerSize = remember { mutableIntStateOf(0) }

    val isHorizontal = direction == MarqueeDirection.LEFT_TO_RIGHT ||
        direction == MarqueeDirection.RIGHT_TO_LEFT
    val isReversed = direction == MarqueeDirection.LEFT_TO_RIGHT ||
        direction == MarqueeDirection.TOP_TO_BOTTOM

    LaunchedEffect(contentSize.intValue, containerSize.intValue) {
        if (contentSize.intValue > 0 && containerSize.intValue > 0) {
            while (true) {
                val startPos = if (isReversed) -contentSize.intValue.toFloat()
                else containerSize.intValue.toFloat()
                val endPos = if (isReversed) containerSize.intValue.toFloat()
                else -contentSize.intValue.toFloat()

                contentOffset.snapTo(startPos)

                contentOffset.animateTo(
                    targetValue = endPos,
                    animationSpec = tween(
                        durationMillis = ((containerSize.intValue + contentSize.intValue) / speed * 1000).toInt(),
                        easing = LinearEasing,
                    ),
                )

                delay(delayBetweenLoops)
            }
        }
    }

    Box(
        modifier = modifier
            .then(
                if (isHorizontal) Modifier.fillMaxWidth()
                else Modifier.fillMaxHeight(),
            )
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                containerSize.intValue = if (isHorizontal) coordinates.size.width
                else coordinates.size.height
            },
        contentAlignment = when (direction) {
            MarqueeDirection.LEFT_TO_RIGHT, MarqueeDirection.RIGHT_TO_LEFT -> Alignment.CenterStart
            MarqueeDirection.BOTTOM_TO_TOP, MarqueeDirection.TOP_TO_BOTTOM -> Alignment.TopCenter
        },
    ) {
        Box(
            modifier = Modifier
                .offset {
                    if (isHorizontal) {
                        IntOffset(contentOffset.value.toInt(), 0)
                    } else {
                        IntOffset(0, contentOffset.value.toInt())
                    }
                }
                .onGloballyPositioned { coordinates ->
                    contentSize.intValue = if (isHorizontal) coordinates.size.width
                    else coordinates.size.height
                },
        ) {
            content()
        }

        when (direction) {
            MarqueeDirection.LEFT_TO_RIGHT, MarqueeDirection.RIGHT_TO_LEFT -> {
                // Left Shade
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(fadeEdgeColor, Color.Transparent),
                                startX = 0f,
                                endX = with(LocalDensity.current) { fadeEdgeWidth.toPx() },
                            ),
                        ),
                )

                // Right Shade
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, fadeEdgeColor),
                                startX = with(LocalDensity.current) {
                                    containerSize.intValue.toFloat() - fadeEdgeWidth.toPx()
                                },
                                endX = containerSize.intValue.toFloat(),
                            ),
                        ),
                )
            }

            MarqueeDirection.BOTTOM_TO_TOP, MarqueeDirection.TOP_TO_BOTTOM -> {
                // Top Shade
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(fadeEdgeColor, Color.Transparent),
                                startY = 0f,
                                endY = with(LocalDensity.current) { fadeEdgeWidth.toPx() },
                            ),
                        ),
                )

                // Bottom Shade
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, fadeEdgeColor),
                                startY = with(LocalDensity.current) {
                                    containerSize.intValue.toFloat() - fadeEdgeWidth.toPx()
                                },
                                endY = containerSize.intValue.toFloat(),
                            ),
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
fun ScrollingPreview() {
    val fadeEdgeColor = Color(0xFF0D0F14)
    val workoutDays = (1..28).filter { it % 2 == 0 || it % 3 == 0 }.associate { day ->
        LocalDate(2026, 2, day) to when {
            day % 3 == 0 -> 1f
            else -> 0.5f
        }
    }
    val workoutDays2 = mapOf(
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
    Scaffold(
        containerColor = fadeEdgeColor,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp).verticalScroll(rememberScrollState())) {

            Text("From right to left", color = Color.White, fontSize = 24.sp)
            MarqueeContent(
                direction = MarqueeDirection.RIGHT_TO_LEFT,
                fadeEdgeColor = fadeEdgeColor,
                content = {

                    ConsistencyHeatmapCardGlow(
                        year = 2026,
                        month = 1,
                        data = HeatmapData(
                            dayIntensities = workoutDays2,
                            currentStreak = 2,
                            recordStreak = 12,
                        ),
                        today = LocalDate(2026, 1, 16),
                        onMonthChange = { _, _ -> },
                    )
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("From left to right ", color = Color.White, fontSize = 24.sp)

            MarqueeContent(
                direction = MarqueeDirection.LEFT_TO_RIGHT,
                fadeEdgeColor = fadeEdgeColor,
                content = {

                    Text("From left to right →")
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
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("From Bottom to Top ", color = Color.White, fontSize = 24.sp)

            MarqueeContent(
                direction = MarqueeDirection.BOTTOM_TO_TOP,
                fadeEdgeColor = fadeEdgeColor,
                modifier = Modifier.height(200.dp),
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("From Top to Bottom ", color = Color.White, fontSize = 24.sp)

            MarqueeContent(
                direction = MarqueeDirection.TOP_TO_BOTTOM,
                modifier = Modifier.height(200.dp),
                fadeEdgeColor = fadeEdgeColor,
                speed = 100f,
                content = {
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
                },
            )
        }
    }
}
