package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: Categoria)

    @Update
    suspend fun actualizar(categoria: Categoria) // CORREGIDO: categiria -> categoria

    @Delete
    suspend fun eliminar(categoria: Categoria)

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Categoria>>
}