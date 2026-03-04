package com.example.huybrancardage.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.location.LocationService
import com.example.huybrancardage.data.location.LocationTimeoutException
import com.example.huybrancardage.data.location.LocationToHospitalMapper
import com.example.huybrancardage.domain.model.Localisation
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
    val hasPermission: Boolean = false,
    val permissionRequested: Boolean = false,
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
 *
 * Utilise Fused Location Provider pour obtenir la position GPS réelle
 */
class LocationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private var locationService: LocationService? = null

    /**
     * Initialise le LocationService avec le contexte
     * Doit être appelé depuis l'écran avec le contexte Android
     */
    fun initLocationService(service: LocationService) {
        this.locationService = service
        checkPermissionAndLoadLocation()
    }

    /**
     * Vérifie les permissions et charge la position si autorisé
     */
    private fun checkPermissionAndLoadLocation() {
        val service = locationService ?: return

        val hasPermission = service.hasLocationPermission()
        _uiState.update { it.copy(hasPermission = hasPermission) }

        if (hasPermission) {
            loadCurrentLocation()
        }
    }

    /**
     * Met à jour l'état après que la permission a été accordée ou refusée
     */
    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasPermission = granted,
                permissionRequested = true
            )
        }

        if (granted) {
            loadCurrentLocation()
        } else {
            // Charger une localisation par défaut si permission refusée
            _uiState.update {
                it.copy(
                    localisation = LocationToHospitalMapper.getDefaultLocalisation(),
                    error = "Permission refusée. Position par défaut utilisée."
                )
            }
        }
    }

    /**
     * Charge la position GPS actuelle
     */
    fun loadCurrentLocation() {
        val service = locationService

        if (service == null) {
            // Pas de service, utiliser mock
            loadMockedLocation()
            return
        }

        if (!service.hasLocationPermission()) {
            _uiState.update {
                it.copy(
                    hasPermission = false,
                    error = "Permission de localisation requise"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Essayer d'abord la dernière position connue (rapide)
                val lastLocation = service.getLastKnownLocation()

                if (lastLocation != null) {
                    updateLocationFromGps(lastLocation)
                } else {
                    // Si pas de position en cache, demander une nouvelle position
                    val currentLocation = service.getCurrentLocation()
                    updateLocationFromGps(currentLocation)
                }
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasPermission = false,
                        error = "Permission de localisation non accordée"
                    )
                }
            } catch (e: LocationTimeoutException) {
                // Timeout GPS - utiliser une position par défaut
                _uiState.update {
                    it.copy(
                        localisation = LocationToHospitalMapper.getDefaultLocalisation(),
                        isLoading = false,
                        error = "Position GPS non trouvée (timeout). Position par défaut utilisée."
                    )
                }
            } catch (e: Exception) {
                // En cas d'erreur, utiliser une position par défaut
                _uiState.update {
                    it.copy(
                        localisation = LocationToHospitalMapper.getDefaultLocalisation(),
                        isLoading = false,
                        error = "GPS indisponible: ${e.message}. Position par défaut utilisée."
                    )
                }
            }
        }
    }

    /**
     * Met à jour l'état avec une position GPS
     */
    private fun updateLocationFromGps(location: Location) {
        val localisation = LocationToHospitalMapper.mapToLocalisation(
            latitude = location.latitude,
            longitude = location.longitude
        )

        _uiState.update {
            it.copy(
                localisation = localisation,
                isLoading = false,
                isGpsEnabled = true,
                error = null
            )
        }
    }

    /**
     * Charge une position mockée (pour les previews ou tests)
     */
    private fun loadMockedLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulation d'un délai
            kotlinx.coroutines.delay(500)

            _uiState.update {
                it.copy(
                    localisation = LocationToHospitalMapper.getDefaultLocalisation(),
                    isLoading = false
                )
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
     * Efface l'erreur
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
}
