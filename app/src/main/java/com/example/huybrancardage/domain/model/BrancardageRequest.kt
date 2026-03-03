package com.example.huybrancardage.domain.model

import java.time.Instant

/**
 * Requête de création d'une demande de brancardage
 */
data class BrancardageRequest(
    val patientId: String,
    val depart: Localisation,
    val destinationId: String,
    val mediaIds: List<String> = emptyList(),
    val commentaire: String? = null
)

/**
 * Réponse de création d'une demande de brancardage
 */
data class BrancardageResponse(
    val id: String,
    val statut: StatutBrancardage,
    val dateCreation: Instant,
    val patient: Patient,
    val depart: Localisation,
    val destination: Destination,
    val medias: List<Media> = emptyList(),
    val commentaire: String? = null
)

/**
 * Statut d'une demande de brancardage
 */
enum class StatutBrancardage {
    EN_ATTENTE,
    EN_COURS,
    TERMINE,
    ANNULE
}

