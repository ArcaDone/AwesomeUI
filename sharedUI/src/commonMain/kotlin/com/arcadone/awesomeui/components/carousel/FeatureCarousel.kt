package com.arcadone.awesomeui.components.carousel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.arcadone.awesomeui.components.utils.getScreenHeight
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * Data model representing a single image in the carousel.
 *
 * @property url URL of the image to load (supports Coil/AsyncImage).
 * @property description Text description of the image, used as contentDescription for accessibility.
 */
data class CarouselImage(val url: String, val description: String)

/**
 * Main component that combines a text header with a 3D image carousel.
 *
 * Displays a title with a gradient-highlighted word, an optional subtitle,
 * and a [Carousel3D] with automatic auto-scroll every 4 seconds.
 * The background is dark (#0A0A0A) with two decorative radial circles (purple and blue).
 *
 * @param title Full title text. If empty, the title is not rendered.
 * @param highlightedWord Word within [title] that will be highlighted with a blue-violet gradient.
 *   Must be an exact substring of [title].
 * @param subtitle Subtitle text shown below the title. If empty, it is not rendered.
 * @param images List of [CarouselImage] to display in the carousel. Must contain at least one element.
 * @param modifier Optional [Modifier] applied to the outer container.
 * @param fractionHeight Divisor of screen height used to calculate the component height.
 *   For example, `3` corresponds to `screenHeight / 3`. Default: `3`.
 */
@Composable
fun FeatureCarousel(
    title: String = "",
    highlightedWord: String = "",
    subtitle: String = "",
    images: List<CarouselImage>,
    modifier: Modifier = Modifier,
    fractionHeight: Int = 2,
) {
    var currentIndex by remember { mutableStateOf(images.size / 2) }

    val screenH = getScreenHeight()
    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentIndex = (currentIndex + 1) % images.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(screenH.toDouble().dp / fractionHeight)
            .background(Color(0xFF0A0A0A)),
    ) {
        // Gradient background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f),
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF805AD5),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = size.minDimension * 0.6f,
                ),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF007BFF),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.minDimension * 0.6f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            if (title.isNotEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        val parts = title.split(highlightedWord)
                        append(parts.getOrNull(0) ?: "")

                        withStyle(
                            style = SpanStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6),
                                        Color(0xFFA855F7),
                                    ),
                                ),
                            ),
                        ) {
                            append(highlightedWord)
                        }

                        append(parts.getOrNull(1) ?: "")
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp),
                )
            }
            // Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center,
            ) {
                Carousel3D(
                    images = images,
                    currentIndex = currentIndex,
                    onPrevious = {
                        currentIndex = (currentIndex - 1 + images.size) % images.size
                    },
                    onNext = {
                        currentIndex = (currentIndex + 1) % images.size
                    },
                )
            }
        }
    }
}

/**
 * 3D carousel that shows at most three visible cards at a time:
 * the center card (in the foreground) and the two adjacent ones (scaled and blurred on the sides).
 *
 * Cards beyond distance 1 from the current index are not composed.
 * Previous/Next navigation buttons are overlaid on the sides of the carousel.
 *
 * @param images Full list of [CarouselImage] to display.
 * @param currentIndex Index of the image currently centered in the carousel.
 * @param onPrevious Callback invoked when the user presses the "previous" button.
 * @param onNext Callback invoked when the user presses the "next" button.
 */
@Composable
fun Carousel3D(
    images: List<CarouselImage>,
    currentIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 60.dp),
            contentAlignment = Alignment.Center,
        ) {
            images.forEachIndexed { index, image ->
                val offset = index - currentIndex
                val total = images.size

                var pos = (offset + total) % total
                if (pos > total / 2) {
                    pos -= total
                }

                val isCenter = pos == 0
                val isAdjacent = abs(pos) == 1
                val isVisible = abs(pos) <= 1

                if (isVisible) {
                    key(index) {
                        CarouselCard(
                            image = image,
                            position = pos,
                            isCenter = isCenter,
                            isAdjacent = isAdjacent,
                        )
                    }
                }
            }
        }

        // Navigation buttons
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .zIndex(100f),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous",
                tint = Color.Black,
                modifier = Modifier.size(32.dp),
            )
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .zIndex(100f),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = Color.Black,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/**
 * Single carousel card with 3D animations for translation, scale, rotation, and opacity.
 *
 * All visual properties (translationX, scale, rotationY, alpha) are animated via
 * [animateFloatAsState] with a damped [spring]. Non-center cards receive a [blur]
 * of 3dp to enhance the depth effect.
 *
 * Target values by position:
 * | Property      | Center (pos=0) | Adjacent (|pos|=1) |
 * |---------------|----------------|--------------------|
 * | scale         | 1.0            | 0.85               |
 * | alpha         | 1.0            | 0.5                |
 * | rotationY     | 0°             | ±15°               |
 * | translationX  | 0px            | ±180px             |
 * | zIndex        | 10             | 5                  |
 *
 * @param image Image data to display in the card.
 * @param position Relative position from the center card:
 *   `0` = center, `-1` = left, `1` = right.
 * @param isCenter `true` if this card is currently selected (position 0).
 * @param isAdjacent `true` if this card is immediately next to the center (|position| == 1).
 */
@Composable
fun CarouselCard(
    image: CarouselImage,
    position: Int,
    isCenter: Boolean,
    isAdjacent: Boolean,
) {
    val density = LocalDensity.current

    val targetTranslationX = position * 180f
    val targetScale = when {
        isCenter -> 1f
        isAdjacent -> 0.85f
        else -> 0.7f
    }
    val targetAlpha = when {
        isCenter -> 1f
        isAdjacent -> 0.5f
        else -> 0f
    }
    val targetRotationY = position * -15f

    val translationX by animateFloatAsState(
        targetValue = targetTranslationX,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f,
        ),
    )

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f,
        ),
    )

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f,
        ),
    )

    val rotationY by animateFloatAsState(
        targetValue = targetRotationY,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f,
        ),
    )

    // Z-index
    val zIndexValue = if (isCenter) {
        10f
    } else if (isAdjacent) {
        5f
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .size(width = 220.dp, height = 360.dp)
            .zIndex(zIndexValue)
            .graphicsLayer {
                this.translationX = translationX
                this.scaleX = scale
                this.scaleY = scale
                this.rotationY = rotationY
                this.alpha = alpha
                cameraDistance = 12f * density.density
            }
            .then(
                if (!isCenter) Modifier.blur(3.dp) else Modifier,
            ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 16.dp,
            color = Color.Transparent,
        ) {
            AsyncImage(
                model = image.url,
                contentDescription = image.description,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview
@Composable
fun FeatureCarouselPreview() {
    val images = remember {
        listOf(
            CarouselImage(
                url = "https://images.unsplash.com/photo-1504051771394-dd2e66b2e08f?w=900",
                description = "Image 1",
            ),
            CarouselImage(
                url = "https://images.unsplash.com/photo-1526510747491-58f928ec870f?w=900",
                description = "Image 2",
            ),
            CarouselImage(
                url = "https://images.unsplash.com/photo-1581403341630-a6e0b9d2d257?w=900",
                description = "Image 3",
            ),
            CarouselImage(
                url = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900",
                description = "Image 4",
            ),
            CarouselImage(
                url = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900",
                description = "Image 5",
            ),
        )
    }

    FeatureCarousel(
        title = "Create your next Outfit",
        highlightedWord = "Outfit",
        images = images,
    )
}
