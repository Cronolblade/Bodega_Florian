package com.example.myapplication.ui.producto

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.data.Producto
import com.example.myapplication.ui.scanner.ScannerViewModel
import com.example.myapplication.util.normalizeForComparison
import com.example.myapplication.util.showDatePicker
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productoViewModel: ProductoViewModel,
    scannerViewModel: ScannerViewModel,
    productId: Int,
    onNavigateUp: () -> Unit,
    onScanClick: () -> Unit
) {
    val isEditing = productId != -1
    val uiState by productoViewModel.addProductUiState.collectAsState()
    val categories by productoViewModel.categories.collectAsState()
    val formState = uiState.formState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (uiState.showAddCategoryDialog) {
        NewCategoryDialog(
            onConfirm = { newName ->
                productoViewModel.confirmAddNewCategory(newName)
            },
            onDismiss = {
                productoViewModel.onDialogDismiss()
            }
        )
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            productoViewModel.onUserMessageShown()
        }
    }

    LaunchedEffect(key1 = productId) {
        if (isEditing) {
            productoViewModel.loadProductIntoForm(productId)
        } else {
            productoViewModel.resetForm()
        }
    }

    val scannedBarcode by scannerViewModel.scannedBarcode.collectAsState()
    LaunchedEffect(key1 = scannedBarcode) {
        scannedBarcode?.let { barcode ->
            scope.launch {
                val exists = productoViewModel.checkBarcodeExists(barcode)
                if (exists) {
                    Toast.makeText(context, "Este código de barras ya está asignado.", Toast.LENGTH_LONG).show()
                } else {
                    productoViewModel.onFormChange(formState.copy(codigoBarras = barcode))
                    Toast.makeText(context, "Código de barras añadido.", Toast.LENGTH_SHORT).show()
                }
                scannerViewModel.resetBarcode()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scannerViewModel.resetBarcode()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Producto" else "Añadir Producto") },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImagePicker(
                imageUri = formState.imagenUri,
                onImageSelected = { uriString ->
                    productoViewModel.onFormChange(formState.copy(imagenUri = uriString))
                }
            )

            OutlinedTextField(
                value = formState.nombre,
                onValueChange = { productoViewModel.onFormChange(formState.copy(nombre = it)) },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth()
            )

            // --- INICIO DEL BLOQUE DE CÓDIGO CORREGIDO ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                var expanded by remember { mutableStateOf(false) }

                // --- INICIO DEL CAMBIO ---
                val filteredCategories = if (uiState.categoryQuery.isEmpty()) {
                    categories
                } else {
                    categories.filter {
                        it.nombre.normalizeForComparison().startsWith(uiState.categoryQuery.normalizeForComparison())
                    }
                }
                // --- FIN DEL CAMBIO ---

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.categoryQuery,
                        onValueChange = {
                            productoViewModel.onCategoryQueryChanged(it)
                            expanded = true // Mantenemos el menú abierto al escribir
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Buscar Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        singleLine = true
                    )

                    if (filteredCategories.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max= 200.dp)
                        ) {
                            filteredCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.nombre) },
                                    onClick = {
                                        productoViewModel.onCategorySelected(category.nombre)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { productoViewModel.requestAddNewCategory() },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Categoría",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Añadir Categoría")
                }
            }
            // --- FIN DEL BLOQUE DE CÓDIGO CORREGIDO ---


            OutlinedTextField(
                value = formState.precioCompra,
                onValueChange = { productoViewModel.onFormChange(formState.copy(precioCompra = it)) },
                label = { Text("Precio de Compra (S/)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = formState.precioVenta,
                onValueChange = { productoViewModel.onFormChange(formState.copy(precioVenta = it)) },
                label = { Text("Precio de Venta (S/)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = formState.stock,
                onValueChange = { productoViewModel.onFormChange(formState.copy(stock = it)) },
                label = { Text("Cantidad en Stock") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            DatePickerField(
                label = "Fecha de Vencimiento",
                timestamp = formState.fechaVencimiento,
                onDateSelected = { newTimestamp -> productoViewModel.onFormChange(formState.copy(fechaVencimiento = newTimestamp)) },
                onClearDate = { productoViewModel.onFormChange(formState.copy(fechaVencimiento = null)) }
            )

            OutlinedTextField(
                value = formState.codigoBarras,
                onValueChange = { productoViewModel.onFormChange(formState.copy(codigoBarras = it)) },
                label = { Text("Código de Barras") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear código")
                    }
                }
            )

            Button(
                onClick = {
                    if (formState.nombre.isNotBlank() && uiState.selectedCategoryName.isNotBlank()) {
                        scope.launch {
                            val fechaCompraOriginal = if (isEditing) productoViewModel.getProductById(productId).firstOrNull()?.fechaCompra else null
                            val productoParaGuardar = Producto(
                                id = if (isEditing) productId else 0,
                                nombre = formState.nombre,
                                categoria = uiState.selectedCategoryName,
                                precioCompra = formState.precioCompra.toDoubleOrNull() ?: 0.0,
                                precioVenta = formState.precioVenta.toDoubleOrNull() ?: 0.0,
                                stock = formState.stock.toIntOrNull() ?: 0,
                                fechaCompra = fechaCompraOriginal ?: System.currentTimeMillis(),
                                fechaVencimiento = formState.fechaVencimiento,
                                imagenUri = formState.imagenUri,
                                codigoBarras = formState.codigoBarras.ifEmpty { null }
                            )
                            if (isEditing) {
                                productoViewModel.actualizarProducto(productoParaGuardar)
                            } else {
                                productoViewModel.insertarProducto(productoParaGuardar)
                            }
                            onNavigateUp()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Guardar Producto")
            }
        }
    }
}


@Composable
fun ImagePicker(imageUri: String?, onImageSelected: (String) -> Unit) {
    val context = LocalContext.current
    val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, imagePermission) == PackageManager.PERMISSION_GRANTED) }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onImageSelected(it.toString()) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable {
                if (hasPermission) imagePickerLauncher.launch("image/*")
                else permissionLauncher.launch(imagePermission)
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(model = Uri.parse(imageUri), contentDescription = "Imagen del producto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Añadir foto", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    timestamp: Long?,
    onDateSelected: (Long) -> Unit,
    onClearDate: () -> Unit
) {
    val context = LocalContext.current
    val utcTimeZone = TimeZone.getTimeZone("UTC")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = utcTimeZone }
    val formattedDate = timestamp?.let { dateFormat.format(Date(it)) } ?: "Sin fecha de vencimiento"

    Box(
        modifier = Modifier.clickable {
            val initialCalendar = Calendar.getInstance(utcTimeZone)
            timestamp?.let { initialCalendar.timeInMillis = it }

            showDatePicker(
                context = context,
                initialTimestamp = timestamp,
                onDateSelected = onDateSelected
            )
        }
    ) {
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                if (timestamp != null) {
                    IconButton(onClick = onClearDate) { Icon(Icons.Default.Clear, "Limpiar fecha") }
                } else {
                    Icon(Icons.Default.DateRange, "Seleccionar fecha")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}

@Composable
fun NewCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Nueva Categoría") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nombre de la categoría") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}