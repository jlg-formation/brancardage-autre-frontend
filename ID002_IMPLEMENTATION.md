# Rapport d'implémentation - ID002

## Tâche

- **ID** : id002
- **Titre** : Implémenter tous les écrans en statique (données mockées)
- **Date** : 2026-03-03

## Description

Implémentation des 9 écrans de l'application de brancardage avec des données mockées (statiques) :

1. **01-Accueil** : Menu principal avec options de scan et recherche manuelle
2. **02-Recherche manuelle** : Formulaire de recherche patient (nom, prénom, IPP, numéro sécu)
3. **03-Scan bracelet** : Interface caméra avec zone de ciblage QR/code-barres
4. **04-Dossier patient** : Affichage des informations patient avec alertes médicales
5. **05-Médias** : Galerie avec options d'ajout photo et documents
6. **06-Localisation** : Affichage GPS avec carte simulée et position détectée
7. **07-Destination** : Liste de destinations avec recherche et sélection radio
8. **08-Récapitulatif** : Synthèse de la demande (patient, trajet, médias)
9. **09-Confirmation** : Écran de succès avec numéro de suivi

## Fichiers créés

| Fichier | Description |
| ------- | ----------- |
| `app/src/main/java/com/example/huybrancardage/ui/screens/AccueilScreen.kt` | Écran d'accueil avec menu principal |
| `app/src/main/java/com/example/huybrancardage/ui/screens/RechercheManuelleScreen.kt` | Formulaire de recherche patient |
| `app/src/main/java/com/example/huybrancardage/ui/screens/ScanBraceletScreen.kt` | Interface de scan caméra |
| `app/src/main/java/com/example/huybrancardage/ui/screens/DossierPatientScreen.kt` | Affichage dossier patient |
| `app/src/main/java/com/example/huybrancardage/ui/screens/MediasScreen.kt` | Gestion des médias |
| `app/src/main/java/com/example/huybrancardage/ui/screens/LocalisationScreen.kt` | Écran de localisation GPS |
| `app/src/main/java/com/example/huybrancardage/ui/screens/DestinationScreen.kt` | Sélection destination |
| `app/src/main/java/com/example/huybrancardage/ui/screens/RecapitulatifScreen.kt` | Récapitulatif avant validation |
| `app/src/main/java/com/example/huybrancardage/ui/screens/ConfirmationScreen.kt` | Écran de confirmation succès |

## Fichiers modifiés

| Fichier | Modification |
| ------- | ------------ |
| `app/src/main/java/com/example/huybrancardage/MainActivity.kt` | Affichage de AccueilScreen |

## Conformité aux maquettes

Tous les écrans respectent les maquettes HTML fournies :

| Écran | Conformité | Notes |
| ----- | ---------- | ----- |
| 01-Accueil | ✓ | Header bleu, 2 options (scan/recherche), demandes récentes |
| 02-Recherche manuelle | ✓ | 4 champs (nom, prénom, IPP, sécu) avec séparateur "OU" |
| 03-Scan bracelet | ✓ | Fond sombre, cadre de visée, boutons flash et simulation |
| 04-Dossier patient | ✓ | Carte patient avec avatar, infos et alertes rouges |
| 05-Médias | ✓ | Boutons ajout photo/document, liste médias, option "Passer" |
| 06-Localisation | ✓ | Carte simulée, position détectée, champ précisions |
| 07-Destination | ✓ | Barre recherche, liste destinations avec radio buttons |
| 08-Récapitulatif | ✓ | Sections Patient/Trajet/Médias avec liens "Modifier" |
| 09-Confirmation | ✓ | Fond teal, icône succès, numéro de suivi |

## Design System appliqué

Tous les écrans utilisent les composants et couleurs du Design System :

- **Couleurs** : Blue600 (primaire), Gray50/100/500/900, White
- **Typographies** : Material3 Typography (titleLarge, bodyMedium, labelLarge, etc.)
- **Composants réutilisés** :
  - `BrancardageTopAppBar` : Header avec bouton retour
  - `BrancardageTextField` : Champs de saisie
  - `PrimaryButton` : Boutons d'action principaux
  - `BrancardageCard` : Cartes d'information
- **Bordures arrondies** : 12dp (boutons, champs), 16dp (cartes)

## Données mockées

Chaque écran utilise des données statiques pour la démonstration :

```kotlin
// Patient mocké
MockPatient(
    nom = "Jean Dupont",
    initiales = "JD",
    genre = "Homme",
    age = 42,
    ipp = "123456789",
    dateNaissance = "12/05/1980",
    chambre = "Cardiologie - 204"
)

// Localisation mockée
MockLocalisation(
    batiment = "Bâtiment A - Cardiologie",
    detail = "Étage 2, Chambre 204"
)

// Destinations mockées
listOf(
    MockDestination("1", "Radiologie", "Bâtiment B - RDC"),
    MockDestination("2", "Bloc Opératoire", "Bâtiment A - Étage 1"),
    MockDestination("3", "Urgences", "Bâtiment C - RDC")
)

// Média mocké
MockMedia(id = "1", nom = "Potence à sérum", type = "Photo", taille = "1.2 Mo")
```

## Résultats des tests

| Commande | Résultat |
| -------- | -------- |
| `./gradlew build` | ✓ BUILD SUCCESSFUL in 6s |
| `./gradlew test` | ✓ BUILD SUCCESSFUL (tests up-to-date) |
| `./gradlew lint` | ✓ BUILD SUCCESSFUL (no issues) |

## Callbacks disponibles

Chaque écran expose des callbacks pour la navigation future (id003) :

| Écran | Callbacks |
| ----- | --------- |
| AccueilScreen | `onScanBraceletClick`, `onRechercheManuelleClick` |
| RechercheManuelleScreen | `onBackClick`, `onSearchClick` |
| ScanBraceletScreen | `onBackClick`, `onScanSuccess` |
| DossierPatientScreen | `onBackClick`, `onCreateRequestClick` |
| MediasScreen | `onBackClick`, `onSkipClick`, `onContinueClick`, `onTakePhotoClick`, `onScanDocumentClick` |
| LocalisationScreen | `onBackClick`, `onConfirmClick` |
| DestinationScreen | `onBackClick`, `onConfirmClick` |
| RecapitulatifScreen | `onBackClick`, `onValidateClick`, `onEditPatient`, `onEditTrajet`, `onEditMedias` |
| ConfirmationScreen | `onReturnHomeClick` |

## Preview Compose

Tous les écrans disposent d'une fonction `@Preview` pour visualisation dans Android Studio :

```kotlin
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccueilScreenPreview() {
    HuyBrancardageTheme {
        AccueilScreen()
    }
}
```

## Notes

- Les écrans sont entièrement statiques et ne dépendent d'aucune API
- L'architecture suit le pattern de "state hoisting" pour faciliter l'intégration avec ViewModels (id004)
- La navigation entre écrans sera implémentée dans la tâche id003
- Les données mockées permettent de tester visuellement l'application sans backend

## Prochaine étape

**id003** - Connecter les écrans avec Navigation Compose pour créer le parcours utilisateur complet.

