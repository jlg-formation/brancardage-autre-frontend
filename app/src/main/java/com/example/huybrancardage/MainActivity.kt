package com.example.huybrancardage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.huybrancardage.navigation.BrancardageNavGraph
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
 * @see BrancardageNavGraph pour le graphe de navigation complet
 */
class MainActivity : ComponentActivity() {

    /**
     * Configure l'activité avec le thème et le graphe de navigation.
     *
     * @param savedInstanceState État sauvegardé de l'activité (si restauration)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuyBrancardageTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrancardageNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
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