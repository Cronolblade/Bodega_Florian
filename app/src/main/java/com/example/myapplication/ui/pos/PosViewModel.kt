package com.example.myapplication.ui.pos

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Producto
import com.example.myapplication.data.ProductoRepository
import com.example.myapplication.data.Venta
import com.example.myapplication.data.VentaDetalle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PosViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.todosLosProductos,
                repository.todasLasCategorias,
                _uiState.map { it.textoBusqueda }.distinctUntilChanged(),
                _uiState.map { it.categoriaFiltro }.distinctUntilChanged()
            ) { productos, categorias, busqueda, filtroCategoria ->
                val filtrados = productos.filter { producto ->
                    val coincideBusqueda = producto.nombre.contains(busqueda, ignoreCase = true)
                    val coincideCategoria = filtroCategoria == "Todas" || producto.categoria == filtroCategoria
                    coincideBusqueda && coincideCategoria
                }
                _uiState.update {
                    it.copy(
                        inventarioFiltrado = filtrados,
                        categorias = categorias
                    )
                }
            }.collect()
        }
    }

    fun addProductToCartByBarcode(barcode: String, context: Context) {
        viewModelScope.launch {
            val producto = repository.findByBarcode(barcode)
            if (producto != null) {
                addToCart(producto)
                Toast.makeText(context, "'${producto.nombre}' añadido al carrito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Producto con ese código de barras no encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onSearchTextChanged(nuevoTexto: String) {
        _uiState.update { it.copy(textoBusqueda = nuevoTexto) }
    }

    fun onFilterCategoryChanged(nuevaCategoria: String) {
        _uiState.update { it.copy(categoriaFiltro = nuevaCategoria) }
    }

    // --- LÓGICA DEL CARRITO 100% CORREGIDA ---

    fun addToCart(producto: Producto) {
        _uiState.update { currentState ->
            val carritoActual = currentState.carrito
            val itemExistente = carritoActual.find { it.producto.id == producto.id }

            val nuevoCarrito = if (itemExistente != null) {
                // Si existe, creamos una NUEVA lista reemplazando el item antiguo por uno nuevo con la cantidad actualizada.
                if (itemExistente.cantidad < producto.stock) {
                    carritoActual.map {
                        if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1) else it
                    }
                } else {
                    carritoActual // No hay cambios si el stock está al máximo
                }
            } else {
                // Si no existe, creamos una NUEVA lista añadiendo el nuevo item.
                if (producto.stock > 0) {
                    carritoActual + CartItem(producto = producto, cantidad = 1)
                } else {
                    carritoActual // No se puede añadir si no hay stock
                }
            }
            currentState.copy(
                carrito = nuevoCarrito,
                totalVenta = calcularTotal(nuevoCarrito)
            )
        }
    }

    fun updateQuantity(cartItem: CartItem, nuevaCantidad: Int) {
        _uiState.update { currentState ->
            val carritoActual = currentState.carrito

            val nuevoCarrito = if (nuevaCantidad <= 0) {
                // Si la cantidad es 0 o menos, creamos una NUEVA lista filtrando el item.
                carritoActual.filterNot { it.producto.id == cartItem.producto.id }
            } else if (nuevaCantidad <= cartItem.producto.stock) {
                // Si la cantidad es válida, creamos una NUEVA lista actualizando el item.
                carritoActual.map {
                    if (it.producto.id == cartItem.producto.id) it.copy(cantidad = nuevaCantidad) else it
                }
            } else {
                carritoActual // No hay cambios si la cantidad excede el stock
            }

            currentState.copy(
                carrito = nuevoCarrito,
                totalVenta = calcularTotal(nuevoCarrito)
            )
        }
    }

    // removeFromCart ya funciona porque .filterNot crea una nueva lista, pero lo unificamos
    // para que sea más claro. Llamará a updateQuantity con cantidad 0.
    fun removeFromCart(cartItem: CartItem) {
        updateQuantity(cartItem, 0)
    }

    private fun calcularTotal(carrito: List<CartItem>): Double {
        return carrito.sumOf { it.producto.precioVenta * it.cantidad }
    }

    fun clearCart() {
        _uiState.update { it.copy(carrito = emptyList(), totalVenta = 0.0) }
    }

    fun finalizarVenta(onVentaExitosa: () -> Unit) {
        viewModelScope.launch {
            val estadoActual = _uiState.value
            if (estadoActual.carrito.isNotEmpty()) {
                val venta = Venta(
                    fecha = System.currentTimeMillis(),
                    total = estadoActual.totalVenta
                )
                // Ahora, al crear los detalles, también pasamos el precio de compra.
                val detalles = estadoActual.carrito.map { cartItem ->
                    VentaDetalle(
                        idVenta = 0,
                        idProducto = cartItem.producto.id,
                        cantidad = cartItem.cantidad,
                        precioVentaUnitario = cartItem.producto.precioVenta,
                        precioCompraUnitario = cartItem.producto.precioCompra // <-- CAMBIO CLAVE
                    )
                }
                repository.realizarVenta(venta, detalles)
                clearCart()
                onVentaExitosa()
            }
        }
    }

}

// El Factory no necesita cambios
class PosViewModelFactory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}