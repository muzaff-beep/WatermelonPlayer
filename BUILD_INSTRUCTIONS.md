# Watermelon Player - Build Instructions

Complete reference for building, testing, and deploying Watermelon Player.

## Quick Start

```bash
# Clone repository
git clone <repository-url>
cd WatermelonPlayer

# Build debug APK (Iran edition)
./gradlew assembleIranDebug

# Build debug APK (Global edition)
./gradlew assembleGlobalDebug

# Install on connected device
./gradlew installIranDebug
```

## Build Commands Reference

### Debug Builds

| Command | Output | Edition |
|---------|--------|---------|
| `./gradlew assembleIranDebug` | `app/build/outputs/apk/iran/debug/app-iran-debug.apk` | Iran |
| `./gradlew assembleGlobalDebug` | `app/build/outputs/apk/global/debug/app-global-debug.apk` | Global |
| `./gradlew assembleDebug` | Both debug APKs | Both |

### Release Builds

| Command | Output | Edition |
|---------|--------|---------|
| `./gradlew assembleIranRelease` | `app/build/outputs/apk/iran/release/app-iran-release.apk` | Iran |
| `./gradlew assembleGlobalRelease` | `app/build/outputs/apk/global/release/app-global-release.apk` | Global |
| `./gradlew assembleRelease` | Both release APKs | Both |

### Bundle Builds (for Play Store)

| Command | Output | Edition |
|---------|--------|---------|
| `./gradlew bundleIranRelease` | `app/build/outputs/bundle/iranRelease/app-iran-release.aab` | Iran |
| `./gradlew bundleGlobalRelease` | `app/build/outputs/bundle/globalRelease/app-global-release.aab` | Global |

### Installation Commands

| Command | Description |
|---------|-------------|
| `./gradlew installIranDebug` | Install Iran debug APK on connected device |
| `./gradlew installGlobalDebug` | Install Global debug APK on connected device |
| `./gradlew installDebug` | Install all debug APKs |
| `adb install app/build/outputs/apk/iran/debug/app-iran-debug.apk` | Manual install via ADB |

### Testing Commands

| Command | Description |
|---------|-------------|
| `./gradlew test` | Run all unit tests |
| `./gradlew testIranDebugUnitTest` | Run Iran debug unit tests |
| `./gradlew testGlobalDebugUnitTest` | Run Global debug unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests on device |
| `./gradlew connectedIranDebugAndroidTest` | Run Iran instrumented tests |

### Cleaning Commands

| Command | Description |
|---------|-------------|
| `./gradlew clean` | Clean build directory |
| `./gradlew cleanBuildCache` | Clean Gradle build cache |
| `./gradlew --stop` | Stop Gradle daemon |

## Build Variants

### Available Combinations

```
Build Types:
├── debug
└── release

Flavors (Edition):
├── iran
└── global

Resulting Variants:
├── iranDebug
├── iranRelease
├── globalDebug
└── globalRelease
```

### Select Variant in Android Studio

1. **Build → Select Build Variant**
2. Choose from:
   - `iranDebug`
   - `iranRelease`
   - `globalDebug`
   - `globalRelease`

## Gradle Build Options

### Parallel Builds

```bash
# Enable parallel builds (faster)
./gradlew build --parallel

# Or add to gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=8
```

### Daemon Control

```bash
# Start daemon
./gradlew build

# Stop daemon
./gradlew --stop

# Disable daemon
./gradlew build --no-daemon
```

### Build Profiling

```bash
# Generate build profile
./gradlew assembleIranDebug --profile

# View report
open build/reports/profile/profile-*.html
```

### Verbose Output

```bash
# Detailed logging
./gradlew assembleIranDebug --info

# Debug logging
./gradlew assembleIranDebug --debug
```

## Release Build Checklist

### Pre-Release

- [ ] Update version in `app/build.gradle.kts`
- [ ] Review and update `README.md` with new features
- [ ] Run all tests: `./gradlew test connectedAndroidTest`
- [ ] Verify lint: `./gradlew lint`
- [ ] Test on multiple devices/API levels

### Signing Configuration

```bash
# Set environment variables
export STORE_PASSWORD="your-password"
export KEY_ALIAS="androiddebugkey"
export KEY_PASSWORD="your-password"

# Build release APK
./gradlew assembleIranRelease
./gradlew assembleGlobalRelease
```

### Post-Build Verification

