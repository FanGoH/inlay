#!/usr/bin/env bash
# Build the FanGoH debug APK and publish a GitHub prerelease for Obtainium.
# The APK versionName must equal TAG without a leading v, and versionCode
# must be higher than the previously installed build, or Obtainium will
# download an "update" that PackageInstaller then ignores.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

TAG="${1:-}"
if [[ -z "$TAG" ]]; then
  echo "usage: $0 vX.Y.Z-rc.N" >&2
  exit 1
fi
expect="${TAG#v}"

versionName=$(awk -F'"' '/versionName = / { print $2; exit }' app/build.gradle.kts)
versionCode=$(awk -F'= ' '/versionCode = / { print $2; exit }' app/build.gradle.kts)
if [[ "$versionName" != "$expect" ]]; then
  echo "versionName $versionName does not match tag $TAG (expected $expect)." >&2
  echo "Bump app/build.gradle.kts before publishing." >&2
  exit 1
fi

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-arm64}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

"$root/tools/build-debug.sh"

src="$root/app/build/outputs/apk/debug/app-debug.apk"
AAPT2="${AAPT2:-$ANDROID_HOME/build-tools/37.0.0/aapt2}"
badging=$("$AAPT2" dump xmltree "$src" --file AndroidManifest.xml)
echo "$badging" | grep -F "versionName" | grep -F "\"$expect\"" >/dev/null
echo "$badging" | grep -F "versionCode" | grep -F "=$versionCode" >/dev/null

mkdir -p "$root/dist"
asset="$root/dist/Inlay-debug.apk"
install -m 0644 "$src" "$asset"

notes=$(cat <<EOF
FanGoH debug build \`$TAG\` (dual-stick + developer keys).

APK \`versionName\` is \`$expect\`, \`versionCode\` $versionCode — these must match the GitHub tag so Obtainium can actually install.

Install \`Inlay-debug.apk\` (lab Android Debug cert). Same package id as upstream Inlay, **different signing key**: it overlays a previous FanGoH debug install, not the official release. If PackageInstaller does nothing, uninstall official Inlay once, then install this APK.
EOF
)

gh release create "$TAG" "$asset" \
  --repo FanGoH/inlay \
  --prerelease \
  --title "Inlay $expect (FanGoH dual-stick + developer keys)" \
  --notes "$notes"

echo "Published $TAG with $asset"
