pluginManagement {
    repositories {
        google()
        mavenCentral() // Critical for Hilt plugin
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WatermelonPlayer"
include(":app")
