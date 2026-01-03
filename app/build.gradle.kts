plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.watermelon.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.watermelon.player"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${System.currentTimeMillis()}\"")
    }

    // Product flavors for different markets (required for assembleIranRelease)
    flavorDimensions += "market"

    productFlavors {
        create("google") {
            dimension = "market"
            // Optional: applicationIdSuffix = ".google"
            // Add Google Play specific config here if needed
        }

        create("iran") {
            dimension = "market"
            // Optional: applicationIdSuffix = ".iran"
            // Add Iran-specific config (e.g., alternative APIs, mirrors) here
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("watermelon.jks")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com
