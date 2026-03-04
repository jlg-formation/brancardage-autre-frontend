# ID012 - Implémentation des Intents (Appel & Partage)

## 📋 Résumé

Ajout de fonctionnalités utilisant les **Intents Android** pour :
1. **Appeler le service de brancardage** (Intent implicite `ACTION_DIAL`)
2. **Partager les informations de transport** (Intent implicite `ACTION_SEND` avec `createChooser`)

## 🎯 Objectifs pédagogiques

- Comprendre les **Intents implicites** vs explicites
- Utiliser `Intent.ACTION_DIAL` pour ouvrir le composeur téléphonique
- Utiliser `Intent.ACTION_SEND` avec `createChooser` pour le partage
- Gérer la **visibilité des packages** (Android 11+) via `<queries>`
- Vérifier la disponibilité d'une application avec `resolveActivity()`

## 📁 Fichiers créés/modifiés

### Nouveau fichier

| Fichier | Description |
|---------|-------------|
| `util/IntentUtils.kt` | Utilitaire centralisant les fonctions d'Intent |

### Fichiers modifiés

| Fichier | Modification |
|---------|--------------|
| `ui/screens/RecapitulatifScreen.kt` | Ajout des boutons "Appeler" et "Partager" |
| `AndroidManifest.xml` | Ajout des déclarations `<queries>` pour Android 11+ |

## 🔧 Détails techniques

### IntentUtils.kt

```kotlin
object IntentUtils {
    // Intent pour appeler (ouvre le composeur, pas besoin de permission CALL_PHONE)
    fun dialBrancardageService(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:+33123456789".toUri()
        }
        context.startActivity(intent)
    }

    // Intent pour partager du texte
    fun shareBrancardageRequest(context: Context, ...) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Demande de brancardage")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Partager via")
        context.startActivity(chooserIntent)
    }
}
```

### Manifest - Package Visibility (Android 11+)

```xml
<queries>
    <!-- Pour l'intent téléphone -->
    <intent>
        <action android:name="android.intent.action.DIAL" />
        <data android:scheme="tel" />
    </intent>

    <!-- Pour le partage -->
    <intent>
        <action android:name="android.intent.action.SEND" />
        <data android:mimeType="text/plain" />
    </intent>
</queries>
```

## 📱 Interface utilisateur

Sur l'écran **Récapitulatif**, deux nouveaux boutons apparaissent :

```
┌─────────────────────────────────────┐
│  [📞 Appeler]    [📤 Partager]      │  ← Boutons secondaires (outline)
├─────────────────────────────────────┤
│       [✓ Valider la demande]        │  ← Bouton principal
└─────────────────────────────────────┘
```

### Bouton "Appeler"
- Ouvre le composeur téléphonique avec le numéro du service de brancardage
- Utilise `ACTION_DIAL` (pas `ACTION_CALL`) → **Pas besoin de permission**

### Bouton "Partager"
- Ouvre un sélecteur d'applications (SMS, Email, WhatsApp, etc.)
- Partage un texte formaté avec les infos du transport

## 📝 Format du message partagé

```
📋 DEMANDE DE BRANCARDAGE
═══════════════════════════

👤 PATIENT
   Nom: Jean Dupont
   IPP: 123456789

🚶 TRAJET
   Départ: Cardiologie - Chambre 204
   Arrivée: Radiologie (Bâtiment B - Étage 1)

📷 MÉDIAS: 2 fichier(s) joint(s)

═══════════════════════════
Envoyé depuis HuyBrancardage
```

## ⚠️ Points d'attention

### ACTION_DIAL vs ACTION_CALL

| Intent | Permission | Comportement |
|--------|------------|--------------|
| `ACTION_DIAL` | ❌ Aucune | Ouvre le composeur, l'utilisateur doit appuyer sur "Appeler" |
| `ACTION_CALL` | ✅ `CALL_PHONE` | Appelle directement (déconseillé) |

**Choix** : `ACTION_DIAL` pour respecter la vie privée de l'utilisateur.

### createChooser vs startActivity direct

```kotlin
// ❌ Peut ouvrir directement une app par défaut
startActivity(shareIntent)

// ✅ Affiche toujours le sélecteur d'applications
startActivity(Intent.createChooser(shareIntent, "Partager via"))
```

### resolveActivity pour la robustesse

```kotlin
if (intent.resolveActivity(context.packageManager) != null) {
    context.startActivity(intent)
} else {
    Toast.makeText(context, "Aucune application disponible", Toast.LENGTH_SHORT).show()
}
```

## ✅ Checklist de validation

- [x] Build successful (`./gradlew assembleDebug`)
- [x] Boutons visibles sur l'écran Récapitulatif
- [x] Déclarations `<queries>` dans le manifest
- [x] Extension KTX `toUri()` utilisée
- [x] Gestion des cas où aucune app n'est disponible

## 🔗 Ressources

- [Documentation Android - Intents](https://developer.android.com/guide/components/intents-filters)
- [Package Visibility (Android 11+)](https://developer.android.com/training/package-visibility)
- [Intent.createChooser](https://developer.android.com/reference/android/content/Intent#createChooser(android.content.Intent,%20java.lang.CharSequence))

