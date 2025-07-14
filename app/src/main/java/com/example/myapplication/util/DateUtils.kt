package com.example.myapplication.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Formatea un timestamp UTC en milisegundos a un string amigable en ESPAÑOL.
 * Esta función es segura para zonas horarias.
 */
fun Long.toFriendlyDateString(): String {
    val spanishLocale = Locale("es", "ES")
    val utcTimeZone = TimeZone.getTimeZone("UTC")

    // --- LÓGICA DE COMPARACIÓN USANDO CALENDARIOS UTC ---

    // Calendario para "AHORA" en UTC
    val now = Calendar.getInstance(utcTimeZone)

    // Calendario para la fecha a formatear, también en UTC
    val dateToFormat = Calendar.getInstance(utcTimeZone).apply { timeInMillis = this@toFriendlyDateString }

    // Reseteamos la hora de "AHORA" a la medianoche para comparar solo los días
    val todayStart = Calendar.getInstance(utcTimeZone).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Reseteamos la hora de la fecha a formatear por si acaso
    dateToFormat.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diff = dateToFormat.timeInMillis - todayStart.timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when (days) {
        0L -> "Hoy"
        -1L -> "Ayer"
        in 1..6 -> {
            // Para obtener el nombre del día, sí necesitamos un formateador
            SimpleDateFormat("EEEE", spanishLocale).apply {
                timeZone = utcTimeZone
            }.format(this).replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
        }
        else -> {
            // Caso por defecto: formatear la fecha
            SimpleDateFormat("dd/MM/yyyy", spanishLocale).apply {
                timeZone = utcTimeZone
            }.format(this)
        }
    }
}