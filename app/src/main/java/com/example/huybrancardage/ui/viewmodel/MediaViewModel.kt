package com.example.huybrancardage.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.media.MediaManager
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * État de l'écran de gestion des médias
 */
data class MediaUiState(
    val medias: List<Media> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessingPhoto: Boolean = false,
    val error: String? = null,
    val pendingCameraUri: Uri? = null
) {
    /**
     * Nombre de médias
     */
    val count: Int get() = medias.size

    /**
     * Vérifie si des médias sont présents
     */
    val hasMedias: Boolean get() = medias.isNotEmpty()
}

/**
 * ViewModel pour l'écran de gestion des médias
 * Gère la prise de photo, l'import depuis la galerie et la compression des images
 */
class MediaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    private var mediaManager: MediaManager? = null

    /**
     * Initialise le MediaManager avec le contexte
     * Doit être appelé une fois le contexte disponible
     */
    fun initMediaManager(manager: MediaManager) {
        mediaManager = manager
    }

    /**
     * Prépare la prise de photo en créant un URI temporaire
     * @return L'URI où l'image sera sauvegardée
     */
    fun prepareTakePicture(): Uri? {
        val manager = mediaManager ?: return null
        val uri = manager.createTempImageUri()
        _uiState.update { it.copy(pendingCameraUri = uri) }
        return uri
    }

    /**
     * Traite le résultat de la prise de photo
     * Compresse l'image et l'ajoute à la liste des médias
     */
    fun onPhotoTaken(success: Boolean, description: String? = null) {
        val uri = _uiState.value.pendingCameraUri ?: return

        if (!success) {
            _uiState.update { it.copy(pendingCameraUri = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPhoto = true, error = null) }

            try {
                val manager = mediaManager
                if (manager != null) {
                    // Compresser l'image
                    val compressedUri = manager.compressImage(uri) ?: uri

                    // Récupérer la taille du fichier compressé
                    val fileSize = manager.getFileSize(compressedUri)

                    val media = Media(
                        id = UUID.randomUUID().toString(),
                        uri = compressedUri.toString(),
                        type = MediaType.PHOTO,
                        mimeType = "image/jpeg",
                        taille = fileSize,
                        dateAjout = Instant.now(),
                        description = description ?: "Photo"
                    )
                    addMedia(media)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erreur lors du traitement de la photo") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false, pendingCameraUri = null) }
            }
        }
    }

    /**
     * Traite une image sélectionnée depuis la galerie
     * Compresse l'image et l'ajoute à la liste des médias
     */
    fun onGalleryImageSelected(uri: Uri, description: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPhoto = true, error = null) }

            try {
                val manager = mediaManager
                if (manager != null) {
                    // Compresser l'image
                    val compressedUri = manager.compressImage(uri) ?: uri

                    // Récupérer la taille et le nom du fichier
                    val fileSize = manager.getFileSize(compressedUri)
                    val fileName = manager.getFileName(uri)

                    val media = Media(
                        id = UUID.randomUUID().toString(),
                        uri = compressedUri.toString(),
                        type = MediaType.PHOTO,
                        mimeType = "image/jpeg",
                        taille = fileSize,
                        dateAjout = Instant.now(),
                        description = description ?: fileName ?: "Photo galerie"
                    )
                    addMedia(media)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erreur lors de l'import de l'image") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false) }
            }
        }
    }

    /**
     * Ajoute un document scanné (utilise l'appareil photo avec traitement document)
     */
    fun onDocumentScanned(success: Boolean, description: String? = null) {
        val uri = _uiState.value.pendingCameraUri ?: return

        if (!success) {
            _uiState.update { it.copy(pendingCameraUri = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPhoto = true, error = null) }

            try {
                val manager = mediaManager
                if (manager != null) {
                    // Compresser l'image
                    val compressedUri = manager.compressImage(uri) ?: uri

                    // Récupérer la taille du fichier
                    val fileSize = manager.getFileSize(compressedUri)

                    val media = Media(
                        id = UUID.randomUUID().toString(),
                        uri = compressedUri.toString(),
                        type = MediaType.DOCUMENT,
                        mimeType = "image/jpeg",
                        taille = fileSize,
                        dateAjout = Instant.now(),
                        description = description ?: "Document scanné"
                    )
                    addMedia(media)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erreur lors du scan du document") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false, pendingCameraUri = null) }
            }
        }
    }

    /**
     * Ajoute un média à la liste
     */
    fun addMedia(media: Media) {
        _uiState.update { state ->
            state.copy(medias = state.medias + media, error = null)
        }
    }

    /**
     * Supprime un média par son ID
     */
    fun removeMedia(mediaId: String) {
        _uiState.update { state ->
            state.copy(medias = state.medias.filter { it.id != mediaId })
        }
    }

    /**
     * Met à jour la description d'un média
     */
    fun updateMediaDescription(mediaId: String, description: String) {
        _uiState.update { state ->
            state.copy(
                medias = state.medias.map { media ->
                    if (media.id == mediaId) {
                        media.copy(description = description)
                    } else {
                        media
                    }
                }
            )
        }
    }

    /**
     * Réinitialise les médias
     */
    fun clearMedias() {
        _uiState.value = MediaUiState()
    }

    /**
     * Efface l'erreur affichée
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Définit la liste de médias (pour récupérer depuis la session)
     */
    fun setMedias(medias: List<Media>) {
        _uiState.update { it.copy(medias = medias) }
    }

    /**
     * Récupère tous les médias actuels
     */
    fun getMedias(): List<Media> = _uiState.value.medias

    /**
     * Formate la taille d'un fichier en format lisible
     */
    fun formatFileSize(bytes: Long): String {
        return mediaManager?.formatFileSize(bytes) ?: "${bytes} octets"
    }

    override fun onCleared() {
        super.onCleared()
        // Nettoyer le cache des images temporaires
        mediaManager?.clearCache()
    }
}

