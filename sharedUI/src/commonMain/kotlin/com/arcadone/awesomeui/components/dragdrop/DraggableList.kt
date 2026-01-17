package com.arcadone.awesomeui.components.dragdrop

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * Orientation for the draggable list
 */
enum class DragOrientation {
    VERTICAL,
    HORIZONTAL,
}

/**
 * Style configuration for DraggableList
 */
data class DraggableListStyle(
    val itemSpacing: Dp = 8.dp,
    val draggedItemScale: Float = 1.05f,
    val draggedItemElevation: Dp = 8.dp,
    val dropSlotColor: Color = Color(0xFF3D4A5C),
    val dropSlotBorderColor: Color = Color(0xFF8B7BF7),
)

/**
 * A reorderable list with drag and drop functionality.
 * Supports vertical, horizontal orientations.
 *
 * @param items List of items to display
 * @param onReorder Callback when items are reordered
 * @param key Function to extract stable key from item (IMPORTANT for correct reordering!)
 * @param orientation VERTICAL or HORIZONTAL
 * @param style Visual styling options
 * @param itemContent Content composable for each item
 */
@Composable
fun <T> DraggableList(
    items: List<T>,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null, // Key extractor for stable identification
    orientation: DragOrientation = DragOrientation.VERTICAL,
    style: DraggableListStyle = DraggableListStyle(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemContent: @Composable (
        item: T,
        index: Int,
        isDragging: Boolean,
        dragModifier: Modifier,
    ) -> Unit,
) {
    val density = LocalDensity.current

    // Drag state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var targetIndex by remember { mutableIntStateOf(-1) }

    // Item positions for hit testing
    val itemPositions = remember { mutableMapOf<Int, Float>() }
    val itemSizes = remember { mutableMapOf<Int, Float>() }

    // Calculate target index based on drag position
    fun updateTargetIndex(
        currentIndex: Int,
        offset: Float,
    ) {
        val currentPos = itemPositions[currentIndex] ?: return
        val draggedPos = currentPos + offset
        val itemSize = itemSizes[currentIndex] ?: return

        var newTarget = currentIndex
        for ((index, pos) in itemPositions) {
            if (index == currentIndex) continue
            val size = itemSizes[index] ?: continue
            val center = pos + size / 2

            if (offset > 0 && draggedPos + itemSize / 2 > center && index > currentIndex) {
                newTarget = maxOf(newTarget, index)
            } else if (offset < 0 && draggedPos + itemSize / 2 < center && index < currentIndex) {
                newTarget = minOf(newTarget, index)
            }
        }

        if (newTarget != targetIndex) {
            println("🟡 TARGET CHANGED - from $targetIndex to $newTarget")
            targetIndex = newTarget
        }
    }

    // Reorder items when drag ends
    fun onDragEnd() {
        val fromIndex = draggedIndex ?: return
        println("🔴 DRAG END - fromIndex: $fromIndex, targetIndex: $targetIndex")
        println("   📋 Items BEFORE: ${items.mapIndexed { i, it -> "$i:${it.hashCode().toString().takeLast(4)}" }}")

        if (targetIndex != -1 && targetIndex != fromIndex) {
            println("   ✅ REORDERING from $fromIndex to $targetIndex")
            val mutableList = items.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(targetIndex, item)
            println("   📋 Items AFTER: ${mutableList.mapIndexed { i, it -> "$i:${it.hashCode().toString().takeLast(4)}" }}")
            onReorder(mutableList)
        } else {
            println("   ⚪ No reorder needed (same position or invalid target)")
        }
        draggedIndex = null
        dragOffset = 0f
        targetIndex = -1
    }

    Box(modifier = modifier) {
        if (orientation == DragOrientation.VERTICAL) {
            LazyColumn(
                state = rememberLazyListState(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(style.itemSpacing),
            ) {
                // Create key function - use provided key or fallback to hashCode
                val keyFn: (Int, T) -> Any = { idx, itm ->
                    key?.invoke(itm) ?: itm.hashCode()
                }
                itemsIndexed(items, key = keyFn) { index, item ->
                    DraggableListItem(
                        index = index,
                        isDragged = draggedIndex == index,
                        draggedIndex = draggedIndex,
                        targetIndex = targetIndex,
                        dragOffset = dragOffset,
                        orientation = orientation,
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
                            dragOffset = 0f
                        },
                        onDrag = { offset ->
                            dragOffset = offset
                            println("🔵 DRAGGING - offset: $offset, targetIndex: $targetIndex")
                            updateTargetIndex(index, offset)
                        },
                        onDragEnd = { onDragEnd() },
                        content = { isDragging, dragModifier ->
                            itemContent(item, index, isDragging, dragModifier)
                        },
                    )
                }
            }
        } else {
            LazyRow(
                state = rememberLazyListState(),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(style.itemSpacing),
            ) {
                // Create key function - use provided key or fallback to hashCode
                val keyFn: (Int, T) -> Any = { idx, itm ->
                    key?.invoke(itm) ?: itm.hashCode()
                }
                itemsIndexed(items, key = keyFn) { index, item ->
                    DraggableListItem(
                        index = index,
                        isDragged = draggedIndex == index,
                        draggedIndex = draggedIndex,
                        targetIndex = targetIndex,
                        dragOffset = dragOffset,
                        orientation = orientation,
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
                            dragOffset = 0f
                        },
                        onDrag = { offset ->
                            dragOffset = offset
                            println("🔵 DRAGGING - offset: $offset, targetIndex: $targetIndex")
                            updateTargetIndex(index, offset)
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
}

@Composable
private fun DraggableListItem(
    index: Int,
    isDragged: Boolean,
    draggedIndex: Int?,
    targetIndex: Int,
    dragOffset: Float,
    orientation: DragOrientation,
    style: DraggableListStyle,
    itemSizes: Map<Int, Float>, // Added: map of item sizes
    onPositionChanged: (Float, Float) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    content: @Composable (isDragging: Boolean, dragModifier: Modifier) -> Unit,
) {
    val density = LocalDensity.current

    // Calculate shift for non-dragged items - use actual item size for full shift
    val shouldShift = draggedIndex != null && index != draggedIndex
    val draggedItemSize = itemSizes[draggedIndex] ?: 0f
    val shiftAmountPx = if (shouldShift && draggedItemSize > 0f) {
        val dragged = draggedIndex ?: -1
        val fullShift = draggedItemSize + with(density) { style.itemSpacing.toPx() }
        val shift = when {
            // Item needs to shift up/left when dragged item moves down
            dragged < index && index <= targetIndex -> -fullShift
            // Item needs to shift down/right when dragged item moves up
            dragged > index && index >= targetIndex -> fullShift
            else -> 0f
        }
        // Debug log for shift calculation
        if (shift != 0f) {
            println("🔶 SHIFT - index: $index, dragged: $dragged, target: $targetIndex, shift: $shift")
        }
        shift
    } else {
        0f
    }

    val animatedShiftPx by animateFloatAsState(
        targetValue = shiftAmountPx,
        animationSpec = spring(),
        label = "itemShift",
    )

    // Local accumulator for drag offset - this persists across drag events
    var localDragOffset by remember { mutableFloatStateOf(0f) }

    // Modifier for the drag gesture (on the handle)
    // IMPORTANT: Key on `index` so that when items are reordered and LazyColumn reuses composables,
    // the gesture callbacks are recreated with the correct index value
    val dragModifier = Modifier
        .pointerInput(index) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    localDragOffset = 0f
                    onDragStart()
                },
                onDragEnd = {
                    localDragOffset = 0f
                    onDragEnd()
                },
                onDragCancel = {
                    localDragOffset = 0f
                    onDragEnd()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val delta = if (orientation == DragOrientation.VERTICAL) {
                        dragAmount.y
                    } else {
                        dragAmount.x
                    }
                    localDragOffset += delta
                    onDrag(localDragOffset)
                },
            )
        }

    // Show placeholder "shadow" on ALL items when dragging is active
    val showPlaceholder = draggedIndex != null

    // Measure item size for placeholder
    var measuredHeight by remember { mutableFloatStateOf(0f) }
    var measuredWidth by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            // CRITICAL: Apply high zIndex to PARENT container when this item is being dragged
            .then(if (isDragged) Modifier.zIndex(100f) else Modifier)
            .onGloballyPositioned { coordinates ->
                val pos = if (orientation == DragOrientation.VERTICAL) {
                    coordinates.positionInParent().y
                } else {
                    coordinates.positionInParent().x
                }
                val size = if (orientation == DragOrientation.VERTICAL) {
                    coordinates.size.height.toFloat()
                } else {
                    coordinates.size.width.toFloat()
                }
                measuredHeight = coordinates.size.height.toFloat()
                measuredWidth = coordinates.size.width.toFloat()
                onPositionChanged(pos, size)
                // Log position for debugging
                // println("📍 POSITION - index: $index, pos: $pos, size: $size")
            },
    ) {
        // Placeholder "shadow" with neon border for all items when dragging
        if (showPlaceholder) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = style.dropSlotColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .border(
                        border = BorderStroke(2.dp, style.dropSlotBorderColor),
                        shape = RoundedCornerShape(12.dp),
                    ),
            )
        }

        // Actual item content with offset and visual effects
        Box(
            modifier = Modifier
                .then(
                    if (isDragged) {
                        Modifier
                            .zIndex(100f) // High z-index to always be on top
                            .offset {
                                if (orientation == DragOrientation.VERTICAL) {
                                    IntOffset(0, dragOffset.roundToInt())
                                } else {
                                    IntOffset(dragOffset.roundToInt(), 0)
                                }
                            }
                            .scale(style.draggedItemScale)
                            .shadow(style.draggedItemElevation, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                            .offset {
                                if (orientation == DragOrientation.VERTICAL) {
                                    IntOffset(0, animatedShiftPx.roundToInt())
                                } else {
                                    IntOffset(animatedShiftPx.roundToInt(), 0)
                                }
                            }
                            .graphicsLayer {
                                // Dim non-dragged items slightly when dragging
                                alpha = if (draggedIndex != null) 0.85f else 1f
                            }
                    },
                ),
        ) {
            content(isDragged, dragModifier)
        }
    }
}

// ============================================================================
// SAMPLE ITEM AND PREVIEWS
// ============================================================================

/**
 * Sample data class for preview
 */
data class SampleDragItem(val id: Int, val title: String, val color: Color)

/**
 * Sample item card for the draggable list
 */
@Composable
fun SampleDragItemCard(
    item: SampleDragItem,
    isDragging: Boolean,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = item.color,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )

            // Drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = dragModifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DraggableListVerticalPreview() {
    val sampleItems = remember {
        mutableStateOf(
            listOf(
                SampleDragItem(1, "Item 1 - Chest", Color(0xFF2A3441)),
                SampleDragItem(2, "Item 2 - Back", Color(0xFF2A3441)),
                SampleDragItem(3, "Item 3 - Legs", Color(0xFF2A3441)),
                SampleDragItem(4, "Item 4 - Shoulders", Color(0xFF2A3441)),
                SampleDragItem(5, "Item 5 - Arms", Color(0xFF2A3441)),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D24)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Drag and Drop - Vertical",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )

            DraggableList(
                items = sampleItems.value,
                onReorder = { newList ->
                    sampleItems.value = newList
                    println("📋 NEW ORDER: ${newList.map { it.title }}")
                },
                orientation = DragOrientation.VERTICAL,
                modifier = Modifier.fillMaxWidth(),
            ) { item, index, isDragging, dragModifier ->
                SampleDragItemCard(
                    item = item,
                    isDragging = isDragging,
                    dragModifier = dragModifier,
                )
            }
        }
    }
}

@Preview
@Composable
private fun DraggableListHorizontalPreview() {
    val sampleItems = remember {
        mutableStateOf(
            listOf(
                SampleDragItem(1, "A", Color(0xFF8B7BF7)),
                SampleDragItem(2, "B", Color(0xFFFF6B35)),
                SampleDragItem(3, "C", Color(0xFF4CAF50)),
                SampleDragItem(4, "D", Color(0xFF2196F3)),
                SampleDragItem(5, "E", Color(0xFFE91E63)),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D24)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Drag and Drop - Horizontal",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )

            DraggableList(
                items = sampleItems.value,
                onReorder = { newList ->
                    sampleItems.value = newList
                    println("📋 NEW ORDER: ${newList.map { it.title }}")
                },
                orientation = DragOrientation.HORIZONTAL,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            ) { item, index, isDragging, dragModifier ->
                Card(
                    modifier = Modifier
                        .size(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = item.color),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = dragModifier,
                        )
                    }
                }
            }
        }
    }
}
