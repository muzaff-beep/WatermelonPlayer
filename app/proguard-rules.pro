# Keep all classes that have the @Keep annotation
-keep class android.support.annotation.Keep
-keep @android.support.annotation.Keep class * {*;}

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity

# Keep Koin
-keep class org.koin.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Media3/ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-keep class androidx.media3.** { *; }

# Keep AndroidX components
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.** { *; }

# Keep application class
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep view models
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
