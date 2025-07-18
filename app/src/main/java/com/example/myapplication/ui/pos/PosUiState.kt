package com.example.myapplication.ui.pos

import com.example.myapplication.data.categoria.Categoria
import com.example.myapplication.data.producto.Producto

// Clase para representar un item en el carrito de compras
data class CartItem(
    val producto: Producto,
    var cantidad: Int
)

// Estado completo y final de la pantalla del POS
data class PosUiState(
    // Ya no necesitamos inventarioCompleto aquí, el ViewModel lo manejará internamente
    val inventarioFiltrado: List<Producto> = emptyList(), // La lista que se mostrará en la UI
    val categorias: List<Categoria> = emptyList(),
    val carrito: List<CartItem> = emptyList(),
    val totalVenta: Double = 0.0,
    val textoBusqueda: String = "",
    val categoriaFiltro: String = "Todas" // "Todas" será la opción por defecto
)