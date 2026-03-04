# ==========================================================
# ProGuard / R8 Rules - HuyBrancardage Application
# ==========================================================

# -----------------------------------------------------------
# General Android rules
# -----------------------------------------------------------

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name in stack traces
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep signatures for generics (required for Retrofit/Serialization)
-keepattributes Signature

# -----------------------------------------------------------
# Kotlin rules
# -----------------------------------------------------------

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Keep kotlin.Unit
-keep class kotlin.Unit { *; }

# Keep kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# -----------------------------------------------------------
# Kotlinx Serialization
# -----------------------------------------------------------

# Keep serializer classes
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated serializers
-if @kotlinx.serialization.Serializable class **
-keep class <1>$$serializer { *; }

# Keep data classes used for serialization
-keep @kotlinx.serialization.Serializable class com.example.huybrancardage.** { *; }

# -----------------------------------------------------------
# Retrofit
# -----------------------------------------------------------

# Keep Retrofit API interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit service methods (required for dynamic proxies)
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore Retrofit warnings about missing annotation defaults
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Keep generic type information for Retrofit
-keepattributes Exceptions

# -----------------------------------------------------------
# OkHttp
# -----------------------------------------------------------

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# -----------------------------------------------------------
# ML Kit Barcode Scanning
# -----------------------------------------------------------

-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# -----------------------------------------------------------
# CameraX
# -----------------------------------------------------------

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# -----------------------------------------------------------
# Google Play Services Location
# -----------------------------------------------------------

-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# -----------------------------------------------------------
# Jetpack Compose
# -----------------------------------------------------------

# Keep Compose runtime classes
-keep class androidx.compose.** { *; }

# Keep @Composable functions for reflection (Previews)
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# -----------------------------------------------------------
# Application Domain Models
# -----------------------------------------------------------

# Keep all model classes for serialization and data integrity
-keep class com.example.huybrancardage.domain.model.** { *; }
-keep class com.example.huybrancardage.data.remote.** { *; }

# -----------------------------------------------------------
# Debugging & Crash Reporting
# -----------------------------------------------------------

# Remove Log calls in release (optional - uncomment if needed)
# -assumenosideeffects class android.util.Log {
#     public static int v(...);
#     public static int d(...);
#     public static int i(...);
# }
