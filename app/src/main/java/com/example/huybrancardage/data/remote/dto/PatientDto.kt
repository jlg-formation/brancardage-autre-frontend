package com.example.huybrancardage.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pour un patient reçu de l'API
 */
@Serializable
data class PatientDto(
    val id: String,
    val ipp: String,
    val nom: String,
    val prenom: String,
    @SerialName("dateNaissance")
    val dateNaissance: String,
    val sexe: String,
    @SerialName("numeroSecuriteSociale")
    val numeroSecuriteSociale: String? = null,
    val chambre: String? = null,
    val service: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    @SerialName("alertesMedicales")
    val alertesMedicales: List<AlerteMedicaleDto>? = null
)

/**
 * DTO pour une alerte médicale
 */
@Serializable
data class AlerteMedicaleDto(
    val type: String,
    val titre: String,
    val description: String? = null
)

/**
 * Réponse paginée de recherche de patients
 */
@Serializable
data class PatientSearchResponseDto(
    val content: List<PatientDto>,
    val totalElements: Int,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

