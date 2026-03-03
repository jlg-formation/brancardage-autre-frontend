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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

/**
 * Écran de localisation GPS
 * Affiche la position détectée et permet de l'affiner
 */
@Composable
fun LocalisationScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    // Position mockée
    val localisation = MockLocalisation(
        batiment = "Bâtiment A - Cardiologie",
        detail = "Étage 2, Chambre 204"
    )

    var precisions by remember { mutableStateOf("") }

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
                text = "La position a été détectée automatiquement via GPS.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Carte de localisation
            LocalisationCard(localisation = localisation)

            Spacer(modifier = Modifier.height(24.dp))

            // Section modification manuelle
            Text(
                text = "Affiner la position (Optionnel)",
                style = MaterialTheme.typography.titleSmall,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrancardageTextField(
                value = precisions,
                onValueChange = { precisions = it },
                label = "Précisions pour le brancardier",
                placeholder = "Ex: Attendre devant l'ascenseur",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton confirmer
            ConfirmButton(
                text = "Confirmer le départ",
                onClick = onConfirmClick
            )
        }
    }
}

@Composable
private fun LocalisationCard(localisation: MockLocalisation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
    ) {
        // Carte simulée (zone grise)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(Gray200),
            contentAlignment = Alignment.Center
        ) {
            // Marqueur de position
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Blue600,
                modifier = Modifier.size(32.dp)
            )
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
            Column {
                Text(
                    text = localisation.batiment,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localisation.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
private fun ConfirmButton(
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
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Data class pour la localisation mockée
private data class MockLocalisation(
    val batiment: String,
    val detail: String
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LocalisationScreenPreview() {
    HuyBrancardageTheme {
        LocalisationScreen()
    }
}

