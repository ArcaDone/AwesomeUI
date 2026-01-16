package com.arcadone.awesomeui.components.glow

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- ENUMS & DATA CLASSES ---

enum class GlowMode {
    GLOBAL,
    INCREMENTAL,
}

data class GlowingTrackerState(val currentValue: Float, val maxValuePerContainer: Float, val containerCount: Int) {
    val totalMax: Float get() = maxValuePerContainer * containerCount
    val isAllComplete: Boolean get() = currentValue >= totalMax
}

data class GlowingTrackerColors(
    val activeColor: Color,
    val inactiveColor: Color,
    val completedColor: Color,
    val backgroundColor: Color,
    val labelActiveColor: Color,
    val labelInactiveColor: Color,
    val borderColor: Color,
    val checkIconColor: Color,
)

data class GlowingTrackerStyle(
    val containerShape: RoundedCornerShape,
    val blockSize: DpSize,
    val visualBlocksPerContainer: Int,
    val colors: GlowingTrackerColors,
    val spacing: Dp,
    val blockSpacing: Dp,
    val labelTextStyle: TextStyle,
    val showLabels: Boolean,
    val glowMode: GlowMode,
    val completionIcon: ImageVector,
    val enableCheckAnimation: Boolean,
    val enableWaveAnimation: Boolean,
    val glowBlurRadius: Dp,
    val ambientGlowSize: DpSize,
)

// --- DEFAULTS OBJECT ---

object GlowingTrackerDefaults {
    val NeonBlue = Color(0xFF29B6F6)
    val NeonGreen = Color(0xFF00E676)
    val InactiveRed = Color(0xFF3E1C1C)
    val DarkBackground = Color(0xFF050505)

    @Composable
    fun colors(
        activeColor: Color = NeonGreen,
        inactiveColor: Color = InactiveRed,
        completedColor: Color = NeonBlue,
        backgroundColor: Color = DarkBackground,
        labelActiveColor: Color = NeonGreen,
        labelInactiveColor: Color = Color.Gray,
        borderColor: Color = Color.White.copy(alpha = 0.2f),
        checkIconColor: Color = Color.White,
    ) = GlowingTrackerColors(
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        completedColor = completedColor,
        backgroundColor = backgroundColor,
        labelActiveColor = labelActiveColor,
        labelInactiveColor = labelInactiveColor,
        borderColor = borderColor,
        checkIconColor = checkIconColor,
    )

    @Composable
    fun style(
        containerShape: RoundedCornerShape = RoundedCornerShape(6.dp),
        blockSize: DpSize = DpSize(24.dp, 40.dp),
        visualBlocksPerContainer: Int = 4,
        colors: GlowingTrackerColors = colors(),
        spacing: Dp = 16.dp,
        blockSpacing: Dp = 4.dp,
        showLabels: Boolean = true,
        labelTextStyle: TextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
        glowMode: GlowMode = GlowMode.GLOBAL,
        completionIcon: ImageVector = Icons.Default.Check,
        enableCheckAnimation: Boolean = true,
        enableWaveAnimation: Boolean = true,
        glowBlurRadius: Dp = 20.dp,
        ambientGlowSize: DpSize = DpSize(120.dp, 70.dp),
    ) = GlowingTrackerStyle(
        containerShape = containerShape,
        blockSize = blockSize,
        visualBlocksPerContainer = visualBlocksPerContainer,
        colors = colors,
        spacing = spacing,
        blockSpacing = blockSpacing,
        showLabels = showLabels,
        labelTextStyle = labelTextStyle,
        glowMode = glowMode,
        completionIcon = completionIcon,
        enableCheckAnimation = enableCheckAnimation,
        enableWaveAnimation = enableWaveAnimation,
        glowBlurRadius = glowBlurRadius,
        ambientGlowSize = ambientGlowSize,
    )
}
