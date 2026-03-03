# ID003 - Navigation Compose Implementation

## Résumé
Implémentation de la navigation entre tous les écrans de l'application en utilisant Navigation Compose.

## Dépendances
- **id001** ✅ - Projet Android initialisé avec Design System
- **id002** ✅ - 9 écrans statiques implémentés

## Modifications apportées

### 1. Dépendances ajoutées

#### `gradle/libs.versions.toml`
- Ajout de la version `navigationCompose = "2.8.9"`
- Ajout de la référence `androidx-navigation-compose`

#### `app/build.gradle.kts`
- Ajout de `implementation(libs.androidx.navigation.compose)`

### 2. Nouveaux fichiers créés

#### `app/src/main/java/com/example/huybrancardage/navigation/Routes.kt`
Définition typée des routes de navigation :
- `Route.Accueil` - Écran d'accueil
- `Route.RechercheManuelle` - Recherche par nom/IPP
- `Route.ScanBracelet` - Scan QR/Code-barres
- `Route.DossierPatient` - Affichage patient
- `Route.Medias` - Ajout photos/documents
- `Route.Localisation` - Position GPS
- `Route.Destination` - Sélection destination
- `Route.Recapitulatif` - Vérification avant envoi
- `Route.Confirmation` - Succès de la demande

#### `app/src/main/java/com/example/huybrancardage/navigation/NavGraph.kt`
NavHost configuré avec tous les parcours :
- **Parcours principal** : Accueil → Recherche/Scan → DossierPatient → Medias → Localisation → Destination → Récapitulatif → Confirmation
- **Retour à l'accueil** depuis Confirmation avec nettoyage de la back stack
- **Navigation retour** sur chaque écran via `popBackStack()`
- **Édition depuis Récapitulatif** : possibilité de revenir sur Patient, Trajet ou Médias

### 3. Fichiers modifiés

#### `app/src/main/java/com/example/huybrancardage/MainActivity.kt`
- Intégration de `rememberNavController()`
- Remplacement de `AccueilScreen` par `BrancardageNavGraph`

## Parcours utilisateur implémentés

### Flow Principal
```
Accueil
  ├─→ Scanner le bracelet → [Scan réussi] → Dossier Patient
  └─→ Recherche manuelle → [Rechercher] → Dossier Patient
                                              │
                                              ▼
                                    Créer une demande
                                              │
                                              ▼
                                    Médias (Optionnel)
                                      │ [Continuer/Passer]
                                      ▼
                                    Localisation
                                      │ [Confirmer le départ]
                                      ▼
                                    Destination
                                      │ [Confirmer la destination]
                                      ▼
                                    Récapitulatif
                                      │ [Valider]
                                      ▼
                                    Confirmation
                                      │ [Retour à l'accueil]
                                      ▼
                                    Accueil (back stack vidée)
```

### Navigation retour
- Chaque écran a un bouton retour fonctionnel
- Depuis Récapitulatif, possibilité d'éditer Patient/Trajet/Médias

## Tests manuels recommandés

1. **Parcours complet via recherche manuelle**
   - Accueil → Recherche → DossierPatient → Medias → Localisation → Destination → Récapitulatif → Confirmation → Accueil

2. **Parcours complet via scan bracelet**
   - Accueil → Scan → DossierPatient → (suite identique)

3. **Parcours avec saut des médias**
   - Medias → [Passer] → Localisation

4. **Édition depuis récapitulatif**
   - Récapitulatif → [Modifier Patient] → Retour au DossierPatient
   - Récapitulatif → [Modifier Trajet] → Retour à Localisation
   - Récapitulatif → [Modifier Médias] → Retour à Medias

5. **Navigation retour**
   - Vérifier que le bouton retour fonctionne sur chaque écran

## Prochaines étapes (id004)
- Ajouter les ViewModels pour gérer l'état partagé entre écrans
- Implémenter le passage de données (Patient sélectionné, médias, etc.)

