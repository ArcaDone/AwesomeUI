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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Direction for shooting stars (diagonal X pattern)
 */
enum class StarDirection(val angleRadians: Float) {
    NORTH_EAST(-PI.toFloat() / 4), // -45°
    NORTH_WEST(-3 * PI.toFloat() / 4), // -135°
    SOUTH_EAST(PI.toFloat() / 4), // 45°
    SOUTH_WEST(3 * PI.toFloat() / 4), // 135°
}

/**
 * A single shooting star particle
 */
data class ShootingStar(
    val id: Int,
    val direction: StarDirection,
    val speed: Float, // 0.3 to 1.0 - affects how far it travels
    val size: Float, // Star size in dp
    val rotationSpeed: Float, // Degrees per animation cycle
    val delay: Float, // 0-1 delay before appearing
    val color: Color,
    val spreadAngle: Float, // Random offset from main direction
)

/**
 * Style configuration for StarBurst effect
 */
data class StarBurstStyle(
    val starCount: Int = 40,
    val minStarSize: Float = 6f,
    val maxStarSize: Float = 18f,
    val minSpeed: Float = 0.3f,
    val maxSpeed: Float = 1f,
    val spreadAngleRange: Float = 0.3f, // Radians spread from main direction
    val colors: List<Color> = listOf(
        StarfieldColors.StarGold,
        StarfieldColors.StarYellow,
        StarfieldColors.StarOrange,
        StarfieldColors.StarWhite,
    ),
    val cycleDurationMs: Int = 3000,
    val fadeOutStart: Float = 0.6f, // Start fading at 60% of travel
)

/**
 * Generate shooting stars for the burst effect
 */
private fun generateShootingStars(
    count: Int,
    style: StarBurstStyle,
    seed: Long,
): List<ShootingStar> {
    val random = Random(seed)
    val directions = StarDirection.entries

    return (0 until count).map { id ->
        ShootingStar(
            id = id,
            direction = directions[id % directions.size], // Distribute evenly across directions
            speed = style.minSpeed + random.nextFloat() * (style.maxSpeed - style.minSpeed),
            size = style.minStarSize + random.nextFloat() * (style.maxStarSize - style.minStarSize),
            rotationSpeed = (random.nextFloat() * 720f - 360f), // -360 to +360 degrees per cycle
            delay = random.nextFloat() * 0.5f, // 0 to 0.5 delay
            color = style.colors[random.nextInt(style.colors.size)],
            spreadAngle = (random.nextFloat() - 0.5f) * 2 * style.spreadAngleRange,
        )
    }
}

/**
 * Star burst particle effect - shoots stars from center in X pattern.
 * Perfect for placing behind a profile image or central element.
 *
 * @param modifier Modifier for the component
 * @param style Style configuration
 * @param seed Random seed for consistent star generation
 * @param centerOffset Offset from center where stars originate
 * @param maxDistance Maximum distance stars travel (as fraction of container size)
 */
@Composable
fun StarBurstEffect(
    modifier: Modifier = Modifier,
    style: StarBurstStyle = StarBurstStyle(),
    seed: Long = 42L,
    centerOffset: Offset = Offset.Zero,
    maxDistance: Float = 0.5f,
) {
    val stars = remember(seed, style.starCount) {
        generateShootingStars(style.starCount, style, seed)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "StarBurstAnimation")

    // Master cycle time (0 to 1)
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "CycleProgress",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2 + centerOffset.x
        val centerY = size.height / 2 + centerOffset.y
        val maxTravelDistance = minOf(size.width, size.height) * maxDistance

        stars.forEach { star ->
            // Calculate this star's progress (accounting for delay and speed)
            val adjustedProgress = ((cycleProgress - star.delay + 1f) % 1f) * star.speed

            // Only draw if visible
            if (adjustedProgress > 0f && adjustedProgress < 1f) {
                // Position along the direction
                val angle = star.direction.angleRadians + star.spreadAngle
                val distance = adjustedProgress * maxTravelDistance

                val starX = centerX + cos(angle) * distance
                val starY = centerY + sin(angle) * distance

                // Calculate alpha (fade in at start, fade out at end)
                val alpha = when {
                    adjustedProgress < 0.1f -> adjustedProgress * 10f // Fade in
                    adjustedProgress > style.fadeOutStart -> {
                        1f - (adjustedProgress - style.fadeOutStart) / (1f - style.fadeOutStart)
                    }
                    else -> 1f
                }

                // Calculate rotation
                val rotation = adjustedProgress * star.rotationSpeed

                // Scale down as it travels
                val scale = 1f - adjustedProgress * 0.3f

                drawStar4Point(
                    center = Offset(starX, starY),
                    size = star.size * scale,
                    color = star.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    rotation = rotation,
                )
            }
        }
    }
}

/**
 * Draw a 4-pointed star
 */
private fun DrawScope.drawStar4Point(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float,
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            val outerRadius = size / 2
            val innerRadius = size / 6

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
 * Complete Telegram Premium-style effect combining:
 * - Static twinkling starfield background
 * - Star burst shooting from center
 * - Optional center content (e.g., profile image)
 */
@Composable
fun PremiumStarEffect(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    starfieldStyle: StarfieldStyle = StarfieldStyle(starCount = 40),
    starBurstStyle: StarBurstStyle = StarBurstStyle(starCount = 50),
    centerContentSize: Dp = 120.dp,
    showBackground: Boolean = true,
    seed: Long = 42L,
    centerContent: @Composable (BoxScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: Static twinkling starfield
        if (showBackground) {
            StarfieldBackground(
                modifier = Modifier.fillMaxSize(),
                style = starfieldStyle.copy(backgroundColor = Color.Transparent),
                seed = seed,
            )
        }

        // Layer 2: Star burst from center
        StarBurstEffect(
            modifier = Modifier.fillMaxSize(),
            style = starBurstStyle,
            seed = seed + 1000,
        )

        // Layer 3: Center content (profile image, etc.)
        if (centerContent != null) {
            Box(
                modifier = Modifier.size(centerContentSize),
                contentAlignment = Alignment.Center,
                content = centerContent,
            )
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun StarBurstEffectPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        StarBurstEffect(
            modifier = Modifier.fillMaxSize(),
            style = StarBurstStyle(starCount = 60),
        )
    }
}

@Preview
@Composable
private fun PremiumStarEffectPreview() {
    PremiumStarEffect(
        modifier = Modifier.fillMaxSize(),
        centerContentSize = 100.dp,
        centerContent = {
            // Placeholder for profile image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.DarkGray),
            )
        },
    )
}

@Preview
@Composable
private fun PremiumStarEffectDensePreview() {
    PremiumStarEffect(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        starfieldStyle = StarfieldStyle(starCount = 60),
        starBurstStyle = StarBurstStyle(
            starCount = 80,
            cycleDurationMs = 1500,
            minStarSize = 20f,
            maxStarSize = 60f,
        ),
        centerContentSize = 120.dp,
        centerContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A2E)),
            )
        },
    )
}
