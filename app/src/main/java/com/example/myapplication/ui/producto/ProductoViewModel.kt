package com.example.myapplication.ui.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.categoria.Categoria
import com.example.myapplication.data.producto.Producto
import com.example.myapplication.data.producto.ProductoRepository
import com.example.myapplication.data.categoria.CategoriaRepository
import com.example.myapplication.util.normalizeForComparison
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado para la lista de productos del inventario
data class ProductListUiState(
    val productList: List<Producto> = emptyList(),
    val productToDelete: Producto? = null,
    val nameSearchText: String = "",
    val categorySearchText: String = ""
)

// Estado para el formulario de producto
data class ProductFormState(
    val nombre: String = "",
    val categoria: String = "",
    val precioCompra: String = "",
    val precioVenta: String = "",
    val stock: String = "",
    val fechaVencimiento: Long? = null,
    val imagenUri: String? = null,
    val codigoBarras: String = ""
)

// El UiState del formulario ahora es mucho más simple.
data class AddProductUiState(
    val showAddCategoryDialog: Boolean = false,
    val userMessage: String? = null,
    val formState: ProductFormState = ProductFormState(),
    val showCategorySearchDialog: Boolean = false,
    val categorySearchQuery: String = ""
)

class ProductoViewModel(
    private val productoRepository: ProductoRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(ProductListUiState())
    val listUiState: StateFlow<ProductListUiState> = _listUiState.asStateFlow()

    private val _addProductUiState = MutableStateFlow(AddProductUiState())
    val addProductUiState: StateFlow<AddProductUiState> = _addProductUiState.asStateFlow()

    val categories: StateFlow<List<Categoria>> = categoriaRepository.todasLasCategorias
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val filteredProductList: StateFlow<List<Producto>> =
        combine(
            productoRepository.todosLosProductos,
            _listUiState.map { it.nameSearchText }.distinctUntilChanged(),
            _listUiState.map { it.categorySearchText }.distinctUntilChanged()
        ) { productList, nameSearch, categorySearch ->
            val normalizedNameSearch = nameSearch.normalizeForComparison()
            val normalizedCategorySearch = categorySearch.normalizeForComparison()
            if (normalizedNameSearch.isBlank() && normalizedCategorySearch.isBlank()) {
                productList
            } else {
                productList.filter { producto ->
                    val nameMatches = if (normalizedNameSearch.isNotBlank()) producto.nombre.normalizeForComparison().startsWith(normalizedNameSearch) else true
                    val categoryMatches = if (normalizedCategorySearch.isNotBlank()) producto.categoria.normalizeForComparison().startsWith(normalizedCategorySearch) else true
                    nameMatches && categoryMatches
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun loadProductIntoForm(productId: Int) {
        viewModelScope.launch {
            productoRepository.obtenerPorId(productId).firstOrNull()?.let { product ->
                _addProductUiState.update {
                    it.copy(
                        formState = ProductFormState(
                            nombre = product.nombre,
                            categoria = product.categoria,
                            precioCompra = product.precioCompra.toString(),
                            precioVenta = product.precioVenta.toString(),
                            stock = product.stock.toString(),
                            fechaVencimiento = product.fechaVencimiento,
                            imagenUri = product.imagenUri,
                            codigoBarras = product.codigoBarras ?: ""
                        )
                    )
                }
            }
        }
    }

    fun resetForm() {
        _addProductUiState.value = AddProductUiState()
    }

    fun openCategorySearchDialog() {
        _addProductUiState.update { it.copy(showCategorySearchDialog = true, categorySearchQuery = "") }
    }

    fun closeCategorySearchDialog() {
        _addProductUiState.update { it.copy(showCategorySearchDialog = false) }
    }

    fun onCategorySearchQueryChange(query: String) {
        _addProductUiState.update { it.copy(categorySearchQuery = query) }
    }

    fun onCategorySelected(name: String) {
        val currentFormState = _addProductUiState.value.formState
        onFormChange(currentFormState.copy(categoria = name))
        closeCategorySearchDialog()
    }

    fun requestAddNewCategory() {
        _addProductUiState.update { it.copy(showAddCategoryDialog = true) }
    }

    fun onDialogDismiss() {
        _addProductUiState.update { it.copy(showAddCategoryDialog = false) }
    }

    fun confirmAddNewCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val normalizedName = name.normalizeForComparison()
                val exists = categories.value.any { it.nombre.normalizeForComparison() == normalizedName }

                if (exists) {
                    _addProductUiState.update { it.copy(userMessage = "La categoría '$name' ya existe.") }
                } else {
                    val categoria = Categoria(nombre = name)
                    categoriaRepository.insertarCategoria(categoria)
                    onCategorySelected(name)
                    _addProductUiState.update { it.copy(userMessage = "Categoría '$name' añadida.") }
                }
            }
            onDialogDismiss()
        }
    }

    fun onFormChange(newFormState: ProductFormState) {
        _addProductUiState.update { it.copy(formState = newFormState) }
    }

    suspend fun checkBarcodeExists(barcode: String): Boolean = productoRepository.findByBarcode(barcode) != null
    fun onNameSearchTextChanged(text: String) { _listUiState.update { it.copy(nameSearchText = text) } }
    fun onCategorySearchTextChanged(text: String) { _listUiState.update { it.copy(categorySearchText = text) } }
    fun onProductDeleteRequest(producto: Producto) { _listUiState.update { it.copy(productToDelete = producto) } }
    fun onProductDeleteConfirm() { _listUiState.value.productToDelete?.let { viewModelScope.launch { productoRepository.eliminar(it); onProductDeleteCancel() } } }
    fun onProductDeleteCancel() { _listUiState.update { it.copy(productToDelete = null) } }
    fun getProductById(id: Int): Flow<Producto?> = productoRepository.obtenerPorId(id)
    fun onUserMessageShown() { _addProductUiState.update { it.copy(userMessage = null) } }
    fun insertarProducto(producto: Producto) { viewModelScope.launch { productoRepository.insertar(producto) } }
    fun actualizarProducto(producto: Producto) { viewModelScope.launch { productoRepository.actualizar(producto) } }
    fun eliminarProducto(producto: Producto) { viewModelScope.launch { productoRepository.eliminar(producto) } }
    fun insertarCategoria(categoryName: String) {
        viewModelScope.launch {
            if (categoryName.isNotBlank()) {
                val categoria = Categoria(nombre = categoryName)
                categoriaRepository.insertarCategoria(categoria)
                _addProductUiState.update { it.copy(userMessage = "Categoría '$categoryName' añadida.") }
            }
        }
    }
    fun actualizarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            categoriaRepository.actualizarCategoria(categoria)
            _addProductUiState.update { it.copy(userMessage = "Categoría actualizada.") }
        }
    }
    fun eliminarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            categoriaRepository.eliminarCategoria(categoria)
            _addProductUiState.update { it.copy(userMessage = "Categoría eliminada.") }
        }
    }
}

class ProductoViewModelFactory(
    private val productoRepository: ProductoRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(productoRepository, categoriaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}