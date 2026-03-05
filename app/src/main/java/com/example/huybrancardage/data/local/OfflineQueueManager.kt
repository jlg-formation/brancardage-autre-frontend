package com.example.huybrancardage.data.local

import android.content.Context
import android.util.Log
import com.example.huybrancardage.domain.model.BrancardageRequest
import com.example.huybrancardage.domain.model.Localisation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Gestionnaire de la file d'attente hors ligne pour les demandes de brancardage.
 *
 * # Objectif pédagogique
 *
 * Cette classe illustre plusieurs concepts importants :
 *
 * ## 1. Pattern Singleton
 * Une seule instance gère toutes les demandes en attente de l'application.
 * Cela garantit la cohérence des données et évite les conflits.
 *
 * ## 2. Persistance avec SharedPreferences
 * Les demandes en attente sont sauvegardées localement pour survivre
 * à un redémarrage de l'application. SharedPreferences est utilisé
 * pour sa simplicité (alternative : Room pour des cas plus complexes).
 *
 * ## 3. Synchronisation automatique
 * Quand le réseau revient (détecté par NetworkReceiver), les demandes
 * en file d'attente sont automatiquement envoyées au serveur.
 *
 * ## 4. Gestion d'état avec StateFlow
 * L'UI peut observer le nombre de demandes en attente et réagir
 * aux changements en temps réel.
 *
 * # Cas d'usage métier
 *
 * Dans un hôpital, un brancardier peut perdre la connexion WiFi
 * en se déplaçant entre les bâtiments. Grâce à cette file d'attente :
 * - Il peut continuer à créer des demandes
 * - Les demandes sont envoyées automatiquement quand il retrouve le réseau
 * - Aucune donnée n'est perdue
 *
 * @see NetworkReceiver pour la détection de connectivité
 */
class OfflineQueueManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "OfflineQueueManager"
        private const val PREFS_NAME = "offline_queue_prefs"
        private const val KEY_PENDING_REQUESTS = "pending_requests"

        @Volatile
        private var instance: OfflineQueueManager? = null

        /**
         * Obtient l'instance unique du gestionnaire de file d'attente.
         *
         * ## Pattern Singleton thread-safe
         * Double-checked locking pour garantir une seule instance
         * même en cas d'accès concurrent.
         *
         * @param context Contexte Android (utilise applicationContext)
         * @return Instance unique de OfflineQueueManager
         */
        fun getInstance(context: Context): OfflineQueueManager {
            return instance ?: synchronized(this) {
                instance ?: OfflineQueueManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ============================================================
    // StateFlow pour l'observation depuis l'UI
    // ============================================================

    private val _pendingRequests = MutableStateFlow<List<PendingBrancardageRequest>>(emptyList())

    /**
     * Liste des demandes en attente de synchronisation.
     * Observable depuis les composants Compose.
     */
    val pendingRequests: StateFlow<List<PendingBrancardageRequest>> = _pendingRequests.asStateFlow()

    /**
     * Nombre de demandes en attente.
     * Pratique pour afficher un badge dans l'UI.
     */
    val pendingCount: Int
        get() = _pendingRequests.value.size

    init {
        // Charger les demandes sauvegardées au démarrage
        loadPendingRequests()
    }

    // ============================================================
    // Gestion de la file d'attente
    // ============================================================

    /**
     * Ajoute une demande à la file d'attente hors ligne.
     *
     * Cette méthode est appelée quand l'utilisateur valide une demande
     * alors que le réseau n'est pas disponible.
     *
     * @param request La demande de brancardage sérialisable à mettre en file d'attente
     * @return L'ID temporaire de la demande en attente
     */
    fun addToQueue(request: SerializableBrancardageRequest): String {
        val pendingRequest = PendingBrancardageRequest(
            id = generateTemporaryId(),
            request = request,
            timestamp = System.currentTimeMillis(),
            status = PendingStatus.WAITING
        )

        val currentList = _pendingRequests.value.toMutableList()
        currentList.add(pendingRequest)
        _pendingRequests.value = currentList

        // Sauvegarder immédiatement
        savePendingRequests()

        Log.i(TAG, "✅ Demande ajoutée à la file d'attente: ${pendingRequest.id}")
        Log.d(TAG, "📊 Total en attente: ${_pendingRequests.value.size}")

        return pendingRequest.id
    }

    /**
     * Supprime une demande de la file d'attente.
     *
     * Appelée après une synchronisation réussie ou en cas d'annulation.
     *
     * @param requestId L'ID de la demande à supprimer
     */
    fun removeFromQueue(requestId: String) {
        val currentList = _pendingRequests.value.toMutableList()
        val removed = currentList.removeAll { it.id == requestId }

        if (removed) {
            _pendingRequests.value = currentList
            savePendingRequests()
            Log.i(TAG, "🗑️ Demande supprimée de la file: $requestId")
        }
    }

    /**
     * Met à jour le statut d'une demande en attente.
     *
     * @param requestId L'ID de la demande
     * @param status Le nouveau statut
     */
    fun updateStatus(requestId: String, status: PendingStatus) {
        val currentList = _pendingRequests.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == requestId }

        if (index != -1) {
            currentList[index] = currentList[index].copy(status = status)
            _pendingRequests.value = currentList
            savePendingRequests()
            Log.d(TAG, "📝 Statut mis à jour: $requestId → $status")
        }
    }

    /**
     * Récupère toutes les demandes en attente de synchronisation.
     *
     * @return Liste des demandes avec statut WAITING
     */
    fun getWaitingRequests(): List<PendingBrancardageRequest> {
        return _pendingRequests.value.filter { it.status == PendingStatus.WAITING }
    }

    /**
     * Vide la file d'attente.
     * Utilisé principalement pour les tests ou le reset de l'application.
     */
    fun clearQueue() {
        _pendingRequests.value = emptyList()
        savePendingRequests()
        Log.i(TAG, "🧹 File d'attente vidée")
    }

    // ============================================================
    // Persistance avec SharedPreferences
    // ============================================================

    /**
     * Sauvegarde les demandes en attente dans SharedPreferences.
     *
     * ## Note pédagogique
     * SharedPreferences stocke les données sous forme de clé-valeur.
     * Pour stocker une liste d'objets, on la sérialise en JSON.
     */
    private fun savePendingRequests() {
        try {
            val jsonString = json.encodeToString(_pendingRequests.value)
            sharedPrefs.edit()
                .putString(KEY_PENDING_REQUESTS, jsonString)
                .apply() // apply() est asynchrone, commit() est synchrone

            Log.d(TAG, "💾 File d'attente sauvegardée (${_pendingRequests.value.size} éléments)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la sauvegarde", e)
        }
    }

    /**
     * Charge les demandes en attente depuis SharedPreferences.
     */
    private fun loadPendingRequests() {
        try {
            val jsonString = sharedPrefs.getString(KEY_PENDING_REQUESTS, null)
            if (jsonString != null) {
                val requests = json.decodeFromString<List<PendingBrancardageRequest>>(jsonString)
                _pendingRequests.value = requests
                Log.i(TAG, "📂 File d'attente chargée: ${requests.size} éléments")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du chargement", e)
            _pendingRequests.value = emptyList()
        }
    }

    /**
     * Génère un ID temporaire unique pour les demandes en attente.
     */
    private fun generateTemporaryId(): String {
        return "pending_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

// ============================================================
// Modèles de données pour la file d'attente
// ============================================================

/**
 * Représente une demande de brancardage en attente de synchronisation.
 */
@Serializable
data class PendingBrancardageRequest(
    val id: String,
    val request: SerializableBrancardageRequest,
    val timestamp: Long,
    val status: PendingStatus
)

/**
 * Version sérialisable de BrancardageRequest pour le stockage JSON.
 *
 * ## Pourquoi une classe séparée ?
 * BrancardageRequest utilise des types complexes (Localisation) qui
 * peuvent nécessiter une sérialisation personnalisée. Cette classe
 * simplifie le stockage en utilisant des types primitifs.
 */
@Serializable
data class SerializableBrancardageRequest(
    val patientId: String,
    val patientName: String, // Pour l'affichage dans l'UI
    val departDescription: String,
    val departLatitude: Double?,
    val departLongitude: Double?,
    val destinationId: String,
    val destinationName: String, // Pour l'affichage dans l'UI
    val mediaIds: List<String>,
    val commentaire: String?
)

/**
 * Statut d'une demande en attente.
 */
@Serializable
enum class PendingStatus {
    /** En attente de connexion réseau */
    WAITING,
    /** Synchronisation en cours */
    SYNCING,
    /** Synchronisation réussie (sera supprimée) */
    SYNCED,
    /** Échec de la synchronisation */
    FAILED
}

/**
 * Extension pour convertir BrancardageRequest en version sérialisable.
 */
fun BrancardageRequest.toSerializable(
    patientName: String = "Patient",
    destinationName: String = "Destination"
): SerializableBrancardageRequest {
    return SerializableBrancardageRequest(
        patientId = this.patientId,
        patientName = patientName,
        departDescription = this.depart.descriptionFormattee,
        departLatitude = this.depart.latitude,
        departLongitude = this.depart.longitude,
        destinationId = this.destinationId,
        destinationName = destinationName,
        mediaIds = this.mediaIds,
        commentaire = this.commentaire
    )
}

/**
 * Extension pour convertir SerializableBrancardageRequest en BrancardageRequest.
 */
fun SerializableBrancardageRequest.toBrancardageRequest(): BrancardageRequest {
    return BrancardageRequest(
        patientId = this.patientId,
        depart = Localisation(
            latitude = this.departLatitude,
            longitude = this.departLongitude,
            description = this.departDescription
        ),
        destinationId = this.destinationId,
        mediaIds = this.mediaIds,
        commentaire = this.commentaire
    )
}


