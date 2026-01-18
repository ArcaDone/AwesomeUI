package com.arcadone.awesomeui.components.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import awesome_ui.sharedui.generated.resources.Res
import awesome_ui.sharedui.generated.resources.ad
import awesome_ui.sharedui.generated.resources.ic_dark_mode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

// ============================================================================
// PARTICLE TYPES
// ============================================================================

/**
 * Type of particle to render
 */
sealed class ParticleShape {
    /** 4-pointed star shape */
    data object Star4Point : ParticleShape()

    /** 6-pointed star shape */
    data object Star6Point : ParticleShape()

    /** Simple circle/dot */
    data object Circle : ParticleShape()

    /** Custom icon from ImageVector */
    data class Icon(val imageVector: ImageVector) : ParticleShape()

    /** Custom icon from DrawableResource */
    @OptIn(ExperimentalResourceApi::class)
    data class Drawable(val resource: DrawableResource) : ParticleShape()
}

/**
 * Direction pattern for particle emission
 */
enum class EmissionPattern {
    /** X pattern: NE, NW, SE, SW diagonals */
    DIAGONAL_X,

    /** All directions radially */
    RADIAL,

    /** Upward only (like fire embers) */
    UPWARD,

    /** Upward with slight spread */
    UPWARD_SPREAD,

    /** Downward (like rain/snow) */
    DOWNWARD,

    /** Horizontal spread */
    HORIZONTAL,
}

// ============================================================================
// PARTICLE DATA
// ============================================================================

/**
 * A single particle in the system
 */
data class Particle(
    val id: Int,
    val shape: ParticleShape,
    val color: Color,
    val baseSize: Float,
    val speed: Float, // 0-1, affects travel distance
    val direction: Float, // Angle in radians
    val rotationSpeed: Float, // Degrees per cycle
    val delay: Float, // 0-1 start delay
    val initialRotation: Float,
    val sizeVariation: Float, // 0-1, how much size changes during travel
    val gravity: Float, // Affects vertical movement over time
    val flutterAmount: Float = 0f, // How much the particle oscillates sideways
    val flutterSpeed: Float = 0f, // How fast the oscillation is
    val flutterPhase: Float = 0f, // Phase offset for oscillation
)

// ============================================================================
// STYLE CONFIGURATION
// ============================================================================

/**
 * Default particle colors
 */
object ParticleColors {
    val Gold = Color(0xFFFFD700)
    val Yellow = Color(0xFFFFC107)
    val Orange = Color(0xFFFF9800)
    val OrangeRed = Color(0xFFFF5722)
    val White = Color(0xFFFFFFFF)
    val Pink = Color(0xFFE91E63)
    val Purple = Color(0xFF9C27B0)
    val Cyan = Color(0xFF00BCD4)
    val Red = Color(0xFFF44336)

    val GoldPalette = listOf(Gold, Yellow, Orange, White)
    val FirePalette = listOf(OrangeRed, Orange, Yellow, Gold)
    val PinkPalette = listOf(Pink, Purple, White)
    val RainbowPalette = listOf(Red, Orange, Yellow, Cyan, Purple, Pink)
}

/**
 * Style configuration for particle burst effect
 */
data class ParticleBurstStyle(
    val particleCount: Int = 50,
    val shapes: List<ParticleShape> = listOf(ParticleShape.Star4Point),
    val colors: List<Color> = ParticleColors.GoldPalette,
    val minSize: Float = 8f,
    val maxSize: Float = 24f,
    val emissionPattern: EmissionPattern = EmissionPattern.DIAGONAL_X,
    val cycleDurationMs: Int = 3000,

    // Speed control - burst effect
    val minSpeed: Float = 0.2f,
    val maxSpeed: Float = 1f,
    val speedEasing: Float = 2f, // Higher = more slowdown over time (easeOut)

    // Fade control
    val fadeInDuration: Float = 0.05f, // Quick fade in
    val fadeOutStart: Float = 0.4f, // Start fading at 40%

    // Rotation
    val minRotationSpeed: Float = -360f,
    val maxRotationSpeed: Float = 360f,

    // Physics
    val gravity: Float = 0f, // Positive = particles fall, negative = rise
    val spreadAngle: Float = 0.4f, // Radians spread from main direction

    // Flutter/oscillation - particles wave left/right perpendicular to direction
    val flutterEnabled: Boolean = true,
    val minFlutterAmount: Float = 10f, // Min oscillation amplitude in pixels
    val maxFlutterAmount: Float = 40f, // Max oscillation amplitude in pixels
    val minFlutterSpeed: Float = 2f, // Min oscillation cycles during travel
    val maxFlutterSpeed: Float = 5f, // Max oscillation cycles during travel
)

