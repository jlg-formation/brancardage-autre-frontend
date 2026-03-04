package com.example.huybrancardage.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.huybrancardage.data.media.MediaManager
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
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
import com.example.huybrancardage.ui.viewmodel.MediaUiState
import com.example.huybrancardage.ui.viewmodel.MediaViewModel

/**
 * Écran de gestion des médias
 * Permet d'ajouter des photos et documents à la demande de brancardage
 */
@Composable
fun MediasScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel? = null,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onScanDocumentClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Initialiser le MediaManager si viewModel est fourni
    LaunchedEffect(viewModel) {
        viewModel?.initMediaManager(MediaManager(context))
    }

    // État du ViewModel ou état mock pour la preview
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(MediaUiState()) }

    // État pour gérer les actions caméra/galerie
    var pendingAction by remember { mutableStateOf<MediaAction?>(null) }
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher pour la permission caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingAction != null) {
            when (pendingAction) {
                MediaAction.TAKE_PHOTO -> {
                    val uri = viewModel?.prepareTakePicture()
                    if (uri != null) {
                        currentCameraUri = uri
                    }
                }
                MediaAction.SCAN_DOCUMENT -> {
                    val uri = viewModel?.prepareTakePicture()
                    if (uri != null) {
                        currentCameraUri = uri
                    }
                }
                else -> {}
            }
        }
        pendingAction = null
    }

    // Launcher pour prendre une photo
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        when (pendingAction) {
            MediaAction.TAKE_PHOTO -> viewModel?.onPhotoTaken(success)
            MediaAction.SCAN_DOCUMENT -> viewModel?.onDocumentScanned(success)
            else -> viewModel?.onPhotoTaken(success)
        }
        currentCameraUri = null
        pendingAction = null
    }

    // Launcher pour sélectionner une image depuis la galerie
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel?.onGalleryImageSelected(it) }
        pendingAction = null
    }

    // Lancer la caméra quand l'URI est prête
    LaunchedEffect(currentCameraUri) {
        currentCameraUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    // Fonctions pour déclencher les actions
    val onTakePhoto: () -> Unit = {
        pendingAction = MediaAction.TAKE_PHOTO
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val onScanDocument: () -> Unit = {
        pendingAction = MediaAction.SCAN_DOCUMENT
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val onPickFromGallery: () -> Unit = {
        pendingAction = MediaAction.PICK_GALLERY
        pickImageLauncher.launch("image/*")
    }

    MediasScreenContent(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onSkipClick = onSkipClick,
        onContinueClick = onContinueClick,
        onTakePhotoClick = if (viewModel != null) onTakePhoto else onTakePhotoClick,
        onScanDocumentClick = if (viewModel != null) onScanDocument else onScanDocumentClick,
        onPickGalleryClick = onPickFromGallery,
        onDeleteMedia = { mediaId -> viewModel?.removeMedia(mediaId) },
        onDismissError = { viewModel?.clearError() },
        formatFileSize = { bytes -> viewModel?.formatFileSize(bytes) ?: formatFileSizeDefault(bytes) }
    )
}

/**
 * Actions possibles pour les médias
 */
private enum class MediaAction {
    TAKE_PHOTO,
    SCAN_DOCUMENT,
    PICK_GALLERY
}

/**
 * Contenu de l'écran (séparé pour faciliter les previews)
 */
@Composable
private fun MediasScreenContent(
    modifier: Modifier = Modifier,
    uiState: MediaUiState,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onContinueClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onScanDocumentClick: () -> Unit,
    onPickGalleryClick: () -> Unit,
    onDeleteMedia: (String) -> Unit,
    onDismissError: () -> Unit,
    formatFileSize: (Long) -> String
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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

                // Boutons d'actions - 3 options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddMediaButton(
                        icon = Icons.Default.CameraAlt,
                        label = "Prendre une photo",
                        onClick = onTakePhotoClick,
                        modifier = Modifier.weight(1f)
                    )

                    AddMediaButton(
                        icon = Icons.Default.PhotoLibrary,
                        label = "Galerie",
                        onClick = onPickGalleryClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bouton scanner document (pleine largeur)
                AddMediaButton(
                    icon = Icons.Default.Description,
                    label = "Scanner un document",
                    onClick = onScanDocumentClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Liste des médias ajoutés
                if (uiState.medias.isNotEmpty()) {
                    Text(
                        text = "Médias joints (${uiState.count})",
                        style = MaterialTheme.typography.titleSmall,
                        color = Gray900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.medias.forEach { media ->
                        MediaItemCard(
                            media = media,
                            formatFileSize = formatFileSize,
                            onDeleteClick = { onDeleteMedia(media.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // Message quand pas de médias
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun média ajouté",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Bouton continuer
                ContinueButton(onClick = onContinueClick)
            }
        }

        // Indicateur de chargement
        if (uiState.isProcessingPhoto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Blue600)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Traitement de l'image...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                }
            }
        }

        // Affichage des erreurs
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onDismissError) {
                        Text("OK", color = White)
                    }
                }
            ) {
                Text(uiState.error)
            }
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
    media: Media,
    formatFileSize: (Long) -> String,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current

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
            // Aperçu miniature
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray200),
                contentAlignment = Alignment.Center
            ) {
                val uri = Uri.parse(media.uri)
                if (media.uri.startsWith("content://") || media.uri.startsWith("file://")) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback pour les URIs mockées
                    Icon(
                        imageVector = if (media.type == MediaType.DOCUMENT)
                            Icons.Default.Description
                        else
                            Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = media.description ?: "Sans titre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray900
                )
                Text(
                    text = "${if (media.type == MediaType.DOCUMENT) "Document" else "Photo"} • ${formatFileSize(media.taille)}",
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

/**
 * Formate la taille par défaut (pour preview)
 */
private fun formatFileSizeDefault(bytes: Long): String {
    return when {
        bytes >= 1_000_000 -> String.format("%.1f Mo", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.1f Ko", bytes / 1_000.0)
        else -> "$bytes octets"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediasScreenPreview() {
    HuyBrancardageTheme {
        MediasScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediasScreenWithItemsPreview() {
    HuyBrancardageTheme {
        MediasScreenContent(
            uiState = MediaUiState(
                medias = listOf(
                    Media(
                        id = "1",
                        uri = "content://mock/photo1.jpg",
                        type = MediaType.PHOTO,
                        taille = 1_200_000,
                        description = "Potence à sérum"
                    ),
                    Media(
                        id = "2",
                        uri = "content://mock/doc1.jpg",
                        type = MediaType.DOCUMENT,
                        taille = 800_000,
                        description = "Fiche de transfert"
                    )
                )
            ),
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            onTakePhotoClick = {},
            onScanDocumentClick = {},
            onPickGalleryClick = {},
            onDeleteMedia = {},
            onDismissError = {},
            formatFileSize = { formatFileSizeDefault(it) }
        )
    }
}









