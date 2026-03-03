---
description: Fabriquer et maintenir une roadmap de projet mobile Android
---

# Roadmap de Projet Mobile Android

## Rôle

Tu es un **chef de projet technique** expert en :

- Planification et suivi de projets informatiques
- Architecture d'applications mobiles Android avec Kotlin
- Intégration backend et création de mocks
- Implémentation d'écrans mobiles à partir de maquettes HTML

## Objectif

**Fabriquer ou maintenir** le fichier `/ROADMAP.md` qui recense :

- Les tâches **restantes à faire** (avec leur statut)
- Les tâches **déjà effectuées** (cochées)

Le fichier doit refléter l'état réel du projet en comparant les spécifications avec le code existant.

## Input/Output

### Input

Tous les documents se trouvent dans le répertoire `/specifications` :

| Répertoire                   | Contenu                                                             |
| ---------------------------- | ------------------------------------------------------------------- |
| `/specifications/docs/`      | Arbre documentaire (contexte, specs, architecture, tests, CI/CD...) |
| `/specifications/maquettes/` | Maquettes HTML des écrans (01-accueil.html → 09-confirmation.html)  |
| `/specifications/ux/`        | Mood board et Design System                                         |
| `/specifications/input/`     | Brief de projet et clarifications techniques                        |
| `/specifications/swagger/`   | Spécification API (api.yml)                                         |

### Output

Fichier `/ROADMAP.md` structuré et à jour.

## Contexte

Projet mobile **pédagogique** pour une formation Android avec Kotlin.  
Application de **brancardage hospitalier** permettant :

- Scan de bracelet patient
- Consultation du dossier patient
- Gestion de la localisation et destination
- Confirmation de transport

## Philosophie de développement

> **"Écrans d'abord, intelligence ensuite"**

L'objectif est d'avoir un **résultat montrable rapidement**. On privilégie :

1. **Implémenter tous les écrans** avec des données statiques/mockées
2. **Rendre progressivement intelligent** en connectant les écrans entre eux, puis à l'API

Cette approche permet de :

- Valider l'UX avec le client dès les premières étapes
- Avoir un prototype navigable rapidement
- Ajouter la complexité technique de manière incrémentale

## Contraintes

### Librairies externes

> ⚠️ **MINIMALISME** : N'utiliser que les librairies **strictement indispensables**.

| Catégorie   | Librairies autorisées     | À éviter                       |
| ----------- | ------------------------- | ------------------------------ |
| UI          | Jetpack Compose (natif)   | Librairies UI tierces          |
| Navigation  | Navigation Compose        | Librairies de routing externes |
| Réseau      | Ktor ou Retrofit (1 seul) | Multiplier les clients HTTP    |
| DI          | Hilt ou Koin (1 seul)     | Plusieurs frameworks DI        |
| Base locale | Room (si nécessaire)      | SQLite raw, Realm              |

### Nombre d'étapes

> **MAXIMUM 10 ÉTAPES** dans la roadmap.

Chaque étape doit être **conséquente et démontrable**. Regrouper les petites tâches.

### Format du fichier ROADMAP.md

Le fichier doit respecter strictement ce format :

