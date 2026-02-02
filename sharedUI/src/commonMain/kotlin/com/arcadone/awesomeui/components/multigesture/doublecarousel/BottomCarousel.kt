package com.arcadone.awesomeui.components.multigesture.doublecarousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Bottom carousel showing square thumbnails
 * with snap behavior and synchronization with the top carousel
 *
 * @param pagerState Top pager state for synchronization
 * @param images List of image URLs
 * @param selectedIndices Set of selected image indices
 * @param onImageClick Callback when an image is clicked (navigation only, not selection)
 * @param onCenterIndexChanged Callback when the centered image changes during scroll
 * @param config Carousel configuration
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun BottomCarousel(
    pagerState: PagerState,
    images: List<String>,
    selectedIndices: Set<Int>,
    onImageClick: (Int) -> Unit,
    onCenterIndexChanged: (Int) -> Unit,
    config: DoubleCarouselConfig,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()

    // Calculate which element is at the center during scroll
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2

            val result = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }?.index ?: pagerState.currentPage

            println("BottomCarousel: derivedStateOf - viewportCenter=$viewportCenter, centerIndex=$result, visibleItems=${layoutInfo.visibleItemsInfo.map { "${it.index}@${it.offset}" }}")
            result
        }
    }

    // Notify when the center element changes, with debounce to avoid rapid updates
    LaunchedEffect(Unit) {
        snapshotFlow { centerIndex }
            .distinctUntilChanged()
            .debounce(300) // Increased to 300ms to better handle fast scrolls
            .collect { index ->
                println("BottomCarousel: centerIndex settled at $index (pagerState.currentPage=${pagerState.currentPage})")
                if (index != pagerState.currentPage) {
                    onCenterIndexChanged(index)
                }
            }
    }

    // Synchronize scroll when the pager changes page (swipe top)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                println("BottomCarousel: pagerState.currentPage changed to $page, scrolling...")

                // Simply scroll to the index
                // contentPadding will handle centering
                listState.animateScrollToItem(
                    index = page,
                    scrollOffset = 0,
                )

                println("BottomCarousel: After scroll - firstVisibleItemIndex=${listState.firstVisibleItemIndex}")
            }
    }

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val density = LocalDensity.current
        val screenWidth = with(density) { maxWidth.toPx() }
        val thumbnailSize = with(density) { config.bottomThumbnailSize.toPx() }
        val startPadding = with(density) { ((screenWidth - thumbnailSize) / 2f).toDp() }

        println("BottomCarousel: BoxWithConstraints - screenWidth=$screenWidth, thumbnailSize=$thumbnailSize, startPadding=$startPadding")

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(start = startPadding, end = startPadding),
            horizontalArrangement = Arrangement.spacedBy(config.spacing),
            flingBehavior = snapBehavior,
        ) {
            itemsIndexed(images) { index, imageUrl ->
                ThumbnailItem(
                    imageUrl = imageUrl,
                    isSelected = selectedIndices.contains(index),
                    onClick = {
                        println("BottomCarousel: thumbnail $index clicked, scrolling to center it")

                        // First center the clicked element, then navigate
                        scope.launch {
                            listState.animateScrollToItem(
                                index = index,
                                scrollOffset = 0,
                            )
                            // Then navigate
                            onImageClick(index)
                        }
                    },
                    config = config,
                )
            }
        }
    }
}

@Composable
private fun ThumbnailItem(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    config: DoubleCarouselConfig,
) {
    Box(
        modifier = Modifier
            .size(config.bottomThumbnailSize)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) config.selectedBorderWidth else 0.dp,
                color = if (isSelected) config.selectedBorderColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Thumbnail",
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop,
        )
    }
}
