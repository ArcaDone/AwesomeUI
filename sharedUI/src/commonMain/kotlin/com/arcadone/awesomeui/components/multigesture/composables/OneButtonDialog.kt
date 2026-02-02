package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun OneButtonDialog(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    buttonText: String,
    disableTapToClose: Boolean = false,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (!disableTapToClose) onDismissRequest()
        },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp),
                )
                Text(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.CenterHorizontally),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    text = description,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.width(16.dp))
                MainButton(
                    text = buttonText,
                    onClick = { onDismissRequest() },
                )
            }
        }
    }
}

@Preview(device = "id:pixel_4")
@Composable
private fun ScaleDiscountShortDialogPreview() {
    MaterialTheme {
        OneButtonDialog(
            title = "Title",
            description = "Description",
            buttonText = "OK",
            onDismissRequest = {
            },
        )
    }
}
