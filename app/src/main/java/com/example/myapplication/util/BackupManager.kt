package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {

    private fun getDbFile(): File? {
        return context.getDatabasePath("bodega_database")
    }

    fun createBackup(destinationUri: Uri): Boolean {
        val dbFile = getDbFile()
        if (dbFile == null || !dbFile.exists()) {
            return false
        }

        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                dbFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreBackup(sourceUri: Uri): Boolean {
        val mainDbFile = getDbFile() ?: return false
        val shmFile = File(mainDbFile.parent, mainDbFile.name + "-shm")
        val walFile = File(mainDbFile.parent, mainDbFile.name + "-wal")

        // Borramos los archivos auxiliares para evitar que Room intente reconstruir el estado antiguo.
        if (shmFile.exists()) {
            shmFile.delete()
        }
        if (walFile.exists()) {
            walFile.delete()
        }

        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(mainDbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getSuggestedBackupName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        return "bodega_backup_${dateFormat.format(Date())}.db"
    }
}