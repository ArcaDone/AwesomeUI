package com.arcadone.awesomeui.components.multigesture.doublecarousel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Double carousel state
 *
 * @param images List of image URLs
 * @param currentIndex Index of the currently displayed image
 * @param selectedIndices Set of selected image indices
 */
data class DoubleCarouselState(val images: List<String> = emptyList(), val currentIndex: Int = 0, val selectedIndices: Set<Int> = emptySet()) {
    fun isSelected(index: Int): Boolean = selectedIndices.contains(index)

    fun toggleSelection(index: Int): DoubleCarouselState {
        val newSelected = if (isSelected(index)) {
            selectedIndices - index
        } else {
            selectedIndices + index
        }
        return copy(selectedIndices = newSelected)
    }

    fun updateCurrentIndex(index: Int): DoubleCarouselState = copy(currentIndex = index.coerceIn(0, images.lastIndex.coerceAtLeast(0)))
}

/**
 * Double carousel configuration
 *
 * @param maxVisibleInBottom Maximum number of visible images in the bottom carousel
 * @param bottomThumbnailSize Size of square thumbnails in the bottom carousel
 * @param selectedBorderColor Border color for selected images
 * @param selectedBorderWidth Border width for selected images
 * @param checkmarkColor Checkmark color for selected images
 * @param spacing Spacing between thumbnails in the bottom carousel
 */
data class DoubleCarouselConfig(
    val maxVisibleInBottom: Int = 5,
    val bottomThumbnailSize: Dp = 80.dp,
    val selectedBorderColor: Color = Color(0xFF3B82F6),
    val selectedBorderWidth: Dp = 3.dp,
    val checkmarkColor: Color = Color(0xFF3B82F6),
    val spacing: Dp = 8.dp,
)
