package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Producto>>

    // --- CORRECCIÓN ---
    // La función ahora puede devolver null si no se encuentra el producto.
    @Query("SELECT * FROM productos WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Producto?>

    @Query("SELECT * FROM productos WHERE codigoBarras = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): Producto?

    @Query("SELECT * FROM productos WHERE stock <= :stockMinimo")
    fun obtenerProductosConStockBajo(stockMinimo: Int): Flow<List<Producto>>

    @Query("""
    SELECT * FROM productos
    WHERE fechaVencimiento IS NOT NULL AND (fechaVencimiento BETWEEN strftime('%s', 'now') * 1000 AND (strftime('%s', 'now', '+' || :dias || ' days') * 1000))
    ORDER BY fechaVencimiento ASC
""")
    fun obtenerProductosProximosAVencer(dias: Int): Flow<List<Producto>>
}