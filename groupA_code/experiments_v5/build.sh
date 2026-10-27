#!/bin/bash
# recompiles groupA (into target/classes), the arena and the dev tools (bash version of arena/build.sh)
cd "$(dirname "$0")/../.."
CP="target/classes:$(cat cp.txt)"
rm -rf target/classes/groupA
javac -nowarn -d target/classes -cp "$CP" src/main/java/groupA/*.java || exit 1
mkdir -p arena/classes arena/dev/out
javac -nowarn -d arena/classes -cp "$CP" arena/src/arena/*.java arena/src/refv2/*.java || exit 1
if [ -d arena/dev/groupA ]; then
  javac -nowarn -d arena/dev/out -cp "target/classes:arena/classes:$(cat cp.txt)" arena/dev/groupA/*.java || exit 1
fi
echo BUILD OK
