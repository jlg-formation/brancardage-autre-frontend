package com.example.huybrancardage.ui.state

import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.Patient

/**
 * État de la session de brancardage en cours
 * Contient toutes les données collectées pendant le parcours utilisateur
 */
data class BrancardageSessionState(
    val patient: Patient? = null,
    val medias: List<Media> = emptyList(),
    val localisation: Localisation? = null,
    val destination: Destination? = null,
    val commentaire: String = ""
) {
    /**
     * Vérifie si la session est prête pour la validation
     */
    val isReadyForValidation: Boolean
        get() = patient != null &&
                localisation?.isValid == true &&
                destination != null
}

