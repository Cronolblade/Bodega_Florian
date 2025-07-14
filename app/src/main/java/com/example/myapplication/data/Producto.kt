package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "productos", indices = [Index(value = ["codigoBarras"], unique = true)]) // Le decimos a Room que esta clase es una tabla
data class Producto (
    @PrimaryKey(autoGenerate = true) // Cada producto tendrá un ID único y autoincremental
    val id: Int = 0,

    val nombre: String,
    val categoria: String,
    val precioCompra: Double,
    val precioVenta: Double,
    val stock: Int,
    val fechaCompra: Long,
    val fechaVencimiento: Long? = null,
    val imagenUri: String? = null,
    val codigoBarras: String? = null
)
