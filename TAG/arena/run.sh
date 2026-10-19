#!/bin/zsh
# usage: arena/run.sh key=value ...   (run from the TAG root)
# groupA classes are snapshotted per run, so rebuilding during a run cannot change a running experiment
export PATH=/opt/homebrew/opt/openjdk/bin:$PATH
cd /Users/halit/Desktop/statistics/TAG
SNAPROOT=/private/tmp/claude-501/-Users-halit--claude-spares-spare-xaEubt/0277c11a-b057-4d85-84f7-91462f4427de/scratchpad/snap
mkdir -p $SNAPROOT
SNAP=$(mktemp -d $SNAPROOT/run.XXXXXX)
cp -R target/classes/groupA $SNAP/ && cp -R arena/classes/* $SNAP/
java ${=JOPTS:--Xms20g -Xmx20g -Xmn14g -XX:SurvivorRatio=4 -XX:+UseParallelGC} -cp "${SNAP}:target/classes:$(cat cp.txt)" arena.Arena "$@"
rc=$?
rm -rf $SNAP
exit $rc
