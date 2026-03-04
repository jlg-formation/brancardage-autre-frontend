package com.example.huybrancardage.data.repository

import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.domain.model.AlerteMedicale
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.domain.model.Sexe
import com.example.huybrancardage.domain.model.TypeAlerte
import java.time.LocalDate

/**
 * Fake implementation du PatientRepository pour les tests
 * Permet de contrôler les réponses sans faire d'appels réseau
 */
class FakePatientRepository : IPatientRepository {

    private var patients: MutableList<Patient> = createMockedPatients().toMutableList()
    private var shouldReturnError = false
    private var errorMessage = "Test error"
    private var searchDelay = 0L

    /**
     * Configure le repository pour retourner une erreur
     */
    fun setReturnError(shouldError: Boolean, message: String = "Test error") {
        shouldReturnError = shouldError
        errorMessage = message
    }

    /**
     * Configure un délai pour simuler un appel réseau
     */
    fun setSearchDelay(delay: Long) {
        searchDelay = delay
    }

    /**
     * Ajoute un patient à la liste pour les tests
     */
    fun addPatient(patient: Patient) {
        patients.add(patient)
    }

    /**
     * Remplace la liste de patients
     */
    fun setPatients(newPatients: List<Patient>) {
        patients = newPatients.toMutableList()
    }

    /**
     * Efface tous les patients
     */
    fun clearPatients() {
        patients.clear()
    }

    override suspend fun searchPatients(
        nom: String?,
        prenom: String?,
        ipp: String?,
        numeroSecuriteSociale: String?,
        page: Int,
        size: Int
    ): NetworkResult<List<Patient>> {
        if (searchDelay > 0) {
            kotlinx.coroutines.delay(searchDelay)
        }

        if (shouldReturnError) {
            return NetworkResult.Error("TEST_ERROR", errorMessage)
        }

        val filtered = patients.filter { patient ->
            (nom.isNullOrBlank() || patient.nom.contains(nom, ignoreCase = true)) &&
            (prenom.isNullOrBlank() || patient.prenom.contains(prenom, ignoreCase = true)) &&
            (ipp.isNullOrBlank() || patient.ipp == ipp) &&
            (numeroSecuriteSociale.isNullOrBlank() || patient.numeroSecuriteSociale == numeroSecuriteSociale)
        }

        return NetworkResult.Success(filtered)
    }

    override suspend fun getPatientById(id: String): NetworkResult<Patient> {
        if (shouldReturnError) {
            return NetworkResult.Error("TEST_ERROR", errorMessage)
        }

        val patient = patients.find { it.id == id }
        return if (patient != null) {
            NetworkResult.Success(patient)
        } else {
            NetworkResult.Error("PATIENT_NOT_FOUND", "Aucun patient trouvé avec l'ID: $id")
        }
    }

    override suspend fun getPatientByIpp(ipp: String): NetworkResult<Patient> {
        if (shouldReturnError) {
            return NetworkResult.Error("TEST_ERROR", errorMessage)
        }

        val patient = patients.find { it.ipp == ipp }
        return if (patient != null) {
            NetworkResult.Success(patient)
        } else {
            NetworkResult.Error("PATIENT_NOT_FOUND", "Aucun patient trouvé avec l'IPP: $ipp")
        }
    }

    companion object {
        /**
         * Crée une liste de patients mockés pour les tests
         */
        fun createMockedPatients(): List<Patient> = listOf(
            Patient(
                id = "P001",
                ipp = "123456789",
                nom = "DUPONT",
                prenom = "Jean",
                dateNaissance = LocalDate.of(1980, 5, 15),
                sexe = Sexe.MASCULIN,
                numeroSecuriteSociale = "180055512345678",
                chambre = "101",
                service = "Cardiologie",
                batiment = "A",
                etage = 2,
                alertesMedicales = listOf(
                    AlerteMedicale(
                        type = TypeAlerte.ALLERGIE,
                        titre = "Pénicilline",
                        description = "Réaction allergique sévère"
                    )
                )
            ),
            Patient(
                id = "P002",
                ipp = "987654321",
                nom = "MARTIN",
                prenom = "Marie",
                dateNaissance = LocalDate.of(1975, 8, 22),
                sexe = Sexe.FEMININ,
                numeroSecuriteSociale = "275085512345678",
                chambre = "205",
                service = "Neurologie",
                batiment = "B",
                etage = 3,
                alertesMedicales = emptyList()
            ),
            Patient(
                id = "P003",
                ipp = "456789123",
                nom = "BERNARD",
                prenom = "Pierre",
                dateNaissance = LocalDate.of(1990, 12, 1),
                sexe = Sexe.MASCULIN,
                numeroSecuriteSociale = "190125512345678",
                chambre = "312",
                service = "Orthopédie",
                batiment = "A",
                etage = 4,
                alertesMedicales = listOf(
                    AlerteMedicale(
                        type = TypeAlerte.ISOLEMENT,
                        titre = "Isolement contact",
                        description = "BMR positif"
                    )
                )
            )
        )

        /**
         * Crée un patient pour les tests
         */
        fun createTestPatient(
            id: String = "TEST001",
            ipp: String = "111222333",
            nom: String = "TEST",
            prenom: String = "Patient"
        ): Patient = Patient(
            id = id,
            ipp = ipp,
            nom = nom,
            prenom = prenom,
            dateNaissance = LocalDate.of(1990, 1, 1),
            sexe = Sexe.MASCULIN,
            numeroSecuriteSociale = null,
            chambre = "101",
            service = "Test Service",
            batiment = "A",
            etage = 1,
            alertesMedicales = emptyList()
        )
    }
}

/**
 * Interface pour le PatientRepository
 * Permet l'injection de dépendances et le mocking
 */
interface IPatientRepository {
    suspend fun searchPatients(
        nom: String? = null,
        prenom: String? = null,
        ipp: String? = null,
        numeroSecuriteSociale: String? = null,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<List<Patient>>

    suspend fun getPatientById(id: String): NetworkResult<Patient>

    suspend fun getPatientByIpp(ipp: String): NetworkResult<Patient>
}

