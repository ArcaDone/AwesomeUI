package com.arcadone.awesomeui.components.deformable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.BarData
import com.arcadone.awesomeui.components.chart.BasicBarChart
import com.arcadone.awesomeui.components.chart.DesignColors
import com.arcadone.awesomeui.components.timer.CircularTimerDefaults
import com.arcadone.awesomeui.components.timer.TimerWatch
import com.arcadone.shared.timer.TimerState

@Composable
fun DeformableCornerItem(
    modifier: Modifier = Modifier.wrapContentSize().size(200.dp),
    circleRadius: Dp = 24.dp,
    rectPadding: Dp = 10.dp,
    cardColor: Color = Color(0xFFE0E0E0),
    circleColor: Color = Color(0xFFFF5252),
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    topLeft: Dp = 32.dp,
    bottomLeft: Dp = 32.dp,
    bottomRight: Dp = 32.dp,
    contentCircle: @Composable BoxScope.() -> Unit = {},
    contentRectangle: @Composable BoxScope.() -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Customize every corner",
            )
        }
    },
) {
    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = cardColor,
                    shape = InclusiveCutoutShape(
                        circleRadius = circleRadius,
                        padding = rectPadding,
                        topLeft = topLeft,
                        bottomLeft = bottomLeft,
                        bottomRight = bottomRight,
                    ),
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = InclusiveCutoutShape(
                        circleRadius = circleRadius,
                        padding = rectPadding,
                        topLeft = topLeft,
                        bottomLeft = bottomLeft,
                        bottomRight = bottomRight,
                    ),
                ),
        )

        // Rectangle content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(borderWidth),
        ) {
            contentRectangle()
        }

        // The Circle
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(circleRadius * 2)
                .clip(CircleShape)
                .background(circleColor),
        ) {
            contentCircle()
        }
    }
}

class InclusiveCutoutShape(
    private val topLeft: Dp = 12.dp,
    private val bottomLeft: Dp = 12.dp,
    private val bottomRight: Dp = 12.dp,
    private val circleRadius: Dp,
    private val padding: Dp = 10.dp,
    private val smoothing: Dp = 20.dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(
        path = Path().apply {
            val w = size.width
            val h = size.height

            val tl = with(density) { topLeft.toPx() }
            val bl = with(density) { bottomLeft.toPx() }
            val br = with(density) { bottomRight.toPx() }

            val cRadius = with(density) { circleRadius.toPx() }
            val pad = with(density) { padding.toPx() }
            val s = with(density) { smoothing.toPx() }

            val cutoutSize = (cRadius * 2) + pad

            // Top-Left Corner
            moveTo(0f, tl)
            if (tl > 0) {
                quadraticTo(0f, 0f, tl, 0f)
            } else {
                lineTo(0f, 0f)
            }

            val startCutoutX = w - cutoutSize - s
            lineTo(x = startCutoutX, y = 0f)

            // Cut
            quadraticTo(
                x1 = w - cutoutSize,
                y1 = 0f,
                x2 = w - cutoutSize,
                y2 = s,
            )
            quadraticTo(
                x1 = w - cutoutSize,
                y1 = cutoutSize,
                x2 = w - s,
                y2 = cutoutSize,
            )
            quadraticTo(
                x1 = w,
                y1 = cutoutSize,
                x2 = w,
                y2 = cutoutSize + s,
            )

            // Bottom-Right Corner
            lineTo(x = w, y = h - br)
            if (br > 0) {
                quadraticTo(x1 = w, y1 = h, x2 = w - br, y2 = h)
            } else {
                lineTo(x = w, y = h)
            }

            // Bottom-Left Corner
            lineTo(x = bl, y = h)
            if (bl > 0) {
                quadraticTo(x1 = 0f, y1 = h, x2 = 0f, y2 = h - bl)
            } else {
                lineTo(x = 0f, y = h)
            }

            close()
        },
    )
}

@Preview
@Composable
private fun BasePreview() {
    DeformableCornerItem(
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp),
        topLeft = 4.dp,
        bottomLeft = 64.dp,
        bottomRight = 40.dp,
    )
}

