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
 * État de l'écran de scan de bracelet
 */
data class ScanUiState(
    val isScanning: Boolean = true,
    val scannedCode: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val hasCameraPermission: Boolean = false,
    val patient: Patient? = null,
    val isFlashEnabled: Boolean = false
)

/**
 * ViewModel pour l'écran de scan de bracelet
 * Gère la détection de codes-barres/QR codes et la récupération du patient
 */
class ScanViewModel(
    private val patientRepository: PatientRepository = PatientRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /**
     * Démarre le scan
     */
    fun startScanning() {
        _uiState.update {
            it.copy(
                isScanning = true,
                error = null,
                scannedCode = null,
                patient = null
            )
        }
    }

    /**
     * Arrête le scan
     */
    fun stopScanning() {
        _uiState.update { it.copy(isScanning = false) }
    }

    /**
     * Active/désactive le flash
     */
    fun toggleFlash() {
        _uiState.update { it.copy(isFlashEnabled = !it.isFlashEnabled) }
    }

    /**
     * Appelé quand un code est détecté par l'analyseur ML Kit
     * Extrait l'IPP et récupère le patient correspondant
     */
    fun onCodeDetected(code: String) {
        // Éviter les traitements multiples si déjà en cours
        if (_uiState.value.isProcessing || _uiState.value.scannedCode != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = false,
                    isProcessing = true,
                    scannedCode = code,
                    error = null
                )
            }

            // Extraire l'IPP du code scanné
            val ipp = extractIppFromCode(code)

            if (ipp != null) {
                // Récupérer le patient depuis l'API
                fetchPatientByIpp(ipp)
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Code invalide. Format IPP attendu: 9 chiffres",
                        scannedCode = null
                    )
                }
            }
        }
    }

    /**
     * Récupère le patient par son IPP
     */
    private suspend fun fetchPatientByIpp(ipp: String) {
        when (val result = patientRepository.getPatientByIpp(ipp)) {
            is NetworkResult.Success -> {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        patient = result.data,
                        error = null
                    )
                }
            }
            is NetworkResult.Error -> {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = result.message,
                        scannedCode = null,
                        patient = null
                    )
                }
            }
            is NetworkResult.Loading -> {
                // État de chargement déjà géré par isProcessing
            }
        }
    }

    /**
     * Extrait l'IPP d'un code scanné
     * Supporte plusieurs formats:
     * - IPP direct (9 chiffres)
     * - URL contenant l'IPP
     * - Préfixe "IPP:" ou "PATIENT:"
     */
    private fun extractIppFromCode(code: String): String? {
        val cleanCode = code.trim()

        // Format direct: 9 chiffres
        if (cleanCode.matches(Regex("^\\d{9}$"))) {
            return cleanCode
        }

        // Format avec préfixe "IPP:123456789" ou "PATIENT:123456789"
        val prefixRegex = Regex("^(?:IPP|PATIENT):?(\\d{9})$", RegexOption.IGNORE_CASE)
        prefixRegex.find(cleanCode)?.let {
            return it.groupValues[1]
        }

        // Format URL avec paramètre ipp
        val urlRegex = Regex("[?&]ipp=(\\d{9})", RegexOption.IGNORE_CASE)
        urlRegex.find(cleanCode)?.let {
            return it.groupValues[1]
        }

        // Extraction de 9 chiffres consécutifs dans le code
        val digitsRegex = Regex("(\\d{9})")
        digitsRegex.find(cleanCode)?.let {
            return it.groupValues[1]
        }

        return null
    }

    /**
     * Met à jour l'état de la permission caméra
     */
    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    /**
     * Réinitialise l'état après une erreur
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
        // Reprendre le scan après avoir effacé l'erreur
        startScanning()
    }

    /**
     * Réinitialise complètement l'état
     */
    fun reset() {
        _uiState.value = ScanUiState()
    }

    /**
     * Simule un scan réussi (pour les tests)
     */
    fun simulateScan(ipp: String = "123456789") {
        onCodeDetected(ipp)
    }

    /**
     * Retourne le patient scanné
     */
    fun getScannedPatient(): Patient? {
        return _uiState.value.patient
    }
}