// ============================================================================
// PARTICLE GENERATION
// ============================================================================

private fun generateParticles(
    count: Int,
    style: ParticleBurstStyle,
    seed: Long,
): List<Particle> {
    val random = Random(seed)

    val directions = when (style.emissionPattern) {
        EmissionPattern.DIAGONAL_X -> listOf(
            -PI.toFloat() / 4, // NE
            -3 * PI.toFloat() / 4, // NW
            PI.toFloat() / 4, // SE
            3 * PI.toFloat() / 4, // SW
        )
        EmissionPattern.RADIAL -> (0 until 8).map { it * PI.toFloat() / 4 }
        EmissionPattern.UPWARD -> listOf(-PI.toFloat() / 2) // Straight up
        EmissionPattern.UPWARD_SPREAD -> listOf(
            -PI.toFloat() / 2, // Up
            -PI.toFloat() / 2 - 0.3f, // Up-left
            -PI.toFloat() / 2 + 0.3f, // Up-right
        )
        EmissionPattern.DOWNWARD -> listOf(PI.toFloat() / 2)
        EmissionPattern.HORIZONTAL -> listOf(0f, PI.toFloat())
    }

    return (0 until count).map { id ->
        val baseDirection = directions[id % directions.size]
        val spreadOffset = (random.nextFloat() - 0.5f) * 2 * style.spreadAngle

        Particle(
            id = id,
            shape = style.shapes[random.nextInt(style.shapes.size)],
            color = style.colors[random.nextInt(style.colors.size)],
            baseSize = style.minSize + random.nextFloat() * (style.maxSize - style.minSize),
            speed = style.minSpeed + random.nextFloat() * (style.maxSpeed - style.minSpeed),
            direction = baseDirection + spreadOffset,
            rotationSpeed = style.minRotationSpeed + random.nextFloat() * (style.maxRotationSpeed - style.minRotationSpeed),
            delay = random.nextFloat(), // Full cycle stagger - continuous effect without gaps
            initialRotation = random.nextFloat() * 360f,
            sizeVariation = 0.2f + random.nextFloat() * 0.3f,
            gravity = style.gravity * (0.5f + random.nextFloat()),
            flutterAmount = if (style.flutterEnabled) {
                style.minFlutterAmount + random.nextFloat() * (style.maxFlutterAmount - style.minFlutterAmount)
            } else {
                0f
            },
            flutterSpeed = if (style.flutterEnabled) {
                style.minFlutterSpeed + random.nextFloat() * (style.maxFlutterSpeed - style.minFlutterSpeed)
            } else {
                0f
            },
            flutterPhase = random.nextFloat() * 2 * PI.toFloat(),
        )
    }
}

// ============================================================================
// PARTICLE BURST COMPOSABLE
// ============================================================================

/**
 * Advanced particle burst effect with customizable icons, colors, and physics.
 *
 * Features:
 * - Custom icons or built-in star shapes
 * - Customizable colors with defaults
 * - Burst effect: fast at start, slows down and fades
 * - Multiple emission patterns (diagonal X, radial, upward embers, etc.)
 *
 * @param modifier Modifier for the component
 * @param style Style configuration
 * @param seed Random seed for consistent particle generation
 * @param centerOffset Offset from center where particles originate
 * @param maxDistance Maximum travel distance as fraction of container size
 */
