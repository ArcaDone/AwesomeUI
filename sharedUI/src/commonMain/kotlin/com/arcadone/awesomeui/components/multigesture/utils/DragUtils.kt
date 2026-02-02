package com.arcadone.awesomeui.components.multigesture.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.arcadone.awesomeui.components.multigesture.model.ImageModel
import kotlinx.coroutines.delay

/**
 * A Composable function that extends a Modifier to support drag-based selection of images
 * from a list. This function allows users to select multiple images by dragging across them.
 *
 * @param imageModelList The list of images to display and select from.
 * @param state The state of the LazyGrid used to display the images.
 * @param imageModelsSelected The list of images currently selected.
 * @param onSelectionChange Callback function invoked when the selection changes.
 * @param lockedImage An optional image that cannot be deselected.
 * @return The modified Modifier with drag selection capability.
 */
@Composable
fun Modifier.dragSelection(
    imageModelList: List<ImageModel>,
    state: LazyGridState,
    imageModelsSelected: List<ImageModel>,
    onSelectionChange: (ImageModel) -> Unit,
    lockedImage: ImageModel? = null,
): Modifier {
    var isDragging by remember { mutableStateOf(false) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    var dragEnd by remember { mutableStateOf(Offset.Zero) }
    var processedImages by remember { mutableStateOf(setOf<ImageModel>()) }
    val haptic = LocalHapticFeedback.current
    val updatedImageModelListState = rememberUpdatedState(newValue = imageModelList)

    LaunchedEffect(isDragging) {
        if (isDragging) {
            while (isDragging) {
                val dragEndY = dragEnd.y
                val layoutInfo = state.layoutInfo
                val viewportHeight = layoutInfo.viewportEndOffset
                val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (dragEndY < 100 && firstVisibleItemIndex > 0) {
                    state.scrollBy(-20f)
                }

                if (dragEndY > viewportHeight - 100 && lastVisibleItemIndex < imageModelList.size - 1) {
                    state.scrollBy(20f)
                }

                delay(20)
            }
        }
    }
    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { startOffset ->
                dragStart = startOffset
                dragEnd = startOffset
                isDragging = true
                imageModelsSelected.toList()
                processedImages = setOf()
                println("DragSelection || Drag started at: $startOffset, ${updatedImageModelListState.value.toList().size}")
            },
            onDrag = { change, dragAmount ->
                change.consume()
                dragEnd += Offset(dragAmount.x, dragAmount.y)
                val minX = minOf(dragStart.x, dragEnd.x)
                val maxX = maxOf(dragStart.x, dragEnd.x)
                val minY = minOf(dragStart.y, dragEnd.y)
                val maxY = maxOf(dragStart.y, dragEnd.y)
                println("DragSelection || Dragging to: $dragEnd")

                val itemsInBox = updatedImageModelListState.value
                    .filter { imageModel ->
                        val itemOffset =
                            state.layoutInfo.visibleItemsInfo.find { it.key == imageModel.id }?.offset
                                ?: IntOffset.Zero
                        val itemSize =
                            state.layoutInfo.visibleItemsInfo.find { it.key == imageModel.id }?.size
                                ?: IntSize.Zero
                        val itemBounds = IntOffset(itemOffset.x, itemOffset.y) to IntSize(
                            itemSize.width,
                            itemSize.height,
                        )
                        val itemStart =
                            Offset(
                                itemBounds.first.x.toFloat(),
                                itemBounds.first.y.toFloat(),
                            )
                        val itemEnd = Offset(
                            itemBounds.first.x + itemBounds.second.width.toFloat(),
                            itemBounds.first.y + itemBounds.second.height.toFloat(),
                        )
                        itemStart.x < maxX && itemEnd.x > minX && itemStart.y < maxY && itemEnd.y > minY
                    }

                println("DragSelection || Items in selection box: ${itemsInBox.size}")

                itemsInBox.forEach { imageModel ->
                    if (imageModel !in processedImages) {
                        processedImages = processedImages + imageModel
                        if (lockedImage?.id != imageModel.id) {
                            println("DragSelection || Image selected: ${imageModel.id}")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectionChange(imageModel)
                        }
                    }
                }
            },
            onDragEnd = {
                isDragging = false
                processedImages = setOf()
                println("DragSelection || Drag ended")
            },
        )
    }
}
