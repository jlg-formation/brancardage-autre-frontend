package com.example.huybrancardage.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.local.OfflineQueueManager
import com.example.huybrancardage.data.local.toSerializable
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.BrancardageRepository
import com.example.huybrancardage.domain.model.BrancardageRequest
import com.example.huybrancardage.domain.model.BrancardageResponse
import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.receiver.NetworkReceiver
import com.example.huybrancardage.ui.state.BrancardageSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État de soumission de la demande
 */
sealed class SubmissionState {
    data object Idle : SubmissionState()
    data object Loading : SubmissionState()
    data class Success(val response: BrancardageResponse) : SubmissionState()
    /** Demande mise en file d'attente (mode hors ligne) */
    data class Queued(val pendingId: String, val patientName: String) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

private const val TAG = "BrancardageViewModel"

/**
 * ViewModel partagé pour la session de brancardage
 * Gère l'état global de la demande en cours de création
 */
class BrancardageViewModel(
    private val brancardageRepository: BrancardageRepository = BrancardageRepository.getInstance()
) : ViewModel() {

    private val _sessionState = MutableStateFlow(BrancardageSessionState())
    val sessionState: StateFlow<BrancardageSessionState> = _sessionState.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

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
        _submissionState.value = SubmissionState.Idle
    }

    /**
     * Vérifie si la session est prête pour la validation
     */
    fun isReadyForValidation(): Boolean = _sessionState.value.isReadyForValidation

    /**
     * Valide et soumet la demande de brancardage
     *
     * ## Gestion du mode hors ligne
     * Si le réseau n'est pas disponible, la demande est mise en file d'attente
     * et sera envoyée automatiquement quand la connexion reviendra.
     */
    fun submitBrancardage(context: Context) {
        Log.d(TAG, "=== submitBrancardage appelé ===")
        val currentState = _sessionState.value

        Log.d(TAG, "Patient: ${currentState.patient?.nomComplet}")
        Log.d(TAG, "Localisation: ${currentState.localisation?.descriptionFormattee}")
        Log.d(TAG, "Destination: ${currentState.destination?.nom}")
        Log.d(TAG, "Médias: ${currentState.medias.size}")
        Log.d(TAG, "isReadyForValidation: ${currentState.isReadyForValidation}")

        // Vérification préalable
        if (!currentState.isReadyForValidation) {
            Log.e(TAG, "Données incomplètes - soumission annulée")
            _submissionState.value = SubmissionState.Error(
                "Données incomplètes. Veuillez vérifier le patient, la localisation et la destination."
            )
            return
        }

        // Vérifier la connectivité réseau
        val isNetworkAvailable = NetworkReceiver.isNetworkAvailable.value
        Log.d(TAG, "Réseau disponible: $isNetworkAvailable")

        if (!isNetworkAvailable) {
            // Mode hors ligne : mettre en file d'attente
            Log.i(TAG, "📵 Mode hors ligne - Mise en file d'attente")
            queueBrancardageRequest(context, currentState)
            return
        }

        // Mode en ligne : envoi direct
        viewModelScope.launch {
            Log.d(TAG, "Début de la coroutine de soumission")
            _submissionState.value = SubmissionState.Loading

            try {
                // 1. Upload des médias si présents
                val uploadedMediaIds = mutableListOf<String>()
                Log.d(TAG, "Upload de ${currentState.medias.size} médias...")
                for (media in currentState.medias) {
                    Log.d(TAG, "Upload média: ${media.id}")
                    val uploadResult = brancardageRepository.uploadMedia(context, media)
                    when (uploadResult) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "Upload réussi: ${uploadResult.data}")
                            uploadedMediaIds.add(uploadResult.data)
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Upload échoué: ${uploadResult.message}")
                            // Continuer malgré l'erreur d'upload (médias optionnels)
                        }
                        is NetworkResult.Loading -> { /* Ignore */ }
                    }
                }

                // 2. Créer la requête de brancardage
                Log.d(TAG, "Création de la requête de brancardage...")
                val request = BrancardageRequest(
                    patientId = currentState.patient!!.id,
                    depart = currentState.localisation!!,
                    destinationId = currentState.destination!!.id,
                    mediaIds = uploadedMediaIds,
                    commentaire = currentState.commentaire.takeIf { it.isNotBlank() }
                )

                // 3. Soumettre la demande
                Log.d(TAG, "Appel createBrancardage...")
                when (val result = brancardageRepository.createBrancardage(request)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Succès! ID: ${result.data.id}")
                        _submissionState.value = SubmissionState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Erreur: ${result.message}")
                        // En cas d'erreur réseau, proposer la mise en file d'attente
                        if (result.message.contains("réseau", ignoreCase = true) ||
                            result.message.contains("network", ignoreCase = true) ||
                            result.message.contains("connection", ignoreCase = true)) {
                            Log.i(TAG, "📵 Erreur réseau détectée - Mise en file d'attente")
                            queueBrancardageRequest(context, currentState)
                        } else {
                            _submissionState.value = SubmissionState.Error(result.message)
                        }
                    }
                    is NetworkResult.Loading -> { /* Ignore */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                _submissionState.value = SubmissionState.Error(
                    e.message ?: "Une erreur inattendue s'est produite"
                )
            }
        }
    }

    /**
     * Met la demande en file d'attente pour envoi ultérieur (mode hors ligne).
     *
     * ## Objectif pédagogique
     * Cette méthode illustre comment gérer gracieusement l'absence de réseau
     * en sauvegardant les données localement pour un envoi différé.
     *
     * @param context Contexte Android
     * @param state État actuel de la session de brancardage
     */
    private fun queueBrancardageRequest(context: Context, state: BrancardageSessionState) {
        val request = BrancardageRequest(
            patientId = state.patient!!.id,
            depart = state.localisation!!,
            destinationId = state.destination!!.id,
            mediaIds = emptyList(), // Les médias seront uploadés lors de la synchronisation
            commentaire = state.commentaire.takeIf { it.isNotBlank() }
        )

        val offlineQueue = OfflineQueueManager.getInstance(context)
        val pendingId = offlineQueue.addToQueue(
            request.toSerializable(
                patientName = state.patient.nomComplet,
                destinationName = state.destination.nom
            )
        )

        Log.i(TAG, "✅ Demande mise en file d'attente: $pendingId")
        _submissionState.value = SubmissionState.Queued(
            pendingId = pendingId,
            patientName = state.patient.nomComplet
        )
    }

    /**
     * Réinitialise l'état de soumission
     */
    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    /**
     * Retourne les erreurs de validation sous forme de liste
     */
    fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        val state = _sessionState.value

        if (state.patient == null) {
            errors.add("Patient non sélectionné")
        }
        if (state.localisation?.isValid != true) {
            errors.add("Localisation de départ invalide")
        }
        if (state.destination == null) {
            errors.add("Destination non sélectionnée")
        }

        return errors
    }
}
