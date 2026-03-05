package com.example.huybrancardage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BroadcastReceiver pour la détection des changements de connectivité réseau.
 *
 * # Objectif pédagogique
 *
 * Ce receiver illustre plusieurs concepts importants du développement Android :
 *
 * ## 1. BroadcastReceiver
 * Un BroadcastReceiver est un composant Android qui écoute et réagit aux "broadcasts"
 * (messages système ou applicatifs). Il permet à l'application de réagir à des
 * événements même quand elle n'est pas au premier plan.
 *
 * ## 2. Enregistrement dynamique vs statique
 * - **Statique** (AndroidManifest.xml) : Le receiver est toujours actif, même si l'app est fermée
 * - **Dynamique** (registerReceiver) : Le receiver n'est actif que quand l'app tourne
 *
 * Depuis Android 7.0 (API 24), les receivers pour CONNECTIVITY_ACTION doivent être
 * enregistrés dynamiquement pour des raisons de performance.
 *
 * ## 3. NetworkCallback (approche moderne)
 * À partir d'Android 7.0, Google recommande d'utiliser NetworkCallback plutôt que
 * BroadcastReceiver pour les changements de connectivité. Cette classe combine
 * les deux approches pour la démonstration pédagogique.
 *
 * ## 4. Cas d'usage métier
 * Dans une application hospitalière, la connectivité peut être instable.
 * Ce receiver permet de :
 * - Mettre en file d'attente les demandes quand le réseau est perdu
 * - Synchroniser automatiquement quand le réseau revient
 * - Informer l'utilisateur de l'état de la connexion
 *
 * @see OfflineQueueManager pour la gestion de la file d'attente
 */
class NetworkReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkReceiver"

        // ============================================================
        // StateFlow statiques pour la communication Receiver ↔ UI
        // ============================================================

        /**
         * État actuel de la connexion réseau.
         * Observable depuis n'importe quel composant Compose.
         *
         * Utilisation dans un Composable :
         * ```kotlin
         * val isConnected by NetworkReceiver.isNetworkAvailable.collectAsStateWithLifecycle()
         * ```
         */
        private val _isNetworkAvailable = MutableStateFlow(true)
        val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

        /**
         * Dernier événement de changement de connectivité.
         * Utile pour afficher des Snackbars ou des notifications.
         */
        private val _networkEvent = MutableStateFlow<NetworkEvent?>(null)
        val networkEvent: StateFlow<NetworkEvent?> = _networkEvent.asStateFlow()

        /**
         * Callback réseau pour l'approche moderne (Android 7.0+).
         * Doit être conservé pour pouvoir se désinscrire proprement.
         */
        private var networkCallback: ConnectivityManager.NetworkCallback? = null

        /**
         * Callback pour la synchronisation des demandes en attente.
         * Appelé automatiquement quand le réseau redevient disponible.
         */
        private var onNetworkRestoredCallback: (() -> Unit)? = null

        /**
         * Définit le callback de synchronisation.
         * Ce callback sera appelé quand le réseau redevient disponible.
         *
         * @param callback Fonction à appeler pour synchroniser les demandes en attente
         */
        fun setOnNetworkRestoredCallback(callback: () -> Unit) {
            onNetworkRestoredCallback = callback
            Log.d(TAG, "✅ Callback de synchronisation enregistré")
        }

        /**
         * Vérifie l'état actuel de la connexion réseau.
         *
         * @param context Contexte Android pour accéder aux services système
         * @return true si une connexion réseau est disponible
         */
        fun checkNetworkStatus(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

            val network = connectivityManager.activeNetwork
            if (network == null) {
                _isNetworkAvailable.value = false
                return false
            }

            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } ?: false

            _isNetworkAvailable.value = isConnected
            return isConnected
        }

        /**
         * Enregistre le NetworkCallback pour l'approche moderne.
         *
         * Cette méthode doit être appelée dans onCreate() de l'Activity principale.
         *
         * ## Pourquoi NetworkCallback ?
         * - Plus fiable que BroadcastReceiver pour la connectivité
         * - Callbacks séparés pour connexion/déconnexion
         * - Fonctionne avec toutes les versions d'Android récentes
         *
         * @param context Contexte Android
         */
        fun registerNetworkCallback(context: Context) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

            // Créer le callback s'il n'existe pas encore
            if (networkCallback == null) {
                networkCallback = object : ConnectivityManager.NetworkCallback() {

                    /**
                     * Appelé quand une connexion réseau devient disponible.
                     */
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "📶 Réseau disponible")
                        val wasOffline = !_isNetworkAvailable.value
                        _isNetworkAvailable.value = true

                        if (wasOffline) {
                            _networkEvent.value = NetworkEvent.Connected
                            Log.i(TAG, "🔄 Connexion rétablie - Déclenchement de la synchronisation")

                            // Appeler le callback de synchronisation si défini
                            onNetworkRestoredCallback?.let { callback ->
                                Log.i(TAG, "📤 Appel du callback de synchronisation")
                                callback()
                            }
                        }
                    }

                    /**
                     * Appelé quand la connexion réseau est perdue.
                     */
                    override fun onLost(network: Network) {
                        Log.d(TAG, "📵 Réseau perdu")
                        _isNetworkAvailable.value = false
                        _networkEvent.value = NetworkEvent.Disconnected
                    }

                    /**
                     * Appelé quand les capacités du réseau changent.
                     * Par exemple : passage de WiFi à 4G
                     */
                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities: NetworkCapabilities
                    ) {
                        val hasInternet = capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET
                        )
                        val isValidated = capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED
                        )

                        Log.d(TAG, "📊 Capacités réseau: internet=$hasInternet, validated=$isValidated")

                        val isConnected = hasInternet && isValidated
                        if (isConnected != _isNetworkAvailable.value) {
                            _isNetworkAvailable.value = isConnected
                            _networkEvent.value = if (isConnected) {
                                NetworkEvent.Connected
                            } else {
                                NetworkEvent.Disconnected
                            }
                        }
                    }
                }
            }

            // Créer la requête réseau
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            // Enregistrer le callback
            try {
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
                Log.i(TAG, "✅ NetworkCallback enregistré")

                // Vérifier l'état initial
                checkNetworkStatus(context)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'enregistrement du NetworkCallback", e)
            }
        }

        /**
         * Désenregistre le NetworkCallback.
         *
         * Cette méthode doit être appelée dans onDestroy() de l'Activity principale.
         *
         * @param context Contexte Android
         */
        fun unregisterNetworkCallback(context: Context) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

            networkCallback?.let { callback ->
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                    Log.i(TAG, "✅ NetworkCallback désenregistré")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur lors du désenregistrement", e)
                }
            }
            networkCallback = null
        }

        /**
         * Réinitialise l'événement réseau après traitement.
         * Appelé par l'UI après avoir affiché un Snackbar.
         */
        fun clearNetworkEvent() {
            _networkEvent.value = null
        }
    }

    /**
     * Méthode appelée quand un broadcast est reçu.
     *
     * ## Note pédagogique
     * Cette méthode est conservée pour la démonstration du pattern BroadcastReceiver
     * classique, mais en pratique, NetworkCallback est préféré pour la connectivité.
     *
     * @param context Contexte Android
     * @param intent Intent contenant les informations du broadcast
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        Log.d(TAG, "📨 Broadcast reçu: ${intent.action}")

        when (intent.action) {
            // Action legacy pour les changements de connectivité
            @Suppress("DEPRECATION")
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                val isConnected = checkNetworkStatus(context)
                Log.i(TAG, "📡 État réseau via BroadcastReceiver: ${if (isConnected) "Connecté" else "Déconnecté"}")
            }
        }
    }
}

/**
 * Événements de changement de connectivité réseau.
 *
 * Ces événements sont émis pour informer l'UI des changements
 * et déclencher les actions appropriées (synchronisation, affichage de messages).
 */
sealed class NetworkEvent {
    /**
     * La connexion réseau a été établie.
     * L'application peut maintenant synchroniser les données en attente.
     */
    data object Connected : NetworkEvent()

    /**
     * La connexion réseau a été perdue.
     * L'application passe en mode hors ligne.
     */
    data object Disconnected : NetworkEvent()
}



