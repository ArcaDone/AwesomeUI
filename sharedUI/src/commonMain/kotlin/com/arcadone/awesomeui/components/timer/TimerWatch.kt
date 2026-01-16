package com.arcadone.awesomeui.components.timer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcadone.awesomeui.components.utils.clickableNoOverlay
import com.arcadone.awesomeui.components.utils.formatTime
import com.arcadone.shared.timer.TimerState
import kotlin.math.abs

// --- Default Constants ---
val AmberColor = Color(0xFFF59E0B)
val GreenColor = Color(0xFF10B981)
val CardDarkColor = Color(0xFF252e42)
val SlateGray = Color(0xFF64748b)

data class CircularTimerStyle(
    val size: Dp,
    val strokeWidth: Dp,
    val indicatorStrokeWidth: Dp,
    val workColor: Color,
    val restColor: Color,
    val overtimeColor: Color,
    val trackColor: Color,
    val timeTextStyle: TextStyle,
    val labelTextStyle: TextStyle,
    val glowColorAlpha: Float,
)

object CircularTimerDefaults {
    @Composable
    fun style(
        size: Dp = 160.dp,
        strokeWidth: Dp = 12.dp,
        indicatorStrokeWidth: Dp = 14.dp,
        workColor: Color = GreenColor,
        restColor: Color = AmberColor,
        overtimeColor: Color = Color.Red,
        trackColor: Color = CardDarkColor,
        timeTextStyle: TextStyle = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
        ),
        labelTextStyle: TextStyle = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = SlateGray,
        ),
        glowColorAlpha: Float = 0.05f,
    ) = CircularTimerStyle(
        size = size,
        strokeWidth = strokeWidth,
        indicatorStrokeWidth = indicatorStrokeWidth,
        workColor = workColor,
        restColor = restColor,
        overtimeColor = overtimeColor,
        trackColor = trackColor,
        timeTextStyle = timeTextStyle,
        labelTextStyle = labelTextStyle,
        glowColorAlpha = glowColorAlpha,
    )
}

@Composable
fun TimerWatch(
    timerState: TimerState,
    isOvertime: Boolean,
    modifier: Modifier = Modifier,
    style: CircularTimerStyle = CircularTimerDefaults.style(),
    topIcon: ImageVector? = Icons.Default.Timer,
    showHeartbeat: Boolean = true,
    onPauseToggle: () -> Unit = {},
) {
    val displayTime = abs(timerState.timeRemaining)

    val progress = if (timerState.totalTime > 0) {
        if (isOvertime) 1f else timerState.timeRemaining.toFloat() / timerState.totalTime.toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "ProgressAnimation",
    )

    val activeColor = when {
        isOvertime -> style.overtimeColor
        timerState.isRest -> style.restColor
        else -> style.workColor
    }

    val isHeartbeatActive = showHeartbeat && !isOvertime && timerState.timeRemaining <= 5 && timerState.timeRemaining > 0

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(style.size * 1.2f),
    ) {
        // 1. Heartbeat Background Layer
        if (isHeartbeatActive) {
            val infiniteTransition = rememberInfiniteTransition(label = "HeartbeatTransition")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "HeartbeatScale",
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "HeartbeatAlpha",
            )

            Box(
                modifier = Modifier
                    .size(style.size)
                    .scale(scale)
                    .background(activeColor.copy(alpha = alpha), CircleShape),
            )
        }

        // 2. Main Watch Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(style.size),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickableNoOverlay { onPauseToggle() },
            ) {
                val strokeWidthPx = style.strokeWidth.toPx()
                val indicatorWidthPx = style.indicatorStrokeWidth.toPx()
                val maxStroke = maxOf(strokeWidthPx, indicatorWidthPx)
                val radius = (size.minDimension - maxStroke) / 2

                // Background circle (Track)
                drawCircle(
                    color = style.trackColor,
                    radius = radius,
                    style = Stroke(width = strokeWidthPx),
                )

                // Outer Glow (Static)
                drawCircle(
                    color = activeColor.copy(alpha = style.glowColorAlpha),
                    radius = radius,
                    style = Stroke(width = strokeWidthPx * 2.5f),
                )

                // Progress arc
                val sweepAngle = 360f * animatedProgress
                val progressRadius = radius + 12f - (indicatorWidthPx / 2)
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = indicatorWidthPx, cap = StrokeCap.Round),
                    topLeft = Offset(
                        x = (size.width - progressRadius * 2) / 2,
                        y = (size.height - progressRadius * 2) / 2,
                    ),
                    size = Size(progressRadius * 2, progressRadius * 2),
                )
            }

            // 3. Content (Text & Icon)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                topIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Text(
                    text = displayTime.formatTime(),
                    style = style.timeTextStyle.copy(
                        color = if (isOvertime) style.overtimeColor else style.timeTextStyle.color,
                    ),
                )

                Text(
                    text = when {
                        isOvertime -> "OVERTIME"
                        timerState.isRest -> "REST"
                        else -> "WORK"
                    },
                    style = style.labelTextStyle.copy(
                        color = if (isOvertime) style.overtimeColor else style.labelTextStyle.color,
                    ),
                )
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 1200)
@Composable
fun TimerWatchLogicGallery() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Timer Logic States",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
            )

            StateShowcase(title = "1. Work State (Standard)") {
                TimerWatch(
                    timerState = TimerState(timeRemaining = 45, totalTime = 60, isRest = false),
                    isOvertime = false,
                    showHeartbeat = false,
                )
            }

            StateShowcase(title = "2. Rest State") {
                TimerWatch(
                    timerState = TimerState(timeRemaining = 15, totalTime = 30, isRest = true),
                    isOvertime = false,
                    showHeartbeat = false,
                )
            }

            StateShowcase(title = "3. Heartbeat (< 5 sec)") {
                TimerWatch(
                    timerState = TimerState(timeRemaining = 4, totalTime = 60, isRest = false),
                    isOvertime = false,
                    showHeartbeat = true,
                )
            }

            StateShowcase(title = "4. Overtime State") {
                TimerWatch(
                    timerState = TimerState(timeRemaining = -15, totalTime = 60, isRest = false),
                    isOvertime = true,
                    showHeartbeat = false,
                )
            }
        }
    }
}

@Composable
private fun StateShowcase(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}
