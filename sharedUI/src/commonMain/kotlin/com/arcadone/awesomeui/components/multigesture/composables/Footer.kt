package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    selectedImageNumber: Int = 0,
    maxSelectableImageNumber: Int,
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 4.dp),
    ) {
        Text(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = "SelectedImages $selectedImageNumber",
        )
        Text(
            text = "Max Images $maxSelectableImageNumber",
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FooterPreview() {
    MaterialTheme {
        Footer(maxSelectableImageNumber = 10)
    }
}
