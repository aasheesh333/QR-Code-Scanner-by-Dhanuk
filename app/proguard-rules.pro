# Add project specific ProGuard rules here.

# Keep Kotlin metadata for reflection-based libraries
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, AnnotationDefault

# Keep Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep OneSignal
-keep class com.onesignal.** { *; }
-keep class com.google.firebase.** { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }

# Keep all database model classes (entities, DAOs, converters)
-keep class com.dhanuk.quickscanpro.database.** { *; }

# Keep BuildConfig values
-keep class com.dhanuk.quickscanpro.BuildConfig { *; }

# ML Kit keeps
-keep class com.google.mlkit.** { *; }

# ZXing keeps
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# CameraX keeps
-keep class androidx.camera.** { *; }

# Coroutines keeps
-keepclassmembernames class kotlinx.** { *; }
-dontwarn kotlinx.coroutines.**

# DataStore keeps
-keep class androidx.datastore.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Suppress warnings for generated/reference code
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

