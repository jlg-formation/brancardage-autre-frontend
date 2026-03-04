package com.example.huybrancardage.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.ui.state.BrancardageSessionState
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
import com.example.huybrancardage.ui.viewmodel.BrancardageViewModel
import com.example.huybrancardage.ui.viewmodel.SubmissionState
import com.example.huybrancardage.util.IntentUtils

/**
 * Écran récapitulatif avant validation
 * Affiche toutes les informations collectées pour la demande
 */
@Composable
fun RecapitulatifScreen(
    modifier: Modifier = Modifier,
    brancardageViewModel: BrancardageViewModel? = null,
    onBackClick: () -> Unit = {},
    onValidateSuccess: (String, String) -> Unit = { _, _ -> }, // trackingNumber, patientName
    onEditPatient: () -> Unit = {},
    onEditTrajet: () -> Unit = {},
    onEditMedias: () -> Unit = {}
) {
    val context = LocalContext.current

    // Collecter l'état de la session
    val sessionState by brancardageViewModel?.sessionState?.collectAsState()
        ?: androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf(BrancardageSessionState())
        }

    val submissionState by brancardageViewModel?.submissionState?.collectAsState()
        ?: androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf<SubmissionState>(SubmissionState.Idle)
        }

    // Gérer le succès de la soumission
    when (val state = submissionState) {
        is SubmissionState.Success -> {
            val patientName = sessionState.patient?.nomComplet ?: "Patient"
            onValidateSuccess(state.response.id, patientName)
            brancardageViewModel?.resetSubmissionState()
        }
        else -> { /* Continue displaying the screen */ }
    }

    // Données de la session ou données mockées
    val patient = sessionState.patient
    val localisation = sessionState.localisation
    val destination = sessionState.destination
    val medias = sessionState.medias

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                    if (patient != null) {
                        PatientRecapContent(
                            nom = patient.nomComplet,
                            initiales = patient.initiales,
                            ipp = patient.ipp
                        )
                    } else {
                        Text(
                            text = "Aucun patient sélectionné",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Trajet
                RecapSection(
                    title = "TRAJET",
                    onEditClick = onEditTrajet
                ) {
                    TrajetRecapContent(
                        depart = localisation?.descriptionFormattee ?: "Non défini",
                        arrivee = destination?.let { "${it.nom} (${it.localisationFormattee})" } ?: "Non défini"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Médias
                RecapSection(
                    title = "MÉDIAS JOINTS (${medias.size})",
                    onEditClick = onEditMedias
                ) {
                    if (medias.isNotEmpty()) {
                        MediasRecapContent(medias = medias)
                    } else {
                        Text(
                            text = "Aucun média joint",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Boutons d'action secondaires (Appeler et Partager)
                ActionButtonsRow(
                    onCallClick = {
                        IntentUtils.dialBrancardageService(context)
                    },
                    onShareClick = {
                        val patientName = patient?.nomComplet ?: "Patient inconnu"
                        val patientIpp = patient?.ipp ?: "N/A"
                        val departText = localisation?.descriptionFormattee ?: "Non défini"
                        val destinationText = destination?.let { "${it.nom} (${it.localisationFormattee})" } ?: "Non défini"

                        IntentUtils.shareBrancardageRequest(
                            context = context,
                            patientName = patientName,
                            patientIpp = patientIpp,
                            depart = departText,
                            destination = destinationText,
                            mediaCount = medias.size
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bouton valider
                ValidateButton(
                    onClick = {
                        brancardageViewModel?.submitBrancardage(context)
                    },
                    enabled = sessionState.isReadyForValidation && submissionState !is SubmissionState.Loading
                )
            }
        }

        // Loading overlay
        if (submissionState is SubmissionState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Envoi en cours...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = White
                    )
                }
            }
        }

        // Error dialog
        if (submissionState is SubmissionState.Error) {
            AlertDialog(
                onDismissRequest = { brancardageViewModel?.resetSubmissionState() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                },
                title = { Text("Erreur") },
                text = { Text((submissionState as SubmissionState.Error).message) },
                confirmButton = {
                    TextButton(onClick = { brancardageViewModel?.resetSubmissionState() }) {
                        Text("OK")
                    }
                }
            )
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
private fun PatientRecapContent(
    nom: String,
    initiales: String,
    ipp: String
) {
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
                text = initiales,
                style = MaterialTheme.typography.labelLarge,
                color = Blue600
            )
        }

        Column {
            Text(
                text = nom,
                style = MaterialTheme.typography.titleMedium,
                color = Gray900
            )
            Text(
                text = "IPP: $ipp",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun TrajetRecapContent(
    depart: String,
    arrivee: String
) {
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
                    text = depart,
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
                    text = arrivee,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
private fun MediasRecapContent(medias: List<Media> = emptyList()) {
    val context = LocalContext.current

    if (medias.isEmpty()) {
        // Miniature placeholder
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
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        items(medias) { media ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(media.uri))
                            .crossfade(true)
                            .build(),
                        contentDescription = media.description ?: "Média",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ValidateButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val backgroundColor = if (enabled) Blue600 else Gray200
    val textColor = if (enabled) White else Gray500

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Valider la demande",
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
    }
}


// Extension pour letter spacing
private val Int.sp: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

/**
 * Boutons d'action secondaires : Appeler le service et Partager
 */
@Composable
private fun ActionButtonsRow(
    onCallClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bouton Appeler
        ActionSecondaryButton(
            text = "Appeler",
            icon = Icons.Default.Phone,
            onClick = onCallClick,
            modifier = Modifier.weight(1f)
        )

        // Bouton Partager
        ActionSecondaryButton(
            text = "Partager",
            icon = Icons.Default.Share,
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Bouton d'action secondaire avec icône
 */
@Composable
private fun ActionSecondaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Blue600,
                shape = RoundedCornerShape(12.dp)
            )
            .background(White)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Blue600,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Blue600
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecapitulatifScreenPreview() {
    HuyBrancardageTheme {
        RecapitulatifScreen()
    }
}





