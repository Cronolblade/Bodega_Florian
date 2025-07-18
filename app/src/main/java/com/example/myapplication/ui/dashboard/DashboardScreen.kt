package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.producto.Producto
import com.example.myapplication.data.reportes.GananciaPorDia
import com.example.myapplication.data.reportes.VentasPorDia
import com.example.myapplication.util.toFriendlyDateString
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateUp: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- CARRUSEL DE MÉTRICAS RESTAURADO ---
            val pagerState = rememberPagerState(pageCount = { 3 })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> TotalMetricCard(
                        title = "Ganancia Total (30 días)",
                        value = "S/ ${"%.2f".format(uiState.gananciaTotal)}"
                    )
                    1 -> ChartCard(
                        title = "Ganancias Diarias",
                        dataLabel = "Ganancia (S/)",
                        modelProducer = viewModel.gananciasModelProducer,
                        chartData = uiState.gananciasDiarias,
                        dataExtractor = { (it as GananciaPorDia).dia }
                    )
                    2 -> ChartCard(
                        title = "Ventas Diarias",
                        dataLabel = "Venta (S/)",
                        modelProducer = viewModel.ventasModelProducer,
                        chartData = uiState.ventasDiarias,
                        dataExtractor = { (it as VentasPorDia).dia }
                    )
                }
            }

            Row(
                Modifier.height(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AlertSection(
                    title = "Alerta de Stock Bajo",
                    alertColor = MaterialTheme.colorScheme.error,
                    productos = uiState.productosStockBajo,
                    umbral = uiState.umbralStockBajo,
                    onProductClick = onProductClick
                )
                AlertSection(
                    title = "Próximos a Vencer (${uiState.diasVencimiento} días)",
                    alertColor = MaterialTheme.colorScheme.tertiary,
                    productos = uiState.productosProximosAVencer,
                    isDateAlert = true,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

@Composable
fun TotalMetricCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(350.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun <T> ChartCard(
    title: String,
    dataLabel: String,
    modelProducer: ChartEntryModelProducer,
    chartData: List<T>,
    dataExtractor: (T) -> String
) {
    Card(modifier = Modifier.fillMaxWidth().height(350.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (chartData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay datos para mostrar.")
                }
            } else {
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    chartData.getOrNull(value.toInt())?.let(dataExtractor) ?: ""
                }

                ProvideChartStyle {
                    Chart(
                        chart = columnChart(), // Usamos gráfico de columnas
                        chartModelProducer = modelProducer,
                        startAxis = rememberStartAxis(title = dataLabel),
                        bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter, guideline = null),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun AlertSection(
    title: String,
    alertColor: Color,
    productos: List<Producto>,
    onProductClick: (Int) -> Unit,
    umbral: Int? = null,
    isDateAlert: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Alerta", tint = alertColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = alertColor)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (productos.isEmpty()) {
                Text(
                    "¡Todo en orden por aquí!",
                    modifier = Modifier.padding(vertical = 24.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                productos.forEach { producto ->
                    AlertProductItem(
                        producto = producto,
                        umbral = umbral,
                        isDateAlert = isDateAlert,
                        alertColor = alertColor,
                        onItemClick = { onProductClick(producto.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertProductItem(
    producto: Producto,
    umbral: Int?,
    isDateAlert: Boolean,
    alertColor: Color,
    onItemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(producto.nombre, style = MaterialTheme.typography.bodyLarge)

            if (isDateAlert) {
                producto.fechaVencimiento?.let { fecha ->
                    Text(
                        "Vence: ${fecha.toFriendlyDateString()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = alertColor
                    )
                }
            } else {
                Text(
                    "Stock: ${producto.stock} (Umbral: $umbral)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
            }
        }
    }
}