package com.arcadone.awesomeui.components.deformable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.chart.BarData
import com.arcadone.awesomeui.components.chart.BasicBarChart
import com.arcadone.awesomeui.components.chart.DesignColors
import com.arcadone.awesomeui.components.timer.CircularTimerDefaults
import com.arcadone.awesomeui.components.timer.TimerWatch
import com.arcadone.shared.timer.TimerState

/**
 * Variant color palette - Dark theme with orange accents
 */
object VariantColors {
    // Card backgrounds
    val CardDark = Color(0xFF1A1D24)
    val CardDarkGray = Color(0xFF2A2D35)
    val CardDarkSurface = Color(0xFF22252E)

    // Accent colors with glow
    val AccentOrange = Color(0xFFFF6B35)
    val AccentOrangeGlow = Color(0xFFFF8C5A)
    val AccentOrangeMuted = Color(0xFFFF9A6C)

    // Text colors
    val TextPrimary = AccentOrange
    val TextSecondary = Color(0xFFB0B3BA)

    // Chart colors
    val ChartBarActive = Color(0xFFFF6B35)
    val ChartBarInactive = Color(0xFF3A3D45)
    val ChartStripe = Color(0xFF2A2D35)

    // Glow colors
    val GlowOrange = Color(0xFFFF6B35)
    val GlowRed = Color(0xFFFF4757)
}

/**
 * Variant version of DeformableCornerItem with:
 * - Dark color scheme
 * - Glow effect on circle
 * - Gradient backgrounds
 * - Enhanced visual depth
 */
@Composable
fun DeformableCornerItemVariant(
    modifier: Modifier = Modifier.wrapContentSize().size(200.dp),
    circleRadius: Dp = 26.dp,
    rectPadding: Dp = 10.dp,
    cardColor: Color = VariantColors.CardDark,
    circleColor: Color = VariantColors.AccentOrange,
    glowColor: Color = VariantColors.GlowOrange,
    enableGlow: Boolean = true,
    glowRadius: Dp = 20.dp,
    topLeft: Dp = 28.dp,
    bottomLeft: Dp = 28.dp,
    bottomRight: Dp = 28.dp,
    contentCircle: @Composable BoxScope.() -> Unit = {},
    contentRectangle: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier) {
        // Card background with gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            cardColor,
                            cardColor.copy(alpha = 0.95f),
                        ),
                    ),
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
                .padding(2.dp),
        ) {
            contentRectangle()
        }

        // Glow effect behind circle
        if (enableGlow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(circleRadius * 2 + glowRadius * 2)
                    .offset(x = glowRadius, y = -(glowRadius / 10))
                    .blur(glowRadius)
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to glowColor.copy(alpha = 0.7f),
                                0.4f to glowColor.copy(alpha = 0.3f),
                                0.7f to glowColor.copy(alpha = 0.1f),
                                0.85f to Color.Transparent,
                                1.0f to Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
        }

        // The Circle with gradient
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(circleRadius * 2)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            circleColor,
                            circleColor.copy(alpha = 0.85f),
                        ),
                    ),
                )
                .drawBehind {
                    // Inner highlight for 3D effect
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.3f, size.height * 0.3f),
                            radius = size.minDimension * 0.4f,
                        ),
                    )
                },
        ) {
            contentCircle()
        }
    }
}

/**
 * Offset modifier extension for positioning glow
 */
private fun Modifier.offset(
    x: Dp,
    y: Dp,
): Modifier = this.then(
    Modifier.padding(start = if (x < 0.dp) -x else 0.dp, top = if (y < 0.dp) -y else 0.dp),
)

// ============================================================================
// CARD VARIANTS
// ============================================================================

/**
 * Calories card with bar chart - dark theme
 */
@Composable
fun CaloriesCardSample(
    modifier: Modifier = Modifier,
    caloriesValue: String = "450",
    cardTitle: String = "Calories Burned:",
    caloriesUnit: String = "kcal",
    chartData: List<BarData>,
    selectedIndex: Int = -1,
    barWidth: Dp = 28.dp,
    barCornerRadius: Dp = 8.dp,
    onBarClick: ((Int) -> Unit)? = null,
) {
    DeformableCornerItemVariant(
        modifier = modifier.height(200.dp),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = VariantColors.CardDark,
        circleColor = VariantColors.AccentOrange,
        glowColor = VariantColors.GlowOrange,
        topLeft = 28.dp,
        bottomLeft = 28.dp,
        bottomRight = 28.dp,
        contentCircle = {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(28.dp),
            )
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp, horizontal = 10.dp),
            ) {
                Text(
                    text = cardTitle,
                    color = VariantColors.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = caloriesValue,
                        color = VariantColors.TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = caloriesUnit,
                        color = VariantColors.TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                BasicBarChart(
                    selectedIndex = selectedIndex,
                    barWidth = barWidth,
                    barCornerRadius = barCornerRadius,
                    textColor = VariantColors.TextSecondary,
                    activeBarColor = VariantColors.ChartBarActive,
                    inactiveBarStripesBg = VariantColors.ChartBarInactive,
                    stripeColor = VariantColors.ChartStripe,
                    chartData = chartData,
                    onBarClick = onBarClick,
                )
            }
        },
    )
}

/**
 * WatchCardSample card with watch - dark theme
 */
