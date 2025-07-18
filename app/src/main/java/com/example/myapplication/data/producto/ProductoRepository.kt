package com.example.myapplication.data.producto

import com.example.myapplication.data.producto.Producto
import com.example.myapplication.data.producto.ProductoDao
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDao: ProductoDao
) {
    val todosLosProductos: Flow<List<Producto>> = productoDao.obtenerTodos()

    suspend fun findByBarcode(barcode: String): Producto? {
        return productoDao.findByBarcode(barcode)
    }

    fun obtenerProductosConStockBajo(umbral: Int): Flow<List<Producto>> {
        return productoDao.obtenerProductosConStockBajo(umbral)
    }

    fun obtenerProductosProximosAVencer(dias: Int): Flow<List<Producto>> {
        return productoDao.obtenerProductosProximosAVencer(dias)
    }

    suspend fun insertar(producto: Producto) {
        productoDao.insertar(producto)
    }

    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)
    }

    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)
    }

    fun obtenerPorId(id: Int): Flow<Producto?> {
        return productoDao.obtenerPorId(id)
    }
}