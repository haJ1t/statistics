#!/usr/bin/env bash
# Builds upload_ready/ with the two group files for QMplus: groupW.zip (only the package directory groupW, without
# the framework) and the report PDF; upload_ready.zip holds the same folder as one download.
# The package gets the agent's Java files, its JSON files (competition, 500 ms, 100 ms), the submission README and
# the generative-AI prompts. It is copied from the development package TAG/src/main/java/groupA and renamed to the
# group's letter (package lines and class names in the JSON files); the development files of that package
# (experiment JSONs, per-game results, development README) stay in the repository.
set -euo pipefail
PKGNAME="groupW"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="$ROOT/TAG/src/main/java/groupA"
REPORT="$ROOT/report/ECS7032P_Assignment1_Report_GroupW.pdf"
OUT="$ROOT/upload_ready"
STAGE="$(mktemp -d)"
PKG="$STAGE/$PKGNAME"

mkdir -p "$PKG/json/competition" "$PKG/genai"
cp "$SRC"/*.java "$PKG/"
cp "$ROOT/groupA_code/submission/README.md" "$PKG/README.md"
cp "$SRC/GENAI_PROMPTS.md" "$PKG/"
cp "$SRC"/genai/* "$PKG/genai/"
cp "$SRC/json/competition/SushiMCTS_1000ms.json" "$PKG/json/competition/"
cp "$SRC/json/competition/SushiMCTS_500ms.json" "$PKG/json/"
cp "$SRC/json/agents_v4/SushiMCTS_100ms.json" "$PKG/json/SushiMCTS_100ms.json"

# rename the package: "package groupA;" and the qualified class names (groupA.X) in code comments and JSON files
for f in "$PKG"/*.java "$PKG"/json/*.json "$PKG"/json/competition/*.json; do
    sed -i -e "s/^package groupA;/package $PKGNAME;/" -e "s/groupA\./$PKGNAME./g" "$f"
done
if grep -l "groupA" "$PKG"/*.java "$PKG"/json/*.json "$PKG"/json/competition/*.json "$PKG/README.md"; then
    echo "groupA is still mentioned in the files above" >&2
    exit 1
fi

rm -rf "$OUT" "$ROOT/upload_ready.zip"
mkdir -p "$OUT"
(cd "$STAGE" && zip -qrX "$OUT/$PKGNAME.zip" "$PKGNAME")
cp "$REPORT" "$OUT/"
(cd "$ROOT" && zip -qrX upload_ready.zip upload_ready)
rm -rf "$STAGE"

unzip -l "$OUT/$PKGNAME.zip"
ls -l "$OUT" "$ROOT/upload_ready.zip"
