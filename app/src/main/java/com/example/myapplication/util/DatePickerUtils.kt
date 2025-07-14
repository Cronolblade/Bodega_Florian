package com.example.myapplication.util

import android.app.DatePickerDialog
import android.content.Context
import java.util.*

fun showDatePicker(
    context: Context,
    initialTimestamp: Long?, // Ahora recibe un Long nulable
    onDateSelected: (Long) -> Unit
) {
    // 1. Creamos el calendario inicial en UTC para mostrarlo correctamente.
    val initialCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    if (initialTimestamp != null) {
        initialCalendar.timeInMillis = initialTimestamp
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // 2. Cuando el usuario confirma, construimos un nuevo timestamp UTC desde cero.
            val selectedUtcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            selectedUtcCalendar.clear()
            selectedUtcCalendar.set(year, month, dayOfMonth)

            onDateSelected(selectedUtcCalendar.timeInMillis)
        },
        initialCalendar.get(Calendar.YEAR),
        initialCalendar.get(Calendar.MONTH),
        initialCalendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}