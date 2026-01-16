package com.arcadone.awesomeui.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.consistency.HeatmapGlowColors
import com.arcadone.awesomeui.components.deformable.VariantColors
import com.arcadone.awesomeui.components.striped.StripedBarItem
import com.arcadone.awesomeui.components.utils.clickableNoOverlay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * BasicBarChart with optional bottom-to-top animation
 *
 * @param animate If true, bars animate from 0 to their target height
 * @param animationDurationMs Duration of the animation in milliseconds
 * @param animationDelayPerBar Delay between each bar's animation start (stagger effect)
 */
@Composable
fun BasicBarChart(
    modifier: Modifier = Modifier,
    selectedIndex: Int = -1,
    barWidth: Dp = 28.dp,
    barCornerRadius: Dp = 8.dp,
    textColor: Color = DesignColors.White,
    activeBarColor: Color = VariantColors.AccentOrange,
    inactiveBarStripesBg: Color = Color.Gray,
    stripeColor: Color = Color.Black,
    chartData: List<BarData> = listOf(
        BarData(label = "Mon", progress = 0.7f),
        BarData(label = "Tue", progress = 0.4f),
        BarData(label = "Wed", progress = 0.6f),
        BarData(label = "Thu", progress = 0f),
        BarData(label = "Fri", progress = 0.5f),
        BarData(label = "Sat", progress = 1.0f),
        BarData(label = "Sun", progress = 0.65f),
    ),
    onBarClick: ((Int) -> Unit)? = null,
    animate: Boolean = true,
    animationDurationMs: Int = 800,
    animationDelayPerBar: Int = 100,
    showTooltip: Boolean = true,
    tooltipValueFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
    tooltipBackgroundColor: Color = Color(0xFF2A2D35),
    tooltipBorderColor: Color = activeBarColor,
) {
    // Animation states for each bar
    val animationProgress = chartData.mapIndexed { index, _ ->
        remember { Animatable(if (animate) 0f else 1f) }
    }

    // Trigger animations
    LaunchedEffect(animate, chartData) {
        if (animate) {
            // Reset all to 0
            animationProgress.forEach { it.snapTo(0f) }

            // Animate each bar - in parallel if no delay, or staggered if delay > 0
            kotlinx.coroutines.coroutineScope {
                if (animationDelayPerBar == 0) {
                    // All at once - launch all animations in parallel
                    animationProgress.map { animatable ->
                        async {
                            animatable.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = animationDurationMs,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }.awaitAll()
                } else {
                    // Stagger effect - delay between each bar
                    animationProgress.mapIndexed { index, animatable ->
                        launch {
                            delay(index * animationDelayPerBar.toLong())
                            animatable.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = animationDurationMs,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }.joinAll()
                }
            }
        } else {
            animationProgress.forEach { it.snapTo(1f) }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        chartData.forEachIndexed { index, data ->
            val isSelected = index == selectedIndex
            val currentProgress = animationProgress.getOrNull(index)?.value ?: 1f
            val animatedHeight = data.progress * currentProgress

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickableNoOverlay { onBarClick?.invoke(index) },
            ) {
                // Chart area with tooltip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Tooltip above bar
                        if (isSelected && showTooltip && animatedHeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = tooltipBackgroundColor,
                                        shape = RoundedCornerShape(6.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = tooltipValueFormatter(data.progress),
                                        color = tooltipBorderColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = data.label,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 9.sp,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Bar
                        val barsModifier = Modifier
                            .width(barWidth)
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(barCornerRadius))

                        if (isSelected) {
                            // Active Bar
                            Box(
                                modifier = barsModifier
                                    .background(activeBarColor),
                            )
                        } else {
                            StripedBarItem(
                                modifier = barsModifier,
                                backgroundColor = inactiveBarStripesBg,
                                stripeColor = stripeColor,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center,
                        text = data.label,
                        color = if (isSelected) textColor else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

object DesignColors {
    val StepsGray = Color(0xFF6B6E75)
    val MinutesOrange = Color(0xFFF48C46)
    val CaloriesBlack = Color(0xFF000000)

    val IconTintSteps = Color(0xFFFFD8B1)
    val IconTintMinutes = Color(0xFFFEE6C5)
    val IconTintCalories = Color(0xFFFFB4A9)

    val White = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.7f)
}

data class BarData(val label: String, val progress: Float)

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
fun BasicBarChartPreview() {
    Card(
        modifier = Modifier
            .background(HeatmapGlowColors.CardBackground)
            .padding(24.dp),
        colors = CardDefaults.cardColors().copy(containerColor = HeatmapGlowColors.CardBackground),
        shape = RoundedCornerShape(24.dp),
    ) {
        BasicBarChart(
            modifier = Modifier.padding(48.dp),
            selectedIndex = 2,
            animate = false,
        )
    }
}

@Preview
@Composable
fun BasicBarChartAnimatedPreview() {
    val selectedIndex = remember { mutableStateOf(-1) }
    Card(
        modifier = Modifier
            .background(HeatmapGlowColors.CardBackground)
            .padding(24.dp),
        colors = CardDefaults.cardColors().copy(containerColor = HeatmapGlowColors.CardBackground),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Animated bars (stagger effect)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )

            BasicBarChart(
                modifier = Modifier.padding(horizontal = 24.dp),
                selectedIndex = selectedIndex.value,
                animate = true,
                animationDurationMs = 800,
                animationDelayPerBar = 100,
                onBarClick = {
                    println("Bar clicked: $it")
                    selectedIndex.value = it
                },
            )
        }
    }
}

@Preview
@Composable
fun BasicBarChartFastAnimationPreview() {
    val selectedIndex = remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier
            .background(HeatmapGlowColors.CardBackground)
            .padding(24.dp),
        colors = CardDefaults.cardColors().copy(containerColor = HeatmapGlowColors.CardBackground),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Fast animation (all at once)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )

            BasicBarChart(
                modifier = Modifier.padding(horizontal = 24.dp),
                selectedIndex = selectedIndex.value,
                animate = true,
                animationDurationMs = 2000,
                animationDelayPerBar = 0,
                onBarClick = {
                    println("Bar clicked: $it")
                    selectedIndex.value = it
                },
            )
        }
    }
}
