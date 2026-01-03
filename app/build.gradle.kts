plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin") // Required for Hilt
    id("androidx.navigation.safeargs.kotlin") // if you use Navigation
    // Add kotlin-kapt if not already there
    kotlin("kapt")
}

android {
    // ... your existing config

    buildFeatures {
        compose = true // if using Compose, otherwise false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // or latest compatible
    }
}

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.2") // if Compose

    // Hilt - Critical for your @HiltAndroidApp and @Inject
    val hilt_version = "2.51" // Latest stable Dagger/Hilt in 2026
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    // Hilt ViewModel
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0") // if using Compose Navigation

    // WorkManager + Hilt
    val work_version = "2.11.0" // Latest stable WorkManager
    implementation("androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.hilt:hilt-work:1.3.0")
    kapt("androidx.hilt:hilt-compiler:1.3.0")

    // Media3 (Modern ExoPlayer - replaces old com.google.android.exoplayer)
    val media3_version = "1.9.0"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")

    // Google Play Billing (for your EditionAwareBilling.kt)
    val billing_version = "8.0.0" // Latest mandatory version in 2026
    implementation("com.android.billingclient:billing-ktx:$billing_version")

    // Coil (for PerformanceMonitor.configureCoilForLowRam)
    val coil_version = "3.3.0"
    implementation("io.coil-kt.coil3:coil-compose:$coil_version") // or coil if not Compose
    implementation("io.coil-kt.coil3:coil-network-okhttp:$coil_version")

    // Room (if you have database/VideoEntity.kt)
    val room_version = "2.6.1" // or latest
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Other common ones you likely need
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
