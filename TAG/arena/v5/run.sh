#!/bin/bash
# usage: arena/v5/run.sh key=value ...   (bash version of arena/run.sh; groupA classes are snapshotted per run)
cd "$(dirname "$0")/../.."
SNAPROOT=${TMPDIR:-/tmp}/arena-snap
mkdir -p $SNAPROOT
SNAP=$(mktemp -d $SNAPROOT/run.XXXXXX)
cp -R target/classes/groupA $SNAP/ && cp -R arena/classes/* $SNAP/
java ${JOPTS:--Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2} -cp "${SNAP}:target/classes:$(cat cp.txt)" arena.Arena "$@"
rc=$?
rm -rf $SNAP
exit $rc
