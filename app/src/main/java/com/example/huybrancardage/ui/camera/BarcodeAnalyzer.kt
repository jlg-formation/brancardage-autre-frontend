package com.example.huybrancardage.ui.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Analyseur d'images pour la détection de codes-barres et QR codes
 * Utilise Google ML Kit Vision API
 */
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage == null || isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                // Prendre le premier code détecté
                val barcode = barcodes.firstOrNull()

                if (barcode != null) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        Log.d(TAG, "Code détecté: $rawValue (format: ${getFormatName(barcode.format)})")
                        onBarcodeDetected(rawValue)
                    }
                }

                isProcessing = false
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erreur lors de l'analyse", exception)
                isProcessing = false
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Retourne le nom du format de code-barres
     */
    private fun getFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_CODE_39 -> "CODE_39"
            Barcode.FORMAT_CODE_93 -> "CODE_93"
            Barcode.FORMAT_CODABAR -> "CODABAR"
            Barcode.FORMAT_EAN_13 -> "EAN_13"
            Barcode.FORMAT_EAN_8 -> "EAN_8"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_UPC_A -> "UPC_A"
            Barcode.FORMAT_UPC_E -> "UPC_E"
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "AZTEC"
            Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            else -> "UNKNOWN"
        }
    }

    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }
}

