#!/usr/bin/env bash
# Builds upload_ready/ with the two group files for QMplus: groupA.zip (only the package directory groupA, without
# the framework) and the report PDF; upload_ready.zip holds the same folder as one download.
# The package gets the agent's Java files, the competition files, the submission README and the generative-AI
# prompts. The development files of TAG/src/main/java/groupA (experiment JSONs, per-game results, development
# README) stay in the repository.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="$ROOT/TAG/src/main/java/groupA"
OUT="$ROOT/upload_ready"
STAGE="$(mktemp -d)"
PKG="$STAGE/groupA"

mkdir -p "$PKG/json/competition" "$PKG/genai"
cp "$SRC"/*.java "$PKG/"
cp "$ROOT/groupA_code/submission/README.md" "$PKG/README.md"
cp "$SRC/GENAI_PROMPTS.md" "$PKG/"
cp "$SRC"/genai/* "$PKG/genai/"
cp "$SRC/json/competition/SushiMCTS_1000ms.json" "$PKG/json/competition/"
cp "$SRC/json/competition/SushiMCTS_500ms.json" "$PKG/json/"

rm -rf "$OUT" "$ROOT/upload_ready.zip"
mkdir -p "$OUT"
(cd "$STAGE" && zip -qrX "$OUT/groupA.zip" groupA)
cp "$ROOT/report/ECS7032P_Assignment1_Report_GroupA.pdf" "$OUT/"
(cd "$ROOT" && zip -qrX upload_ready.zip upload_ready)
rm -rf "$STAGE"

unzip -l "$OUT/groupA.zip"
ls -l "$OUT" "$ROOT/upload_ready.zip"
