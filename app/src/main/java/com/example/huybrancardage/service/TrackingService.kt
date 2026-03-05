package com.example.huybrancardage.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.huybrancardage.MainActivity
import com.example.huybrancardage.R
import com.example.huybrancardage.data.location.WifiLocationProvider
import com.example.huybrancardage.domain.model.WifiLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service de suivi en temps réel du brancardier pendant un transport.
 *
 * # Objectif pédagogique
 *
 * Ce service illustre plusieurs concepts importants du développement Android :
 *
 * ## 1. Foreground Service
 * Un service "foreground" est un service qui effectue une opération visible par l'utilisateur.
 * Android exige qu'il affiche une notification persistante pour informer l'utilisateur
 * qu'une tâche tourne en arrière-plan.
 *
 * ## 2. Cycle de vie d'un Service
 * - `onCreate()` : Appelé une seule fois à la création
 * - `onStartCommand()` : Appelé à chaque démarrage via startService()
 * - `onDestroy()` : Appelé quand le service est arrêté
 *
 * ## 3. Communication Service ↔ UI
 * On utilise des StateFlow statiques (dans le companion object) pour permettre
 * à l'UI d'observer l'état du service. C'est un pattern simplifié pour la démo.
 * En production, on utiliserait plutôt un bound service ou un EventBus.
 *
 * ## 4. Coroutines dans un Service
 * Le service utilise un CoroutineScope personnalisé avec SupervisorJob
 * pour gérer les tâches asynchrones (scan WiFi périodique).
 *
 * # Fonctionnement
 *
 * 1. Le brancardier démarre le transport → le service est lancé
 * 2. Toutes les 10 secondes, le service :
 *    - Scanne les bornes WiFi
 *    - Identifie la plus proche
 *    - Met à jour la notification
 *    - Envoie la position au serveur (simulé)
 * 3. Quand le transport est terminé → le service est arrêté
 */
class TrackingService : Service() {

