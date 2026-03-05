package com.example.huybrancardage.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huybrancardage.data.local.OfflineQueueManager
import com.example.huybrancardage.data.local.PendingBrancardageRequest
import com.example.huybrancardage.data.local.PendingStatus
import com.example.huybrancardage.data.local.toBrancardageRequest
import com.example.huybrancardage.data.remote.NetworkResult
import com.example.huybrancardage.data.repository.BrancardageRepository
import com.example.huybrancardage.receiver.NetworkEvent
import com.example.huybrancardage.receiver.NetworkReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * État de la synchronisation des demandes hors ligne.
 */
sealed class SyncState {
    /** Aucune synchronisation en cours */
    data object Idle : SyncState()

    /** Synchronisation en cours */
    data class Syncing(val current: Int, val total: Int) : SyncState()

    /** Synchronisation terminée avec succès */
    data class Success(val syncedCount: Int) : SyncState()

    /** Échec de la synchronisation */
    data class Error(val message: String, val failedCount: Int) : SyncState()
}

private const val TAG = "SyncViewModel"

/**
 * ViewModel pour la gestion de la connectivité réseau et la synchronisation.
 *
 * # Objectif pédagogique
 *
 * Ce ViewModel illustre plusieurs concepts importants :
 *
 * ## 1. Observation des changements réseau
 * Le ViewModel observe les événements de NetworkReceiver et réagit
 * automatiquement aux changements de connectivité.
 *
 * ## 2. Synchronisation automatique
 * Quand le réseau revient, les demandes en file d'attente sont
 * automatiquement synchronisées avec le serveur.
 *
 * ## 3. Gestion d'état complexe
 * Plusieurs états (connectivité, synchronisation, file d'attente)
 * sont combinés pour une UI réactive.
 *
 * ## 4. Coroutines et Flow
 * Utilisation de `viewModelScope` pour les opérations asynchrones
 * et de `StateFlow` pour l'observation depuis l'UI.
 *
 * @param brancardageRepository Repository pour l'envoi des demandes
 */
