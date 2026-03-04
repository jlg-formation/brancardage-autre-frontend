# ID008 - Intégrer Play Services Location pour le GPS

## Résumé

Cette tâche implémente la géolocalisation GPS en utilisant Google Play Services Location (Fused Location Provider) pour détecter automatiquement la position de l'utilisateur sur l'écran de localisation.

## Fichiers créés

### 1. `app/src/main/java/com/example/huybrancardage/data/location/LocationService.kt`

Service de géolocalisation encapsulant l'accès au Fused Location Provider.

**Fonctionnalités :**
- `hasLocationPermission()` : Vérifie si les permissions de localisation sont accordées
- `getLastKnownLocation()` : Récupère la dernière position connue (rapide, peut être null)
- `getCurrentLocation(timeoutMillis)` : Récupère la position actuelle avec timeout (défaut: 10s)
- `observeLocationUpdates(intervalMillis)` : Flow pour suivre les mises à jour de position en continu

**Gestion du timeout :**
- Timeout de 10 secondes par défaut
- Lève `LocationTimeoutException` si le GPS ne répond pas dans le délai
- L'application utilise une position par défaut en cas de timeout

### 2. `app/src/main/java/com/example/huybrancardage/data/location/LocationToHospitalMapper.kt`

Convertisseur de coordonnées GPS en localisation hospitalière (simulation).

**Fonctionnalités :**
- `mapToLocalisation(latitude, longitude)` : Convertit des coordonnées GPS en `Localisation` avec informations de bâtiment
- `getDefaultLocalisation()` : Retourne une localisation par défaut (Bâtiment A - Cardiologie)
- Zones hospitalières simulées pour le mapping

### 3. `app/src/test/java/com/example/huybrancardage/ui/viewmodel/LocationViewModelTest.kt`

Tests unitaires pour le LocationViewModel et LocationToHospitalMapper.

## Fichiers modifiés

### 1. `gradle/libs.versions.toml`

Ajout de la dépendance Play Services Location :
```toml
playServicesLocation = "21.3.0"
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
```

### 2. `app/build.gradle.kts`

Ajout de l'implémentation :
```kotlin
implementation(libs.play.services.location)
```

### 3. `app/src/main/AndroidManifest.xml`

Ajout des permissions GPS :
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 4. `app/src/main/java/com/example/huybrancardage/ui/viewmodel/LocationViewModel.kt`

Refactoring complet pour utiliser le vrai GPS :
- Ajout de `initLocationService(service)` pour initialiser le service GPS
- Ajout de `onPermissionResult(granted)` pour gérer le résultat des permissions
- Gestion du timeout GPS avec message d'erreur approprié
- Fallback vers position par défaut en cas d'erreur
- Nouveaux champs dans `LocationUiState` : `hasPermission`, `permissionRequested`

### 5. `app/src/main/java/com/example/huybrancardage/ui/screens/LocalisationScreen.kt`

Mise à jour pour utiliser le ViewModel avec permissions runtime :
- Demande de permission de localisation via `ActivityResultContracts.RequestMultiplePermissions`
- Affichage de l'état de chargement pendant la recherche GPS
- Bouton de rafraîchissement pour relancer la recherche
- Affichage des coordonnées GPS
- Message d'erreur en cas de timeout ou erreur GPS
- Bouton pour re-demander la permission si refusée

### 6. `app/src/main/java/com/example/huybrancardage/navigation/NavGraph.kt`

Ajout du LocationViewModel partagé et passage à l'écran LocalisationScreen.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    LocalisationScreen                        │
│  - Demande permission runtime                                │
│  - Affiche position / loading / erreur                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   LocationViewModel                          │
│  - initLocationService()                                     │
│  - loadCurrentLocation()                                     │
│  - onPermissionResult()                                      │
│  - Gestion timeout + fallback                                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   LocationService                            │
│  - Fused Location Provider                                   │
│  - Timeout 10s avec withTimeout()                            │
│  - LocationTimeoutException                                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│              LocationToHospitalMapper                        │
│  - Convertit GPS → Localisation hospitalière                 │
│  - Zones simulées pour le mapping                            │
└─────────────────────────────────────────────────────────────┘
```

## Comportement du timeout

1. L'utilisateur arrive sur l'écran de localisation
2. L'app demande la permission de localisation (si pas déjà accordée)
3. Si accordée, l'app essaie d'abord `getLastKnownLocation()` (rapide)
4. Si null, l'app appelle `getCurrentLocation()` avec timeout de 10s
5. Si timeout atteint → `LocationTimeoutException` → position par défaut + message d'erreur
6. L'utilisateur peut rafraîchir manuellement avec le bouton refresh

## Tests

Les tests unitaires couvrent :
- État initial du ViewModel
- Gestion des permissions (accordée/refusée)
- Mise à jour des précisions et localisation
- Fonctions clear/reset
- Constantes du LocationService
- Mapping GPS vers localisation hospitalière

## Exécution des tests

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.huybrancardage.ui.viewmodel.LocationViewModelTest"
```

