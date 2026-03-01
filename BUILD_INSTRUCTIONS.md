# White Noise App - Build Instructions

This document provides detailed instructions for building the White Noise Android APK.

## Prerequisites

Before building, ensure you have installed:

1. **JDK 17 or later**
   - macOS: `brew install openjdk@17`
   - Linux: `sudo apt-get install openjdk-17-jdk`
   - Windows: Download from https://jdk.java.net/

2. **Android SDK**
   - Install Android Studio from https://developer.android.com/studio
   - Or install Android Command-Line Tools and set `ANDROID_HOME`

3. **Gradle** (optional, project includes gradle wrapper)
   - macOS: `brew install gradle`
   - Or use the included `./gradlew`

## Environment Setup

### macOS

```bash
# Install JDK
brew install openjdk@17
brew link openjdk@17

# Install Android SDK (via Android Studio)
# Or set ANDROID_HOME manually
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

Add to your `~/.zshrc` or `~/.bash_profile`:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### Linux

```bash
# Install JDK
sudo apt-get install openjdk-17-jdk

# Set Android SDK path (usually after Android Studio install)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Windows

```cmd
# Install JDK from jdk.java.net
# Then set environment variables
setx ANDROID_HOME C:\Users\YourUsername\AppData\Local\Android\Sdk
setx PATH %PATH%;%ANDROID_HOME%\platform-tools
```

## Building the APK

### Method 1: Using the Build Script (Easiest)

```bash
chmod +x build_apk.sh
./build_apk.sh
```

### Method 2: Manual Build

```bash
# Navigate to project directory
cd /path/to/White-noise

# Clean build (deletes previous builds)
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Or for debug APK (faster):
./gradlew assembleDebug
```

### Using Gradle (System Installation)

If you have Gradle installed system-wide:

```bash
gradle clean
gradle assembleRelease
```

## APK Locations

After building, find your APK at:

- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (if built)

## Installing on Device

### Debug APK (Development)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (Production - Requires Signing)

First, sign the APK:

```bash
# Create a keystore (if you don't have one)
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# Sign the APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  my-key-alias

# Verify signature
jarsigner -verify -verbose app/build/outputs/apk/release/app-release-unsigned.apk
```

Then install:

```bash
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

## Troubleshooting

### "ANDROID_HOME not set"

```bash
# Find your Android SDK location
# Usually: ~/Library/Android/sdk (macOS) or ~/Android/Sdk (Linux)

export ANDROID_HOME=~/Library/Android/sdk
./gradlew clean assembleRelease
```

### "API 34 not installed"

Install via Android Studio:
1. Open Android Studio
2. Tools → SDK Manager
3. Go to SDK Platforms tab
4. Check "Android 14 (API 34)"
5. Click "Apply"

Or via command line:

```bash
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### "Java version mismatch"

Ensure JDK 17+ is installed:

```bash
java -version
# Should show something like: openjdk version "17.x.x"

# If not, update JAVA_HOME:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Build hangs or times out

```bash
# Try increasing Java heap size
export _JAVA_OPTIONS=-Xmx4096m
./gradlew clean assembleRelease
```

## Optimizations

### Faster Builds

```bash
# Skip tests and linting
./gradlew assembleRelease -x test -x lint

# Parallel builds
./gradlew assembleRelease --parallel --max-workers=4
```

### Smaller APK

The project is already optimized, but you can enable additional optimization:

Edit `app/build.gradle`:
```gradle
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
}
```

## Publishing to Play Store

1. Sign your APK (see above)
2. Test thoroughly on real devices
3. Create Play Store account
4. Follow Google's publishing guidelines
5. Upload signed APK to Play Store

See: https://developer.android.com/studio/publish

## Support

For issues or questions:
- Check Android Studio logs
- Review Gradle error messages
- Check Java/SDK versions
- Open an issue on GitHub: https://github.com/dmitrivenger/White-noise/issues
