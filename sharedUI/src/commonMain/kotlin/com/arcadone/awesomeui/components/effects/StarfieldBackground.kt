package com.arcadone.awesomeui.components.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Color palette for starfield effect
 */
object StarfieldColors {
    val Background = Color(0xFF000000)
    val StarGold = Color(0xFFFFD700)
    val StarYellow = Color(0xFFFFC107)
    val StarOrange = Color(0xFFFF9800)
    val StarWhite = Color(0xFFFFFFFF)
    val StarPale = Color(0xFFFFF8E1)
}

/**
 * Data class representing a single star
 */
data class Star(
    val x: Float, // 0-1 normalized position
    val y: Float, // 0-1 normalized position
    val size: Float, // Size in dp
    val color: Color,
    val rotationSpeed: Float, // Degrees per second
    val twinkleSpeed: Float, // Alpha animation duration in ms
    val twinklePhase: Float, // 0-1 phase offset for staggered animation
    val initialRotation: Float, // Initial rotation angle
)

/**
 * Style configuration for StarfieldBackground
 */
data class StarfieldStyle(
    val backgroundColor: Color = StarfieldColors.Background,
    val starCount: Int = 60,
    val minStarSize: Float = 4f,
    val maxStarSize: Float = 16f,
    val colors: List<Color> = listOf(
        StarfieldColors.StarGold,
        StarfieldColors.StarYellow,
        StarfieldColors.StarOrange,
        StarfieldColors.StarWhite,
        StarfieldColors.StarPale,
    ),
    val twinkleMinAlpha: Float = 0.3f,
    val twinkleMaxAlpha: Float = 1f,
    val twinkleDurationMin: Int = 1000,
    val twinkleDurationMax: Int = 3000,
    val enableRotation: Boolean = true,
    val rotationSpeedMin: Float = 5f,
    val rotationSpeedMax: Float = 30f,
)

/**
 * Generate a list of random stars
 */
private fun generateStars(
    count: Int,
    style: StarfieldStyle,
    seed: Long = Clock.System.now().toEpochMilliseconds(),
): List<Star> {
    val random = Random(seed)
    return (0 until count).map {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            size = style.minStarSize + random.nextFloat() * (style.maxStarSize - style.minStarSize),
            color = style.colors[random.nextInt(style.colors.size)],
            rotationSpeed = if (style.enableRotation) {
                (style.rotationSpeedMin + random.nextFloat() * (style.rotationSpeedMax - style.rotationSpeedMin)) *
                    if (random.nextBoolean()) 1f else -1f
            } else {
                0f
            },
            twinkleSpeed = (style.twinkleDurationMin + random.nextInt(style.twinkleDurationMax - style.twinkleDurationMin)).toFloat(),
            twinklePhase = random.nextFloat(),
            initialRotation = random.nextFloat() * 360f,
        )
    }
}

/**
 * Premium starfield background with twinkling, rotating stars.
 * Inspired by Telegram Premium effect.
 *
 * @param modifier Modifier for the component
 * @param style Style configuration
 * @param seed Random seed for star positions (use same seed for consistent layout)
 */
@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    style: StarfieldStyle = StarfieldStyle(),
    seed: Long = 42L,
) {
    val stars = remember(seed, style.starCount) {
        generateStars(style.starCount, style, seed)
    }

    // Global animation time
    val infiniteTransition = rememberInfiniteTransition(label = "StarfieldAnimation")

    // Master time that drives all animations (0 to 10 seconds loop)
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MasterTime",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor),
    ) {
        stars.forEach { star ->
            // Calculate twinkle alpha based on time and phase
            val twinklePeriod = star.twinkleSpeed / 1000f // Convert to seconds
            val twinkleTime = (animationTime + star.twinklePhase * twinklePeriod) % twinklePeriod
            val twinkleProgress = (sin(twinkleTime / twinklePeriod * 2 * PI).toFloat() + 1f) / 2f
            val alpha = style.twinkleMinAlpha + twinkleProgress * (style.twinkleMaxAlpha - style.twinkleMinAlpha)

            // Calculate rotation
            val rotation = star.initialRotation + animationTime * star.rotationSpeed * 36f // 36 degrees per time unit

            // Draw star
            val starX = star.x * size.width
            val starY = star.y * size.height

            drawStar(
                center = Offset(starX, starY),
                size = star.size,
                color = star.color.copy(alpha = alpha),
                rotation = rotation,
            )
        }
    }
}

/**
 * Draw a 4-pointed star shape
 */
private fun DrawScope.drawStar(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float,
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            // 4-pointed star with elongated points
            val outerRadius = size / 2
            val innerRadius = size / 6

            // Create star path with alternating outer and inner points
            val points = 4
            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (i * 360f / (points * 2) - 90) * (PI / 180).toFloat()
                val x = center.x + cos(angle) * radius
                val y = center.y + sin(angle) * radius

                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}

/**
 * Starfield with gradient overlay (useful for blending with content)
 */
@Composable
fun StarfieldBackgroundWithGradient(
    modifier: Modifier = Modifier,
    style: StarfieldStyle = StarfieldStyle(),
    seed: Long = 42L,
    gradientColors: List<Color> = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.5f),
        Color.Black,
    ),
) {
    Box(modifier = modifier) {
        StarfieldBackground(
            modifier = Modifier.fillMaxSize(),
            style = style,
            seed = seed,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors),
                ),
        )
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun StarfieldBackgroundPreview() {
    StarfieldBackground(
        modifier = Modifier.fillMaxSize(),
        style = StarfieldStyle(starCount = 50),
        seed = 123L,
    )
}

@Preview
@Composable
private fun StarfieldBackgroundDensePreview() {
    StarfieldBackground(
        modifier = Modifier.fillMaxSize(),
        style = StarfieldStyle(
            starCount = 100,
            minStarSize = 3f,
            maxStarSize = 20f,
        ),
        seed = 456L,
    )
}

@Preview
@Composable
private fun StarfieldBackgroundWithGradientPreview() {
    StarfieldBackgroundWithGradient(
        modifier = Modifier.fillMaxSize(),
        seed = 789L,
    )
}
