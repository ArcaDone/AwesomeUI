package com.arcadone.awesomeui.expectactuals

import android.os.Build
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.Instant as JavaInstant
import java.time.ZoneId
import java.util.Calendar
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
actual fun NativeDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    minimumDate: LocalDate?,
) {
    val initialMillis = selectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
    val minMillis = minimumDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = minMillis?.let { utcTimeMillis >= it } ?: true
        },
    )

    val colorsDefault = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
    )
    DatePickerDialog(
        colors = colorsDefault,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            JavaInstant.ofEpochMilli(millis)
                        } else {
                            val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = millis

                            onDateSelected(
                                LocalDate(
                                    year = calendar.get(Calendar.YEAR),
                                    monthNumber = calendar.get(Calendar.MONTH) + 1, // Calendar.MONTH è 0-based
                                    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                                ),
                            )
                            return@let
                        }
                        val localDate = instant.atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(
                            LocalDate(
                                localDate.year,
                                localDate.monthValue,
                                localDate.dayOfMonth,
                            ),
                        )
                    }
                },
                content = {
                    Text("OK")
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annulla")
            }
        },
        content = {
            DatePicker(
                state = datePickerState,
                colors = colorsDefault,
                title = null,
            )
        },
    )
}
