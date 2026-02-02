package com.arcadone.awesomeui.components.multigesture

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arcadone.awesomeui.components.multigesture.composables.Footer
import com.arcadone.awesomeui.components.multigesture.composables.Header
import com.arcadone.awesomeui.components.multigesture.composables.ModalBottomSheet
import com.arcadone.awesomeui.components.multigesture.composables.OneButtonDialog
import com.arcadone.awesomeui.components.multigesture.composables.PhotoGridMultiSelect
import com.arcadone.awesomeui.components.multigesture.model.ImageModel
import com.arcadone.awesomeui.components.multigesture.model.PhotoGridSelectionState

private val photos: List<ImageModel> =
    List(40) { ImageModel(image = "https://picsum.photos/id/${(0..1000).random()}/600/600") }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GridView() {
    val viewModel = remember { PhotoGridViewModel() }
    val showBottomSheet = viewModel.showBottomSheet
    val showZoomBottomSheet = remember { mutableStateOf(false) }

    // IMPORTANT: Use collectAsState() to observe StateFlow changes
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setEvent(
            PhotoGridEvents.OnStart(
                lockedImage = null,
                imageModelList = photos,
                selectedImage = emptyList(),
                selectionConstraints = SelectionConstraints(1, 50),
            ),
        )
    }

    Scaffold(
        topBar = {
            Header(
                imageModelList = photos,
                selectedList = state.selectedImage,
                selectionLimitReached = state.selectionLimitReached,

                onMultiSelectHeaderTap = { selectionState ->
                    if (selectionState == PhotoGridSelectionState.SELECT_ALL) {
                        viewModel.setEvent(PhotoGridEvents.SelectAll(state.imageModelList))
                    } else {
                        viewModel.setEvent(PhotoGridEvents.UnSelectAll(state.imageModelList))
                    }
                },
            )
        },
        bottomBar = {
            Footer(
                selectedImageNumber = state.selectedImage.size,
                maxSelectableImageNumber = state.selectionConstraints.max,
            )
        },
    ) { innerPadding ->
        PhotoGridMultiSelect(
            modifier = Modifier.padding(innerPadding),
            imageModelList = photos,
            selectedList = state.selectedImage,
            onImageLongClick = { _, _ ->
                showZoomBottomSheet.value = true
            },
            onSelectionChange = { imageModel ->
                viewModel.setEvent(PhotoGridEvents.OnPhotoTap(imageModel))
            },
            lockedImage = state.lockedImage,
        )
    }

    if (showZoomBottomSheet.value) {
        OneButtonDialog(
            title = "On Long Press",
            description = "You can do something here",
            buttonText = "Ok",
            onDismissRequest = {
                showZoomBottomSheet.value = false
            },
        )
    }
    if (showBottomSheet.value) {
        ModalBottomSheet(
            titleText = "Selection Limit Reached",
            descriptionText = "You have reached the maximum number of selections",
            onDismissRequest = {
                viewModel.setEvent(PhotoGridEvents.HideSelectionLimitReachedBottomSheet)
            },
        )
    }
}
