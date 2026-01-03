// Top-level build file - common config for all modules
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    id("kotlin-parcelize") apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
