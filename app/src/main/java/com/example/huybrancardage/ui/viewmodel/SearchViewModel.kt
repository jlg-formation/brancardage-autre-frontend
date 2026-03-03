package com.example.huybrancardage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.PatientRepository
import com.example.huybrancardage.domain.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État de l'écran de recherche
 */
data class SearchUiState(
    val nom: String = "",
    val prenom: String = "",
    val ipp: String = "",
    val numeroSecuriteSociale: String = "",
    val isLoading: Boolean = false,
    val results: List<Patient> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
) {
    /**
     * Vérifie si au moins un critère de recherche est rempli
     */
    val canSearch: Boolean
        get() = nom.isNotBlank() ||
                prenom.isNotBlank() ||
                ipp.isNotBlank() ||
                numeroSecuriteSociale.isNotBlank()
}

/**
 * ViewModel pour l'écran de recherche manuelle
 */
class SearchViewModel(
    private val patientRepository: PatientRepository = PatientRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * Met à jour le nom de recherche
     */
    fun setNom(nom: String) {
        _uiState.update { it.copy(nom = nom, error = null) }
    }

    /**
     * Met à jour le prénom de recherche
     */
    fun setPrenom(prenom: String) {
        _uiState.update { it.copy(prenom = prenom, error = null) }
    }

    /**
     * Met à jour l'IPP de recherche
     */
    fun setIpp(ipp: String) {
        _uiState.update { it.copy(ipp = ipp, error = null) }
    }

    /**
     * Met à jour le numéro de sécurité sociale de recherche
     */
    fun setNumeroSecuriteSociale(numeroSecuriteSociale: String) {
        _uiState.update { it.copy(numeroSecuriteSociale = numeroSecuriteSociale, error = null) }
    }

    /**
     * Lance la recherche de patients via le Repository
     */
    fun search() {
        if (!_uiState.value.canSearch) {
            _uiState.update { it.copy(error = "Veuillez saisir au moins un critère de recherche") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val result = patientRepository.searchPatients(
                nom = state.nom.takeIf { it.isNotBlank() },
                prenom = state.prenom.takeIf { it.isNotBlank() },
                ipp = state.ipp.takeIf { it.isNotBlank() },
                numeroSecuriteSociale = state.numeroSecuriteSociale.takeIf { it.isNotBlank() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = result.data,
                            hasSearched = true,
                            error = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            hasSearched = true
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    // État déjà géré
                }
            }
        }
    }

    /**
     * Réinitialise la recherche
     */
    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
}

