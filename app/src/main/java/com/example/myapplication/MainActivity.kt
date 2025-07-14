package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.Theme
import com.example.myapplication.ui.categoria.CategoryListScreen
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModelFactory
import com.example.myapplication.ui.pos.PosScreen
import com.example.myapplication.ui.pos.PosViewModel
import com.example.myapplication.ui.pos.PosViewModelFactory
import com.example.myapplication.ui.producto.AddEditProductScreen
import com.example.myapplication.ui.producto.ProductListScreen
import com.example.myapplication.ui.producto.ProductoViewModel
import com.example.myapplication.ui.producto.ProductoViewModelFactory
import com.example.myapplication.ui.scanner.BarcodeScannerScreen
import com.example.myapplication.ui.scanner.ScannerViewModel
import com.example.myapplication.ui.settings.SettingsScreen
import com.example.myapplication.ui.settings.SettingsViewModel
import com.example.myapplication.ui.settings.SettingsViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class NavDrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val drawerItems = listOf(
    NavDrawerItem("productList", "Inventario", Icons.Default.Inventory),
    NavDrawerItem("posScreen", "Punto de Venta", Icons.Default.PointOfSale),
    NavDrawerItem("dashboard", "Dashboard", Icons.Default.BarChart),
    NavDrawerItem("categoryList", "Categorías", Icons.Default.Category),
    NavDrawerItem("settings", "Configuración", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val productoViewModel: ProductoViewModel by viewModels { ProductoViewModelFactory((application as BodegaApplication).repository) }
    private val posViewModel: PosViewModel by viewModels { PosViewModelFactory((application as BodegaApplication).repository) }
    private val dashboardViewModel: DashboardViewModel by viewModels { DashboardViewModelFactory((application as BodegaApplication).repository) }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            (application as BodegaApplication).settingsManager,
            (application as BodegaApplication).backupManager,
            (application as BodegaApplication).database
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val useDarkTheme = when (settingsUiState.currentTheme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(Modifier.height(12.dp))
                            drawerItems.forEach { item ->
                                NavigationDrawerItem(
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = { Text(item.label) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                ) {
                    val scannerViewModel: ScannerViewModel = viewModel()

                    AppContent(
                        navController = navController,
                        drawerState = drawerState,
                        productoViewModel = productoViewModel,
                        posViewModel = posViewModel,
                        dashboardViewModel = dashboardViewModel,
                        settingsViewModel = settingsViewModel,
                        scannerViewModel = scannerViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    navController: NavHostController,
    drawerState: DrawerState,
    productoViewModel: ProductoViewModel,
    posViewModel: PosViewModel,
    dashboardViewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel,
    scannerViewModel: ScannerViewModel
) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController = navController) }

        composable("productList") {
            ProductListScreen(
                viewModel = productoViewModel,
                drawerState = drawerState,
                onAddProductClick = { navController.navigate("productForm") },
                onProductClick = { productId -> navController.navigate("productForm?id=$productId") }
            )
        }

        composable("barcodeScanner") {
            BarcodeScannerScreen(
                viewModel = scannerViewModel,
                onNavigateUp = { navController.popBackStack() }
            )
        }

        composable("posScreen") {
            val context = LocalContext.current
            val scannedBarcode by scannerViewModel.scannedBarcode.collectAsState()
            LaunchedEffect(scannedBarcode) {
                scannedBarcode?.let { barcode ->
                    posViewModel.addProductToCartByBarcode(barcode, context)
                    scannerViewModel.resetBarcode()
                }
            }
            PosScreen(
                viewModel = posViewModel,
                onNavigateUp = { navController.navigateUp() },
                onScanBarcodeClick = {
                    scannerViewModel.resetBarcode()
                    navController.navigate("barcodeScanner")
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateUp = { navController.navigateUp() },
                onProductClick = { productId -> navController.navigate("productForm?id=$productId") }
            )
        }

        composable("categoryList") {
            CategoryListScreen(viewModel = productoViewModel, onNavigateUp = { navController.navigateUp() })
        }

        composable("settings") {
            SettingsScreen(viewModel = settingsViewModel, onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = "productForm?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) {
            val productId = it.arguments?.getInt("id") ?: -1

            // --- LLAMADA SIMPLIFICADA ---
            // Ya no pasamos el formViewModel
            AddEditProductScreen(
                productoViewModel = productoViewModel,
                scannerViewModel = scannerViewModel,
                productId = productId,
                onNavigateUp = { navController.navigateUp() },
                onScanClick = {
                    scannerViewModel.resetBarcode()
                    navController.navigate("barcodeScanner")
                }
            )
        }
    }
}

@Composable
fun LoadingScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate("productList") {
            popUpTo("loading") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.bodega_florian),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bodega Florian",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}