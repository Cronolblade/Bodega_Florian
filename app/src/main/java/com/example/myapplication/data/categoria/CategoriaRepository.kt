package com.example.myapplication.data.categoria

import com.example.myapplication.data.categoria.Categoria
import com.example.myapplication.data.categoria.CategoriaDao
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(
    private val categoriaDao: CategoriaDao
) {
    val todasLasCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()

    suspend fun insertarCategoria(categoria: Categoria) {
        categoriaDao.insertar(categoria)
    }

    suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.actualizar(categoria)
    }

    suspend fun eliminarCategoria(categoria: Categoria) {
        categoriaDao.eliminar(categoria)
    }
}