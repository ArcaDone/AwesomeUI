package com.arcadone.awesomeui.components.multigesture.doublecarousel

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

/**
 * Top carousel showing a single image at a time
 * maintaining the original aspect ratio
 *
 * @param pagerState Pager state for synchronization
 * @param images List of image URLs
 * @param selectedIndices Set of selected image indices
 * @param onImageTap Callback when current image is tapped (to select/deselect it)
 * @param config Carousel configuration
 * @param modifier Optional modifier
 */
@Composable
fun TopCarousel(
    pagerState: PagerState,
    images: List<String>,
    selectedIndices: Set<Int>,
    onImageTap: (Int) -> Unit,
    config: DoubleCarouselConfig,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        val isSelected = selectedIndices.contains(page)
        val imageUrl = images[page]

        var imageAspectRatio by remember(imageUrl) { mutableStateOf<Float?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (imageAspectRatio != null) {
                            Modifier.aspectRatio(imageAspectRatio!!, matchHeightConstraintsFirst = true)
                        } else {
                            Modifier.fillMaxSize()
                        },
                    )
                    .clip(RectangleShape)
                    .clickable { onImageTap(page) }
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = config.selectedBorderWidth * 2,
                                color = config.selectedBorderColor,
                                shape = RoundedCornerShape(16.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        Modifier
                            .clip(RoundedCornerShape(16.dp)),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image $page",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Success) {
                            val size = state.painter.intrinsicSize
                            if (size.width > 0 && size.height > 0) {
                                imageAspectRatio = size.width / size.height
                            }
                        }
                    },
                )
            }
        }
    }
}
