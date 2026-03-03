#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
GRADLE_PROPS_PATH="$PROJECT_ROOT/gradle.properties"

if [ ! -f "$GRADLE_PROPS_PATH" ]; then
  echo "gradle.properties not found: $GRADLE_PROPS_PATH" >&2
  exit 1
fi

TMP_PATH="$GRADLE_PROPS_PATH.tmp.$$"

awk '
BEGIN { updated = 0 }
/^useLocalZuiComposeDesktop=/ {
  print "useLocalZuiComposeDesktop=true"
  updated = 1
  next
}
{ print }
END {
  if (!updated) {
    print "useLocalZuiComposeDesktop=true"
  }
}
' "$GRADLE_PROPS_PATH" > "$TMP_PATH"

mv "$TMP_PATH" "$GRADLE_PROPS_PATH"
echo "Switched to local zui-compose-desktop (useLocalZuiComposeDesktop=true)."
