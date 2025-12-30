# Keep entry points
-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

# Keep Kotlin metadata
-keepclassmembers class **.kotlin.Metadata {
    public *;
}

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class * {
    *;
}
