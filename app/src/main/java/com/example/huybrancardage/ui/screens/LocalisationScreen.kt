package com.example.huybrancardage.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.data.location.LocationService
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.BrancardageTextField
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray200
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White
import com.example.huybrancardage.ui.viewmodel.LocationUiState
import com.example.huybrancardage.ui.viewmodel.LocationViewModel

/**
 * Écran de localisation GPS
 * Affiche la position détectée via GPS et permet de l'affiner
 */
@Composable
fun LocalisationScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel? = null,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Initialiser le LocationService
    LaunchedEffect(viewModel) {
        viewModel?.initLocationService(LocationService(context))
    }

    // État du ViewModel ou état mock pour la preview
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(createMockUiState()) }

    // Launcher pour demander la permission de localisation
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel?.onPermissionResult(fineLocationGranted || coarseLocationGranted)
    }

    // Demander la permission si nécessaire
    LaunchedEffect(uiState.hasPermission, uiState.permissionRequested) {
        if (!uiState.hasPermission && !uiState.permissionRequested && viewModel != null) {
            locationPermissionLauncher.launch(LocationService.REQUIRED_PERMISSIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Lieu de départ",
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Titre et description
            Text(
                text = "Où se trouve le patient ?",
                style = MaterialTheme.typography.titleLarge,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.hasPermission) {
                    "La position a été détectée automatiquement via GPS."
                } else {
                    "Autorisez l'accès à la localisation pour détecter votre position."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Carte de localisation
            LocalisationCard(
                localisation = uiState.localisation,
                isLoading = uiState.isLoading,
                onRefreshClick = { viewModel?.refreshLocation() }
            )

            // Message d'erreur
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        TextButton(onClick = { viewModel?.clearError() }) {
                            Text("OK", color = White)
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Bouton pour demander la permission si non accordée
            if (!uiState.hasPermission && uiState.permissionRequested) {
                Spacer(modifier = Modifier.height(16.dp))
                RequestPermissionButton(
                    onClick = {
                        locationPermissionLauncher.launch(LocationService.REQUIRED_PERMISSIONS)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section modification manuelle
            Text(
                text = "Affiner la position (Optionnel)",
                style = MaterialTheme.typography.titleSmall,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrancardageTextField(
                value = uiState.precisions,
                onValueChange = { viewModel?.setPrecisions(it) },
                label = "Précisions pour le brancardier",
                placeholder = "Ex: Attendre devant l'ascenseur",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton confirmer
            ConfirmButton(
                text = "Confirmer le départ",
                onClick = onConfirmClick,
                enabled = uiState.hasValidLocation
            )
        }
    }
}

@Composable
private fun LocalisationCard(
    localisation: Localisation?,
    isLoading: Boolean,
    onRefreshClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
    ) {
        // Carte simulée (zone grise) avec marqueur ou loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(Gray200),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Blue600,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Marqueur de position
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Détails de la position
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blue100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Informations
            Column(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Text(
                        text = "Recherche de la position...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray500
                    )
                } else if (localisation != null) {
                    Text(
                        text = localisation.descriptionFormattee.ifEmpty { "Position inconnue" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray900
                    )
                    if (localisation.detailsFormattes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localisation.detailsFormattes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                    // Afficher les coordonnées GPS si disponibles
                    if (localisation.latitude != null && localisation.longitude != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GPS: ${String.format("%.4f", localisation.latitude)}, ${String.format("%.4f", localisation.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                } else {
                    Text(
                        text = "Position non disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray500
                    )
                }
            }

            // Bouton de rafraîchissement
            IconButton(
                onClick = onRefreshClick,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir la position",
                    tint = if (isLoading) Gray500 else Blue600
                )
            }
        }
    }
}

@Composable
private fun RequestPermissionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Blue100)
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            tint = Blue600,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Autoriser la localisation",
            style = MaterialTheme.typography.labelLarge,
            color = Blue600
        )
    }
}

@Composable
private fun ConfirmButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Blue600 else Gray500)
            .then(
                if (enabled) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Crée un état mock pour les previews
 */
private fun createMockUiState(): LocationUiState {
    return LocationUiState(
        localisation = Localisation(
            latitude = 48.8566,
            longitude = 2.3522,
            description = "Bâtiment A - Cardiologie",
            batiment = "A",
            etage = 2,
            chambre = "204"
        ),
        hasPermission = true,
        isLoading = false
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LocalisationScreenPreview() {
    HuyBrancardageTheme {
        LocalisationScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LocalisationScreenLoadingPreview() {
    HuyBrancardageTheme {
        LocalisationScreen()
    }
}
