#!/bin/zsh
# recompiles groupA (into target/classes) and the arena
export PATH=/opt/homebrew/opt/openjdk/bin:$PATH
cd /Users/halit/Desktop/statistics/TAG
CP="target/classes:$(cat cp.txt)"
rm -rf target/classes/groupA
javac -nowarn -d target/classes -cp "$CP" src/main/java/groupA/*.java || exit 1
javac -nowarn -d arena/classes -cp "$CP" arena/src/arena/*.java arena/src/refv2/*.java || exit 1
echo BUILD OK
