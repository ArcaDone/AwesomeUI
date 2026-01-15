package com.arcadone.awesomeui.components.striped

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StripedBarItem(
    modifier: Modifier = Modifier.size(38.dp),
    backgroundColor: Color = Color(0xFF222222),
    stripeColor: Color = Color.Black,
    gap: Dp = 6.dp,
    rotation: Float = 45f,
) {
    Canvas(modifier = modifier.background(backgroundColor)) {
        val w = size.width + size.height
        withTransform({ rotate(rotation) }) {
            var x = -size.height
            while (x < w) {
                drawLine(
                    color = stripeColor,
                    start = Offset(x, -size.height),
                    end = Offset(x, size.height * 2),
                    strokeWidth = gap.toPx(),
                )
                x += gap.toPx() * 2
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStripedBarItem() {
    StripedBarItem()
}
