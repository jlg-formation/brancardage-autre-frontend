package com.example.huybrancardage.data.repository

import com.example.huybrancardage.data.remote.NetworkResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests d'intégration pour FakePatientRepository
 *
 * Vérifie le comportement du repository avec différents scénarios
 * de recherche, filtrage et gestion d'erreurs
 */
class PatientRepositoryIntegrationTest {

    private lateinit var repository: FakePatientRepository

    @Before
    fun setUp() {
        repository = FakePatientRepository()
    }

    // ==================== Tests searchPatients ====================

    @Test
    fun `searchPatients without criteria should return all patients`() = runTest {
        // When
        val result = repository.searchPatients()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(3, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `searchPatients by nom should filter correctly`() = runTest {
        // When
        val result = repository.searchPatients(nom = "DUPONT")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("DUPONT", patients[0].nom)
    }

    @Test
    fun `searchPatients by nom should be case insensitive`() = runTest {
        // When
        val result = repository.searchPatients(nom = "dupont")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("DUPONT", patients[0].nom)
    }

    @Test
    fun `searchPatients by prenom should filter correctly`() = runTest {
        // When
        val result = repository.searchPatients(prenom = "Marie")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("Marie", patients[0].prenom)
    }

    @Test
    fun `searchPatients by ipp should return exact match`() = runTest {
        // When
        val result = repository.searchPatients(ipp = "123456789")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("123456789", patients[0].ipp)
    }

    @Test
    fun `searchPatients by numeroSecuriteSociale should filter correctly`() = runTest {
        // When
        val result = repository.searchPatients(numeroSecuriteSociale = "180055512345678")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("180055512345678", patients[0].numeroSecuriteSociale)
    }

    @Test
    fun `searchPatients with multiple criteria should combine filters`() = runTest {
        // When - search by nom AND service (via full criteria)
        val result = repository.searchPatients(nom = "DUPONT", prenom = "Jean")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("Jean", patients[0].prenom)
        assertEquals("DUPONT", patients[0].nom)
    }

    @Test
    fun `searchPatients with no matches should return empty list`() = runTest {
        // When
        val result = repository.searchPatients(nom = "NonExistant")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }

    @Test
    fun `searchPatients should return error when configured`() = runTest {
        // Given
        repository.setReturnError(true, "Connexion impossible")

        // When
        val result = repository.searchPatients(nom = "Test")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Connexion impossible", (result as NetworkResult.Error).message)
    }

    @Test
    fun `searchPatients partial match should work`() = runTest {
        // When - partial name match
        val result = repository.searchPatients(nom = "MAR") // Should match MARTIN

        // Then
        assertTrue(result is NetworkResult.Success)
        val patients = (result as NetworkResult.Success).data
        assertEquals(1, patients.size)
        assertEquals("MARTIN", patients[0].nom)
    }

    // ==================== Tests getPatientById ====================

    @Test
    fun `getPatientById should return patient when exists`() = runTest {
        // When
        val result = repository.getPatientById("P001")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertEquals("P001", patient.id)
        assertEquals("DUPONT", patient.nom)
    }

    @Test
    fun `getPatientById should return error when not found`() = runTest {
        // When
        val result = repository.getPatientById("NonExistant")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("PATIENT_NOT_FOUND", (result as NetworkResult.Error).code)
    }

    @Test
    fun `getPatientById should return error when configured`() = runTest {
        // Given
        repository.setReturnError(true, "Erreur serveur")

        // When
        val result = repository.getPatientById("P001")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Erreur serveur", (result as NetworkResult.Error).message)
    }

    // ==================== Tests getPatientByIpp ====================

    @Test
    fun `getPatientByIpp should return patient when exists`() = runTest {
        // When
        val result = repository.getPatientByIpp("123456789")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertEquals("123456789", patient.ipp)
        assertEquals("DUPONT", patient.nom)
    }

    @Test
    fun `getPatientByIpp should return error when not found`() = runTest {
        // When
        val result = repository.getPatientByIpp("000000000")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("PATIENT_NOT_FOUND", (result as NetworkResult.Error).code)
    }

    @Test
    fun `getPatientByIpp should return error when configured`() = runTest {
        // Given
        repository.setReturnError(true, "IPP invalide")

        // When
        val result = repository.getPatientByIpp("123456789")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("IPP invalide", (result as NetworkResult.Error).message)
    }

    // ==================== Tests manipulation des données ====================

    @Test
    fun `addPatient should add patient to repository`() = runTest {
        // Given
        val newPatient = FakePatientRepository.createTestPatient(
            id = "NEW001",
            ipp = "999888777",
            nom = "NOUVEAU",
            prenom = "Patient"
        )

        // When
        repository.addPatient(newPatient)
        val result = repository.getPatientById("NEW001")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("NOUVEAU", (result as NetworkResult.Success).data.nom)
    }

    @Test
    fun `setPatients should replace all patients`() = runTest {
        // Given
        val customPatients = listOf(
            FakePatientRepository.createTestPatient(id = "CUSTOM1"),
            FakePatientRepository.createTestPatient(id = "CUSTOM2")
        )

        // When
        repository.setPatients(customPatients)
        val result = repository.searchPatients()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(2, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `clearPatients should remove all patients`() = runTest {
        // When
        repository.clearPatients()
        val result = repository.searchPatients()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }

    // ==================== Tests données patient ====================

    @Test
    fun `patient should have alertes medicales`() = runTest {
        // When
        val result = repository.getPatientById("P001") // DUPONT has allergy

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertTrue(patient.alertesMedicales.isNotEmpty())
        assertEquals("Pénicilline", patient.alertesMedicales[0].titre)
    }

    @Test
    fun `patient should have correct service and chambre`() = runTest {
        // When
        val result = repository.getPatientById("P001")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertEquals("Cardiologie", patient.service)
        assertEquals("101", patient.chambre)
        assertEquals("A", patient.batiment)
        assertEquals(2, patient.etage)
    }

    @Test
    fun `patient nomComplet should be formatted correctly`() = runTest {
        // When
        val result = repository.getPatientById("P001")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertEquals("Jean DUPONT", patient.nomComplet)
    }

    @Test
    fun `patient initiales should be correct`() = runTest {
        // When
        val result = repository.getPatientById("P001")

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        assertEquals("JD", patient.initiales)
    }

    @Test
    fun `patient age should be calculated correctly`() = runTest {
        // When
        val result = repository.getPatientById("P001") // Born 1980-05-15

        // Then
        assertTrue(result is NetworkResult.Success)
        val patient = (result as NetworkResult.Success).data
        // Age depends on current date, but should be roughly 45-46 in 2026
        assertTrue(patient.age in 45..46)
    }

    // ==================== Tests NetworkResult ====================

    @Test
    fun `NetworkResult Success should have correct properties`() {
        val result: NetworkResult<String> = NetworkResult.Success("test data")

        assertTrue(result.isSuccess)
        assertEquals("test data", result.getOrNull())
        assertEquals("test data", result.getOrDefault("default"))
    }

    @Test
    fun `NetworkResult Error should have correct properties`() {
        val result: NetworkResult<String> = NetworkResult.Error("CODE", "message", 404)

        assertTrue(result is NetworkResult.Error)
        assertEquals("CODE", (result as NetworkResult.Error).code)
        assertEquals("message", result.message)
        assertEquals(404, result.httpCode)
    }

    @Test
    fun `NetworkResult getOrNull should return null for Error`() {
        val result: NetworkResult<String> = NetworkResult.Error("ERROR", "msg")

        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `NetworkResult getOrDefault should return default for Error`() {
        val result: NetworkResult<String> = NetworkResult.Error("ERROR", "msg")

        assertEquals("default", result.getOrDefault("default"))
    }
}

