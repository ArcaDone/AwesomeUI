package com.arcadone.awesomeui.expectactuals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
expect fun NativeDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    minimumDate: LocalDate? = null,
)

fun String.toLocalDate(): LocalDate? {
    return try {
        val parts = this.split("/")
        if (parts.size != 3) return null

        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null

        LocalDate(year, month, day)
    } catch (e: Exception) {
        println("toLocalDate Exception: ${e.message}")
        null
    }
}

fun LocalDate.toFormattedString(): String {
    val day = day.toString().padStart(2, '0')
    val month = month.number.toString().padStart(2, '0')
    val year = this.year.toString()
    return "$day/$month/$year"
}

@Preview
@Composable
private fun NativePickerPreview() {
    var selectedDate by remember {
        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    NativeDatePicker(
        selectedDate = selectedDate,
        onDateSelected = { date ->
            selectedDate = date
        },
        onDismissRequest = {  },
    )
}
