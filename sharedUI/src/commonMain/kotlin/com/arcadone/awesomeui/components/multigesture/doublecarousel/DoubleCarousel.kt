package com.arcadone.awesomeui.components.multigesture.doublecarousel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Main component that combines top and bottom carousel
 * with bidirectional synchronization
 *
 * @param state Carousel state
 * @param onStateChange Callback when state changes
 * @param config Carousel configuration
 * @param modifier Optional modifier
 */
@Composable
fun DoubleCarousel(
    state: DoubleCarouselState,
    onStateChange: (DoubleCarouselState) -> Unit,
    config: DoubleCarouselConfig = DoubleCarouselConfig(),
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.images.size },
    )

    // Flags to avoid synchronization loops
    var isUpdatingFromPager by remember { mutableStateOf(false) }
    var isUpdatingFromState by remember { mutableStateOf(false) }

    // Synchronize state when pager changes page (swipe top)
    LaunchedEffect(pagerState.currentPage) {
        if (!isUpdatingFromState && pagerState.currentPage != state.currentIndex) {
            println("DoubleCarousel: pagerState.currentPage = ${pagerState.currentPage}, updating state")
            isUpdatingFromPager = true
            onStateChange(state.updateCurrentIndex(pagerState.currentPage))
            isUpdatingFromPager = false
        }
    }

    // Synchronize pager when state changes externally (scroll bottom or click)
    LaunchedEffect(state.currentIndex) {
        if (!isUpdatingFromPager && pagerState.currentPage != state.currentIndex) {
            println("DoubleCarousel: state.currentIndex = ${state.currentIndex}, scrolling pager to ${state.currentIndex}")
            isUpdatingFromState = true
            pagerState.animateScrollToPage(state.currentIndex)
            isUpdatingFromState = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top carousel - handles selection
        TopCarousel(
            pagerState = pagerState,
            images = state.images,
            selectedIndices = state.selectedIndices,
            onImageTap = { index ->
                println("DoubleCarousel: TopCarousel tapped, toggling selection for $index")
                onStateChange(state.toggleSelection(index))
            },
            config = config,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        // Bottom carousel - handles navigation only
        BottomCarousel(
            pagerState = pagerState,
            images = state.images,
            selectedIndices = state.selectedIndices,
            onImageClick = { index ->
                println("DoubleCarousel: BottomCarousel clicked, navigating to $index")
                // Navigation only, NO selection
                onStateChange(state.updateCurrentIndex(index))
            },
            onCenterIndexChanged = { index ->
                // Update when free scroll brings an element to center
                // BUT only if we're not already updating from other sources
                if (!isUpdatingFromPager && !isUpdatingFromState && index != state.currentIndex) {
                    println("DoubleCarousel: BottomCarousel centerIndex = $index, updating state")
                    onStateChange(state.updateCurrentIndex(index))
                }
            },
            config = config,
            modifier = Modifier
                .fillMaxWidth()
                .height(config.bottomThumbnailSize + 16.dp)
                .padding(vertical = 8.dp),
        )
    }
}
