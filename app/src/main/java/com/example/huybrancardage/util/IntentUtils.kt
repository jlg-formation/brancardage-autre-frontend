package com.example.huybrancardage.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Utilitaires pour les Intents Android
 * Gère les appels téléphoniques et le partage de contenu
 */
object IntentUtils {

    /**
     * Numéro du standard de brancardage de l'hôpital
     */
    const val BRANCARDAGE_PHONE_NUMBER = "tel:+33123456789"

    /**
     * Ouvre le composeur téléphonique avec le numéro de brancardage
     * Utilise ACTION_DIAL pour ne pas nécessiter de permission CALL_PHONE
     *
     * @param context Le contexte Android
     */
    fun dialBrancardageService(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = BRANCARDAGE_PHONE_NUMBER.toUri()
        }

        // Vérifier qu'une application peut gérer cet intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Aucune application téléphone disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Ouvre le composeur téléphonique avec un numéro personnalisé
     *
     * @param context Le contexte Android
     * @param phoneNumber Le numéro à appeler (format: "+33123456789")
     */
    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Aucune application téléphone disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Partage les informations d'une demande de brancardage
     *
     * @param context Le contexte Android
     * @param patientName Nom du patient
     * @param patientIpp IPP du patient
     * @param depart Lieu de départ
     * @param destination Destination
     * @param mediaCount Nombre de médias joints
     */
    fun shareBrancardageRequest(
        context: Context,
        patientName: String,
        patientIpp: String,
        depart: String,
        destination: String,
        mediaCount: Int = 0
    ) {
        val shareText = buildShareText(
            patientName = patientName,
            patientIpp = patientIpp,
            depart = depart,
            destination = destination,
            mediaCount = mediaCount
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Demande de brancardage - $patientName")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        // Créer un chooser pour permettre à l'utilisateur de choisir l'application
        val chooserIntent = Intent.createChooser(shareIntent, "Partager via")

        if (chooserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooserIntent)
        } else {
            Toast.makeText(
                context,
                "Aucune application de partage disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Construit le texte de partage formaté
     */
    private fun buildShareText(
        patientName: String,
        patientIpp: String,
        depart: String,
        destination: String,
        mediaCount: Int
    ): String {
        return buildString {
            appendLine("📋 DEMANDE DE BRANCARDAGE")
            appendLine("═══════════════════════════")
            appendLine()
            appendLine("👤 PATIENT")
            appendLine("   Nom: $patientName")
            appendLine("   IPP: $patientIpp")
            appendLine()
            appendLine("🚶 TRAJET")
            appendLine("   Départ: $depart")
            appendLine("   Arrivée: $destination")
            appendLine()
            if (mediaCount > 0) {
                appendLine("📷 MÉDIAS: $mediaCount fichier(s) joint(s)")
                appendLine()
            }
            appendLine("═══════════════════════════")
            appendLine("Envoyé depuis HuyBrancardage")
        }
    }

    /**
     * Envoie un email avec les détails de brancardage
     *
     * @param context Le contexte Android
     * @param recipient Adresse email du destinataire
     * @param patientName Nom du patient
     * @param patientIpp IPP du patient
     * @param depart Lieu de départ
     * @param destination Destination
     */
    fun sendBrancardageEmail(
        context: Context,
        recipient: String = "brancardage@hopital.fr",
        patientName: String,
        patientIpp: String,
        depart: String,
        destination: String
    ) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, "Demande de brancardage - $patientName (IPP: $patientIpp)")
            putExtra(
                Intent.EXTRA_TEXT,
                buildShareText(patientName, patientIpp, depart, destination, 0)
            )
        }

        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(emailIntent)
        } else {
            Toast.makeText(
                context,
                "Aucune application email disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


