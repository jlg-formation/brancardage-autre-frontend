package com.example.huybrancardage

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.huybrancardage.data.local.OfflineQueueManager
import com.example.huybrancardage.data.local.PendingStatus
import com.example.huybrancardage.data.local.toBrancardageRequest
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.BrancardageRepository
import com.example.huybrancardage.navigation.BrancardageNavGraph
import com.example.huybrancardage.navigation.Route
import com.example.huybrancardage.receiver.NetworkReceiver
import com.example.huybrancardage.ui.screens.AccueilScreen
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
 * ## Objectif pédagogique - BroadcastReceiver et NetworkCallback
 *
 * Cette activité enregistre un **NetworkCallback** pour surveiller les changements
 * de connectivité réseau. Cela permet à l'application de :
 * - Détecter quand le réseau devient indisponible (mode hors ligne)
 * - Mettre en file d'attente les demandes pendant la déconnexion
 * - Synchroniser automatiquement quand le réseau revient
 *
 * ### Exemple d'enregistrement du NetworkCallback :
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     NetworkReceiver.registerNetworkCallback(this)
 * }
 *
 * override fun onDestroy() {
 *     NetworkReceiver.unregisterNetworkCallback(this)
 *     super.onDestroy()
 * }
 * ```
 *
 * @see BrancardageNavGraph pour le graphe de navigation complet
 * @see NetworkReceiver pour la gestion de la connectivité réseau
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"

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
     * Gestionnaire de la file d'attente hors ligne.
     */
    private lateinit var offlineQueueManager: OfflineQueueManager

    /**
     * Repository pour l'envoi des demandes de brancardage.
     */
    private val brancardageRepository = BrancardageRepository.getInstance()

    /**
     * Configure l'activité avec le thème et le graphe de navigation.
     *
     * @param savedInstanceState État sauvegardé de l'activité (si restauration)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialiser le gestionnaire de file d'attente
        offlineQueueManager = OfflineQueueManager.getInstance(this)

        // ============================================================
        // Enregistrement du NetworkCallback pour la détection de connectivité
        // ============================================================
        // Le NetworkCallback surveille les changements de réseau et permet
        // de basculer automatiquement entre mode en ligne et hors ligne.
        NetworkReceiver.registerNetworkCallback(this)

        // ============================================================
        // Configuration du callback de synchronisation
        // ============================================================
        // Quand le réseau revient, ce callback est appelé pour synchroniser
        // automatiquement les demandes en attente.
        NetworkReceiver.setOnNetworkRestoredCallback {
            Log.i(TAG, "🔄 Callback de synchronisation appelé - Lancement de la sync")
            syncPendingRequests()
        }

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
     * Appelé quand l'activité est détruite.
     *
     * ## Objectif pédagogique - Cycle de vie et NetworkCallback
     * Il est crucial de désenregistrer le NetworkCallback quand l'activité
     * est détruite pour éviter les fuites de mémoire et les callbacks orphelins.
     */
    override fun onDestroy() {
        // Désenregistrer le NetworkCallback pour éviter les fuites de mémoire
        NetworkReceiver.unregisterNetworkCallback(this)
        super.onDestroy()
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

    /**
     * Synchronise les demandes de brancardage en attente avec le serveur.
     *
     * ## Objectif pédagogique - BroadcastReceiver et synchronisation
     *
     * Cette méthode est appelée automatiquement quand le réseau redevient disponible.
     * Elle illustre :
     * 1. L'utilisation de `lifecycleScope` pour les coroutines liées au cycle de vie
     * 2. La gestion d'une file d'attente locale
     * 3. La synchronisation automatique des données
     */
    private fun syncPendingRequests() {
        lifecycleScope.launch {
            // Petit délai pour s'assurer que la connexion est stable
            delay(1000)

            val waitingRequests = offlineQueueManager.getWaitingRequests()

            if (waitingRequests.isEmpty()) {
                Log.d(TAG, "📭 Aucune demande en attente à synchroniser")
                return@launch
            }

            Log.i(TAG, "🔄 Synchronisation de ${waitingRequests.size} demande(s) en attente")

            var syncedCount = 0
            var failedCount = 0

            for (pendingRequest in waitingRequests) {
                // Marquer comme en cours de synchronisation
                offlineQueueManager.updateStatus(pendingRequest.id, PendingStatus.SYNCING)

                try {
                    // Convertir et envoyer la demande
                    val request = pendingRequest.request.toBrancardageRequest()
                    val result = brancardageRepository.createBrancardage(request)

                    when (result) {
                        is NetworkResult.Success -> {
                            Log.i(TAG, "✅ Demande ${pendingRequest.id} synchronisée avec succès")
                            offlineQueueManager.removeFromQueue(pendingRequest.id)
                            syncedCount++
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "❌ Échec de synchronisation: ${result.message}")
                            offlineQueueManager.updateStatus(pendingRequest.id, PendingStatus.FAILED)
                            failedCount++
                        }
                        is NetworkResult.Loading -> { /* Ignore */ }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception lors de la synchronisation", e)
                    offlineQueueManager.updateStatus(pendingRequest.id, PendingStatus.FAILED)
                    failedCount++
                }

                // Petit délai entre les requêtes
                delay(500)
            }

            Log.i(TAG, "📊 Synchronisation terminée: $syncedCount réussie(s), $failedCount échouée(s)")
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
