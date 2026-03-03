package com.example.huybrancardage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.domain.model.Localisation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État de l'écran de localisation
 */
data class LocationUiState(
    val localisation: Localisation? = null,
    val precisions: String = "",
    val isLoading: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val error: String? = null
) {
    /**
     * Vérifie si une localisation valide est disponible
     */
    val hasValidLocation: Boolean
        get() = localisation?.isValid == true
}

/**
 * ViewModel pour l'écran de localisation GPS
 */
class LocationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        // Charger automatiquement la localisation GPS mockée
        loadCurrentLocation()
    }

    /**
     * Charge la position GPS actuelle
     */
    fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Simulation de la récupération GPS (sera remplacé dans id008)
                delay(1000)
                val mockLocation = getMockedLocation()
                _uiState.update {
                    it.copy(
                        localisation = mockLocation,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Impossible d'obtenir la position GPS: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Met à jour les précisions saisies par l'utilisateur
     */
    fun setPrecisions(precisions: String) {
        _uiState.update { state ->
            val updatedLocalisation = state.localisation?.copy(precisions = precisions)
            state.copy(
                precisions = precisions,
                localisation = updatedLocalisation
            )
        }
    }

    /**
     * Définit manuellement une localisation
     */
    fun setLocalisation(localisation: Localisation) {
        _uiState.update {
            it.copy(
                localisation = localisation,
                error = null
            )
        }
    }

    /**
     * Met à jour le bâtiment manuellement
     */
    fun setBatiment(batiment: String) {
        _uiState.update { state ->
            val updatedLocalisation = state.localisation?.copy(batiment = batiment)
                ?: Localisation(batiment = batiment)
            state.copy(localisation = updatedLocalisation)
        }
    }

    /**
     * Met à jour l'étage manuellement
     */
    fun setEtage(etage: Int) {
        _uiState.update { state ->
            val updatedLocalisation = state.localisation?.copy(etage = etage)
                ?: Localisation(etage = etage)
            state.copy(localisation = updatedLocalisation)
        }
    }

    /**
     * Met à jour la chambre manuellement
     */
    fun setChambre(chambre: String) {
        _uiState.update { state ->
            val updatedLocalisation = state.localisation?.copy(chambre = chambre)
                ?: Localisation(chambre = chambre)
            state.copy(localisation = updatedLocalisation)
        }
    }

    /**
     * Rafraîchit la position GPS
     */
    fun refreshLocation() {
        loadCurrentLocation()
    }

    /**
     * Réinitialise l'état
     */
    fun clear() {
        _uiState.value = LocationUiState()
    }

    /**
     * Récupère la localisation finale avec les précisions
     */
    fun getFinalLocalisation(): Localisation? {
        val state = _uiState.value
        return state.localisation?.copy(precisions = state.precisions)
    }

    /**
     * Localisation mockée basée sur le patient par défaut
     * TODO: Remplacer par la vraie position GPS dans id008
     */
    private fun getMockedLocation(): Localisation {
        return Localisation(
            latitude = 48.8566,
            longitude = 2.3522,
            description = "Bâtiment A - Cardiologie",
            batiment = "A",
            etage = 2,
            chambre = "204"
        )
    }
}

