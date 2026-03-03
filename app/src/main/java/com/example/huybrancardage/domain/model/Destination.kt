package com.example.huybrancardage.domain.model

/**
 * Représentation d'une destination dans l'établissement hospitalier
 */
data class Destination(
    val id: String,
    val nom: String,
    val batiment: String,
    val etage: Int,
    val etageLibelle: String? = null,
    val frequente: Boolean = false
) {
    /**
     * Localisation formatée de la destination
     */
    val localisationFormattee: String
        get() = buildString {
            append("Bâtiment $batiment")
            append(" - ")
            append(etageLibelle ?: if (etage == 0) "RDC" else "Étage $etage")
        }
}

