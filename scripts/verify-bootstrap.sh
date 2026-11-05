#!/usr/bin/env bash
# NexVora Terminal: checks whether a Termux bootstrap zip was built for a given application ID.
# A bootstrap built for com.termux has "/data/data/com.termux/files/usr" hardcoded in ELF RUNPATHs,
# script shebangs, dpkg/apt configuration and SYMLINKS.txt, so it cannot run under another app ID.
# Usage: scripts/verify-bootstrap.sh <bootstrap-*.zip> [expected_app_id]
set -u
ZIP="${1:?usage: $0 bootstrap.zip [app_id]}"
ID="${2:-com.nexvora.terminal}"
OLD="com.termux"
W="$(mktemp -d)"; trap 'rm -rf "$W"' EXIT
unzip -q "$ZIP" -d "$W" || { echo "cannot unzip $ZIP"; exit 2; }
want="/data/data/$ID/files/usr"
have_old=$(grep -rl --binary-files=text "/data/data/$OLD/files/usr" "$W" 2>/dev/null | wc -l)
have_new=$(grep -rl --binary-files=text "$want" "$W" 2>/dev/null | wc -l)
echo "files referencing /data/data/$OLD/files/usr : $have_old"
echo "files referencing $want : $have_new"
if [ "$ID" = "$OLD" ]; then
  [ "$have_old" -gt 0 ] && { echo "RESULT: bootstrap matches $ID"; exit 0; }
  echo "RESULT: UNEXPECTED - no $OLD prefix found"; exit 1
fi
if [ "$have_old" -gt 0 ]; then
  echo "RESULT: INCOMPATIBLE - bootstrap was built for $OLD, not $ID."
  echo "        Binaries (bash, apt, dpkg, libs) will not start. A bootstrap built with"
  echo "        TERMUX_APP__PACKAGE_NAME=$ID from termux-packages is required (see docs/NEXVORA.md)."
  exit 1
fi
[ "$have_new" -gt 0 ] && { echo "RESULT: static check passed for $ID (still run the on-device tests)"; exit 0; }
echo "RESULT: UNKNOWN - no prefix references found"; exit 1
