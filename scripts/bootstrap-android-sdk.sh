#!/usr/bin/env bash
set -euo pipefail

# Minimal Android SDK bootstrap for CI/local dev (installs command line tools + platform-tools + build-tools)
# Usage: ./scripts/bootstrap-android-sdk.sh

ROOT="$PWD"
SDK_ROOT="$HOME/Android/Sdk"
CMDLINE_ZIP="commandlinetools-linux-9477386_latest.zip"
CMDLINE_URL="https://dl.google.com/android/repository/${CMDLINE_ZIP}"

mkdir -p "$SDK_ROOT/cmdline-tools"
cd "$HOME/Downloads" || mkdir -p "$HOME/Downloads" && cd "$HOME/Downloads"

if [ ! -f "$CMDLINE_ZIP" ]; then
  echo "Downloading Android commandline tools..."
  curl -Ls -o "$CMDLINE_ZIP" "$CMDLINE_URL"
fi

unzip -o "$CMDLINE_ZIP" -d "$SDK_ROOT/cmdline-tools" >/dev/null
# cmdline-tools package unzips to cmdline-tools/ (folder), move to latest
mkdir -p "$SDK_ROOT/cmdline-tools/latest"
cp -r "$SDK_ROOT/cmdline-tools/cmdline-tools"/* "$SDK_ROOT/cmdline-tools/latest/" || true

export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/cmdline-tools/latest/bin:$PATH"

echo "Installing platform-tools and build tools (this may take a few minutes)..."
# Run sdkmanager verbosely to show errors if any
yes | sdkmanager --sdk_root="$SDK_ROOT" "platform-tools" "platforms;android-34" "build-tools;34.0.0" || {
  echo "sdkmanager failed with exit code $?"
  exit 1
}

cat > "$ROOT/local.properties" <<EOF
sdk.dir=$SDK_ROOT
EOF

echo "Android SDK bootstrapped at $SDK_ROOT" 

echo "Now you can run: ./gradlew assembleDebug"
