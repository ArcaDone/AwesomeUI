package com.arcadone.shared.timer

data class TimerState(
    val timeRemaining: Int, // in seconds
    val totalTime: Int,
    val isRest: Boolean,
)