@Composable
fun ParticleBurst(
    modifier: Modifier = Modifier,
    style: ParticleBurstStyle = ParticleBurstStyle(),
    seed: Long = 42L,
    centerOffset: Offset = Offset.Zero,
    maxDistance: Float = 1.0f, // 1.0 = particles travel to screen edge and beyond
) {
    val particles = remember(seed, style.particleCount, style.shapes, style.colors) {
        generateParticles(style.particleCount, style, seed)
    }

    // Pre-create VectorPainters for all icon shapes (must be done in Composable scope)
    val iconPainters: Map<ImageVector, VectorPainter> = style.shapes
        .filterIsInstance<ParticleShape.Icon>()
        .map { it.imageVector }
        .distinct()
        .associateWith { rememberVectorPainter(it) }

    // Pre-create Painters for drawable shapes
    @OptIn(ExperimentalResourceApi::class)
    val drawablePainters = style.shapes
        .filterIsInstance<ParticleShape.Drawable>()
        .associate { it.resource to painterResource(it.resource) }

    val infiniteTransition = rememberInfiniteTransition(label = "ParticleBurstAnimation")

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

        particles.forEach { particle ->
            // Calculate progress with delay
            val rawProgress = ((cycleProgress - particle.delay + 1f) % 1f)

            if (rawProgress > 0f && rawProgress < 1f) {
                // Apply easing for slowdown effect (fast start, slow end)
                val easedProgress = 1f - (1f - rawProgress).pow(style.speedEasing)

                // Distance traveled
                val distance = easedProgress * maxTravelDistance * particle.speed

                // Apply gravity over time
                val gravityOffset = easedProgress * easedProgress * particle.gravity * 100f

                // Calculate flutter/oscillation perpendicular to direction
                val flutterOffset = if (particle.flutterAmount > 0f) {
                    sin(easedProgress * particle.flutterSpeed * 2 * PI.toFloat() + particle.flutterPhase) * particle.flutterAmount
                } else {
                    0f
                }

                // Perpendicular direction (90 degrees offset)
                val perpDirection = particle.direction + (PI / 2).toFloat()

                // Calculate position with flutter
                val particleX = centerX +
                    cos(particle.direction) * distance +
                    cos(perpDirection) * flutterOffset
                val particleY = centerY +
                    sin(particle.direction) * distance +
                    sin(perpDirection) * flutterOffset +
                    gravityOffset

                // Calculate alpha (quick fade in, gradual fade out)
                val alpha = when {
                    rawProgress < style.fadeInDuration -> rawProgress / style.fadeInDuration
                    rawProgress > style.fadeOutStart -> {
                        1f - (rawProgress - style.fadeOutStart) / (1f - style.fadeOutStart)
                    }
                    else -> 1f
                }

                // Calculate size (shrinks as it travels)
                val sizeMultiplier = 1f - easedProgress * particle.sizeVariation

                // Calculate rotation
                val rotation = particle.initialRotation + easedProgress * particle.rotationSpeed

                // Draw the particle
                drawParticle(
                    particle = particle,
                    center = Offset(particleX, particleY),
                    size = particle.baseSize * sizeMultiplier,
                    alpha = alpha.coerceIn(0f, 1f),
                    rotation = rotation,
                    iconPainters = iconPainters,
                    drawablePainters = drawablePainters,
                )
            }
        }
    }
}

/**
 * Draw a single particle
 */
@OptIn(ExperimentalResourceApi::class)
private fun DrawScope.drawParticle(
    particle: Particle,
    center: Offset,
    size: Float,
    alpha: Float,
    rotation: Float,
    iconPainters: Map<ImageVector, VectorPainter> = emptyMap(),
    drawablePainters: Map<DrawableResource, androidx.compose.ui.graphics.painter.Painter> = emptyMap(),
) {
    val color = particle.color.copy(alpha = alpha)

    when (val shape = particle.shape) {
        is ParticleShape.Star4Point -> {
            drawStar(center, size, color, rotation, points = 4)
        }
        is ParticleShape.Star6Point -> {
            drawStar(center, size, color, rotation, points = 6)
        }
        is ParticleShape.Circle -> {
            drawCircle(color = color, radius = size / 2, center = center)
        }
        is ParticleShape.Icon -> {
            val painter = iconPainters[shape.imageVector]
            if (painter != null) {
                rotate(degrees = rotation, pivot = center) {
                    translate(left = center.x - size / 2, top = center.y - size / 2) {
                        with(painter) {
                            draw(
                                size = Size(size, size),
                                alpha = alpha,
                                colorFilter = ColorFilter.tint(particle.color),
                            )
                        }
                    }
                }
            } else {
                // Fallback to star if painter not available
                drawStar(center, size, color, rotation, points = 4)
            }
        }
        is ParticleShape.Drawable -> {
            val painter = drawablePainters[shape.resource]
            if (painter != null) {
                rotate(degrees = rotation, pivot = center) {
                    translate(left = center.x - size / 2, top = center.y - size / 2) {
                        with(painter) {
                            draw(
                                size = Size(size, size),
                                alpha = alpha,
                                colorFilter = ColorFilter.tint(particle.color),
                            )
                        }
                    }
                }
            } else {
                // Fallback to star if painter not available
                drawStar(center, size, color, rotation, points = 4)
            }
        }
    }
}

