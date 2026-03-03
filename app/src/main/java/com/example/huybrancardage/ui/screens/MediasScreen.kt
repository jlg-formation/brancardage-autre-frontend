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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.Gray100
import com.example.huybrancardage.ui.theme.Gray200
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray700
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

/**
 * Écran de gestion des médias
 * Permet d'ajouter des photos et documents à la demande de brancardage
 */
@Composable
fun MediasScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onScanDocumentClick: () -> Unit = {}
) {
    // Médias mockés
    val medias = listOf(
        MockMedia(
            id = "1",
            nom = "Potence à sérum",
            type = "Photo",
            taille = "1.2 Mo"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header avec option "Passer"
        MediasTopBar(
            onBackClick = onBackClick,
            onSkipClick = onSkipClick
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
                text = "Ajouter des informations visuelles",
                style = MaterialTheme.typography.titleLarge,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aidez le brancardier en ajoutant des photos de l'équipement ou des documents médicaux.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Boutons d'actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddMediaButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Prendre une photo",
                    onClick = onTakePhotoClick,
                    modifier = Modifier.weight(1f)
                )

                AddMediaButton(
                    icon = Icons.Default.Description,
                    label = "Scanner un document",
                    onClick = onScanDocumentClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Liste des médias ajoutés
            if (medias.isNotEmpty()) {
                Text(
                    text = "Médias joints (${medias.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray900
                )

                Spacer(modifier = Modifier.height(12.dp))

                medias.forEach { media ->
                    MediaItemCard(
                        media = media,
                        onDeleteClick = { /* Delete media */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton continuer
            ContinueButton(onClick = onContinueClick)
        }
    }
}

@Composable
private fun MediasTopBar(
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue600)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bouton retour
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .padding(8.dp)
            ) {
                Text(
                    text = "←",
                    color = White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Text(
                text = "Médias (Optionnel)",
                style = MaterialTheme.typography.titleLarge,
                color = White
            )
        }

        // Bouton "Passer"
        Text(
            text = "Passer",
            style = MaterialTheme.typography.labelLarge,
            color = Blue100,
            modifier = Modifier
                .clickable { onSkipClick() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun AddMediaButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = Blue100,
                shape = RoundedCornerShape(12.dp)
            )
            .background(White)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Blue100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Blue600,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray700
        )
    }
}

@Composable
private fun MediaItemCard(
    media: MockMedia,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(
                width = 1.dp,
                color = Gray100,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aperçu miniature (simulée)
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

            Column {
                Text(
                    text = media.nom,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray900
                )
                Text(
                    text = "${media.type} • ${media.taille}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }

        // Bouton suppression
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { onDeleteClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ContinueButton(onClick: () -> Unit) {
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
            text = "Continuer",
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

// Data class pour les médias mockés
private data class MockMedia(
    val id: String,
    val nom: String,
    val type: String,
    val taille: String
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediasScreenPreview() {
    HuyBrancardageTheme {
        MediasScreen()
    }
}









