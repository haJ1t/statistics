#!/bin/zsh
# usage: arena/batch.sh LOGFILE "label|players|games|focus|opps" ...   (env: THREADS, SEED, EXTRA = more Arena args)
LOG=$1; shift
for spec in "$@"; do
  IFS='|' read -r label np ng focus opps <<< "$spec"
  arena/run.sh players=$np games=$ng threads=${THREADS:-16} seed=${SEED:-1} focus=$focus opps=$opps label=$label out=arena/results/$label.csv ${=EXTRA:-types=1} 2>arena/results/$label.err >> $LOG
done
echo "BATCH DONE" >> $LOG
