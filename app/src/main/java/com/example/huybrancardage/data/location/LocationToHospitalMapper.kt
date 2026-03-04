package com.example.huybrancardage.data.location

import com.example.huybrancardage.domain.model.Localisation

/**
 * Convertisseur de coordonnées GPS en localisation hospitalière
 *
 * Dans un vrai projet, cette classe interrogerait une API ou une base de données
 * pour convertir les coordonnées en informations de bâtiment.
 *
 * Pour ce projet pédagogique, nous utilisons une simulation basée sur la proximité
 * avec des coordonnées prédéfinies pour chaque bâtiment.
 */
object LocationToHospitalMapper {

    /**
     * Zone hospitalière avec ses coordonnées et informations
     */
    private data class HospitalZone(
        val name: String,
        val batiment: String,
        val service: String,
        val etage: Int,
        val chambre: String?,
        val latitude: Double,
        val longitude: Double
    )

    /**
     * Zones hospitalières mockées
     * Ces coordonnées sont fictives et représentent différents bâtiments d'un hôpital
     */
    private val hospitalZones = listOf(
        HospitalZone(
            name = "Bâtiment A - Cardiologie",
            batiment = "A",
            service = "Cardiologie",
            etage = 2,
            chambre = "204",
            latitude = 48.8566,
            longitude = 2.3522
        ),
        HospitalZone(
            name = "Bâtiment B - Urgences",
            batiment = "B",
            service = "Urgences",
            etage = 0,
            chambre = null,
            latitude = 48.8570,
            longitude = 2.3525
        ),
        HospitalZone(
            name = "Bâtiment C - Radiologie",
            batiment = "C",
            service = "Radiologie",
            etage = 1,
            chambre = null,
            latitude = 48.8560,
            longitude = 2.3520
        ),
        HospitalZone(
            name = "Bâtiment D - Chirurgie",
            batiment = "D",
            service = "Chirurgie",
            etage = 3,
            chambre = "312",
            latitude = 48.8565,
            longitude = 2.3530
        ),
        HospitalZone(
            name = "Bâtiment E - Pédiatrie",
            batiment = "E",
            service = "Pédiatrie",
            etage = 1,
            chambre = "105",
            latitude = 48.8575,
            longitude = 2.3515
        )
    )

    /**
     * Convertit des coordonnées GPS en Localisation hospitalière
     *
     * @param latitude Latitude GPS
     * @param longitude Longitude GPS
     * @return Localisation avec les informations du bâtiment le plus proche
     */
    fun mapToLocalisation(latitude: Double, longitude: Double): Localisation {
        // Trouver la zone la plus proche
        val nearestZone = findNearestZone(latitude, longitude)

        return if (nearestZone != null) {
            Localisation(
                latitude = latitude,
                longitude = longitude,
                description = nearestZone.name,
                batiment = nearestZone.batiment,
                etage = nearestZone.etage,
                chambre = nearestZone.chambre
            )
        } else {
            // Zone inconnue - retourner juste les coordonnées
            Localisation(
                latitude = latitude,
                longitude = longitude,
                description = "Position GPS",
                batiment = null,
                etage = null,
                chambre = null
            )
        }
    }

    /**
     * Trouve la zone hospitalière la plus proche des coordonnées données
     *
     * @param latitude Latitude GPS
     * @param longitude Longitude GPS
     * @return La zone la plus proche ou null si aucune n'est à portée
     */
    private fun findNearestZone(latitude: Double, longitude: Double): HospitalZone? {
        // Calcul de la distance (approximation simple, suffisante pour notre cas)
        // Dans un vrai projet, on utiliserait la formule de Haversine
        return hospitalZones.minByOrNull { zone ->
            val latDiff = latitude - zone.latitude
            val lonDiff = longitude - zone.longitude
            latDiff * latDiff + lonDiff * lonDiff
        }
    }

    /**
     * Localisation par défaut (pour les cas où le GPS n'est pas disponible)
     */
    fun getDefaultLocalisation(): Localisation {
        return Localisation(
            latitude = 48.8566,
            longitude = 2.3522,
            description = "Bâtiment A - Cardiologie",
            batiment = "A",
            etage = 2,
            chambre = "204"
        )
    }
}

