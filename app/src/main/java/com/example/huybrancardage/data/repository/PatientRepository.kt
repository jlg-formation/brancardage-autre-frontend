package com.example.huybrancardage.data.repository

import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.remote.api.ApiClient
import com.example.huybrancardage.data.remote.api.BrancardageApiService
import com.example.huybrancardage.data.remote.dto.AlerteMedicaleDto
import com.example.huybrancardage.data.remote.dto.PatientDto
import com.example.huybrancardage.data.remote.dto.PatientSearchResponseDto
import com.example.huybrancardage.data.remote.mapper.PatientMapper
import com.example.huybrancardage.domain.model.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repository pour la gestion des patients
 * Utilise des données mockées en attendant l'API backend
 */
class PatientRepository(
    private val apiService: BrancardageApiService = ApiClient.apiService,
    private val useMockedData: Boolean = false // Utiliser l'API réelle
) {

    /**
     * Recherche de patients selon les critères
     */
    suspend fun searchPatients(
        nom: String? = null,
        prenom: String? = null,
        ipp: String? = null,
        numeroSecuriteSociale: String? = null,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<List<Patient>> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                // Simulation d'un délai réseau
                delay(500)
                val patients = searchPatientsMocked(nom, prenom, ipp, numeroSecuriteSociale)
                NetworkResult.Success(patients)
            } else {
                // Appel API réel
                val response = apiService.searchPatients(
                    nom = nom?.takeIf { it.isNotBlank() },
                    prenom = prenom?.takeIf { it.isNotBlank() },
                    ipp = ipp?.takeIf { it.isNotBlank() },
                    numeroSecuriteSociale = numeroSecuriteSociale?.takeIf { it.isNotBlank() },
                    page = page,
                    size = size
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(PatientMapper.toDomainList(body.content))
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = "API_ERROR",
                        message = "Erreur lors de la recherche: ${response.message()}",
                        httpCode = response.code()
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Erreur réseau inconnue"
            )
        }
    }

    /**
     * Récupère un patient par son ID
     */
    suspend fun getPatientById(id: String): NetworkResult<Patient> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                delay(300)
                val patient = getMockedPatients().find { it.id == id }
                if (patient != null) {
                    NetworkResult.Success(PatientMapper.toDomain(patient))
                } else {
                    NetworkResult.Error("PATIENT_NOT_FOUND", "Aucun patient trouvé avec l'ID: $id")
                }
            } else {
                val response = apiService.getPatientById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(PatientMapper.toDomain(body))
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = if (response.code() == 404) "PATIENT_NOT_FOUND" else "API_ERROR",
                        message = "Patient non trouvé",
                        httpCode = response.code()
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Erreur réseau inconnue"
            )
        }
    }

    /**
     * Récupère un patient par son IPP
     */
    suspend fun getPatientByIpp(ipp: String): NetworkResult<Patient> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                delay(300)
                val patient = getMockedPatients().find { it.ipp == ipp }
                if (patient != null) {
                    NetworkResult.Success(PatientMapper.toDomain(patient))
                } else {
                    NetworkResult.Error("PATIENT_NOT_FOUND", "Aucun patient trouvé avec l'IPP: $ipp")
                }
            } else {
                val response = apiService.getPatientByIpp(ipp)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(PatientMapper.toDomain(body))
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = if (response.code() == 404) "PATIENT_NOT_FOUND" else "API_ERROR",
                        message = "Aucun patient trouvé avec l'IPP: $ipp",
                        httpCode = response.code()
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Erreur réseau inconnue"
            )
        }
    }

    /**
     * Recherche mockée de patients
     */
    private fun searchPatientsMocked(
        nom: String?,
        prenom: String?,
        ipp: String?,
        numeroSecuriteSociale: String?
    ): List<Patient> {
        return getMockedPatients()
            .filter { patient ->
                val matchNom = nom.isNullOrBlank() ||
                    patient.nom.contains(nom, ignoreCase = true)
                val matchPrenom = prenom.isNullOrBlank() ||
                    patient.prenom.contains(prenom, ignoreCase = true)
                val matchIpp = ipp.isNullOrBlank() ||
                    patient.ipp == ipp
                val matchSecu = numeroSecuriteSociale.isNullOrBlank() ||
                    patient.numeroSecuriteSociale == numeroSecuriteSociale

                matchNom && matchPrenom && matchIpp && matchSecu
            }
            .map { PatientMapper.toDomain(it) }
    }

    /**
     * Liste de patients mockés (données en dur)
     */
    private fun getMockedPatients(): List<PatientDto> = listOf(
        PatientDto(
            id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            ipp = "123456789",
            nom = "Dupont",
            prenom = "Jean",
            dateNaissance = "1980-05-12",
            sexe = "M",
            numeroSecuriteSociale = "180057512345678",
            chambre = "204",
            service = "Cardiologie",
            batiment = "A",
            etage = 2,
            alertesMedicales = listOf(
                AlerteMedicaleDto(
                    type = "PRECAUTION",
                    titre = "Précautions",
                    description = "Patient sous perfusion. Nécessite une potence à sérum."
                )
            )
        ),
        PatientDto(
            id = "a23bc45d-67ef-8901-g234-5h67i89j0k12",
            ipp = "987654321",
            nom = "Martin",
            prenom = "Marie",
            dateNaissance = "1975-03-22",
            sexe = "F",
            numeroSecuriteSociale = "275037512345612",
            chambre = "112",
            service = "Pneumologie",
            batiment = "B",
            etage = 1,
            alertesMedicales = listOf(
                AlerteMedicaleDto(
                    type = "ALLERGIE",
                    titre = "Allergie",
                    description = "Allergie aux pénicillines."
                )
            )
        ),
        PatientDto(
            id = "b34cd56e-78fg-9012-h345-6i78j90k1l23",
            ipp = "456789123",
            nom = "Durand",
            prenom = "Pierre",
            dateNaissance = "1965-11-08",
            sexe = "M",
            numeroSecuriteSociale = null,
            chambre = "305",
            service = "Orthopédie",
            batiment = "A",
            etage = 3,
            alertesMedicales = null
        ),
        PatientDto(
            id = "c45de67f-89gh-0123-i456-7j89k01l2m34",
            ipp = "789123456",
            nom = "Bernard",
            prenom = "Sophie",
            dateNaissance = "1990-07-15",
            sexe = "F",
            numeroSecuriteSociale = "290077512345890",
            chambre = "118",
            service = "Neurologie",
            batiment = "C",
            etage = 1,
            alertesMedicales = listOf(
                AlerteMedicaleDto(
                    type = "ISOLEMENT",
                    titre = "Isolement",
                    description = "Isolement respiratoire. Port du masque obligatoire."
                )
            )
        ),
        PatientDto(
            id = "d56ef78g-90hi-1234-j567-8k90l12m3n45",
            ipp = "321654987",
            nom = "Petit",
            prenom = "Jacques",
            dateNaissance = "1958-02-28",
            sexe = "M",
            numeroSecuriteSociale = "158027512345234",
            chambre = "401",
            service = "Gériatrie",
            batiment = "D",
            etage = 4,
            alertesMedicales = listOf(
                AlerteMedicaleDto(
                    type = "PRECAUTION",
                    titre = "Risque de chute",
                    description = "Patient à mobilité réduite. Aide à la marche nécessaire."
                ),
                AlerteMedicaleDto(
                    type = "ALLERGIE",
                    titre = "Allergie",
                    description = "Allergie au latex."
                )
            )
        )
    )

    companion object {
        @Volatile
        private var instance: PatientRepository? = null

        /**
         * Instance singleton du repository
         */
        fun getInstance(): PatientRepository {
            return instance ?: synchronized(this) {
                instance ?: PatientRepository().also { instance = it }
            }
        }
    }
}

