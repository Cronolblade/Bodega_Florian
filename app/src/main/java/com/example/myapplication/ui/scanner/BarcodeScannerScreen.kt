package com.example.myapplication.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.util.BarcodeAnalyser

@Composable
fun BarcodeScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        BarcodeAnalyser { barcode ->
                            // El analizador nos devuelve el código una sola vez.
                            // Aquí actualizamos el ViewModel y navegamos.
                            viewModel.onBarcodeScanned(barcode)
                            onNavigateUp()
                        }
                    )

                    try {
                        cameraProviderFuture.get().bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Se necesita permiso de cámara para usar el escáner.")
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Otorgar Permiso")
                    }
                }
            }
        }

        IconButton(
            onClick = onNavigateUp,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
    }
}