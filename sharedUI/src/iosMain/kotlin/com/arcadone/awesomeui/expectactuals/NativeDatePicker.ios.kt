package com.arcadone.awesomeui.expectactuals

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.CoreGraphics.CGRectGetHeight
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSLocale
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIScreen
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun NativeDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    minimumDate: LocalDate?,
) {
    var tempSelectedDate by remember {
        mutableStateOf(selectedDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    println("tempSelectedDate: $tempSelectedDate")
    val calendar = NSCalendar.currentCalendar
    val dialogBackgroundColor = MaterialTheme.colorScheme.surface
    val tintColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        text = {
            val initialDate = tempSelectedDate.let {
                val components = NSDateComponents().apply {
                    year = it.year.toLong()
                    month = it.month.ordinal.toLong() + 1
                    day = it.day.toLong()
                }
                calendar.dateFromComponents(components) ?: NSDate()
            }

            val minDate = minimumDate?.let {
                val components = NSDateComponents().apply {
                    year = it.year.toLong()
                    month = it.month.ordinal.toLong() + 1
                    day = it.day.toLong()
                }
                calendar.dateFromComponents(components)
            }
            UIKitView(
                factory = {
                    UIDatePicker().apply {
                        locale = NSLocale("it-IT")
                        datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleInline
                        date = initialDate
                        backgroundColor = dialogBackgroundColor.toUIColor()
                        this.tintColor = tintColor.toUIColor()
                        opaque = true
                        minDate?.let { this.minimumDate = it }
                        addTarget(
                            target = object : NSObject() {
                                @ObjCAction
                                fun dateChanged() {
                                    println("dateChanged")
                                    val selectedNSDate = this@apply.date
                                    val components = calendar.components(
                                        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
                                        selectedNSDate,
                                    )
                                    println("components: $components && $selectedNSDate")
                                    tempSelectedDate = LocalDate(
                                        year = components.year.toInt(),
                                        monthNumber = components.month.toInt(),
                                        dayOfMonth = components.day.toInt(),
                                    )
                                }
                            },
                            action = NSSelectorFromString("dateChanged"),
                            forControlEvents = UIControlEventValueChanged,
                        )
                    }
                },
                update = { datePicker ->
                    datePicker.date = initialDate
                    minDate?.let { datePicker.minimumDate = it }
                },
                modifier = Modifier.fillMaxWidth()
                    .defaultMinSize(minHeight = (getScreenHeight() / 2).dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(tempSelectedDate)
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annulla")
            }
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun getScreenHeight(): Float {
    val mainScreenBounds = UIScreen.mainScreen.bounds
    val screenWidthPoints = CGRectGetHeight(mainScreenBounds)
    return screenWidthPoints.toFloat()
}

fun Color.toUIColor(): UIColor = UIColor(
    red = this.red.toDouble(),
    green = this.green.toDouble(),
    blue = this.blue.toDouble(),
    alpha = this.alpha.toDouble(),
)
