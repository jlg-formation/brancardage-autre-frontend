package com.example.huybrancardage.domain.model

/**
 * Représentation d'une localisation (GPS + descriptive)
 */
data class Localisation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    val chambre: String? = null,
    val precisions: String? = null
) {
    /**
     * Localisation formatée pour l'affichage
     */
    val descriptionFormattee: String
        get() = buildString {
            if (description != null) {
                append(description)
            } else {
                batiment?.let { append("Bâtiment $it") }
                etage?.let {
                    if (isNotEmpty()) append(" - ")
                    append(if (it == 0) "RDC" else "Étage $it")
                }
                chambre?.let {
                    if (isNotEmpty()) append(", ")
                    append("Chambre $it")
                }
            }
        }

    /**
     * Détails supplémentaires formatés
     */
    val detailsFormattes: String
        get() = buildString {
            etage?.let { append(if (it == 0) "RDC" else "Étage $it") }
            chambre?.let {
                if (isNotEmpty()) append(", ")
                append("Chambre $it")
            }
        }

    /**
     * Vérifie si la localisation est valide (a au moins des coordonnées ou une description)
     */
    val isValid: Boolean
        get() = (latitude != null && longitude != null) ||
                !description.isNullOrBlank() ||
                !batiment.isNullOrBlank()
}

