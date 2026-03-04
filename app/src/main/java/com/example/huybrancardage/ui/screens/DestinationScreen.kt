package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray200
import com.example.huybrancardage.ui.theme.Gray300
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White
import com.example.huybrancardage.ui.viewmodel.DestinationViewModel

/**
 * Écran de sélection de la destination
 * Liste des services/chambres d'arrivée avec recherche
 */
@Composable
fun DestinationScreen(
    modifier: Modifier = Modifier,
    viewModel: DestinationViewModel? = null,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    // Collecter l'état du ViewModel
    val uiState by viewModel?.uiState?.collectAsState()
        ?: androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf(
                com.example.huybrancardage.ui.viewmodel.DestinationUiState(
                    destinations = getMockDestinations()
                )
            )
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Destination",
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
                text = "Où va le patient ?",
                style = MaterialTheme.typography.titleLarge,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sélectionnez le service ou la chambre d'arrivée.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barre de recherche
            SearchBar(
                value = uiState.searchQuery,
                onValueChange = { viewModel?.setSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Liste des destinations
            Text(
                text = if (uiState.searchQuery.isBlank()) "Destinations fréquentes" else "Résultats",
                style = MaterialTheme.typography.titleSmall,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue600)
                }
            } else if (uiState.displayedDestinations.isEmpty()) {
                Text(
                    text = "Aucune destination trouvée",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                uiState.displayedDestinations.forEach { destination ->
                    DestinationOption(
                        destination = destination,
                        isSelected = uiState.selectedDestination?.id == destination.id,
                        onSelect = { viewModel?.selectDestination(destination) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton confirmer
            ConfirmDestinationButton(
                text = "Confirmer la destination",
                onClick = onConfirmClick,
                enabled = uiState.hasSelection
            )
        }
    }
}

/**
 * Destinations mockées pour le preview
 */
private fun getMockDestinations(): List<Destination> = listOf(
    Destination(
        id = "1",
        nom = "Radiologie",
        batiment = "B",
        etage = 0,
        etageLibelle = "RDC",
        frequente = true
    ),
    Destination(
        id = "2",
        nom = "Bloc Opératoire",
        batiment = "A",
        etage = 1,
        etageLibelle = "Étage 1",
        frequente = true
    ),
    Destination(
        id = "3",
        nom = "Urgences",
        batiment = "C",
        etage = 0,
        etageLibelle = "RDC",
        frequente = true
    )
)

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Gray300,
                shape = RoundedCornerShape(12.dp)
            )
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(20.dp)
        )

        if (value.isEmpty()) {
            Text(
                text = "Rechercher un service...",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* Focus input */ }
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray900,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DestinationOption(
    destination: Destination,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Blue600 else Gray200,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) Blue100.copy(alpha = 0.3f) else White)
            .clickable { onSelect() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Blue600 else Blue100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = if (isSelected) White else Blue600,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Informations
            Column {
                Text(
                    text = destination.nom,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900
                )
                Text(
                    text = destination.localisationFormattee,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }

        // Radio button
        RadioButton(
            selected = isSelected,
            onClick = { onSelect() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Blue600,
                unselectedColor = Gray300
            )
        )
    }
}

@Composable
private fun ConfirmDestinationButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Blue600 else Gray300)
            .clickable(enabled = enabled) { onClick() }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DestinationScreenPreview() {
    HuyBrancardageTheme {
        DestinationScreen()
    }
}
