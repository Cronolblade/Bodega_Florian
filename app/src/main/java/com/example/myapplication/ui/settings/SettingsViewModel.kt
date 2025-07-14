package com.example.myapplication.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.BodegaDatabase
import com.example.myapplication.data.SettingsManager
import com.example.myapplication.data.Theme
import com.example.myapplication.util.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// El estado ahora incluye la URI pendiente de restauración
data class SettingsUiState(
    val currentTheme: Theme = Theme.SYSTEM,
    val userMessage: String? = null,
    val pendingRestoreUri: Uri? = null // URI del backup esperando confirmación
)

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val backupManager: BackupManager,
    private val database: BodegaDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.theme.collect { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsManager.setTheme(theme)
        }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // --- LÓGICA DE BACKUP/RESTORE MEJORADA ---

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                BodegaDatabase.closeAndInvalidateInstance()
                backupManager.createBackup(uri)
            }
            val message = if (success) "Copia de seguridad creada con éxito." else "Error al crear la copia de seguridad."
            _uiState.update { it.copy(userMessage = message) }
        }
    }

    // El usuario selecciona un archivo. Guardamos la URI y esperamos confirmación.
    fun onRestoreFileSelected(uri: Uri) {
        _uiState.update { it.copy(pendingRestoreUri = uri) }
    }

    // El usuario cancela el diálogo de confirmación.
    fun onRestoreCancelled() {
        _uiState.update { it.copy(pendingRestoreUri = null) }
    }

    // El usuario confirma. Ahora sí, procedemos con la restauración.
    fun onRestoreConfirmed() {
        _uiState.value.pendingRestoreUri?.let { uri ->
            viewModelScope.launch {
                val success = withContext(Dispatchers.IO) {
                    BodegaDatabase.closeAndInvalidateInstance()
                    backupManager.restoreBackup(uri)
                }
                val message = if (success) "Restauración completada. Por favor, reinicia la aplicación." else "Error al restaurar."
                // Limpiamos la URI pendiente y mostramos el mensaje final
                _uiState.update { it.copy(pendingRestoreUri = null, userMessage = message) }
            }
        }
    }
}

class SettingsViewModelFactory(
    private val settingsManager: SettingsManager,
    private val backupManager: BackupManager,
    private val database: BodegaDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager, backupManager, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}