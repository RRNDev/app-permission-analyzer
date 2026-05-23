# App Permission Analyzer release rules.
#
# The app does not use reflection-based JSON parsing, DI, Room, WebView bridges,
# or custom serialization. Keep this file intentionally small so R8 can shrink
# and optimize Kotlin, Compose, and flavor-specific code effectively.

# Keep line numbers and enough Kotlin/Java metadata for readable crash reports
# and library callback signatures. Source file names are normalized below.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Keep members annotated with AndroidX Keep if they are introduced later.
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Android manifest components are handled by the Android Gradle Plugin. No broad
# app-code keep rules are needed here.
