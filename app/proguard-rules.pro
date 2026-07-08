# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

-keep class com.example.tetris3d.** { *; }
-keepattributes *Annotation*
-dontwarn javax.microedition.khronos.**
-keep class javax.microedition.khronos.** { *; }
