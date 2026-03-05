# ID014 - BroadcastReceiver : Détection de connectivité réseau

## 📋 Résumé

Implémentation d'un **BroadcastReceiver** pour détecter les changements de connectivité réseau et gérer un mode hors ligne avec file d'attente des demandes de brancardage.

## 🎯 Objectif pédagogique

Cette fonctionnalité illustre plusieurs concepts importants du développement Android :

### 1. BroadcastReceiver
Un composant Android qui écoute et réagit aux événements système (broadcasts). Permet à l'application de réagir aux changements même quand elle n'est pas au premier plan.

### 2. NetworkCallback (approche moderne)
Depuis Android 7.0, Google recommande d'utiliser `NetworkCallback` plutôt que `BroadcastReceiver` pour les changements de connectivité. Cette implémentation combine les deux approches.

### 3. Persistance locale avec SharedPreferences
Les demandes en attente sont sauvegardées localement pour survivre à un redémarrage de l'application.

### 4. Pattern Singleton
Le `OfflineQueueManager` utilise ce pattern pour garantir une seule instance gérant les demandes en attente.

## 📁 Fichiers créés/modifiés

### Nouveaux fichiers

| Fichier | Description |
|---------|-------------|
| `receiver/NetworkReceiver.kt` | BroadcastReceiver pour la détection de connectivité |
| `data/local/OfflineQueueManager.kt` | Gestionnaire de file d'attente hors ligne |
| `ui/components/NetworkStatusIndicator.kt` | Composants UI pour le statut réseau |
| `ui/viewmodel/SyncViewModel.kt` | ViewModel pour la synchronisation |
| `navigation/Routes.kt` | Ajout de `ConfirmationQueued` |

### Fichiers modifiés

| Fichier | Modifications |
|---------|---------------|
| `MainActivity.kt` | Enregistrement/désenregistrement du NetworkCallback |
| `ui/viewmodel/BrancardageViewModel.kt` | Gestion du mode hors ligne |
| `ui/screens/AccueilScreen.kt` | Affichage de l'indicateur réseau |
| `ui/screens/ConfirmationScreen.kt` | Nouvel état "en file d'attente" |
| `navigation/NavGraph.kt` | Route pour confirmation hors ligne |

## 🔄 User Flow de démonstration

### Phase 1 : Utilisation normale (réseau disponible)
1. Ouvrir l'application → **Indicateur vert "En ligne"** visible
2. Créer une demande de brancardage
3. Valider → **Envoi immédiat** → Écran de confirmation ✅

### Phase 2 : Perte de connexion
4. **Désactiver le WiFi** sur le téléphone
5. Le `NetworkReceiver` détecte la perte
6. **Indicateur rouge "Hors ligne"** + **Bannière rouge**
7. Snackbar : *"Mode hors ligne activé"*

### Phase 3 : Création hors ligne
8. Créer une nouvelle demande de brancardage
9. Valider → **Mise en file d'attente locale**
10. Écran de confirmation orange : *"Demande enregistrée - En attente de connexion"*
11. Retour à l'accueil → Badge **"1 en attente"**

### Phase 4 : Retour de la connexion
12. **Réactiver le WiFi**
13. Le `NetworkReceiver` détecte le retour
14. **Indicateur passe au vert** + Snackbar : *"Connexion rétablie"*
15. Synchronisation automatique des demandes en attente
16. Snackbar : *"1 demande synchronisée avec succès"* ✅

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer                                               │
│  ├─ AccueilScreen (observe NetworkReceiver.isNetworkAvailable)
│  ├─ NetworkStatusIndicator (affiche l'état)            │
│  └─ ConfirmationScreen (gère isQueued)                 │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│  ViewModel Layer                                        │
│  ├─ BrancardageViewModel (queueBrancardageRequest)     │
│  └─ SyncViewModel (syncPendingRequests)                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│  Data Layer                                             │
│  ├─ NetworkReceiver (StateFlow<isNetworkAvailable>)    │
│  └─ OfflineQueueManager (SharedPreferences)            │
└─────────────────────────────────────────────────────────┘
```

## 🔑 Points clés du code

### Enregistrement du NetworkCallback (MainActivity.kt)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialiser le gestionnaire de file d'attente
    offlineQueueManager = OfflineQueueManager.getInstance(this)
    
    // Enregistrer le callback pour surveiller la connectivité
    NetworkReceiver.registerNetworkCallback(this)
    
    // Configurer le callback de synchronisation automatique
    NetworkReceiver.setOnNetworkRestoredCallback {
        syncPendingRequests()
    }
}

override fun onDestroy() {
    // IMPORTANT : Désenregistrer pour éviter les fuites de mémoire
    NetworkReceiver.unregisterNetworkCallback(this)
    super.onDestroy()
}
```

### Synchronisation automatique (MainActivity.kt)

```kotlin
private fun syncPendingRequests() {
    lifecycleScope.launch {
        delay(1000) // Attendre que la connexion soit stable
        
        val waitingRequests = offlineQueueManager.getWaitingRequests()
        for (pendingRequest in waitingRequests) {
            val request = pendingRequest.request.toBrancardageRequest()
            when (val result = brancardageRepository.createBrancardage(request)) {
                is NetworkResult.Success -> {
                    offlineQueueManager.removeFromQueue(pendingRequest.id)
                }
                is NetworkResult.Error -> {
                    offlineQueueManager.updateStatus(pendingRequest.id, PendingStatus.FAILED)
                }
            }
        }
    }
}
```

### Callback dans NetworkReceiver

```kotlin
// Le callback est appelé quand le réseau redevient disponible
override fun onAvailable(network: Network) {
    val wasOffline = !_isNetworkAvailable.value
    _isNetworkAvailable.value = true

    if (wasOffline) {
        _networkEvent.value = NetworkEvent.Connected
        // Appeler le callback de synchronisation
        onNetworkRestoredCallback?.invoke()
    }
}
```

## ✅ Tests manuels

1. **Test de base** : Vérifier que l'indicateur affiche "En ligne" au démarrage
2. **Test déconnexion** : Désactiver le WiFi → Vérifier la bannière rouge
3. **Test file d'attente** : Créer une demande hors ligne → Vérifier le badge
4. **Test reconnexion** : Réactiver le WiFi → Vérifier la synchronisation automatique
5. **Test persistance** : Créer une demande hors ligne → Fermer l'app → Rouvrir → Vérifier que la demande est toujours en attente

## 📚 Ressources

- [Guide officiel Android - Network connectivity](https://developer.android.com/training/monitoring-device-state/connectivity-status-type)
- [BroadcastReceiver documentation](https://developer.android.com/reference/android/content/BroadcastReceiver)
- [NetworkCallback documentation](https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback)

