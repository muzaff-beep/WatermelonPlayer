# Watermelon Player - Batch 1 Foundation

A production-ready native Android music player application built with **Kotlin + Jetpack Compose** featuring dual-edition architecture for Iran and Global markets.

## 🎯 Project Overview

**Watermelon Player** is a modern Android application with:

- **100% Native Android Development**: Kotlin + Jetpack Compose UI framework
- **Dual Edition Architecture**: Separate product flavors for Iran (`ir`) and Global (`global`) editions
- **Production-Ready**: Fully configured build system, signing, and optimization
- **Modern Tech Stack**: ExoPlayer for media playback, Koin for dependency injection, Material3 design

## 📁 Project Structure

```
WatermelonPlayer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/watermelon/player/
│   │   │   │   ├── MainActivity.kt              # Main activity with Compose UI
│   │   │   │   ├── WatermelonApp.kt             # Application class with DI setup
│   │   │   │   ├── EditionManager.kt            # Edition detection logic
│   │   │   │   ├── di/
│   │   │   │   │   └── AppModule.kt             # Koin dependency injection module
│   │   │   │   ├── player/
│   │   │   │   │   └── PlaybackService.kt       # Media playback service
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Theme.kt                 # Material3 theme
│   │   │   │       ├── Color.kt                 # Color palette
│   │   │   │       └── Type.kt                  # Typography
│   │   │   ├── AndroidManifest.xml              # App manifest with permissions
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml              # String resources
│   │   │       │   └── themes.xml               # Theme resources
│   │   │       └── xml/
│   │   │           ├── network_security_config.xml
│   │   │           ├── data_extraction_rules.xml
│   │   │           └── backup_rules.xml
│   │   └── test/                                # Unit tests (placeholder)
│   ├── build.gradle.kts                         # App-level build configuration
│   └── proguard-rules.pro                       # ProGuard rules for minification
├── build.gradle.kts                             # Root build configuration
├── settings.gradle.kts                          # Project settings
├── gradle.properties                            # Gradle properties
├── .gitignore                                   # Git ignore rules
└── README.md                                    # This file
```

## 🛠️ Build Configuration

### Dual Edition Architecture

The project uses Gradle product flavors to maintain two separate editions:

#### Iran Edition (`iranDebug` / `iranRelease`)
- **Application ID**: `com.watermelon.player.ir`
- **App Name**: "پخش کننده هندوانه" (Persian)
- **Edition Flag**: `IS_IRAN_EDITION = true`
- **Dependencies**: Zarinpal, IDPay payment gateways
- **Version Suffix**: `-ir`

#### Global Edition (`globalDebug` / `globalRelease`)
- **Application ID**: `com.watermelon.player.global`
- **App Name**: "Watermelon Player" (English)
- **Edition Flag**: `IS_IRAN_EDITION = false`
- **Dependencies**: Google Play Billing, TensorFlow Lite
- **Version Suffix**: `-global`

### Build Variants

```bash
# Debug builds
./gradlew assembleIranDebug      # Iran debug APK
./gradlew assembleGlobalDebug    # Global debug APK

# Release builds
./gradlew assembleIranRelease    # Iran release APK
./gradlew assembleGlobalRelease  # Global release APK

# Install on connected device
./gradlew installIranDebug
./gradlew installGlobalDebug
```

## 📋 Key Dependencies

### Core Android
- `androidx.core:core-ktx:1.13.1`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.8.0`
- `androidx.activity:activity-compose:1.9.3`

### Jetpack Compose (UI)
- `androidx.compose.ui:ui`
- `androidx.compose.material3:material3`
- `androidx.compose.material:material-icons-extended`

### Media Playback
- `androidx.media3:media3-exoplayer:1.3.1` (ExoPlayer)
- `androidx.media3:media3-ui:1.3.1`

### Dependency Injection
- `io.insert-koin:koin-android:3.5.6`
- `io.insert-koin:koin-androidx-compose:3.5.6`

### Edition-Specific
- **Iran**: Zarinpal, IDPay payment SDKs
- **Global**: Google Play Billing, TensorFlow Lite

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Flamingo or later)
- **Java Development Kit (JDK)** 17 or higher
- **Android SDK** API 35 (Android 15)
- **Gradle** 8.5.0+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd WatermelonPlayer
   ```

2. **Open in Android Studio**
   - File → Open → Select `WatermelonPlayer` directory
   - Android Studio will automatically sync Gradle files

3. **Configure Signing (Release Builds)**
   - Create `app/watermelon.jks` keystore file
   - Set environment variables:
     ```bash
     export STORE_PASSWORD=your_store_password
     export KEY_ALIAS=your_key_alias
     export KEY_PASSWORD=your_key_password
     ```

4. **Build and Run**
   ```bash
   # Build debug APK
   ./gradlew assembleIranDebug
   
   # Install on device
   ./gradlew installIranDebug
   
   # Run tests
   ./gradlew test
   ```

## 🔧 Configuration

### Compile Options
- **Source Compatibility**: Java 17
- **Target Compatibility**: Java 17
- **Kotlin JVM Target**: 17
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

### Build Features
- **Jetpack Compose**: Enabled
- **BuildConfig**: Enabled for dynamic configuration
- **Kotlin Compiler Extension**: 1.5.14

### Optimization
- **Minification**: Enabled for release builds
- **Resource Shrinking**: Enabled for release builds
- **ProGuard**: Configured with custom rules
- **ABI Split**: ARM64 only (modern devices)

## 📦 Release Build

### Prerequisites for Release
1. Keystore file (`app/watermelon.jks`)
2. Environment variables for signing credentials
3. ProGuard rules configured

### Building Release APK
```bash
# Set signing credentials
export STORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_password

# Build release APK
./gradlew assembleIranRelease
./gradlew assembleGlobalRelease
```

### Output
- **Iran Release**: `app/build/outputs/apk/iran/release/app-iran-release.apk`
- **Global Release**: `app/build/outputs/apk/global/release/app-global-release.apk`

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 📱 Supported Devices

- **Minimum API Level**: 21 (Android 5.0 Lollipop)
- **Target API Level**: 35 (Android 15)
- **Architecture**: ARM64-v8a (64-bit only)
- **RTL Support**: Enabled

## 🎨 UI Framework

The app uses **Jetpack Compose** with **Material3** design system:

- Modern declarative UI
- Real-time preview in Android Studio
- Dark/Light theme support
- Responsive layouts

### Theme Customization
Edit `app/src/main/java/com/watermelon/player/ui/theme/`:
- `Theme.kt` - Theme configuration
- `Color.kt` - Color palette
- `Type.kt` - Typography settings

## 🔐 Security

- **Network Security**: Cleartext traffic disabled by default
- **Backup**: Configured to exclude sensitive data
- **Data Extraction**: Restricted for privacy compliance
- **ProGuard**: Obfuscation enabled for release builds

## 📝 Permissions

The app requests the following permissions:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 🚦 Build Status

- ✅ Gradle configuration: Complete
- ✅ Dual edition setup: Complete
- ✅ Core dependencies: Complete
- ✅ UI framework: Complete
- ⏳ Media player features: Batch 2
- ⏳ Payment integration: Batch 2
- ⏳ Advanced features: Future batches

## 📚 Documentation

- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [ExoPlayer Documentation](https://exoplayer.dev/)
- [Koin Dependency Injection](https://insert-koin.io/)

## 📄 License

This project is proprietary and confidential.

## 👥 Support

For issues, feature requests, or contributions, please contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: Batch 1 Foundation  
**Status**: Production-Ready Foundation
