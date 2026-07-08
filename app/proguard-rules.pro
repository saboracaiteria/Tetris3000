# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep OpenGL classes
-keep class javax.microedition.khronos.** { *; }

# Keep GLSurfaceView
-keep class android.opengl.** { *; }
