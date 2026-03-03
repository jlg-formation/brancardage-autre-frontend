# Stratégie de Tests (Unitaires et E2E)

Ce document décrit la stratégie de tests mise en place pour garantir la qualité et la fiabilité de l'application de brancardage, tant côté Front-end (Android) que Back-end (Ktor).

## 1. Objectifs de la Stratégie de Tests

- **Validation Fonctionnelle :** S'assurer que les fonctionnalités critiques (recherche patient, scan, commande) fonctionnent comme prévu.
- **Non-Régression :** Prévenir l'introduction de bugs lors des évolutions futures.
- **Pédagogie :** Démontrer aux développeurs en formation les bonnes pratiques de test dans l'écosystème Kotlin moderne.

## 2. Tests Front-end (Android)

### 2.1. Tests Unitaires (Logique Métier)

- **Outils :** JUnit 4/5, MockK (pour le mocking), Kotlin Coroutines Test.
- **Périmètre :**
  - **ViewModels :** Vérifier les transitions d'état (StateFlow) suite aux actions de l'utilisateur.
  - **UseCases / Repositories :** Valider la logique de traitement des données et la gestion des erreurs réseau (Retrofit).
- **Exemple :** Tester que la recherche d'un patient avec un IPP invalide déclenche bien un état d'erreur dans le ViewModel.

### 2.2. Tests d'Interface Utilisateur (UI Tests)

- **Outils :** Compose Testing Library (`compose-test-rule`).
- **Périmètre :**
  - Vérifier le rendu correct des composants (Composables) en fonction de différents états (chargement, succès, erreur).
  - Simuler des interactions utilisateur (clics, saisie de texte) et vérifier les changements d'état visuels.
- **Exemple :** Vérifier que le bouton "Valider la demande" est désactivé tant qu'aucune destination n'est sélectionnée.

### 2.3. Tests d'Intégration (Optionnels pour la formation)

- **Outils :** Espresso (si interaction avec des vues XML legacy), UI Automator.
- **Périmètre :** Tester les flux complets (User Flows) sur un émulateur ou un appareil physique.

## 3. Tests Back-end (Ktor)

### 3.1. Tests d'API (Intégration)

- **Outils :** Ktor Server Test Application (`testApplication`), JUnit.
- **Périmètre :**
  - Vérifier le bon fonctionnement des endpoints REST (Routing).
  - Valider la sérialisation/désérialisation JSON.
  - S'assurer que les codes HTTP retournés sont corrects (200 OK, 400 Bad Request, 404 Not Found).
- **Exemple :** Envoyer une requête `POST /brancardage` avec un payload valide et vérifier que la réponse est `201 Created` et que les données sont bien persistées dans le fichier JSON de test.

### 3.2. Tests Unitaires (Logique Serveur)

- **Outils :** JUnit, MockK.
- **Périmètre :** Tester la logique interne des services ou repositories Ktor (ex: validation des données avant écriture dans le fichier JSON).

## 4. Bonnes Pratiques

- **Isolation :** Chaque test doit être indépendant et ne pas dépendre de l'état laissé par un test précédent.
- **Nommage :** Utiliser des noms de méthodes descriptifs (ex: `givenInvalidIpp_whenSearchPatient_thenReturnsErrorState`).
- **Mocking :** Mocker les dépendances externes (API réseau, base de données, capteurs matériels) pour garantir la rapidité et la fiabilité des tests unitaires.
