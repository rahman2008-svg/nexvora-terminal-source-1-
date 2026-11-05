#!/usr/bin/env bash
# NexVora Terminal: verifies every place that must carry the application ID agrees with it.
# Usage: scripts/validate-package-id.sh [expected_id]   (default: com.nexvora.terminal)
set -u
ID="${1:-com.nexvora.terminal}"
cd "$(dirname "$0")/.."
fail=0
chk() { # description, file, fixed-string
  if grep -qF -- "$3" "$2"; then echo "OK    $1"; else echo "FAIL  $1  ($2 lacks: $3)"; fail=1; fi
}
chk "applicationId"                      app/build.gradle "applicationId \"$ID\""
chk "manifest placeholder"               app/build.gradle "manifestPlaceholders.TERMUX_PACKAGE_NAME = \"$ID\""
chk "TermuxConstants.TERMUX_PACKAGE_NAME" termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java "TERMUX_PACKAGE_NAME = \"$ID\""
chk "app strings entity"                 app/src/main/res/values/strings.xml "<!ENTITY TERMUX_PACKAGE_NAME \"$ID\">"
chk "shared strings entity"              termux-shared/src/main/res/values/strings.xml "<!ENTITY TERMUX_PACKAGE_NAME \"$ID\">"
chk "shared prefix entity"               termux-shared/src/main/res/values/strings.xml "/data/data/$ID/files/usr"
chk "shortcuts targetPackage"            app/src/main/res/xml/shortcuts.xml "android:targetPackage=\"$ID\""
if [ "$ID" != "com.termux" ] && grep -q 'targetPackage="com.termux"' app/src/main/res/xml/shortcuts.xml; then
  echo "FAIL  shortcuts.xml still has targetPackage=com.termux"; fail=1; fi
# Java package names (com.termux.*) and JNI symbol names (Java_com_termux_*) are intentionally UNCHANGED:
# the Java namespace is independent of the applicationId.
echo "NOTE  Java namespace stays com.termux (JNI symbols Java_com_termux_* must match it)."
exit $fail
