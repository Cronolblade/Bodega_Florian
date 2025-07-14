package com.example.myapplication.util

import java.text.Normalizer

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun String.normalizeForComparison(): String{
    //1. Quitar tildes y diacríticos (ej: "Lácteos" -> "Lacteos")
    val unaccented = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(REGEX_UNACCENT,"")

    //2. Convertir a minúsculas y quitar espacios al inicio y al final
    return unaccented.lowercase().trim()
}