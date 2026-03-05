package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

// Couleurs spécifiques à l'écran de confirmation
private val TealPrimary = Color(0xFF14B8A6)
private val TealLight = Color(0xFF5EEAD4)
private val RedPrimary = Color(0xFFDC2626)
private val RedLight = Color(0xFFFCA5A5)
private val AmberPrimary = Color(0xFFF59E0B) // Pour le mode hors ligne
private val AmberLight = Color(0xFFFCD34D)

/**
 * Écran de confirmation de succès
 * Affiché après la validation réussie d'une demande de brancardage
 *
 * ## Objectif pédagogique - BroadcastReceiver et mode hors ligne
 *
 * Cet écran gère trois états :
 * 1. **Succès (en ligne)** : La demande a été envoyée au serveur
 * 2. **En file d'attente (hors ligne)** : La demande est sauvegardée localement
 * 3. **Erreur** : L'envoi a échoué
 *
 * Le paramètre `isQueued` permet d'afficher un message spécifique
 * informant l'utilisateur que sa demande sera envoyée automatiquement
 * quand la connexion reviendra.
 *
 * @param isQueued true si la demande est en file d'attente (mode hors ligne)
 */
@Composable
fun ConfirmationScreen(
    modifier: Modifier = Modifier,
    patientName: String = "Jean Dupont",
    trackingNumber: String = "BRC-2026-084",
    isSuccess: Boolean = true,
    isQueued: Boolean = false,
    errorMessage: String? = null,
    onReturnHomeClick: () -> Unit = {}
) {
    // Déterminer les couleurs et icônes selon l'état
    val backgroundColor = when {
        isQueued -> AmberPrimary
        isSuccess -> TealPrimary
        else -> RedPrimary
    }
    val lightColor = when {
        isQueued -> AmberLight
        isSuccess -> TealLight
        else -> RedLight
    }
    val iconVector = when {
        isQueued -> Icons.Default.Schedule
        isSuccess -> Icons.Default.Check
        else -> Icons.Default.Close
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Icône de succès/erreur/attente
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = when {
                    isQueued -> "En attente"
                    isSuccess -> "Succès"
                    else -> "Erreur"
                },
                tint = backgroundColor,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Titre
        Text(
            text = when {
                isQueued -> "Demande enregistrée"
                isSuccess -> "Demande envoyée !"
                else -> "Erreur"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message de confirmation, d'attente ou d'erreur
        Text(
            text = when {
                isQueued -> "La demande de brancardage pour $patientName a été enregistrée. " +
                        "Elle sera envoyée automatiquement dès que la connexion sera rétablie."
                isSuccess -> "La demande de brancardage pour $patientName a bien été transmise. " +
                        "Un brancardier va être assigné sous peu."
                else -> errorMessage ?: "Une erreur s'est produite lors de l'envoi de la demande."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = lightColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Carte de statut (numéro de suivi ou message hors ligne)
        when {
            isQueued -> QueuedStatusCard()
            isSuccess -> TrackingNumberCard(trackingNumber = trackingNumber)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton retour à l'accueil
        ReturnHomeButton(
            onClick = onReturnHomeClick,
            accentColor = backgroundColor
        )
    }
}

/**
 * Carte affichant le statut "en file d'attente" pour le mode hors ligne.
 */
@Composable
private fun QueuedStatusCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(alpha = 0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "MODE HORS LIGNE",
            style = MaterialTheme.typography.labelSmall,
            color = AmberLight,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "En attente de connexion",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = White
        )
    }
}

@Composable
private fun TrackingNumberCard(trackingNumber: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(alpha = 0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NUMÉRO DE SUIVI",
            style = MaterialTheme.typography.labelSmall,
            color = TealLight,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = trackingNumber,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = White
        )
    }
}

@Composable
private fun ReturnHomeButton(
    onClick: () -> Unit,
    accentColor: Color = TealPrimary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Retour à l'accueil",
            style = MaterialTheme.typography.labelLarge,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmationScreenPreview() {
    HuyBrancardageTheme {
        ConfirmationScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmationScreenQueuedPreview() {
    HuyBrancardageTheme {
        ConfirmationScreen(
            isQueued = true,
            patientName = "Marie Laurent"
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmationScreenErrorPreview() {
    HuyBrancardageTheme {
        ConfirmationScreen(
            isSuccess = false,
            errorMessage = "Impossible de contacter le serveur"
        )
    }
}
