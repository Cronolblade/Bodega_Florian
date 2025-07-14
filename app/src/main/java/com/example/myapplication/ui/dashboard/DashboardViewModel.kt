package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.GananciaPorDia
import com.example.myapplication.data.Producto
import com.example.myapplication.data.ProductoRepository
import com.example.myapplication.data.VentasPorDia
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- El UiState vuelve a contener solo DATOS CRUDOS ---
data class DashboardUiState(
    val ventasDiarias: List<VentasPorDia> = emptyList(),
    val gananciasDiarias: List<GananciaPorDia> = emptyList(),
    val gananciaTotal: Float = 0f,
    val productosStockBajo: List<Producto> = emptyList(),
    val productosProximosAVencer: List<Producto> = emptyList(),
    val umbralStockBajo: Int = 5,
    val diasVencimiento: Int = 30
)

class DashboardViewModel(repository: ProductoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // --- LOS CONTROLADORES DEL GRÁFICO VIVEN AQUÍ, ESTABLES Y ÚNICOS ---
    val ventasModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()
    val gananciasModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    init {
        // --- USAMOS UN ÚNICO COMBINE PARA EVITAR RACE CONDITIONS ---
        viewModelScope.launch {
            combine(
                repository.ventasTotalesPorDia,
                repository.gananciasTotalesPorDia,
                repository.obtenerProductosConStockBajo(_uiState.value.umbralStockBajo),
                repository.obtenerProductosProximosAVencer(_uiState.value.diasVencimiento)
            ) { ventas, ganancias, stockBajo, porVencer ->

                // 1. Actualizamos los modelos de los gráficos (efecto secundario)
                val ventasEntries = ventas.mapIndexed { index, venta -> entryOf(index.toFloat(), venta.total) }
                ventasModelProducer.setEntries(ventasEntries)

                val gananciasEntries = ganancias.mapIndexed { index, ganancia -> entryOf(index.toFloat(), ganancia.totalGanancia) }
                gananciasModelProducer.setEntries(gananciasEntries)

                // 2. Creamos y emitimos el nuevo estado de la UI con los datos crudos
                DashboardUiState(
                    ventasDiarias = ventas,
                    gananciasDiarias = ganancias,
                    gananciaTotal = ganancias.sumOf { ganancia -> ganancia.totalGanancia.toDouble() }.toFloat(),
                    productosStockBajo = stockBajo,
                    productosProximosAVencer = porVencer
                )
            }.collect { newState ->
                // Asignamos el nuevo estado completo a nuestro StateFlow
                _uiState.value = newState
            }
        }
    }
}

class DashboardViewModelFactory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}