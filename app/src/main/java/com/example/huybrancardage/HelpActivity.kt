package com.example.huybrancardage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.huybrancardage.ui.screens.HelpScreen
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme

/**
 * Activité dédiée à l'affichage de l'aide et des informations de l'application.
 *
 * Cette activité est lancée via un **Intent explicite** depuis l'écran d'accueil,
 * contrairement aux Intent implicites utilisés pour les appels téléphoniques
 * ou le partage de contenu.
 *
 * ## Objectif pédagogique
 * Démontrer l'utilisation d'un Intent explicite pour naviguer entre deux
 * activités au sein de la même application.
 *
 * ## Contenu de l'écran
 * - Informations sur l'application (version, développeur)
 * - Guide d'utilisation rapide
 * - Contacts du service de brancardage
 * - Mentions légales
 *
 * @see com.example.huybrancardage.util.IntentUtils.openHelpActivity pour l'Intent explicite
 * @see MainActivity pour l'activité principale
 */
class HelpActivity : ComponentActivity() {

    /**
     * Configure l'activité avec le thème et l'écran d'aide.
     *
     * @param savedInstanceState État sauvegardé de l'activité (si restauration)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuyBrancardageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HelpScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