@Composable
fun WatchCardSample(
    modifier: Modifier = Modifier,
    circleIcon: ImageVector = Icons.Default.HourglassTop,
    restColor: Color = VariantColors.AccentOrange,
    trackColor: Color = VariantColors.AccentOrange.copy(alpha = 0.2f),
) {
    DeformableCornerItemVariant(
        modifier = modifier.height(200.dp),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = VariantColors.CardDarkGray,
        circleColor = VariantColors.AccentOrange,
        glowColor = VariantColors.GlowOrange,
        topLeft = 28.dp,
        bottomLeft = 28.dp,
        bottomRight = 28.dp,
        contentCircle = {
            Icon(
                imageVector = circleIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(28.dp),
            )
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp, horizontal = 10.dp),
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
                            restColor = restColor,
                            trackColor = trackColor,
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

/**
 * Minutes card with wavy line chart - Variant dark theme
 */
@Composable
fun MinutesCardVariant(
    modifier: Modifier = Modifier,
    minutes: String = "127",
    cardTitle: String = "Minutes:",
    dataPoints: List<Float> = listOf(0.5f, 0.2f, 0.8f, 0.35f),
) {
    DeformableCornerItemVariant(
        modifier = modifier.height(200.dp),
        circleRadius = 26.dp,
        rectPadding = 8.dp,
        cardColor = VariantColors.CardDarkGray,
        circleColor = VariantColors.AccentOrange,
        glowColor = VariantColors.GlowOrange,
        topLeft = 28.dp,
        bottomLeft = 28.dp,
        bottomRight = 28.dp,
        contentCircle = {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(24.dp),
            )
        },
        contentRectangle = {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = cardTitle,
                    color = VariantColors.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = minutes,
                        color = VariantColors.TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "min",
                        color = VariantColors.TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Wavy line chart with gradient fill
                WavyLineChartVariant(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    lineColor = VariantColors.AccentOrange,
                    fillColor = VariantColors.AccentOrange.copy(alpha = 0.3f),
                    dataPoints = dataPoints,
                )
            }
        },
    )
}

/**
 * Wavy line chart with gradient fill
 */

@Composable
fun WavyLineChartVariant(
    dataPoints: List<Float> = listOf(0.5f, 0.2f, 0.8f, 0.35f),
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFFF9800),
    fillColor: Color = Color(0xFFFF9800).copy(alpha = 0.2f),
    strokeWidth: Dp = 2.5.dp,
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val path = generateSmoothPath(dataPoints, size)

        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = size.height,
            ),
            style = Fill,
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                // join = StrokeJoin.Round // Opzionale per angoli più morbidi
            ),
        )
    }
}

/**
 * Funzione magica che converte una lista di Float in una curva di Bézier smussata
 */
fun generateSmoothPath(
    data: List<Float>,
    size: Size,
): Path {
    val path = Path()
    if (data.isEmpty()) return path

    // Mapping delle coordinate:
    // X: distribuita equamente in base all'indice
    // Y: invertita (0 è in alto) e scalata. Assumiamo input 0.0..1.0, altrimenti normalizza prima.
    val stepX = size.width / (data.size - 1)

    // Funzione helper per ottenere le coordinate XY di un punto dato l'indice
    fun getPoint(index: Int): Offset {
        val value = data[index].coerceIn(0f, 1f) // Sicurezza
        val x = index * stepX
        val y = size.height * (1 - value) // 1 - value perché Y=0 è in alto
        return Offset(x, y)
    }

    // Punto iniziale
    val startPoint = getPoint(0)
    path.moveTo(startPoint.x, startPoint.y)

    // Calcolo Curve di Bézier cubiche tra i punti
    for (i in 0 until data.size - 1) {
        val p0 = getPoint(i)
        val p1 = getPoint(i + 1)

        // Logica per i punti di controllo (Control Points)
        // Usiamo una logica semplice: i punti di controllo sono a metà strada orizzontalmente
        // tra i due punti, mantenendo la stessa Y del punto di origine/destinazione.
        // Questo crea una curva a "S" morbida.

        val controlPoint1 = Offset(
            x = (p0.x + p1.x) / 2f,
            y = p0.y,
        )

        val controlPoint2 = Offset(
            x = (p0.x + p1.x) / 2f,
            y = p1.y,
        )

        path.cubicTo(
            controlPoint1.x,
            controlPoint1.y,
            controlPoint2.x,
            controlPoint2.y,
            p1.x,
            p1.y,
        )
    }

    return path
}

@Preview
@Composable
fun SampleCardsRowPreview() {
    Column {
        val weekData = listOf(
            BarData(label = "Mon", progress = 0.7f),
            BarData(label = "Tue", progress = 0.4f),
            BarData(label = "Wed", progress = 0.6f),
            BarData(label = "Thu", progress = 0f),
            BarData(label = "Fri", progress = 0.5f),
            BarData(label = "Sat", progress = 1.0f),
            BarData(label = "Sun", progress = 0.65f),
        )

        Box(
            modifier = Modifier
                .background(Color(0xFF0D0F14))
                .padding(16.dp),
        ) {
            CaloriesCardSample(
                modifier = Modifier.fillMaxWidth(),
                caloriesValue = "450",
                chartData = weekData,
                selectedIndex = 5,
            )
        }
        Box(
            modifier = Modifier
                .background(Color(0xFF0D0F14))
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WatchCardSample(
                    modifier = Modifier.weight(1f),
                )
                MinutesCardVariant(
                    modifier = Modifier.weight(1f),
                    minutes = "127",
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F14))
                .padding(16.dp),
        ) {
            MinutesCardVariant(
                dataPoints = listOf(0.5f, 0.8f, 0.4f, 0.9f, 0.2f, 0.6f, 0.5f),
                modifier = Modifier.fillMaxWidth(),
                minutes = "91",
            )
        }
    }
}
