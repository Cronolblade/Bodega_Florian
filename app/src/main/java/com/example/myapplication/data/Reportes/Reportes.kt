package com.example.myapplication.data.reportes

data class VentasPorDia(
    val dia: String, // "01/07", "02/07", etc.
    val total: Float
)

data class GananciaPorDia(
    val dia: String,
    val totalGanancia: Float
)