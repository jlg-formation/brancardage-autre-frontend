package com.example.huybrancardage.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Réponse d'erreur standardisée de l'API
 */
@Serializable
data class ErrorResponseDto(
    val code: String,
    val message: String,
    val timestamp: String,
    val details: List<ErrorDetailDto>? = null
)

/**
 * Détail d'une erreur de validation
 */
@Serializable
data class ErrorDetailDto(
    val field: String,
    val message: String
)

