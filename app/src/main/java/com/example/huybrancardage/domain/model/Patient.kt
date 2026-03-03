package com.example.huybrancardage.domain.model

import java.time.LocalDate

/**
 * Représentation d'un patient
 */
data class Patient(
    val id: String,
    val ipp: String,
    val nom: String,
    val prenom: String,
    val dateNaissance: LocalDate,
    val sexe: Sexe,
    val numeroSecuriteSociale: String? = null,
    val chambre: String? = null,
    val service: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    val alertesMedicales: List<AlerteMedicale> = emptyList()
) {
    /**
     * Nom complet du patient
     */
    val nomComplet: String
        get() = "$prenom $nom"

    /**
     * Initiales du patient (pour l'avatar)
     */
    val initiales: String
        get() = "${prenom.firstOrNull()?.uppercaseChar() ?: ""}${nom.firstOrNull()?.uppercaseChar() ?: ""}"

    /**
     * Âge calculé du patient
     */
    val age: Int
        get() {
            val today = LocalDate.now()
            return today.year - dateNaissance.year -
                    (if (today.dayOfYear < dateNaissance.dayOfYear) 1 else 0)
        }

    /**
     * Localisation formatée du patient
     */
    val localisationFormattee: String
        get() = buildString {
            service?.let { append(it) }
            chambre?.let {
                if (isNotEmpty()) append(" - ")
                append(it)
            }
        }
}

/**
 * Sexe du patient
 */
enum class Sexe(val libelle: String) {
    MASCULIN("Homme"),
    FEMININ("Femme");

    companion object {
        fun fromCode(code: String): Sexe = when (code.uppercase()) {
            "M" -> MASCULIN
            "F" -> FEMININ
            else -> MASCULIN
        }
    }
}

/**
 * Alerte médicale associée au patient
 */
data class AlerteMedicale(
    val type: TypeAlerte,
    val titre: String,
    val description: String? = null
)

/**
 * Type d'alerte médicale
 */
enum class TypeAlerte {
    ALLERGIE,
    PRECAUTION,
    ISOLEMENT,
    AUTRE
}

