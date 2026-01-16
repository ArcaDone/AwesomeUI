package com.arcadone.awesomeui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        chartData.forEachIndexed { index, data ->
            val isSelected = index == selectedIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onBarClick != null) { onBarClick?.invoke(index) },
            ) {
                // Chart area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val barsModifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(data.progress)
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

data class BarData(
    val label: String, // Es: "Mon", "Tue"
    val progress: Float, // From 0.0f to 1.0f
)

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
        )
    }
}
