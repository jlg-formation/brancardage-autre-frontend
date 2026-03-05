package com.example.huybrancardage.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.example.huybrancardage.HelpActivity

/**
 * Utilitaires pour les Intents Android
 * Gère les appels téléphoniques, le partage de contenu et la navigation inter-activités.
 *
 * ## Types d'Intents
 *
 * ### Intent Explicite
 * Un Intent explicite spécifie le composant exact (classe) à lancer.
 * Utilisé pour naviguer entre les activités de la même application.
 * Exemple : [openHelpActivity]
 *
 * ### Intent Implicite
 * Un Intent implicite déclare une action générale à effectuer.
 * Le système Android trouve le composant approprié pour l'exécuter.
 * Exemples : [dialBrancardageService], [shareBrancardageRequest], [sendBrancardageEmail]
 *
 * @see <a href="https://developer.android.com/guide/components/intents-filters">Documentation officielle des Intents</a>
 */
object IntentUtils {

    // ========================================
    // INTENT EXPLICITE
    // ========================================

    /**
     * Ouvre l'écran d'aide (HelpActivity) via un Intent explicite.
     *
     * ## Intent Explicite
     * Un Intent explicite spécifie directement la classe du composant à démarrer.
     * Il est utilisé lorsque vous savez exactement quelle activité vous voulez lancer,
     * généralement au sein de votre propre application.
     *
     * ### Caractéristiques :
     * - Spécifie le composant cible via le nom de classe
     * - Ne nécessite pas de filtre d'intent dans le manifest
     * - Plus sécurisé car ne peut pas être intercepté par d'autres applications
     * - Utilisé pour la navigation interne à l'application
     *
     * ### Exemple de code :
     * ```kotlin
     * val intent = Intent(context, HelpActivity::class.java)
     * context.startActivity(intent)
     * ```
     *
     * @param context Le contexte Android (Activity ou Application)
     *
     * @see dialBrancardageService pour un exemple d'Intent implicite
     */
    fun openHelpActivity(context: Context) {
        // Création d'un Intent EXPLICITE
        // On spécifie directement la classe de l'activité cible
        val intent = Intent(context, HelpActivity::class.java)

        // Lancement de l'activité
        // Pas besoin de vérifier resolveActivity() car on cible notre propre composant
        context.startActivity(intent)
    }

    // ========================================
    // INTENTS IMPLICITES
    // ========================================

    /**
     * Numéro du standard de brancardage de l'hôpital
     */
    const val BRANCARDAGE_PHONE_NUMBER = "tel:+32111222333"

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


