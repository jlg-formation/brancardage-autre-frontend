# ID007 - Gestion des Médias (Appareil Photo + Galerie)

## Résumé

Implémentation complète de la gestion des médias pour l'application de brancardage, permettant aux utilisateurs de prendre des photos, d'importer des images depuis la galerie, et de scanner des documents. Toutes les images sont compressées automatiquement avant stockage.

## Fichiers créés

### 1. `app/src/main/java/com/example/huybrancardage/data/media/MediaManager.kt`

Manager pour la manipulation des médias avec les fonctionnalités suivantes :
- **Création d'URI temporaires** via FileProvider pour la prise de photo
- **Compression d'images** (redimensionnement à max 1024px, qualité JPEG 85%)
- **Récupération des métadonnées** (taille, nom du fichier)
- **Formatage de la taille** en format lisible (Ko, Mo)
- **Nettoyage du cache** des images temporaires

### 2. `app/src/main/res/xml/file_paths.xml`

Configuration du FileProvider pour le partage sécurisé des URIs de fichiers avec l'appareil photo.

## Fichiers modifiés

### 1. `app/src/main/AndroidManifest.xml`

Ajout du FileProvider pour permettre le partage des URIs de photos :
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.example.huybrancardage.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### 2. `app/src/main/java/com/example/huybrancardage/ui/viewmodel/MediaViewModel.kt`

Refonte complète du ViewModel pour gérer les médias :
- **État étendu** (`MediaUiState`) avec `isProcessingPhoto`, `pendingCameraUri`
- **Intégration MediaManager** pour la compression
- **Méthodes pour caméra** : `prepareTakePicture()`, `onPhotoTaken()`
- **Méthode pour galerie** : `onGalleryImageSelected()`
- **Méthode pour documents** : `onDocumentScanned()`
- **Gestion des erreurs** avec messages utilisateur
- **Nettoyage automatique** du cache dans `onCleared()`

### 3. `app/src/main/java/com/example/huybrancardage/ui/screens/MediasScreen.kt`

Écran complètement refondu avec :
- **ActivityResult Contracts** :
  - `RequestPermission` pour la permission caméra
  - `TakePicture` pour la prise de photo
  - `GetContent` pour la sélection depuis la galerie
- **3 boutons d'action** : Prendre photo, Galerie, Scanner document
- **Affichage dynamique** des médias depuis le ViewModel
- **Miniatures réelles** des images avec Coil (`AsyncImage`)
- **Indicateur de chargement** pendant le traitement des images
- **Snackbar d'erreur** pour les messages d'erreur
- **État vide** quand aucun média n'est ajouté

### 4. `app/src/main/java/com/example/huybrancardage/navigation/NavGraph.kt`

- Ajout du `MediaViewModel` partagé au niveau du NavGraph
- Connexion du ViewModel à l'écran `MediasScreen`

### 5. `gradle/libs.versions.toml`

Ajout de la dépendance Coil pour le chargement d'images :
```toml
coil = "2.6.0"
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

### 6. `app/build.gradle.kts`

Ajout de l'implémentation Coil :
```kotlin
implementation(libs.coil.compose)
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        MediasScreen                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  ActivityResult Launchers                                │    │
│  │  - RequestPermission (CAMERA)                           │    │
│  │  - TakePicture                                          │    │
│  │  - GetContent (image/*)                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    MediaViewModel                        │    │
│  │  - uiState: StateFlow<MediaUiState>                     │    │
│  │  - prepareTakePicture() → Uri                           │    │
│  │  - onPhotoTaken(success)                                │    │
│  │  - onGalleryImageSelected(uri)                          │    │
│  │  - onDocumentScanned(success)                           │    │
│  │  - removeMedia(id)                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    MediaManager                          │    │
│  │  - createTempImageUri() → Uri (FileProvider)            │    │
│  │  - compressImage(uri) → Uri (compressed)                │    │
│  │  - getFileSize(uri) → Long                              │    │
│  │  - formatFileSize(bytes) → String                       │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## Flux utilisateur

### Prise de photo
1. Utilisateur clique sur "Prendre une photo"
2. Demande de permission CAMERA (si nécessaire)
3. Création d'une URI temporaire via FileProvider
4. Lancement de l'appareil photo système
5. Retour avec succès/échec
6. Compression de l'image (max 1024px, JPEG 85%)
7. Ajout à la liste des médias avec métadonnées

### Import depuis galerie
1. Utilisateur clique sur "Galerie"
2. Ouverture du sélecteur de fichiers (image/*)
3. Sélection d'une image
4. Compression de l'image
5. Ajout à la liste des médias

### Scanner un document
1. Même flux que la prise de photo
2. Le média est typé comme `DOCUMENT` au lieu de `PHOTO`

## Compression des images

- **Dimension max** : 1024 pixels (largeur ou hauteur)
- **Qualité JPEG** : 85%
- **Conservation du ratio** : Oui
- **Format de sortie** : JPEG

## Tests manuels effectués

- [x] Build réussi sans erreurs
- [x] Compilation du projet
- [x] Structure des fichiers correcte

## Prochaines étapes

La tâche **id008** (Géolocalisation GPS) peut maintenant être implémentée.