@Composable
fun StepsLiquidCard(modifier: Modifier = Modifier) {
    DeformableCornerItem(
        modifier = modifier.height(180.dp),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = DesignColors.StepsGray,
        circleColor = DesignColors.StepsGray,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        topLeft = 24.dp,
        bottomLeft = 24.dp,
        bottomRight = 24.dp,
        contentCircle = {
            Icon(
                imageVector = Icons.Default.RunCircle,
                contentDescription = null,
                tint = DesignColors.IconTintSteps,
                modifier = Modifier.align(Alignment.Center).size(48.dp),
            )
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("Run:", color = DesignColors.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TimerWatch(
                        timerState = TimerState(timeRemaining = 45, totalTime = 60, isRest = true),
                        isOvertime = false,
                        showHeartbeat = false,
                        topIcon = null,
                        style = CircularTimerDefaults.style().copy(
                            size = 100.dp,
                            restColor = DesignColors.IconTintSteps,
                            trackColor = DesignColors.IconTintSteps.copy(alpha = 0.2f),
                            timeTextStyle = CircularTimerDefaults.style().timeTextStyle.copy(fontSize = 16.sp),
                            labelTextStyle = CircularTimerDefaults.style().labelTextStyle.copy(fontSize = 8.sp, color = Color.White),
                        ),
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        },
    )
}

@Composable
fun MinutesLiquidCard(
    modifier: Modifier = Modifier,
    minutes: String = "127",
) {
    DeformableCornerItem(
        modifier = modifier.height(180.dp),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = DesignColors.MinutesOrange,
        circleColor = DesignColors.MinutesOrange,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentCircle = {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = DesignColors.IconTintMinutes,
                modifier = Modifier.align(Alignment.Center).size(24.dp),
            )
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("Minutes:", color = DesignColors.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(minutes, color = DesignColors.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("min", color = DesignColors.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Canvas(modifier = Modifier.fillMaxWidth().height(45.dp)) {
                    val path = Path().apply {
                        val w = size.width
                        val h = size.height
                        moveTo(0f, h * 0.6f)
                        cubicTo(w * 0.15f, h * 0.6f, w * 0.15f, h * 0.3f, w * 0.3f, h * 0.3f)
                        cubicTo(w * 0.45f, h * 0.3f, w * 0.45f, h * 0.9f, w * 0.6f, h * 0.9f)
                        cubicTo(w * 0.75f, h * 0.9f, w * 0.8f, h * 0.4f, w, h * 0.4f)
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun DoubleItemsInRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StepsLiquidCard(modifier = Modifier.weight(1f))
        MinutesLiquidCard(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CaloriesLiquidCard(
    modifier: Modifier = Modifier,
    // --- Data ---
    caloriesValue: String = "450",
    cardTitle: String = "Calories Burned:",
    caloriesUnit: String = "kcal",
    chartData: List<BarData>,
    selectedIndex: Int = -1,

    // --- Style adn color ---
    cardColor: Color = DesignColors.CaloriesBlack,
    textColor: Color = DesignColors.White,
    activeBarColor: Color = DesignColors.MinutesOrange,
    inactiveBarStripesBg: Color = Color(0xFF222222),
    stripeColor: Color = Color.Black,

    // --- Geometry ---
    height: Dp = 200.dp,
    barWidth: Dp = 28.dp,
    barCornerRadius: Dp = 8.dp,
    contentCircle: @Composable BoxScope.() -> Unit = {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = DesignColors.IconTintCalories,
            modifier = Modifier.align(Alignment.Center).size(24.dp),
        )
    },

    onBarClick: ((Int) -> Unit)? = null,
) {
    DeformableCornerItem(
        modifier = modifier.height(height),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = cardColor,
        circleColor = cardColor,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentCircle = {
            contentCircle()
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Header
                Text(
                    text = cardTitle,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )

                // Value Large
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = caloriesValue,
                        color = textColor,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = caloriesUnit,
                        color = textColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                BasicBarChart(
                    selectedIndex = selectedIndex,
                    barWidth = barWidth,
                    barCornerRadius = barCornerRadius,
                    textColor = textColor,
                    activeBarColor = activeBarColor,
                    inactiveBarStripesBg = inactiveBarStripesBg,
                    stripeColor = stripeColor,
                    chartData = chartData,
                    onBarClick = onBarClick,
                )
            }
        },
    )
}

@Preview
@Composable
fun SingleItemsInRow() {
    val weekData = listOf(
        BarData(label = "Mon", progress = 0.7f),
        BarData(label = "Tue", progress = 0.4f),
        BarData(label = "Wed", progress = 0.6f),
        BarData(label = "Thu", progress = 0f),
        BarData(label = "Fri", progress = 0.5f),
        BarData(label = "Sat", progress = 1.0f),
        BarData(label = "Sun", progress = 0.65f),
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CaloriesLiquidCard(
            modifier = Modifier.fillMaxWidth(),
            caloriesValue = "450",
            chartData = weekData,
            selectedIndex = 2,
        )
    }
}
