// Top-level build file
plugins {
    id("com.android.application") version "8.5.0" apply false // Update to your AGP version
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false // Or latest Kotlin
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false // For Room KSP
}

repositories {
    google()
    mavenCentral()
}