/**
 * Draw an N-pointed star
 */
private fun DrawScope.drawStar(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float,
    points: Int,
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            val outerRadius = size / 2
            val innerRadius = size / (if (points == 4) 6 else 4)

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

// ============================================================================
// EMBER/SPARK EFFECT (Fire Lapilli)
// ============================================================================

/**
 * Style for ember/spark effect (fire lapilli rising upward)
 */
data class EmberStyle(
    val particleCount: Int = 30,
    val shapes: List<ParticleShape> = listOf(ParticleShape.Circle),
    val colors: List<Color> = ParticleColors.FirePalette,
    val minSize: Float = 4f,
    val maxSize: Float = 12f,
    val cycleDurationMs: Int = 4000,
    val riseSpeed: Float = 0.6f,
    val horizontalDrift: Float = 0.3f, // How much they drift sideways
    val flickerSpeed: Float = 0.5f, // Alpha flicker rate
    val minRotationSpeed: Float = -180f,
    val maxRotationSpeed: Float = 180f,
)

/**
 * Ember/spark effect - particles rising upward like fire embers from center.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun EmberEffect(
    modifier: Modifier = Modifier,
    style: EmberStyle = EmberStyle(),
    seed: Long = 42L,
    centerOffset: Offset = Offset.Zero,
) {
    val random = remember(seed) { Random(seed) }

    val particles = remember(seed, style.particleCount, style.shapes) {
        (0 until style.particleCount).map { id ->
            Particle(
                id = id,
                shape = style.shapes[random.nextInt(style.shapes.size)],
                color = style.colors[random.nextInt(style.colors.size)],
                baseSize = style.minSize + random.nextFloat() * (style.maxSize - style.minSize),
                speed = 0.3f + random.nextFloat() * 0.7f,
                direction = -PI.toFloat() / 2 + (random.nextFloat() - 0.5f) * 0.8f, // Upward with wider spread
                rotationSpeed = style.minRotationSpeed + random.nextFloat() * (style.maxRotationSpeed - style.minRotationSpeed),
                delay = random.nextFloat(),
                initialRotation = random.nextFloat() * 360f,
                sizeVariation = 0.3f,
                gravity = 0f,
            )
        }
    }

    // Pre-create VectorPainters for icon shapes
    val iconPainters: Map<ImageVector, VectorPainter> = style.shapes
        .filterIsInstance<ParticleShape.Icon>()
        .map { it.imageVector }
        .distinct()
        .associateWith { rememberVectorPainter(it) }

    // Pre-create Painters for drawable shapes
    val drawablePainters = style.shapes
        .filterIsInstance<ParticleShape.Drawable>()
        .associate { it.resource to painterResource(it.resource) }

    val infiniteTransition = rememberInfiniteTransition(label = "EmberAnimation")

    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "EmberCycle",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2 + centerOffset.x
        val centerY = size.height / 2 + centerOffset.y
        val maxTravelDistance = maxOf(size.width, size.height) * style.riseSpeed

        particles.forEach { particle ->
            // Each particle has its own cycle based on delay
            val particleProgress = (cycleProgress + particle.delay) % 1f

            // Start from center
            val distance = particleProgress * maxTravelDistance * particle.speed

            // Horizontal drift (sine wave)
            val drift = sin(particleProgress * PI * 3).toFloat() * 30f * style.horizontalDrift

            val x = centerX + cos(particle.direction) * distance + drift
            val y = centerY + sin(particle.direction) * distance

            // Flicker alpha
            val flicker = (sin(particleProgress * PI * 8 * style.flickerSpeed).toFloat() + 1f) / 2f
            val baseAlpha = 1f - particleProgress // Fade as they travel
            val alpha = baseAlpha * (0.5f + flicker * 0.5f)

            // Size decreases as they travel
            val currentSize = particle.baseSize * (1f - particleProgress * 0.5f)

            // Rotation
            val rotation = particle.initialRotation + particleProgress * particle.rotationSpeed

            if (alpha > 0.05f) {
                drawParticleEmber(
                    particle = particle,
                    center = Offset(x, y),
                    size = currentSize,
                    alpha = alpha.coerceIn(0f, 1f),
                    rotation = rotation,
                    iconPainters = iconPainters,
                    drawablePainters = drawablePainters,
                )
            }
        }
    }
}

/**
 * Draw a particle for EmberEffect
 */
@OptIn(ExperimentalResourceApi::class)
private fun DrawScope.drawParticleEmber(
    particle: Particle,
    center: Offset,
    size: Float,
    alpha: Float,
    rotation: Float,
    iconPainters: Map<ImageVector, VectorPainter>,
    drawablePainters: Map<DrawableResource, androidx.compose.ui.graphics.painter.Painter>,
) {
    val color = particle.color.copy(alpha = alpha)

    when (val shape = particle.shape) {
        is ParticleShape.Star4Point -> {
            drawStar(center, size, color, rotation, points = 4)
        }
        is ParticleShape.Star6Point -> {
            drawStar(center, size, color, rotation, points = 6)
        }
        is ParticleShape.Circle -> {
            drawCircle(color = color, radius = size / 2, center = center)
        }
        is ParticleShape.Icon -> {
            val painter = iconPainters[shape.imageVector]
            if (painter != null) {
                rotate(degrees = rotation, pivot = center) {
                    translate(left = center.x - size / 2, top = center.y - size / 2) {
                        with(painter) {
                            draw(
                                size = Size(size, size),
                                alpha = alpha,
                                colorFilter = ColorFilter.tint(particle.color),
                            )
                        }
                    }
                }
            } else {
                drawCircle(color = color, radius = size / 2, center = center)
            }
        }
        is ParticleShape.Drawable -> {
            val painter = drawablePainters[shape.resource]
            if (painter != null) {
                rotate(degrees = rotation, pivot = center) {
                    translate(left = center.x - size / 2, top = center.y - size / 2) {
                        with(painter) {
                            draw(
                                size = Size(size, size),
                                alpha = alpha,
                                colorFilter = ColorFilter.tint(particle.color),
                            )
                        }
                    }
                }
            } else {
                drawCircle(color = color, radius = size / 2, center = center)
            }
        }
    }
}

// ============================================================================
// COMBINED PREMIUM EFFECT
// ============================================================================

/**
 * Complete premium effect combining:
 * - Particle burst (stars/icons shooting from center)
 * - Ember effect (fire sparks rising)
 * - Flicker stars (intermittent visibility)
 * - Optional static starfield background
 */
@Composable
fun PremiumParticleEffect(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    burstStyle: ParticleBurstStyle = ParticleBurstStyle(),
    emberStyle: EmberStyle? = null,
    flickerStyle: FlickerStarsStyle? = null,
    showStarfieldBackground: Boolean = true,
    starfieldStyle: StarfieldStyle = StarfieldStyle(starCount = 30),
    centerContentSize: Dp = 120.dp,
    seed: Long = 42L,
    centerContent: @Composable (BoxScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: Static starfield background
        if (showStarfieldBackground) {
            StarfieldBackground(
                modifier = Modifier.fillMaxSize(),
                style = starfieldStyle.copy(backgroundColor = Color.Transparent),
                seed = seed,
            )
        }

        // Layer 2: Ember effect (if enabled)
        if (emberStyle != null) {
            EmberEffect(
                modifier = Modifier.fillMaxSize(),
                style = emberStyle,
                seed = seed + 500,
            )
        }

        // Layer 3: Flicker stars (if enabled)
        if (flickerStyle != null) {
            FlickerStarsEffect(
                modifier = Modifier.fillMaxSize(),
                style = flickerStyle,
                seed = seed + 750,
            )
        }

        // Layer 4: Particle burst from center
        ParticleBurst(
            modifier = Modifier.fillMaxSize(),
            style = burstStyle,
            seed = seed + 1000,
        )

        // Layer 5: Center content
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
// FLICKER STARS EFFECT
// ============================================================================

/**
 * Style for flicker stars effect
 */
data class FlickerStarsStyle(
    val particleCount: Int = 40,
    val shapes: List<ParticleShape> = listOf(ParticleShape.Star4Point),
    val colors: List<Color> = ParticleColors.GoldPalette,
    val minSize: Float = 8f,
    val maxSize: Float = 20f,
    val cycleDurationMs: Int = 4000,
    val minSpeed: Float = 0.3f,
    val maxSpeed: Float = 0.8f,
    // Flicker timing
    val minFlickerDuration: Float = 0.1f,
    val maxFlickerDuration: Float = 0.4f,
    val minFlickerInterval: Float = 0.1f,
    val maxFlickerInterval: Float = 0.3f,
)

private data class FlickerStar(
    val id: Int,
    val shape: ParticleShape,
    val color: Color,
    val size: Float,
    val direction: Float,
    val speed: Float,
    val delay: Float,
    val rotation: Float,
    val flickerOnDuration: Float,
    val flickerOffDuration: Float,
    val flickerPhase: Float,
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FlickerStarsEffect(
    modifier: Modifier = Modifier,
    style: FlickerStarsStyle = FlickerStarsStyle(),
    seed: Long = 42L,
    centerOffset: Offset = Offset.Zero,
    maxDistance: Float = 1.0f,
) {
    val random = remember(seed) { Random(seed) }

    val stars = remember(seed, style.particleCount) {
        (0 until style.particleCount).map { id ->
            FlickerStar(
                id = id,
                shape = style.shapes[random.nextInt(style.shapes.size)],
                color = style.colors[random.nextInt(style.colors.size)],
                size = style.minSize + random.nextFloat() * (style.maxSize - style.minSize),
                direction = random.nextFloat() * 2 * PI.toFloat(),
                speed = style.minSpeed + random.nextFloat() * (style.maxSpeed - style.minSpeed),
                delay = random.nextFloat(),
                rotation = random.nextFloat() * 360f,
                flickerOnDuration = style.minFlickerDuration + random.nextFloat() * (style.maxFlickerDuration - style.minFlickerDuration),
                flickerOffDuration = style.minFlickerInterval + random.nextFloat() * (style.maxFlickerInterval - style.minFlickerInterval),
                flickerPhase = random.nextFloat(),
            )
        }
    }

    val iconPainters: Map<ImageVector, VectorPainter> = style.shapes
        .filterIsInstance<ParticleShape.Icon>()
        .map { it.imageVector }
        .distinct()
        .associateWith { rememberVectorPainter(it) }

    val drawablePainters = style.shapes
        .filterIsInstance<ParticleShape.Drawable>()
        .associate { it.resource to painterResource(it.resource) }

    val infiniteTransition = rememberInfiniteTransition(label = "FlickerStarsAnimation")

    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "FlickerCycle",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2 + centerOffset.x
        val centerY = size.height / 2 + centerOffset.y
        val maxTravelDistance = minOf(size.width, size.height) * maxDistance

        stars.forEach { star ->
            val starProgress = (cycleProgress + star.delay) % 1f
            val distance = starProgress * maxTravelDistance * star.speed

            val flickerCycle = star.flickerOnDuration + star.flickerOffDuration
            val flickerTime = (starProgress + star.flickerPhase) % flickerCycle
            val isVisible = flickerTime < star.flickerOnDuration

            if (isVisible && starProgress < 0.95f) {
                val x = centerX + cos(star.direction) * distance
                val y = centerY + sin(star.direction) * distance
                val alpha = (1f - starProgress).coerceIn(0.3f, 1f)
                val color = star.color.copy(alpha = alpha)

                when (val shape = star.shape) {
                    is ParticleShape.Star4Point -> drawStar(Offset(x, y), star.size, color, star.rotation, 4)
                    is ParticleShape.Star6Point -> drawStar(Offset(x, y), star.size, color, star.rotation, 6)
                    is ParticleShape.Circle -> drawCircle(color, star.size / 2, Offset(x, y))
                    is ParticleShape.Icon -> {
                        iconPainters[shape.imageVector]?.let { painter ->
                            rotate(star.rotation, Offset(x, y)) {
                                translate(x - star.size / 2, y - star.size / 2) {
                                    with(painter) { draw(Size(star.size, star.size), alpha, ColorFilter.tint(star.color)) }
                                }
                            }
                        }
                    }
                    is ParticleShape.Drawable -> {
                        drawablePainters[shape.resource]?.let { painter ->
                            rotate(star.rotation, Offset(x, y)) {
                                translate(x - star.size / 2, y - star.size / 2) {
                                    with(painter) { draw(Size(star.size, star.size), alpha, ColorFilter.tint(star.color)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview
@Composable
private fun ParticleBurstPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black),
    ) {
        ParticleBurst(
            style = ParticleBurstStyle(
                particleCount = 60,
                colors = ParticleColors.GoldPalette,
            ),
        )
    }
}

@Preview
@Composable
private fun ParticleBurstWithIconsPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black),
    ) {
        ParticleBurst(
            style = ParticleBurstStyle(
                particleCount = 40,
                shapes = listOf(
                    ParticleShape.Star4Point,
                    ParticleShape.Star6Point,
                    ParticleShape.Circle,
                ),
                colors = ParticleColors.PinkPalette,
                emissionPattern = EmissionPattern.RADIAL,
            ),
        )
    }
}

@Preview
@Composable
private fun EmberEffectPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black),
    ) {
        EmberEffect(
            style = EmberStyle(
                particleCount = 50,
                colors = ParticleColors.FirePalette,
            ),
        )
    }
}

@Preview
@Composable
private fun PremiumParticleEffectPreview() {
    PremiumParticleEffect(
        Modifier
            .fillMaxWidth()
            .height(300.dp),
        burstStyle = ParticleBurstStyle(
            particleCount = 50,
            colors = ParticleColors.GoldPalette,
        ),
        emberStyle = EmberStyle(
            particleCount = 30,
        ),
        centerContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
            )
        },
    )
}

@Preview
@Composable
private fun FlickerStarsEffectPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black),
    ) {
        FlickerStarsEffect(
            style = FlickerStarsStyle(particleCount = 60, colors = ParticleColors.GoldPalette),
        )
    }
}

