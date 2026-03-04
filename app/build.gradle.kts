plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.example.huybrancardage"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.huybrancardage"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL du backend - paramétrable via gradle.properties ou ligne de commande
        // Exemple: ./gradlew assembleRelease -PBACKEND_URL="https://api.production.com/api/v1/"
        val backendUrl = project.findProperty("BACKEND_URL")?.toString()
            ?: "http://localhost:8080/api/v1/"
        buildConfigField("String", "BACKEND_URL", "\"$backendUrl\"")
    }

    buildTypes {
        debug {
            // URL pour le développement local (émulateur: 10.0.2.2, USB: localhost avec adb reverse)
            val debugUrl = project.findProperty("DEBUG_BACKEND_URL")?.toString()
                ?: project.findProperty("BACKEND_URL")?.toString()
                ?: "http://10.0.2.2:8080/api/v1/"
            buildConfigField("String", "BACKEND_URL", "\"$debugUrl\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // URL pour la production - DOIT être définie via gradle.properties ou ligne de commande
            val releaseUrl = project.findProperty("RELEASE_BACKEND_URL")?.toString()
                ?: project.findProperty("BACKEND_URL")?.toString()
                ?: "https://api.example.com/api/v1/"
            buildConfigField("String", "BACKEND_URL", "\"$releaseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = false
        warningsAsErrors = false
        checkDependencies = true
        htmlReport = true
        xmlReport = true
        textReport = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Network - Retrofit + OkHttp + Kotlinx Serialization
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode.scanning)

    // Coil Image Loading
    implementation(libs.coil.compose)

    // ExifInterface for image orientation
    implementation(libs.androidx.exifinterface)

    // Play Services Location for GPS
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
