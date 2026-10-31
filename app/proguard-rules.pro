# ProGuard configuration for ARIFA AI

# Keep data classes and models
-keepclassmembers class com.arifa.assistant.core.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retain Android Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
