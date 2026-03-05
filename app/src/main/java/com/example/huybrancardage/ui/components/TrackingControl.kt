package com.example.huybrancardage.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.huybrancardage.domain.model.WifiLocation
import com.example.huybrancardage.service.TrackingService
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray600
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.Green600
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

/**
 * Composant pour afficher et contrôler le suivi en temps réel du brancardier.
 *
 * # Objectif pédagogique
 *
 * Ce composant illustre plusieurs concepts Compose importants :
 *
 * ## 1. Observation d'un Service depuis l'UI
 * On utilise `collectAsState()` pour observer les StateFlow exposés par le Service.
 * L'UI se met à jour automatiquement quand la position change.
 *
 * ## 2. Gestion des permissions runtime
 * Avant de démarrer le tracking, on vérifie et demande les permissions nécessaires :
 * - ACCESS_FINE_LOCATION (pour le scan WiFi)
 * - POST_NOTIFICATIONS (Android 13+, pour la notification du service)
 *
 * ## 3. Animations Compose
 * On utilise `AnimatedVisibility` pour afficher/masquer la carte de position
 * avec une animation fluide.
 *
 * @param patientName Nom du patient transporté
 * @param brancardageId ID de la demande de brancardage
 * @param modifier Modifier Compose
 */
@Composable
fun TrackingControl(
    patientName: String,
    brancardageId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe l'état du service via les StateFlow statiques
    val isTracking by TrackingService.isTracking.collectAsState()
    val currentLocation by TrackingService.currentLocation.collectAsState()

    // État pour les permissions
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Pas nécessaire avant Android 13
            }
        )
    }

    // Launcher pour demander les permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        }

        // Si toutes les permissions sont accordées, démarre le tracking
        if (hasLocationPermission && hasNotificationPermission) {
            TrackingService.start(context, patientName, brancardageId)
        }
    }

    Column(modifier = modifier) {
        // Bouton Start/Stop
        TrackingButton(
            isTracking = isTracking,
            onClick = {
                if (isTracking) {
                    // Arrête le tracking
                    TrackingService.stop(context)
                } else {
                    // Vérifie les permissions avant de démarrer
                    val permissionsToRequest = mutableListOf<String>()

                    if (!hasLocationPermission) {
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        // Demande les permissions manquantes
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        // Toutes les permissions sont OK, démarre le tracking
                        TrackingService.start(context, patientName, brancardageId)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Carte de position actuelle (animée)
        AnimatedVisibility(
            visible = isTracking && currentLocation != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            currentLocation?.let { location ->
                LocationCard(location = location)
            }
        }

        // Message si tracking actif mais pas encore de position
        AnimatedVisibility(
            visible = isTracking && currentLocation == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Recherche de la position...",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Bouton pour démarrer/arrêter le tracking.
 */
@Composable
private fun TrackingButton(
    isTracking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTracking) Color(0xFFDC2626) else Green600
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isTracking) "Arrêter le suivi" else "Démarrer le transport",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Carte affichant la position actuelle du brancardier.
 */
@Composable
private fun LocationCard(
    location: WifiLocation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Blue100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône animée (pulse effect simulé par la couleur)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blue600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Informations de localisation
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Position actuelle",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray600
                )
                Text(
                    text = "${location.batiment} - ${location.etage}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = location.zone,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }

            // Indicateur de signal WiFi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = getSignalColor(location.signalStrength),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${location.signalStrength} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }
        }
    }
}

/**
 * Retourne une couleur en fonction de la force du signal WiFi.
 *
 * - Excellent : > -50 dBm (vert)
 * - Bon : -50 à -70 dBm (vert clair)
 * - Moyen : -70 à -80 dBm (orange)
 * - Faible : < -80 dBm (rouge)
 */
@Composable
private fun getSignalColor(signalStrength: Int): Color {
    return when {
        signalStrength > -50 -> Green600
        signalStrength > -70 -> Color(0xFF84CC16) // lime
        signalStrength > -80 -> Color(0xFFF59E0B) // amber
        else -> Color(0xFFEF4444) // red
    }
}

// ============================================================
// Previews
// ============================================================

@Preview(showBackground = true)
@Composable
private fun TrackingButtonPreview_Stopped() {
    HuyBrancardageTheme {
        TrackingButton(
            isTracking = false,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingButtonPreview_Started() {
    HuyBrancardageTheme {
        TrackingButton(
            isTracking = true,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationCardPreview() {
    HuyBrancardageTheme {
        LocationCard(
            location = WifiLocation(
                bssid = "AA:BB:CC:DD:EE:FF",
                ssid = "HOPITAL-HUY",
                batiment = "Bâtiment B",
                etage = "2ème étage",
                zone = "Radiologie",
                signalStrength = -55
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}




