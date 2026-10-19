#!/usr/bin/env bash
# Build an installable debug APK for handheld testing (Thor / Odin).
# Needs JDK 17+ and an Android SDK with the compileSdk used in app/build.gradle.kts.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "Set JAVA_HOME to a JDK 17 or newer." >&2
  exit 1
fi
if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" ]]; then
  echo "Set ANDROID_HOME or ANDROID_SDK_ROOT." >&2
  exit 1
fi
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
apk="$root/app/build/outputs/apk/debug/app-debug.apk"
echo "Debug APK: $apk"
ls -lh "$apk"
