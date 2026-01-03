// Top-level build file
plugins {
    id("com.android.application") version "8.7.0" apply false  // Latest stable AGP ~late 2025/early 2026
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false  // Latest stable Kotlin
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false  // Matches Kotlin for KSP/Room
}

// No repositories block here anymore!
// All repositories are now centralized in settings.gradle.kts
