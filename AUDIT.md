# 🔒 Audit de Sécurité - HuyBrancardage

**Date de l'audit** : 4 mars 2026  
**Version analysée** : 1.0  
**Type d'application** : Android Mobile (Kotlin / Jetpack Compose)  
**Contexte** : Application de gestion de brancardage hospitalier

---

## 📋 Résumé exécutif

| Catégorie | Niveau de risque | Statut |
|-----------|------------------|--------|
| **Dépendances** | 🟢 Faible | Aucune CVE connue |
| **Données sensibles** | 🟠 Moyen | Améliorations recommandées |
| **Communications réseau** | 🟠 Moyen | Améliorations recommandées |
| **Authentification** | 🔴 Élevé | Non implémentée |
| **Stockage local** | 🟢 Faible | Pas de stockage persistant sensible |
| **Logs & Debug** | 🟠 Moyen | Logs excessifs en production |
| **Permissions** | 🟢 Faible | Appropriées pour l'usage |
| **Configuration ProGuard** | 🟢 Faible | Correctement configurée |

**Score global de sécurité** : **65/100** (Acceptable pour un POC, insuffisant pour la production)

---

## 🔍 Analyse détaillée

### 1. Dépendances et CVE

#### ✅ Statut : Aucune vulnérabilité connue

Les dépendances suivantes ont été analysées :

| Dépendance | Version | CVE |
|------------|---------|-----|
| OkHttp | 4.12.0 | ✅ Aucune |
| Retrofit | 2.11.0 | ✅ Aucune |
| kotlinx-serialization-json | 1.7.3 | ✅ Aucune |
| Coil | 2.6.0 | ✅ Aucune |
| CameraX | 1.4.1 | ✅ Aucune |
| ML Kit Barcode | 17.3.0 | ✅ Aucune |
| Play Services Location | 21.3.0 | ✅ Aucune |

**Recommandation** : Mettre en place une vérification automatique des CVE dans le pipeline CI/CD (ex: Dependabot, OWASP Dependency-Check).

---

### 2. Authentification et Autorisation

#### 🔴 Risque ÉLEVÉ : Absence d'authentification

**Constat** :
- Aucune implémentation d'authentification dans l'application
- La spécification API (`api.yml`) définit un schéma JWT Bearer, mais non implémenté côté client
- Pas de gestion de tokens, sessions, ou credentials

**Fichiers concernés** :
- `data/remote/api/ApiClient.kt` - Aucun intercepteur d'authentification
- `data/remote/api/BrancardageApiService.kt` - Aucun header Authorization

**Impact potentiel** :
- Accès non autorisé aux données patients (données de santé - RGPD Article 9)
- Usurpation d'identité brancardier
- Modification frauduleuse de demandes de brancardage

**Recommandations** :
```kotlin
// Ajouter un intercepteur d'authentification dans ApiClient.kt
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        tokenProvider()?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(request.build())
    }
}
```

**Priorité** : 🔴 CRITIQUE - À implémenter avant tout déploiement

---

### 3. Communications réseau

#### 🟠 Risque MOYEN : Configuration HTTP cleartext pour développement

**Constat** :

**Fichier** : `res/xml/network_security_config.xml`
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">localhost</domain>
    <domain includeSubdomains="true">127.0.0.1</domain>
    <domain includeSubdomains="true">10.0.2.2</domain>
</domain-config>
```

**Points positifs** :
- ✅ Cleartext limité aux adresses de développement uniquement
- ✅ HTTPS implicitement requis pour tous les autres domaines
- ✅ URL de production configurée en HTTPS (`https://api.example.com`)

**Points d'attention** :
- ⚠️ Pas de certificate pinning configuré
- ⚠️ Pas de validation stricte des certificats serveur

**Recommandations** :

1. **Implémenter le Certificate Pinning** pour la production :
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.hopital.internal</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">BASE64_ENCODED_HASH</pin>
            <!-- Backup pin -->
            <pin digest="SHA-256">BACKUP_BASE64_ENCODED_HASH</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

2. **Supprimer la configuration cleartext en release** via build variants

**Priorité** : 🟠 HAUTE - À implémenter pour la production

---

### 4. Données sensibles en transit et au repos

#### 🟠 Risque MOYEN : Manipulation de données de santé

**Types de données sensibles identifiées** :

| Donnée | Classification RGPD | Stockage | Transit |
|--------|---------------------|----------|---------|
| Nom/Prénom patient | Personnelle | ❌ Non persisté | ⚠️ Non chiffré |
| IPP | Personnelle | ❌ Non persisté | ⚠️ Non chiffré |
| N° Sécurité Sociale | Sensible | ❌ Non persisté | ⚠️ Non chiffré |
| Localisation GPS | Personnelle | ❌ Non persisté | ⚠️ Non chiffré |
| Photos médicales | Sensible (santé) | ⚠️ Cache temporaire | ⚠️ Non chiffré |

