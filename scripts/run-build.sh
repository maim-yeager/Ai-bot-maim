#!/usr/bin/env bash
set -euo pipefail

ROOT="$PWD"

if [ ! -f "$ROOT/local.properties" ] && [ -z "${ANDROID_SDK_ROOT-}" ]; then
  echo "Android SDK not configured locally. Running bootstrap to install minimal SDK..."
  ./scripts/bootstrap-android-sdk.sh
fi

echo "Starting Gradle build..."
chmod +x ./gradlew || true
./gradlew assembleDebug --no-daemon

echo "APK should be at: app/build/outputs/apk/debug/app-debug.apk"
