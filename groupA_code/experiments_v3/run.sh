#!/bin/zsh
# usage: arena/run.sh key=value ...   (run from the TAG root)
# groupA classes are snapshotted per run, so rebuilding during a run cannot change a running experiment
# needs Java 21+ on the PATH
cd "$(dirname "$0")/.."
SNAPROOT=${TMPDIR:-/tmp}/arena-snap
mkdir -p $SNAPROOT
SNAP=$(mktemp -d $SNAPROOT/run.XXXXXX)
cp -R target/classes/groupA $SNAP/ && cp -R arena/classes/* $SNAP/
java ${=JOPTS:--Xms20g -Xmx20g -Xmn14g -XX:SurvivorRatio=4 -XX:+UseParallelGC} -cp "${SNAP}:target/classes:$(cat cp.txt)" arena.Arena "$@"
rc=$?
rm -rf $SNAP
exit $rc
