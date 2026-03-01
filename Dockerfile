FROM android:latest

# Install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    gradle \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_HOME /android
ENV PATH $PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept Android SDK licenses
RUN mkdir -p /android && \
    yes | sdkmanager --licenses 2>/dev/null || true

# Install required SDK components
RUN sdkmanager --update || true && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" || true

# Create working directory
WORKDIR /app

# Copy project
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build APK
RUN ./gradlew clean assembleRelease

# Output directory
RUN mkdir -p /output && \
    cp app/build/outputs/apk/release/app-release-unsigned.apk /output/ 2>/dev/null || \
    cp app/build/outputs/apk/debug/app-debug.apk /output/ 2>/dev/null || true

CMD ["bash", "-c", "echo 'APK build completed. Check /output directory.'"]
