# AGENTS.md - Guide pour Agents IA

## 📋 Vue d'ensemble du projet

**HuyBrancardage** est une application mobile Android de gestion des demandes de brancardage hospitalier, développée en **Kotlin** avec **Jetpack Compose**. Ce projet sert de cas d'étude pédagogique pour une formation technique de 4 jours destinée aux développeurs de la DSI d'un centre hospitalier.

---

## 🎯 Objectifs du projet

1. **Pédagogique** : Former les développeurs au développement Android moderne (Kotlin, Jetpack Compose)
2. **Technique** : Fournir une base de code saine et architecturée pour les futurs projets de la DSI

---

## 🏗️ Architecture technique

### Stack principale

| Composant | Technologie |
|-----------|-------------|
| **Langage** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Navigation** | Navigation Compose |
| **Réseau** | Retrofit + OkHttp + Kotlinx Serialization |
| **Asynchronisme** | Coroutines + Flow |
| **Camera** | CameraX (prévu) |
| **Scan QR/Barcode** | Google ML Kit (prévu) |
| **Géolocalisation** | Play Services Location (prévu) |

### Structure du projet

```
app/src/main/java/com/example/huybrancardage/
├── data/
│   ├── remote/          # ApiService, DTOs
│   └── repository/      # Repositories (PatientRepository, etc.)
├── domain/
│   └── model/           # Modèles métier (Patient, Media, Destination, etc.)
├── navigation/          # Configuration de la navigation
├── ui/
│   ├── screens/         # Écrans Compose (10 écrans)
│   ├── state/           # États UI
│   ├── theme/           # Thème, couleurs, typographie
│   └── viewmodel/       # ViewModels (7 ViewModels)
└── MainActivity.kt      # Point d'entrée
```

### Couches d'architecture (MVVM)

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer (Compose Screens)                             │
│  - Composants stateless                                 │
│  - Collecte des StateFlow avec collectAsStateWithLifecycle()
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│  ViewModels (State Holders)                             │
│  - Exposent l'état via StateFlow                        │
│  - Gèrent la logique de présentation                    │
│  - Utilisent viewModelScope pour les coroutines         │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│  Data Layer (Repositories + Remote)                     │
│  - PatientRepository: abstraction des sources de données│
│  - ApiService: endpoints Retrofit                       │
│  - Dispatchers.IO pour les appels réseau                │
└─────────────────────────────────────────────────────────┘
```

---

## 📱 Écrans de l'application

| # | Écran | Fichier | ViewModel associé | Description |
|---|-------|---------|-------------------|-------------|
| 01 | Accueil | `AccueilScreen.kt` | - | Menu principal |
| 02 | Recherche manuelle | `RechercheManuelleScreen.kt` | `SearchViewModel` | Formulaire de recherche patient |
| 03 | Scan bracelet | `ScanBraceletScreen.kt` | `ScanViewModel` | Caméra + détection QR/barcode |
| 04 | Dossier patient | `DossierPatientScreen.kt` | `PatientViewModel` | Affichage info patient |
| 05 | Médias | `MediasScreen.kt` | `MediaViewModel` | Galerie + prise de photos |
| 06 | Localisation | `LocalisationScreen.kt` | `LocationViewModel` | Position GPS |
| 07 | Destination | `DestinationScreen.kt` | `DestinationViewModel` | Sélection destination |
| 08 | Récapitulatif | `RecapitulatifScreen.kt` | `BrancardageViewModel` | Synthèse avant envoi |
| 09 | Confirmation | `ConfirmationScreen.kt` | - | Feedback succès |

---

## 🗂️ Modèles de données

### Patient
```kotlin
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val ipp: String,           // Identifiant Permanent Patient
    val dateNaissance: String,
    val numeroSecuriteSociale: String?
)
```

### Destination
```kotlin
data class Destination(
    val id: String,
    val nom: String,
    val batiment: String?,
    val etage: String?
)
```

### BrancardageRequest
```kotlin
data class BrancardageRequest(
    val patientId: String,
    val localisationDepart: Localisation,
    val destinationId: String,
    val mediaIds: List<String>
)
```

---

## 🌐 API Endpoints (Backend Ktor)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/patients` | Recherche de patients (query params) |
| `GET` | `/patients/{id}` | Récupération d'un dossier patient |
| `POST` | `/brancardage` | Création d'une demande de transport |
| `POST` | `/upload` | Envoi de médias (photos/documents) |

