package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.producto.Producto
import com.example.myapplication.data.categoria.Categoria
import com.example.myapplication.data.venta.Venta
import com.example.myapplication.data.venta.VentaDetalle
import com.example.myapplication.data.producto.ProductoDao
import com.example.myapplication.data.categoria.CategoriaDao
import com.example.myapplication.data.venta.VentaDao

@Database(entities = [Producto::class, Categoria::class, Venta::class, VentaDetalle::class], version = 7, exportSchema = false)
abstract class BodegaDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: BodegaDatabase? = null

        fun getDatabase(context: Context): BodegaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BodegaDatabase::class.java,
                    "bodega_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        // Método clave para forzar el cierre y la eliminación de la instancia en memoria.
        fun closeAndInvalidateInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}