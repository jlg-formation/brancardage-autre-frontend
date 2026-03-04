# 🏥 HuyBrancardage - Application Mobile Android

Application mobile Android native de gestion des demandes de brancardage hospitalier, développée en **Kotlin** avec **Jetpack Compose**.

## 📋 Table des matières

- [Présentation](#-présentation)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Lancer l'application](#-lancer-lapplication)
- [Structure du projet](#-structure-du-projet)
- [Tests](#-tests)
- [CI/CD](#-cicd)
- [Technologies utilisées](#-technologies-utilisées)
- [Documentation](#-documentation)

---

## 🎯 Présentation

Cette application permet au personnel soignant hospitalier de :

- **Rechercher un patient** (par nom, prénom, IPP ou numéro de sécurité sociale)
- **Scanner le bracelet patient** (code-barres / QR code)
- **Consulter le dossier patient**
- **Ajouter des médias** (photos via caméra ou galerie)
- **Géolocaliser la position actuelle** (GPS)
- **Sélectionner une destination** (service de l'établissement)
- **Créer une demande de brancardage**

### Écrans de l'application

| Écran | Description |
|-------|-------------|
| Accueil | Menu principal avec accès aux fonctionnalités |
| Recherche manuelle | Formulaire de recherche patient |
| Scan bracelet | Scanner de code-barres/QR avec CameraX |
| Dossier patient | Affichage des informations patient |
| Médias | Gestion des photos (prise de vue / galerie) |
| Localisation | Position GPS avec modification manuelle |
| Destination | Sélection du service de destination |
| Récapitulatif | Synthèse de la demande avant envoi |
| Confirmation | Confirmation de création avec numéro de suivi |

---

## 🔧 Prérequis

### Logiciels requis

| Logiciel | Version minimale | Recommandée |
|----------|------------------|-------------|
| **Android Studio** | Ladybug (2024.2.1) | Dernière version stable |
| **JDK** | 11 | 17 |
| **Gradle** | 8.x | 8.11+ (via wrapper) |
| **Android SDK** | API 31 (Android 12) | API 36 |

### Configuration système

- **RAM** : 8 Go minimum (16 Go recommandé)
- **Espace disque** : 10 Go minimum pour Android Studio + SDK
- **OS** : Windows 10+, macOS 10.14+, ou Linux (Ubuntu 18.04+)

---

## 📥 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/votre-organisation/HuyBrancardage.git
cd HuyBrancardage
```

### 2. Ouvrir dans Android Studio

1. Lancer **Android Studio**
2. Sélectionner **File > Open**
3. Naviguer vers le dossier `HuyBrancardage`
4. Cliquer sur **OK**
5. Attendre la synchronisation Gradle (peut prendre quelques minutes)

### 3. Synchroniser les dépendances

Android Studio devrait automatiquement synchroniser les dépendances Gradle. Si ce n'est pas le cas :

- Cliquer sur **File > Sync Project with Gradle Files**
- Ou exécuter en ligne de commande :

```bash
# Windows
.\gradlew.bat build

# macOS / Linux
./gradlew build
```

---

## ⚙️ Configuration

### Configuration du Backend

L'application nécessite une API REST backend. L'URL du backend est configurable via le fichier `gradle.properties` ou en ligne de commande.

#### Option 1 : Modifier `gradle.properties`

Décommenter et modifier les lignes suivantes dans `gradle.properties` :

```properties
# URL pour les builds debug (émulateur Android)
DEBUG_BACKEND_URL=http://10.0.2.2:8080/api/v1/

# URL pour les builds release (production)
RELEASE_BACKEND_URL=https://api.votre-domaine.com/api/v1/
```

> **Note** : `10.0.2.2` est l'alias pour `localhost` depuis l'émulateur Android.

#### Option 2 : Passer en paramètre Gradle

```bash
# Debug avec backend local
.\gradlew.bat assembleDebug -PDEBUG_BACKEND_URL="http://10.0.2.2:8080/api/v1/"

# Release avec backend production
.\gradlew.bat assembleRelease -PRELEASE_BACKEND_URL="https://api.production.com/api/v1/"
```

#### Option 3 : Appareil physique via USB

Si vous testez sur un appareil physique connecté en USB, utilisez `adb reverse` :

```bash
adb reverse tcp:8080 tcp:8080
```

Puis configurez l'URL comme `http://localhost:8080/api/v1/`

### Fichier `local.properties`

Ce fichier est généré automatiquement par Android Studio et contient le chemin vers votre SDK Android. **Ne pas versionner ce fichier**.

Exemple :
```properties
sdk.dir=C\:\\Users\\VotreNom\\AppData\\Local\\Android\\Sdk
```

---

## 🚀 Lancer l'application

### Sur émulateur

1. **Créer un émulateur** (si pas encore fait) :
   - **Tools > Device Manager > Create Device**
   - Sélectionner un appareil (ex: Pixel 6)
   - Sélectionner une image système API 31+ (Android 12+)
   - Terminer la création

2. **Lancer l'émulateur** :
   - Cliquer sur le bouton ▶️ à côté de l'émulateur dans le Device Manager

3. **Exécuter l'application** :
   - Cliquer sur **Run > Run 'app'** (ou `Shift + F10`)
   - Ou utiliser la ligne de commande :

```bash
.\gradlew.bat installDebug
```

### Sur appareil physique

1. **Activer le mode développeur** sur l'appareil :
   - Paramètres > À propos du téléphone > Taper 7 fois sur "Numéro de build"

2. **Activer le débogage USB** :
   - Paramètres > Options développeur > Débogage USB

3. **Connecter l'appareil** via USB et accepter la demande de débogage

4. **Exécuter l'application** via Android Studio ou :

```bash
.\gradlew.bat installDebug
```

### Permissions requises

L'application demandera les permissions suivantes au runtime :

| Permission | Utilisation |
|------------|-------------|
| `CAMERA` | Scan de code-barres et prise de photos |
| `ACCESS_FINE_LOCATION` | Géolocalisation GPS |
| `ACCESS_COARSE_LOCATION` | Localisation approximative |

---

## 📁 Structure du projet

```
HuyBrancardage/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/huybrancardage/
│   │   │   │   ├── data/           # Couche données (API, Repository)
│   │   │   │   │   ├── remote/     # API Retrofit
│   │   │   │   │   └── repository/ # Repositories
│   │   │   │   ├── domain/         # Modèles de domaine
│   │   │   │   ├── navigation/     # Navigation Compose
│   │   │   │   ├── ui/
│   │   │   │   │   ├── camera/     # Composants CameraX
│   │   │   │   │   ├── screens/    # Écrans de l'application
│   │   │   │   │   ├── state/      # États partagés
│   │   │   │   │   ├── theme/      # Design System (couleurs, typo)
│   │   │   │   │   └── viewmodel/  # ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                # Ressources Android
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                   # Tests unitaires
│   │   └── androidTest/            # Tests instrumentés
│   └── build.gradle.kts            # Config module app
├── gradle/
│   ├── libs.versions.toml          # Catalogue de versions
│   └── wrapper/
├── specifications/                  # Documentation fonctionnelle
│   ├── docs/                       # Spécifications techniques
│   ├── maquettes/                  # Maquettes HTML
│   ├── swagger/                    # API specification (OpenAPI)
│   └── ux/                         # Design system
├── build.gradle.kts                # Config projet racine
├── gradle.properties               # Propriétés Gradle
├── settings.gradle.kts
└── ROADMAP.md                      # Roadmap du projet
```

---

## 🧪 Tests

### Exécuter les tests unitaires

```bash
.\gradlew.bat testDebugUnitTest
```

Les rapports de tests sont générés dans :
`app/build/reports/tests/testDebugUnitTest/index.html`

### Exécuter les tests instrumentés (sur émulateur/appareil)

```bash
.\gradlew.bat connectedDebugAndroidTest
```

### Couverture de tests

Le projet vise une couverture minimale de **70%** sur les ViewModels.

Tests implémentés :
- ✅ Tests unitaires ViewModels (SearchViewModel, MediaViewModel, LocationViewModel, etc.)
- ✅ Tests Repository (PatientRepository, DestinationRepository)
- ✅ Tests E2E avec Compose Testing Library

---

## 🔄 CI/CD

Le projet utilise **GitHub Actions** pour l'intégration continue.

### Pipeline automatisé

Le workflow `.github/workflows/android-ci.yml` s'exécute sur :
- Push sur `main` ou `develop`
- Pull requests vers `main` ou `develop`

### Étapes du pipeline

1. **Checkout** du code
2. **Setup JDK 17**
3. **Ktlint** - Analyse statique du code Kotlin
4. **Android Lint** - Détection de problèmes Android
5. **Tests unitaires**
6. **Build APK Debug**
7. **Build APK Release** (uniquement sur `main`)

### Artifacts générés

- APK Debug
- APK Release (minifié avec R8/ProGuard)
- Rapports de tests
- Rapports de lint

---

## 🛠 Technologies utilisées

### Langage & Framework

| Technologie | Version | Description |
|-------------|---------|-------------|
| **Kotlin** | 2.0.21 | Langage principal |
| **Jetpack Compose** | BOM 2024.09.00 | UI déclarative |
| **Android Gradle Plugin** | 9.0.1 | Build system |

### Architecture & Navigation

| Bibliothèque | Version | Usage |
|--------------|---------|-------|
| **Navigation Compose** | 2.8.9 | Navigation entre écrans |
| **Lifecycle ViewModel** | 2.10.0 | Gestion d'état |
| **Lifecycle Runtime Compose** | 2.10.0 | Collecte d'états |

### Réseau

| Bibliothèque | Version | Usage |
|--------------|---------|-------|
| **Retrofit** | 2.11.0 | Client HTTP |
| **OkHttp** | 4.12.0 | HTTP client + logging |
| **Kotlinx Serialization** | 1.7.3 | Sérialisation JSON |

### Fonctionnalités natives

| Bibliothèque | Version | Usage |
|--------------|---------|-------|
| **CameraX** | 1.4.1 | Accès caméra |
| **ML Kit Barcode** | 17.3.0 | Scan code-barres/QR |
| **Play Services Location** | 21.3.0 | GPS |
| **Coil** | 2.6.0 | Chargement d'images |

### Tests

| Bibliothèque | Version | Usage |
|--------------|---------|-------|
| **JUnit** | 4.13.2 | Tests unitaires |
| **MockK** | 1.13.12 | Mocking Kotlin |
| **Coroutines Test** | 1.8.1 | Tests asynchrones |
| **Compose UI Test** | - | Tests UI |

### Qualité de code

| Outil | Usage |
|-------|-------|
| **Ktlint** | Linting Kotlin |
| **Android Lint** | Analyse statique Android |
| **ProGuard/R8** | Minification release |

---

## 📚 Documentation

### Spécifications fonctionnelles

| Document | Description |
|----------|-------------|
| [01-contexte-vision.md](specifications/docs/01-contexte-vision.md) | Contexte et objectifs |
| [02-persona-parcours.md](specifications/docs/02-persona-parcours.md) | Personas utilisateurs |
| [03-user-stories-user-flows.md](specifications/docs/03-user-stories-user-flows.md) | User stories et parcours |
| [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md) | Specs fonctionnelles détaillées |

### Spécifications techniques

| Document | Description |
|----------|-------------|
| [05-architecture-decision-record.md](specifications/docs/05-architecture-decision-record.md) | Décisions d'architecture |
| [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md) | Architecture technique |
| [07-code-guidelines.md](specifications/docs/07-code-guidelines.md) | Conventions de code |
| [08-tests-unitaire-et-e2e.md](specifications/docs/08-tests-unitaire-et-e2e.md) | Stratégie de tests |

### API Backend

| Document | Description |
|----------|-------------|
| [api.yml](specifications/swagger/api.yml) | Spécification OpenAPI 3.0 |

### Design System

| Document | Description |
|----------|-------------|
| [01-mood-board.html](specifications/ux/01-mood-board.html) | Inspiration visuelle |
| [02-design-system.html](specifications/ux/02-design-system.html) | Composants UI |
| [Maquettes](specifications/maquettes/) | Maquettes HTML des écrans |

---

## 🐛 Dépannage

### Erreurs courantes

#### 1. `SDK location not found`

Créer un fichier `local.properties` à la racine avec :
```properties
sdk.dir=C\:\\Users\\VotreNom\\AppData\\Local\\Android\\Sdk
```

#### 2. Erreur de synchronisation Gradle

```bash
.\gradlew.bat clean
.\gradlew.bat --refresh-dependencies
```

#### 3. Émulateur ne démarre pas

- Vérifier que la virtualisation (Intel VT-x / AMD-V) est activée dans le BIOS
- Augmenter la RAM allouée à l'émulateur

#### 4. Permission refusée sur Linux/macOS

```bash
chmod +x gradlew
```

#### 5. L'API ne répond pas

- Vérifier que le backend est lancé
- Vérifier l'URL dans `gradle.properties`
- Pour l'émulateur : utiliser `10.0.2.2` au lieu de `localhost`

---

## 📄 Licence

Ce projet est propriétaire - © Centre Hospitalier

---

## 👥 Contact

Pour toute question technique, contacter l'équipe DSI.

