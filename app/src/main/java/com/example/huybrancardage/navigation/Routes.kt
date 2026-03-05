package com.example.huybrancardage.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    // Confirmation avec paramètres
    data object Confirmation : Route("confirmation/{trackingNumber}/{patientName}") {
        fun createRoute(trackingNumber: String, patientName: String): String {
            val encodedTracking = URLEncoder.encode(trackingNumber, StandardCharsets.UTF_8.toString())
            val encodedName = URLEncoder.encode(patientName, StandardCharsets.UTF_8.toString())
            return "confirmation/$encodedTracking/$encodedName"
        }

        fun decodeTrackingNumber(encoded: String): String {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        }

        fun decodePatientName(encoded: String): String {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        }
    }

    // Confirmation hors ligne (demande mise en file d'attente)
    data object ConfirmationQueued : Route("confirmation_queued/{patientName}") {
        fun createRoute(patientName: String): String {
            val encodedName = URLEncoder.encode(patientName, StandardCharsets.UTF_8.toString())
            return "confirmation_queued/$encodedName"
        }

        fun decodePatientName(encoded: String): String {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        }
    }
}
