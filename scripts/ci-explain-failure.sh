#!/usr/bin/env bash
# Turns a Gradle log into GitHub annotations + a job summary so failures are readable.
LOG="${1:-build.log}"
[ -f "$LOG" ] || { echo "::error::No build log found"; exit 0; }
{
  echo "### Build failed"
  echo '```'
  grep -nE "error:|FAILED|What went wrong|Execution failed|Could not |cannot find symbol|AAPT:|Wrong checksum|Unsupported" "$LOG" | head -40
  echo '```'
} | tee -a "${GITHUB_STEP_SUMMARY:-/dev/null}"
# Java compiler errors as file annotations
grep -E "^/.*\.java:[0-9]+: error:" "$LOG" | head -20 | while IFS= read -r l; do
  f="${l%%:*}"; rest="${l#*:}"; n="${rest%%:*}"; m="${rest#*: error: }"
  echo "::error file=${f#$PWD/},line=$n::$m"
done
grep -m5 -E "What went wrong" -A3 "$LOG" | sed 's/^/::error::/' | head -10
exit 0