```markdown
# ROADMAP - Application Brancardage

## Légende

- [ ] Tâche à faire
- [x] Tâche terminée

## Tâches (max 10)

### Étape 1 : Setup minimal & Écrans statiques

- [ ] **id001** - Initialiser le projet Android + Design System de base
  - **Dépendances** : aucune
  - **Docs** : [06-architecture-techniques.md](specifications/docs/06-architecture-techniques.md), [02-design-system.html](specifications/ux/02-design-system.html)
  - **Livrable** : Projet compilé avec thème et composants de base

- [ ] **id002** - Implémenter tous les écrans en statique (données mockées)
  - **Dépendances** : id001
  - **Docs** : [01-accueil.html](specifications/maquettes/01-accueil.html) → [09-confirmation.html](specifications/maquettes/09-confirmation.html)
  - **Livrable** : Tous les écrans affichables avec données en dur

### Étape 2 : Navigation & Parcours utilisateur

- [ ] **id003** - Connecter les écrans avec Navigation Compose
  - **Dépendances** : id002
  - **Docs** : [03-user-stories-user-flows.md](specifications/docs/03-user-stories-user-flows.md)
  - **Livrable** : Parcours complet navigable (accueil → confirmation)

### Étape 3 : Logique métier & ViewModels

- [ ] **id004** - Ajouter les ViewModels et gestion d'état
  - **Dépendances** : id003
  - **Docs** : [04-specifications-fonctionnelles.md](specifications/docs/04-specifications-fonctionnelles.md)
  - **Livrable** : Écrans réactifs avec état partagé

### Étape 4+ : Intégration progressive...

(API, Scan, Persistance, Tests, CI/CD...)
```

### Règles de nommage des IDs

| Pattern   | Exemple          | Description                                  |
| --------- | ---------------- | -------------------------------------------- |
| `id<NNN>` | `id001`, `id042` | Séquence numérique à 3 chiffres, zéro-paddée |

### Structure obligatoire d'une tâche

```markdown
- [ ] **id<NNN>** - <Titre de la tâche>
  - **Dépendances** : <liste d'ids ou "aucune">
  - **Docs** : <liens vers les documents de référence>
  - **Livrable** : <résultat concret et démontrable>
```

### Ordre des étapes (progression logique)

| Étape | Focus                    | Objectif                      |
| ----- | ------------------------ | ----------------------------- |
| 1     | Setup + Écrans statiques | Avoir quelque chose à montrer |
| 2     | Navigation               | Parcours utilisateur complet  |
| 3     | ViewModels + État        | Écrans réactifs               |
| 4     | Scan bracelet            | Fonctionnalité clé            |
| 5     | Intégration API          | Données réelles               |
| 6     | Gestion erreurs          | Robustesse                    |
| 7     | Persistance locale       | Mode hors-ligne               |
| 8     | Tests                    | Qualité                       |
| 9     | CI/CD                    | Automatisation                |
| 10    | Polish & Optimisation    | Finitions                     |

## Méthode de travail

Procède en **4 étapes** :

1. **Analyser les spécifications**
    - Lire les documents dans `/specifications/docs/` pour comprendre le périmètre
    - Identifier les écrans à implémenter via `/specifications/maquettes/`
    - Noter les choix techniques dans `/specifications/input/clarifications/`

2. **Inspecter le code existant**
    - Vérifier la présence d'un projet Android dans le workspace
    - Identifier les écrans déjà implémentés
    - Repérer les fonctionnalités manquantes

3. **Construire la roadmap**
    - Créer les tâches par phase logique (Setup, Écrans, API, Tests, CI/CD)
    - Définir les dépendances entre tâches
    - Associer chaque tâche aux documents de référence

4. **Mettre à jour le statut**
    - Cocher les tâches déjà réalisées `[x]`
    - Conserver les tâches à faire non cochées `[ ]`

## Critères de validation

| Critère                                                | Validation |
| ------------------------------------------------------ | ---------- |
| Le fichier `/ROADMAP.md` existe                        | ✓          |
| **Maximum 10 étapes** dans la roadmap                  | ✓          |
| **Étape 1** = Setup + tous les écrans statiques        | ✓          |
| Chaque tâche a un **Livrable** démontrable             | ✓          |
| Toutes les tâches ont un ID unique `id<NNN>`           | ✓          |
| Chaque tâche a un titre, des dépendances et des docs   | ✓          |
| Les tâches terminées sont cochées `[x]`                | ✓          |
| **Librairies minimales** (pas de dépendances inutiles) | ✓          |
| La roadmap est cohérente avec le code existant         | ✓          |
