package com.example.huybrancardage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.DestinationRepository
import com.example.huybrancardage.domain.model.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État de l'écran de sélection de destination
 */
data class DestinationUiState(
    val destinations: List<Destination> = emptyList(),
    val filteredDestinations: List<Destination> = emptyList(),
    val searchQuery: String = "",
    val selectedDestination: Destination? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /**
     * Destinations à afficher (filtrées ou toutes)
     */
    val displayedDestinations: List<Destination>
        get() = if (searchQuery.isBlank()) destinations else filteredDestinations

    /**
     * Vérifie si une destination est sélectionnée
     */
    val hasSelection: Boolean
        get() = selectedDestination != null
}

/**
 * ViewModel pour l'écran de sélection de destination
 */
class DestinationViewModel(
    private val destinationRepository: DestinationRepository = DestinationRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DestinationUiState())
    val uiState: StateFlow<DestinationUiState> = _uiState.asStateFlow()

    init {
        loadDestinations()
    }

    /**
     * Charge la liste des destinations disponibles via le Repository
     */
    fun loadDestinations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = destinationRepository.getDestinations()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            destinations = result.data,
                            filteredDestinations = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
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
     * Met à jour la requête de recherche et filtre les destinations
     */
    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.destinations
            } else {
                state.destinations.filter { destination ->
                    destination.nom.contains(query, ignoreCase = true) ||
                    destination.batiment.contains(query, ignoreCase = true) ||
                    (destination.etageLibelle?.contains(query, ignoreCase = true) == true)
                }
            }
            state.copy(
                searchQuery = query,
                filteredDestinations = filtered
            )
        }
    }

    /**
     * Sélectionne une destination
     */
    fun selectDestination(destination: Destination) {
        _uiState.update { it.copy(selectedDestination = destination) }
    }

    /**
     * Désélectionne la destination actuelle
     */
    fun clearSelection() {
        _uiState.update { it.copy(selectedDestination = null) }
    }

    /**
     * Réinitialise l'état
     */
    fun clear() {
        _uiState.value = DestinationUiState()
        loadDestinations()
    }

    /**
     * Récupère la destination sélectionnée
     */
    fun getSelectedDestination(): Destination? = _uiState.value.selectedDestination
}
