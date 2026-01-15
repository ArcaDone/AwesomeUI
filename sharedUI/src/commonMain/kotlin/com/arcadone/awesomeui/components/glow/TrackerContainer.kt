package com.arcadone.awesomeui.components.glow

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TrackerContainer(
    fillPercentage: Float, // 0.0 to 1.0
    isCompleted: Boolean,
    isActive: Boolean,
    isGlobalComplete: Boolean,
    waveDelayMillis: Int,
    rangeStart: Int,
    rangeEnd: Int,
    style: GlowingTrackerStyle,
) {
    // Color base
    val baseColor = when {
        isCompleted -> style.colors.completedColor
        isActive -> style.colors.activeColor
        else -> style.colors.inactiveColor
    }

    // Color Label
    val labelColor = when {
        isCompleted || isActive -> style.colors.labelActiveColor
        else -> style.colors.labelInactiveColor
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Box(contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(style.blockSpacing)) {
                    repeat(style.visualBlocksPerContainer) { blockIndex ->
                        val stepSize = 1f / style.visualBlocksPerContainer
                        val blockStart = blockIndex * stepSize
                        val blockProgress = ((fillPercentage - blockStart) / stepSize).coerceIn(0f, 1f)

                        TrackerBlock(
                            color = baseColor,
                            progress = blockProgress,
                            isWaveActive = isGlobalComplete && style.enableWaveAnimation,
                            waveDelay = waveDelayMillis + (blockIndex * 50),
                            style = style,
                        )
                    }
                }

                // Scale Animation
                val scale by animateFloatAsState(
                    targetValue = if (isCompleted) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "CheckScale",
                )

                if (scale > 0f) {
                    Icon(
                        imageVector = style.completionIcon,
                        contentDescription = null,
                        tint = style.colors.checkIconColor,
                        modifier = Modifier
                            .scale(scale)
                            .size(24.dp),
                    )
                }
            }
        }
        if (style.showLabels) {
            Row(
                modifier = Modifier.width(style.blockSize.width * style.visualBlocksPerContainer + style.blockSpacing * (style.visualBlocksPerContainer - 1)),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = rangeStart.toString(),
                    style = style.labelTextStyle,
                    color = labelColor,
                )
                Text(
                    text = rangeEnd.toString(),
                    style = style.labelTextStyle,
                    color = style.colors.labelInactiveColor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TrackerContainerPreview() {
    TrackerContainer(
        fillPercentage = 0.5f,
        isCompleted = false,
        isActive = true,
        isGlobalComplete = false,
        waveDelayMillis = 0,
        rangeStart = 0,
        rangeEnd = 100,
        style = GlowingTrackerDefaults.style(),
    )
}
