package com.example.huybrancardage.data.repository

import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.remote.api.ApiClient
import com.example.huybrancardage.data.remote.api.BrancardageApiService
import com.example.huybrancardage.data.remote.dto.DestinationDto
import com.example.huybrancardage.data.remote.mapper.DestinationMapper
import com.example.huybrancardage.domain.model.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repository pour la gestion des destinations
 * Utilise des données mockées en attendant l'API backend
 */
class DestinationRepository(
    private val apiService: BrancardageApiService = ApiClient.apiService,
    private val useMockedData: Boolean = false // Utiliser l'API réelle
) {

    /**
     * Récupère la liste des destinations
     */
    suspend fun getDestinations(
        recherche: String? = null,
        frequentes: Boolean? = null
    ): NetworkResult<List<Destination>> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                delay(300)
                val destinations = getDestinationsMocked(recherche, frequentes)
                NetworkResult.Success(destinations)
            } else {
                val response = apiService.getDestinations(
                    recherche = recherche?.takeIf { it.isNotBlank() },
                    frequentes = frequentes
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(DestinationMapper.toDomainList(body))
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = "API_ERROR",
                        message = "Erreur lors de la récupération des destinations",
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
     * Récupère une destination par son ID
     */
    suspend fun getDestinationById(id: String): NetworkResult<Destination> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                delay(200)
                val destination = getMockedDestinations().find { it.id == id }
                if (destination != null) {
                    NetworkResult.Success(DestinationMapper.toDomain(destination))
                } else {
                    NetworkResult.Error("DESTINATION_NOT_FOUND", "Destination non trouvée")
                }
            } else {
                val response = apiService.getDestinationById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(DestinationMapper.toDomain(body))
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = if (response.code() == 404) "DESTINATION_NOT_FOUND" else "API_ERROR",
                        message = "Destination non trouvée",
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
     * Recherche mockée de destinations
     */
    private fun getDestinationsMocked(
        recherche: String?,
        frequentes: Boolean?
    ): List<Destination> {
        return getMockedDestinations()
            .filter { destination ->
                val matchRecherche = recherche.isNullOrBlank() ||
                    destination.nom.contains(recherche, ignoreCase = true) ||
                    destination.batiment.contains(recherche, ignoreCase = true)
                val matchFrequentes = frequentes == null || destination.frequente == frequentes

                matchRecherche && matchFrequentes
            }
            .map { DestinationMapper.toDomain(it) }
    }

    /**
     * Liste de destinations mockées
     */
    private fun getMockedDestinations(): List<DestinationDto> = listOf(
        DestinationDto(
            id = "dest-001",
            nom = "Radiologie",
            batiment = "B",
            etage = 0,
            etageLibelle = "RDC",
            frequente = true
        ),
        DestinationDto(
            id = "dest-002",
            nom = "Bloc Opératoire",
            batiment = "A",
            etage = 1,
            etageLibelle = "Étage 1",
            frequente = true
        ),
        DestinationDto(
            id = "dest-003",
            nom = "Urgences",
            batiment = "C",
            etage = 0,
            etageLibelle = "RDC",
            frequente = true
        ),
        DestinationDto(
            id = "dest-004",
            nom = "Scanner",
            batiment = "B",
            etage = 0,
            etageLibelle = "RDC",
            frequente = true
        ),
        DestinationDto(
            id = "dest-005",
            nom = "IRM",
            batiment = "B",
            etage = -1,
            etageLibelle = "Sous-sol",
            frequente = true
        ),
        DestinationDto(
            id = "dest-006",
            nom = "Cardiologie",
            batiment = "A",
            etage = 2,
            etageLibelle = "Étage 2",
            frequente = false
        ),
        DestinationDto(
            id = "dest-007",
            nom = "Pneumologie",
            batiment = "B",
            etage = 1,
            etageLibelle = "Étage 1",
            frequente = false
        ),
        DestinationDto(
            id = "dest-008",
            nom = "Neurologie",
            batiment = "C",
            etage = 1,
            etageLibelle = "Étage 1",
            frequente = false
        ),
        DestinationDto(
            id = "dest-009",
            nom = "Orthopédie",
            batiment = "A",
            etage = 3,
            etageLibelle = "Étage 3",
            frequente = false
        ),
        DestinationDto(
            id = "dest-010",
            nom = "Gériatrie",
            batiment = "D",
            etage = 4,
            etageLibelle = "Étage 4",
            frequente = false
        ),
        DestinationDto(
            id = "dest-011",
            nom = "Réanimation",
            batiment = "A",
            etage = 1,
            etageLibelle = "Étage 1",
            frequente = true
        ),
        DestinationDto(
            id = "dest-012",
            nom = "Laboratoire",
            batiment = "B",
            etage = -1,
            etageLibelle = "Sous-sol",
            frequente = false
        )
    )

    companion object {
        @Volatile
        private var instance: DestinationRepository? = null

        /**
         * Instance singleton du repository
         */
        fun getInstance(): DestinationRepository {
            return instance ?: synchronized(this) {
                instance ?: DestinationRepository().also { instance = it }
            }
        }
    }
}

