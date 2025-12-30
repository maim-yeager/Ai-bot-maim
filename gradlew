#!/usr/bin/env bash
set -e
# Minimal gradlew shim: tries local wrapper, then system gradle
ROOT="$(cd "$(dirname "$0")" && pwd)"
if [ -x "$ROOT/gradlew" ] && [ "$0" != "$ROOT/gradlew" ]; then
  exec "$ROOT/gradlew" "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
# Try to download a small gradle and run
GRADLE_VERSION=8.4.1
DIST="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
TMPDIR="${ROOT}/.gradle-temp"
mkdir -p "$TMPDIR"
ZIP="$TMPDIR/gradle.zip"
if [ ! -d "$TMPDIR/gradle-${GRADLE_VERSION}" ]; then
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  curl -Ls "$DIST" -o "$ZIP"
  unzip -o "$ZIP" -d "$TMPDIR" >/dev/null
fi
exec "$TMPDIR/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