**Points positifs** :
- ✅ Pas de stockage persistant de données sensibles (SharedPreferences, Room)
- ✅ Photos stockées dans le cache temporaire (`context.cacheDir`)
- ✅ FileProvider correctement configuré avec `exported="false"`

**Points d'attention** :
- ⚠️ Photos temporaires non chiffrées dans le cache
- ⚠️ Données en mémoire (ViewModel) accessibles via debug

**Fichiers concernés** :
- `data/media/MediaManager.kt` - Stockage temporaire non chiffré
- `domain/model/Patient.kt` - Modèle avec données sensibles

**Recommandations** :

1. **Chiffrer les fichiers temporaires** :
```kotlin
// Utiliser EncryptedFile pour les médias sensibles
val encryptedFile = EncryptedFile.Builder(
    context,
    file,
    masterKey,
    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
).build()
```

2. **Nettoyer le cache** à la fermeture de l'application

3. **Obfusquer les données en mémoire** pour les builds release

**Priorité** : 🟠 MOYENNE - Recommandé pour la conformité RGPD/HDS

---

### 5. Logs et informations de debug

#### 🟠 Risque MOYEN : Logs excessifs exposant des données sensibles

**Constat** :

Les fichiers suivants contiennent des logs en production :
- `ui/viewmodel/BrancardageViewModel.kt` (20+ Log.d)
- `data/repository/BrancardageRepository.kt` (15+ Log.d)
- `ui/camera/CameraPreview.kt`
- `ui/camera/BarcodeAnalyzer.kt`

**Exemples de logs problématiques** :
```kotlin
Log.d(TAG, "Patient: ${currentState.patient?.nomComplet}")
Log.d(TAG, "PatientId: ${request.patientId}")
Log.d(TAG, "Code détecté: $rawValue")
```

**Impact** :
- Données patients lisibles via `adb logcat`
- IPP et informations de localisation exposés
- Codes de bracelet (identifiants) loggés

**Recommandations** :

1. **Désactiver les logs en production** via ProGuard (décommenter les règles existantes) :
```proguard
# proguard-rules.pro - Décommenter pour la production
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
```

2. **Utiliser Timber** avec configuration par BuildType :
```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

3. **Ne jamais logger de données sensibles** (IPP, nom, n° SS)

**Priorité** : 🟠 HAUTE - À corriger avant la production

---

### 6. Permissions Android

#### ✅ Statut : Appropriées pour l'usage

**Permissions déclarées** (`AndroidManifest.xml`) :

| Permission | Justification | Risque |
|------------|---------------|--------|
| `INTERNET` | Appels API | ✅ Standard |
| `ACCESS_NETWORK_STATE` | Vérification connectivité | ✅ Standard |
| `CAMERA` | Scan bracelet + photos | ✅ Justifiée |
| `ACCESS_FINE_LOCATION` | Géolocalisation brancardage | ✅ Justifiée |
| `ACCESS_COARSE_LOCATION` | Fallback GPS | ✅ Justifiée |

**Points positifs** :
- ✅ `android:required="false"` pour la caméra (dégradation gracieuse)
- ✅ Pas de permissions excessives
- ✅ Permissions runtime correctement gérées

**Recommandation** : Documenter la justification de chaque permission pour les stores (Google Play Data Safety).

---

### 7. Composants exportés et Intent

#### ✅ Statut : Correctement sécurisé

**Analyse du Manifest** :

| Composant | Exporté | Justification |
|-----------|---------|---------------|
| `MainActivity` | `true` | Point d'entrée (Launcher) - ✅ Normal |
| `FileProvider` | `false` | Partage interne uniquement - ✅ Sécurisé |

**Points positifs** :
- ✅ Pas de BroadcastReceiver/Service exportés
- ✅ Pas de ContentProvider public
- ✅ FileProvider avec `grantUriPermissions="true"` et scope limité

---

### 8. Sauvegarde et extraction de données

#### 🟠 Risque MOYEN : Configuration par défaut

**Constat** :

**Fichier** : `AndroidManifest.xml`
```xml
android:allowBackup="true"
android:fullBackupContent="@xml/backup_rules"
android:dataExtractionRules="@xml/data_extraction_rules"
```

**Fichiers** : `backup_rules.xml` et `data_extraction_rules.xml` sont vides (templates par défaut)

**Impact potentiel** :
- Backup automatique peut inclure des données sensibles en cache
- Extraction de données sur appareil rooté

**Recommandations** :

1. **Désactiver le backup** pour une application médicale :
```xml
android:allowBackup="false"
```

2. **Ou configurer explicitement** les exclusions :
```xml
<!-- backup_rules.xml -->
<full-backup-content>
    <exclude domain="sharedpref" path="." />
    <exclude domain="database" path="." />
    <exclude domain="file" path="images/" />
