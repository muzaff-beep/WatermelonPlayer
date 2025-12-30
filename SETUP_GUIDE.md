# Watermelon Player - Setup Guide

Complete step-by-step guide to set up and build the Watermelon Player project.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Project Configuration](#project-configuration)
4. [Building the Project](#building-the-project)
5. [Running on Device](#running-on-device)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows, macOS, or Linux
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: 20GB free space

### Software Requirements

| Software | Version | Purpose |
|----------|---------|---------|
| Android Studio | Flamingo or later | IDE |
| Java Development Kit | 17 or higher | Compilation |
| Android SDK | API 35 | Target platform |
| Gradle | 8.5.0+ | Build system |
| Git | Latest | Version control |

## Environment Setup

### 1. Install Java Development Kit (JDK)

#### Windows
```bash
# Using Chocolatey
choco install openjdk17

# Or download from https://adoptium.net/
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@17

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
```

### 2. Install Android Studio

1. Download from [developer.android.com](https://developer.android.com/studio)
2. Install following platform-specific instructions
3. Launch Android Studio
4. Complete initial setup wizard

### 3. Configure Android SDK

1. Open Android Studio
2. Go to **File → Settings → Appearance & Behavior → System Settings → Android SDK**
3. Install the following:
   - **SDK Platforms**: Android 15 (API 35)
   - **SDK Tools**:
     - Android SDK Build-Tools 35.0.0
     - Android Emulator
     - Android SDK Platform-Tools
     - Google Play Services

### 4. Set Environment Variables

#### Windows (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:ANDROID_HOME = "C:\Users\YourUsername\AppData\Local\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
```

#### macOS/Linux (Bash)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
```

Add to `~/.bashrc` or `~/.zshrc` for persistence.

## Project Configuration

### 1. Clone the Repository

```bash
git clone <repository-url>
cd WatermelonPlayer
```

### 2. Verify Gradle Wrapper

```bash
# Check if gradlew exists
ls -la gradlew

# Make it executable (Linux/macOS)
chmod +x gradlew
```

### 3. Configure Gradle Properties

Edit `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official

# Optional: Enable parallel builds
org.gradle.parallel=true
org.gradle.workers.max=8
```

### 4. Create Local Properties

Create `local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk
ndk.dir=/path/to/Android/Sdk/ndk/25.1.8937393
```

### 5. Configure Signing (Release Builds Only)

#### Create Keystore

```bash
# Generate keystore file
keytool -genkey -v -keystore app/watermelon.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias androiddebugkey

# When prompted:
# - Keystore password: [your-password]
# - Key password: [your-password]
# - Common Name: Your Name
# - Organizational Unit: Your Company
# - Organization: Your Company
# - City: Your City
# - State: Your State
# - Country: US
```

#### Set Environment Variables

```bash
# Linux/macOS
export STORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="androiddebugkey"
export KEY_PASSWORD="your-key-password"

# Windows (PowerShell)
$env:STORE_PASSWORD = "your-keystore-password"
$env:KEY_ALIAS = "androiddebugkey"
$env:KEY_PASSWORD = "your-key-password"
```

## Building the Project

### 1. Sync Gradle Files

```bash
# Using Gradle wrapper
./gradlew sync

# Or in Android Studio: File → Sync Now
```

### 2. Build Debug APK

#### Iran Edition
```bash
./gradlew assembleIranDebug
# Output: app/build/outputs/apk/iran/debug/app-iran-debug.apk
```

#### Global Edition
```bash
./gradlew assembleGlobalDebug
# Output: app/build/outputs/apk/global/debug/app-global-debug.apk
```

#### All Debug Variants
```bash
./gradlew assembleDebug
```

### 3. Build Release APK

#### Iran Edition
```bash
./gradlew assembleIranRelease
# Output: app/build/outputs/apk/iran/release/app-iran-release.apk
```

#### Global Edition
```bash
./gradlew assembleGlobalRelease
# Output: app/build/outputs/apk/global/release/app-global-release.apk
```

### 4. Build Bundle (for Play Store)

```bash
./gradlew bundleIranRelease
./gradlew bundleGlobalRelease
# Output: app/build/outputs/bundle/
```

### Build Troubleshooting

| Issue | Solution |
|-------|----------|
| `Gradle sync failed` | Run `./gradlew clean` then sync again |
| `Unsupported Java version` | Ensure JDK 17+ is installed and JAVA_HOME is set |
| `SDK not found` | Configure Android SDK path in `local.properties` |
| `Out of memory` | Increase heap: `org.gradle.jvmargs=-Xmx8192m` |

## Running on Device

### 1. Connect Android Device

```bash
# Enable USB Debugging on device
# Settings → Developer Options → USB Debugging

# Verify connection
adb devices
# Output: device-id    device
```

### 2. Install Debug APK

#### Using Gradle
```bash
./gradlew installIranDebug
./gradlew installGlobalDebug
```

#### Using ADB
```bash
adb install app/build/outputs/apk/iran/debug/app-iran-debug.apk
adb install app/build/outputs/apk/global/debug/app-global-debug.apk
```

### 3. Run from Android Studio

1. Select build variant (Iran Debug or Global Debug)
2. Click **Run** button or press `Shift + F10`
3. Select connected device or emulator
4. App launches on device

### 4. View Logs

```bash
# Real-time logs
adb logcat

# Filter by app
adb logcat | grep "WatermelonPlayer"

# In Android Studio: View → Tool Windows → Logcat
```

## Running on Emulator

### 1. Create Virtual Device

1. Open Android Studio
2. Go to **Tools → Device Manager**
3. Click **Create Device**
4. Select device profile (Pixel 6 recommended)
5. Select API 35 image
6. Configure settings and create

### 2. Launch Emulator

```bash
# List available emulators
emulator -list-avds

# Launch specific emulator
emulator -avd Pixel_6_API_35

# Or use Android Studio: Tools → Device Manager → Play button
```

### 3. Install and Run

Same as device installation (see above).

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class

```bash
./gradlew testIranDebugUnitTest --tests "com.watermelon.player.*.Test"
```

## Development Workflow

### 1. Daily Development

```bash
# Clean build
./gradlew clean

# Build and install debug APK
./gradlew installIranDebug

# Run tests
./gradlew test
```

### 2. Code Analysis

```bash
# Lint checks
./gradlew lint

# Ktlint formatting
./gradlew ktlintFormat
```

### 3. Build Optimization

```bash
# Profile build
./gradlew assembleIranDebug --profile

# View build report
open build/reports/profile/profile-*.html
```

## Troubleshooting

### Common Issues and Solutions

#### Issue: "Could not find com.android.tools.build:gradle:8.5.0"

**Solution**: Update Gradle plugin
```bash
./gradlew wrapper --gradle-version 8.5.0
```

#### Issue: "Execution failed for task ':app:compileDebugKotlin'"

**Solution**: Clear build cache
```bash
./gradlew clean
./gradlew build
```

#### Issue: "No connected devices found"

**Solution**: 
1. Enable USB Debugging on device
2. Install USB drivers (Windows)
3. Restart adb: `adb kill-server && adb start-server`

#### Issue: "Gradle daemon stopped unexpectedly"

**Solution**: Stop and restart daemon
```bash
./gradlew --stop
./gradlew build
```

#### Issue: "Insufficient permissions to access /dev/kvm"

**Solution** (Linux):
```bash
sudo chmod 666 /dev/kvm
```

#### Issue: "Build fails with ProGuard errors"

**Solution**: Update ProGuard rules in `app/proguard-rules.pro`

### Getting Help

1. Check Android Studio logs: **Help → Show Log in Explorer**
2. Review Gradle build output: `./gradlew build --info`
3. Check official documentation:
   - [Android Developers](https://developer.android.com/)
   - [Gradle Documentation](https://gradle.org/documentation/)
   - [Jetpack Compose](https://developer.android.com/jetpack/compose)

## Next Steps

1. ✅ Project setup complete
2. 📱 Build and run on device
3. 📝 Review code structure in `ARCHITECTURE.md`
4. 🔧 Implement features (Batch 2)
5. 🧪 Add unit tests
6. 📦 Prepare for release

---

**Last Updated**: Batch 1 Foundation  
**Status**: Setup Guide Complete
