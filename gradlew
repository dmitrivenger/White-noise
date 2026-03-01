#!/bin/bash
# Gradle wrapper script for macOS/Linux
set -e

# Get the directory where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Check if gradle is available
if command -v gradle &> /dev/null; then
    exec gradle "$@"
else
    echo "Error: gradle not found in PATH"
    exit 1
fi
