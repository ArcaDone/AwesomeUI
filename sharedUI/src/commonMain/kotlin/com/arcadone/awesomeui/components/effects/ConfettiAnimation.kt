package com.arcadone.awesomeui.components.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

val goldColor = Color(0xFFEAB308) // Gold/all-out
val orangeColorAlt = Color(0xFFF97316) // Orange variant

/**
 * Confetti celebration animation for PR cards
 */
@Composable
fun ConfettiAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return

    val particles = remember {
        List(40) { createConfettiParticle() }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawConfettiParticle(particle, progress)
            }
        }
    }
}

private data class ConfettiParticle(val startX: Float, val startY: Float, val velocityX: Float, val velocityY: Float, val color: Color, val size: Float, val rotation: Float)

private fun createConfettiParticle(): ConfettiParticle {
    val colors = listOf(
        goldColor,
        orangeColorAlt,
        Color(0xFFEC4899),
        Color(0xFF10B981),
        Color(0xFF6B5CE7),
    )

    return ConfettiParticle(
        startX = Random.nextFloat(),
        startY = -0.1f,
        velocityX = (Random.nextFloat() - 0.5f) * 0.3f,
        velocityY = Random.nextFloat() * 0.5f + 0.3f,
        color = colors.random(),
        size = Random.nextFloat() * 8f + 4f,
        rotation = Random.nextFloat() * 360f,
    )
}

private fun DrawScope.drawConfettiParticle(
    particle: ConfettiParticle,
    progress: Float,
) {
    val x = size.width * (particle.startX + particle.velocityX * progress)
    val y = size.height * (particle.startY + particle.velocityY * progress)

    // Fade out as it falls
    val alpha = (1f - progress).coerceIn(0f, 1f)

    // Simple circle confetti (more performant than rotated rectangles)
    drawCircle(
        color = particle.color.copy(alpha = alpha),
        radius = particle.size / 2,
        center = Offset(x, y),
    )
}

@Preview
@Composable
private fun ConfettiAnimationPreview() {
    ConfettiAnimation(isVisible = true)
}
