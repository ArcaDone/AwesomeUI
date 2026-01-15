package com.arcadone.awesomeui.components.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Format time from seconds to mm:ss format
 */
fun Int.formatTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

/**
 * Format time from minutes to "Xh Ym" format
 */
fun Int.formatTimeFromMinutes(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

/**
 * Format volume to display in compact form (e.g., 14.5k for 14500)
 */
fun Double.formatVolume(): String = when {
    this >= 1000 -> {
        val value = (this / 1000 * 10).toInt() / 10.0
        "${value}k"
    }
    else -> this.toInt().toString()
}

/**
 * Format date from timestamp to dd/MM/yyyy
 */
fun Long.formatDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
}

/**
 * Format date from timestamp to dd/MM (short format)
 */
fun Long.formatShortDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.dayOfMonth}/${date.monthNumber}"
}

/**
 * Format month and year from timestamp
 */
fun Long.formatMonthYear(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = listOf(
        "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
        "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre",
    )
    return "${months[date.monthNumber - 1]} ${date.year}"
}
