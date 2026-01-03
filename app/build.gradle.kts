plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp") // For Room processing
}

android {
    namespace = "com.watermelon.player"
    compileSdk = 35 // Or your target

    defaultConfig {
        applicationId = "com.watermelon.player"
        minSdk = 21 // Or your min
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true // Set to false if you don't use Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Latest compatible with Kotlin 1.9.x
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Lifecycle & ViewModel
    val lifecycle_version = "2.8.6"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Hilt
    val hilt_version = "2.51"
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-compiler:$hilt_version") // Use ksp instead of kapt for better performance

    // Hilt WorkManager integration
    val work_version = "2.11.0"
    implementation("androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.hilt:hilt-work:1.3.0")
    ksp("androidx.hilt:hilt-compiler:1.3.0")

    // Media3 ExoPlayer (modern replacement for old ExoPlayer)
    val media3_version = "1.9.0"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")

    // Coil for image loading (used in PerformanceMonitor)
    val coil_version = "3.3.0"
    implementation("io.coil-kt.coil3:coil-compose:$coil_version") // Use coil if not Compose
    implementation("io.coil-kt.coil3:coil-network-okhttp:$coil_version")

    // Room (for your VideoEntity database)
    val room_version = "2.6.1"
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Compose (if you use it)
    val composeBom = "2024.10.00" // Latest Compose BOM
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Testing (optional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
