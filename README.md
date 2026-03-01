# White Noise - Sleep Support Android App

A lightweight Android application designed to help people fall asleep with customizable white noise.

## Features

- **Continuous White Noise Playback**: Soothing audio to help you relax and sleep
- **Volume Control**: Adjustable volume slider to find your perfect level
- **Pitch/Frequency Control**: Customize the tone of the white noise  
- **Sleep Timer**: Set an automatic timer to mute the sound after a specified time (0-120 minutes)
- **Minimalistic UI**: Clean, distraction-free interface with calm blue color scheme
- **Lightweight**: Optimized for performance and battery life
- **No Ads**: Pure functionality without advertisements

## Technical Details

- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Language**: Java + XML layouts
- **Architecture**: Service-based audio playback with UI control

## Building the APK

### Prerequisites
- JDK 17+
- Android SDK (API 34)
- Gradle 8.0+

### Quick Build
```bash
cd /path/to/White-noise
./gradlew clean assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Sign the APK (for Play Store)
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore your-keystore.jks app-release-unsigned.apk your-key-alias
```

## Installation

1. Enable "Unknown Sources" in your device settings
2. Download the APK file
3. Tap the APK to install
4. Grant permissions when prompted
5. Launch the app and enjoy!

## Usage

1. **Play**: Tap the Play button to start white noise
2. **Volume**: Adjust the volume slider (0-100%)
3. **Pitch**: Set the pitch/frequency (Low, Medium, High)
4. **Sleep Timer**: Set a timer (0-120 minutes) to auto-stop
5. **Stop**: Tap Stop to end playback

## Permissions

- **FOREGROUND_SERVICE**: Allows audio to continue playing in background
- **POST_NOTIFICATIONS**: For playback notifications

## Developer Notes

The app architecture consists of:
- `MainActivity`: UI control and user interactions
- `WhiteNoiseService`: Background service for audio playback (keeps playing even when app is in background)
- `AudioGenerator`: Generates white noise using PCM audio synthesis
- `TimerManager`: Handles sleep timer functionality

### Audio Generation
White noise is generated synthetically using randomized PCM samples with optional frequency shaping based on pitch setting.

## License

This project is open source and available under the MIT License.

## Contact

For bug reports or feature requests, please open an issue on GitHub.
