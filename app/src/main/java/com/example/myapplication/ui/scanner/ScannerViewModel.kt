package com.example.myapplication.ui.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScannerViewModel : ViewModel() {

    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode = _scannedBarcode.asStateFlow()

    fun onBarcodeScanned(barcode: String) {
        _scannedBarcode.update { barcode }
    }

    fun resetBarcode() {
        _scannedBarcode.update { null }
    }
}