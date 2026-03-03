package com.example.huybrancardage.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.ui.theme.BrancardageTextField
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.PrimaryButton
import com.example.huybrancardage.ui.viewmodel.SearchViewModel

/**
 * Écran de recherche manuelle de patient
 * Formulaire avec nom, prénom, IPP et numéro de sécurité sociale
 */
@Composable
fun RechercheManuelleScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPatientSelected: (Patient) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Recherche patient",
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Contenu
        if (uiState.hasSearched && uiState.results.isNotEmpty()) {
            // Afficher les résultats
            SearchResultsList(
                results = uiState.results,
                onPatientClick = onPatientSelected,
                onNewSearch = { viewModel.clearSearch() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Afficher le formulaire de recherche
            SearchForm(
                nom = uiState.nom,
                prenom = uiState.prenom,
                ipp = uiState.ipp,
                numeroSecu = uiState.numeroSecuriteSociale,
                isLoading = uiState.isLoading,
                error = uiState.error,
                canSearch = uiState.canSearch,
                hasSearched = uiState.hasSearched,
                onNomChange = viewModel::setNom,
                onPrenomChange = viewModel::setPrenom,
                onIppChange = viewModel::setIpp,
                onNumeroSecuChange = viewModel::setNumeroSecuriteSociale,
                onSearchClick = viewModel::search,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SearchForm(
    nom: String,
    prenom: String,
    ipp: String,
    numeroSecu: String,
    isLoading: Boolean,
    error: String?,
    canSearch: Boolean,
    hasSearched: Boolean,
    onNomChange: (String) -> Unit,
    onPrenomChange: (String) -> Unit,
    onIppChange: (String) -> Unit,
    onNumeroSecuChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Instructions
        Text(
            text = "Saisissez au moins un critère pour identifier le patient.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Formulaire de recherche
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Champ Nom
            BrancardageTextField(
                value = nom,
                onValueChange = onNomChange,
                label = "Nom",
                placeholder = "Ex: Dupont",
                modifier = Modifier.fillMaxWidth()
            )

            // Champ Prénom
            BrancardageTextField(
                value = prenom,
                onValueChange = onPrenomChange,
                label = "Prénom",
                placeholder = "Ex: Jean",
                modifier = Modifier.fillMaxWidth()
            )

            // Séparateur "OU"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OU",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Champ IPP
            BrancardageTextField(
                value = ipp,
                onValueChange = onIppChange,
                label = "Numéro de patient (IPP)",
                placeholder = "Ex: 123456789",
                modifier = Modifier.fillMaxWidth()
            )

            // Champ Numéro de sécu
            BrancardageTextField(
                value = numeroSecu,
                onValueChange = onNumeroSecuChange,
                label = "Numéro de sécurité sociale",
                placeholder = "Ex: 1 80 05 75...",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Message d'erreur
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Message "aucun résultat"
        if (hasSearched && error == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucun patient trouvé avec ces critères.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Bouton de recherche
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PrimaryButton(
                text = "Rechercher",
                onClick = onSearchClick,
                enabled = canSearch,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<Patient>,
    onPatientClick: (Patient) -> Unit,
    onNewSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "${results.size} patient(s) trouvé(s)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(results) { patient ->
                PatientResultCard(
                    patient = patient,
                    onClick = { onPatientClick(patient) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Nouvelle recherche",
            onClick = onNewSearch,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PatientResultCard(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Blue600.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Infos patient
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.nomComplet,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "IPP: ${patient.ipp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                if (patient.service != null) {
                    Text(
                        text = patient.localisationFormattee,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RechercheManuelleScreenPreview() {
    HuyBrancardageTheme {
        RechercheManuelleScreen()
    }
}


