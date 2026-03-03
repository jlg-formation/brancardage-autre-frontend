# ROADMAP - Application Brancardage

## Légende

- [ ] Tâche à faire
- [x] Tâche terminée

## Tâches (max 10)

### Étape 1 : Setup minimal & Écrans statiques

- [x] **id001** - Initialiser le projet Android + Design System de base
  - **Dépendances** : aucune
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [02-design-system.html](specifications/ux/02-design-system.html)
  - **Livrable** : Projet compilé avec thème personnalisé, typographies, couleurs, et composants réutilisables (Buttons, Cards, TextFields)

- [x] **id002** - Implémenter tous les écrans en statique (données mockées)
  - **Dépendances** : id001
  - **Docs** : [01-accueil.html](specifications/maquettes/01-accueil.html) → [09-confirmation.html](specifications/maquettes/09-confirmation.html), [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** : Les 9 écrans affichables avec données en dur :
    - 01-Accueil (menu principal)
    - 02-Recherche manuelle (formulaire)
    - 03-Scan bracelet (écran caméra)
    - 04-Dossier patient (affichage dossier)
    - 05-Médias (galerie + prise photo)
    - 06-Localisation (GPS/map)
    - 07-Destination (sélection destination)
    - 08-Récapitulatif (synthèse)
    - 09-Confirmation (feedback succès)

### Étape 2 : Navigation & Parcours utilisateur

- [x] **id003** - Connecter les écrans avec Navigation Compose
  - **Dépendances** : id002
  - **Docs** : [03-user-stories-user-flows.md](specifications/docs/03-user-stories-user-flows.md), [gradle/libs.versions.toml](gradle/libs.versions.toml)
  - **Livrable** : Parcours complet navigable :
    - Accueil → Recherche manuelle OU Scan bracelet
    - Résultats recherche → Dossier patient
    - Dossier patient → Médias → Localisation → Destination → Récapitulatif → Confirmation
    - Retour à l'accueil depuis confirmation

### Étape 3 : Logique métier & ViewModels

- [x] **id004** - Ajouter les ViewModels et gestion d'état (State Management)
  - **Dépendances** : id003
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** : 
    - ViewModel pour chaque écran (SearchViewModel, PatientViewModel, MediaViewModel, etc.)
    - Gestion d'état partagée (patient sélectionné, médias, localisation, destination)
    - Data classes pour les modèles (Patient, Media, Location, BrancardageRequest)

### Étape 4 : Intégration API & Recherche de patient

- [x] **id005** - Créer couche réseau (Retrofit + Repository) et connecter recherche manuelle
  - **Dépendances** : id004
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [swagger/api.yml](specifications/swagger/api.yml)
  - **Livrable** :
    - Ajouter Retrofit et Kotlinx Serialization aux dépendances
    - Créer ApiService avec endpoint `GET /patients` (mock en hardcoding)
    - Créer PatientRepository
    - Connecter SearchViewModel à l'API
    - Afficher les résultats dynamiques de la recherche

### Étape 5 : Caméra & Scan de Code-barres/QR Code

- [x] **id006** - Intégrer CameraX et ML Kit pour le scan de bracelet
  - **Dépendances** : id005
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** :
    - Ajouter CameraX, ML Kit Vision à build.gradle.kts
    - Implémenter ScanViewModel avec logique de détection QR/Code-barres
    - Écran 03-Scan bracelet fonctionnel : affiche caméra, détecte code, extrait IPP
    - Récupère automatiquement le patient après détection

### Étape 6 : Galerie photo & Prise de photo

- [ ] **id007** - Implémenter gestion des médias (appareil photo + galerie)
  - **Dépendances** : id006
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** :
    - ActivityResult Contracts pour caméra et galerie
    - MediaViewModel pour gérer liste de photos
    - Écran 05-Médias : affiche miniatures, boutons "Prendre photo" et "Import galerie"
    - Compression des images avant stockage

### Étape 7 : Géolocalisation (GPS)

- [ ] **id008** - Intégrer Play Services Location pour le GPS
  - **Dépendances** : id007
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** :
    - Ajouter Play Services Location aux dépendances
    - LocationViewModel avec Fused Location Provider
    - Permissions runtime pour ACCESS_FINE_LOCATION
    - Écran 06-Localisation : affiche position détectée, permet modification manuelle
    - Récupère destinations depuis API

### Étape 8 : Validation & Envoi de la demande

- [ ] **id009** - Implémenter récapitulatif, validation et POST de brancardage
  - **Dépendances** : id008
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [swagger/api.yml](specifications/swagger/api.yml)
  - **Livrable** :
    - Écran 08-Récapitulatif : affiche patient, médias, lieu départ, destination
    - BrancardageViewModel : valide et formate les données
    - POST `/brancardage` + upload des médias
    - Écran 09-Confirmation : affiche succès/erreur
    - Gestion des erreurs réseau

### Étape 9 : Tests unitaires & tests d'intégration

- [ ] **id010** - Ajouter tests (ViewModels, Repository, Use Cases)
  - **Dépendances** : id009
  - **Docs** : [08-tests-unitaire-et-e2e.md](specifications/docs/08-tests-unitaire-et-e2e.md), [07-code-guidelines.md](specifications/docs/07-code-guidelines.md)
  - **Livrable** :
    - Tests unitaires pour SearchViewModel, MediaViewModel, LocationViewModel
    - Tests d'intégration pour PatientRepository
    - Mock de l'API avec FakePatientRepository
    - Couverture minimale : 70% des ViewModels
    - Tests E2E pour le parcours principal

### Étape 10 : CI/CD & Polish final

- [ ] **id011** - Mettre en place CI/CD et optimisations
  - **Dépendances** : id010
  - **Docs** : [09-ci-cd.md](specifications/docs/09-ci-cd.md), [07-code-guidelines.md](specifications/docs/07-code-guidelines.md)
  - **Livrable** :
    - GitHub Actions (build, lint, tests)
    - ProGuard/R8 configuration pour release
    - Documentation code (KDoc)
    - Optimisations performance (lazy composition, etc.)
    - Artefacts APK signés

---

## État du Projet

**Phase actuelle :** Étape 5 - Caméra & Scan de Code-barres/QR Code

**État du code :**
- ✅ Projet Android compilable
- ✅ Jetpack Compose configuré
- ✅ Design System implémenté (thème, couleurs, typographies, composants)
- ✅ 9 écrans métier implémentés (données mockées)
- ✅ Navigation Compose configurée (parcours complet navigable)
- ✅ ViewModels créés (SearchViewModel, PatientViewModel, MediaViewModel, LocationViewModel, DestinationViewModel, ScanViewModel, BrancardageViewModel)
- ✅ Modèles de données (Patient, Media, Localisation, Destination, BrancardageRequest)
- ✅ Gestion d'état partagée (BrancardageSessionState)
- ✅ Couche réseau (Retrofit + OkHttp + Kotlinx Serialization)
- ✅ Repositories (PatientRepository, DestinationRepository) avec données mockées
- ✅ ViewModels connectés aux Repositories

**Prochaines étapes prioritaires :**
1. Implémenter id006 : Intégrer CameraX et ML Kit pour le scan de bracelet
2. Implémenter id007 : Gestion des médias (appareil photo + galerie)
3. Implémenter id008 : Intégrer Play Services Location pour le GPS

