#!/bin/bash
# usage: arena/v5/probe.sh OUTFILE [games]  -- pauses running arena games (SIGSTOP; CPU-time budgets do not advance
# while stopped), measures the wall-clock and CPU time of every decision of the two competition files on the otherwise
# idle machine (arena.WallProbe, one table size at a time), then resumes the games (bash version of tools/quiet_probe.sh)
cd "$(dirname "$0")/../.."
OUT=$1; G=${2:-2}
CP="target/classes:arena/classes:$(cat cp.txt)"
resume() { pkill -CONT -f "^java.*arena\.Arena"; echo "$(date '+%H:%M:%S') resumed arena runs" >> $OUT; }
trap resume EXIT INT TERM
pkill -STOP -f "^java.*arena\.Arena"
echo "$(date '+%H:%M:%S') paused arena runs; probing on a quiet machine" > $OUT
probe() {   # players file opponent-dir limit
  java -Xms2g -Xmx2g -XX:+UseParallelGC -XX:ParallelGCThreads=2 -cp "$CP" arena.WallProbe players=$1 games=$G \
    focus=src/main/java/groupA/json/competition/$2 opps=arena/agents/$3/ref.json,arena/agents/$3/tagmcts.json seed=500 limit=$4 2>/dev/null \
    | grep -v JAVA_TOOL | sed "s|^|[$1p $2] |" >> $OUT
}
probe 3 SushiMCTS_1000ms.json b900 1000
probe 4 SushiMCTS_500ms.json b500 500
