package com.example.huybrancardage.ui.camera

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * Composable affichant la prévisualisation de la caméra avec analyse de code-barres
 *
 * @param modifier Modificateur Compose
 * @param onBarcodeDetected Callback appelé lorsqu'un code-barres est détecté
 * @param isScanning Si true, l'analyse est active
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit,
    isScanning: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(isScanning) {
        val executor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            bindCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraProvider = cameraProvider,
                previewView = previewView,
                onBarcodeDetected = if (isScanning) onBarcodeDetected else { _ -> },
                executor = executor
            )
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la libération de la caméra", e)
            }
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Lie la caméra au cycle de vie avec l'analyseur de code-barres
 */
private fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    onBarcodeDetected: (String) -> Unit,
    executor: java.util.concurrent.ExecutorService
) {
    try {
        // Unbind avant de rebind
        cameraProvider.unbindAll()

        // Configuration de la prévisualisation
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Configuration de l'analyse d'image
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, BarcodeAnalyzer(onBarcodeDetected))
            }

        // Sélection de la caméra arrière
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Liaison au cycle de vie
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )

        Log.d(TAG, "Caméra liée avec succès")
    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors de la liaison de la caméra", e)
    }
}

private const val TAG = "CameraPreview"


