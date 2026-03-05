package com.example.huybrancardage.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.huybrancardage.domain.model.WifiLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Provider de localisation indoor basé sur les bornes WiFi.
 *
 * # Principe de fonctionnement
 *
 * Dans un hôpital, le GPS ne fonctionne pas à l'intérieur des bâtiments.
 * Cette classe utilise une approche alternative : la géolocalisation par WiFi.
 *
 * Chaque borne WiFi de l'hôpital est enregistrée dans une base de données avec sa position
 * physique (bâtiment, étage, zone). En scannant les réseaux WiFi disponibles et en
 * identifiant la borne connue avec le signal le plus fort, on peut déterminer
 * approximativement où se trouve le brancardier.
 *
 * # Utilisation pédagogique
 *
 * Cette classe illustre plusieurs concepts Android :
 * - Accès aux services système (WifiManager)
 * - Gestion des permissions
 * - Opérations en arrière-plan avec Coroutines
 * - Pattern Provider/Repository
 *
 * @param context Context Android pour accéder au WifiManager
 */
class WifiLocationProvider(private val context: Context) {

    companion object {
        private const val TAG = "WifiLocationProvider"
    }

    /**
     * Instance du WifiManager pour scanner les réseaux.
     * Utilise applicationContext pour éviter les fuites de mémoire.
     */
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    /**
     * Base de données simulée des bornes WiFi de l'hôpital.
     *
     * En production, cette liste viendrait du serveur backend via une API.
     * Ici, on simule plusieurs bornes réparties dans l'hôpital.
     *
     * La clé est le BSSID (adresse MAC) de la borne, qui est unique.
     */
    private val hospitalWifiMap = mapOf(
        // Bâtiment A - Rez-de-chaussée
        "AA:BB:CC:DD:EE:01" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:01",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment A",
            etage = "Rez-de-chaussée",
            zone = "Accueil principal"
        ),
        "AA:BB:CC:DD:EE:02" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:02",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment A",
            etage = "Rez-de-chaussée",
            zone = "Salle d'attente"
        ),

        // Bâtiment A - 1er étage
        "AA:BB:CC:DD:EE:03" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:03",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment A",
            etage = "1er étage",
            zone = "Cardiologie"
        ),
        "AA:BB:CC:DD:EE:04" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:04",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment A",
            etage = "1er étage",
            zone = "Pneumologie"
        ),

        // Bâtiment B - 2ème étage
        "AA:BB:CC:DD:EE:05" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:05",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment B",
            etage = "2ème étage",
            zone = "Radiologie"
        ),
        "AA:BB:CC:DD:EE:06" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:06",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment B",
            etage = "2ème étage",
            zone = "Scanner/IRM"
        ),

        // Bâtiment B - Sous-sol
        "AA:BB:CC:DD:EE:07" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:07",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment B",
            etage = "Sous-sol",
            zone = "Bloc opératoire"
        ),
        "AA:BB:CC:DD:EE:08" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:08",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment B",
            etage = "Sous-sol",
            zone = "Salle de réveil"
        ),

        // Bâtiment C - Rez-de-chaussée
        "AA:BB:CC:DD:EE:09" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:09",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment C",
            etage = "Rez-de-chaussée",
            zone = "Urgences"
        ),
        "AA:BB:CC:DD:EE:10" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:10",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment C",
            etage = "Rez-de-chaussée",
            zone = "Box de soins"
        ),

        // Bâtiment C - 1er étage
        "AA:BB:CC:DD:EE:11" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:11",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment C",
            etage = "1er étage",
            zone = "Maternité"
        ),
        "AA:BB:CC:DD:EE:12" to WifiLocation(
            bssid = "AA:BB:CC:DD:EE:12",
            ssid = "HOPITAL-HUY",
            batiment = "Bâtiment C",
            etage = "1er étage",
            zone = "Pédiatrie"
        )
    )

    /**
     * Scanne les réseaux WiFi et retourne la localisation la plus probable.
     *
     * # Algorithme
     * 1. Déclenche un scan WiFi via le WifiManager
     * 2. Récupère les résultats du scan (liste de réseaux détectés)
     * 3. Filtre pour ne garder que les bornes connues de l'hôpital
     * 4. Retourne celle avec le signal le plus fort (la plus proche physiquement)
     *
     * # Notes techniques
     * - Le scan WiFi nécessite la permission ACCESS_FINE_LOCATION sur Android 6+
     * - startScan() est deprecated depuis Android 9, mais fonctionne encore
     * - Les résultats peuvent être mis en cache par le système (max 4 scans/2 min)
     *
     * @return WifiLocation si une borne connue est détectée, null sinon
     */
    @Suppress("DEPRECATION")
    suspend fun getCurrentLocation(): WifiLocation? = withContext(Dispatchers.IO) {
        // Vérifier les permissions
        if (!hasLocationPermission()) {
            Log.w(TAG, "Permission de localisation non accordée")
            return@withContext null
        }

        try {
            // Déclenche un scan WiFi (peut être limité par le système)
            val scanStarted = wifiManager.startScan()
            if (!scanStarted) {
                Log.w(TAG, "Le scan WiFi n'a pas pu démarrer (throttling système)")
            }

            // Récupère les résultats du dernier scan
            val scanResults = wifiManager.scanResults

            Log.d(TAG, "Scan WiFi: ${scanResults.size} réseaux détectés")

            // Trouve la borne de l'hôpital avec le meilleur signal
            val bestMatch = scanResults
                .filter { result ->
                    // Filtre les bornes connues de l'hôpital
                    result.BSSID.uppercase() in hospitalWifiMap.keys.map { it.uppercase() }
                }
                .maxByOrNull { result ->
                    // Prend celle avec le meilleur signal (level le plus proche de 0)
                    result.level
                }

            if (bestMatch != null) {
                val location = hospitalWifiMap[bestMatch.BSSID.uppercase()]?.copy(
                    signalStrength = bestMatch.level
                )
                Log.d(TAG, "Localisation trouvée: ${location?.descriptionFormattee} (signal: ${bestMatch.level} dBm)")
                location
            } else {
                Log.d(TAG, "Aucune borne de l'hôpital détectée")
                null
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Erreur de permission lors du scan WiFi", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du scan WiFi", e)
            null
        }
    }

    /**
     * Vérifie si la permission de localisation est accordée.
     *
     * Le scan WiFi nécessite ACCESS_FINE_LOCATION depuis Android 6.0 (API 23).
     * C'est une exigence de Google pour des raisons de vie privée,
     * car les réseaux WiFi peuvent révéler la position de l'utilisateur.
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Version simulée pour les tests et démonstrations.
     *
     * Retourne une localisation aléatoire parmi les bornes de l'hôpital.
     * Utile quand :
     * - On teste sur un émulateur (pas de vrai WiFi)
     * - On fait une démo sans être dans l'hôpital
     * - Le scan WiFi échoue
     *
     * @return Une WifiLocation aléatoire avec un signal simulé
     */
    fun getSimulatedLocation(): WifiLocation {
        val location = hospitalWifiMap.values.random()
        // Simule un signal entre -30 (excellent) et -80 (faible)
        val simulatedSignal = (-80..-30).random()
        return location.copy(signalStrength = simulatedSignal)
    }

    /**
     * Retourne toutes les localisations connues de l'hôpital.
     * Utile pour afficher une carte ou une liste des zones.
     */
    fun getAllKnownLocations(): List<WifiLocation> {
        return hospitalWifiMap.values.toList()
    }

    /**
     * Vérifie si le WiFi est activé sur l'appareil.
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }
}



