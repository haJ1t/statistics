#!/bin/bash
# copies the per-game results and arena logs of every run (groupA_code/experiments_v3-v5/results) into the code
# package (groupA/results/v3, v4, v5) and its copy in groupA_code; run after sync_to_groupA_code.sh
cd "$(dirname "$0")/../../.."
for P in TAG/src/main/java/groupA/results groupA_code/groupA/results; do
  rm -rf $P/v3 $P/v4 $P/v5 && mkdir -p $P/v3 $P/v4 $P/v5
  for v in 3 4 5; do
    for f in groupA_code/experiments_v$v/results/*.csv groupA_code/experiments_v$v/results/*.log \
             groupA_code/experiments_v$v/results/*.txt; do
      [ -f "$f" ] && grep -v "^Picked up JAVA_TOOL_OPTIONS" "$f" > $P/v$v/$(basename "$f")
    done
  done
done
cp TAG/src/main/java/groupA/results/README.md groupA_code/groupA/results/README.md
echo "results copied into the code package"
