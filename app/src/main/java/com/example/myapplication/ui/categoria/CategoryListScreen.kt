package com.example.myapplication.ui.categoria

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Categoria
import com.example.myapplication.ui.producto.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: ProductoViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.addProductUiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf<DialogState>(DialogState.Hidden) }

    // Efecto para mostrar Toasts (sin cambios)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onUserMessageShown()
        }
    }

    // --- CORRECCIÓN 1: Bloque `when` corregido ---
    when (val state = showDialog) {
        DialogState.Hidden -> {}
        DialogState.AddNew -> {
            CategoryDialog(
                onDismiss = { showDialog = DialogState.Hidden },
                onConfirm = { name ->
                    viewModel.insertarCategoria(name)
                    showDialog = DialogState.Hidden
                }
            )
        }
        is DialogState.Edit -> {
            CategoryDialog(
                initialValue = state.category.nombre,
                onDismiss = { showDialog = DialogState.Hidden },
                onConfirm = { newName ->
                    viewModel.actualizarCategoria(state.category.copy(nombre = newName))
                    showDialog = DialogState.Hidden
                }
            )
        }
        // La rama ahora es 'is DialogState.Delete'
        is DialogState.Delete -> {
            ConfirmDeleteDialog(
                categoryName = state.category.nombre,
                onDismiss = { showDialog = DialogState.Hidden },
                onConfirm = {
                    viewModel.eliminarCategoria(state.category)
                    showDialog = DialogState.Hidden
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Categorías") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = DialogState.AddNew }) {
                Icon(Icons.Default.Add, "Añadir Categoría")
            }
        }
    ) { padding ->
        // --- CORRECCIÓN 2: Manejo de lista vacía ---
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay categorías. ¡Añade una con el botón +!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(items = categories, key = { it.id }) { categoria ->
                    CategoryItem(
                        categoria = categoria,
                        onEditClick = { showDialog = DialogState.Edit(categoria) },
                        // La acción aquí ahora abre el diálogo de confirmación
                        onDeleteClick = { showDialog = DialogState.Delete(categoria) }
                    )
                    // Ya no se necesita el Divider, lo quitamos.
                }
            }
        }
    }
}

// Composable para el diálogo de confirmación de borrado (sin cambios)
@Composable
fun ConfirmDeleteDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar la categoría '$categoryName'? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Composable para cada item de la lista (sin cambios)
@Composable
fun CategoryItem(
    categoria: Categoria,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Composable para el diálogo de añadir/editar (sin cambios)
@Composable
fun CategoryDialog(
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialValue.isEmpty()) "Añadir Categoría" else "Editar Categoría") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nombre") }
            )
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onConfirm(text) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Clase sellada para el estado del diálogo (sin cambios)
sealed class DialogState {
    object Hidden : DialogState()
    object AddNew : DialogState()
    data class Edit(val category: Categoria) : DialogState()
    data class Delete(val category: Categoria) : DialogState()
}