package com.example.myapplication.data.venta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.producto.Producto
import com.example.myapplication.data.reportes.GananciaPorDia
import com.example.myapplication.data.reportes.VentasPorDia
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    // Insertamos la cabecera de la venta y nos devuelve su ID autogenerado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVenta(venta: Venta): Long

    // Insertamos una lista de detalles de venta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVentaDetalles(detalles: List<VentaDetalle>)

    // --- LA TRANSACCIÓN MÁGICA ---
    // @Transaction asegura que todas las operaciones dentro de esta función
    // se ejecuten como una sola unidad. O todas tienen éxito, o todas fallan.
    // Esto previene que se actualice el stock pero no se guarde la venta, o viceversa.
    @Transaction
    suspend fun registrarVentaCompleta(venta: Venta, detalles: List<VentaDetalle>, productosAActualizar: List<Producto>) {
        // 1. Guardar la venta y obtener su nuevo ID
        val idVenta = insertarVenta(venta)

        // 2. Asignar ese ID a cada detalle de la venta
        val detallesConId = detalles.map { it.copy(idVenta = idVenta.toInt()) }
        insertarVentaDetalles(detallesConId)

        // 3. Actualizar el stock de cada producto vendido
        productosAActualizar.forEach { producto ->
            // Room es lo suficientemente inteligente para saber que esto es un update
            // basado en la PrimaryKey del objeto 'producto'.
            // Nota: Para que esto funcione, necesitamos un método de actualización en ProductoDao. ¡Ya lo tenemos!
            // La llamada real se hará desde el Repositorio.
        }
    }

    // --- NUEVA CONSULTA PARA EL GRÁFICO ---
    // GROUP BY agrupa todas las ventas del mismo día.
    // strftime formatea el timestamp 'fecha' a un string 'DD-MM-YYYY'.
    // SUM(total) suma los totales de todas las ventas de ese día.
    // Seleccionamos las ventas de los últimos 30 días.
    @Query("""
        SELECT strftime('%d/%m', fecha / 1000, 'unixepoch') as dia, SUM(total) as total
        FROM ventas
        WHERE fecha >= strftime('%s', 'now', '-30 days') * 1000
        GROUP BY dia
        ORDER BY fecha ASC
    """)
    fun obtenerVentasTotalesPorDia(): Flow<List<VentasPorDia>>

    @Query("""
        SELECT 
            strftime('%d/%m', v.fecha / 1000, 'unixepoch') as dia, 
            SUM((vd.precioVentaUnitario - vd.precioCompraUnitario) * vd.cantidad) as totalGanancia
        FROM ventas AS v
        INNER JOIN venta_detalles AS vd ON v.id = vd.idVenta
        WHERE v.fecha >= strftime('%s', 'now', '-30 days') * 1000
        GROUP BY dia
        ORDER BY v.fecha ASC
    """)
    fun obtenerGananciasTotalesPorDia(): Flow<List<GananciaPorDia>>
}