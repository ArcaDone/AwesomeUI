package com.arcadone.awesomeui.components.dragdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium color palette for drag & drop showcase
 * Inspired by VariantColors - vibrant neon with glow effects
 */
object DragDropShowcaseColors {
    val Background = Color(0xFF0A0C10)
    val CardDark = Color(0xFF14171F)

    // Premium neon card colors - vibrant and distinguishable
    val Card1 = Color(0xFFFF6B35) // Neon Orange
    val Card2 = Color(0xFF8B7BF7) // Electric Purple
    val Card3 = Color(0xFF00D9FF) // Neon Cyan
    val Card4 = Color(0xFF00FF88) // Neon Green
    val Card5 = Color(0xFFFF3366) // Neon Pink
    val Card6 = Color(0xFFFFD93D) // Neon Yellow
    val Card7 = Color(0xFF6C5CE7) // Deep Purple
    val Card8 = Color(0xFF00CEC9) // Teal
    val Card9 = Color(0xFFFF9F43) // Warm Orange

    // Placeholder/drop slot
    val DropSlotFill = Color(0xFFFF6B35)
    val DropSlotBorder = Color(0xFFFF8C5A) // Purple glow border

    // Text
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B3BA)
}

/**
 * Simple numbered item for showcases
 */
data class NumberedItem(val id: Int, val number: Int, val color: Color, val icon: ImageVector)

/**
 * Get a list of numbered items with distinct colors
 */
fun getNumberedItems(count: Int): List<NumberedItem> {
    val colors = listOf(
        DragDropShowcaseColors.Card1,
        DragDropShowcaseColors.Card2,
        DragDropShowcaseColors.Card3,
        DragDropShowcaseColors.Card4,
        DragDropShowcaseColors.Card5,
        DragDropShowcaseColors.Card6,
        DragDropShowcaseColors.Card7,
        DragDropShowcaseColors.Card8,
        DragDropShowcaseColors.Card9,
    )
    val icons = listOf(
        Icons.Default.Image,
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.MusicNote,
        Icons.Default.VideoLibrary,
        Icons.Default.Image,
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.MusicNote,
    )
    return (1..count).map { num ->
        NumberedItem(
            id = num,
            number = num,
            color = colors[(num - 1) % colors.size],
            icon = icons[(num - 1) % icons.size],
        )
    }
}

// ============================================================================
// GRID SHOWCASE - 2D Drag
// ============================================================================

/**
 * 2D Grid Showcase - Premium numbered cards
 */
@Preview
@Composable
fun DragDropGridShowcase() {
    var items by remember { mutableStateOf(getNumberedItems(9)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DragDropShowcaseColors.Background)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Text(
                text = "2D Grid Reorder",
                color = DragDropShowcaseColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Drag cards in any direction to reorder",
                color = DragDropShowcaseColors.TextSecondary,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2D Draggable Grid
            DraggableGrid(
                items = items,
                onReorder = { newList ->
                    items = newList
                    println("📋 GRID REORDERED: ${newList.map { it.number }}")
                },
                columns = 3,
                key = { it.id },
                style = DraggableGridStyle(
                    itemSpacing = 12.dp,
                    draggedItemScale = 1.08f,
                    draggedItemElevation = 16.dp,
                    dropSlotColor = DragDropShowcaseColors.DropSlotFill,
                    dropSlotBorderColor = DragDropShowcaseColors.DropSlotBorder,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.fillMaxSize(),
            ) { item, index, isDragging, dragModifier ->
                NumberedGridCard(
                    item = item,
                    isDragging = isDragging,
                    dragModifier = dragModifier,
                    modifier = Modifier.aspectRatio(1f),
                )
            }
        }
    }
}

/**
 * Premium numbered card for grid
 */
@Composable
fun NumberedGridCard(
    item: NumberedItem,
    isDragging: Boolean,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val gradientColors = listOf(
        item.color,
        item.color.copy(alpha = 0.7f),
    )

    Card(
        modifier = modifier
            .then(dragModifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDragging) item.color else Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 16.dp else 4.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                    ),
            )

            // Large number
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.number}",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Drag handle indicator (top right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ============================================================================
// LIST SHOWCASE - Vertical Drag
// ============================================================================

/**
 * Vertical List Showcase
 */
@Preview
@Composable
fun DragDropListShowcase() {
    var items by remember { mutableStateOf(getNumberedItems(5)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DragDropShowcaseColors.Background)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Text(
                text = "Vertical List Reorder",
                color = DragDropShowcaseColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hold and drag to reorder items",
                color = DragDropShowcaseColors.TextSecondary,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Vertical Draggable List
            DraggableList(
                items = items,
                onReorder = { newList ->
                    items = newList
                    println("📋 LIST REORDERED: ${newList.map { it.number }}")
                },
                key = { it.id },
                orientation = DragOrientation.VERTICAL,
                style = DraggableListStyle(
                    itemSpacing = 12.dp,
                    draggedItemScale = 1.02f,
                    draggedItemElevation = 12.dp,
                    dropSlotColor = DragDropShowcaseColors.DropSlotFill,
                    dropSlotBorderColor = DragDropShowcaseColors.DropSlotBorder,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.fillMaxSize(),
            ) { item, index, isDragging, dragModifier ->
                NumberedListCard(
                    item = item,
                    isDragging = isDragging,
                    dragModifier = dragModifier,
                )
            }
        }
    }
}

/**
 * Premium numbered card for vertical list
 */
@Composable
fun NumberedListCard(
    item: NumberedItem,
    isDragging: Boolean,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .then(dragModifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DragDropShowcaseColors.CardDark,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 12.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Colored number badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${item.number}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Item ${item.number}",
                    color = DragDropShowcaseColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Drag to reorder",
                    color = DragDropShowcaseColors.TextSecondary,
                    fontSize = 14.sp,
                )
            }

            // Drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = DragDropShowcaseColors.TextSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ============================================================================
// HORIZONTAL SHOWCASE
// ============================================================================

/**
 * Horizontal List Showcase
 */
@Preview
@Composable
fun DragDropHorizontalShowcase() {
    var items by remember { mutableStateOf(getNumberedItems(6)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DragDropShowcaseColors.Background)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Text(
                text = "Horizontal Reorder",
                color = DragDropShowcaseColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Drag cards left or right to reorder",
                color = DragDropShowcaseColors.TextSecondary,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Horizontal Draggable List
            DraggableList(
                items = items,
                onReorder = { newList ->
                    items = newList
                    println("📋 HORIZONTAL REORDERED: ${newList.map { it.number }}")
                },
                key = { it.id },
                orientation = DragOrientation.HORIZONTAL,
                style = DraggableListStyle(
                    itemSpacing = 12.dp,
                    draggedItemScale = 1.05f,
                    draggedItemElevation = 12.dp,
                    dropSlotColor = DragDropShowcaseColors.DropSlotFill,
                    dropSlotBorderColor = DragDropShowcaseColors.DropSlotBorder,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) { item, index, isDragging, dragModifier ->
                NumberedHorizontalCard(
                    item = item,
                    isDragging = isDragging,
                    dragModifier = dragModifier,
                )
            }
        }
    }
}

/**
 * Premium numbered card for horizontal list
 */
@Composable
fun NumberedHorizontalCard(
    item: NumberedItem,
    isDragging: Boolean,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val gradientColors = listOf(
        item.color,
        item.color.copy(alpha = 0.6f),
    )

    Card(
        modifier = modifier
            .width(120.dp)
            .height(140.dp)
            .then(dragModifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDragging) item.color else Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 12.dp else 4.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.number}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
