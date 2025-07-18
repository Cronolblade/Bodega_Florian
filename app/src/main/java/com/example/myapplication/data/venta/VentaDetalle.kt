package com.example.myapplication.data.venta

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.myapplication.data.producto.Producto

// La clave primaria es una combinación de idVenta e idProducto
@Entity(
    tableName = "venta_detalles",
    primaryKeys = ["idVenta", "idProducto"],
    foreignKeys = [
        ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["idVenta"],
            onDelete = ForeignKey.CASCADE // Si se borra una Venta, se borran sus detalles
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["idProducto"],
            onDelete = ForeignKey.RESTRICT // No se puede borrar un Producto si está en una venta
        )
    ]
)
data class VentaDetalle(
    val idVenta: Int,
    val idProducto: Int,
    val cantidad: Int,
    val precioVentaUnitario: Double, // Guardamos el precio al que se vendió, por si cambia en el futuro
    val precioCompraUnitario: Double
)