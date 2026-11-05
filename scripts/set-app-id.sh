#!/usr/bin/env bash
# NexVora Terminal: switch the application ID everywhere it must change, then validate.
#   scripts/set-app-id.sh com.termux            # test build that works with the OFFICIAL Termux bootstrap
#   scripts/set-app-id.sh com.nexvora.terminal  # branded build (needs a custom-prefix bootstrap)
# A "com.termux" build uses the same package name as the real Termux app: uninstall Termux first
# and never publish such a build as NexVora Terminal.
set -eu
NEW="${1:?usage: $0 <application_id>}"
cd "$(dirname "$0")/.."
OLD="$(grep -oP 'applicationId "\K[^"]+' app/build.gradle)"
[ "$OLD" = "$NEW" ] && { echo "already $NEW"; scripts/validate-package-id.sh "$NEW"; exit $?; }
sed -i "s|applicationId \"$OLD\"|applicationId \"$NEW\"|; s|manifestPlaceholders.TERMUX_PACKAGE_NAME = \"$OLD\"|manifestPlaceholders.TERMUX_PACKAGE_NAME = \"$NEW\"|" app/build.gradle
sed -i "s|TERMUX_PACKAGE_NAME = \"$OLD\"|TERMUX_PACKAGE_NAME = \"$NEW\"|" termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java
for f in app/src/main/res/values/strings.xml termux-shared/src/main/res/values/strings.xml; do
  sed -i "s|<!ENTITY TERMUX_PACKAGE_NAME \"$OLD\">|<!ENTITY TERMUX_PACKAGE_NAME \"$NEW\">|; s|/data/data/$OLD/files/usr|/data/data/$NEW/files/usr|" "$f"
done
sed -i "s|android:targetPackage=\"$OLD\"|android:targetPackage=\"$NEW\"|" app/src/main/res/xml/shortcuts.xml
echo "application ID: $OLD -> $NEW"
scripts/validate-package-id.sh "$NEW"
