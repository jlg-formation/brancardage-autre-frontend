package com.example.huybrancardage.navigation

/**
 * Définition des routes de navigation de l'application
 * Utilise un sealed class pour typer fortement les routes
 */
sealed class Route(val route: String) {

    // Écran d'accueil - Point d'entrée
    data object Accueil : Route("accueil")

    // Recherche de patient
    data object RechercheManuelle : Route("recherche_manuelle")
    data object ScanBracelet : Route("scan_bracelet")

    // Dossier patient (avec paramètre patientId optionnel pour future utilisation)
    data object DossierPatient : Route("dossier_patient")

    // Création de demande
    data object Medias : Route("medias")
    data object Localisation : Route("localisation")
    data object Destination : Route("destination")
    data object Recapitulatif : Route("recapitulatif")
    data object Confirmation : Route("confirmation")
}

