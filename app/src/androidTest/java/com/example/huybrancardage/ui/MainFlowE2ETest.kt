package com.example.huybrancardage.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.huybrancardage.ui.screens.AccueilScreen
import com.example.huybrancardage.ui.screens.RechercheManuelleScreen
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.viewmodel.SearchViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests E2E pour le parcours principal de l'application
 *
 * Ces tests vérifient les flux utilisateur complets avec
 * Compose Testing Library
 */
@RunWith(AndroidJUnit4::class)
class MainFlowE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Tests Écran d'Accueil ====================

    @Test
    fun accueilScreen_displaysTitle() {
        composeTestRule.setContent {
            HuyBrancardageTheme {
                AccueilScreen(
                    onRechercheManuelleClick = {},
                    onScanBraceletClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Nouvelle demande", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun accueilScreen_displaysSearchButton() {
        composeTestRule.setContent {
            HuyBrancardageTheme {
                AccueilScreen(
                    onRechercheManuelleClick = {},
                    onScanBraceletClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Recherche manuelle", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun accueilScreen_displaysScanButton() {
        composeTestRule.setContent {
            HuyBrancardageTheme {
                AccueilScreen(
                    onRechercheManuelleClick = {},
                    onScanBraceletClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Scanner", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun accueilScreen_searchButtonClick_triggersNavigation() {
        var navigationTriggered = false

        composeTestRule.setContent {
            HuyBrancardageTheme {
                AccueilScreen(
                    onRechercheManuelleClick = { navigationTriggered = true },
                    onScanBraceletClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Recherche manuelle", substring = true)
            .performClick()

        assert(navigationTriggered) { "Navigation to search should be triggered" }
    }

    @Test
    fun accueilScreen_scanButtonClick_triggersNavigation() {
        var navigationTriggered = false

        composeTestRule.setContent {
            HuyBrancardageTheme {
                AccueilScreen(
                    onRechercheManuelleClick = {},
                    onScanBraceletClick = { navigationTriggered = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Scanner", substring = true, ignoreCase = true)
            .performClick()

        assert(navigationTriggered) { "Navigation to scan should be triggered" }
    }

    // ==================== Tests Écran de Recherche ====================

    @Test
    fun rechercheScreen_displaysSearchFields() {
        val viewModel = SearchViewModel()

        composeTestRule.setContent {
            HuyBrancardageTheme {
                RechercheManuelleScreen(
                    viewModel = viewModel,
                    onPatientSelected = {},
                    onBackClick = {}
                )
            }
        }

        // Vérifier que les champs de recherche sont affichés
        composeTestRule
            .onNodeWithText("Nom", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Prénom", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun rechercheScreen_searchButtonDisabledWhenEmpty() {
        val viewModel = SearchViewModel()

        composeTestRule.setContent {
            HuyBrancardageTheme {
                RechercheManuelleScreen(
                    viewModel = viewModel,
                    onPatientSelected = {},
                    onBackClick = {}
                )
            }
        }

        // Le bouton rechercher devrait être désactivé sans critères
        composeTestRule
            .onNodeWithText("Rechercher", substring = true, ignoreCase = true)
            .assertIsNotEnabled()
    }

    @Test
    fun rechercheScreen_searchButtonEnabledWithInput() {
        val viewModel = SearchViewModel()

        composeTestRule.setContent {
            HuyBrancardageTheme {
                RechercheManuelleScreen(
                    viewModel = viewModel,
                    onPatientSelected = {},
                    onBackClick = {}
                )
            }
        }

        // Saisir un nom
        composeTestRule
            .onNodeWithText("Nom", substring = true)
            .performTextInput("Dupont")

        // Le bouton devrait être activé
        composeTestRule
            .onNodeWithText("Rechercher", substring = true, ignoreCase = true)
            .assertIsEnabled()
    }

    @Test
    fun rechercheScreen_backButtonClick_triggersNavigation() {
        var backTriggered = false
        val viewModel = SearchViewModel()

        composeTestRule.setContent {
            HuyBrancardageTheme {
                RechercheManuelleScreen(
                    viewModel = viewModel,
                    onPatientSelected = {},
                    onBackClick = { backTriggered = true }
                )
            }
        }

        // Cliquer sur le bouton retour
        composeTestRule
            .onNodeWithContentDescription("Retour", substring = true, ignoreCase = true)
            .performClick()

        assert(backTriggered) { "Back navigation should be triggered" }
    }
}


