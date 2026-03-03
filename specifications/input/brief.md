# Spécifications du Projet : Application de Brancardage (Projet Fil Rouge)

## 1. Contexte et Objectifs

Ce projet sert de fil rouge pour une formation Kotlin de 4 jours destinée à des développeurs de la DSI d'un centre hospitalier.
L'objectif principal est la maîtrise du développement Android moderne en Kotlin (Front-end), avec une introduction au développement côté serveur en Kotlin (Back-end).

## 2. Architecture Globale

- **Front-end (Prioritaire) :** Application mobile Android native développée en Kotlin (idéalement avec Jetpack Compose).
- **Back-end (Secondaire) :** API REST développée avec le framework Ktor (Kotlin).

## 3. Spécifications Fonctionnelles (Front-end Android)

### 3.1. Identification et Recherche de Patient

L'utilisateur (personnel soignant) doit pouvoir identifier le patient à transporter.

- **Recherche manuelle :** Formulaire permettant de rechercher un patient par :
  - Nom et Prénom
  - Numéro de patient (IPP)
  - Numéro de sécurité sociale
- **Recherche par Scan :** Utilisation de l'appareil photo pour scanner le bracelet du patient (Code-barres ou QR Code) et récupérer automatiquement son dossier.

### 3.2. Gestion des Médias et Documents

L'application doit permettre d'enrichir la demande de brancardage avec des documents visuels.

- **Prise de photo :** Capturer une image directement depuis l'application (ex: état du patient, équipement spécifique requis).
- **Import depuis la galerie :** Sélectionner une photo existante sur le téléphone.
- **Scan de documents :** Prendre en photo un document médical (ex: fiche de transfert, ordonnance) avec une interface optimisée pour la lisibilité du document.

### 3.3. Commande d'un Brancardier (Workflow principal)

Une fois le patient identifié, l'utilisateur crée la demande de transport.

- **Localisation de départ (GPS) :** L'application utilise les services de localisation de l'appareil pour déterminer automatiquement (ou affiner manuellement) le lieu exact où le brancardier doit récupérer le patient.
- **Sélection de la destination :** Interface permettant de choisir le service ou la chambre de destination au sein de l'hôpital (via une liste déroulante ou une recherche).
- **Validation :** Récapitulatif de la demande (Patient, Médias, Départ, Arrivée) et envoi au Back-end.

## 4. Spécifications Techniques

### 4.1. Front-end (Android / Kotlin)

- **UI :** Jetpack Compose (recommandé pour une formation moderne) ou XML classique.
- **Asynchronisme :** Coroutines et Flow.
- **Réseau :** Retrofit ou Ktor Client pour communiquer avec l'API.
- **Fonctionnalités matérielles :**
  - _CameraX_ pour la prise de photo et le scan de documents.
  - _ML Kit_ ou _ZXing_ pour la lecture des QR Codes / Code-barres.
  - _Play Services Location_ pour la récupération des coordonnées GPS.

### 4.2. Back-end (Ktor / Kotlin)

- **Serveur :** Ktor Server avec Netty.
- **API :** Endpoints REST pour :
  - Rechercher un patient (Mock de base de données).
  - Recevoir et stocker une demande de brancardage.
  - Uploader des images/documents.
- **Base de données :** Base de données en mémoire (H2) ou liste statique pour simplifier la partie backend de la formation.

## 5. Proposition de Découpage Pédagogique (4 Jours)

- **Jour 1 :** Fondamentaux Kotlin, UI Android (Recherche patient et formulaires).
- **Jour 2 :** Architecture Android, Coroutines, et mise en place du Back-end Ktor (API de recherche).
- **Jour 3 :** Intégration matérielle (CameraX, Scan QR Code, Galerie photo).
- **Jour 4 :** Géolocalisation (GPS), finalisation de la commande, appels réseau (Ktor Client/Retrofit) et tests.
