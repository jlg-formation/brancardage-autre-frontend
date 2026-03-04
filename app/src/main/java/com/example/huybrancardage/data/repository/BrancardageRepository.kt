package com.example.huybrancardage.data.repository

import android.content.Context
import android.util.Log
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.remote.api.ApiClient
import com.example.huybrancardage.data.remote.api.BrancardageApiService
import com.example.huybrancardage.data.remote.dto.BrancardageResponseDto
import com.example.huybrancardage.data.remote.dto.LocalisationDto
import com.example.huybrancardage.data.remote.dto.MediaUploadResponseDto
import com.example.huybrancardage.data.remote.dto.PatientResumeDto
import com.example.huybrancardage.data.remote.mapper.BrancardageMapper
import com.example.huybrancardage.domain.model.BrancardageRequest
import com.example.huybrancardage.domain.model.BrancardageResponse
import com.example.huybrancardage.domain.model.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.UUID

private const val TAG = "BrancardageRepository"

/**
 * Repository pour la gestion des demandes de brancardage
 */
class BrancardageRepository(
    private val apiService: BrancardageApiService = ApiClient.apiService,
    private val useMockedData: Boolean = false // Utiliser l'API réelle par défaut
) {

    companion object {
        @Volatile
        private var instance: BrancardageRepository? = null

        fun getInstance(): BrancardageRepository {
            return instance ?: synchronized(this) {
                instance ?: BrancardageRepository(
                    apiService = ApiClient.apiService,
                    useMockedData = false // API réelle
                ).also { instance = it }
            }
        }
    }

    /**
     * Crée une nouvelle demande de brancardage
     */
    suspend fun createBrancardage(request: BrancardageRequest): NetworkResult<BrancardageResponse> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "=== POST /brancardages ===")
            Log.d(TAG, "PatientId: ${request.patientId}")
            Log.d(TAG, "Départ: ${request.depart.descriptionFormattee}")
            Log.d(TAG, "DestinationId: ${request.destinationId}")
            Log.d(TAG, "MediaIds: ${request.mediaIds}")
            Log.d(TAG, "Commentaire: ${request.commentaire}")
            Log.d(TAG, "UseMockedData: $useMockedData")

            try {
                if (useMockedData) {
                    Log.d(TAG, "Mode MOCK - Simulation de l'envoi...")
                    // Simulation d'un délai réseau
                    delay(1500)

                    // Création d'une réponse mockée
                    val mockResponse = createMockedResponse(request)
                    Log.d(TAG, "Réponse mockée créée - ID: ${mockResponse.id}")
                    NetworkResult.Success(BrancardageMapper.toDomain(mockResponse))
                } else {
                    Log.d(TAG, "Mode API RÉELLE - Envoi vers le serveur...")
                    // Appel API réel
                    val requestDto = BrancardageMapper.toDto(request)
                    val response = apiService.createBrancardage(requestDto)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.d(TAG, "Succès API - ID: ${body.id}")
                            NetworkResult.Success(BrancardageMapper.toDomain(body))
                        } else {
                            Log.e(TAG, "Erreur: Réponse vide du serveur")
                            NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                        }
                    } else {
                        Log.e(TAG, "Erreur API: ${response.code()} - ${response.message()}")
                        NetworkResult.Error(
                            code = "API_ERROR",
                            message = "Erreur lors de la création: ${response.message()}",
                            httpCode = response.code()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                NetworkResult.Error(
                    code = "NETWORK_ERROR",
                    message = e.message ?: "Erreur réseau inconnue"
                )
            }
        }

    /**
     * Upload un média (photo ou document)
     */
    suspend fun uploadMedia(
        context: Context,
        media: Media
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            if (useMockedData) {
                // Simulation d'un délai réseau
                delay(800)

                // Retourne un ID mocké
                val mockedId = "media-${UUID.randomUUID().toString().take(8)}"
                NetworkResult.Success(mockedId)
            } else {
                // Préparation du fichier
                val file = createTempFileFromUri(context, media)
                    ?: return@withContext NetworkResult.Error(
                        "FILE_ERROR",
                        "Impossible de lire le fichier média"
                    )

                val requestFile = file.asRequestBody("image/jpeg".toMediaType())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val type = media.type.name.toRequestBody("text/plain".toMediaType())
                val description = media.description?.toRequestBody("text/plain".toMediaType())

                val response = apiService.uploadMedia(body, type, description)

                // Nettoyer le fichier temporaire
                file.delete()

                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse != null) {
                        NetworkResult.Success(uploadResponse.id)
                    } else {
                        NetworkResult.Error("EMPTY_RESPONSE", "Réponse vide du serveur")
                    }
                } else {
                    NetworkResult.Error(
                        code = "API_ERROR",
                        message = "Erreur lors de l'upload: ${response.message()}",
                        httpCode = response.code()
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                code = "UPLOAD_ERROR",
                message = e.message ?: "Erreur lors de l'upload du média"
            )
        }
    }

    /**
     * Crée un fichier temporaire à partir d'une URI
     */
    private fun createTempFileFromUri(context: Context, media: Media): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(
                android.net.Uri.parse(media.uri)
            ) ?: return null

            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crée une réponse mockée pour le développement
     */
    private fun createMockedResponse(request: BrancardageRequest): BrancardageResponseDto {
        // Génère un numéro de suivi unique
        val trackingNumber = "BRC-2026-${String.format("%03d", (1..999).random())}"

        return BrancardageResponseDto(
            id = trackingNumber,
            statut = "EN_ATTENTE",
            dateCreation = Instant.now().toString(),
            patient = PatientResumeDto(
                id = request.patientId,
                nom = "Dupont",  // Valeurs par défaut
                prenom = "Jean",
                ipp = "123456789"
            ),
            depart = LocalisationDto(
                latitude = request.depart.latitude,
                longitude = request.depart.longitude,
                description = request.depart.description,
                batiment = request.depart.batiment,
                etage = request.depart.etage,
                chambre = request.depart.chambre
            ),
            destination = com.example.huybrancardage.data.remote.dto.DestinationDto(
                id = request.destinationId,
                nom = "Bloc Opératoire",
                batiment = "A",
                etage = 1,
                etageLibelle = "Étage 1",
                frequente = true
            ),
            medias = emptyList(),
            commentaire = request.commentaire
        )
    }
}