---

## 📜 Conventions de code

### Kotlin général
- **Null Safety** : Types non-nullables par défaut
- **Immutabilité** : Préférer `val` à `var`, collections immuables
- **Fonctions d'extension** : Avec parcimonie

### Jetpack Compose
- **State Hoisting** : Remonter l'état au parent, composants enfants stateless
- **Nommage** : PascalCase pour les Composables (ex: `PatientProfileCard`)
- **Preview** : Créer des fonctions `@Preview` pour chaque composant

### ViewModels
- Exposer l'état via `StateFlow<UiState>`
- Utiliser `viewModelScope` pour les coroutines
- Dispatcher approprié (`Dispatchers.IO` pour réseau/disque)

### Gestion des erreurs réseau
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## 📁 Fichiers de configuration importants

| Fichier | Description |
|---------|-------------|
| `build.gradle.kts` (app) | Dépendances de l'application |
| `gradle/libs.versions.toml` | Catalogue de versions centralisé |
| `AndroidManifest.xml` | Permissions et configuration Android |
| `specifications/swagger/api.yml` | Spécification OpenAPI du backend |

---

## 🛣️ Roadmap (État actuel)

### ✅ Complété
- [x] **id001** - Setup projet + Design System
- [x] **id002** - Écrans statiques (9 écrans)
- [x] **id003** - Navigation Compose
- [x] **id004** - ViewModels + State Management
- [x] **id005** - Couche réseau (Retrofit + Repository)

### 🔜 À faire
- [ ] **id006** - CameraX + ML Kit (scan bracelet)
- [ ] **id007** - Gestion médias (appareil photo + galerie)
- [ ] **id008** - Géolocalisation (GPS)
- [ ] **id009** - Validation + POST brancardage
- [ ] **id010** - Tests et polish final

---

## 📚 Documentation de référence

| Document | Chemin | Description |
|----------|--------|-------------|
| Contexte et vision | `specifications/docs/01-contexte-vision.md` | Objectifs du projet |
| User stories | `specifications/docs/03-user-stories-user-flows.md` | Parcours utilisateur |
| Spécifications fonctionnelles | `specifications/docs/04-specifications-fonctionnelles.md` | Détails fonctionnels |
| Architecture technique | `specifications/docs/06-architecture-techniques.md` | Stack et architecture |
| Code guidelines | `specifications/docs/07-code-guidelines.md` | Conventions de code |
| API Swagger | `specifications/swagger/api.yml` | Spécification API |
| Maquettes HTML | `specifications/maquettes/*.html` | Maquettes des écrans |
| Design System | `specifications/ux/02-design-system.html` | Composants UI |

---

## ⚠️ Instructions pour les agents IA

### Lors de modifications de code

1. **Respecter l'architecture MVVM** : UI → ViewModel → Repository → Remote
2. **Utiliser le Design System existant** : Composants dans `ui/theme/`
3. **Suivre les conventions Kotlin/Compose** définies dans `07-code-guidelines.md`
4. **Tester les modifications** avec `./gradlew build` ou `./gradlew assembleDebug`
5. **Vérifier la navigation** : Routes définies dans `navigation/`

### Pour ajouter une nouvelle fonctionnalité

1. Créer le modèle dans `domain/model/` si nécessaire
2. Ajouter les endpoints dans `data/remote/ApiService.kt`
3. Mettre à jour ou créer le Repository dans `data/repository/`
4. Créer/modifier le ViewModel dans `ui/viewmodel/`
5. Créer/modifier l'écran Compose dans `ui/screens/`
6. Mettre à jour la navigation si nécessaire

### Commandes utiles

```bash
# Build du projet
./gradlew assembleDebug

# Tests unitaires
./gradlew test

# Vérification du code
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

---

## 🔧 Dépendances principales

```toml
# Voir gradle/libs.versions.toml pour les versions exactes
- androidx-core-ktx
- androidx-lifecycle-runtime-ktx
- androidx-lifecycle-viewmodel-compose
- androidx-activity-compose
- androidx-compose-bom (Material 3)
- androidx-navigation-compose
- retrofit + okhttp
- kotlinx-serialization-json
```

---

## 📞 Contact

Projet de formation DSI - Centre Hospitalier

