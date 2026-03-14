package com.arcadone.awesomeui.components.utils

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun getScreenWidth(): Float {
    val mainScreenBounds = UIScreen.mainScreen.bounds
    val screenWidthPoints = CGRectGetWidth(mainScreenBounds)
    return screenWidthPoints.toFloat()
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun getScreenHeight(): Float {
    val mainScreenBounds = UIScreen.mainScreen.bounds
    val screenWidthPoints = CGRectGetHeight(mainScreenBounds)
    return screenWidthPoints.toFloat()
}
