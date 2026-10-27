#!/bin/bash
# usage: arena/v5/queue.sh QUEUEFILE   -- runs the specs of QUEUEFILE one after another (re-read after every run, so
# lines can be appended while it runs). A spec line: label players games seed focus opps [threads]; done lines get "#".
cd "$(dirname "$0")/../.."
Q=$1
export JOPTS=${JOPTS:-"-Xms11g -Xmx11g -Xmn8g -XX:SurvivorRatio=3 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC -XX:ParallelGCThreads=4"}
while true; do
  # wait while another arena JVM runs (one experiment at a time uses all cores and most of the memory)
  while pgrep -f "[a]rena\.Arena" > /dev/null; do sleep 10; done
  line=$(grep -v '^#' "$Q" | grep -v '^\s*$' | head -1)
  [ -z "$line" ] && { sleep 20; continue; }
  read -r L NP NG SEED FOCUS OPPS TH <<< "$line"
  sed -i "0,/^$L /s//#$L /" "$Q"
  echo "$(date +%H:%M:%S) start $L" >> arena/v5/results/queue.log
  arena/v5/run.sh players=$NP games=$NG threads=${TH:-4} seed=$SEED focus=$FOCUS opps=$OPPS label=$L out=arena/v5/results/$L.csv types=1 \
    > arena/v5/results/$L.log 2> arena/v5/results/$L.err
  echo "$(date +%H:%M:%S) done $L rc=$?" >> arena/v5/results/queue.log
done
