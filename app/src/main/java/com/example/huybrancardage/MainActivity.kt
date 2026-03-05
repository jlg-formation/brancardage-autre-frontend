package com.example.huybrancardage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.huybrancardage.navigation.BrancardageNavGraph
import com.example.huybrancardage.navigation.Route
import com.example.huybrancardage.ui.screens.AccueilScreen
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme

/**
 * Point d'entrée principal de l'application HuyBrancardage.
 *
 * Cette application permet aux brancardiers de :
 * - Rechercher un patient (par scan de bracelet ou recherche manuelle)
 * - Consulter les informations du patient et ses alertes médicales
 * - Prendre des photos ou sélectionner des médias
 * - Définir la localisation de départ et la destination
 * - Soumettre une demande de brancardage
 *
 * ## Objectif pédagogique - Bundles
 *
 * Cette activité démontre l'utilisation des **Bundles** pour la communication
 * entre composants Android. Quand l'utilisateur clique sur la notification
 * du service de tracking, l'Intent contient un Bundle avec les données
 * de navigation pour afficher directement l'écran de récapitulatif.
 *
 * ### Exemple de Bundle utilisé :
 * ```kotlin
 * val bundle = Bundle().apply {
 *     putString(EXTRA_NAVIGATE_TO, DESTINATION_RECAPITULATIF)
 *     putString(EXTRA_PATIENT_ID, "12345")
 * }
 * intent.putExtras(bundle)
 * ```
 *
 * @see BrancardageNavGraph pour le graphe de navigation complet
 */
class MainActivity : ComponentActivity() {

    companion object {
        // ============================================================
        // Clés pour les extras du Bundle
        // ============================================================

        /**
         * Clé pour spécifier l'écran de destination.
         * Valeur attendue : une des constantes DESTINATION_*
         */
        const val EXTRA_NAVIGATE_TO = "navigate_to"

        /**
         * Clé pour passer l'ID du patient (optionnel).
         */
        const val EXTRA_PATIENT_ID = "patient_id"

        // ============================================================
        // Valeurs possibles pour EXTRA_NAVIGATE_TO
        // ============================================================

        /**
         * Valeur pour naviguer vers l'écran de récapitulatif.
         * Utilisé quand l'utilisateur clique sur la notification de tracking.
         */
        const val DESTINATION_RECAPITULATIF = "recapitulatif"
    }

    /**
     * Référence au NavController pour permettre la navigation depuis onNewIntent.
     */
    private var navController: NavHostController? = null

    /**
     * Configure l'activité avec le thème et le graphe de navigation.
     *
     * @param savedInstanceState État sauvegardé de l'activité (si restauration)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extraire les données du Bundle (via Intent extras)
        // Le Bundle est un conteneur clé-valeur pour passer des données
        val navigateTo = intent.extras?.getString(EXTRA_NAVIGATE_TO)
        val patientId = intent.extras?.getString(EXTRA_PATIENT_ID)

        setContent {
            HuyBrancardageTheme {
                val localNavController = rememberNavController()
                navController = localNavController

                // Navigation automatique si l'activité est lancée depuis la notification
                // LaunchedEffect s'exécute une fois quand navigateTo change
                LaunchedEffect(navigateTo) {
                    handleNavigationFromBundle(localNavController, navigateTo)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrancardageNavGraph(
                        navController = localNavController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Appelé quand l'activité reçoit un nouvel Intent alors qu'elle est déjà ouverte.
     *
     * Cela arrive quand l'utilisateur clique sur la notification alors que
     * l'application est déjà au premier plan.
     *
     * ## Objectif pédagogique
     * Démontre la gestion de `onNewIntent` pour les Intent avec flags
     * `FLAG_ACTIVITY_SINGLE_TOP` ou `FLAG_ACTIVITY_CLEAR_TOP`.
     *
     * @param intent Le nouvel Intent reçu
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Met à jour l'Intent stocké pour une future référence
        setIntent(intent)

        // Extraire les données du Bundle
        val navigateTo = intent.extras?.getString(EXTRA_NAVIGATE_TO)

        // Naviguer directement si on a un NavController
        navController?.let { controller ->
            handleNavigationFromBundle(controller, navigateTo)
        }
    }

    /**
     * Gère la navigation basée sur les données du Bundle.
     *
     * ## Objectif pédagogique
     * Cette méthode montre comment utiliser les données extraites d'un Bundle
     * pour effectuer une action (ici, naviguer vers un écran spécifique).
     *
     * @param navController Le NavController pour la navigation
     * @param navigateTo La destination extraite du Bundle (ou null)
     */
    private fun handleNavigationFromBundle(navController: NavHostController, navigateTo: String?) {
        when (navigateTo) {
            DESTINATION_RECAPITULATIF -> {
                // Navigue vers l'écran de récapitulatif
                // popUpTo évite d'empiler les écrans dans le back stack
                navController.navigate(Route.Recapitulatif.route) {
                    // On garde l'accueil dans le back stack pour le retour
                    popUpTo(Route.Accueil.route) {
                        inclusive = false
                    }
                    // Évite de créer plusieurs instances du même écran
                    launchSingleTop = true
                }
            }
            // Ajouter d'autres destinations ici si nécessaire
            // DESTINATION_MEDIAS -> navController.navigate(Route.Medias.route)
        }
    }
}

/**
 * Prévisualisation de l'écran d'accueil pour l'IDE.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HuyBrancardageTheme {
        AccueilScreen()
    }
}
