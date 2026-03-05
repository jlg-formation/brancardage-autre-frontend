package com.example.huybrancardage.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.huybrancardage.ui.screens.AccueilScreen
import com.example.huybrancardage.ui.screens.ConfirmationScreen
import com.example.huybrancardage.ui.screens.DestinationScreen
import com.example.huybrancardage.ui.screens.DossierPatientScreen
import com.example.huybrancardage.ui.screens.LocalisationScreen
import com.example.huybrancardage.ui.screens.MediasScreen
import com.example.huybrancardage.ui.screens.RecapitulatifScreen
import com.example.huybrancardage.ui.screens.RechercheManuelleScreen
import com.example.huybrancardage.ui.screens.ScanBraceletScreen
import com.example.huybrancardage.ui.viewmodel.BrancardageViewModel
import com.example.huybrancardage.ui.viewmodel.DestinationViewModel
import com.example.huybrancardage.ui.viewmodel.LocationViewModel
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

    // ViewModel partagé pour la localisation GPS
    val locationViewModel: LocationViewModel = viewModel()

    // ViewModel partagé pour la destination
    val destinationViewModel: DestinationViewModel = viewModel()

    // ViewModel partagé pour la session de brancardage
    val brancardageViewModel: BrancardageViewModel = viewModel()

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
                    // Stocker le patient dans les ViewModels
                    patientViewModel.setPatient(patient)
                    brancardageViewModel.setPatient(patient)
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
                    // Stocker le patient scanné dans les ViewModels
                    patientViewModel.setPatient(patient)
                    brancardageViewModel.setPatient(patient)
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
                    // Sauter l'étape médias - vider les médias dans la session
                    brancardageViewModel.setMedias(emptyList())
                    navController.navigate(Route.Localisation.route)
                },
                onContinueClick = {
                    // Transférer les médias vers la session de brancardage
                    brancardageViewModel.setMedias(mediaViewModel.uiState.value.medias)
                    navController.navigate(Route.Localisation.route)
                }
            )
        }

        composable(Route.Localisation.route) {
            LocalisationScreen(
                viewModel = locationViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    // Transférer la localisation vers la session de brancardage
                    locationViewModel.uiState.value.localisation?.let { localisation ->
                        brancardageViewModel.setLocalisation(localisation)
                    }
                    navController.navigate(Route.Destination.route)
                }
            )
        }

        composable(Route.Destination.route) {
            DestinationScreen(
                viewModel = destinationViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    // Transférer la destination vers la session de brancardage
                    destinationViewModel.uiState.value.selectedDestination?.let { destination ->
                        brancardageViewModel.setDestination(destination)
                    }
                    navController.navigate(Route.Recapitulatif.route)
                }
            )
        }

        composable(Route.Recapitulatif.route) {
            RecapitulatifScreen(
                brancardageViewModel = brancardageViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onValidateSuccess = { trackingNumber, patientName ->
                    navController.navigate(Route.Confirmation.createRoute(trackingNumber, patientName)) {
                        // Effacer la back stack jusqu'à l'accueil pour éviter de revenir en arrière
                        popUpTo(Route.Accueil.route) {
                            saveState = false
                        }
                    }
                },
                onQueuedSuccess = { patientName ->
                    // Mode hors ligne : naviguer vers l'écran de confirmation spécifique
                    navController.navigate(Route.ConfirmationQueued.createRoute(patientName)) {
                        popUpTo(Route.Accueil.route) {
                            saveState = false
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

        composable(
            route = Route.Confirmation.route,
            arguments = listOf(
                navArgument("trackingNumber") { type = NavType.StringType },
                navArgument("patientName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val trackingNumber = backStackEntry.arguments?.getString("trackingNumber")?.let {
                Route.Confirmation.decodeTrackingNumber(it)
            } ?: "N/A"
            val patientName = backStackEntry.arguments?.getString("patientName")?.let {
                Route.Confirmation.decodePatientName(it)
            } ?: "Patient"

            ConfirmationScreen(
                trackingNumber = trackingNumber,
                patientName = patientName,
                isSuccess = true,
                onReturnHomeClick = {
                    // Réinitialiser la session et retourner à l'accueil
                    brancardageViewModel.resetSession()
                    mediaViewModel.clearMedias()
                    locationViewModel.clear()
                    destinationViewModel.clearSelection()

                    navController.navigate(Route.Accueil.route) {
                        popUpTo(Route.Accueil.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ==================== CONFIRMATION HORS LIGNE ====================
        composable(
            route = Route.ConfirmationQueued.route,
            arguments = listOf(
                navArgument("patientName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientName = backStackEntry.arguments?.getString("patientName")?.let {
                Route.ConfirmationQueued.decodePatientName(it)
            } ?: "Patient"

            ConfirmationScreen(
                trackingNumber = "En attente",
                patientName = patientName,
                isSuccess = true,
                isQueued = true, // Indique que c'est une demande en file d'attente
                onReturnHomeClick = {
                    // Réinitialiser la session et retourner à l'accueil
                    brancardageViewModel.resetSession()
                    mediaViewModel.clearMedias()
                    locationViewModel.clear()
                    destinationViewModel.clearSelection()

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

