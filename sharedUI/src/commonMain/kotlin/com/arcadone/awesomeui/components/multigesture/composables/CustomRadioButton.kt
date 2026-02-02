package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import awesome_ui.sharedui.generated.resources.Res
import awesome_ui.sharedui.generated.resources.ic_check_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun CustomRadioButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    circleRadius: Dp = 10.dp,
    iconSize: Dp = 10.dp,
    selected: Boolean = false,
) {
    val boxModifier =
        if (selected) {
            Modifier
        } else {
            Modifier.border(2.dp, Color.LightGray, CircleShape)
        }

    Box(
        modifier = Modifier
            .then(modifier)
            .size(circleRadius * 2)
            .then(boxModifier),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = circleRadius.toPx()
            drawCircle(
                color = if (selected) backgroundColor else Color.Transparent,
                radius = radius,
            )
        }
        if (selected) {
            Image(
                painter = painterResource(Res.drawable.ic_check_icon),
                contentDescription = "Check",
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Inside,
            )
        }
    }
}

@Preview
@Composable
private fun CustomRadioButtonPreview() {
    MaterialTheme {
        CustomRadioButton(selected = true)
    }
}

@Preview
@Composable
private fun CustomRadioButtonDeselectedPreview() {
    MaterialTheme {
        CustomRadioButton(selected = false)
    }
}
