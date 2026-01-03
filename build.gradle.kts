// Top-level build file
plugins {
    id("com.android.application") version "8.7.0" apply false  // Compatible stable AGP
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false  // Latest stable Kotlin (Dec 2025)
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "2.3.0" apply false  // Fixed: Latest stable KSP
}
