package com.arcadone.awesomeui.components.multigesture.utils

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

fun DrawScope.drawRoundedRectangle(
    color: Color,
    width: Float = size.minDimension,
    height: Float = size.minDimension,
    cornerRadius: Float = 8.dp.toPx(),
) {
    drawRoundRect(
        color = color,
        topLeft = center.copy(x = center.x - width / 2, y = center.y - height / 2),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
    )
}
