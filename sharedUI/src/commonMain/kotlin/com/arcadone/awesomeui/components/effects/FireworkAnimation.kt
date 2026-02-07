package com.arcadone.awesomeui.components.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FireworkAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier.size(80.dp),
    durationMillis: Int = 1500,
    onAnimationEnd: () -> Unit = {},
) {
    if (!isVisible) return

    val particles = remember(isVisible) {
        List(60) { createFireworkParticle() }
    }

    val animatable = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearOutSlowInEasing),
            )
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            drawFireworkParticle(particle, animatable.value)
        }
    }
}

private data class FireworkParticle(val angle: Double, val speed: Float, val color: Color, val size: Float, val acceleration: Float = 0.8f)

private fun createFireworkParticle(): FireworkParticle {
    val colors = listOf(
        Color(0xFFEAB308), // Gold
        Color(0xFFF97316), // Orange
        Color(0xFFEC4899), // Pink
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Green
    )

    return FireworkParticle(
        angle = Random.nextDouble(0.0, 2.0 * PI),
        speed = Random.nextFloat() * 0.4f + 0.2f,
        color = colors.random(),
        size = Random.nextFloat() * 6f + 4f,
    )
}

private fun DrawScope.drawFireworkParticle(
    particle: FireworkParticle,
    progress: Float,
) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    val distance = progress * particle.speed * size.minDimension

    val x = centerX + (cos(particle.angle) * distance).toFloat()
    val y = centerY + (sin(particle.angle) * distance).toFloat() + (progress * progress * 100f)

    val alpha = if (progress < 0.6f) {
        1f
    } else {
        (1f - (progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
    }

    drawCircle(
        color = particle.color.copy(alpha = alpha),
        radius = particle.size,
        center = Offset(x, y),
    )
}

@Preview
@Composable
private fun Preview() {
    FireworkAnimation(
        isVisible = true,
    )
}
