# Architecture Decision Records (ADR)

Ce document recense les décisions d'architecture majeures prises pour le projet de brancardage, suite aux clarifications apportées.

## ADR-001 : Choix du framework UI Android

**Date :** 21 Février 2026
**Statut :** Accepté

**Contexte :**
Le projet sert de fil rouge pour une formation Kotlin moderne. Il est nécessaire de choisir le framework de création d'interface utilisateur le plus pertinent pédagogiquement et techniquement.

**Décision :**
Nous utiliserons **Jetpack Compose**.

**Justification :**
Jetpack Compose est le standard actuel et futur recommandé par Google pour le développement d'UI natives sur Android. Son approche déclarative s'intègre parfaitement avec Kotlin et les Coroutines/Flow, offrant une expérience d'apprentissage moderne et cohérente avec les pratiques de l'industrie.

---

## ADR-002 : Choix du client réseau Android

**Date :** 21 Février 2026
**Statut :** Accepté

**Contexte :**
L'application Android doit communiquer avec une API REST (développée en Ktor). Il faut sélectionner une librairie robuste pour gérer ces appels réseau.

**Décision :**
Nous utiliserons **Retrofit**.

**Justification :**
Bien que Ktor Client soit une option cohérente avec le backend, Retrofit reste le standard absolu de l'industrie pour le développement Android. Sa maîtrise est une compétence indispensable pour les développeurs de la DSI. Il s'intègre par ailleurs très bien avec les Coroutines Kotlin.

---

## ADR-003 : Choix de la librairie de scan (QR Code / Code-barres)

**Date :** 21 Février 2026
**Statut :** Accepté

**Contexte :**
L'application doit permettre de scanner les bracelets des patients (QR Code ou Code-barres) pour les identifier rapidement.

**Décision :**
Nous utiliserons **Google ML Kit**.

**Justification :**
Google ML Kit offre des performances de reconnaissance supérieures, une intégration moderne et est activement maintenu par Google. Il est plus adapté aux standards actuels que des solutions plus anciennes comme ZXing, et simplifie l'implémentation par rapport à une analyse d'image manuelle via CameraX.

---

## ADR-004 : Choix de la persistance des données Backend

**Date :** 21 Février 2026
**Statut :** Accepté

**Contexte :**
Le backend Ktor, développé dans le cadre de la formation, a besoin d'une solution de stockage simple pour persister les données (patients, demandes de brancardage).

**Décision :**
Nous utiliserons des **fichiers JSON**.

**Justification :**
L'objectif principal de la formation est le développement Android, le backend étant secondaire. L'utilisation de fichiers JSON permet de simuler une persistance de données sans la complexité d'installation et de configuration d'une véritable base de données (même en mémoire comme H2). C'est une solution légère, facile à inspecter et à réinitialiser pour les besoins pédagogiques.
