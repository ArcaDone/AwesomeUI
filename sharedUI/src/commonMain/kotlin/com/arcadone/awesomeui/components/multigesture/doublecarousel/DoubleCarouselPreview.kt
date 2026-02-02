package com.arcadone.awesomeui.components.multigesture.doublecarousel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun DoubleCarouselPreview() {
    val sizeExampleIndex = listOf(900, 400, 600, 800, 350, 200, 500, 700, 900, 600)
    val sampleImages = remember {
        List(sizeExampleIndex.size) { index ->
            "https://picsum.photos/id/${(index * 10)}/${sizeExampleIndex[index]}/800"
        }
    }

    var carouselState by remember {
        mutableStateOf(
            DoubleCarouselState(
                images = sampleImages,
                currentIndex = 0,
                selectedIndices = emptySet(),
            ),
        )
    }

    MaterialTheme {
        DoubleCarousel(
            state = carouselState,
            onStateChange = { carouselState = it },
            config = DoubleCarouselConfig(
                maxVisibleInBottom = 5,
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
