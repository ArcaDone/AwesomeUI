package com.arcadone.awesomeui.components.chart.progreessionchart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.line.ChartDataPoint
import com.arcadone.awesomeui.components.chart.line.LineChart

private val borderSurfaceColor = Color(0xFF374151) // Standard borders
private val surfaceColor = Color(0xFF1C2333)
private val greenColor = Color(0xFF10B981)

// Success, positive trends
@Composable
fun ProgressionChart(
    modifier: Modifier = Modifier,
    chartData: List<ChartDataPoint>,
    plus: String = "+5%",
    exerciseName: String = "Bench Press 1RM",
    weight: String = "105 kg",
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderSurfaceColor),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        exerciseName,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        weight,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Surface(
                    color = greenColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = greenColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            plus,
                            color = greenColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Chart with Y-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Y-axis labels (ordinata)
                val minValue = chartData.minOfOrNull { it.value } ?: 0f
                val maxValue = chartData.maxOfOrNull { it.value } ?: 100f
                val midValue = (minValue + maxValue) / 2

                Column(
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${maxValue.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                    )
                    Text(
                        text = "${midValue.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                    )
                    Text(
                        text = "${minValue.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                    )
                }

                // LineChart
                LineChart(
                    data = chartData,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                chartData.forEach { (_, label) ->
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        letterSpacing = 0.3.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val chartData = remember {
        listOf(
            ChartDataPoint(100f, "Nov 1"),
            ChartDataPoint(90f, "Nov 15"),
            ChartDataPoint(100f, "Dec 1"),
            ChartDataPoint(103f, "Dec 15"),
            ChartDataPoint(95f, "Today"),
        )
    }
    ProgressionChart(
        modifier = Modifier.padding(horizontal = 16.dp),
        chartData = chartData,
    )
}