</full-backup-content>
```

**Priorité** : 🟠 MOYENNE - Recommandé pour conformité HDS

---

### 9. Obfuscation et protection du code

#### ✅ Statut : Bien configuré

**Configuration** (`build.gradle.kts`) :
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(...)
}
```

**Points positifs** :
- ✅ Minification et shrinking activés en release
- ✅ Règles ProGuard complètes pour Retrofit, Kotlinx Serialization, etc.
- ✅ Préservation des numéros de ligne pour le debugging

**Recommandations complémentaires** :
- Ajouter une vérification d'intégrité anti-tampering (optionnel)
- Considérer DexGuard pour une protection avancée (si budget)

---

### 10. Validation des entrées

#### 🟠 Risque MOYEN : Validation côté client insuffisante

**Constat** :

**Fichier** : `ui/viewmodel/SearchViewModel.kt`
- La recherche accepte n'importe quelle chaîne sans validation
- Pas de sanitization des entrées avant envoi API

**Recommandations** :

1. **Ajouter des validations** côté client :
```kotlin
fun setIpp(ipp: String) {
    // Valider format IPP (9 chiffres)
    val sanitized = ipp.filter { it.isDigit() }.take(9)
    _uiState.update { it.copy(ipp = sanitized, error = null) }
}

fun setNumeroSecuriteSociale(nss: String) {
    // Valider format NSS (15 chiffres)
    val sanitized = nss.filter { it.isDigit() }.take(15)
    _uiState.update { it.copy(numeroSecuriteSociale = sanitized, error = null) }
}
```

2. **Encoder les paramètres** avant envoi (Retrofit le fait automatiquement ✅)

**Note** : La validation principale doit être côté serveur (défense en profondeur)

---

## 📊 Matrice des risques

| ID | Vulnérabilité | Probabilité | Impact | Risque | Priorité |
|----|---------------|-------------|--------|--------|----------|
| SEC-01 | Absence d'authentification | Élevée | Critique | 🔴 Critique | P0 |
| SEC-02 | Logs avec données sensibles | Élevée | Élevé | 🔴 Élevé | P1 |
| SEC-03 | Pas de certificate pinning | Moyenne | Élevé | 🟠 Moyen | P2 |
| SEC-04 | Cache photos non chiffré | Faible | Moyen | 🟡 Faible | P3 |
| SEC-05 | Backup activé par défaut | Faible | Moyen | 🟡 Faible | P3 |
| SEC-06 | Validation entrées client | Moyenne | Faible | 🟡 Faible | P4 |

---

## ✅ Plan de remédiation

### Phase 1 - Critique (Avant MVP)

| Action | Effort | Fichiers impactés |
|--------|--------|-------------------|
| Implémenter authentification JWT | 3-5 jours | `ApiClient.kt`, nouveau `AuthManager.kt` |
| Supprimer/conditionner les logs | 0.5 jour | `BrancardageViewModel.kt`, `BrancardageRepository.kt`, etc. |

### Phase 2 - Haute (Avant Production)

| Action | Effort | Fichiers impactés |
|--------|--------|-------------------|
| Configurer certificate pinning | 1 jour | `network_security_config.xml` |
| Configurer backup rules | 0.5 jour | `backup_rules.xml`, `data_extraction_rules.xml` |
| Valider entrées utilisateur | 1 jour | ViewModels |

### Phase 3 - Conformité HDS (Recommandé)

| Action | Effort | Fichiers impactés |
|--------|--------|-------------------|
| Chiffrer cache médias | 2 jours | `MediaManager.kt` |
| Audit code externe | 1 semaine | - |
| Tests de pénétration | 1-2 semaines | - |

---

## 📚 Références

- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [ANSSI - Guide d'hygiène informatique](https://www.ssi.gouv.fr/guide/guide-dhygiene-informatique/)
- [RGPD Article 9 - Données de santé](https://www.cnil.fr/fr/reglement-europeen-protection-donnees)
- [Hébergement Données de Santé (HDS)](https://esante.gouv.fr/produits-services/hds)

---

## 📝 Historique des audits

| Date | Version | Auditeur | Statut |
|------|---------|----------|--------|
| 04/03/2026 | 1.0 | Audit automatisé | Initial |

---

*Ce rapport a été généré dans le cadre d'un audit de sécurité préliminaire. Un audit complet par un expert en sécurité applicative est recommandé avant tout déploiement en environnement de production hospitalier.*

