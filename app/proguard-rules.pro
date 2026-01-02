# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }

# Keep Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Database class * { *; }

# Keep ExoPlayer
-keep class androidx.media3.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep our models
-keep class com.watermelon.player.database.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel

# Keep custom classes
-keep class com.watermelon.player.** { *; }

# Remove logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
