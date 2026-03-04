package com.example.huybrancardage.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service de géolocalisation utilisant Fused Location Provider
 *
 * Encapsule l'accès au GPS et fournit des méthodes pour :
 * - Obtenir la dernière position connue
 * - Obtenir la position actuelle (une seule fois)
 * - Suivre les mises à jour de position en continu
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Vérifie si les permissions de localisation sont accordées
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Récupère la dernière position connue (peut être nulle)
     *
     * @return La dernière position connue ou null si non disponible
     * @throws SecurityException si les permissions ne sont pas accordées
     */
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            throw SecurityException("Permission de localisation non accordée")
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    /**
     * Récupère la position actuelle avec une haute précision
     *
     * Cette méthode force une nouvelle récupération de position,
     * contrairement à getLastKnownLocation qui peut retourner une position en cache.
     *
     * @param timeoutMillis Timeout en millisecondes (défaut: 10 secondes)
     * @return La position actuelle
     * @throws SecurityException si les permissions ne sont pas accordées
     * @throws LocationTimeoutException si le timeout est atteint
     * @throws Exception si la récupération échoue
     */
    suspend fun getCurrentLocation(timeoutMillis: Long = LOCATION_TIMEOUT_MS): Location {
        if (!hasLocationPermission()) {
            throw SecurityException("Permission de localisation non accordée")
        }

        return try {
            withTimeout(timeoutMillis) {
                suspendCancellableCoroutine { continuation ->
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000L // Intervalle de 1 seconde
                    )
                        .setWaitForAccurateLocation(false) // Ne pas attendre une position précise
                        .setMinUpdateIntervalMillis(500L)
                        .setMaxUpdates(1) // Une seule mise à jour
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                fusedLocationClient.removeLocationUpdates(this)
                                if (continuation.isActive) {
                                    continuation.resume(location)
                                }
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    ).addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }

                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw LocationTimeoutException("Impossible d'obtenir la position GPS dans le délai imparti (${timeoutMillis / 1000}s)")
        }
    }

    /**
     * Observe les mises à jour de position en continu
     *
     * @param intervalMillis Intervalle entre les mises à jour en millisecondes
     * @return Flow émettant les nouvelles positions
     */
    fun observeLocationUpdates(intervalMillis: Long = 5000L): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Permission de localisation non accordée"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        )
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { exception ->
            close(exception)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    companion object {
        /**
         * Timeout pour la récupération de la position GPS (10 secondes)
         */
        const val LOCATION_TIMEOUT_MS = 10_000L

        /**
         * Permissions requises pour la localisation
         */
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}

/**
 * Exception levée lorsque le timeout de localisation est atteint
 */
class LocationTimeoutException(message: String) : Exception(message)

