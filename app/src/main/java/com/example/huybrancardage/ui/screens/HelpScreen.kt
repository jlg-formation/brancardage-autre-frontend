package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.huybrancardage.util.IntentUtils

/**
 * Écran d'aide et d'informations sur l'application.
 *
 * Cet écran est affiché dans HelpActivity, lancée via un Intent explicite.
 * Il contient :
 * - Informations sur l'application
 * - Guide d'utilisation
 * - Contacts du service
 * - Mentions légales
 *
 * @param modifier Modificateur Compose
 * @param onBackClick Callback pour le bouton retour
 */
@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        HelpTopBar(onBackClick = onBackClick)

        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section : À propos
            AboutSection()

            // Section : Guide d'utilisation
            UsageGuideSection()

            // Section: Contacts
            ContactSection(
                onPhoneClick = { IntentUtils.dialBrancardageService(context) },
                onEmailClick = {
                    IntentUtils.sendBrancardageEmail(
                        context = context,
                        patientName = "Support",
                        patientIpp = "N/A",
                        depart = "N/A",
                        destination = "N/A"
                    )
                }
            )

            // Section : Mentions légales
            LegalSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue600)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = White
            )
        }
        Text(
            text = "Aide & Informations",
            style = MaterialTheme.typography.titleLarge,
            color = White
        )
    }
}

@Composable
private fun AboutSection() {
    HelpCard(
        title = "À propos",
        icon = Icons.Default.Info
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo/Icône de l'app
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Blue100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column {
                    Text(
                        text = "HuyBrancardage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }

            HorizontalDivider(color = Gray100)

            InfoRow(label = "Développeur", value = "DSI - Centre Hospitalier de Huy")
            InfoRow(label = "Année", value = "2026")
            InfoRow(label = "Plateforme", value = "Android (Kotlin / Jetpack Compose)")
        }
    }
}

@Composable
private fun UsageGuideSection() {
    HelpCard(
        title = "Guide d'utilisation",
        icon = Icons.AutoMirrored.Filled.Help
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuideStep(
                stepNumber = 1,
                title = "Identifier le patient",
                description = "Scannez le bracelet du patient ou effectuez une recherche manuelle par nom, IPP ou numéro de sécurité sociale.",
                icon = Icons.Default.QrCodeScanner
            )
            GuideStep(
                stepNumber = 2,
                title = "Vérifier les informations",
                description = "Consultez le dossier patient et vérifiez les alertes médicales importantes (allergies, précautions).",
                icon = Icons.Default.Search
            )
            GuideStep(
                stepNumber = 3,
                title = "Ajouter des médias",
                description = "Prenez des photos ou ajoutez des documents si nécessaire pour la demande de brancardage.",
                icon = Icons.Default.Camera
            )
            GuideStep(
                stepNumber = 4,
                title = "Définir le trajet",
                description = "Indiquez la localisation de départ et la destination du patient.",
                icon = Icons.Default.LocalHospital
            )
        }
    }
}

@Composable
private fun ContactSection(
    onPhoneClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    HelpCard(
        title = "Contacts",
        icon = Icons.Default.Phone
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Service de brancardage",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )

            // Bouton téléphone
            ContactButton(
                icon = Icons.Default.Phone,
                label = "Appeler le standard",
                sublabel = "+32 111 222 333",
                onClick = onPhoneClick
            )

            // Bouton email
            ContactButton(
                icon = Icons.Default.Email,
                label = "Envoyer un email",
                sublabel = "brancardage@hopital.fr",
                onClick = onEmailClick
            )

            HorizontalDivider(color = Gray100)

            Text(
                text = "Horaires: 7h - 19h (7j/7)",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun LegalSection() {
    HelpCard(
        title = "Mentions légales",
        icon = Icons.Default.Gavel
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Protection des données",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
            Text(
                text = "Cette application traite des données de santé à caractère personnel. " +
                        "Conformément au RGPD et à la loi relative à l'informatique, aux fichiers " +
                        "et aux libertés, vous disposez d'un droit d'accès, de rectification et " +
                        "de suppression de vos données.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Confidentialité",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
            Text(
                text = "Les données collectées sont exclusivement utilisées dans le cadre de la " +
                        "gestion des demandes de brancardage et ne sont pas partagées avec des tiers.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

// ============== Composants réutilisables ==============

@Composable
private fun HelpCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête de la carte
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenu
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
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
            fontWeight = FontWeight.Medium,
            color = Gray900
        )
    }
}

@Composable
private fun GuideStep(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Numéro d'étape
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Blue600),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

@Composable
private fun ContactButton(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray900
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelpScreenPreview() {
    HuyBrancardageTheme {
        HelpScreen()
    }
}

