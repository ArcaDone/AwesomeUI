package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arcadone.awesomeui.components.multigesture.model.ImageModel
import com.arcadone.awesomeui.components.multigesture.model.PhotoGridSelectionState

@Composable
fun Header(
    modifier: Modifier = Modifier,
    text: String = "Multi Gesture",
    imageModelList: List<ImageModel>,
    selectedList: List<ImageModel>,
    onMultiSelectHeaderTap: (PhotoGridSelectionState) -> Unit = { },
    selectionLimitReached: Boolean,
) {
    val isAllImagesSelected = imageModelList.all { it in selectedList }

    val selectionState = when {
        (selectionLimitReached && !isAllImagesSelected) -> PhotoGridSelectionState.CLEAR_SELECTION
        isAllImagesSelected -> PhotoGridSelectionState.DESELECT_ALL
        else -> PhotoGridSelectionState.SELECT_ALL
    }

    val textRes = when (selectionState) {
        PhotoGridSelectionState.SELECT_ALL -> "Select All"
        PhotoGridSelectionState.DESELECT_ALL -> "Deselect All"
        PhotoGridSelectionState.CLEAR_SELECTION -> "Clear Selection"
    }
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontWeight = FontWeight.Bold,
        )

        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable {
                    onMultiSelectHeaderTap(selectionState)
                },
            text = textRes,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
    MaterialTheme {
        Header(
            imageModelList = emptyList(),
            selectedList = emptyList(),
            selectionLimitReached = false,
        )
    }
}
