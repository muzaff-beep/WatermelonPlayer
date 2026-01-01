# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve annotations for Jetpack Compose
-keepattributes RuntimeVisibleAnnotations

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep Koin definitions
-keep class org.koin.** { *; }

# Keep Coil image loading
-keep class coil.** { *; }

# Don't warn about okhttp
-dontwarn okhttp3.**

# Don't warn about retrofit
-dontwarn retrofit2.**

# Security crypto
-keep class androidx.security.** { *; }

# For debugging, keep line numbers
-keepattributes SourceFile,LineNumberTable

# Optimize: remove logging in release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep parcelize
-keep class * implements kotlinx.parcelize.Parceler { *; }
