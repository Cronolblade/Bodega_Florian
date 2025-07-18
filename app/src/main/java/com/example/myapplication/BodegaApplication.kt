package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.database.BodegaDatabase
import com.example.myapplication.data.producto.ProductoRepository
import com.example.myapplication.data.categoria.CategoriaRepository
import com.example.myapplication.data.venta.VentaRepository
import com.example.myapplication.data.reportes.ReporteRepository
import com.example.myapplication.data.settings.SettingsManager
import com.example.myapplication.util.BackupManager

class BodegaApplication : Application() {
    val database by lazy { BodegaDatabase.getDatabase(this) }
    val productoRepository by lazy { ProductoRepository(database.productoDao()) }
    val categoriaRepository by lazy { CategoriaRepository(database.categoriaDao()) }
    val ventaRepository by lazy { VentaRepository(database.ventaDao(), database.productoDao()) }
    val reporteRepository by lazy { ReporteRepository(database.ventaDao()) }
    // --- NUEVA INSTANCIA DEL GESTOR DE PREFERENCIAS ---
    val settingsManager by lazy { SettingsManager(this) }
    val backupManager by lazy { BackupManager(this) }
}