```bash
# Verify APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/iran/release/app-iran-release.apk

# Check APK contents
unzip -l app/build/outputs/apk/iran/release/app-iran-release.apk

# Get APK info
aapt dump badging app/build/outputs/apk/iran/release/app-iran-release.apk
```

## Continuous Integration (CI/CD)

### GitHub Actions Example

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Gradle
        run: ./gradlew build
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/*/debug/*.apk
```

## Troubleshooting Build Issues

### Build Fails with Gradle Sync Error

```bash
# Solution 1: Clean and sync
./gradlew clean
./gradlew sync

# Solution 2: Invalidate cache in Android Studio
File → Invalidate Caches → Invalidate and Restart
```

### Out of Memory During Build

```bash
# Increase heap size in gradle.properties
org.gradle.jvmargs=-Xmx8192m -Dfile.encoding=UTF-8
```

### Kotlin Compilation Errors

```bash
# Clean and rebuild
./gradlew clean
./gradlew compileIranDebugKotlin

# Check Kotlin version compatibility
./gradlew dependencies | grep kotlin
```

### Dependency Resolution Issues

```bash
# Refresh dependencies
./gradlew build --refresh-dependencies

# Check dependency tree
./gradlew dependencies
./gradlew dependencyInsight --dependency androidx.compose.ui:ui
```

### ProGuard/R8 Errors

```bash
# Disable minification temporarily
# In app/build.gradle.kts, set isMinifyEnabled = false

# Or add rules to app/proguard-rules.pro
-keep class com.watermelon.player.** { *; }
```

## Performance Optimization

### Faster Builds

```bash
# Enable parallel builds
org.gradle.parallel=true

# Increase workers
org.gradle.workers.max=8

# Use build cache
org.gradle.caching=true

# Skip tests during development
./gradlew assembleIranDebug -x test
```

### Gradle Daemon

```bash
# Allocate more memory to daemon
org.gradle.jvmargs=-Xmx4096m

# Monitor daemon
jps -l | grep GradleDaemon
```

## APK Analysis

### Size Analysis

```bash
# Generate APK analyzer report
./gradlew bundleIranRelease

# Analyze in Android Studio
Build → Analyze APK → select APK file
```

### Dependency Tree

```bash
# View dependency tree
./gradlew dependencies

# Filter by configuration
./gradlew dependencies --configuration iranDebugRuntimeClasspath
```

## Deployment

### Local Testing

```bash
# Install and run
./gradlew installIranDebug

# Monitor logs
adb logcat | grep WatermelonPlayer
```

### Play Store Submission

```bash
# Build release bundle
./gradlew bundleIranRelease
./gradlew bundleGlobalRelease

# Upload to Play Console
# https://play.google.com/console/
```

### Direct APK Distribution

```bash
# Build release APK
./gradlew assembleIranRelease

# Share APK
# app/build/outputs/apk/iran/release/app-iran-release.apk
```

## Advanced Gradle Tasks

### Custom Tasks

```bash
# List all available tasks
./gradlew tasks

# List tasks for specific project
./gradlew app:tasks

# Run specific task
./gradlew app:assembleIranDebug
```

### Task Dependencies

```bash
# View task dependencies
./gradlew assembleIranDebug --dry-run

# Execute with dependencies
./gradlew clean assembleIranDebug
```

## Environment Variables

### Build Configuration

| Variable | Purpose | Example |
|----------|---------|---------|
| `STORE_PASSWORD` | Keystore password | `export STORE_PASSWORD=mypass` |
| `KEY_ALIAS` | Key alias | `export KEY_ALIAS=androiddebugkey` |
| `KEY_PASSWORD` | Key password | `export KEY_PASSWORD=mypass` |
| `JAVA_HOME` | Java location | `export JAVA_HOME=/usr/lib/jvm/java-17` |
| `ANDROID_HOME` | Android SDK | `export ANDROID_HOME=$HOME/Android/Sdk` |

## Build Properties

### gradle.properties Configuration

```properties
# JVM Arguments
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8

# Parallel builds
org.gradle.parallel=true
org.gradle.workers.max=8

# Build cache
org.gradle.caching=true

# Kotlin
kotlin.code.style=official

# Android
android.useAndroidX=true
android.enableJetifier=true
```

## Documentation

- [Gradle User Guide](https://docs.gradle.org/)
- [Android Gradle Plugin](https://developer.android.com/studio/build)
- [Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle.html)
- [Jetpack Compose Build](https://developer.android.com/jetpack/compose/setup)

---

**Last Updated**: Batch 1 Foundation  
**Status**: Build Instructions Complete
