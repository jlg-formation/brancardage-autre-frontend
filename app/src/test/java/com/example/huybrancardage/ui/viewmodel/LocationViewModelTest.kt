package com.example.huybrancardage.ui.viewmodel

import com.example.huybrancardage.data.location.LocationService
import com.example.huybrancardage.data.location.LocationTimeoutException
import com.example.huybrancardage.data.location.LocationToHospitalMapper
import com.example.huybrancardage.domain.model.Localisation
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
 * Tests unitaires pour LocationViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LocationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LocationViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have no location and not loading`() {
        val state = viewModel.uiState.value

        assertNull(state.localisation)
        assertFalse(state.isLoading)
        assertFalse(state.hasPermission)
        assertFalse(state.permissionRequested)
        assertNull(state.error)
        assertEquals("", state.precisions)
    }

    @Test
    fun `onPermissionResult with granted true should set hasPermission`() = runTest {
        viewModel.onPermissionResult(true)

        val state = viewModel.uiState.value
        assertTrue(state.hasPermission)
        assertTrue(state.permissionRequested)
    }

    @Test
    fun `onPermissionResult with granted false should use default location`() = runTest {
        viewModel.onPermissionResult(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasPermission)
        assertTrue(state.permissionRequested)
        assertNotNull(state.localisation)
        assertNotNull(state.error)
    }

    @Test
    fun `setPrecisions should update precisions in state`() {
        viewModel.setPrecisions("Devant l'ascenseur")

        val state = viewModel.uiState.value
        assertEquals("Devant l'ascenseur", state.precisions)
    }

    @Test
    fun `setLocalisation should update localisation in state`() {
        val localisation = Localisation(
            latitude = 48.8566,
            longitude = 2.3522,
            batiment = "B",
            etage = 3
        )

        viewModel.setLocalisation(localisation)

        val state = viewModel.uiState.value
        assertEquals(localisation, state.localisation)
        assertNull(state.error)
    }

    @Test
    fun `setBatiment should update batiment in localisation`() {
        viewModel.setBatiment("C")

        val state = viewModel.uiState.value
        assertEquals("C", state.localisation?.batiment)
    }

    @Test
    fun `setEtage should update etage in localisation`() {
        viewModel.setEtage(5)

        val state = viewModel.uiState.value
        assertEquals(5, state.localisation?.etage)
    }

    @Test
    fun `setChambre should update chambre in localisation`() {
        viewModel.setChambre("312")

        val state = viewModel.uiState.value
        assertEquals("312", state.localisation?.chambre)
    }

    @Test
    fun `clear should reset state to initial values`() {
        // Set up some state
        viewModel.setPrecisions("Test")
        viewModel.setBatiment("A")
        viewModel.setEtage(2)

        // Clear
        viewModel.clear()

        val state = viewModel.uiState.value
        assertNull(state.localisation)
        assertEquals("", state.precisions)
        assertFalse(state.isLoading)
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        // First, trigger an error by denying permission
        viewModel.onPermissionResult(false)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        // Clear error
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getFinalLocalisation should return localisation with precisions`() {
        val localisation = Localisation(
            latitude = 48.8566,
            longitude = 2.3522,
            batiment = "A",
            etage = 2
        )

        viewModel.setLocalisation(localisation)
        viewModel.setPrecisions("Devant l'ascenseur")

        val finalLocalisation = viewModel.getFinalLocalisation()

        assertNotNull(finalLocalisation)
        assertEquals("Devant l'ascenseur", finalLocalisation?.precisions)
        assertEquals("A", finalLocalisation?.batiment)
    }

    @Test
    fun `hasValidLocation should be true when localisation has coordinates`() {
        val localisation = Localisation(
            latitude = 48.8566,
            longitude = 2.3522
        )

        viewModel.setLocalisation(localisation)

        assertTrue(viewModel.uiState.value.hasValidLocation)
    }

    @Test
    fun `hasValidLocation should be true when localisation has batiment`() {
        viewModel.setBatiment("A")

        assertTrue(viewModel.uiState.value.hasValidLocation)
    }

    @Test
    fun `hasValidLocation should be false when no localisation`() {
        assertFalse(viewModel.uiState.value.hasValidLocation)
    }

    @Test
    fun `loadCurrentLocation without service should use mocked location`() = runTest {
        viewModel.loadCurrentLocation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.localisation)
        assertFalse(state.isLoading)
    }
}

/**
 * Tests unitaires pour LocationToHospitalMapper
 */
class LocationToHospitalMapperTest {

    @Test
    fun `mapToLocalisation should return localisation with coordinates`() {
        val localisation = LocationToHospitalMapper.mapToLocalisation(48.8566, 2.3522)

        assertNotNull(localisation)
        assertEquals(48.8566, localisation.latitude)
        assertEquals(2.3522, localisation.longitude)
        assertNotNull(localisation.description)
    }

    @Test
    fun `mapToLocalisation should find nearest zone`() {
        // Test avec coordonnées proches du Bâtiment A
        val localisation = LocationToHospitalMapper.mapToLocalisation(48.8566, 2.3522)

        assertEquals("A", localisation.batiment)
    }

    @Test
    fun `getDefaultLocalisation should return valid localisation`() {
        val defaultLocalisation = LocationToHospitalMapper.getDefaultLocalisation()

        assertNotNull(defaultLocalisation)
        assertNotNull(defaultLocalisation.latitude)
        assertNotNull(defaultLocalisation.longitude)
        assertNotNull(defaultLocalisation.batiment)
        assertTrue(defaultLocalisation.isValid)
    }
}

/**
 * Tests pour LocationTimeoutException et constantes
 */
class LocationServiceConstantsTest {

    @Test
    fun `LOCATION_TIMEOUT_MS should be 10 seconds`() {
        assertEquals(10_000L, LocationService.LOCATION_TIMEOUT_MS)
    }

    @Test
    fun `LocationTimeoutException should have correct message`() {
        val exception = LocationTimeoutException("Test timeout message")
        assertEquals("Test timeout message", exception.message)
    }

    @Test
    fun `REQUIRED_PERMISSIONS should contain both location permissions`() {
        val permissions = LocationService.REQUIRED_PERMISSIONS
        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}

