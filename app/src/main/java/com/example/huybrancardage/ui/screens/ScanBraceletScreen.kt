package com.example.huybrancardage.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.ui.camera.CameraPreview
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White
import com.example.huybrancardage.ui.viewmodel.ScanUiState
import com.example.huybrancardage.ui.viewmodel.ScanViewModel

/**
 * Écran de scan du bracelet patient
 * Utilise CameraX et ML Kit pour la détection de codes-barres/QR codes
 */
@Composable
fun ScanBraceletScreen(
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onScanSuccess: (Patient) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Vérification initiale de la permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher pour demander la permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        viewModel.setCameraPermission(isGranted)
    }

    // Demander la permission au lancement si non accordée
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            viewModel.setCameraPermission(true)
        }
    }

    // Naviguer vers le dossier patient quand un patient est détecté
    LaunchedEffect(uiState.patient) {
        uiState.patient?.let { patient ->
            onScanSuccess(patient)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1F2937))
    ) {
        if (hasPermission) {
            // Affichage de la caméra avec analyse
            CameraContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onBarcodeDetected = viewModel::onCodeDetected,
                onToggleFlash = viewModel::toggleFlash,
                onRetry = viewModel::clearError,
                onSimulateScan = { viewModel.simulateScan() }
            )
        } else {
            // Affichage quand la permission est refusée
            PermissionDeniedContent(
                onBackClick = onBackClick,
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

@Composable
private fun CameraContent(
    uiState: ScanUiState,
    onBackClick: () -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onToggleFlash: () -> Unit,
    onRetry: () -> Unit,
    onSimulateScan: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Prévisualisation de la caméra
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onBarcodeDetected = onBarcodeDetected,
            isScanning = uiState.isScanning && !uiState.isProcessing
        )

        // Header transparent avec gradient
        HeaderOverlay(
            onBackClick = onBackClick
        )

        // Zone centrale avec le cadre de scan
        ScanFrameOverlay(
            isProcessing = uiState.isProcessing,
            error = uiState.error
        )

        // Overlay d'erreur
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            ErrorOverlay(
                error = uiState.error ?: "",
                onRetry = onRetry
            )
        }

        // Actions en bas avec gradient
        BottomActionsOverlay(
            isFlashEnabled = uiState.isFlashEnabled,
            isProcessing = uiState.isProcessing,
            onToggleFlash = onToggleFlash,
            onSimulateScan = onSimulateScan
        )
    }
}

@Composable
private fun HeaderOverlay(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bouton retour
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Scanner le bracelet",
                style = MaterialTheme.typography.titleLarge,
                color = White
            )
        }
    }
}

@Composable
private fun ScanFrameOverlay(
    isProcessing: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Instructions
        Text(
            text = if (isProcessing) "Traitement en cours..."
            else if (error != null) ""
            else "Placez le code-barres ou QR Code du bracelet dans le cadre.",
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Cadre de visée
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Coins du cadre
            ScanCorners()

            // Ligne de scan animée
            if (!isProcessing && error == null) {
                AnimatedScanLine()
            }

            // Indicateur de chargement
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Blue600,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedScanLine() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = (offsetY * 100).dp)
            .height(2.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Red,
                        Color.Red,
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun ErrorOverlay(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌ Erreur",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Réessayer")
        }
    }
}

@Composable
private fun BottomActionsOverlay(
    isFlashEnabled: Boolean,
    isProcessing: Boolean,
    onToggleFlash: () -> Unit,
    onSimulateScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton flash
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFlashEnabled) Blue600
                            else White.copy(alpha = 0.2f)
                        )
                        .clickable(enabled = !isProcessing) { onToggleFlash() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled)
                            Icons.Default.FlashOn
                        else
                            Icons.Default.FlashOff,
                        contentDescription = if (isFlashEnabled)
                            "Désactiver le flash"
                        else
                            "Activer le flash",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Bouton de simulation de succès (pour tests/démo)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Blue600)
                        .clickable(enabled = !isProcessing) { onSimulateScan() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Simuler scan réussi",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onBackClick: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Scanner le bracelet",
                    style = MaterialTheme.typography.titleLarge,
                    color = White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Icône caméra
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = White.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permission caméra requise",
            style = MaterialTheme.typography.titleLarge,
            color = White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pour scanner le bracelet patient, l'application a besoin d'accéder à la caméra.",
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRequestPermission) {
            Text("Autoriser la caméra")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScanCorners() {
    val cornerSize = 32.dp
    val cornerStrokeWidth = 4.dp
    val cornerColor = Blue600

    Box(modifier = Modifier.fillMaxSize()) {
        // Coin haut-gauche
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .border(
                    width = cornerStrokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(topStart = 8.dp)
                )
                .clip(RoundedCornerShape(topStart = 8.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin haut-droite
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin bas-gauche
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin bas-droite
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScanBraceletScreenPreview() {
    HuyBrancardageTheme {
        // Preview avec contenu statique (sans caméra)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2937))
        ) {
            HeaderOverlay(onBackClick = {})

            ScanFrameOverlay(
                isProcessing = false,
                error = null
            )

            BottomActionsOverlay(
                isFlashEnabled = false,
                isProcessing = false,
                onToggleFlash = {},
                onSimulateScan = {}
            )
        }
    }
}
