# Code Guidelines

Ce document définit les standards de développement à respecter pour le projet de brancardage, tant pour le Front-end (Android) que pour le Back-end (Ktor).

## 1. Conventions Générales Kotlin

- **Style Guide Officiel :** Suivre les recommandations de style de Kotlin (indentation, nommage, etc.).
- **Null Safety :** Utiliser les types non-nullables par défaut. Ne recourir aux types nullables (`?`) que lorsque l'absence de valeur est sémantiquement justifiée.
- **Immutabilité :** Privilégier `val` à `var`. Utiliser des collections immuables (`List`, `Map`) sauf nécessité absolue de modification.
- **Fonctions d'Extension :** Les utiliser pour enrichir des classes existantes sans héritage, mais avec parcimonie pour ne pas polluer l'espace de noms.

## 2. Guidelines Front-end (Android / Jetpack Compose)

### 2.1. Architecture UI (Compose)

- **State Hoisting :** Remonter l'état (State) au composant parent le plus pertinent. Les composants enfants doivent être "Stateless" (sans état interne) et recevoir leurs données via des paramètres, et émettre des événements via des lambdas.
- **Nommage des Composables :** Utiliser le PascalCase (ex: `PatientProfileCard`). Les fonctions Composable qui retournent `Unit` doivent être des verbes ou des noms décrivant l'UI.
- **Prévisualisation :** Créer des fonctions `@Preview` pour chaque composant UI significatif, en fournissant des données mockées pour faciliter le développement itératif.

### 2.2. Gestion de l'État et Asynchronisme

- **ViewModel :** Exposer l'état de l'UI sous forme de `StateFlow` (ex: `uiState: StateFlow<MyUiState>`).
- **Coroutines :**
  - Ne jamais bloquer le thread principal (Main Thread).
  - Utiliser les `Dispatchers` appropriés (`Dispatchers.IO` pour le réseau/disque, `Dispatchers.Default` pour le calcul intensif).
  - Lancer les coroutines dans le `viewModelScope` ou le `lifecycleScope`.
- **Collecte de Flow dans Compose :** Utiliser `collectAsStateWithLifecycle()` pour observer les flux de données en respectant le cycle de vie de l'activité/fragment.

### 2.3. Appels Réseau (Retrofit)

- **Interfaces :** Définir clairement les endpoints dans des interfaces Retrofit.
- **Gestion des Erreurs :** Encapsuler les réponses réseau dans des classes scellées (`sealed class Result<T>`) pour gérer explicitement les succès et les erreurs (ex: `Success`, `Error`, `Loading`).

## 3. Guidelines Back-end (Ktor)

### 3.1. Structure du Serveur

- **Modularité :** Découper l'application en modules Ktor (ex: `Application.module()`) pour faciliter les tests et la maintenance.
- **Routage :** Organiser les routes par domaine fonctionnel (ex: `patientRoutes()`, `brancardageRoutes()`) dans des fichiers séparés.

### 3.2. Gestion des Données (JSON)

- **Sérialisation :** Utiliser `kotlinx.serialization` pour mapper les objets Kotlin en JSON et inversement.
- **Accès aux Fichiers :** Isoler la logique de lecture/écriture des fichiers JSON dans des classes dédiées (Repositories) pour ne pas coupler le routage à la persistance.
