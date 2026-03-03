package com.example.huybrancardage.domain.model

import java.time.Instant

/**
 * Représentation d'un fichier média (photo ou document)
 */
data class Media(
    val id: String,
    val uri: String,
    val type: MediaType,
    val mimeType: String = "image/jpeg",
    val taille: Long = 0,
    val dateAjout: Instant = Instant.now(),
    val description: String? = null
)

/**
 * Type de média
 */
enum class MediaType {
    PHOTO,
    DOCUMENT
}

