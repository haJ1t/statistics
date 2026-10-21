#!/bin/zsh
# Pauses the running arena games (SIGSTOP; CPU-time budgets do not advance while stopped), measures the wall-clock and
# CPU time of every decision of the two competition files on the otherwise idle machine, then resumes the games.
export PATH=/opt/homebrew/opt/openjdk/bin:$PATH
cd /Users/halit/Desktop/statistics/TAG
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
probe 3 SushiMCTS_1000ms.json b900 1000 > /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp1.txt &
probe 4 SushiMCTS_1000ms.json b900 1000 > /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp2.txt &
probe 3 SushiMCTS_500ms.json b500 500 > /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp3.txt &
probe 4 SushiMCTS_500ms.json b500 500 > /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp4.txt &
wait
cat /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp1.txt /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp2.txt /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp3.txt /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp4.txt >> $OUT
rm -f /private/tmp/claude-501/-Users-halit-Desktop-statistics/761f2ac5-4fae-4278-a393-11b2a1801d49/scratchpad/qp[1-4].txt
