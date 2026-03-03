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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
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
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray100
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White
import com.example.huybrancardage.ui.viewmodel.PatientViewModel

/**
 * Écran de dossier patient
 * Affiche les informations du patient identifié avec alertes éventuelles
 */
@Composable
fun DossierPatientScreen(
    modifier: Modifier = Modifier,
    patientViewModel: PatientViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onCreateRequestClick: () -> Unit = {}
) {
    val uiState by patientViewModel.uiState.collectAsState()

    // Utiliser le patient du ViewModel ou des données mockées
    val patient = uiState.patient?.toMockPatient() ?: MockPatient(
        nom = "Jean Dupont",
        initiales = "JD",
        genre = "Homme",
        age = 42,
        ipp = "123456789",
        dateNaissance = "12/05/1980",
        chambre = "Cardiologie - 204",
        alertes = listOf(
            PatientAlerte(
                titre = "Précautions",
                description = "Patient sous perfusion. Nécessite une potence à sérum."
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Dossier Patient",
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
            // Carte d'identité patient
            PatientIdentityCard(patient = patient)

            Spacer(modifier = Modifier.height(16.dp))

            // Alertes médicales
            if (patient.alertes.isNotEmpty()) {
                patient.alertes.forEach { alerte ->
                    AlerteCard(alerte = alerte)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bouton d'action principal
            PrimaryButtonWithIcon(
                text = "Créer une demande",
                onClick = onCreateRequestClick
            )
        }
    }
}

@Composable
private fun PatientIdentityCard(patient: MockPatient) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(20.dp)
    ) {
        // En-tête avec avatar et nom
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Blue100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = patient.initiales,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Blue600
                )
            }

            Column {
                Text(
                    text = patient.nom,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Gray900
                )
                Text(
                    text = "${patient.genre}, ${patient.age} ans",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Gray100)
        Spacer(modifier = Modifier.height(16.dp))

        // Informations détaillées
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PatientInfoRow(label = "IPP", value = patient.ipp)
            PatientInfoRow(label = "Né(e) le", value = patient.dateNaissance)
            PatientInfoRow(label = "Chambre", value = patient.chambre)
        }
    }
}

@Composable
private fun PatientInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray900
        )
    }
}

@Composable
private fun AlerteCard(alerte: PatientAlerte) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF2F2))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(20.dp)
        )

        Column {
            Text(
                text = alerte.titre,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF991B1B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alerte.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFDC2626)
            )
        }
    }
}

@Composable
private fun PrimaryButtonWithIcon(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Blue600)
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = White
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Data classes pour les données mockées
private data class MockPatient(
    val nom: String,
    val initiales: String,
    val genre: String,
    val age: Int,
    val ipp: String,
    val dateNaissance: String,
    val chambre: String,
    val alertes: List<PatientAlerte> = emptyList()
)

private data class PatientAlerte(
    val titre: String,
    val description: String
)

/**
 * Convertit un Patient du domaine en MockPatient pour l'affichage
 */
private fun Patient.toMockPatient(): MockPatient {
    val genreText = when (sexe.name) {
        "MASCULIN" -> "Homme"
        "FEMININ" -> "Femme"
        else -> sexe.libelle
    }

    val dateFormatee = "${dateNaissance.dayOfMonth.toString().padStart(2, '0')}/${dateNaissance.monthValue.toString().padStart(2, '0')}/${dateNaissance.year}"

    return MockPatient(
        nom = nomComplet,
        initiales = initiales,
        genre = genreText,
        age = age,
        ipp = ipp,
        dateNaissance = dateFormatee,
        chambre = localisationFormattee,
        alertes = alertesMedicales.map { alerte ->
            PatientAlerte(
                titre = alerte.titre,
                description = alerte.description ?: ""
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DossierPatientScreenPreview() {
    HuyBrancardageTheme {
        DossierPatientScreen()
    }
}





