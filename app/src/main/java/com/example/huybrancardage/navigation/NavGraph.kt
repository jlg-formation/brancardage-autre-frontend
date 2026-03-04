package com.example.huybrancardage.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.huybrancardage.ui.screens.AccueilScreen
import com.example.huybrancardage.ui.screens.ConfirmationScreen
import com.example.huybrancardage.ui.screens.DestinationScreen
import com.example.huybrancardage.ui.screens.DossierPatientScreen
import com.example.huybrancardage.ui.screens.LocalisationScreen
import com.example.huybrancardage.ui.screens.MediasScreen
import com.example.huybrancardage.ui.screens.RecapitulatifScreen
import com.example.huybrancardage.ui.screens.RechercheManuelleScreen
import com.example.huybrancardage.ui.screens.ScanBraceletScreen
import com.example.huybrancardage.ui.viewmodel.MediaViewModel
import com.example.huybrancardage.ui.viewmodel.PatientViewModel

/**
 * NavGraph principal de l'application
 *
 * Parcours utilisateur :
 * - Accueil → Recherche manuelle OU Scan bracelet
 * - Résultats recherche → Dossier patient
 * - Dossier patient → Médias → Localisation → Destination → Récapitulatif → Confirmation
 * - Retour à l'accueil depuis confirmation
 */
@Composable
fun BrancardageNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.Accueil.route
) {
    // ViewModel partagé pour le patient sélectionné
    val patientViewModel: PatientViewModel = viewModel()

    // ViewModel partagé pour les médias
    val mediaViewModel: MediaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ==================== ACCUEIL ====================
        composable(Route.Accueil.route) {
            AccueilScreen(
                onScanBraceletClick = {
                    navController.navigate(Route.ScanBracelet.route)
                },
                onRechercheManuelleClick = {
                    navController.navigate(Route.RechercheManuelle.route)
                }
            )
        }

        // ==================== RECHERCHE PATIENT ====================
        composable(Route.RechercheManuelle.route) {
            RechercheManuelleScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPatientSelected = { patient ->
                    // Stocker le patient sélectionné dans le ViewModel partagé
                    patientViewModel.setPatient(patient)
                    // Naviguer vers le dossier patient
                    navController.navigate(Route.DossierPatient.route)
                }
            )
        }

        composable(Route.ScanBracelet.route) {
            ScanBraceletScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onScanSuccess = { patient ->
                    // Stocker le patient scanné dans le ViewModel partagé
                    patientViewModel.setPatient(patient)
                    // Naviguer vers le dossier patient
                    navController.navigate(Route.DossierPatient.route)
                }
            )
        }

        // ==================== DOSSIER PATIENT ====================
        composable(Route.DossierPatient.route) {
            DossierPatientScreen(
                patientViewModel = patientViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateRequestClick = {
                    // Commencer le parcours de création de demande
                    navController.navigate(Route.Medias.route)
                }
            )
        }

        // ==================== CRÉATION DE DEMANDE ====================
        composable(Route.Medias.route) {
            MediasScreen(
                viewModel = mediaViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSkipClick = {
                    // Sauter l'étape médias
                    navController.navigate(Route.Localisation.route)
                },
                onContinueClick = {
                    navController.navigate(Route.Localisation.route)
                }
            )
        }

        composable(Route.Localisation.route) {
            LocalisationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.navigate(Route.Destination.route)
                }
            )
        }

        composable(Route.Destination.route) {
            DestinationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.navigate(Route.Recapitulatif.route)
                }
            )
        }

        composable(Route.Recapitulatif.route) {
            RecapitulatifScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onValidateClick = {
                    navController.navigate(Route.Confirmation.route) {
                        // Effacer la back stack jusqu'à l'accueil pour éviter de revenir en arrière
                        popUpTo(Route.Accueil.route) {
                            saveState = true
                        }
                    }
                },
                onEditPatient = {
                    // Retourner au dossier patient pour modifier
                    navController.popBackStack(Route.DossierPatient.route, inclusive = false)
                },
                onEditTrajet = {
                    // Retourner à la localisation pour modifier
                    navController.popBackStack(Route.Localisation.route, inclusive = false)
                },
                onEditMedias = {
                    // Retourner aux médias pour modifier
                    navController.popBackStack(Route.Medias.route, inclusive = false)
                }
            )
        }

        composable(Route.Confirmation.route) {
            ConfirmationScreen(
                onReturnHomeClick = {
                    // Retour à l'accueil en vidant toute la back stack
                    navController.navigate(Route.Accueil.route) {
                        popUpTo(Route.Accueil.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

