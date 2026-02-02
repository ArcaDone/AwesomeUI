package com.arcadone.awesomeui.components.multigesture.model

data class ImageModel(
    val id: Int = (1..12).flatMap { ('a'..'z') + ('0'..'9') }.shuffled().take(12)
        .joinToString("").hashCode(),
    val image: String,
)
