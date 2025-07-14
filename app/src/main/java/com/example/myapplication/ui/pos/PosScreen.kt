package com.example.myapplication.ui.pos

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    onNavigateUp: () -> Unit,
    onScanBarcodeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Venta") },
            text = { Text("¿Desea finalizar la venta por un total de S/ ${"%.2f".format(uiState.totalVenta)}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.finalizarVenta {
                        Toast.makeText(context, "Venta realizada con éxito", Toast.LENGTH_SHORT).show()
                    }
                    showConfirmDialog = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onScanBarcodeClick) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear Código")
                    }
                }
            )
        },
        bottomBar = {
            PosBottomBar(
                total = uiState.totalVenta,
                onCheckoutClick = {
                    if (uiState.carrito.isNotEmpty()) {
                        showConfirmDialog = true
                    } else {
                        Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // --- Columna Izquierda: Ahora incluye búsqueda y filtros ---
            Column(modifier = Modifier.weight(1f)) {
                SearchBar(
                    searchText = uiState.textoBusqueda,
                    onSearchChange = viewModel::onSearchTextChanged,
                    categories = uiState.categorias,
                    selectedCategory = uiState.categoriaFiltro,
                    onCategorySelected = viewModel::onFilterCategoryChanged
                )
                ProductSelectionList(
                    productos = uiState.inventarioFiltrado, // Usamos la lista filtrada
                    onProductClick = { viewModel.addToCart(it) }
                )
            }

            VerticalDivider()

            // --- Columna Derecha: Carrito de compras (sin cambios) ---
            Box(modifier = Modifier.weight(1f)) {
                CartSection(
                    cartItems = uiState.carrito,
                    onQuantityChange = { item, newQuantity -> viewModel.updateQuantity(item, newQuantity) },
                    onRemoveItem = { viewModel.removeFromCart(it) }
                )
            }
        }
    }
}

// --- NUEVO COMPONENTE: Barra de Búsqueda y Filtro ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    categories: List<Categoria>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            label = { Text("Buscar producto...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = isCategoryDropdownExpanded,
            onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar por categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isCategoryDropdownExpanded,
                onDismissRequest = { isCategoryDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas") },
                    onClick = {
                        onCategorySelected("Todas")
                        isCategoryDropdownExpanded = false
                    }
                )
                categories.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.nombre) },
                        onClick = {
                            onCategorySelected(categoria.nombre)
                            isCategoryDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}


// El resto de los componentes de la pantalla no necesitan cambios.
// ProductSelectionList, CartSection, CartItemView, etc., se quedan igual.

@Composable
fun ProductSelectionList(
    productos: List<Producto>,
    onProductClick: (Producto) -> Unit
) {
    if (productos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron productos.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(productos, key = { it.id }) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = producto.stock > 0) { onProductClick(producto) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (producto.stock > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stock: ${producto.stock}", style = MaterialTheme.typography.bodySmall)
                            Text("S/ ${"%.2f".format(producto.precioVenta)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartSection(
    cartItems: List<CartItem>,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit
) {
    if (cartItems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carrito vacío")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stickyHeader {
                Text(
                    "CARRITO",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            items(cartItems, key = { it.producto.id }) { item ->
                CartItemView(item, onQuantityChange, onRemoveItem)
            }
        }
    }
}

@Composable
fun CartItemView(
    item: CartItem,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.producto.nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { onRemoveItem(item) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar del carrito")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text("S/ ${"%.2f".format(item.producto.precioVenta * item.cantidad)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(16.dp))
                QuantitySelector(
                    quantity = item.cantidad,
                    onQuantityChange = { newQuantity -> onQuantityChange(item, newQuantity) },
                    maxQuantity = item.producto.stock
                )
            }
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit, maxQuantity: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onQuantityChange(quantity - 1) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Remove, "Quitar uno")
        }
        Text("$quantity", style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = { onQuantityChange(quantity + 1) }, enabled = quantity < maxQuantity, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Add, "Añadir uno")
        }
    }
}

@Composable
fun PosBottomBar(total: Double, onCheckoutClick: () -> Unit) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL:", style = MaterialTheme.typography.titleLarge)
            Text("S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCheckoutClick) {
                Text("Finalizar Venta")
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(color = MaterialTheme.colorScheme.outlineVariant)
    )
}