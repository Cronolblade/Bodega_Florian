package com.example.myapplication.data.venta

import com.example.myapplication.data.venta.Venta
import com.example.myapplication.data.venta.VentaDetalle
import com.example.myapplication.data.venta.VentaDao
import com.example.myapplication.data.producto.ProductoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.room.Transaction
import com.example.myapplication.data.reportes.GananciaPorDia
import com.example.myapplication.data.reportes.VentasPorDia

class VentaRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao
) {
    val ventasTotalesPorDia: Flow<List<VentasPorDia>> = ventaDao.obtenerVentasTotalesPorDia()
    val gananciasTotalesPorDia: Flow<List<GananciaPorDia>> = ventaDao.obtenerGananciasTotalesPorDia()

    @Transaction
    suspend fun realizarVenta(venta: Venta, detalles: List<VentaDetalle>) {
        val idVenta = ventaDao.insertarVenta(venta)
        val detallesConId = detalles.map { it.copy(idVenta = idVenta.toInt()) }
        ventaDao.insertarVentaDetalles(detallesConId)

        detallesConId.forEach { detalle ->
            val producto = productoDao.obtenerPorId(detalle.idProducto).first()
            if (producto != null) {
                val nuevoStock = producto.stock - detalle.cantidad
                productoDao.actualizar(producto.copy(stock = nuevoStock))
            }
        }
    }
}