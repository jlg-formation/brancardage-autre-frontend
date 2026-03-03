# Architecture Technique

## 1. Architecture Globale

Le système repose sur une architecture client-serveur classique :

- **Client :** Une application mobile Android native (Front-end).
- **Serveur :** Une API REST (Back-end) hébergée sur un serveur.

La communication entre les deux entités s'effectue via le protocole HTTP/HTTPS, avec des échanges de données au format JSON.

## 2. Front-end (Application Android)

L'application Android est développée en Kotlin et suit les recommandations d'architecture de Google (Guide to App Architecture).

### 2.1. Modèle d'Architecture (MVVM)

- **UI Layer (Couche de Présentation) :**
  - **Vues :** Développées avec **Jetpack Compose** (UI déclarative).
  - **State Holders :** Utilisation de `ViewModel` pour gérer l'état de l'interface utilisateur et la logique de présentation.
- **Domain Layer (Couche Métier - Optionnelle) :**
  - Encapsulation de la logique métier complexe dans des `UseCases` (ex: validation d'une demande de brancardage).
- **Data Layer (Couche de Données) :**
  - **Repositories :** Point d'accès unique aux données (abstraction de la source).
  - **Data Sources :**
    - _Remote :_ Appels réseau via **Retrofit**.
    - _Local :_ (Si nécessaire pour du cache, ex: Room ou DataStore).

### 2.2. Stack Technique Principale

- **Langage :** Kotlin.
- **UI :** Jetpack Compose.
- **Asynchronisme & Réactivité :** Coroutines et Kotlin Flow.
- **Réseau :** Retrofit (avec OkHttp et un convertisseur JSON comme Moshi ou Kotlinx Serialization).


### 2.3. Intégrations Matérielles

- **Appareil Photo :** **CameraX** pour la prise de photos et le scan de documents.
- **Scan de Code-barres/QR Code :** **Google ML Kit** (Vision API).
- **Géolocalisation :** **Play Services Location** (Fused Location Provider) pour récupérer les coordonnées GPS.

## 3. Back-end (API REST)

Le backend est conçu pour être léger et pédagogique, servant principalement à valider les concepts vus côté client.

### 3.1. Stack Technique

- **Langage :** Kotlin.
- **Framework Serveur :** **Ktor Server** (avec le moteur Netty).
- **Sérialisation :** Kotlinx Serialization (pour le parsing JSON).

### 3.2. Architecture et Persistance

- **Structure :** Découpage par "Features" Ktor (Routing, Content Negotiation, etc.).
- **Endpoints :**
  - `GET /patients` : Recherche de patients.
  - `GET /patients/{id}` : Récupération d'un dossier patient.
  - `POST /brancardage` : Création d'une demande de transport.
  - `POST /upload` : Envoi de médias (photos/documents).
- **Persistance des Données :** Stockage dans des **fichiers JSON** locaux. Cette approche simplifie le déploiement et la réinitialisation des données pour les sessions de formation, évitant la configuration d'une base de données relationnelle.
