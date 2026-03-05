# ID013 - Service de Tracking WiFi Indoor

## 📋 Résumé

Implémentation d'un **Foreground Service** Android pour le suivi en temps réel de la position du brancardier pendant un transport, utilisant la **géolocalisation indoor par WiFi**.

## 🎯 Objectif pédagogique

Ce module illustre plusieurs concepts Android fondamentaux :

| Concept | Description |
|---------|-------------|
| **Foreground Service** | Service qui effectue une opération visible par l'utilisateur |
| **Notification persistante** | Obligation légale Android pour les services longue durée |
| **Coroutines dans un Service** | Boucle périodique avec `delay()` et `SupervisorJob` |
| **StateFlow statique** | Pattern simple pour communiquer Service ↔ UI |
| **WiFi Scanning** | Alternative au GPS pour la localisation indoor |
| **Permissions runtime** | Gestion des permissions Android 6+ |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  RecapitulatifScreen                                        │
│  └── TrackingSection                                        │
│      └── TrackingControl (Composable)                       │
│          - Bouton Start/Stop                                │
│          - Affichage position actuelle                      │
│          - Gestion permissions                              │
└────────────────────┬────────────────────────────────────────┘
                     │ collectAsState()
                     │ (observe StateFlow)
┌────────────────────▼────────────────────────────────────────┐
│  TrackingService (Foreground Service)                       │
│  - Notification persistante                                 │
│  - Boucle de scan WiFi (10 sec)                            │
│  - Expose StateFlow (currentLocation, isTracking)          │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│  WifiLocationProvider                                       │
│  - Scan des réseaux WiFi                                   │
│  - Mapping BSSID → Localisation physique                   │
│  - Mode simulation pour démo/tests                         │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Fichiers créés/modifiés

### Nouveaux fichiers

| Fichier | Description |
|---------|-------------|
| `domain/model/WifiLocation.kt` | Modèle de données pour la localisation WiFi |
| `data/location/WifiLocationProvider.kt` | Provider de géolocalisation indoor |
| `service/TrackingService.kt` | Foreground Service de tracking |
| `ui/components/TrackingControl.kt` | Composant UI pour contrôler le tracking |

### Fichiers modifiés

| Fichier | Modification |
|---------|--------------|
| `AndroidManifest.xml` | Ajout permissions WiFi + déclaration service |
| `ui/theme/Color.kt` | Ajout couleurs vertes (Green100, Green500, Green600) |
| `ui/screens/RecapitulatifScreen.kt` | Intégration du TrackingControl |

## 🔐 Permissions requises

```xml
<!-- WiFi scanning -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Foreground Service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 🔄 Flux de fonctionnement

### Démarrage du tracking

```
1. Utilisateur clique sur "Démarrer le transport"
2. TrackingControl vérifie les permissions
3. Si OK → TrackingService.start(context, patientName, brancardageId)
4. Service démarre en foreground avec notification
5. Boucle de tracking lancée (coroutine)
```

### Boucle de tracking (toutes les 10 secondes)

```
1. WifiLocationProvider.getCurrentLocation()
   └── Scan WiFi → Filtre bornes hôpital → Meilleur signal
2. Si pas de borne trouvée → getSimulatedLocation()
3. Met à jour StateFlow (_currentLocation)
4. Met à jour notification
5. Envoie position au serveur (simulé)
6. delay(10_000) → Répète
```

### Arrêt du tracking

```
1. Utilisateur clique sur "Arrêter le suivi"
2. TrackingService.stop(context)
3. onDestroy() appelé
4. Job annulé, StateFlow réinitialisés
5. Notification supprimée
```

## 📍 Base de données WiFi simulée

Le `WifiLocationProvider` contient une map simulant les bornes WiFi de l'hôpital :

| BSSID | Bâtiment | Étage | Zone |
|-------|----------|-------|------|
| AA:BB:CC:DD:EE:01 | Bâtiment A | RDC | Accueil principal |
| AA:BB:CC:DD:EE:02 | Bâtiment A | RDC | Salle d'attente |
| AA:BB:CC:DD:EE:03 | Bâtiment A | 1er | Cardiologie |
| AA:BB:CC:DD:EE:04 | Bâtiment A | 1er | Pneumologie |
| AA:BB:CC:DD:EE:05 | Bâtiment B | 2ème | Radiologie |
| AA:BB:CC:DD:EE:06 | Bâtiment B | 2ème | Scanner/IRM |
| AA:BB:CC:DD:EE:07 | Bâtiment B | Sous-sol | Bloc opératoire |
| AA:BB:CC:DD:EE:08 | Bâtiment B | Sous-sol | Salle de réveil |
| AA:BB:CC:DD:EE:09 | Bâtiment C | RDC | Urgences |
| AA:BB:CC:DD:EE:10 | Bâtiment C | RDC | Box de soins |
| AA:BB:CC:DD:EE:11 | Bâtiment C | 1er | Maternité |
| AA:BB:CC:DD:EE:12 | Bâtiment C | 1er | Pédiatrie |

## 🧪 Test manuel

1. Lancer l'application sur un appareil/émulateur
2. Aller sur l'écran Récapitulatif (avec un patient sélectionné)
3. Accepter les permissions demandées
4. Cliquer sur "Démarrer le transport"
5. Observer :
   - La notification persistante dans la barre de statut
   - La carte de position qui s'affiche et se met à jour
   - Les logs dans Logcat (tag: `TrackingService`)

## 📚 Points clés à retenir pour la formation

### 1. Foreground Service obligatoire

Depuis Android 8 (Oreo), un service qui doit continuer en arrière-plan DOIT :
- Être démarré avec `startForegroundService()`
- Appeler `startForeground()` dans les 5 secondes
- Afficher une notification persistante

### 2. Canal de notification (Android 8+)

```kotlin
val channel = NotificationChannel(
    CHANNEL_ID,
    "Nom visible",
    NotificationManager.IMPORTANCE_LOW
)
manager.createNotificationChannel(channel)
```

### 3. Communication Service ↔ UI

Pattern simplifié avec StateFlow statique :
```kotlin
companion object {
    private val _currentLocation = MutableStateFlow<WifiLocation?>(null)
    val currentLocation: StateFlow<WifiLocation?> = _currentLocation.asStateFlow()
}
```

L'UI observe avec :
```kotlin
val currentLocation by TrackingService.currentLocation.collectAsState()
```

### 4. Coroutines dans un Service

```kotlin
private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

trackingJob = serviceScope.launch {
    while (isActive) {
        // ... travail périodique
        delay(10_000L)
    }
}
```

## ✅ Statut

- [x] Modèle WifiLocation
- [x] WifiLocationProvider avec simulation
- [x] TrackingService (Foreground Service)
- [x] Composant TrackingControl
- [x] Intégration dans RecapitulatifScreen
- [x] Permissions dans AndroidManifest
- [x] Build successful

## 🔜 Améliorations possibles

- [ ] Appel API réel pour envoyer la position
- [ ] Historique des positions (Room database)
- [ ] Affichage sur une carte de l'hôpital
- [ ] Bound Service pour une communication plus robuste
- [ ] WorkManager pour la persistance en cas de kill

