package com.example.huybrancardage.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.ui.state.BrancardageSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel partagé pour la session de brancardage
 * Gère l'état global de la demande en cours de création
 */
class BrancardageViewModel : ViewModel() {

    private val _sessionState = MutableStateFlow(BrancardageSessionState())
    val sessionState: StateFlow<BrancardageSessionState> = _sessionState.asStateFlow()

    /**
     * Définit le patient sélectionné
     */
    fun setPatient(patient: Patient) {
        _sessionState.update { it.copy(patient = patient) }
    }

    /**
     * Ajoute un média à la session
     */
    fun addMedia(media: Media) {
        _sessionState.update { state ->
            state.copy(medias = state.medias + media)
        }
    }

    /**
     * Supprime un média de la session
     */
    fun removeMedia(mediaId: String) {
        _sessionState.update { state ->
            state.copy(medias = state.medias.filter { it.id != mediaId })
        }
    }

    /**
     * Met à jour les médias de la session
     */
    fun setMedias(medias: List<Media>) {
        _sessionState.update { it.copy(medias = medias) }
    }

    /**
     * Définit la localisation de départ
     */
    fun setLocalisation(localisation: Localisation) {
        _sessionState.update { it.copy(localisation = localisation) }
    }

    /**
     * Définit la destination
     */
    fun setDestination(destination: Destination) {
        _sessionState.update { it.copy(destination = destination) }
    }

    /**
     * Met à jour le commentaire
     */
    fun setCommentaire(commentaire: String) {
        _sessionState.update { it.copy(commentaire = commentaire) }
    }

    /**
     * Réinitialise la session (après validation ou annulation)
     */
    fun resetSession() {
        _sessionState.value = BrancardageSessionState()
    }

    /**
     * Vérifie si la session est prête pour la validation
     */
    fun isReadyForValidation(): Boolean = _sessionState.value.isReadyForValidation
}

