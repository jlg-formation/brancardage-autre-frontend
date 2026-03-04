package com.example.huybrancardage.data.remote.api

import com.example.huybrancardage.data.remote.dto.BrancardageRequestDto
import com.example.huybrancardage.data.remote.dto.BrancardageResponseDto
import com.example.huybrancardage.data.remote.dto.DestinationDto
import com.example.huybrancardage.data.remote.dto.MediaUploadResponseDto
import com.example.huybrancardage.data.remote.dto.PatientDto
import com.example.huybrancardage.data.remote.dto.PatientSearchResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit pour l'API Brancardage
 */
interface BrancardageApiService {

    /**
     * Recherche de patients selon différents critères
     */
    @GET("patients")
    suspend fun searchPatients(
        @Query("nom") nom: String? = null,
        @Query("prenom") prenom: String? = null,
        @Query("ipp") ipp: String? = null,
        @Query("numeroSecuriteSociale") numeroSecuriteSociale: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PatientSearchResponseDto>

    /**
     * Récupère un patient par son ID
     */
    @GET("patients/{id}")
    suspend fun getPatientById(
        @Path("id") id: String
    ): Response<PatientDto>

    /**
     * Récupère un patient par son IPP
     */
    @GET("patients/by-ipp/{ipp}")
    suspend fun getPatientByIpp(
        @Path("ipp") ipp: String
    ): Response<PatientDto>

    /**
     * Récupère la liste des destinations disponibles
     */
    @GET("destinations")
    suspend fun getDestinations(
        @Query("recherche") recherche: String? = null,
        @Query("frequentes") frequentes: Boolean? = null
    ): Response<List<DestinationDto>>

    /**
     * Récupère une destination par son ID
     */
    @GET("destinations/{id}")
    suspend fun getDestinationById(
        @Path("id") id: String
    ): Response<DestinationDto>

    // ==================== BRANCARDAGE ====================

    /**
     * Crée une nouvelle demande de brancardage
     */
    @POST("brancardages")
    suspend fun createBrancardage(
        @Body request: BrancardageRequestDto
    ): Response<BrancardageResponseDto>

    // ==================== MEDIAS ====================

    /**
     * Upload un fichier média (photo ou document)
     */
    @Multipart
    @POST("medias")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("description") description: RequestBody?
    ): Response<MediaUploadResponseDto>
}

