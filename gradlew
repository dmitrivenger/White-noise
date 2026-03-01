#!/bin/bash
###############################################################################
# Gradle wrapper script for macOS/Linux
###############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# OS specific support (must be 'true' or 'false').
darwin=false
case "$(uname)" in
Darwin*)
    darwin=true
    ;;
esac

# Determine the Java command to use to start the JVM.
if [[ -n "$JAVA_HOME" ]]; then
    if [[ -x "$JAVA_HOME/bin/java" ]]; then
        javaexe="$JAVA_HOME/bin/java"
    else
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    javaexe="$(type -p java)" || {
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
        exit 1
    }
fi

if ! [[ -x "$javaexe" ]]; then
    echo "ERROR: Java executable not found at: $javaexe"
    exit 1
fi

# Increase the default stack size for Gradle
if $darwin; then
    DEFAULT_JVM_OPTS="-XX:+IgnoreUnrecognizedVMOptions -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -Xms1024m -Xmx2048m"
else
    DEFAULT_JVM_OPTS="-XX:+IgnoreUnrecognizedVMOptions -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -Xms1024m -Xmx2048m"
fi

GRADLE_OPTS="${GRADLE_OPTS:-$DEFAULT_JVM_OPTS}"

# Collect all arguments for the java command
exec "$javaexe" \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_NAME" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
