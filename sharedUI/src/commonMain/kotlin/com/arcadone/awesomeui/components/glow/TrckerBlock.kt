package com.arcadone.awesomeui.components.glow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TrackerBlock(
    color: Color,
    progress: Float, // 0.0 - 1.0
    isWaveActive: Boolean,
    waveDelay: Int,
    style: GlowingTrackerStyle,
) {
    var waveOffset by remember { mutableStateOf(0f) }
    if (isWaveActive) {
        LaunchedEffect(Unit) {
            delay(waveDelay.toLong())
            animate(
                initialValue = 0f,
                targetValue = -10f,
                animationSpec = tween(150, easing = FastOutSlowInEasing),
            ) { value, _ -> waveOffset = value }
            animate(
                initialValue = -10f,
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
            ) { value, _ -> waveOffset = value }
        }
    }

    val isActiveBlock = progress > 0f

    Box(
        modifier = Modifier
            .graphicsLayer { translationY = waveOffset }
            .size(style.blockSize)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(style.containerShape)
                .background(color.copy(alpha = 0.2f)),
        )

        if (isActiveBlock) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .align(Alignment.BottomCenter)
                    .clip(style.containerShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.9f), color),
                        ),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.4f),
                            shape = style.containerShape,
                        ),
                ) {
                    if (progress >= 1f) {
                        Icon(
                            imageVector = style.completionIcon,
                            contentDescription = null,
                            tint = style.colors.checkIconColor,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TrackerBlockPreview() {
    var animatedValue by remember { mutableStateOf(10f) }

    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0..400) {
                animatedValue = i / 10f
                delay(1000)
            }
            delay(1000)
            animatedValue = 0f
            delay(500)
        }
    }

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Green.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TrackerBlock(
            color = Color.Green,
            progress = 1f,
            isWaveActive = true,
            waveDelay = 0,
            style = GlowingTrackerDefaults.style(),
        )
        TrackerBlock(
            color = Color.Green,
            progress = 0.8f,
            isWaveActive = true,
            waveDelay = 0,
            style = GlowingTrackerDefaults.style(),
        )
        TrackerBlock(
            color = Color.Green,
            progress = 0.6f,
            isWaveActive = true,
            waveDelay = 0,
            style = GlowingTrackerDefaults.style(),
        )
        TrackerBlock(
            color = Color.Green,
            progress = animatedValue,
            isWaveActive = true,
            waveDelay = 0,
            style = GlowingTrackerDefaults.style(),
        )
    }
}
