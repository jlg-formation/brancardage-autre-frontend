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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.Blue100
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.Gray100
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.Gray600
import com.example.huybrancardage.ui.theme.Gray900
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

/**
 * Écran d'accueil - Menu principal
 * Permet de choisir entre scanner un bracelet ou faire une recherche manuelle
 */
@Composable
fun AccueilScreen(
    modifier: Modifier = Modifier,
    onScanBraceletClick: () -> Unit = {},
    onRechercheManuelleClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        AccueilTopBar()

        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Titre et sous-titre
            Text(
                text = "Nouvelle demande",
                style = MaterialTheme.typography.headlineMedium,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Identifiez le patient pour commencer.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options de recherche
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Option 1: Scanner le bracelet
                ActionOptionCard(
                    title = "Scanner le bracelet",
                    description = "QR Code ou Code-barres",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    iconBackgroundColor = Blue100,
                    onClick = onScanBraceletClick
                )

                // Option 2: Recherche manuelle
                ActionOptionCard(
                    title = "Recherche manuelle",
                    description = "Nom, Prénom, IPP, Sécu...",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Gray600,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    iconBackgroundColor = Gray100,
                    onClick = onRechercheManuelleClick
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Section demandes récentes
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Demandes récentes",
                style = MaterialTheme.typography.titleSmall,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Demande récente mockée
            RecentRequestCard(
                initials = "ML",
                name = "Marie Laurent",
                time = "Terminé à 10:42"
            )
        }
    }
}

@Composable
private fun AccueilTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue600)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Brancardage",
            style = MaterialTheme.typography.titleLarge,
            color = White
        )
        // Avatar utilisateur
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Blue100),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PS",
                style = MaterialTheme.typography.labelMedium,
                color = Blue600
            )
        }
    }
}

@Composable
private fun ActionOptionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    iconBackgroundColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        // Texte
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun RecentRequestCard(
    initials: String,
    name: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color(0xFFCCFBF1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color(0xFF0D9488)
                )
            }

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray900
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }

        // Icône check
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color(0xFF14B8A6),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccueilScreenPreview() {
    HuyBrancardageTheme {
        AccueilScreen()
    }
}