// ============================================================================
// PREMIUM STAR CARD
// ============================================================================

/**
 * Style for PremiumStarCard
 */
data class PremiumStarCardStyle(
    val backgroundColor: Color = Color.Black,
    val titleColor: Color = Color.White,
    val subtitleColor: Color = Color.Gray,
    val accentColor: Color = ParticleColors.Gold,
    val profileSize: Dp = 100.dp,
    val profileBorderWidth: Dp = 2.dp,
    val cornerRadius: Dp = 24.dp,
)

/**
 * Premium Star Card - A Telegram Premium style card with particle effects.
 * Features:
 * - Starfield background with twinkling stars
 * - Particle burst shooting from center
 * - Flicker stars with intermittent visibility
 * - Center profile area with optional content
 * - Title and subtitle text below profile
 */
@Composable
fun PremiumStarCard(
    modifier: Modifier = Modifier,
    title: String = "Premium",
    subtitle: String? = null,
    style: PremiumStarCardStyle = PremiumStarCardStyle(),
    burstStyle: ParticleBurstStyle = ParticleBurstStyle(
        particleCount = 80,
        shapes = listOf(ParticleShape.Star4Point),
        colors = ParticleColors.GoldPalette,
        emissionPattern = EmissionPattern.RADIAL,
        spreadAngle = 0.8f,
        cycleDurationMs = 3000,
    ),
    flickerStyle: FlickerStarsStyle? = FlickerStarsStyle(
        particleCount = 30,
        colors = ParticleColors.GoldPalette,
    ),
    starfieldStyle: StarfieldStyle = StarfieldStyle(
        starCount = 50,
        colors = ParticleColors.GoldPalette,
    ),
    effectCenterOffset: Offset = Offset.Zero, // Offset from center for particle effects
    seed: Long = 42L,
    profileContent: @Composable (BoxScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(style.cornerRadius))
            .background(style.backgroundColor),
    ) {
        // Layer 1: Starfield background
        StarfieldBackground(
            modifier = Modifier.fillMaxSize(),
            style = starfieldStyle.copy(backgroundColor = Color.Transparent),
            seed = seed,
        )

        // Layer 2: Flicker stars
        if (flickerStyle != null) {
            FlickerStarsEffect(
                modifier = Modifier.fillMaxSize(),
                style = flickerStyle,
                seed = seed + 500,
                centerOffset = effectCenterOffset,
            )
        }

        // Layer 3: Particle burst
        ParticleBurst(
            modifier = Modifier.fillMaxSize(),
            style = burstStyle,
            seed = seed + 1000,
            centerOffset = effectCenterOffset,
        )

        // Layer 4: Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Profile circle
            Box(
                modifier = Modifier
                    .size(style.profileSize)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                style.accentColor.copy(alpha = 0.3f),
                                style.backgroundColor,
                            ),
                        ),
                    )
                    .drawBehind {
                        drawCircle(
                            color = style.accentColor.copy(alpha = 0.5f),
                            radius = size.minDimension / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = style.profileBorderWidth.toPx(),
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (profileContent != null) {
                    profileContent()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = title,
                color = style.titleColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            // Subtitle
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = style.subtitleColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PremiumStarCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF0D0F14))
            .padding(16.dp),
    ) {
        PremiumStarCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            title = "New Gift Premium",
            subtitle = "Give someone access to exclusive features\n with Premium.",
            burstStyle = ParticleBurstStyle(
                particleCount = 200,
                shapes = listOf(ParticleShape.Star4Point, ParticleShape.Icon(imageVector = vectorResource(Res.drawable.ic_dark_mode))),
                colors = ParticleColors.GoldPalette,
                emissionPattern = EmissionPattern.DIAGONAL_X,
                cycleDurationMs = 2000,
                spreadAngle = 0.5f,
                minSize = 24f,
                maxSize = 48f,
                flutterEnabled = true,
                maxFlutterSpeed = 3f,
                maxFlutterAmount = 2f,

            ),
            flickerStyle = FlickerStarsStyle(
                particleCount = 40,
                colors = ParticleColors.GoldPalette,
            ),
            effectCenterOffset = Offset(0f, -80f), // Move effect center up behind profile
            profileContent = {
                // For PNG use Image + painterResource, not Icon + vectorResource
                androidx.compose.foundation.Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    painter = painterResource(Res.drawable.ad),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                )
            },
        )
    }
}

@Preview
@Composable
private fun PremiumParticleEffectPinkPreview() {
    PremiumParticleEffect(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        burstStyle = ParticleBurstStyle(
            particleCount = 200,
            shapes = listOf(ParticleShape.Star4Point, ParticleShape.Icon(imageVector = vectorResource(Res.drawable.ic_dark_mode))),
            colors = ParticleColors.GoldPalette,
            emissionPattern = EmissionPattern.DIAGONAL_X,
            cycleDurationMs = 2000,
            minSize = 24f,
            maxSize = 48f,
            flutterEnabled = true,
            maxFlutterSpeed = 3f,
            maxFlutterAmount = 2f,

        ),
        emberStyle = EmberStyle(
            particleCount = 50,
            shapes = listOf(ParticleShape.Star4Point, ParticleShape.Icon(imageVector = vectorResource(Res.drawable.ic_dark_mode))),
            colors = ParticleColors.FirePalette,
            minSize = 24f,
            maxSize = 48f,
        ),
        showStarfieldBackground = true,
        starfieldStyle = StarfieldStyle(
            starCount = 40,
            colors = listOf(Color.White, ParticleColors.Pink),
        ),
        centerContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A2E)),
            )
        },
    )
}
