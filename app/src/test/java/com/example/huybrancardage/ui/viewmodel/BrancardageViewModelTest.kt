package com.example.huybrancardage.ui.viewmodel

import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.domain.model.Sexe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.time.Instant
import java.time.LocalDate

/**
 * Tests unitaires pour BrancardageViewModel
 *
 * Vérifie la gestion de la session de brancardage,
 * la validation des données et les états de soumission
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrancardageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BrancardageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BrancardageViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Tests État Initial ====================

    @Test
    fun `initial session state should be empty`() {
        val state = viewModel.sessionState.value

        assertNull(state.patient)
        assertTrue(state.medias.isEmpty())
        assertNull(state.localisation)
        assertNull(state.destination)
        assertEquals("", state.commentaire)
        assertFalse(state.isReadyForValidation)
    }

    @Test
    fun `initial submission state should be Idle`() {
        assertTrue(viewModel.submissionState.value is SubmissionState.Idle)
    }

    // ==================== Tests setPatient ====================

    @Test
    fun `setPatient should update patient in session`() {
        // Given
        val patient = createTestPatient()

        // When
        viewModel.setPatient(patient)

        // Then
        assertEquals(patient, viewModel.sessionState.value.patient)
    }

    @Test
    fun `setPatient should replace existing patient`() {
        // Given
        val patient1 = createTestPatient(id = "P001", nom = "DUPONT")
        val patient2 = createTestPatient(id = "P002", nom = "MARTIN")

        // When
        viewModel.setPatient(patient1)
        viewModel.setPatient(patient2)

        // Then
        assertEquals(patient2, viewModel.sessionState.value.patient)
        assertEquals("P002", viewModel.sessionState.value.patient?.id)
    }

    // ==================== Tests Médias ====================

    @Test
    fun `addMedia should add media to session`() {
        // Given
        val media = createTestMedia("1")

        // When
        viewModel.addMedia(media)

        // Then
        assertEquals(1, viewModel.sessionState.value.medias.size)
        assertEquals(media, viewModel.sessionState.value.medias[0])
    }

    @Test
    fun `addMedia should append to existing medias`() {
        // Given
        val media1 = createTestMedia("1")
        val media2 = createTestMedia("2")

        // When
        viewModel.addMedia(media1)
        viewModel.addMedia(media2)

        // Then
        assertEquals(2, viewModel.sessionState.value.medias.size)
    }

    @Test
    fun `removeMedia should remove media by id`() {
        // Given
        viewModel.addMedia(createTestMedia("1"))
        viewModel.addMedia(createTestMedia("2"))

        // When
        viewModel.removeMedia("1")

        // Then
        val medias = viewModel.sessionState.value.medias
        assertEquals(1, medias.size)
        assertEquals("2", medias[0].id)
    }

    @Test
    fun `setMedias should replace all medias`() {
        // Given
        viewModel.addMedia(createTestMedia("old"))
        val newMedias = listOf(createTestMedia("A"), createTestMedia("B"))

        // When
        viewModel.setMedias(newMedias)

        // Then
        val medias = viewModel.sessionState.value.medias
        assertEquals(2, medias.size)
        assertEquals("A", medias[0].id)
        assertEquals("B", medias[1].id)
    }

    // ==================== Tests setLocalisation ====================

    @Test
    fun `setLocalisation should update localisation in session`() {
        // Given
        val localisation = createTestLocalisation()

        // When
        viewModel.setLocalisation(localisation)

        // Then
        assertEquals(localisation, viewModel.sessionState.value.localisation)
    }

    @Test
    fun `setLocalisation should replace existing localisation`() {
        // Given
        val loc1 = Localisation(batiment = "A", etage = 1)
        val loc2 = Localisation(batiment = "B", etage = 2)

        // When
        viewModel.setLocalisation(loc1)
        viewModel.setLocalisation(loc2)

        // Then
        assertEquals("B", viewModel.sessionState.value.localisation?.batiment)
    }

    // ==================== Tests setDestination ====================

    @Test
    fun `setDestination should update destination in session`() {
        // Given
        val destination = createTestDestination()

        // When
        viewModel.setDestination(destination)

        // Then
        assertEquals(destination, viewModel.sessionState.value.destination)
    }

    // ==================== Tests setCommentaire ====================

    @Test
    fun `setCommentaire should update commentaire in session`() {
        // When
        viewModel.setCommentaire("Patient nécessite un brancard spécial")

        // Then
        assertEquals("Patient nécessite un brancard spécial", viewModel.sessionState.value.commentaire)
    }

    @Test
    fun `setCommentaire with empty string should be allowed`() {
        // Given
        viewModel.setCommentaire("Test")

        // When
        viewModel.setCommentaire("")

        // Then
        assertEquals("", viewModel.sessionState.value.commentaire)
    }

    // ==================== Tests isReadyForValidation ====================

    @Test
    fun `isReadyForValidation should be false when patient is null`() {
        // Given
        viewModel.setLocalisation(createTestLocalisation())
        viewModel.setDestination(createTestDestination())

        // Then
        assertFalse(viewModel.isReadyForValidation())
    }

    @Test
    fun `isReadyForValidation should be false when localisation is null`() {
        // Given
        viewModel.setPatient(createTestPatient())
        viewModel.setDestination(createTestDestination())

        // Then
        assertFalse(viewModel.isReadyForValidation())
    }

    @Test
    fun `isReadyForValidation should be false when destination is null`() {
        // Given
        viewModel.setPatient(createTestPatient())
        viewModel.setLocalisation(createTestLocalisation())

        // Then
        assertFalse(viewModel.isReadyForValidation())
    }

    @Test
    fun `isReadyForValidation should be true when all required fields are set`() {
        // Given
        viewModel.setPatient(createTestPatient())
        viewModel.setLocalisation(createTestLocalisation())
        viewModel.setDestination(createTestDestination())

        // Then
        assertTrue(viewModel.isReadyForValidation())
    }

    @Test
    fun `isReadyForValidation should be true even without medias or commentaire`() {
        // Given - only required fields
        viewModel.setPatient(createTestPatient())
        viewModel.setLocalisation(createTestLocalisation())
        viewModel.setDestination(createTestDestination())

        // Then - medias and commentaire are optional
        assertTrue(viewModel.isReadyForValidation())
        assertTrue(viewModel.sessionState.value.medias.isEmpty())
        assertEquals("", viewModel.sessionState.value.commentaire)
    }

    // ==================== Tests resetSession ====================

    @Test
    fun `resetSession should clear all data`() {
        // Given - populate session
        viewModel.setPatient(createTestPatient())
        viewModel.setLocalisation(createTestLocalisation())
        viewModel.setDestination(createTestDestination())
        viewModel.addMedia(createTestMedia("1"))
        viewModel.setCommentaire("Test")

        // When
        viewModel.resetSession()

        // Then
        val state = viewModel.sessionState.value
        assertNull(state.patient)
        assertNull(state.localisation)
        assertNull(state.destination)
        assertTrue(state.medias.isEmpty())
        assertEquals("", state.commentaire)
    }

    @Test
    fun `resetSession should reset submission state to Idle`() = runTest {
        // Given - set some submission state
        viewModel.resetSubmissionState()

        // When
        viewModel.resetSession()

        // Then
        assertTrue(viewModel.submissionState.value is SubmissionState.Idle)
    }

    // ==================== Tests resetSubmissionState ====================

    @Test
    fun `resetSubmissionState should set state to Idle`() {
        // When
        viewModel.resetSubmissionState()

        // Then
        assertTrue(viewModel.submissionState.value is SubmissionState.Idle)
    }

    // ==================== Tests BrancardageSessionState Properties ====================

    @Test
    fun `session state should track all changes`() {
        // When
        val patient = createTestPatient()
        val localisation = createTestLocalisation()
        val destination = createTestDestination()
        val media = createTestMedia("1")

        viewModel.setPatient(patient)
        viewModel.setLocalisation(localisation)
        viewModel.setDestination(destination)
        viewModel.addMedia(media)
        viewModel.setCommentaire("Note importante")

        // Then
        val state = viewModel.sessionState.value
        assertEquals(patient, state.patient)
        assertEquals(localisation, state.localisation)
        assertEquals(destination, state.destination)
        assertEquals(1, state.medias.size)
        assertEquals("Note importante", state.commentaire)
        assertTrue(state.isReadyForValidation)
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

    private fun createTestLocalisation(): Localisation = Localisation(
        latitude = 48.8566,
        longitude = 2.3522,
        batiment = "A",
        etage = 2,
        chambre = "201",
        description = "Chambre 201"
    )

    private fun createTestDestination(): Destination = Destination(
        id = "D001",
        nom = "Radiologie",
        batiment = "B",
        etage = 0,
        etageLibelle = "RDC",
        frequente = true
    )

    private fun createTestMedia(id: String): Media = Media(
        id = id,
        uri = "content://test/$id",
        type = MediaType.PHOTO,
        mimeType = "image/jpeg",
        taille = 1024,
        dateAjout = Instant.now(),
        description = "Test media"
    )
}

