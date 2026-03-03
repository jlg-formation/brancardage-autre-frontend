# ID004 - ViewModels et Gestion d'État (State Management)

## Résumé

Implémentation de l'architecture MVVM avec ViewModels et gestion d'état pour l'application Brancardage.

## Fichiers créés

### Modèles de données (Domain Layer)

| Fichier | Description |
|---------|-------------|
| `domain/model/Patient.kt` | Modèle Patient avec alertes médicales |
| `domain/model/Media.kt` | Modèle Media (photo/document) |
| `domain/model/Localisation.kt` | Modèle Localisation GPS + descriptive |
| `domain/model/Destination.kt` | Modèle Destination hospitalière |
| `domain/model/BrancardageRequest.kt` | Requête et réponse de brancardage |

### ViewModels (UI Layer)

| Fichier | Description |
|---------|-------------|
| `ui/viewmodel/BrancardageViewModel.kt` | ViewModel partagé pour la session de brancardage |
| `ui/viewmodel/SearchViewModel.kt` | ViewModel pour la recherche manuelle de patients |
| `ui/viewmodel/PatientViewModel.kt` | ViewModel pour le dossier patient |
| `ui/viewmodel/MediaViewModel.kt` | ViewModel pour la gestion des médias |
| `ui/viewmodel/LocationViewModel.kt` | ViewModel pour la géolocalisation |
| `ui/viewmodel/DestinationViewModel.kt` | ViewModel pour la sélection de destination |
| `ui/viewmodel/ScanViewModel.kt` | ViewModel pour le scan de bracelet |

### État partagé

| Fichier | Description |
|---------|-------------|
| `ui/state/BrancardageSessionState.kt` | État de la session de brancardage |

## Modifications apportées

### Dépendances ajoutées

```toml
# gradle/libs.versions.toml
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.lifecycle.runtime.compose)
```

## Architecture

```
app/src/main/java/com/example/huybrancardage/
├── domain/
│   └── model/
│       ├── Patient.kt           # Modèle Patient + Sexe + AlerteMedicale + TypeAlerte
│       ├── Media.kt             # Modèle Media + MediaType
│       ├── Localisation.kt      # Modèle Localisation GPS
│       ├── Destination.kt       # Modèle Destination
│       └── BrancardageRequest.kt # Request/Response + StatutBrancardage
├── ui/
│   ├── state/
│   │   └── BrancardageSessionState.kt  # État partagé de la session
│   ├── viewmodel/
│   │   ├── BrancardageViewModel.kt     # Session partagée
│   │   ├── SearchViewModel.kt          # Recherche manuelle
│   │   ├── PatientViewModel.kt         # Dossier patient
│   │   ├── MediaViewModel.kt           # Gestion médias
│   │   ├── LocationViewModel.kt        # Géolocalisation
│   │   ├── DestinationViewModel.kt     # Sélection destination
│   │   └── ScanViewModel.kt            # Scan bracelet
│   └── screens/
│       └── ... (écrans existants)
```

## Modèles de données

### Patient

```kotlin
data class Patient(
    val id: String,
    val ipp: String,
    val nom: String,
    val prenom: String,
    val dateNaissance: LocalDate,
    val sexe: Sexe,
    val numeroSecuriteSociale: String? = null,
    val chambre: String? = null,
    val service: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    val alertesMedicales: List<AlerteMedicale> = emptyList()
)
```

### Media

```kotlin
data class Media(
    val id: String,
    val uri: String,
    val type: MediaType,
    val mimeType: String = "image/jpeg",
    val taille: Long = 0,
    val dateAjout: Instant = Instant.now(),
    val description: String? = null
)
```

### Localisation

```kotlin
data class Localisation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    val batiment: String? = null,
    val etage: Int? = null,
    val chambre: String? = null,
    val precisions: String? = null
)
```

### Destination

```kotlin
data class Destination(
    val id: String,
    val nom: String,
    val batiment: String,
    val etage: Int,
    val etageLibelle: String? = null,
    val frequente: Boolean = false
)
```

## UI States

Chaque ViewModel expose un `StateFlow` avec son UI State :

| ViewModel | UI State | Propriétés principales |
|-----------|----------|------------------------|
| `SearchViewModel` | `SearchUiState` | nom, prenom, ipp, numeroSecu, isLoading, results, error |
| `PatientViewModel` | `PatientUiState` | patient, isLoading, error |
| `MediaViewModel` | `MediaUiState` | medias, isLoading, error |
| `LocationViewModel` | `LocationUiState` | localisation, precisions, isLoading, isGpsEnabled, error |
| `DestinationViewModel` | `DestinationUiState` | destinations, filteredDestinations, searchQuery, selectedDestination |
| `ScanViewModel` | `ScanUiState` | isScanning, scannedCode, isProcessing, hasCameraPermission, error |

## État partagé de la session

```kotlin
data class BrancardageSessionState(
    val patient: Patient? = null,
    val medias: List<Media> = emptyList(),
    val localisation: Localisation? = null,
    val destination: Destination? = null,
    val commentaire: String = ""
) {
    val isReadyForValidation: Boolean
        get() = patient != null && 
                localisation?.isValid == true && 
                destination != null
}
```

## Utilisation future

### Intégration dans les écrans (id005+)

```kotlin
@Composable
fun RechercheManuelleScreen(
    viewModel: SearchViewModel = viewModel(),
    onPatientSelected: (Patient) -> Unit,
    // ...
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Utiliser uiState.nom, uiState.results, etc.
}
```

### Session partagée entre écrans

Le `BrancardageViewModel` sera partagé entre les écrans du parcours de création de demande pour maintenir l'état global (patient sélectionné, médias, localisation, destination).

## Tests

Les ViewModels sont prêts pour les tests unitaires avec :
- Données mockées intégrées
- Méthodes clairement séparées
- États exposés via StateFlow

## Prochaines étapes

1. **id005** : Connecter les ViewModels aux écrans et créer la couche réseau (Retrofit)
2. **id006** : Intégrer CameraX/ML Kit dans ScanViewModel
3. **id007** : Connecter MediaViewModel avec la caméra et la galerie
4. **id008** : Intégrer le GPS dans LocationViewModel

## Conformité

- ✅ Architecture MVVM respectée
- ✅ Modèles conformes au swagger API
- ✅ États réactifs avec StateFlow/Kotlin Flow
- ✅ Séparation des préoccupations (Domain/UI)

