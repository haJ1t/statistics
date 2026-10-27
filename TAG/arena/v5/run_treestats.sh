#!/bin/bash
# 4-player search diagnostics of the final 500 ms agent (dev tools of experiments_v4/dev): iterations per decision
# with and without F (Bench4), tree shape (TreeStats4) and decision agreement (Agree4); outputs in arena/v5/results
cd "$(dirname "$0")/../.."
CP="arena/dev/out:target/classes:arena/classes:$(cat cp.txt)"
J="java -Xms3g -Xmx3g -XX:+UseParallelGC -cp $CP"
R=arena/v5/results
$J groupA.Bench4 arena/agents/final/SushiMCTS_500_FL8.json arena/v5/agents/b500_4noF.json 2>/dev/null | grep -v JAVA_TOOL > $R/bench4.txt
$J groupA.TreeStats4 t500 arena/agents/final/SushiMCTS_500_FL8.json 2>/dev/null | grep -v JAVA_TOOL > $R/treestats4.txt
$J groupA.Agree4 arena/agents/final/SushiMCTS_500_FL8.json arena/agents/final/SushiMCTS_500_FL8.json arena/v5/agents/b500_4L16.json arena/v5/agents/b1000_4FL8.json 2>/dev/null | grep -v JAVA_TOOL > $R/agree4.txt
echo TREESTATS_DONE
