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
 * État de l'écran de dossier patient
 */
data class PatientUiState(
    val patient: Patient? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel pour l'écran de dossier patient
 */
class PatientViewModel(
    private val patientRepository: PatientRepository = PatientRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    /**
     * Charge un patient par son ID via le Repository
     */
    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = patientRepository.getPatientById(patientId)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            patient = result.data,
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
     * Charge un patient par son IPP (après scan de bracelet) via le Repository
     */
    fun loadPatientByIpp(ipp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = patientRepository.getPatientByIpp(ipp)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            patient = result.data,
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
     * Définit directement un patient (depuis la recherche)
     */
    fun setPatient(patient: Patient) {
        _uiState.update { it.copy(patient = patient, error = null) }
    }

    /**
     * Réinitialise l'état
     */
    fun clear() {
        _uiState.value = PatientUiState()
    }
}

