pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // Enforces centralization (common setting)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WatermelonPlayer"
include(":app")
