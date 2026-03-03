package com.example.huybrancardage.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO pour une destination reçue de l'API
 */
@Serializable
data class DestinationDto(
    val id: String,
    val nom: String,
    val batiment: String,
    val etage: Int,
    val etageLibelle: String? = null,
    val frequente: Boolean = false
)

