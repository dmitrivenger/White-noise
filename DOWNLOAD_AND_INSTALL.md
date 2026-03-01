# White Noise App - Download & Installation Guide

## Download the APK

### Option 1: Build It Yourself (Recommended for Security)

#### Using Docker (Easiest - No Android SDK needed!)
```bash
git clone https://github.com/dmitrivenger/White-noise.git
cd White-noise
docker-compose up --build
```
Your APK will be in `app/build/outputs/apk/release/`

#### Using Local Build Script
```bash
git clone https://github.com/dmitrivenger/White-noise.git
cd White-noise
chmod +x build_apk.sh
./build_apk.sh
```

#### Manual Build with Gradle
```bash
git clone https://github.com/dmitrivenger/White-noise.git
cd White-noise
./gradlew clean assembleRelease
```

See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for detailed platform-specific setup.

### Option 2: Download Pre-built APK from GitHub

1. Go to: https://github.com/dmitrivenger/White-noise/releases
2. Download the latest `app-release-unsigned.apk` or `app-debug.apk`
3. Follow installation instructions below

## Installation on Android

### For Debug APK (Quick Testing)
1. Enable "Unknown Sources" in Settings → Security
2. Transfer the APK file to your Android device (USB, AirDrop, etc.)
3. Open the APK file with a file manager
4. Tap "Install" when prompted
5. Grant permissions when the app launches

### For Release APK (Production)
The APK must be signed. If you built it locally, it will be unsigned.

#### Sign the APK:
```bash
keytool -genkey -v -keystore release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release-alias

jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release-key.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  release-alias
```

Then install via ADB:
```bash
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk
```

## System Requirements

- **Android Version**: 5.0 (API 21) or higher
- **Storage**: ~5 MB
- **Permissions Required**:
  - FOREGROUND_SERVICE: For background audio playback
  - POST_NOTIFICATIONS: For playback notifications

## Troubleshooting Installation

### "File type not recognized" error
- Make sure the file extension is `.apk`
- Verify the download completed successfully

### "Unknown app" or "Untrusted source" warning
- This is normal for apps not from Google Play Store
- Tap "Install anyway" (exact text varies by Android version)

### App crashes on launch
- Ensure you're on Android 5.0+
- Try uninstalling, reboot device, and reinstall
- Check device storage space

### Want official Play Store version?
- Coming Soon! Check back for official release
- Or build and submit yourself to Play Store

## App Permissions

The app requests two permissions:

1. **FOREGROUND_SERVICE** - Allows audio to play in background (essential for sleep timer to work)
2. **POST_NOTIFICATIONS** - Shows playback notifications

Both are necessary for the app to function properly.

## First Run

1. Open the White Noise app
2. Grant any permission prompts
3. Tap "Play" to start white noise
4. Adjust volume and pitch with sliders
5. Optional: Set sleep timer
6. Lock your phone - audio continues playing!

## Features Overview

- **Play/Stop**: Control playback
- **Volume**: 0-100% slider
- **Pitch**: Low/Medium/High frequency selection
- **Sleep Timer**: Auto-stop after 0-120 minutes
- **Background Audio**: Continues even when phone is locked

## FAQ

**Q: Will this work offline?**  
A: Yes! Audio is generated locally, no internet needed.

**Q: Does it drain battery?**  
A: Moderately - continuous audio requires significant processing. Battery drain depends on volume level and device.

**Q: Can I set a timer longer than 120 minutes?**  
A: Not in the current version, but you can manually stop it anytime.

**Q: Will it work with other audio apps?**  
A: No, only one audio app can play at a time on Android.

**Q: Is my data private?**  
A: Completely! The app doesn't connect to the internet or collect any data.

## Support & Reporting Issues

Found a bug? Have a suggestion? 
- Open an issue on GitHub: https://github.com/dmitrivenger/White-noise/issues
- Include your Android version and device model

## Development & Contributing

The app is open source! Check the README.md and BUILD_INSTRUCTIONS.md for development setup.

---

**Enjoy better sleep with White Noise!** 🎵😴
