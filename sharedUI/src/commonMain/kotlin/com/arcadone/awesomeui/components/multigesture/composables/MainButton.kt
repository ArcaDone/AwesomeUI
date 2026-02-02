package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MainButton(
    modifier: Modifier = Modifier,
    text: String = "Text String",
    onClick: () -> Unit,
    buttonHeight: Dp = 50.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    contentColor: Color? = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    border: BorderStroke? = null,
    buttonEnabled: Boolean = true,
    verticalPadding: Dp = 8.dp,
) {
    TextButton(
        onClick = { onClick() },
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = contentColor ?: MaterialTheme.colorScheme.primary,
        ),
        enabled = buttonEnabled,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .height(buttonHeight)
            .then(modifier),
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text,
            maxLines = maxLines,
            softWrap = softWrap,
            color = textColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainButtonPreview() {
    MaterialTheme {
        MainButton(
            modifier = Modifier.padding(all = 4.dp),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    MaterialTheme {
        MainButton(
            modifier = Modifier.padding(all = 4.dp),
            onClick = {},
            contentColor = Color.White,
            textColor = MaterialTheme.colorScheme.primary,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainButtonDisabled() {
    MaterialTheme {
        MainButton(
            modifier = Modifier.padding(all = 4.dp),
            onClick = {},
            buttonEnabled = false,
        )
    }
}
