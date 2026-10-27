#!/bin/bash
# copies this folder (without runs in progress) and the new dev tool to groupA_code/experiments_v5 (run from anywhere)
cd "$(dirname "$0")/../../.."
DST=groupA_code/experiments_v5
rm -rf $DST && mkdir -p $DST/dev/groupA
cp -R TAG/arena/v5/. $DST/
rm -rf $DST/results/running
cp TAG/arena/dev/groupA/DenialDuel.java $DST/dev/groupA/
echo "synced to $DST"
