package com.example.huybrancardage.domain.model

/**
 * Représente une localisation basée sur une borne WiFi.
 *
 * Dans un environnement hospitalier, le GPS ne fonctionne pas à l'intérieur des bâtiments.
 * Chaque borne WiFi de l'hôpital est mappée à une position physique connue (bâtiment, étage, zone).
 * En scannant les réseaux WiFi disponibles et en identifiant la borne avec le meilleur signal,
 * on peut déterminer approximativement la position du brancardier.
 *
 * @param bssid Adresse MAC unique de la borne WiFi (ex: "AA:BB:CC:DD:EE:FF")
 * @param ssid Nom du réseau WiFi (ex: "HOPITAL-HUY-2B")
 * @param batiment Nom du bâtiment où se trouve la borne (ex: "Bâtiment B")
 * @param etage Étage où se trouve la borne (ex: "2ème étage")
 * @param zone Zone/secteur précis (ex: "Couloir radiologie")
 * @param signalStrength Force du signal en dBm (plus proche de 0 = meilleur signal)
 */
data class WifiLocation(
    val bssid: String,
    val ssid: String,
    val batiment: String,
    val etage: String,
    val zone: String,
    val signalStrength: Int = 0
) {
    /**
     * Description formatée de la localisation pour affichage.
     */
    val descriptionFormattee: String
        get() = "$batiment - $etage ($zone)"

    /**
     * Description courte pour les notifications.
     */
    val descriptionCourte: String
        get() = "$batiment - $etage"
}

