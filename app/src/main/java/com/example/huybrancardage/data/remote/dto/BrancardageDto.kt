package com.example.huybrancardage.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pour une requête de création de brancardage
 */
@Serializable
data class BrancardageRequestDto(
    @SerialName("patientId")
    val patientId: String,
    val depart: LocalisationDto,
    @SerialName("destinationId")
    val destinationId: String,
    @SerialName("mediaIds")
    val mediaIds: List<String> = emptyList(),
    val commentaire: String? = null
)

/**
 * DTO pour une localisation
 */
@Serializable
data class LocalisationDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    val chambre: String? = null
)

/**
 * DTO pour la réponse de création de brancardage
 */
@Serializable
data class BrancardageResponseDto(
    val id: String,
    val statut: String,
    @SerialName("dateCreation")
    val dateCreation: String,
    val patient: PatientResumeDto,
    val depart: LocalisationDto,
    val destination: DestinationDto,
    val medias: List<MediaDto> = emptyList(),
    val commentaire: String? = null
)

/**
 * DTO résumé pour un patient (dans la réponse brancardage)
 */
@Serializable
data class PatientResumeDto(
    val id: String,
    val nom: String,
    val prenom: String,
    val ipp: String
)

/**
 * DTO pour un média
 */
@Serializable
data class MediaDto(
    val id: String,
    val url: String,
    val type: String,
    @SerialName("mimeType")
    val mimeType: String = "image/jpeg",
    val taille: Long = 0,
    @SerialName("dateUpload")
    val dateUpload: String? = null,
    val description: String? = null
)

/**
 * DTO pour la réponse d'upload de média
 */
@Serializable
data class MediaUploadResponseDto(
    val id: String,
    val url: String,
    val type: String,
    @SerialName("mimeType")
    val mimeType: String,
    val taille: Long,
    @SerialName("dateUpload")
    val dateUpload: String? = null,
    val description: String? = null
)

