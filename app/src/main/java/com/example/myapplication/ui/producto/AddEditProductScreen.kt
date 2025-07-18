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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.compose.runtime.saveable.rememberSaveable
import coil.compose.AsyncImage
import com.example.myapplication.data.categoria.Categoria
import com.example.myapplication.data.producto.Producto
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
            onConfirm = { newName -> productoViewModel.confirmAddNewCategory(newName) },
            onDismiss = { productoViewModel.onDialogDismiss() }
        )
    }

    if (uiState.showCategorySearchDialog) {
        CategorySearchDialog(
            allCategories = categories,
            query = uiState.categorySearchQuery,
            onQueryChange = { productoViewModel.onCategorySearchQueryChange(it) },
            onCategorySelected = { productoViewModel.onCategorySelected(it.nombre) },
            onDismiss = { productoViewModel.closeCategorySearchDialog() }
        )
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            productoViewModel.onUserMessageShown()
        }
    }

    val isFormInitialized = rememberSaveable(productId) { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isFormInitialized.value) {
            if (isEditing) {
                productoViewModel.loadProductIntoForm(productId)
            } else {
                productoViewModel.resetForm()
            }
            isFormInitialized.value = true
        }
    }

    val scannedBarcode by scannerViewModel.scannedBarcode.collectAsState()
    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            scope.launch {
                val exists = productoViewModel.checkBarcodeExists(barcode)
                if (exists) {
                    Toast.makeText(context, "Este código de barras ya está asignado.", Toast.LENGTH_LONG).show()
                } else {
                    productoViewModel.onFormChange(
                        productoViewModel.addProductUiState.value.formState.copy(codigoBarras = barcode)
                    )
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

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.clickable { productoViewModel.openCategorySearchDialog() }) {
                    OutlinedTextField(
                        value = formState.categoria,
                        onValueChange = {},
                        label = { Text("Categoría") },
                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { productoViewModel.requestAddNewCategory() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, "Añadir Categoría", Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Añadir Nueva Categoría")
                }
            }

            OutlinedTextField(value = formState.precioCompra, onValueChange = { productoViewModel.onFormChange(formState.copy(precioCompra = it)) }, label = { Text("Precio de Compra (S/)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(value = formState.precioVenta, onValueChange = { productoViewModel.onFormChange(formState.copy(precioVenta = it)) }, label = { Text("Precio de Venta (S/)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(value = formState.stock, onValueChange = { productoViewModel.onFormChange(formState.copy(stock = it)) }, label = { Text("Cantidad en Stock") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            DatePickerField(label = "Fecha de Vencimiento", timestamp = formState.fechaVencimiento, onDateSelected = { newTimestamp -> productoViewModel.onFormChange(formState.copy(fechaVencimiento = newTimestamp)) }, onClearDate = { productoViewModel.onFormChange(formState.copy(fechaVencimiento = null)) })
            OutlinedTextField(
                value = formState.codigoBarras,
                onValueChange = { productoViewModel.onFormChange(formState.copy(codigoBarras = it)) },
                label = { Text("Código de Barras") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = onScanClick) { Icon(Icons.Default.QrCodeScanner, "Escanear código") } }
            )

            Button(
                onClick = {
                    if (formState.nombre.isNotBlank() && formState.categoria.isNotBlank()) {
                        scope.launch {
                            val fechaCompraOriginal = if (isEditing) productoViewModel.getProductById(productId).firstOrNull()?.fechaCompra else null
                            val productoParaGuardar = Producto(id = if (isEditing) productId else 0, nombre = formState.nombre, categoria = formState.categoria, precioCompra = formState.precioCompra.toDoubleOrNull() ?: 0.0, precioVenta = formState.precioVenta.toDoubleOrNull() ?: 0.0, stock = formState.stock.toIntOrNull() ?: 0, fechaCompra = fechaCompraOriginal ?: System.currentTimeMillis(), fechaVencimiento = formState.fechaVencimiento, imagenUri = formState.imagenUri, codigoBarras = formState.codigoBarras.ifEmpty { null })
                            if (isEditing) { productoViewModel.actualizarProducto(productoParaGuardar) } else { productoViewModel.insertarProducto(productoParaGuardar) }
                            onNavigateUp()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditing) "Guardar Cambios" else "Guardar Producto") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchDialog(
    allCategories: List<Categoria>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (Categoria) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredCategories = if (query.isBlank()) {
        allCategories
    } else {
        allCategories.filter { it.nombre.normalizeForComparison().contains(query.normalizeForComparison()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Buscar Categoría") },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") } }
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Buscar...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredCategories, key = { it.id }) { category ->
                        ListItem(
                            headlineContent = { Text(category.nombre) },
                            modifier = Modifier.clickable { onCategorySelected(category) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ImagePicker(imageUri: String?, onImageSelected: (String) -> Unit) {
    val context = LocalContext.current
    val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, imagePermission) == PackageManager.PERMISSION_GRANTED) }
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { onImageSelected(it.toString()) } }
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> hasPermission = isGranted; if (isGranted) { imagePickerLauncher.launch("image/*") } else { Toast.makeText(context, "Permiso denegado.", Toast.LENGTH_SHORT).show() } }
    Box(modifier = Modifier.size(150.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer).clickable { if (hasPermission) imagePickerLauncher.launch("image/*") else permissionLauncher.launch(imagePermission) }, contentAlignment = Alignment.Center) { if (imageUri != null) { AsyncImage(model = Uri.parse(imageUri), contentDescription = "Imagen del producto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) } else { Icon(Icons.Default.AddAPhoto, "Añadir foto", Modifier.size(48.dp), MaterialTheme.colorScheme.onSecondaryContainer) } }
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
    Box(modifier = Modifier.clickable { val initialCalendar = Calendar.getInstance(utcTimeZone); timestamp?.let { initialCalendar.timeInMillis = it }; showDatePicker(context = context, initialTimestamp = timestamp, onDateSelected = onDateSelected) }) { OutlinedTextField(value = formattedDate, onValueChange = {}, label = { Text(label) }, trailingIcon = { if (timestamp != null) { IconButton(onClick = onClearDate) { Icon(Icons.Default.Clear, "Limpiar fecha") } } else { Icon(Icons.Default.DateRange, "Seleccionar fecha") } }, modifier = Modifier.fillMaxWidth(), enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurface)) }
}

@Composable
fun NewCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Añadir Nueva Categoría") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nombre de la categoría") }, singleLine = true) }, confirmButton = { Button(onClick = { if (text.isNotBlank()) { onConfirm(text) } }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}