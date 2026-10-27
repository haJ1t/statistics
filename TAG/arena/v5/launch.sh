#!/bin/bash
# usage: arena/v5/launch.sh label players games seed focus opps [threads]  -> runs in the background, results in arena/v5/results
cd "$(dirname "$0")/../.."
L=$1; NP=$2; NG=$3; SEED=$4; FOCUS=$5; OPPS=$6; TH=${7:-4}
export JOPTS=${JOPTS:-"-Xms11g -Xmx11g -Xmn8g -XX:SurvivorRatio=3 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC -XX:ParallelGCThreads=4"}
nohup arena/v5/run.sh players=$NP games=$NG threads=$TH seed=$SEED focus=$FOCUS opps=$OPPS label=$L out=arena/v5/results/$L.csv types=1 \
  > arena/v5/results/$L.log 2> arena/v5/results/$L.err &
echo "started $L pid $!"
