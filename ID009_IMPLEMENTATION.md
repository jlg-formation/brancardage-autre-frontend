# ID009 - Implémenter récapitulatif, validation et POST de brancardage

## Statut : ✅ TERMINÉ

## Description
Cette tâche implémente l'écran récapitulatif, la validation et l'envoi de la demande de brancardage vers l'API.

## Dépendances
- ✅ id008 - Géolocalisation GPS (complété)

## Livrables

### 1. DTOs pour Brancardage (data/remote/dto/BrancardageDto.kt)
- `BrancardageRequestDto` : Requête de création de brancardage
- `LocalisationDto` : Localisation GPS et descriptive
- `BrancardageResponseDto` : Réponse de création
- `PatientResumeDto` : Résumé patient pour la réponse
- `MediaDto` : Média attaché
- `MediaUploadResponseDto` : Réponse d'upload média

### 2. API Endpoints (data/remote/api/BrancardageApiService.kt)
- `POST /brancardages` : Création d'une demande de brancardage
- `POST /medias` (multipart) : Upload de média

### 3. BrancardageMapper (data/remote/mapper/BrancardageMapper.kt)
- Conversion BrancardageRequest → BrancardageRequestDto
- Conversion Localisation → LocalisationDto
- Conversion BrancardageResponseDto → BrancardageResponse
- Conversion MediaUploadResponseDto → Media

### 4. BrancardageRepository (data/repository/BrancardageRepository.kt)
- `createBrancardage()` : Envoi de la demande
- `uploadMedia()` : Upload de fichiers médias
- Mode mocké par défaut avec délai simulé
- Gestion des erreurs réseau

### 5. BrancardageViewModel amélioré (ui/viewmodel/BrancardageViewModel.kt)
- `SubmissionState` : États de soumission (Idle, Loading, Success, Error)
- `submitBrancardage(context)` : Validation et envoi
- Upload des médias avant création de la demande
- Gestion des erreurs de validation et réseau
- `getValidationErrors()` : Liste des erreurs de validation

### 6. RecapitulatifScreen amélioré (ui/screens/RecapitulatifScreen.kt)
- Affiche les données réelles de la session (patient, trajet, médias)
- Loading overlay pendant l'envoi
- Dialog d'erreur en cas d'échec
- Bouton désactivé si données incomplètes
- Navigation vers Confirmation en cas de succès

### 7. ConfirmationScreen amélioré (ui/screens/ConfirmationScreen.kt)
- Paramètres dynamiques : trackingNumber, patientName
- Support de l'état d'erreur (isSuccess, errorMessage)
- Affichage du numéro de suivi

### 8. Routes mises à jour (navigation/Routes.kt)
- `Confirmation` avec paramètres URL encodés
- `createRoute(trackingNumber, patientName)`
- Fonctions de décodage URL

### 9. NavGraph mis à jour (navigation/NavGraph.kt)
- Intégration de `BrancardageViewModel` partagé
- Transfert des données entre écrans (médias, localisation, destination)
- Navigation vers Confirmation avec paramètres
- Reset de session au retour à l'accueil

### 10. DestinationScreen amélioré (ui/screens/DestinationScreen.kt)
- Accepte `DestinationViewModel` optionnel
- Utilise données réelles du Repository
- Recherche et filtrage des destinations

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer                                  │
├─────────────────────────────────────────────────────────────────┤
│  RecapitulatifScreen ─────► BrancardageViewModel                │
│       │                           │                              │
│       │ onValidateSuccess         │ submitBrancardage()          │
│       ▼                           ▼                              │
│  ConfirmationScreen         SubmissionState                      │
│       │                     (Idle/Loading/Success/Error)         │
│       │ onReturnHomeClick                                        │
│       ▼                                                          │
│  AccueilScreen                                                   │
├─────────────────────────────────────────────────────────────────┤
│                       Domain Layer                               │
├─────────────────────────────────────────────────────────────────┤
│  BrancardageRequest          BrancardageResponse                 │
│  BrancardageSessionState     SubmissionState                     │
├─────────────────────────────────────────────────────────────────┤
│                        Data Layer                                │
├─────────────────────────────────────────────────────────────────┤
│  BrancardageRepository ────► BrancardageApiService               │
│       │                           │                              │
│       │ createBrancardage()       │ POST /brancardages           │
│       │ uploadMedia()             │ POST /medias                 │
│       ▼                           ▼                              │
│  NetworkResult               BrancardageDto                      │
│  (Success/Error/Loading)     (Request/Response)                  │
└─────────────────────────────────────────────────────────────────┘
```

## Flux de données

1. **Récapitulatif** : Affiche BrancardageSessionState
2. **Validation** : Clic sur "Valider la demande"
3. **Upload médias** : Pour chaque média, POST /medias
4. **Création brancardage** : POST /brancardages avec mediaIds
5. **Succès** : Navigation vers Confirmation avec trackingNumber
6. **Erreur** : Affichage dialog avec message d'erreur

## Gestion des erreurs

- **Validation** : Données incomplètes → bouton désactivé
- **Upload média** : Continue même si upload échoue (médias optionnels)
- **Création brancardage** : Affiche dialog d'erreur
- **Réseau** : Catch des exceptions avec message utilisateur

## Tests manuels

1. Parcours complet : Accueil → Patient → Médias → Localisation → Destination → Récapitulatif → Confirmation
2. Validation avec données manquantes (bouton désactivé)
3. Envoi avec succès (numéro de suivi affiché)
4. Retour à l'accueil (session reset)

## Fichiers modifiés/créés

### Nouveaux fichiers
- `app/src/main/java/com/example/huybrancardage/data/remote/dto/BrancardageDto.kt`
- `app/src/main/java/com/example/huybrancardage/data/remote/mapper/BrancardageMapper.kt`
- `app/src/main/java/com/example/huybrancardage/data/repository/BrancardageRepository.kt`

### Fichiers modifiés
- `app/src/main/java/com/example/huybrancardage/data/remote/api/BrancardageApiService.kt`
- `app/src/main/java/com/example/huybrancardage/ui/viewmodel/BrancardageViewModel.kt`
- `app/src/main/java/com/example/huybrancardage/ui/screens/RecapitulatifScreen.kt`
- `app/src/main/java/com/example/huybrancardage/ui/screens/ConfirmationScreen.kt`
- `app/src/main/java/com/example/huybrancardage/ui/screens/DestinationScreen.kt`
- `app/src/main/java/com/example/huybrancardage/navigation/Routes.kt`
- `app/src/main/java/com/example/huybrancardage/navigation/NavGraph.kt`

