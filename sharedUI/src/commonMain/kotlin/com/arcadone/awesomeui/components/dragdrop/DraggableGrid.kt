package com.arcadone.awesomeui.components.dragdrop

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * Style configuration for DraggableGrid
 */
data class DraggableGridStyle(
    val itemSpacing: Dp = 8.dp,
    val draggedItemScale: Float = 1.05f,
    val draggedItemElevation: Dp = 8.dp,
    val dropSlotColor: Color = Color(0xFF3D4A5C),
    val dropSlotBorderColor: Color = Color(0xFF8B7BF7),
)

/**
 * A 2D reorderable grid with drag and drop functionality.
 * Items can be dragged both horizontally and vertically.
 *
 * @param items List of items to display
 * @param onReorder Callback when items are reordered
 * @param columns Number of columns in the grid
 * @param key Function to extract stable key from item (IMPORTANT for correct reordering!)
 * @param style Visual styling options
 * @param itemContent Content composable for each item
 */
@Composable
fun <T> DraggableGrid(
    items: List<T>,
    onReorder: (List<T>) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    style: DraggableGridStyle = DraggableGridStyle(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemContent: @Composable (
        item: T,
        index: Int,
        isDragging: Boolean,
        dragModifier: Modifier,
    ) -> Unit,
) {
    val density = LocalDensity.current

    // State for drag operation
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Track item positions and sizes for hit detection
    val itemPositions = remember { mutableStateMapOf<Int, Offset>() }
    val itemSizes = remember { mutableStateMapOf<Int, Pair<Float, Float>>() } // width, height

    // Calculate item dimensions from first item (assuming uniform size)
    val itemWidth = itemSizes[0]?.first ?: 0f
    val itemHeight = itemSizes[0]?.second ?: 0f
    val spacingPx = with(density) { style.itemSpacing.toPx() }

    // Function to calculate target index based on 2D position
    fun updateTargetIndex(
        fromIndex: Int,
        offsetX: Float,
        offsetY: Float,
    ) {
        if (itemWidth <= 0 || itemHeight <= 0) return

        val fromRow = fromIndex / columns
        val fromCol = fromIndex % columns

        // Calculate how many cells we've moved
        val cellWidth = itemWidth + spacingPx
        val cellHeight = itemHeight + spacingPx

        val colDelta = (offsetX / cellWidth).roundToInt()
        val rowDelta = (offsetY / cellHeight).roundToInt()

        val newCol = (fromCol + colDelta).coerceIn(0, columns - 1)
        val newRow = (fromRow + rowDelta).coerceIn(0, (items.size - 1) / columns)

        var newIndex = newRow * columns + newCol
        newIndex = newIndex.coerceIn(0, items.size - 1)

        if (newIndex != targetIndex) {
            println("🟡 TARGET CHANGED - from $targetIndex to $newIndex (row: $newRow, col: $newCol)")
            targetIndex = newIndex
        }
    }

    // Handle reorder on drag end
    fun onDragEnd() {
        val from = draggedIndex
        val to = targetIndex

        println("🔴 DRAG END - fromIndex: $from, targetIndex: $to")
        println("   📋 Items BEFORE: ${items.mapIndexed { i, it -> "$i:${key?.invoke(it) ?: it.hashCode().toString().takeLast(4)}" }}")

        if (from != null && to >= 0 && from != to) {
            println("   ✅ REORDERING from $from to $to")
            val mutableList = items.toMutableList()
            val item = mutableList.removeAt(from)
            mutableList.add(to, item)
            println("   📋 Items AFTER: ${mutableList.mapIndexed { i, it -> "$i:${key?.invoke(it) ?: it.hashCode().toString().takeLast(4)}" }}")
            onReorder(mutableList)
        }

        draggedIndex = null
        targetIndex = -1
        dragOffsetX = 0f
        dragOffsetY = 0f
    }

    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = rememberLazyGridState(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(style.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(style.itemSpacing),
        ) {
            val keyFn: (Int, T) -> Any = { idx, itm ->
                key?.invoke(itm) ?: itm.hashCode()
            }
            itemsIndexed(items, key = keyFn) { index, item ->
                DraggableGridItem(
                    index = index,
                    columns = columns,
                    totalItems = items.size,
                    isDragged = draggedIndex == index,
                    draggedIndex = draggedIndex,
                    targetIndex = targetIndex,
                    dragOffsetX = dragOffsetX,
                    dragOffsetY = dragOffsetY,
                    style = style,
                    itemSizes = itemSizes,
                    onPositionChanged = { pos, size ->
                        itemPositions[index] = pos
                        itemSizes[index] = size
                    },
                    onDragStart = {
                        println("🟢 DRAG START - index: $index, item: $item")
                        println("   📋 Current items: ${items.mapIndexed { i, it -> "$i:${key?.invoke(it) ?: it.hashCode().toString().takeLast(4)}" }}")
                        draggedIndex = index
                        targetIndex = index
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDrag = { offsetX, offsetY ->
                        dragOffsetX = offsetX
                        dragOffsetY = offsetY
                        updateTargetIndex(index, offsetX, offsetY)
                    },
                    onDragEnd = { onDragEnd() },
                    content = { isDragging, dragModifier ->
                        itemContent(item, index, isDragging, dragModifier)
                    },
                )
            }
        }
    }
}

@Composable
private fun DraggableGridItem(
    index: Int,
    columns: Int,
    totalItems: Int,
    isDragged: Boolean,
    draggedIndex: Int?,
    targetIndex: Int,
    dragOffsetX: Float,
    dragOffsetY: Float,
    style: DraggableGridStyle,
    itemSizes: Map<Int, Pair<Float, Float>>,
    onPositionChanged: (Offset, Pair<Float, Float>) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    content: @Composable (isDragging: Boolean, dragModifier: Modifier) -> Unit,
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { style.itemSpacing.toPx() }

    // Get item dimensions
    val itemWidth = itemSizes[0]?.first ?: 0f
    val itemHeight = itemSizes[0]?.second ?: 0f
    val cellWidth = itemWidth + spacingPx
    val cellHeight = itemHeight + spacingPx

    // Calculate shift for non-dragged items
    val shiftOffset: Offset = if (!isDragged && draggedIndex != null && targetIndex >= 0) {
        val dragged = draggedIndex
        val target = targetIndex

        // Determine if this item needs to shift
        val shouldShift = when {
            dragged < target -> index in (dragged + 1)..target
            dragged > target -> index in target until dragged
            else -> false
        }

        if (shouldShift) {
            // Calculate 2D shift based on grid position
            val shiftDirection = if (dragged < target) -1 else 1
            val currentRow = index / columns
            val currentCol = index % columns

            // Calculate new position after shift
            val newIndex = index + shiftDirection
            val newRow = newIndex / columns
            val newCol = newIndex % columns

            // Calculate pixel offset
            val shiftX = (newCol - currentCol) * cellWidth
            val shiftY = (newRow - currentRow) * cellHeight

            Offset(shiftX, shiftY)
        } else {
            Offset.Zero
        }
    } else {
        Offset.Zero
    }

    val animatedShiftX by animateFloatAsState(
        targetValue = shiftOffset.x,
        animationSpec = spring(),
        label = "gridShiftX",
    )
    val animatedShiftY by animateFloatAsState(
        targetValue = shiftOffset.y,
        animationSpec = spring(),
        label = "gridShiftY",
    )

    // Local accumulator for drag offset
    var localDragOffsetX by remember { mutableFloatStateOf(0f) }
    var localDragOffsetY by remember { mutableFloatStateOf(0f) }

    // Modifier for the drag gesture
    // IMPORTANT: Key on `index` so gestures are recreated when item position changes
    val dragModifier = Modifier
        .pointerInput(index) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    localDragOffsetX = 0f
                    localDragOffsetY = 0f
                    onDragStart()
                },
                onDragEnd = {
                    localDragOffsetX = 0f
                    localDragOffsetY = 0f
                    onDragEnd()
                },
                onDragCancel = {
                    localDragOffsetX = 0f
                    localDragOffsetY = 0f
                    onDragEnd()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    localDragOffsetX += dragAmount.x
                    localDragOffsetY += dragAmount.y
                    onDrag(localDragOffsetX, localDragOffsetY)
                },
            )
        }

    // Show placeholder when dragging is active
    val showPlaceholder = draggedIndex != null

    // Main container - CRITICAL: zIndex here affects stacking relative to other grid items
    Box(
        modifier = Modifier
            .zIndex(if (isDragged) 1000f else 0f) // Dragged item floats above ALL others
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInParent()
                val size = Pair(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat(),
                )
                onPositionChanged(pos, size)
            },
    ) {
        // Placeholder background - STAYS IN PLACE (rendered first = behind)
        if (showPlaceholder) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = style.dropSlotColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        border = BorderStroke(2.dp, style.dropSlotBorderColor),
                        shape = RoundedCornerShape(16.dp),
                    ),
            )
        }

        // Content container - MOVES with offset, DEFINES the size (rendered second = on top)
        Box(
            modifier = Modifier
                .zIndex(if (isDragged) 1000f else 1f) // High z-index when dragged
                .offset {
                    if (isDragged) {
                        IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt())
                    } else {
                        IntOffset(animatedShiftX.roundToInt(), animatedShiftY.roundToInt())
                    }
                }
                .then(
                    if (isDragged) {
                        Modifier
                            .scale(style.draggedItemScale)
                            .shadow(style.draggedItemElevation, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    },
                ),
        ) {
            // Actual content - this defines the cell size
            content(isDragged, dragModifier)
        }
    }
}
