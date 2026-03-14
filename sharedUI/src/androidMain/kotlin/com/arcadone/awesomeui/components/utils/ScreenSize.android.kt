package com.arcadone.awesomeui.components.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenWidth(): Float = LocalConfiguration.current.screenWidthDp.dp.value

@Composable
actual fun getScreenHeight(): Float = LocalConfiguration.current.screenHeightDp.dp.value
