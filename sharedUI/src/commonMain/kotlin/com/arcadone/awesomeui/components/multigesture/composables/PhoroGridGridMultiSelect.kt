package com.arcadone.awesomeui.components.multigesture.composables

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arcadone.awesomeui.components.multigesture.model.ImageModel
import com.arcadone.awesomeui.components.multigesture.utils.dragSelection
import kotlinx.coroutines.launch

/**
 * A Composable function that displays a grid of images and supports multiple selection
 * using tap, long press, and drag gestures.
 *
 * @param modifier Modifier for customizing the appearance and behavior.
 * @param imageModelList List of images to display in the grid.
 * @param selectedList List of currently selected images.
 * @param onImageLongClick Callback function invoked when an image is long-pressed.
 * @param onSelectionChange Callback function invoked when the selection changes.
 * @param lockedImage An optional image that cannot be deselected.
 * @param showLoaderItem Boolean flag to show a loader item at the end of the grid.
 */

@Composable
fun PhotoGridMultiSelect(
    modifier: Modifier = Modifier,
    imageModelList: List<ImageModel>,
    selectedList: List<ImageModel>,
    onImageLongClick: (Int, ImageModel) -> Unit,
    onSelectionChange: (ImageModel) -> Unit,
    lockedImage: ImageModel? = null,
    showLoaderItem: Boolean = false,
) {
    val state = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current

    val localModifier = modifier
        .fillMaxSize()
        .dragSelection(
            imageModelList = imageModelList,
            state = state,
            imageModelsSelected = selectedList,
            onSelectionChange = { changedPhoto ->
                onSelectionChange(changedPhoto)
            },
            lockedImage = lockedImage,
        )

    Column {
        Box(
            modifier = localModifier,
        ) {
            LazyVerticalGrid(
                state = state,
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier,
            ) {
                item(span = { GridItemSpan(3) }) {
                }

                itemsIndexed(
                    imageModelList,
                    key = { _: Int, item: ImageModel -> item.id },
                ) { index, photo ->
                    val selected = selectedList.any { it.id == photo.id }
                    val photoModifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (lockedImage?.id == photo.id) return@detectTapGestures
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectionChange(photo)
                            },
                            onLongPress = {
                                if (lockedImage?.id != photo.id) {
                                    onImageLongClick(index, photo)
                                }
                            },
                        )
                    }
                    SelectableImageItem(
                        modifier = photoModifier,
                        imageModel = photo,
                        contentDescription = "Photo Grid Item index $index",
                        isImageSelected = selected && lockedImage?.id != photo.id,
                        isImageLocked = lockedImage?.id == photo.id,
                    )
                }
                if (showLoaderItem) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_4")
@Composable
fun SamplePhotosGridPreview() {
    MaterialTheme {
        val photos: List<ImageModel> =
            List(40) { ImageModel(image = "https://picsum.photos/id/${(0..1000).random()}/400/400") }
        var selectedList by remember { mutableStateOf(photos.take(5)) }
        val coroutineScope = rememberCoroutineScope()
        val showBottomSheet = remember { mutableStateOf(false) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        Scaffold(
            topBar = {},
            bottomBar = {},
        ) {
            PhotoGridMultiSelect(
                modifier = Modifier.padding(it),
                imageModelList = photos,
                selectedList = selectedList,
                onImageLongClick = { _, _ ->
                    showBottomSheet.value = true
                },
                onSelectionChange = { imageModel ->
                    if (selectedList.contains(imageModel)) {
                        selectedList = selectedList.filter { photo -> photo.id != imageModel.id }
                    } else {
                        selectedList += imageModel
                    }
                },
                lockedImage = photos.first(),
            )
        }

        LaunchedEffect(showBottomSheet) {
            if (showBottomSheet.value) {
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            } else {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        }
    }
}
