package com.example.huybrancardage.ui.viewmodel

import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.FakePatientRepository
import com.example.huybrancardage.data.repository.PatientRepository
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.domain.model.Sexe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Tests unitaires pour SearchViewModel
 *
 * Vérifie la gestion des états de recherche et les transitions
 * selon les cas de succès et d'erreur
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: PatientRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = SearchViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Tests État Initial ====================

    @Test
    fun `initial state should have empty fields and no results`() {
        val state = viewModel.uiState.value

        assertEquals("", state.nom)
        assertEquals("", state.prenom)
        assertEquals("", state.ipp)
        assertEquals("", state.numeroSecuriteSociale)
        assertFalse(state.isLoading)
        assertTrue(state.results.isEmpty())
        assertNull(state.error)
        assertFalse(state.hasSearched)
        assertFalse(state.canSearch)
    }

    // ==================== Tests Modification des Champs ====================

    @Test
    fun `setNom should update nom in state`() {
        viewModel.setNom("Dupont")

        assertEquals("Dupont", viewModel.uiState.value.nom)
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `setPrenom should update prenom in state`() {
        viewModel.setPrenom("Jean")

        assertEquals("Jean", viewModel.uiState.value.prenom)
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `setIpp should update ipp in state`() {
        viewModel.setIpp("123456789")

        assertEquals("123456789", viewModel.uiState.value.ipp)
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `setNumeroSecuriteSociale should update numeroSecuriteSociale in state`() {
        viewModel.setNumeroSecuriteSociale("180055512345678")

        assertEquals("180055512345678", viewModel.uiState.value.numeroSecuriteSociale)
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `setting any field should clear error`() {
        // Setup initial error
        viewModel.search() // Will fail without criteria
        assertNotNull(viewModel.uiState.value.error)

        // When setting a field
        viewModel.setNom("Test")

        // Then error should be cleared
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== Tests canSearch ====================

    @Test
    fun `canSearch should be false when all fields are empty`() {
        assertFalse(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `canSearch should be true when nom is not blank`() {
        viewModel.setNom("Test")
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `canSearch should be true when prenom is not blank`() {
        viewModel.setPrenom("Test")
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `canSearch should be true when ipp is not blank`() {
        viewModel.setIpp("123")
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `canSearch should be true when numeroSecuriteSociale is not blank`() {
        viewModel.setNumeroSecuriteSociale("123")
        assertTrue(viewModel.uiState.value.canSearch)
    }

    @Test
    fun `canSearch should be false when fields contain only whitespace`() {
        viewModel.setNom("   ")
        viewModel.setPrenom("   ")

        assertFalse(viewModel.uiState.value.canSearch)
    }

    // ==================== Tests Recherche ====================

    @Test
    fun `search without criteria should set error`() {
        viewModel.search()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Veuillez saisir au moins un critère de recherche", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `search with criteria should call repository`() = runTest {
        // Given
        val testPatients = listOf(createTestPatient())
        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Success(testPatients)

        viewModel.setNom("Dupont")

        // When
        viewModel.search()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.results.size)
        assertTrue(state.hasSearched)
        assertNull(state.error)
    }

    @Test
    fun `search should set loading state then complete`() = runTest {
        // Given
        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Success(emptyList())

        viewModel.setNom("Test")

        // When
        viewModel.search()
        advanceUntilIdle()

        // Then - after completion, loading should be false
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.hasSearched)
    }

    @Test
    fun `search success should update results`() = runTest {
        // Given
        val testPatients = listOf(
            createTestPatient(id = "1", nom = "DUPONT"),
            createTestPatient(id = "2", nom = "DURAND")
        )
        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Success(testPatients)

        viewModel.setNom("DU")

        // When
        viewModel.search()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.results.size)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `search error should set error message`() = runTest {
        // Given
        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Error("NETWORK_ERROR", "Erreur réseau")

        viewModel.setNom("Test")

        // When
        viewModel.search()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Erreur réseau", state.error)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
    }

    @Test
    fun `search with empty results should show empty list`() = runTest {
        // Given
        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Success(emptyList())

        viewModel.setNom("NonExistant")

        // When
        viewModel.search()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.results.isEmpty())
        assertTrue(state.hasSearched)
        assertNull(state.error)
    }

    // ==================== Tests clearSearch ====================

    @Test
    fun `clearSearch should reset all fields`() = runTest {
        // Given - set some values
        viewModel.setNom("Test")
        viewModel.setPrenom("Patient")
        viewModel.setIpp("123")

        coEvery {
            mockRepository.searchPatients(any(), any(), any(), any())
        } returns NetworkResult.Success(listOf(createTestPatient()))

        viewModel.search()
        advanceUntilIdle()

        // When
        viewModel.clearSearch()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.nom)
        assertEquals("", state.prenom)
        assertEquals("", state.ipp)
        assertEquals("", state.numeroSecuriteSociale)
        assertTrue(state.results.isEmpty())
        assertFalse(state.hasSearched)
        assertNull(state.error)
    }

    // ==================== Helpers ====================

    private fun createTestPatient(
        id: String = "P001",
        nom: String = "TEST"
    ): Patient = Patient(
        id = id,
        ipp = "123456789",
        nom = nom,
        prenom = "Patient",
        dateNaissance = LocalDate.of(1990, 1, 1),
        sexe = Sexe.MASCULIN,
        numeroSecuriteSociale = null,
        chambre = "101",
        service = "Test",
        batiment = "A",
        etage = 1,
        alertesMedicales = emptyList()
    )
}


