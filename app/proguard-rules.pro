# --- General Android & Project Rules ---

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# --- Room Database ---
# Room uses reflection to find the generated implementation of your @Database class.
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao
-keep class * extends androidx.room.Entity
-keep @androidx.room.Entity class * { *; }

# --- Hilt / Dagger ---
# Hilt relies on generated classes and reflection for injection.
-keep class com.google.dagger.** { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends hilt.**
-keep interface dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# --- Kotlin Serialization ---
# Keep serializable classes and their properties for JSON parsing.
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# --- Coil (Image Loading) ---
# Coil rules to prevent issues with image decoding.
-keep class coil3.** { *; }
-dontwarn coil3.**

# --- Markdown Renderer ---
# Prevent optimization from breaking the markdown parsing logic.
-keep class com.mikepenz.markdown.** { *; }

# --- Data Models ---
# Keep your domain and database entities to prevent mapping errors.
-keep class com.magpiny.notafo.domain.model.** { *; }
-keep class com.magpiny.notafo.data.db.** { *; }
