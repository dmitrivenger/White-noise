# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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

# Keep white noise app classes
-keep class com.whitenoise.app.** { *; }
-keep class com.whitenoise.app.MainActivity { *; }
-keep class com.whitenoise.app.WhiteNoiseService { *; }
-keep class com.whitenoise.app.AudioGenerator { *; }
-keep class com.whitenoise.app.TimerManager { *; }
-keep class com.whitenoise.app.TimerManager$TimerCallback { *; }
-keep class com.whitenoise.app.WhiteNoiseService$LocalBinder { *; }
