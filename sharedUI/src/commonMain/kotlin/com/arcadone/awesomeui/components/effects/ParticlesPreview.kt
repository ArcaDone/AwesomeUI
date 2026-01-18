package com.arcadone.awesomeui.components.effects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import awesome_ui.sharedui.generated.resources.Res
import awesome_ui.sharedui.generated.resources.ad
import awesome_ui.sharedui.generated.resources.ic_dark_mode
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

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
                colors = ParticleColors.RainbowPalette,
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
                colors = ParticleColors.RainbowPalette,
            ),
            effectCenterOffset = Offset(0f, -80f),
            profileContent = {
                Image(
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

// ============================================================================
// PARTICLE SHOWCASE - Multiple Examples
// ============================================================================

@Preview
@Composable
fun ParticleShowcasePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Example 1: Gold Classic - Diagonal X Pattern
        PremiumStarCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            title = "Gold Classic",
            subtitle = "Diagonal X pattern with gold stars",
            style = PremiumStarCardStyle(
                accentColor = ParticleColors.Gold,
            ),
            burstStyle = ParticleBurstStyle(
                particleCount = 100,
                shapes = listOf(ParticleShape.Star4Point, ParticleShape.Star6Point),
                colors = ParticleColors.GoldPalette,
                emissionPattern = EmissionPattern.DIAGONAL_X,
                cycleDurationMs = 3000,
                spreadAngle = 0.6f,
                flutterEnabled = true,
                maxFlutterAmount = 25f,
            ),
            flickerStyle = FlickerStarsStyle(
                particleCount = 30,
                colors = ParticleColors.GoldPalette,
            ),
            effectCenterOffset = Offset(0f, -60f),
            profileContent = {
                Image(
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    painter = painterResource(Res.drawable.ad),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                )
            },
        )

        // Example 2: Fire Burst - Radial Pattern (Fast)
        PremiumStarCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            title = "Fire Burst",
            subtitle = "Radial pattern with fire colors - Fast",
            style = PremiumStarCardStyle(
                accentColor = ParticleColors.OrangeRed,
            ),
            burstStyle = ParticleBurstStyle(
                particleCount = 280,
                shapes = listOf(ParticleShape.Star4Point, ParticleShape.Circle),
                colors = ParticleColors.FirePalette,
                emissionPattern = EmissionPattern.DIAGONAL_X,
                cycleDurationMs = 1500,
                spreadAngle = 0.4f,
                minSpeed = 0.4f,
                maxSpeed = 1f,
                flutterEnabled = true,
                maxFlutterAmount = 15f,
            ),
            flickerStyle = FlickerStarsStyle(
                particleCount = 50,
                colors = ParticleColors.FirePalette,
                minFlickerDuration = 0.15f,
                maxFlickerDuration = 0.3f,
            ),
            effectCenterOffset = Offset(0f, -60f),
            profileContent = {
                Image(
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    painter = painterResource(Res.drawable.ad),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                )
            },
        )

        // Example 3: Pink Dream - Upward Spread (Slow & Elegant)
        PremiumStarCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            title = "Pink Dream",
            subtitle = "Upward spread - Slow & elegant",
            style = PremiumStarCardStyle(
                accentColor = ParticleColors.Pink,
            ),
            burstStyle = ParticleBurstStyle(
                particleCount = 60,
                shapes = listOf(ParticleShape.Star6Point),
                colors = ParticleColors.PinkPalette,
                emissionPattern = EmissionPattern.UPWARD_SPREAD,
                cycleDurationMs = 5000,
                spreadAngle = 0.8f,
                minSpeed = 0.1f,
                maxSpeed = 0.5f,
                flutterEnabled = true,
                maxFlutterAmount = 40f,
                maxFlutterSpeed = 3f,
            ),
            flickerStyle = FlickerStarsStyle(
                particleCount = 25,
                colors = ParticleColors.PinkPalette,
                minFlickerDuration = 0.3f,
                maxFlickerDuration = 0.6f,
            ),
            effectCenterOffset = Offset(0f, -60f),
            profileContent = {
                Image(
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    painter = painterResource(Res.drawable.ad),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                )
            },
        )

        // Example 4: Rainbow Explosion - High particle count
        PremiumStarCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            title = "Rainbow Explosion",
            subtitle = "Radial with all colors - High density",
            style = PremiumStarCardStyle(
                accentColor = ParticleColors.Cyan,
            ),
            burstStyle = ParticleBurstStyle(
                particleCount = 200,
                shapes = listOf(ParticleShape.Star4Point, ParticleShape.Star6Point, ParticleShape.Circle),
                colors = ParticleColors.RainbowPalette,
                emissionPattern = EmissionPattern.RADIAL,
                cycleDurationMs = 2500,
                spreadAngle = 1f,
                minSize = 6f,
                maxSize = 20f,
                flutterEnabled = true,
                maxFlutterAmount = 30f,
            ),
            flickerStyle = FlickerStarsStyle(
                particleCount = 60,
                colors = ParticleColors.RainbowPalette,
            ),
            effectCenterOffset = Offset(0f, -60f),
            profileContent = {
                Image(
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    painter = painterResource(Res.drawable.ad),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                )
            },
        )

        // Row with 2 smaller cards side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Example 5: Minimal Gold - Small
            PremiumStarCard(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
                title = "Minimal",
                subtitle = null,
                style = PremiumStarCardStyle(
                    profileSize = 60.dp,
                    accentColor = ParticleColors.Gold,
                ),
                burstStyle = ParticleBurstStyle(
                    particleCount = 30,
                    shapes = listOf(ParticleShape.Star4Point),
                    colors = ParticleColors.GoldPalette,
                    emissionPattern = EmissionPattern.DIAGONAL_X,
                    cycleDurationMs = 4000,
                    flutterEnabled = true,
                ),
                flickerStyle = null,
                effectCenterOffset = Offset(0f, -40f),
                profileContent = {
                    Image(
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        painter = painterResource(Res.drawable.ad),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                    )
                },
            )

            // Example 6: Intense Fire - Small
            PremiumStarCard(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
                title = "Intense",
                subtitle = null,
                style = PremiumStarCardStyle(
                    profileSize = 60.dp,
                    accentColor = ParticleColors.OrangeRed,
                ),
                burstStyle = ParticleBurstStyle(
                    particleCount = 100,
                    shapes = listOf(ParticleShape.Star4Point, ParticleShape.Circle),
                    colors = ParticleColors.FirePalette,
                    emissionPattern = EmissionPattern.RADIAL,
                    cycleDurationMs = 1000,
                    minSpeed = 0.5f,
                    maxSpeed = 1f,
                    flutterEnabled = true,
                    maxFlutterAmount = 10f,
                ),
                flickerStyle = FlickerStarsStyle(
                    particleCount = 40,
                    colors = ParticleColors.FirePalette,
                ),
                effectCenterOffset = Offset(0f, -40f),
                profileContent = {
                    Image(
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        painter = painterResource(Res.drawable.ad),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                    )
                },
            )
        }
    }
}
