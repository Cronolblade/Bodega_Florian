package com.example.myapplication.data

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val categoriaDao: CategoriaDao,
    private val ventaDao: VentaDao // <-- NUEVO DAO INYECTADO
){   // --- CORRECCIÓN ---
    // Room ya ejecuta las consultas Flow en un hilo de fondo.
    val todosLosProductos: Flow<List<Producto>> = productoDao.obtenerTodos()
    val todasLasCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()
    val ventasTotalesPorDia: Flow<List<VentasPorDia>> = ventaDao.obtenerVentasTotalesPorDia()
    val gananciasTotalesPorDia: Flow<List<GananciaPorDia>> = ventaDao.obtenerGananciasTotalesPorDia()

    suspend fun findByBarcode(barcode: String): Producto?{
        return productoDao.findByBarcode(barcode)
    }

    fun obtenerProductosConStockBajo(umbral: Int): Flow<List<Producto>>{
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

    suspend fun insertarCategoria(categoria: Categoria) {
        categoriaDao.insertar(categoria)
    }

    suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.actualizar(categoria)
    }

    suspend fun eliminarCategoria(categoria: Categoria) {
        categoriaDao.eliminar(categoria)
    }

    @Transaction
    suspend fun realizarVenta(venta: Venta, detalles: List<VentaDetalle>) {
        // 1. Guardar la venta y sus detalles.
        val idVenta = ventaDao.insertarVenta(venta)
        val detallesConId = detalles.map { it.copy(idVenta = idVenta.toInt()) }
        ventaDao.insertarVentaDetalles(detallesConId)

        // 2. Por cada detalle, buscar el producto original y actualizar su stock.
        detallesConId.forEach { detalle ->
            // Usamos .first() para obtener el valor actual del Flow.
            // La base de datos es la fuente de la verdad para el stock actual.
            val producto = productoDao.obtenerPorId(detalle.idProducto).first()
            if (producto != null) {
                val nuevoStock = producto.stock - detalle.cantidad
                productoDao.actualizar(producto.copy(stock = nuevoStock))
            }
        }
    }
}