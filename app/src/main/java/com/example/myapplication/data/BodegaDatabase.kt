package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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