#!/bin/zsh
# Pauses the arena JVMs (SIGSTOP) when the Mac runs on battery at or below THRESHOLD %, resumes them on AC power.
# CPU-time budgets do not advance while a process is stopped, so paused games are not affected.
THRESHOLD=${THRESHOLD:-25}
LOG=/Users/halit/Desktop/statistics/TAG/arena/results/battery_guard.log
paused=0
while true; do
  b=$(pmset -g batt)
  pct=$(echo "$b" | grep -o '[0-9]*%' | head -1 | tr -d '%')
  if echo "$b" | grep -q "Battery Power"; then
    if [ $paused -eq 0 ] && [ "${pct:-100}" -le $THRESHOLD ]; then
      pkill -STOP -f "^java.*arena\.(Arena|WallProbe)"; paused=1
      echo "$(date '+%H:%M:%S') PAUSED arena runs (battery ${pct}%)" >> $LOG
    fi
  else
    if [ $paused -eq 1 ]; then
      pkill -CONT -f "^java.*arena\.(Arena|WallProbe)"; paused=0
      echo "$(date '+%H:%M:%S') RESUMED arena runs (AC power, ${pct}%)" >> $LOG
    fi
  fi
  sleep 30
done
