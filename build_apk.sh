#!/bin/bash

# White Noise APK Build Script
# This script automates the building of the White Noise Android APK

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "========================================="
echo "White Noise APK Builder"
echo "========================================="
echo ""

# Check for required tools
echo "Checking prerequisites..."

if ! command -v gradle &> /dev/null; then
    # Try using gradle wrapper
    if [ ! -f "./gradlew" ]; then
        echo "Error: gradle wrapper not found. Please ensure you have Gradle installed or use './gradlew' from the project root."
        exit 1
    fi
    GRADLE_CMD="./gradlew"
else
    GRADLE_CMD="gradle"
fi

if ! command -v java &> /dev/null; then
    echo "Error: Java not found. Please install JDK 17 or later."
    exit 1
fi

echo "✓ Java: $(java -version 2>&1 | head -1)"
echo "✓ Gradle: OK"
echo ""

# Check Android SDK
if [ -z "$ANDROID_HOME" ]; then
    # Try common installation paths
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    elif [ -d "/opt/android-sdk" ]; then
        export ANDROID_HOME="/opt/android-sdk"
    else
        echo "Warning: ANDROID_HOME not set. Please set it to your Android SDK location."
        echo "Example: export ANDROID_HOME=\$HOME/Library/Android/sdk"
        exit 1
    fi
fi

echo "Using Android SDK: $ANDROID_HOME"
echo ""

# Check if API 34 is available
if [ ! -d "$ANDROID_HOME/platforms/android-34" ]; then
    echo "Installing Android SDK API 34..."
    echo "You may need to manually install it using Android Studio:"
    echo "  1. Open Android Studio"
    echo "  2. Go to SDK Manager"
    echo "  3. Install Android API 34 and Build Tools 34.x.x"
    echo ""
fi

# Clean and build
echo "Building APK..."
echo ""

$GRADLE_CMD clean
$GRADLE_CMD assembleRelease -x lintVitalRelease

APK_PATH="$PROJECT_DIR/app/build/outputs/apk/release/app-release-unsigned.apk"

if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo ""
    echo "========================================="
    echo "✓ Build Successful!"
    echo "========================================="
    echo "APK Location: $APK_PATH"
    echo "APK Size: $APK_SIZE"
    echo ""
    echo "Next steps:"
    echo "1. To sign the APK for Play Store, run:"
    echo "   jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \\"
    echo "     -keystore your-keystore.jks \"$APK_PATH\" your-key-alias"
    echo ""
    echo "2. Or simply install on a test device (development):"
    echo "   adb install \"$APK_PATH\""
else
    echo ""
    echo "========================================="
    echo "✗ Build Failed"
    echo "========================================="
    echo "The APK was not generated. Check the output above for errors."
    exit 1
fi