    companion object {
        private const val TAG = "TrackingService"

        // Identifiants pour la notification
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        // Intervalle entre chaque scan WiFi (10 secondes)
        private const val SCAN_INTERVAL_MS = 10_000L

        // Clés pour les extras de l'Intent
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_BRANCARDAGE_ID = "brancardage_id"

        // ============================================================
        // StateFlow statiques pour la communication Service ↔ UI
        // ============================================================

        /**
         * Position actuelle du brancardier.
         * Observable depuis n'importe quel écran Compose.
         */
        private val _currentLocation = MutableStateFlow<WifiLocation?>(null)
        val currentLocation: StateFlow<WifiLocation?> = _currentLocation.asStateFlow()

        /**
         * Indique si le tracking est en cours.
         */
        private val _isTracking = MutableStateFlow(false)
        val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

        /**
         * Nom du patient en cours de transport.
         */
        private val _currentPatientName = MutableStateFlow<String?>(null)
        val currentPatientName: StateFlow<String?> = _currentPatientName.asStateFlow()

        // ============================================================
        // Méthodes helper pour démarrer/arrêter le service
        // ============================================================

        /**
         * Démarre le service de tracking.
         *
         * @param context Context Android
         * @param patientName Nom du patient transporté (affiché dans la notification)
         * @param brancardageId ID de la demande de brancardage
         */
        fun start(context: Context, patientName: String, brancardageId: String) {
            Log.d(TAG, "Demande de démarrage du service pour: $patientName")

            val intent = Intent(context, TrackingService::class.java).apply {
                putExtra(EXTRA_PATIENT_NAME, patientName)
                putExtra(EXTRA_BRANCARDAGE_ID, brancardageId)
            }

            // Sur Android 8+, on doit utiliser startForegroundService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Arrête le service de tracking.
         */
        fun stop(context: Context) {
            Log.d(TAG, "Demande d'arrêt du service")
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }

    // Provider pour la géolocalisation WiFi
    private lateinit var wifiLocationProvider: WifiLocationProvider

    // Job pour la boucle de tracking (permet de l'annuler)
    private var trackingJob: Job? = null

    // Scope de coroutines pour le service
    // SupervisorJob permet aux coroutines enfants d'échouer indépendamment
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Informations sur le transport en cours
    private var patientName: String = "Patient"
    private var brancardageId: String = ""

    // ============================================================
    // Cycle de vie du Service
    // ============================================================

    /**
     * Appelé une seule fois quand le service est créé.
     * C'est ici qu'on initialise les ressources.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() - Service créé")

        // Initialise le provider WiFi
        wifiLocationProvider = WifiLocationProvider(this)

        // Crée le canal de notification (requis Android 8+)
        createNotificationChannel()
    }

    /**
     * Appelé à chaque fois que le service est démarré via startService().
     *
     * @param intent Intent contenant les extras (nom patient, ID brancardage)
     * @param flags Flags additionnels
     * @param startId ID unique de ce démarrage
     * @return Mode de redémarrage du service si tué par le système
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() - Service démarré")

        // Récupère les informations du transport depuis l'Intent
        patientName = intent?.getStringExtra(EXTRA_PATIENT_NAME) ?: "Patient"
        brancardageId = intent?.getStringExtra(EXTRA_BRANCARDAGE_ID) ?: ""

        Log.d(TAG, "Transport: Patient=$patientName, ID=$brancardageId")

        // Passe en mode foreground avec une notification persistante
        // IMPORTANT: Doit être appelé dans les 5 secondes après startForegroundService()
        startForeground(NOTIFICATION_ID, createNotification("Démarrage du suivi..."))

        // Lance la boucle de tracking
        startTracking()

        // START_STICKY: Le système redémarrera le service s'il est tué
        // Alternatives: START_NOT_STICKY, START_REDELIVER_INTENT
        return START_STICKY
    }

    /**
     * Appelé quand un client veut se lier au service (bindService).
     * On retourne null car on n'utilise pas le binding dans cet exemple.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Appelé quand le service est détruit.
     * C'est ici qu'on libère les ressources.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() - Service détruit")

        // Arrête le tracking
        stopTracking()

        // Réinitialise les StateFlow
        _isTracking.value = false
        _currentLocation.value = null
        _currentPatientName.value = null
    }

    // ============================================================
    // Logique de tracking
    // ============================================================

    /**
     * Démarre la boucle de scan WiFi périodique.
     */
    private fun startTracking() {
        Log.d(TAG, "Démarrage du tracking")

        // Met à jour les StateFlow
        _isTracking.value = true
        _currentPatientName.value = patientName

        // Lance une coroutine qui s'exécute en boucle
        trackingJob = serviceScope.launch {
            while (isActive) {  // isActive devient false quand le job est annulé

                // Scan la position actuelle
                val location = try {
                    // Essaie d'abord le vrai scan WiFi
                    wifiLocationProvider.getCurrentLocation()
                        // Si aucune borne trouvée, utilise une simulation
                        ?: wifiLocationProvider.getSimulatedLocation()
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors du scan WiFi", e)
                    // En cas d'erreur, utilise une simulation
                    wifiLocationProvider.getSimulatedLocation()
                }

                Log.d(TAG, "Position: ${location.descriptionFormattee}")

                // Met à jour le StateFlow (observable par l'UI)
                _currentLocation.value = location

                // Met à jour la notification
                updateNotification(location)

                // Simule l'envoi au serveur
                sendLocationToServer(location)

                // Attend avant le prochain scan
                delay(SCAN_INTERVAL_MS)
            }
        }
    }

    /**
     * Arrête la boucle de tracking.
     */
    private fun stopTracking() {
        Log.d(TAG, "Arrêt du tracking")
        trackingJob?.cancel()
        trackingJob = null
    }

    // ============================================================
    // Gestion des notifications
    // ============================================================

    /**
     * Crée le canal de notification.
     *
     * Requis depuis Android 8.0 (API 26). Chaque notification doit être
     * associée à un canal qui définit son importance et son comportement.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Suivi de transport",  // Nom visible dans les paramètres
                NotificationManager.IMPORTANCE_LOW  // Pas de son, juste l'icône
            ).apply {
                description = "Affiche la position du brancardier pendant un transport"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            Log.d(TAG, "Canal de notification créé")
        }
    }

    /**
     * Crée la notification persistante du foreground service.
     *
     * @param locationText Texte à afficher (position actuelle)
     */
    private fun createNotification(locationText: String): Notification {
        // PendingIntent pour ouvrir l'app quand on clique sur la notification
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🏥 Transport: $patientName")
            .setContentText(locationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)  // Non dismissable par l'utilisateur
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Met à jour la notification avec la nouvelle position.
     */
    private fun updateNotification(location: WifiLocation) {
        val locationText = "📍 ${location.batiment} - ${location.etage} (${location.zone})"
        val notification = createNotification(locationText)

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    // ============================================================
    // Communication avec le serveur (simulé)
    // ============================================================

    /**
     * Envoie la position au serveur.
     *
     * Dans une vraie application, ce serait un appel Retrofit :
     * ```
     * apiService.updateBrancardierLocation(
     *     brancardageId = brancardageId,
     *     location = LocationUpdateDto(
     *         batiment = location.batiment,
     *         etage = location.etage,
     *         zone = location.zone,
     *         timestamp = System.currentTimeMillis()
     *     )
     * )
     * ```
     */
    private fun sendLocationToServer(location: WifiLocation) {
        // Simulation : on log juste l'envoi
        Log.d(
            TAG,
            "📡 Envoi position au serveur: " +
            "brancardageId=$brancardageId, " +
            "position=${location.batiment}/${location.etage}/${location.zone}"
        )

        // TODO: Implémenter l'appel API réel
        // repository.updateBrancardierLocation(brancardageId, location)
    }
}

