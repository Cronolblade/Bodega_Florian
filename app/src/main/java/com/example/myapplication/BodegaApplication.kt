package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.BodegaDatabase
import com.example.myapplication.data.ProductoRepository
import com.example.myapplication.data.SettingsManager
import com.example.myapplication.util.BackupManager

class BodegaApplication : Application() {
    val database by lazy { BodegaDatabase.getDatabase(this) }
    val repository by lazy {
        ProductoRepository(
            database.productoDao(),
            database.categoriaDao(),
            database.ventaDao() // <-- AÑADIMOS EL NUEVO DAO
        )
    }
    // --- NUEVA INSTANCIA DEL GESTOR DE PREFERENCIAS ---
    val settingsManager by lazy { SettingsManager(this) }

    val backupManager by lazy { BackupManager(this) }
}