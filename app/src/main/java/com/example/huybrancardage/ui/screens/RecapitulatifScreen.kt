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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray100
import com.example.huybrancardage.ui.theme.Gray200
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

/**
 * Écran récapitulatif avant validation
 * Affiche toutes les informations collectées pour la demande
 */
@Composable
fun RecapitulatifScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onValidateClick: () -> Unit = {},
    onEditPatient: () -> Unit = {},
    onEditTrajet: () -> Unit = {},
    onEditMedias: () -> Unit = {}
) {
    // Données mockées
    val patient = MockRecapPatient(
        nom = "Jean Dupont",
        initiales = "JD",
        ipp = "123456789"
    )

    val trajet = MockTrajet(
        depart = "Bâtiment A - Cardiologie (Ch. 204)",
        arrivee = "Bloc Opératoire (Bâtiment A - Étage 1)"
    )

    val mediasCount = 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Récapitulatif",
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
                text = "Vérifiez la demande",
                style = MaterialTheme.typography.titleLarge,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Assurez-vous que toutes les informations sont correctes avant de valider.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Patient
            RecapSection(
                title = "PATIENT",
                onEditClick = onEditPatient
            ) {
                PatientRecapContent(patient = patient)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Trajet
            RecapSection(
                title = "TRAJET",
                onEditClick = onEditTrajet
            ) {
                TrajetRecapContent(trajet = trajet)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Médias
            RecapSection(
                title = "MÉDIAS JOINTS ($mediasCount)",
                onEditClick = onEditMedias
            ) {
                MediasRecapContent()
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton valider
            ValidateButton(onClick = onValidateClick)
        }
    }
}

@Composable
private fun RecapSection(
    title: String,
    onEditClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                letterSpacing = 1.sp
            )
            Text(
                text = "Modifier",
                style = MaterialTheme.typography.labelSmall,
                color = Blue600,
                modifier = Modifier.clickable { onEditClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun PatientRecapContent(patient: MockRecapPatient) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Blue100),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = patient.initiales,
                style = MaterialTheme.typography.labelLarge,
                color = Blue600
            )
        }

        Column {
            Text(
                text = patient.nom,
                style = MaterialTheme.typography.titleMedium,
                color = Gray900
            )
            Text(
                text = "IPP: ${patient.ipp}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun TrajetRecapContent(trajet: MockTrajet) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Ligne de connexion
        Column(
            modifier = Modifier.padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Point départ
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Blue600)
            )
            // Ligne
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .background(Gray200)
            )
            // Point arrivée
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14B8A6))
            )
        }

        // Informations
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = "Départ",
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray900
                )
                Text(
                    text = trajet.depart,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }

            Column {
                Text(
                    text = "Arrivée",
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray900
                )
                Text(
                    text = trajet.arrivee,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
private fun MediasRecapContent() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Miniature simulée
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray200),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ValidateButton(onClick: () -> Unit) {
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
            text = "Valider la demande",
            style = MaterialTheme.typography.labelLarge,
            color = White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Data classes pour les données mockées
private data class MockRecapPatient(
    val nom: String,
    val initiales: String,
    val ipp: String
)

private data class MockTrajet(
    val depart: String,
    val arrivee: String
)

// Extension pour letter spacing
private val Int.sp: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecapitulatifScreenPreview() {
    HuyBrancardageTheme {
        RecapitulatifScreen()
    }
}





