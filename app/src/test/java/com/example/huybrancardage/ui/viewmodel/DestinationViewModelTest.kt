package com.example.huybrancardage.ui.viewmodel

import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.DestinationRepository
import com.example.huybrancardage.domain.model.Destination
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

/**
 * Tests unitaires pour DestinationViewModel
 *
 * Vérifie le chargement des destinations, la recherche/filtrage
 * et la sélection de destination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DestinationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: DestinationRepository
    private lateinit var viewModel: DestinationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)

        // Setup default behavior - return test destinations
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DestinationViewModel {
        return DestinationViewModel(mockRepository)
    }

    // ==================== Tests État Initial ====================

    @Test
    fun `initial state should have empty destinations before load`() = runTest {
        // Create ViewModel - init triggers load
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }

    // ==================== Tests Chargement des Destinations ====================

    @Test
    fun `loadDestinations success should populate destinations list`() = runTest {
        // Given
        val destinations = createTestDestinations()
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(destinations)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.destinations.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadDestinations error should set error message`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Error("NETWORK_ERROR", "Erreur réseau")

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Erreur réseau", state.error)
        assertTrue(state.destinations.isEmpty())
    }

    // ==================== Tests Recherche/Filtrage ====================

    @Test
    fun `setSearchQuery should filter destinations by name`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setSearchQuery("Radio")

        // Then
        val state = viewModel.uiState.value
        assertEquals("Radio", state.searchQuery)
        assertEquals(1, state.displayedDestinations.size)
        assertEquals("Radiologie", state.displayedDestinations[0].nom)
    }

    @Test
    fun `setSearchQuery should filter destinations by batiment`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setSearchQuery("B") // Bâtiment B

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.displayedDestinations.any { it.batiment == "B" })
    }

    @Test
    fun `setSearchQuery with empty string should show all destinations`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // First filter
        viewModel.setSearchQuery("Radio")
        assertEquals(1, viewModel.uiState.value.displayedDestinations.size)

        // When - clear search
        viewModel.setSearchQuery("")

        // Then - should show all
        assertEquals(3, viewModel.uiState.value.displayedDestinations.size)
    }

    @Test
    fun `setSearchQuery should be case insensitive`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When - lowercase search
        viewModel.setSearchQuery("radiologie")

        // Then
        assertEquals(1, viewModel.uiState.value.displayedDestinations.size)
        assertEquals("Radiologie", viewModel.uiState.value.displayedDestinations[0].nom)
    }

    @Test
    fun `setSearchQuery with no matches should return empty list`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setSearchQuery("NonExistant")

        // Then
        assertTrue(viewModel.uiState.value.displayedDestinations.isEmpty())
    }

    // ==================== Tests Sélection ====================

    @Test
    fun `selectDestination should update selectedDestination`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        val destination = viewModel.uiState.value.destinations[0]

        // When
        viewModel.selectDestination(destination)

        // Then
        val state = viewModel.uiState.value
        assertEquals(destination, state.selectedDestination)
        assertTrue(state.hasSelection)
    }

    @Test
    fun `clearSelection should remove selectedDestination`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        val destination = viewModel.uiState.value.destinations[0]
        viewModel.selectDestination(destination)

        // When
        viewModel.clearSelection()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.selectedDestination)
        assertFalse(state.hasSelection)
    }

    @Test
    fun `getSelectedDestination should return current selection`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        val destination = viewModel.uiState.value.destinations[0]
        viewModel.selectDestination(destination)

        // When
        val result = viewModel.getSelectedDestination()

        // Then
        assertEquals(destination, result)
    }

    @Test
    fun `getSelectedDestination should return null when no selection`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        val result = viewModel.getSelectedDestination()

        // Then
        assertNull(result)
    }

    // ==================== Tests Clear ====================

    @Test
    fun `clear should reset state and reload destinations`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // Modify state
        viewModel.setSearchQuery("Test")
        viewModel.selectDestination(viewModel.uiState.value.destinations[0])

        // When
        viewModel.clear()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertNull(state.selectedDestination)
    }

    // ==================== Tests DestinationUiState Properties ====================

    @Test
    fun `displayedDestinations should return filtered when search query present`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setSearchQuery("Urgences")

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.displayedDestinations.size)
    }

    @Test
    fun `displayedDestinations should return all when search query blank`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then - no filter applied
        val state = viewModel.uiState.value
        assertEquals(state.destinations.size, state.displayedDestinations.size)
    }

    @Test
    fun `hasSelection should be false initially`() = runTest {
        // Given
        coEvery { mockRepository.getDestinations(any(), any()) } returns
            NetworkResult.Success(createTestDestinations())
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.hasSelection)
    }

    // ==================== Helpers ====================

    private fun createTestDestinations(): List<Destination> = listOf(
        Destination(
            id = "D001",
            nom = "Radiologie",
            batiment = "A",
            etage = 0,
            etageLibelle = "RDC",
            frequente = true
        ),
        Destination(
            id = "D002",
            nom = "Urgences",
            batiment = "B",
            etage = 0,
            etageLibelle = "RDC",
            frequente = true
        ),
        Destination(
            id = "D003",
            nom = "Chirurgie",
            batiment = "A",
            etage = 2,
            etageLibelle = "Étage 2",
            frequente = false
        )
    )
}


