#!/bin/zsh
# Pauses the running arena games (SIGSTOP; CPU-time budgets do not advance while stopped), measures the wall-clock and
# CPU time of every decision of the two competition files on the otherwise idle machine, then resumes the games.
# needs Java 21+ on the PATH
cd "$(dirname "$0")/../.."
OUT=arena/results/probe_quiet.txt
CP="target/classes:arena/classes:$(cat cp.txt)"
resume() { pkill -CONT -f "^java.*arena\.Arena"; echo "$(date '+%H:%M:%S') resumed arena runs" >> $OUT; }
trap resume EXIT INT TERM
pkill -STOP -f "^java.*arena\.Arena"
echo "$(date '+%H:%M:%S') paused arena runs; probing on a quiet machine" > $OUT
sleep 5
probe() {   # players file opponent-dir limit
  java -Xms2g -Xmx2g -XX:+UseParallelGC -XX:ParallelGCThreads=2 -cp "$CP" arena.WallProbe players=$1 games=4 \
    focus=arena/agents/comp/$2 opps=arena/agents/$3/ref.json,arena/agents/$3/tagmcts.json seed=500 limit=$4 2>/dev/null \
    | sed "s|^|[$1p $2] |"
}
probe 3 SushiMCTS_1000ms.json b900 1000 > ${TMPDIR:-/tmp}/qp1.txt &
probe 4 SushiMCTS_1000ms.json b900 1000 > ${TMPDIR:-/tmp}/qp2.txt &
probe 3 SushiMCTS_500ms.json b500 500 > ${TMPDIR:-/tmp}/qp3.txt &
probe 4 SushiMCTS_500ms.json b500 500 > ${TMPDIR:-/tmp}/qp4.txt &
wait
cat ${TMPDIR:-/tmp}/qp1.txt ${TMPDIR:-/tmp}/qp2.txt ${TMPDIR:-/tmp}/qp3.txt ${TMPDIR:-/tmp}/qp4.txt >> $OUT
rm -f ${TMPDIR:-/tmp}/qp[1-4].txt
