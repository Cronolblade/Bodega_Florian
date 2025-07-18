package com.example.myapplication.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.settings.Theme
import com.example.myapplication.util.BackupManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.sqlite3"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.createBackup(it) }
        }
    )

    // El lanzador ahora solo notifica al ViewModel que un archivo ha sido seleccionado.
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onRestoreFileSelected(it) }
        }
    )

    // Efecto para mostrar mensajes (Toasts)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.onUserMessageShown()
        }
    }

    // --- NUEVO DIÁLOGO DE CONFIRMACIÓN ---
    // Se muestra solo si hay una URI pendiente en el estado.
    if (uiState.pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onRestoreCancelled() },
            title = { Text("Confirmar Restauración") },
            text = { Text("¿Estás seguro? Esta acción sobreescribirá todos los datos actuales de la aplicación. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onRestoreConfirmed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onRestoreCancelled() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Sección de Tema (sin cambios)
            Text(
                text = "Tema de la Aplicación",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(Modifier.selectableGroup()) {
                ThemeRadioButton(text = "Claro", selected = uiState.currentTheme == Theme.LIGHT, onClick = { viewModel.setTheme(Theme.LIGHT) })
                ThemeRadioButton(text = "Oscuro", selected = uiState.currentTheme == Theme.DARK, onClick = { viewModel.setTheme(Theme.DARK) })
                ThemeRadioButton(text = "Predeterminado del Sistema", selected = uiState.currentTheme == Theme.SYSTEM, onClick = { viewModel.setTheme(Theme.SYSTEM) })
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            // Sección de Copia de Seguridad (sin cambios)
            Text(
                text = "Copia de Seguridad",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Guarda o restaura todos los datos de tu bodega. Se recomienda hacer copias de seguridad regularmente.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val backupManager = BackupManager(context)
                        createBackupLauncher.launch(backupManager.getSuggestedBackupName())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Crear Backup")
                }
                OutlinedButton(
                    onClick = { restoreBackupLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restaurar")
                }
            }
        }
    }
}

@Composable
fun ThemeRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // El clic se maneja en el Row
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}