class SyncViewModel(
    private val brancardageRepository: BrancardageRepository = BrancardageRepository.getInstance()
) : ViewModel() {

    // ============================================================
    // États observables
    // ============================================================

    /**
     * État actuel de la connexion réseau.
     * Proxy vers NetworkReceiver.isNetworkAvailable pour faciliter l'accès.
     */
    val isNetworkAvailable: StateFlow<Boolean> = NetworkReceiver.isNetworkAvailable

    /**
     * Événement réseau actuel (connexion/déconnexion).
     * Utilisé pour afficher des Snackbars.
     */
    val networkEvent: StateFlow<NetworkEvent?> = NetworkReceiver.networkEvent

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    /**
     * État de la synchronisation en cours.
     */
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow<String?>(null)
    /**
     * Dernier message de synchronisation à afficher.
     */
    val lastSyncMessage: StateFlow<String?> = _lastSyncMessage.asStateFlow()

    // Référence au gestionnaire de file d'attente (initialisée par initialize())
    private var offlineQueueManager: OfflineQueueManager? = null

    /**
     * Liste des demandes en attente.
     * Null tant que le manager n'est pas initialisé.
     */
    val pendingRequests: StateFlow<List<PendingBrancardageRequest>>?
        get() = offlineQueueManager?.pendingRequests

    /**
     * Nombre de demandes en attente.
     */
    val pendingCount: Int
        get() = offlineQueueManager?.pendingCount ?: 0

    init {
        // Observer les événements réseau pour déclencher la synchronisation
        observeNetworkEvents()
    }

    // ============================================================
    // Initialisation
    // ============================================================

    /**
     * Initialise le ViewModel avec le contexte Android.
     *
     * Cette méthode doit être appelée depuis l'Activity ou un Composable
     * car le ViewModel n'a pas accès au Context par défaut.
     *
     * @param context Contexte Android
     */
    fun initialize(context: Context) {
        if (offlineQueueManager == null) {
            offlineQueueManager = OfflineQueueManager.getInstance(context)
            Log.d(TAG, "✅ SyncViewModel initialisé")
        }
    }

    // ============================================================
    // Observation des événements réseau
    // ============================================================

    /**
     * Observe les événements de changement de connectivité.
     *
     * Quand le réseau redevient disponible, déclenche automatiquement
     * la synchronisation des demandes en attente.
     */
    private fun observeNetworkEvents() {
        viewModelScope.launch {
            NetworkReceiver.networkEvent.collect { event ->
                when (event) {
                    is NetworkEvent.Connected -> {
                        Log.i(TAG, "📶 Réseau connecté - Vérification des demandes en attente")
                        // Petit délai pour s'assurer que la connexion est stable
                        delay(1000)
                        syncPendingRequests()
                    }
                    is NetworkEvent.Disconnected -> {
                        Log.i(TAG, "📵 Réseau déconnecté - Mode hors ligne activé")
                        _lastSyncMessage.value = "Mode hors ligne activé"
                    }
                    null -> { /* Pas d'événement */ }
                }
            }
        }
    }

    // ============================================================
    // Synchronisation
    // ============================================================

    /**
     * Synchronise toutes les demandes en attente avec le serveur.
     *
     * Cette méthode :
     * 1. Récupère les demandes en attente
     * 2. Les envoie une par une au serveur
     * 3. Met à jour leur statut
     * 4. Les supprime de la file si envoyées avec succès
     */
    fun syncPendingRequests() {
        val manager = offlineQueueManager ?: return

        viewModelScope.launch {
            val waitingRequests = manager.getWaitingRequests()

            if (waitingRequests.isEmpty()) {
                Log.d(TAG, "📭 Aucune demande en attente à synchroniser")
                return@launch
            }

            Log.i(TAG, "🔄 Début de la synchronisation de ${waitingRequests.size} demande(s)")
            _syncState.value = SyncState.Syncing(0, waitingRequests.size)

            var syncedCount = 0
            var failedCount = 0

            waitingRequests.forEachIndexed { index, pendingRequest ->
                _syncState.value = SyncState.Syncing(index + 1, waitingRequests.size)

                // Marquer comme en cours de synchronisation
                manager.updateStatus(pendingRequest.id, PendingStatus.SYNCING)

                try {
                    // Convertir et envoyer la demande
                    val request = pendingRequest.request.toBrancardageRequest()
                    val result = brancardageRepository.createBrancardage(request)

                    when (result) {
                        is NetworkResult.Success -> {
                            Log.i(TAG, "✅ Demande ${pendingRequest.id} synchronisée")
                            manager.removeFromQueue(pendingRequest.id)
                            syncedCount++
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "❌ Échec de synchronisation: ${result.message}")
                            manager.updateStatus(pendingRequest.id, PendingStatus.FAILED)
                            failedCount++
                        }
                        is NetworkResult.Loading -> { /* Ignore */ }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception lors de la synchronisation", e)
                    manager.updateStatus(pendingRequest.id, PendingStatus.FAILED)
                    failedCount++
                }

                // Petit délai entre les requêtes pour ne pas surcharger le serveur
                delay(500)
            }

            // Résultat final
            _syncState.value = if (failedCount == 0) {
                SyncState.Success(syncedCount)
            } else {
                SyncState.Error(
                    "Certaines demandes n'ont pas pu être synchronisées",
                    failedCount
                )
            }

            // Message pour l'UI
            _lastSyncMessage.value = when {
                syncedCount > 0 && failedCount == 0 ->
                    "$syncedCount demande${if (syncedCount > 1) "s" else ""} synchronisée${if (syncedCount > 1) "s" else ""} avec succès"
                syncedCount > 0 && failedCount > 0 ->
                    "$syncedCount réussie${if (syncedCount > 1) "s" else ""}, $failedCount échouée${if (failedCount > 1) "s" else ""}"
                else ->
                    "Échec de la synchronisation"
            }

            Log.i(TAG, "📊 Synchronisation terminée: $syncedCount réussies, $failedCount échouées")

            // Reset l'état après un délai
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }

    /**
     * Réinitialise le message de synchronisation.
     * Appelé par l'UI après avoir affiché le Snackbar.
     */
    fun clearSyncMessage() {
        _lastSyncMessage.value = null
    }

    /**
     * Réinitialise l'événement réseau.
     * Appelé par l'UI après avoir traité l'événement.
     */
    fun clearNetworkEvent() {
        NetworkReceiver.clearNetworkEvent()
    }

    /**
     * Force une synchronisation manuelle.
     * Utile si l'utilisateur veut réessayer après un échec.
     */
    fun retrySyncFailed() {
        val manager = offlineQueueManager ?: return

        viewModelScope.launch {
            // Remettre les demandes échouées en attente
            manager.pendingRequests.value
                .filter { it.status == PendingStatus.FAILED }
                .forEach { manager.updateStatus(it.id, PendingStatus.WAITING) }

            // Relancer la synchronisation
            syncPendingRequests()
        }
    }
}

