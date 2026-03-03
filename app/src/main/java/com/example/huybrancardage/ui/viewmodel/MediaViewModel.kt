package com.example.huybrancardage.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID

/**
 * État de l'écran de gestion des médias
 */
data class MediaUiState(
    val medias: List<Media> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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
 */
class MediaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    init {
        // Charger un média mocké par défaut pour la démo
        loadMockedMedia()
    }

    /**
     * Ajoute une photo depuis l'appareil photo
     */
    fun addPhotoFromCamera(uri: String, description: String? = null) {
        val media = Media(
            id = UUID.randomUUID().toString(),
            uri = uri,
            type = MediaType.PHOTO,
            mimeType = "image/jpeg",
            dateAjout = Instant.now(),
            description = description
        )
        addMedia(media)
    }

    /**
     * Ajoute une photo depuis la galerie
     */
    fun addPhotoFromGallery(uri: String, description: String? = null) {
        val media = Media(
            id = UUID.randomUUID().toString(),
            uri = uri,
            type = MediaType.PHOTO,
            mimeType = "image/jpeg",
            dateAjout = Instant.now(),
            description = description
        )
        addMedia(media)
    }

    /**
     * Ajoute un document scanné
     */
    fun addScannedDocument(uri: String, description: String? = null) {
        val media = Media(
            id = UUID.randomUUID().toString(),
            uri = uri,
            type = MediaType.DOCUMENT,
            mimeType = "image/jpeg",
            dateAjout = Instant.now(),
            description = description
        )
        addMedia(media)
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
     * Charge des médias mockés pour la démo
     */
    private fun loadMockedMedia() {
        val mockedMedia = Media(
            id = "mock-media-1",
            uri = "content://mock/potence_serum.jpg",
            type = MediaType.PHOTO,
            mimeType = "image/jpeg",
            taille = 1_200_000, // 1.2 Mo
            dateAjout = Instant.now(),
            description = "Potence à sérum"
        )
        _uiState.update { it.copy(medias = listOf(mockedMedia)) }
    }
}

