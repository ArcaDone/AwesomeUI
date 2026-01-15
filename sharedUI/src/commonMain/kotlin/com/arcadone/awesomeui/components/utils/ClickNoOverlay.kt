package com.arcadone.awesomeui.components.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

fun Modifier.clickableNoOverlay(onClick: () -> Unit): Modifier = this.clickable(
    onClick = { onClick() },
    indication = null,
    interactionSource = MutableInteractionSource(),
)
