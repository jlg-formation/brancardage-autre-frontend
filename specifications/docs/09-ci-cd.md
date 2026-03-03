# Intégration et Déploiement Continus (CI/CD)

Ce document décrit les pipelines d'intégration et de déploiement continus mis en place pour le projet de brancardage, couvrant à la fois l'application Android et le backend Ktor.

## 1. Objectifs de la CI/CD

- **Automatisation :** Réduire les tâches manuelles répétitives (compilation, tests, packaging).
- **Qualité :** Garantir que chaque modification de code respecte les standards de qualité (linting, tests unitaires).
- **Livraison Rapide :** Faciliter la distribution des versions de l'application aux testeurs ou aux formateurs.

## 2. Outils Utilisés

- **Plateforme CI/CD :** GitHub Actions (ou GitLab CI, selon l'infrastructure de la DSI).
- **Gestionnaire de Dépendances :** Gradle (pour Android et Ktor).
- **Analyse Statique :** Ktlint (pour le respect des conventions Kotlin), Android Lint.

## 3. Pipeline Front-end (Android)

Le pipeline Android est déclenché à chaque `push` ou `pull request` sur la branche principale (`main` ou `develop`).

### 3.1. Étapes du Pipeline (Build & Test)

1. **Checkout du Code :** Récupération du code source depuis le dépôt Git.
2. **Configuration de l'Environnement :** Installation du JDK (Java Development Kit) requis (ex: JDK 17).
3. **Cache Gradle :** Restauration du cache Gradle pour accélérer les builds ultérieurs.
4. **Analyse Statique (Linting) :** Exécution de `ktlint` et `Android Lint` pour vérifier la qualité du code.
5. **Tests Unitaires :** Exécution des tests unitaires locaux (`./gradlew testDebugUnitTest`).
6. **Compilation (Build) :** Génération de l'APK de debug (`./gradlew assembleDebug`).

### 3.2. Déploiement (Release - Optionnel)

- **Génération de l'APK/AAB Release :** Compilation de la version de production signée.
- **Distribution :** Envoi automatique de l'APK sur une plateforme de distribution interne (ex: Firebase App Distribution) pour les tests utilisateurs.

## 4. Pipeline Back-end (Ktor)

Le pipeline Ktor suit une logique similaire, adaptée à un projet serveur.

### 4.1. Étapes du Pipeline (Build & Test)

1. **Checkout du Code :** Récupération du code source.
2. **Configuration de l'Environnement :** Installation du JDK.
3. **Cache Gradle :** Restauration du cache.
4. **Analyse Statique :** Exécution de `ktlint`.
5. **Tests Unitaires et d'Intégration :** Exécution des tests Ktor (`./gradlew test`).
6. **Compilation (Build) :** Génération du fichier JAR exécutable (`./gradlew buildFatJar`).

### 4.2. Déploiement (Release - Optionnel)

- **Création d'Image Docker :** Construction d'une image Docker contenant le JAR Ktor.
- **Déploiement :** Poussée de l'image sur un registre (ex: Docker Hub, GitLab Container Registry) et déploiement sur un serveur de test ou de production.

## 5. Bonnes Pratiques

- **Rapidité :** Optimiser les temps de build en utilisant le cache Gradle et en parallélisant les tâches si possible.
- **Visibilité :** Configurer des notifications (ex: Slack, Email) en cas d'échec du pipeline pour une réaction rapide de l'équipe.
- **Sécurité :** Ne jamais stocker de secrets (clés d'API, mots de passe, keystores) en clair dans le code source. Utiliser les variables d'environnement sécurisées de la plateforme CI/CD.
