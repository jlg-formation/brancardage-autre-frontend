package com.example.huybrancardage.ui.viewmodel

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour le ScanViewModel
 * Vérifie la gestion des états et la validation des codes
 */
class ScanViewModelTest {

    private lateinit var viewModel: ScanViewModel

    @Before
    fun setup() {
        viewModel = ScanViewModel()
    }

    // ==================== Tests de gestion d'état ====================

    @Test
    fun `etat initial est scanning true`() {
        // Then
        assertTrue(viewModel.uiState.value.isScanning)
        assertNull(viewModel.uiState.value.scannedCode)
        assertNull(viewModel.uiState.value.patient)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isProcessing)
    }

    @Test
    fun `setCameraPermission met a jour l'etat`() {
        // When
        viewModel.setCameraPermission(true)

        // Then
        assertTrue(viewModel.uiState.value.hasCameraPermission)

        // When
        viewModel.setCameraPermission(false)

        // Then
        assertFalse(viewModel.uiState.value.hasCameraPermission)
    }

    @Test
    fun `toggleFlash bascule l'etat du flash`() {
        // Initially false
        assertFalse(viewModel.uiState.value.isFlashEnabled)

        // When
        viewModel.toggleFlash()

        // Then
        assertTrue(viewModel.uiState.value.isFlashEnabled)

        // When
        viewModel.toggleFlash()

        // Then
        assertFalse(viewModel.uiState.value.isFlashEnabled)
    }

    @Test
    fun `stopScanning arrete le scan`() {
        // Given
        assertTrue(viewModel.uiState.value.isScanning)

        // When
        viewModel.stopScanning()

        // Then
        assertFalse(viewModel.uiState.value.isScanning)
    }

    @Test
    fun `startScanning remet isScanning a true`() {
        // Given
        viewModel.stopScanning()
        assertFalse(viewModel.uiState.value.isScanning)

        // When
        viewModel.startScanning()

        // Then
        assertTrue(viewModel.uiState.value.isScanning)
    }

    @Test
    fun `reset reinitialise completement l'etat`() {
        // Given - modifier quelques états
        viewModel.toggleFlash()
        viewModel.setCameraPermission(true)
        viewModel.stopScanning()

        // When
        viewModel.reset()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ScanUiState(), state)
    }

    @Test
    fun `getScannedPatient retourne null initialement`() {
        // Then
        assertNull(viewModel.getScannedPatient())
    }

    @Test
    fun `clearError remet isScanning a true`() {
        // Given
        viewModel.stopScanning()

        // When
        viewModel.clearError()

        // Then
        assertTrue(viewModel.uiState.value.isScanning)
        assertNull(viewModel.uiState.value.error)
    }
}
