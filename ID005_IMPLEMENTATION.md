# ID005 - Couche réseau (Retrofit + Repository)

## Résumé
Création de la couche réseau complète avec Retrofit et pattern Repository pour connecter l'application aux APIs backend.

## Fichiers créés

### Configuration Gradle
- **gradle/libs.versions.toml** : Ajout des versions pour Retrofit (2.11.0), OkHttp (4.12.0), Kotlinx Serialization (1.7.3)
- **app/build.gradle.kts** : Ajout du plugin `kotlin-serialization` et des dépendances réseau

### DTOs (Data Transfer Objects)
- **data/remote/dto/PatientDto.kt** : DTO pour les patients et alertes médicales
- **data/remote/dto/DestinationDto.kt** : DTO pour les destinations
- **data/remote/dto/ErrorResponseDto.kt** : DTO pour les erreurs API

### Mappers
- **data/remote/mapper/PatientMapper.kt** : Conversion PatientDto → Patient domain
- **data/remote/mapper/DestinationMapper.kt** : Conversion DestinationDto → Destination domain

### API Service
- **data/remote/api/BrancardageApiService.kt** : Interface Retrofit avec tous les endpoints
  - `GET /patients` - Recherche de patients
  - `GET /patients/{id}` - Récupération par ID
  - `GET /patients/by-ipp/{ipp}` - Récupération par IPP
  - `GET /destinations` - Liste des destinations
  - `GET /destinations/{id}` - Destination par ID

### Client API
- **data/remote/api/ApiClient.kt** : Configuration Retrofit/OkHttp avec logging
- **data/remote/NetworkResult.kt** : Sealed class pour wrapper les résultats (Success/Error/Loading)

### Repositories
- **data/repository/PatientRepository.kt** : Repository patients avec données mockées
- **data/repository/DestinationRepository.kt** : Repository destinations avec données mockées

### Manifest
- **AndroidManifest.xml** : Ajout permissions `INTERNET` et `ACCESS_NETWORK_STATE`

## Fichiers modifiés

### ViewModels refactorisés
- **SearchViewModel.kt** : Utilise maintenant `PatientRepository.searchPatients()`
- **PatientViewModel.kt** : Utilise `PatientRepository.getPatientById()` et `getPatientByIpp()`
- **DestinationViewModel.kt** : Utilise `DestinationRepository.getDestinations()`

## Architecture

```
data/
├── remote/
│   ├── api/
│   │   ├── ApiClient.kt          # Configuration Retrofit
│   │   └── BrancardageApiService.kt  # Interface API
│   ├── dto/
│   │   ├── PatientDto.kt
│   │   ├── DestinationDto.kt
│   │   └── ErrorResponseDto.kt
│   ├── mapper/
│   │   ├── PatientMapper.kt
│   │   └── DestinationMapper.kt
│   └── NetworkResult.kt          # Wrapper résultats
└── repository/
    ├── PatientRepository.kt
    └── DestinationRepository.kt
```

## Données mockées

Les repositories utilisent par défaut des données mockées en dur (`useMockedData = true`) permettant de :
- Tester l'application sans backend
- Simuler des délais réseau réalistes
- Avoir un jeu de données cohérent pour les tests

### Patients mockés (5)
| Nom | Prénom | IPP | Service |
|-----|--------|-----|---------|
| Dupont | Jean | 123456789 | Cardiologie |
| Martin | Marie | 987654321 | Pneumologie |
| Durand | Pierre | 456789123 | Orthopédie |
| Bernard | Sophie | 789123456 | Neurologie |
| Petit | Jacques | 321654987 | Gériatrie |

### Destinations mockées (12)
Radiologie, Bloc Opératoire, Urgences, Scanner, IRM, Cardiologie, Pneumologie, Neurologie, Orthopédie, Gériatrie, Réanimation, Laboratoire

## Basculer vers l'API réelle

Pour utiliser l'API réelle au lieu des mocks :

```kotlin
// Dans le Repository
PatientRepository(useMockedData = false)
DestinationRepository(useMockedData = false)
```

L'URL de base est configurée dans `ApiClient.kt` :
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"
```

## Tests

La compilation a été vérifiée avec succès (`./gradlew assembleDebug`).

## Prochaines étapes
- **ID006** : Écran scan bracelet avec CameraX et ML Kit
- **ID007** : Intégration GPS pour la localisation
- **ID008** : Finaliser l'écran de sélection de destination

