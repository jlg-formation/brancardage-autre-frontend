# ID006 - Intégrer CameraX et ML Kit pour le scan de bracelet

## Résumé
Implémentation de la fonctionnalité de scan de bracelet patient utilisant CameraX pour la prévisualisation caméra et Google ML Kit Barcode Scanning pour la détection de codes-barres et QR codes.

## Fichiers créés

### 1. `app/src/main/java/com/example/huybrancardage/ui/camera/BarcodeAnalyzer.kt`
Analyseur d'images pour ML Kit qui :
- Implémente `ImageAnalysis.Analyzer` de CameraX
- Utilise ML Kit pour détecter les codes-barres (CODE_128, CODE_39, EAN, etc.) et QR codes
- Callback vers le ViewModel lors de la détection d'un code

### 2. `app/src/main/java/com/example/huybrancardage/ui/camera/CameraPreview.kt`
Composable Jetpack Compose pour la prévisualisation caméra :
- Utilise `AndroidView` pour intégrer `PreviewView` de CameraX
- Configure le `ProcessCameraProvider` avec Preview et ImageAnalysis
- Gère le cycle de vie de la caméra automatiquement

### 3. `app/src/test/java/com/example/huybrancardage/ui/viewmodel/ScanViewModelTest.kt`
Tests unitaires pour le ScanViewModel couvrant :
- Extraction d'IPP depuis différents formats de codes
- Gestion des états (scanning, processing, error)
- Toggle flash et permissions caméra

## Fichiers modifiés

### 1. `gradle/libs.versions.toml`
Ajout des dépendances :
```toml
cameraX = "1.4.1"
mlkitBarcodeScanning = "17.3.0"
kotlinxCoroutines = "1.8.1"

camerax-core, camerax-camera2, camerax-lifecycle, camerax-view
mlkit-barcode-scanning
kotlinx-coroutines-test
```

### 2. `app/build.gradle.kts`
Ajout des implémentations CameraX, ML Kit et coroutines-test

### 3. `app/src/main/AndroidManifest.xml`
Ajout de la permission caméra :
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

### 4. `app/src/main/java/com/example/huybrancardage/ui/viewmodel/ScanViewModel.kt`
Mise à jour complète :
- Intégration avec `PatientRepository` pour récupérer le patient après scan
- Extraction d'IPP multi-format (direct, préfixe IPP:, URL, etc.)
- Gestion des états : scanning, processing, error, patient trouvé
- Toggle flash et permissions caméra

### 5. `app/src/main/java/com/example/huybrancardage/ui/screens/ScanBraceletScreen.kt`
Refonte complète de l'écran :
- Intégration de `CameraPreview` pour la prévisualisation
- Gestion des permissions caméra avec `rememberLauncherForActivityResult`
- Overlay de scan avec animation de ligne rouge
- Gestion des erreurs avec bouton "Réessayer"
- Bouton flash et bouton de simulation

### 6. `app/src/main/java/com/example/huybrancardage/ui/screens/DossierPatientScreen.kt`
- Accepte maintenant un `PatientViewModel` pour afficher le patient scanné
- Fonction d'extension `Patient.toMockPatient()` pour conversion

### 7. `app/src/main/java/com/example/huybrancardage/navigation/NavGraph.kt`
- Utilisation d'un `PatientViewModel` partagé
- Passage du patient entre `ScanBraceletScreen` et `DossierPatientScreen`

## Architecture

```
┌─────────────────────┐
│  ScanBraceletScreen │
│  (Jetpack Compose)  │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐     ┌─────────────────────┐
│   CameraPreview     │────▶│   BarcodeAnalyzer   │
│   (CameraX View)    │     │   (ML Kit)          │
└─────────────────────┘     └─────────┬───────────┘
                                      │ onBarcodeDetected
                                      ▼
                            ┌─────────────────────┐
                            │   ScanViewModel     │
                            │   - extractIpp()    │
                            │   - fetchPatient()  │
                            └─────────┬───────────┘
                                      │
                                      ▼
                            ┌─────────────────────┐
                            │  PatientRepository  │
                            │  - getPatientByIpp()│
                            └─────────────────────┘
```

## Formats de codes supportés

L'extracteur d'IPP supporte plusieurs formats :
1. **Direct** : `123456789` (9 chiffres)
2. **Préfixe IPP** : `IPP:123456789` ou `IPP123456789`
3. **Préfixe PATIENT** : `PATIENT:123456789`
4. **URL** : `https://hospital.com/patient?ipp=123456789`
5. **Extraction** : Tout code contenant 9 chiffres consécutifs

## Flux utilisateur

1. L'utilisateur ouvre l'écran de scan
2. Demande de permission caméra si nécessaire
3. La caméra démarre avec l'analyseur ML Kit
4. L'utilisateur pointe le bracelet patient
5. ML Kit détecte le code-barres/QR code
6. L'IPP est extrait et validé
7. Le patient est récupéré via l'API
8. Navigation automatique vers le dossier patient

## Tests

Exécuter les tests :
```bash
./gradlew test --tests "ScanViewModelTest"
```

Tests couverts :
- ✅ Extraction IPP format direct
- ✅ Extraction IPP format préfixe
- ✅ Extraction IPP format URL
- ✅ Gestion code invalide
- ✅ État scanning/processing
- ✅ Toggle flash
- ✅ Permissions caméra
- ✅ Reset et clear error

## Statut
✅ **Implémenté et testé**

Build : `./gradlew assembleDebug` - SUCCESS

