package com.example.huybrancardage.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme

/**
 * Indicateur de statut de connexion réseau.
 *
 * # Objectif pédagogique
 *
 * Ce composant illustre plusieurs concepts de Jetpack Compose :
 *
 * ## 1. Animation d'état
 * - `animateColorAsState` : Animation fluide des couleurs
 * - `AnimatedVisibility` : Apparition/disparition animée
 *
 * ## 2. Conditional Rendering
 * L'affichage change selon l'état de connexion et le nombre de demandes en attente.
 *
 * ## 3. Composable stateless
 * Le composant reçoit son état en paramètre et ne le gère pas lui-même.
 * Cela facilite les tests et la réutilisation.
 *
 * @param isConnected true si le réseau est disponible
 * @param pendingCount Nombre de demandes en attente de synchronisation
 * @param isSyncing true si une synchronisation est en cours
 * @param modifier Modificateur Compose
 */
@Composable
fun NetworkStatusIndicator(
    isConnected: Boolean,
    pendingCount: Int = 0,
    isSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animation de la couleur de fond
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isConnected -> Color(0xFFDC2626) // Rouge - Hors ligne
            isSyncing -> Color(0xFFF59E0B) // Orange - Synchronisation
            pendingCount > 0 -> Color(0xFFF59E0B) // Orange - En attente
            else -> Color(0xFF16A34A) // Vert - En ligne
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    // Animation de rotation pour l'icône de sync
    val rotation by animateFloatAsState(
        targetValue = if (isSyncing) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "rotation"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pastille de statut
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        )

        // Icône
        Icon(
            imageVector = when {
                !isConnected -> Icons.Default.WifiOff
                isSyncing -> Icons.Default.Sync
                else -> Icons.Default.CloudQueue
            },
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    if (isSyncing) {
                        rotationZ = rotation
                    }
                }
        )

        // Texte de statut
        Text(
            text = when {
                !isConnected -> "Hors ligne"
                isSyncing -> "Synchronisation..."
                pendingCount > 0 -> "$pendingCount en attente"
                else -> "En ligne"
            },
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor
        )
    }
}

/**
 * Bannière de statut hors ligne.
 *
 * Affichée en haut de l'écran quand la connexion est perdue.
 * Disparaît automatiquement quand la connexion revient.
 *
 * @param isVisible true pour afficher la bannière
 * @param pendingCount Nombre de demandes en attente
 * @param modifier Modificateur Compose
 */
@Composable
fun OfflineBanner(
    isVisible: Boolean,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFDC2626))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = if (pendingCount > 0) {
                    "Mode hors ligne • $pendingCount demande${if (pendingCount > 1) "s" else ""} en attente"
                } else {
                    "Mode hors ligne • Les demandes seront envoyées au retour de la connexion"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Badge indiquant le nombre de demandes en attente.
 *
 * Affiché sur l'écran d'accueil pour informer l'utilisateur
 * qu'il y a des demandes non synchronisées.
 *
 * @param count Nombre de demandes en attente
 * @param modifier Modificateur Compose
 */
@Composable
fun PendingBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF59E0B))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

// ============================================================
// Previews
// ============================================================

@Preview(showBackground = true)
@Composable
private fun NetworkStatusIndicator_Online_Preview() {
    HuyBrancardageTheme {
        NetworkStatusIndicator(
            isConnected = true,
            pendingCount = 0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkStatusIndicator_Offline_Preview() {
    HuyBrancardageTheme {
        NetworkStatusIndicator(
            isConnected = false,
            pendingCount = 2,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkStatusIndicator_Pending_Preview() {
    HuyBrancardageTheme {
        NetworkStatusIndicator(
            isConnected = true,
            pendingCount = 3,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkStatusIndicator_Syncing_Preview() {
    HuyBrancardageTheme {
        NetworkStatusIndicator(
            isConnected = true,
            pendingCount = 2,
            isSyncing = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBanner_Preview() {
    HuyBrancardageTheme {
        OfflineBanner(
            isVisible = true,
            pendingCount = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PendingBadge_Preview() {
    HuyBrancardageTheme {
        PendingBadge(
            count = 3,
            modifier = Modifier.padding(16.dp)
        )
    }
}

