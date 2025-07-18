package com.example.myapplication.data.reportes

import com.example.myapplication.data.reportes.GananciaPorDia
import com.example.myapplication.data.reportes.VentasPorDia
import com.example.myapplication.data.venta.VentaDao
import kotlinx.coroutines.flow.Flow

class ReporteRepository(
    private val ventaDao: VentaDao
) {
    val ventasTotalesPorDia: Flow<List<VentasPorDia>> = ventaDao.obtenerVentasTotalesPorDia()
    val gananciasTotalesPorDia: Flow<List<GananciaPorDia>> = ventaDao.obtenerGananciasTotalesPorDia()
}